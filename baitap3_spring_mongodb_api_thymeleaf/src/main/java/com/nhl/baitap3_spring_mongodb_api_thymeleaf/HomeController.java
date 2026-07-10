package com.nhl.baitap3_spring_mongodb_api_thymeleaf;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller     // Khóa chặn nhãn điều phối cửa ngõ tổng
public class HomeController {

    @GetMapping("/")        // CHỐT CHẶN TRỤC ĐƯỜNG DẪN GỐC TỐI CAO CỦA HỆ THỐNG
    public String index(){
        // Kích nổ lệnh chuyển hướng RAM, ép trình duyệt tự động nhảy vào trục chức năng Phim
        return "redirect:/movies";
    }
}
