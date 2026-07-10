package com.nhl.baitap3_spring_mongodb_api.config;

import com.nhl.baitap3_spring_mongodb_api.model.MovieModel;
import com.nhl.baitap3_spring_mongodb_api.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component      // Biến class thành linh kiện hệ thống chạy ngầm trên bộ nhớ RAM
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    // 1. KHAI BÁO NƠI LƯU DATA
    private final MovieRepository movieRepository;

    // 2. HÀM NẠP DỮ LIỆU MẪU
    @Override
    public void run(String... args) throws Exception{
        // 1. Kiểm tra nếu tổng số lượng database = 0 thì mới kích nổ nạp dữ liệu mẫu
        if(movieRepository.count() == 0){
            movieRepository.saveAll(Arrays.asList(
                    new MovieModel(null, "Inception", 2010, Arrays.asList("Action", "Sci-Fi"),
                            "Christopher Nolan", 8.8),
                    new MovieModel(null, "The Dark Knight", 2008, Arrays.asList("Action", "Crime"),
                            "Christopher Nolan", 9.0),
                    new MovieModel(null, "Interstellar", 2014, Arrays.asList("Adventure", "Drama"),
                            "Christopher Nolan", 8.7),
                    new MovieModel(null, "Avatar", 2009, Arrays.asList("Action", "Adventure"),
                            "James Cameron", 7.8),
                    new MovieModel(null, "The Matrix", 1999, Arrays.asList("Action", "Sci-Fi"),
                            "Lana Wachowski", 8.7),
                    new MovieModel(null, "Spirited Away", 2001, Arrays.asList("Animation", "Family"),
                            "Hayao Miyazaki", 8.6),
                    new MovieModel(null, "Parasite", 2019, Arrays.asList("Drama", "Thriller"),
                            "Bong Joon Ho", 8.5),
                    new MovieModel(null, "The Godfather", 1972, Arrays.asList("Crime", "Drama"),
                            "Francis Ford Coppola", 9.2)
            ));
            System.out.println("HẠ TẦNG: ĐÃ PHÓNG ĐẠN NẠP THÀNH CÔNG 8 BỘ PHIM MẪU XUỐNG " +
                    "Ổ CỨNG VĨNH VIỄN");
        }
    }
}
