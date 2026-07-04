package com.nhl.miniproject.product.controller;

import com.nhl.miniproject.product.dto.ProductPage;
import com.nhl.miniproject.product.model.Product;
import com.nhl.miniproject.product.model.ProductRequest;
import com.nhl.miniproject.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Optional;

@Controller                 // Bộ điều hướng hiển thị giao diện HTML Thymeleaf Bootstrap
@RequiredArgsConstructor    // Cỗ máy tiêm tự động bộ não ProductService của Lombok
public class ProductViewController {
    // 1. KHAI BÁO DỊCH VỤ GỌI HÀM TỪ SERVICE
    private final ProductService productService;

    // 2. HÀM 1: GET /HOME -> Hiển thị danh sách sản phẩm + Tìm kiếm + Phân trang + Lọc nhóm + Sắp xếp trộn lẫn động
    @GetMapping("/home")       // Đón nhận chính xác URL đường dẫn http://localhost:8081/home từ HomeController
    public String home(
            @RequestParam(value = "q", required = false) String query,      // Tham số tìm kiếm theo keyword
            @RequestParam(value = "category", required = false) String category,    // Tham số lọc theo nhóm sản phẩm
            @RequestParam(value = "sortBy",required = false) String sortBy,     // Tham số sắp xếp danh sách sau khi lọc
            @RequestParam(value = "order", required = false) String order,      // Tham số lọc theo đơn hàng
            @RequestParam(value = "page", defaultValue = "1") int page,     // Tham số trang hiện hành, mặc định = 1
            @RequestParam(value = "size", defaultValue = "10") int size,     // Tham số: số lượng hiển thị tróng 1 page
            Model model
        ){
        // 1. Gọi bộ não Proxy Service cào dữ liệu API quốc tế về nhào nặn phân trang
        ProductPage productPage = productService.findPage(query,category,sortBy,order,page,size);

        // 2. Sắp xếp dữ liệu lên xe đẩy Model theo đúng mác thùng hàng bằng cách:
        // Nạp đạn đầy đủ các mác biến độc lập để thông mạch với file home.html
        // 2.1. Khởi tạo thực thể trang hiển thị
        model.addAttribute("productPage", productPage);

        // 2.2. Truyền 2 tham số cho thực thể trang hiển thị: ProductPage
        model.addAttribute("products", productPage.getProducts());  // Danh sách 10 sản phẩm page hiện tại
        model.addAttribute("q", query);             // Tham số tìm kiém theo keyword
        model.addAttribute("category", category);   // Đồng bộ chữ ${category} trong home.html
        model.addAttribute("sortBy", sortBy);       // Đồng bộ chữ ${sortBy} trong home.html
        model.addAttribute("order", order);         // Đồng bộ chữ ${order} trong home.html
        model.addAttribute("size", size);           // Đồng bộ chữ ${size} trong home.html
        model.addAttribute("total", productPage.getTotalItems());   // Đồng bộ chữ ${total} trong home.html
        model.addAttribute("totalPages", productPage.getTotalPages());  // Đồng bộ chữ ${totalPages} trong home.html
        model.addAttribute("currentPage", productPage.getPage()); // Đồng bộ chữ ${current} trong home.html

        // 3. Kết thúc hàm
        return "home";      // Kích nổ hiển thị file templates/home.html
    }

    // 3. HÀM 2: GET /product_search -> Đón nhận từ khóa từ search.js để hiển thị trang kết quả tìm kiếm (Chức năng 4)
    @GetMapping("/product_search")      // Đón nhận chính xác trục tọa độ do file search.js bẻ lái sang
    public String productSearch(
            @RequestParam("keyword") String keyword,    // Đón tham số ?keyword=... từ trình duyệt
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ){
        // 1. Tái sử dụng bộ não Proxy Service, truyền từ khóa keyword vào vị trí tham số query
        ProductPage productPage = productService.findPage(keyword, null, sortBy, order, page, size);

        // 2. Xếp đạn dữ liệu lên xe đẩy Model đồng bộ 100% các mác nhãn với file product-search.html
        model.addAttribute("products", productPage.getProducts());
        model.addAttribute("q", keyword);
        model.addAttribute("total", productPage.getTotalItems());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("currentPage", productPage.getPage());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);
        model.addAttribute("size", size);

