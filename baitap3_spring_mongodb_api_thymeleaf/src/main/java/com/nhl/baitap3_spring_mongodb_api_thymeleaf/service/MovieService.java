package com.nhl.baitap3_spring_mongodb_api_thymeleaf.service;

import com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto.MovieDto;
import com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto.MovieFormDto;
import com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto.PagedListView;
import com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto.PagedResponse;
import com.nhl.baitap3_spring_mongodb_api_thymeleaf.exception.ResourceNotFoundException;
import com.nhl.baitap3_spring_mongodb_api_thymeleaf.model.MovieModel;
import com.nhl.baitap3_spring_mongodb_api_thymeleaf.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service                    // Kích nỗ nghiệp vụ tối cao Service
@RequiredArgsConstructor    // Tự động chọc ống dẫn Dependency Injection tiêm Repository vào Service
public class MovieService {
    // 1. KHAI BÁO BIẾN TOÀN CỤC TỪ REPOSITORY
    private final MovieRepository movieRepository;

    // 2. HÀM 1: ĐỌC TOÀN BỘ DS PHIM TỪ DƯỚI ĐĨA -> BỎ VÀO MODEL --> CHUYỂN THÀNH DS DTO ĐỂ TRẢ RA NGOÀI
    public List<MovieDto> getAllMovie(){
        return movieRepository.findAll()        // tìm tất cả ds phim có trong Database
                .stream()                       // biến 1 ds tỉnh (List) thành dòng chảy liên tục qua CPU
                .map(MovieDto::fromEntity)      // CPU chuyển từng item (trong List) từ dạng Model --> DTO
                .collect(Collectors.toList());  // Gom hết items DTO (vừa tạo) vào ds tỉnh (List) --> trả KQ
    }

    // 3. HÀM 2: ĐỌC 1 PHIM THEO ID: DÙNG CHỐT CHẶN BẢO HIỂM OPTIONAL ĐỂ CHỐNG LỖI NullPointerException LÀM SẬP App
    public MovieDto getById(String id){
        MovieModel movieModel = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với ID: " + id));
        return MovieDto.fromEntity(movieModel);
    }

    // 4. HÀM 3: GHI PHIM MỚI TINH XUỐNG Ổ ĐĨA CỨNG
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

    // 5. HÀM 4: CẬP NHẬT THÔNG TIN PHIM
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

    // 6. HÀM 5: XÓA CỨNG PHIM
    public void delete(String id){
        // 1. Tìm Model phim theo ID từ Database
        MovieModel existingMovieModel = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim để cập nhật với ID: "+ id));

        // 2. Xóa cứng phim Model khỏi Database
        movieRepository.delete(existingMovieModel);
    }

