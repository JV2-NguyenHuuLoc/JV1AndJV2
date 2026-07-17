package com.nhl.baitap4_spring_mongodb_thymeleaf_import.controller;

import com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto.ImportResultDto;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto.PagedListView;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto.RestaurantDto;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto.RestaurantFormDto;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller     // Trạm điều phối Web giao diện đồ họa mẫu Thymeleaf View
@RequestMapping("/restaurants")     // Định vị trục đường ống dẫn tổng của căn phòng ban
@RequiredArgsConstructor    // Khí tài Lombok tự động tiêm cứng quả tim Service lên bộ nhớ RAM
public class RestaurantViewController {
    // 1. KHAI BÁO DỊCH VỤ TỪ TẦNG SERVICE
    private final RestaurantService restaurantService;

    // 2. Bốc hằng số Page Size = 10 đồng bộ từ file application.properties vào biến pageSize
    @Value("${app.restaurants.page-size:10}")    // org.springframework.beans.factory.annotation.Value;
    private int pageSize;

    // 3. HÀM 1: XUẤT MÀN HÌNH CHỌN FILE ĐỂ NẠP LÔ (GET /upload)
    @GetMapping("/upload")
    public String showUploadPage(){
        return "restaurants/upload";    // Trả về tên file HTML templates/restaurants/upload
    }