        // 3. Kết thúc hàm, gọi đến file giao diện vật lý templates/product-search.html
        return "product-search";
    }


    // 4. HÀM 3: GET /category -> Hiển thị danh sách tất cả các nhóm sản phẩm (Chức năng 2)
    @GetMapping("/category")        // Đón nhận chính xác URL đường dẫn http://localhost:8081/category
    public String category(Model model){
        // 1. Gọi Service cào mảng danh sách tên nhóm từ API quốc tế về
        List<String> categories = productService.findAllCategories();

        // 2. Nạp vào mác thùng hàng "categories" đẩy sang giao diện html
        model.addAttribute("categories", categories);

        // 3. Kết thúc hàm
        return "category";   // Kích nổ hiển thị file templates/category.html
    }

    // 5. HÀM 4: GET /product_detail -> Xem thông tin chi tiết một sản phẩm dựa trên tham số ?id=xxx (Chức năng 3)
    @GetMapping("/product_detail")  // Đón nhận chính xác URL đường dẫn http://localhost:8081/product_detail
    public String productDetail(@RequestParam("id") Long id, Model model){
        // 1. Triệu hồi chiếc hộp bảo hiểm từ tầng Service
        Optional<Product> productOptional = productService.findById(id);

        // 2. Chốt chặn bẫy lỗi: Nếu mở hộp ra thấy trống rỗng, điều hướng sang trang báo lỗi 404 thân thiện (Chức năng 12)
        if(productOptional.isEmpty()){
            return "error/404";     // Mở file templates/error/404.html
        }

        // 3. Nếu hàng tồn tại, bốc ra nạp lên xe đẩy với nhãn mác "product"
        model.addAttribute("product", productOptional.get());

        // 4. Kết thúc hàm
        return "product-detail";    // Mở file templates/product_detail.html
    }

    // 6. HÀM 5: GET /product_add -> Mở form thêm mới sản phẩm trống trơn (Chức năng 5)
    @GetMapping("/product_add")     // Đón nhận chính xác URL http://localhost:8081/product_add
    public String createForm(Model model){
        // 1. Nạp một vật thể Request trống lên xe đẩy để kích hoạt cỗ máy liên kết biểu mẫu Thymeleaf
        model.addAttribute("productRequest", new ProductRequest());

        // 2. Gọi đến file giao diện vật lý templates/product-add.html
        return "product-add";
    }

    // 7. HÀM 6: GET /product_edit -> Mở form sửa thông tin sản phẩm và đổ sẵn dữ liệu cũ dựa
    // trên tham số ?id=xxx (Chức năng 6)
    @GetMapping("/product_edit")     // Đón nhận chính xác URL http://localhost:8081/product_edit?id=xxx
    public String editForm(@RequestParam("id") Long id, Model model){
        // 1. Gọi Service cào thông tin cũ của sản phẩm từ API quốc tế về qua chiếc hộp bảo hiểm
        Optional<Product> productOptional = productService.findById(id);

        // 2. Chốt chặn phòng thủ: Nếu không tìm thấy ID sản phẩm, đẩy ngay sang trang lỗi 404 văn minh
        if(productOptional.isEmpty()){
            return "error/404";
        }

        // 3. Lấy dữ liệu cũ của Product
        Product oldProduct = productOptional.get();

        // 4. Kỹ thuật chuyển dịch dữ liệu sang hộp Request để Thymeleaf điền sẵn chữ cũ vào các ô textbox
        ProductRequest request = new ProductRequest();
        request.setTitle(oldProduct.getTitle());
        request.setDescription(oldProduct.getDescription());
        request.setPrice(oldProduct.getPrice());
        request.setCategory(oldProduct.getCategory());
        request.setThumbnail(oldProduct.getThumbnail());
        request.setBrand(oldProduct.getBrand());

        // 5. Nạp vật thể chỉnh sửa kèm mã số id cũ lên xe đẩy chở sang đất HTML
        // 5.1. Khởi tạo vật thể request
        model.addAttribute("productRequest", request);

        // NÂNG CẤP VÁ LỖI CHÍ MẠNG: Chất thêm thùng hàng "product" để khớp 100% với file product-edit.html
        model.addAttribute("product", oldProduct);

        // 5.2. Đẩy thêm ID ra để JavaScript biết đường chọc vào cửa khẩu API PUT sau này!
        model.addAttribute("productId", id);

        // 6. Gọi đến file giao diện vật lý templates/product-edit.html
        return "product-edit";
    }
}
