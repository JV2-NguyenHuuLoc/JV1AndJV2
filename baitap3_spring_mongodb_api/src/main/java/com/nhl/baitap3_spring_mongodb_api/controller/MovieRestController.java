package com.nhl.baitap3_spring_mongodb_api.controller;

import com.nhl.baitap3_spring_mongodb_api.dto.MovieDto;
import com.nhl.baitap3_spring_mongodb_api.dto.PagedResponse;
import com.nhl.baitap3_spring_mongodb_api.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController     // Cấu hình chốt chặn ép toàn bộ dữ liệu đầu ra thành chuỗi JSON
@RequiredArgsConstructor    // Tự động tiêm trái tim MovieService vào RAM của Controller
@RequestMapping("/v1/api/movies")   // Định vị trục đường dẫn chính của API
public class MovieRestController {

    // 1. KHAI BÁO BIẾN TỔNG TỬ DỊCH VỤ SERVICE
    private final MovieService movieService;

    // 2. HÀM 1: LẤY TOÀN BỘ DANH SÁCH PHIM
    @GetMapping
    public ResponseEntity<List<MovieDto>> getAll(){
        // Gọi khí tài getAllMovies từ Service và bọc trong ResponseEntity Status 200 OK
        return ResponseEntity.ok(movieService.getAllMovie());
    }

    // 3. HÀM 2: TRUY VẾT VÀ ĐỌC 1 BỘ PHIM ĐÍCH DANH THEO ID TỪ DATABASE
    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getById(@PathVariable String id){
        // Gọi khí tài getById từ Service và bọc trong ResponseEntity Status 200 OK
        return ResponseEntity.ok(movieService.getById(id));
    }

    // 4. HÀM 3: TÌM KIẾM DS PHIM THEO KEYWORD + KẾT HỢP PHÂN TRANG
    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<MovieDto>> getPagedMovies(
            @RequestParam(required = false) String keyword,     // Chuỗi tìm kiếm
            @RequestParam(defaultValue = "0") int page,         // Trang hiện hành, mặc định = 0
            @RequestParam(defaultValue = "5") int size          // Số items/page, mặc định là 5
    ){
        // 1. Khởi tạo nhanh đối tượng phân trang trên RAM từ thông số Client gửi lên
        Pageable pageable = PageRequest.of(page, size);

        // 2. Gọi Service băm nhỏ dữ liệu từ database và trả PagedResponse để chuẩn bị xuất cho API
        return ResponseEntity.ok(movieService.findPageAsPagedResponse(keyword,pageable));
    }

    // 5. HÀM 4: GHI 1 PHIM TỪ CLIENT (POSTMAN) XUỐNG DATABASE VÀ NHẬN KẾT QUẢ PHIM (ĐÃ GHI) ĐỂ XUẤT RA API
    @PostMapping
    public ResponseEntity<MovieDto> create(@Valid @RequestBody MovieDto movieDto){
        // 1. Khởi tạo lấy phim từ database và chuyển sang dạng MovieDto
        MovieDto createdMovieDto = movieService.create(movieDto);

        // 2. Trả về dữ liệu kèm mã trạng thái 201 CREATED hiển hách
        return new ResponseEntity<>(createdMovieDto, HttpStatus.CREATED);
    }

    // 6. HÀM CẬP NHẬT THÔNG TIN 1 PHIM
    @PutMapping("/{id}")
    public ResponseEntity<MovieDto> update(
            @PathVariable String id,                // ID của phim cần Update
            @Valid @RequestBody MovieDto movieDto   // Kích hoạt công cự chuyển phim từ JSON text sang Object để xử lý
    ){
        return ResponseEntity.ok(movieService.update(id,movieDto));
    }

    // 7. HÀM XÓA CỨNG PHIM VÀ TRẢ MÃ 204 KHÔNG CÓ NỘI DUNG (KHÔNG TRẢ RA KẾT QUẢ, CHỈ XUẤT MÃ THÔNG BÁO)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id){
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
