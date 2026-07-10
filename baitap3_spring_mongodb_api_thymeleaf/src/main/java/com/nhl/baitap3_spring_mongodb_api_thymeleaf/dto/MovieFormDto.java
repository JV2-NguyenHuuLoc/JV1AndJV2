package com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieFormDto {
    private String id;

    @NotBlank(message = "Tên phim không được phép để trống hoàn toàn")
    private String title;

    @NotNull(message = "Năm sản xuất không được để trống")
    @Min(value = 1888, message = "Năm sản xuất không được nhỏ hơn thời điểm khai sinh ngành điện ảnh 1888")
    private int year;

    // CHIẾC BẪY BẺ KHÓA: Nhận dữ liệu text phẳng dạng "Action, Sci-Fi" từ Form HTML
    private String genreText;

    @NotBlank(message = "Đạo diễn không được phép để trống hoàn toàn")
    private String director;

    @NotNull(message = "Điểm số đánh giá không được để trống")
    @Min(value = 0, message = "Điểm số thấp nhất là 0")
    @Max(value = 10, message = "Điểm số cao nhất là 10")
    private Double rating;
}
