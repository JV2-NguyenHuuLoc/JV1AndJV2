package com.nhl.baitap3_spring_mongodb_api.repository;

import com.nhl.baitap3_spring_mongodb_api.model.MovieModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository         // Đánh dấu chốt chặn thông mạch tầng kết nối NoSQL vật lý
public interface MovieRepository extends MongoRepository<MovieModel, String> {

    // 1. Tìm chính xác 1 bộ phim theo tiêu đề (Trả về Optional bảo hiểm)
    Optional<MovieModel> findByTitle(String title);

    // 2. Lọc danh sách phim theo tên đạo diễn
    List<MovieModel> findByDirector(String director);

    // 3. Phép toán so sánh lớn hơn - Tìm các phim có điểm số rating vượt mốc
    List<MovieModel> findByRatingGreaterThan(Double rating);

    // 4. Tìm phim lọc theo chuỗi con chứa từ khóa, không phân biệt hoa thường chuẩn Senior
    List<MovieModel> findByTitleContainingIgnoreCase(String keyword);

    // 5. Tìm kiếm gần đúng kết hợp cắt gọt phân trang dưới đĩa cứng
    Page<MovieModel> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
