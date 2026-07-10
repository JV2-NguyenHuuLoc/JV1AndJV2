package com.nhl.baitap3_spring_mongodb_api_thymeleaf.controller.web;

import com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto.MovieDto;
import com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto.MovieFormDto;
import com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto.PagedListView;
import com.nhl.baitap3_spring_mongodb_api_thymeleaf.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller     // Khóa chặt nhãn trả về View giao diện HTML
@RequestMapping("/movies")  // Định vị đường dẫn trục chính của trang Web phim
@RequiredArgsConstructor    // Tiêm trái tim Service vào RAM
public class MovieViewController {

    // 1. KHAI BÁO DỊCH VỤ SERVICE
    private final MovieService movieService;

    // 2. HÀM 1: HIỂN THỊ DANH SÁCH + TÌM KIẾM + PHÂN TRANG HTML
    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ){
        // 1. Cố định cấu hình phân trang hệ thống: 5 phim/trang, sắp xếp theo tên phim tăng dần
        int pageSize = 5;
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, Sort.by("title").ascending());

        // 2. Gọi Service bốc dữ liệu đóng hòm vào hộp Adapter PagedListView
        PagedListView<MovieDto> listView = movieService.findPage(keyword,pageable);

        // 3. Nhét 3 nguyên liệu vào xe tải Model để bắn ra cho Front-end Thymeleaf bốc dỡ
        // 3.1. Khởi tạo Metadata phân trang
        model.addAttribute("moviePage", listView.getPagination());

        // 3.2. Truyền Danh sách thịt dữ liệu 5 phim
        model.addAttribute("movies", listView.getContent());

        // 3.3. Giữ lại từ khóa tìm kiếm
        model.addAttribute("keyword", listView.getKeyword());

        // 4. Trả về đường dẫn vật lý đến file HTML: src/main/resources/templates/movies/list.html
        return "movies/list";
    }

    // 3. HÀM 2: TRUY VẾT VÀ HIỂN THỊ CHI TIẾT 1 BỘ PHIM THEO ID TỪ DATABASE
    @GetMapping("/{id}")
    public String getDetail(@PathVariable String id, Model model){
        // 1. Gọi hàm Getter từ Service bốc phim theo ID từ Database và chuyển sang DTO
        // (do DTO giống 100% Mode; Trường hợp khác phải gọi thêm hàm chuyển đổi từ Model --> DTOl)
        MovieDto movieDto = movieService.getById(id);

        // 2. Nhét phim sạch vào xe tải Model với đúng cái nhãn nhúng "movie" để HTML nhận diện
        model.addAttribute("movie", movieDto);

        // 3. Kết thúc hàm: Trả về đường dẫn đến file: src/main/resources/templates/movies/detail.html
        return "movies/detail";
    }

    // 4. HÀM 3: TẠO FORM HIỂN THỊ MÀN HÌNH TẠO MỚI PHIM
    // GHI 1 PHIM TỪ FORM XUỐNG DATABASE VÀ XUẤT KẾT QUẢ THÔNG BÁO RA HTML
    @GetMapping("/new")
    public String showCreateForm(Model model){
        // 1. Gài biến trạng thái: false nghĩa là TRANG THÊM MỚI (Không phải trang sửa)
        model.addAttribute("isEdit", false);

        // 2. Khởi tạo một chiếc hộp trống MovieFormDto nhét vào nhãn "movieForm"
        // để làm khung xương bắt buộc để các thẻ th:field ngoài HTML không bị mù thông tin
        model.addAttribute("movieForm",new MovieFormDto());

        // 3. Kết thúc hàm: Trả về tấm bản đồ dẫn đến file: src/main/resources/templates/movies/form.html
        return "movies/form";
    }

    // 5. HÀM 4: TIẾN TRÌNH THỰC HIỆN TẠO MỚI PHIM (POST /movies)
    @PostMapping
    public String createMovie(
            @Valid @ModelAttribute("movieForm") MovieFormDto movieFormDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ){
        // 1. Kiểm tra dự liệu nhập bẩn: Nếu Client nhập bẩn vi phạm các nhãn @NotBlank, @NotNull, @Min, ..
        if(bindingResult.hasErrors()){  // Nếu có lỗi nhập dữ liệu
            // 2. Ép giữ nguyên RAM trạng thái, nạp lại nhãn isEdit = false
            model.addAttribute("isEdit", false);

            // 3. Render lại Form nhập liệu để Thymeleaf bung các dòng thông báo lỗi th:errors
            return "movies/form";
        }

        // 4. Dữ liệu nhập an toàn: Gọi Service kích nổ hàm tạo mới và băm chuỗi thể loại phim
        MovieDto created = movieService.createFromForm(movieFormDto);

        // 5. Gâm chặt thông báo Flash chớp nhoáng trên RAM đúng một nhịp lật trang
        redirectAttributes.addFlashAttribute("message",
                "HẠ TẦNG: ĐÃ TẠO MỚI PHIM XUỐNG Ổ CỨNG THÀNH CÔNG!");

        // 6. Kết thúc hàm: Chuyển hướng về chính trang danh sách phim
        return "redirect:/movies";
    }

    // 6. HÀM 5: TẠO FORM HIỂN THỊ MÀN HÌNH SỬA PHIM (GET /movies/{id}/edit)
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model){
        // 1. Gọi hàm Getter từ Service bốc phim theo ID từ Database và chuyển sang DTO
        // (do DTO giống 100% Mode; Trường hợp khác phải gọi thêm hàm chuyển đổi từ Model --> DTOl)
        MovieDto movieDto = movieService.getById(id);

        // 2. Khởi tạo Form edit
        // Đổi từ field genre từ mảng List<String> sang chuỗi String phẳng sạch
        MovieFormDto editForm = new MovieFormDto();

        // 3. Lấy thông tin phim lên editForm cho người dùng xem
        editForm.setId(movieDto.getId());
        editForm.setTitle(movieDto.getTitle());
        editForm.setYear(movieDto.getYear());
        editForm.setDirector(movieDto.getDirector());
        editForm.setRating(movieDto.getRating());

        // 4. Chuyển field genre từ List<String> thành chuỗi văn bản cách nhau bởi dấu phẩy
        // để nhét vào ô nhập HTML
        if(movieDto.getGenre() != null){
            editForm.setGenreText(String.join(",",movieDto.getGenre()));
        }

        // 5. Nạp 2 nguyên liệu vào xe tải Model bắn ra HTML
        model.addAttribute("isEdit", true);   // Nhãn thùng chứa
        model.addAttribute("movieForm", editForm);      // Dữ liệu thịt

        // 6. Kết thúc hàm: Chuyển về trang form nhập liệu
        return "movies/form";
    }

    // 7. HÀM 6: TIẾN TRÌNH THỰC HIỆN CẬP NHẬT PHIM (POST /movies/{id})
    @PostMapping("/{id}")
    public String updateMovie(
            @PathVariable String id,
            @Valid @ModelAttribute("movieForm") MovieFormDto movieFormDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ){
        // 1. Kiểm tra dữ liệu nhập bẩn: Nếu sửa dữ liệu bị lỗi cú pháp validation
        if(bindingResult.hasErrors()){
            // 2. Giữ nguyên công tắc màn hình sửa
            model.addAttribute("isEdit", true);

            // 3. Thoát hàm và Render lại Form để hiện chữ báo lỗi đỏ
            return "movies/form";
        }

        // 4. Dữ liệu nhập sạch: Gọi Service ép ghi đè thông tin mới xuống Database
        movieService.updateFromForm(id, movieFormDto);

        // 5. Gài thông báo Flash chớp nhoáng
        redirectAttributes.addFlashAttribute("message",
                "HẠ TẦNG: ĐÃ CẬP NHẬT THÔNG TIN PHIM XUỐNG Ổ CỨNG THÀNH CÔNG!");

        // 6. Kết thúc hàm: Chuyển hướng về chính trang xem chi tiết bộ phim vừa sửa
        return "redirect:/movies/" + id;
    }

    // 8. HÀM 7: TIẾN TRÌNH THỰC HIỆN XÓA PHIM TRÊN GIAO DIỆN (POST /movies/{id}/delete)
    @PostMapping("/{id}/delete")
    public String deleteMovie(
            @PathVariable String id,
            RedirectAttributes redirectAttributes
    ){
        // 1. Gọi khí tài delete từ Service để xóa cứng bản ghi khỏi Database
        movieService.delete(id);

        // 2. Gài thông báo Flash chớp nhoáng báo về cho trang danh sách
        redirectAttributes.addFlashAttribute("message",
                "HẠ TẦNG: ĐÃ TIÊU DIỆT VÀ XÓA SỔ PHIM KHỎI HỆ THỐNG VĨNH VIỄN!");

        // 3. Kết thúc hàm: Chuyển hướng về trang danh sách tổng
        return "redirect:/movies";
    }
}
