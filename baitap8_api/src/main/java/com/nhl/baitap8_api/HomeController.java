package com.nhl.baitap8_api;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller                         // Báo hiệu đây là bộ điều hướng giao diện sang HTML
public class HomeController {

    // Hàm điều hướng chính
    @GetMapping("/")
    public String index(){
        // Bản chất: Khi người dùng gõ http://localhost:8080/, Server tự động quay đầu xe chuyển hướng
        // sang trang danh sách User
        return "redirect:/v1/users";
    }

}
