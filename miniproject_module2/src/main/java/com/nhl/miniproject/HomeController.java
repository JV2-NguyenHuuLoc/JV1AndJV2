package com.nhl.miniproject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    // Cửa khẩu gốc tuyệt đối: Đón nhận request khi người dùng gõ trống trơn địa chỉ http://localhost:8081/
    @GetMapping("/")
    public String index(){
        return "redirect:/home";
    }
}
