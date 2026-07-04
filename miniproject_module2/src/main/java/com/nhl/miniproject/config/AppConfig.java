package com.nhl.miniproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration      // Báo hiệu đây là file cầu hình hạ tầng hệ thống
public class AppConfig {

    @Bean       // Đúc cổ máy kết nối mạng RestTemplate bỏ vào kho Spring Boot để Service sẵn sàng tiêm sử dụng
    public RestTemplate restTemplate(){
        // 1. Khởi tạo vật thể nhà máy sản xuất request vật lý của Java Core
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 2. Kích nổ 2 lệnh cài đặt thời gian chờ (Đơn vị tính bằng Miligiây: 10.000ms = 10s)
        factory.setConnectTimeout(10000);       // Cài đặt thời gian tối đa để thiết lập kết nối mạng
        factory.setReadTimeout(10000);          // Cài đặt thời gian tối đa để chờ cào dữ liệu về RAM

        // 3. Đút nhà máy cấu hình này vào ruột cỗ máy RestTemplate và xuất xưởng nạp vào kho Spring
        return  new RestTemplate(factory);
    }
}