    /* 7. HÀM 6: TÌM DS PHIM TỪ DATABASE THEO KEYWORD
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

    // 8. HÀM 7: PHÂN TRANG THẾ HỆ MỚI PHỤC VỤ ĐƯỜNG ỐNG THYMELEAF VIEW
    public PagedListView<MovieDto> findPage(String keyword, Pageable pageable){
        // 1. Chốt chặn bảo hiểm: Ép từ khóa null về chuỗi rỗng phẳng sạch
        String safeKeyword = (keyword == null) ? "" : keyword.trim();

        // 2. Chọc ống dẫn bốc dữ liệu băm nhỏ dưới đĩa cứng MongoDB lên RAM
        Page<MovieModel> pageModel = movieRepository.findByTitleContainingIgnoreCase(safeKeyword, pageable);

        // 3. Kích nổ hàm chuyển đổi hai chiều từ Model sang DTO
        PagedResponse<MovieDto> pagedResponse = PagedResponse.fromPage(pageModel, MovieDto::fromEntity);

        // 4. Bốc thịt dữ liệu thô ra khỏi hộp PagedResponse truyền thống
        List<MovieDto> contentList = pagedResponse.getContent();

        // 5. Kết thúc hàm: Đóng gói toàn bộ nguyên liệu vào hộp quà Adapter PagedListView phục vụ UI
        return new PagedListView<>(contentList, pagedResponse,safeKeyword);
    }

    // 9. HÀM 8: CHUYỂN ĐỔI ÉP VỎ FORM_DTO THÀNH MODEL THÔ PHỤC VỤ LƯU DATABASE
    public MovieModel convertFormtoEntity(MovieFormDto movieFormDto){
        // 1. Kiểm tra Form nhập liệu null
        if(movieFormDto == null){
            return null;
        }

        // 2. Khởi tạo mảng phụ hứng mảng List<String> của field genre
        List<String> genreList = new ArrayList<>();

        // 3. Kiểm tra chuỗi nập liệu rỗng hay empty
        if(movieFormDto.getGenreText() != null && !movieFormDto.getGenreText().trim().isEmpty()){
            // 4. Tách chuỗi thành mảng dựa trên dấu phẩy
            String[] tokens = movieFormDto.getGenreText().split(",");

            // 5. Duyệt vòng lập mảng thu được + cắt khoảng trắng thừa --> đưa List kết quả field
            for(String s: tokens){
                if(!s.trim().isEmpty()){
                    genreList.add(s.trim());
                }
            }
        }

        // 6. Kết thúc hàm: Chuyển trả Form nhập liệu về ModelDto
        return new MovieModel(movieFormDto.getId(),
                movieFormDto.getTitle(),
                movieFormDto.getYear(),
                genreList,
                movieFormDto.getDirector(),
                movieFormDto.getRating());
    }

    // 10 HÀM 9 (CREATE): GHI PHIM MỚI MODEL (TỪ FORM_DTO) XUỐNG DATABASE,
    // ĐỒNG THỜI TRẢ RA PHIM DTO ĐỂ XUẤT CHO CONTROLLER
    public MovieDto createFromForm(MovieFormDto movieFormDto){
        // 1. Chuyển Phim từ Form nhập liệu (FormDto) --> Model để chuẩn bị ghi xuống Database
        MovieModel movieModel = convertFormtoEntity(movieFormDto);

        // 2, Set ID = null để ép Spring boot tự động khởi tạo ObjectId chuẩn
        movieModel.setId(null);

        // 3. Gọi Hàm Save từ Repository để lưu phim Model xuống Database
        MovieModel saved = movieRepository.save(movieModel);

        // 4. Kết thúc hàm: Trả ra phim DTO để xuất cho Controller
        return MovieDto.fromEntity(saved);
    }

    // 11. HÀM 10: CẬP NHẬT THÔNG TIN PHIM TỪ FORM GIAO DIỆN
    // ĐỒNG THỜI TRẢ RA PHIM DTO ĐỂ XUẤT CHO CONTROLLER
    public MovieDto updateFromForm(String id, MovieFormDto movieFormDto){
        // 1. Gọi hàm Find từ Repository để tìm phim Model từ Database
        MovieModel existMovieModel = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim ID: " + id));

        // 2. Khởi tạo mảng phụ hứng mảng List<String> của field genre
        List<String> genreList = new ArrayList<>();

        // 3. Kiểm tra chuỗi nập liệu rỗng hay empty
        if(movieFormDto.getGenreText() != null && !movieFormDto.getGenreText().trim().isEmpty()){
            // 4. Tách chuỗi thành mảng dựa trên dấu phẩy
            String[] tokens = movieFormDto.getGenreText().split(",");

            // 5. Duyệt vòng lập mảng thu được + cắt khoảng trắng thừa --> đưa List kết quả field
            for(String s: tokens){
                if(!s.trim().isEmpty()){
                    genreList.add(s.trim());
                }
            }
        }

        // 4. Cập nhật thông tin thay đổi Phim vào RAM
        existMovieModel.setTitle(movieFormDto.getTitle());
        existMovieModel.setYear(movieFormDto.getYear());
        existMovieModel.setGenre(genreList);
        existMovieModel.setDirector(movieFormDto.getDirector());
        existMovieModel.setRating(movieFormDto.getRating());

        // 5. Gọi Hàm Save từ Repository để lưu phim Model xuống Database
        MovieModel saved = movieRepository.save(existMovieModel);

        // 6. Kết thúc hàm: Trả ra phim DTO để xuất cho Controller
        return MovieDto.fromEntity(saved);
    }
}
