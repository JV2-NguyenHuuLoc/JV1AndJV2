package com.nhl.baitap3_spring_mongodb_api_thymeleaf.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")    // Chọc đúng ống dẫn ánh xạ vào collection movies vật lý dưới ổ D
public class MovieModel {

    @Id                 // Khóa chính tối cao của hệ thống NoSQL
    private String id;  // Bắt buộc dùng đúng tên id để MongoDB tự sinh - chuyển đổi ObId - String

    @Indexed            // Tạo B-tree để tìm kiếm field tên phim
    private String title;

    private Integer year;

    private List<String> genre;     // Mảng chứa danh mục thể loại phim lồng nhau chuẩn NoSQL

    private String director;

    private Double rating;
}
