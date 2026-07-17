package com.nhl.baitap4_spring_mongodb_thymeleaf_import;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller     // Đăng ký cổng điều phối tổng, trả View HTML
public class HomeController {
    /* 1. HÀM INDEX Ở GỐC TỌA ĐỘ TRỐNG ĐỊA CHỈ TRÌNH DUYỆT:
    - Bản chất vật lý ngầm định: Khi người dùng gõ vào thanh địa chỉ độc duy nhất chữ "localhost:8082/",
    hàm này thức dậy trên RAM, ngay lập tức kích nổ lệnh điều hướng "redirect:" lái chiếc xe trình duyệt
    di chuyển sang trục tọa độ "/restaurants" của trạm nghiệp vụ chính một cách tự động phẳng sạch.
     */
    @GetMapping("/")
    public String index(){
        return "redirect:/restaurants";
    }
}
