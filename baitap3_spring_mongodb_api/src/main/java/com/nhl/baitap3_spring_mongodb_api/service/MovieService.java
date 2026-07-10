package com.nhl.baitap3_spring_mongodb_api.service;

import com.nhl.baitap3_spring_mongodb_api.dto.MovieDto;
import com.nhl.baitap3_spring_mongodb_api.dto.PagedResponse;
import com.nhl.baitap3_spring_mongodb_api.exception.ResourceNotFoundException;
import com.nhl.baitap3_spring_mongodb_api.model.MovieModel;
import com.nhl.baitap3_spring_mongodb_api.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service                    // Kích nỗ nghiệp vụ tối cao Service
@RequiredArgsConstructor    // Tự động chọc ống dẫn Dependency Injection tiêm Repository vào Service
public class MovieService {
    // 1. KHAI BÁO BIẾN TOÀN CỤC TỪ REPOSITORY
    private final MovieRepository movieRepository;

    // 2. HÀM  ĐỌC TOÀN BỘ DS PHIM TỪ DƯỚI ĐĨA -> BỎ VÀO MODEL --> CHUYỂN THÀNH DS DTO ĐỂ TRẢ RA NGOÀI
    public List<MovieDto> getAllMovie(){
        return movieRepository.findAll()        // tìm tất cả ds phim có trong Database
                .stream()                       // biến 1 ds tỉnh (List) thành dòng chảy liên tục qua CPU
                .map(MovieDto::fromEntity)      // CPU chuyển từng item (trong List) từ dạng Model --> DTO
                .collect(Collectors.toList());  // Gom hết items DTO (vừa tạo) vào ds tỉnh (List) --> trả KQ
    }

    // 3. HÀM ĐỌC 1 PHIM THEO ID: DÙNG CHỐT CHẶN BẢO HIỂM OPTIONAL ĐỂ CHỐNG LỖI NullPointerException LÀM SẬP App
    public MovieDto getById(String id){
        MovieModel movieModel = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với ID: " + id));
        return MovieDto.fromEntity(movieModel);
    }

    // 4. HÀM GHI PHIM MỚI TINH XUỐNG Ổ ĐĨA CỨNG
    public MovieDto create(MovieDto movieDto){
        // 1. Chuyển phim từ DTO sang Model
        MovieModel movieModel = movieDto.toEntity();

        // 2. Gán id cho phim Model bằng null để kích nổ cơ chế tự tăng ObjectId dưới ổ D của MongoDB
        movieModel.setId(null);

        // 3. Lưu phim MODEL xuống ổ cứng (database: MongoDB)
        MovieModel saved = movieRepository.save(movieModel);

        // 4. Kết thúc hàm: Trả kết quả phim ra DTO để xuất về cho Controller
        return MovieDto.fromEntity(saved);
    }

    // 5. HÀM CẬP NHẬT THÔNG TIN PHIM
    public MovieDto update(String id, MovieDto movieDto){
        // 1. Tìm Model phim theo ID từ Database
        MovieModel existingMovieModel = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim để cập nhật với ID: " + id));

        // 2. Ghi đè thông tin mới từ vỏ DTO vào thịt Model phim đã tìm được, đang nằm trên RAM
        existingMovieModel.setTitle(movieDto.getTitle());       // update tên phim
        existingMovieModel.setYear(movieDto.getYear());         // update năm sản xuất
        existingMovieModel.setGenre(movieDto.getGenre());       // update thẻ loại phim
        existingMovieModel.setDirector(movieDto.getDirector()); // update tên đạo diễn
        existingMovieModel.setRating(movieDto.getRating());     // update đánh giá xếp hạng

        // 3. Lưu phim Model sau khi cập nhật thông tin xuống Database
        MovieModel updatedMovieModel = movieRepository.save(existingMovieModel);

        // 4. Kết thúc hàm: Trả kết quả phim sau khi update ra DTO để xuất về cho Controller
        return MovieDto.fromEntity(updatedMovieModel);
    }

    // 6. HÀM XÓA CỨNG PHIM
    public void delete(String id){
        // 1. Tìm Model phim theo ID từ Database
        MovieModel existingMovieModel = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim để cập nhật với ID: "+ id));

        // 2. Xóa cứng phim Model khỏi Database
        movieRepository.delete(existingMovieModel);
    }

    /* 7. HÀM TÌM DS PHIM TỪ DATABASE THEO KEYWORD
    --> TẠO 1 PAGE KẾT QUẢ GỒM DS PHIM TÌM ĐƯỢC VỚI SỐ LƯỢNG PHIM ĐƯỢC TRUYỀN VÀO (SỐ LƯỢNG ITEMS/PAGE)
     */
    public PagedResponse<MovieDto> findPageAsPagedResponse(String keyword, Pageable pageable){
        // 1. Kiểm tra Nếu client truyền từ khóa null, ép thành chuỗi rỗng để tránh lỗi logic tìm kiếm
        String safeKeyword = (keyword == null) ? "" : keyword.trim();

        // 2. Tìm phim Model từ database bằng keyword
        /* Pageable: Đây là một Ống dẫn thông số (Interface) của Spring Data, bọc gọn 3 tham số tối cao
         từ Client gửi về: page (Trang muốn xem), size (Số lượng phim/trang), và sort (Sắp xếp theo cột nào)
          . Thay vì viết 3 biến rời rạc, Spring gom chung vào Pageable để nạp lên RAM quét nhanh nội dung
          --> MongoDB dựa vào pageable để chạy ngầm lệnh toán tử .sort().skip().limit() vật lý và trả về một
          khối dữ liệu đóng hòm có tên là Page<MovieModel> (Chứa 5 con phim kèm metadata tổng số trang)
         */
        Page<MovieModel> pageModel = movieRepository.findByTitleContainingIgnoreCase(safeKeyword,pageable);

        // 3. Kết thúc hàm:  Kích nổ chuyển đổi hàng loạt Model sang DTO lồng trong hộp quà Enterprise
        return PagedResponse.fromPage(pageModel, MovieDto::fromEntity);
    }
}
