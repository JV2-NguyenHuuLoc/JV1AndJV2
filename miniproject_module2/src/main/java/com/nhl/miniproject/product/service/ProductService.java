package com.nhl.miniproject.product.service;


import com.nhl.miniproject.product.dto.ProductListResponse;
import com.nhl.miniproject.product.dto.ProductPage;
import com.nhl.miniproject.product.model.Product;
import com.nhl.miniproject.product.model.ProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service                    // Đăng ký Class này vào kho Spring là Trạm xử lý nghiệp vụ lõi
@RequiredArgsConstructor    // Kích hoạt cỗ máy tiêm tự động RestTemplate bằng Constructor
public class ProductService {

    // 1. TIÊM CỎ MÁY KẾT NỐI MẠNG REST_TEMPLATE VÀO CĂN CỨ ĐỂ SỬ DỤNG
    private final RestTemplate restTemplate;

    // 2. LẤY TỌA ĐỘ GỐC API QUỐC TẾT TRANG DummyJSON/Products
    private final String BASE_URL = "https://dummyjson.com/products";

    // 3. HÀM TÌM KIẾM TRONG DS PRODUCTS THEO CÁC BỘ LỌC --> TRUY XUẤT DS PRODUCTS TRONG 1 PAGE
    /**
     * Thuật toán tối cao: Spring Proxy kết nối DummyJSON kết hợp Tìm kiếm + Phân trang + Lọc nhóm + Sắp xếp
     */
    public ProductPage findPage(String query, String category, String sortBy, String order, int page, int size){
        // 1. Quy đổi trục tọa độ page sang limit và skip cho API quốc tế
        int limit = size;
        int skip = (page - 1) * size;

        // 2. Khởi tạo xây dựng đường dẫn URL động (Dynamic URL Builder) từ đường dẫn gốc của trang DummyJSON/Products
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        // 3.1. Trường hợp 1: Nếu người dùng thực hiện Tìm kiếm với từ khóa "query"
        if(query != null && !query.isBlank()){
            urlBuilder.append("/search?q=").append(query.trim());
            urlBuilder.append("&limit=").append(limit).append("&skip=").append(skip);
        }

        // 3.2. Trường hợp 2: Nếu người dùng thực hiện Lọc theo nhóm sản phẩm (Category)
        else if (category !=null && !category.isBlank()) {
            urlBuilder.append("/category/").append(category.trim());
            urlBuilder.append("?limit=").append(limit).append("&skip=").append(skip);
        }

        // 3.3. Trường hợp 3: Trang chủ danh sách thông thường, không lọc
        else {
            urlBuilder.append("?limit=").append(limit).append("&skip=").append(skip);
        }

        // 4. Tích hợp thuật toán Sắp xếp động (Sort) nếu người dùng chọn tiêu chí sắp xếp
        if(sortBy != null && !sortBy.isBlank()){
            urlBuilder.append("&sortBy=").append(sortBy.trim());
            urlBuilder.append("&order=").append((order != null && !order.isBlank()) ? order.trim() : "asc");
        }

        // 5. Trang đường dẫn động cuối cùng
        String finalUrl = urlBuilder.toString();

        // 6. Phóng mũi khoan ProductListResponse DummyJSON/Products bốc dữ liệu thô về bộ nhớ RAM
        ProductListResponse response = restTemplate.getForObject(finalUrl,ProductListResponse.class);

        // 7. Khởi tạo chiếc hộp DTO để bốc tách, phẫu thuật lấy dữ liệu sạch
        ProductPage productPage = new ProductPage();        // Khởi tạo 1 page product

        // 8. Kiểm tra dữ liệu thu về khác rỗng và phải là product
        if(response != null && response.getProducts() != null){
            productPage.setProducts(response.getProducts());
            productPage.setQuery(query);
            productPage.setCategory(category);
            productPage.setPage(page);
            productPage.setPageSize(size);
            productPage.setTotalItems(response.getTotal());
        }else {
            productPage.setProducts(new ArrayList<>());
            productPage.setTotalItems(0);
        }

        // 9. Kết thúc hàm
        return productPage;
    }

    // 4. HÀM PROXY LẤY THÔNG TIN CHI TIẾT 1 SẢN PHẨM THEO ID (BỌC TRONG OTIONAL AN TOÀN)
    public Optional<Product> findById(Long id){
        // 1. Khai báo đường dẫn đến sản phẩm ID
        String url = BASE_URL + "/" + id;

        // 2. Tìm product theo Id
        try{
            Product p = restTemplate.getForObject(url, Product.class);
            return Optional.ofNullable(p);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // 5. HÀM PHÁT LỆNH POST TẠO SẢN PHẨM MỚI SANG HỆ THỐNG DUMMYJSON
    public Product createProduct(ProductRequest request){
        String url = BASE_URL + "/add";
        return restTemplate.postForObject(url,request,Product.class);
    }

    // 6. HÀM PROXY PHÁT LỆNH PÚT CẬP NHẬT ĐÈ THÔNG TIN SẢN PHẨM SAN HỆ THỐNG DummyJSON THEO ID
    public Product updateProduct(Long id, ProductRequest request){
        // 1. Khai báo đường dẫn đến sản phẩm ID
        String url = BASE_URL + "/" + id;

        // 2. Lấy dữ liệu thô product từ Dummyjson theo ID gán vào request
        restTemplate.put(url,request);

        // 3. Tạo Product mới với dữ liệu sạch
        Product p = new Product();
        p.setId(id);
        p.setTitle(request.getTitle());
        p.setDescription(request.getDescription());
        p.setPrice(request.getPrice());
        p.setCategory(request.getCategory());
        p.setThumbnail(request.getThumbnail());
        p.setBrand(request.getBrand());

        // 4. Kết thúc hàm
        return p;
    }

    // 7. HÀM PROXY PHÁT LỆNH DELETE CỨNG SẢN PHẨM THEO ID KHỎI HỆ THỐNG
    public void deleteProduct(Long id){
        // 1. Khai báo đường dẫn đến sản phẩm ID
        String url = BASE_URL + "/" + id;

        // 2. Xóa sản phẩm
        restTemplate.delete(url);
    }

    // 8. HÀM CHỨC NĂNG 2: PROXY LẤY TOÀN BỘ DANH SÁCH NHÓM SẢN PHẨM THEO CATEGORY TỪ API QUỐC TẾ
    public List<String> findAllCategories(){
        // 1. Khai báo đường dẫn đến sản phẩm ID
        String url = BASE_URL + "/categories";

        // 2. Máy chủ DummyJSON trả về một mảng các chuỗi văn bản thô đại diện cho tên nhóm
        try{
            return restTemplate.getForObject(url,List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
