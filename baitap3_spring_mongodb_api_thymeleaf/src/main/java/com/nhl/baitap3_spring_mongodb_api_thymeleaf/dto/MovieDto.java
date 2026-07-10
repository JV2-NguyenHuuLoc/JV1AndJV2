package com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto;

import com.nhl.baitap3_spring_mongodb_api_thymeleaf.model.MovieModel;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {     // Vỏ bọc phẳng bảo mật thông tin phim
    private String id;

    @NotBlank(message = "Tên phim không được phép để trống hoàn toàn")
    private String title;

    @NotNull(message = "Năm sản xuất không được để trống")
    @Min(value = 1888, message = "Năm sản xuất không được nhỏ hơn thời điểm khai sinh ngành điện ảnh 1888")
    private Integer year;

    @NotEmpty(message = "Mảng thể loại phim phải chứa ít nhất 1 phần tử")
    private List<String> genre;

    @NotBlank(message = "Đạo diễn không được phép để trống hoàn toàn")
    private String director;

    @NotNull(message = "Điểm số đánh giá không được để trống")
    @Min(value = 0, message = "Điểm số thấp nhất là 0")
    @Max(value = 10, message = "Điểm số cao nhất là 10")
    private Double rating;

    // 1. HÀM BỐC MODEL TỪ DB LÊN --> ÉP CHUYỂN THÀNH DTO ĐỂ TRẢ RA NGOÀI
    public static MovieDto fromEntity(MovieModel movieModel){
        // 1. Kiểm tra an khác null
        if(movieModel == null){
            return null;
        }

        // 2. An toàn: Trả về DTO
        return new MovieDto(
                movieModel.getId(),
                movieModel.getTitle(),
                movieModel.getYear(),
                movieModel.getGenre(),
                movieModel.getDirector(),
                movieModel.getRating()
        );
    }

    // 2. HÀM RÃ VỎ DTO --> ÉP ĐÓNG GÓI NGƯỢC TRỞ LẠI THÀNH MODEL ĐỂ SAVE XUỐNG ĐĨA CỨNG
    public MovieModel toEntity(){
        return new MovieModel(
                this.id,
                this.title,
                this.year,
                this.genre,
                this.director,
                this.rating
        );
    }
}