    // 4. HÀM 2: XỬ LÝ BĂM NHỎ FILE LÔ THEO NHỊP 10 DÒNG (POST /upload)
    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file, Model model){
        // 1. Gọi hàm handleImport từ service để import dữ liệu theo lô (batch)
        ImportResultDto resultImport = restaurantService.handleImport(file);

        // 2. Lấy thông báo kết quả import file --> Chuyển vào Model để xuất ra giao diện HTML
        model.addAttribute("message", resultImport.message());

        // 3. Lấy số dòng dữ liệu import thành công --> Chuyển vào Model để xuất ra giao diện HTML
        if(resultImport.success()){
            model.addAttribute("importCount", resultImport.importCount());
        }

        return "restaurants/upload";    // Trả về tên file HTML templates/restaurants/upload
    }

    // =========================================================================
    // 5. HÀM 3 NÂNG CẤP TỐI CAO: XUẤT DANH SÁCH + TÌM KIẾM KÉP [QUẬN + TÊN] + PHÂN TRANG + SORT ĐA THUỘC TÍNH (GET)
    // =========================================================================
    @GetMapping
    public String listRestaurants(
            @RequestParam(required = false) String borough,     // Keyword tìm kiếm lọc theo field quận hành chính
            @RequestParam(required = false) String name,        // TIÊM MỚI: Keyword tìm kiếm đích danh theo Tên nhà hàng
            @RequestParam(defaultValue = "0") int page,         // Trang hiện hành hệ 0-indexed, mặc định = 0
            @RequestParam(defaultValue = "name") String sortBy, // Sắp xếp, mặc định theo field tên Nhà hàng
            @RequestParam(defaultValue = "asc") String dir,     // Kiểu sắp xếp, mặc định tăng dần
            Model model     // Công cụ xe tải chứa thịt dữ liệu để bắn thông mạch ra ngoài đồ họa HTML
    ){
        // 1. Thiết lập cấu trúc cấu chỉ mục sắp xếp đa thuộc tính nối đuôi nhau: default là "name + cuisine"
        Sort sort = dir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending().and(Sort.by("cuisine").ascending())
                : Sort.by(sortBy).ascending().and(Sort.by("cuisine").ascending());

        // 2. Đúc chiếc khuôn cấu hình phân trang hệ thống 0-indexed đẩy xuống tầng đĩa cứng
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, sort);

        // 3. VÁ LỖI CÚ PHÁP: Gọi quả tim Service băm nhỏ dữ liệu kẹp thêm biến tham số name đầu vào
        PagedListView<RestaurantDto> listView = restaurantService.findPage(borough, name, pageable, sortBy, dir);

        // 4. Nạp toàn bộ dữ liệu vào Model để chuyển ra giao diện HTML
        model.addAttribute("list", listView.getContent());      // 4.1. Danh sách Restaurant dạng DTO sạch sau lột vỏ
        model.addAttribute("borough", listView.getBorough());    // 4.2. Găm từ khóa lọc quận để giữ trạng thái lật trang URL
        model.addAttribute("name", listView.getName());          // TIÊM MỚI: Găm từ khóa tìm kiếm Tên nhà hàng ra ngoài giao diện
        model.addAttribute("currentPage", listView.getPagination().getPage()); // 4.4. Truyền trang hiện hành ra giao diện HTML
        model.addAttribute("totalPages", listView.getPagination().getTotalPages()); // 4.5. Truyền tổng số trang hiện hành
        model.addAttribute("startPage", listView.getStartPage()); // 4.6. Truyền trang "tường trái" cửa sổ số di động
        model.addAttribute("endPage", listView.getEndPage());     // 4.7. Truyền trang "tường phải" cửa sổ số di động
        model.addAttribute("sortBy", listView.getSortBy());       // 4.8. Truyền field định vị cột sắp xếp ra ngoài UI
        model.addAttribute("dir", listView.getDir());             // 4.9. Truyền hướng tăng/giảm ra giao diện HTML

        return "restaurants/list";  // Trả về tên file HTML templates/restaurants/list
    }


    // 6. HÀM 4: MÀN HÌNH XUẤT BIỂU MẪU XEM CHI TIẾT RESTAURANT THEO RestaurantId (GET /detail/{id})
    @GetMapping("/detail/{id}")
    public String showRestaurantDetail(@PathVariable String id, Model model){
        // 1. Tìm Restauarant đưa lên FormDto, theo mã restauarantId (không dùng ObjectId)
        RestaurantFormDto restaurant = restaurantService.getFormByRestaurantId(id);

        // 2. Kiểm tra nếu tìm không có --> chuyển màn hình HTML sang trang báo lỗi trống
        if(restaurant == null){
            return "restaurants/not-found";     // Trả về tên file HTML templates/restaurants/not-found
        }

        // 3. Nếu tìm được --> chuyển dữ liệu sang màn hình HTML thông tin chi tiết restaurant
        model.addAttribute("restaurant", restaurant);
        return "restaurants/detail";        // Trả về tên file HTML templates/restaurants/detail
    }

    // 7. HÀM 5: CẬP NHẬT RESTAURANT TỪ BIỂU MẪU FormDto + KẾT HỢP CHUYỂN TRANG REDIRECT ĐỂ CHỐNG SAVE
    // CHỒNG DỮ LIỆU THEO ID (POST)
    @PostMapping("/detail/{id}")
    public String updateRestaurant(
            @PathVariable String id,        // ObjectId
            @Valid @ModelAttribute("restaurant") RestaurantFormDto updatedRestaurant, // jakarta.validation.Valid;
            BindingResult bindingResult,                // Gom lỗi
            RedirectAttributes redirectAttributes       // Thông báo chớp nhoáng
    ){
        // 1. Kiểm tra dữ liệu bẩn: trống ô nhập --> bắt lỗi trả màn hình HTML thông tin chi tiết đang update
        if(bindingResult.hasErrors()){
            return "restaurants/detail";
        }

        // 2. Đẩy thịt dữ liệu xuống tầng Service tiến hành Update
        RestaurantFormDto saved = restaurantService.updateDetail(id, updatedRestaurant);

        // 3. Kiểm tra kết quả cập nhật
        if(saved == null){
            redirectAttributes.addFlashAttribute("message",
                    "LỖI: Không tìm thấy nhà hàng mang mã số hành chính này!");
            return "redirect:/restaurants";     // Chuyển về HTML templates/restaurants/list
        }

        // 4. Mô hình bảo hiểm PRG tối cao. Gài hộp quà Flash chớp nhoáng lên RAM
        redirectAttributes.addFlashAttribute("message",
                "ĐÃ CẬP NHẬT DỮ LIỆU RESTAURANT THÀNH CÔNG VẸN TOÀN!");

        // 5. Kết thúc hàm: chuyển sang giao thức GET Restaurants/detail/{id} để khi người dùng nhấm F5
        // --> vẫn KHÔNG gây ra hiện tượng trùng lặp dữ liệu
        return "redirect:/restaurants/detail/" + id;    // Chuyển về HTML templates/restaurants/detail
    }
}
