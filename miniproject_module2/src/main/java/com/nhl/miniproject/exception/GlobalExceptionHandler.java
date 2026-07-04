package com.nhl.miniproject.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice       // Bộ xử lý bẫy lỗi tập trung toàn cục tối cao của hệ thống Hybrid
public class GlobalExceptionHandler {

    /**
     * BẨY LỖI 1: Trảm lỗi hệ thống API quốc tế bị sập nguồn hoặc nghẽn mạch (Mã lỗi 502 Bad Gateway)
     * Trả về dữ liệu JSON thô cho JavaScript nhận diện để rải lỗi lên Form
     */
    @ExceptionHandler(RestClientResponseException.class)    // Hứng trọn gói mọi lỗi nổ ra khi gọi RestTemplate
    @ResponseBody        // Ép xuất xưởng dữ liệu JSON thô cho JavaScript nhận diện
    public ResponseEntity<Map<String, String>> handlerApiError (RestClientResponseException e){
        // 1. Khởi tạo mảng để lưu lỗi
        Map<String, String> response = new HashMap<>();

        // 2. Gán lỗi vào mảng
        response.put("error","Máy chủ DummyJSON API ngoài gặp sự cố hoặc phản hồi chậm!");

        // 3. Kết thúc hàm
        return ResponseEntity.status(502).body(response);
    }

    /**
     * BẨY LỖI 2: Đánh chặn toàn cục lỗi Không Tìm Thấy Sản Phẩm / Nội Dung (Mã lỗi 404 Not Found)
     * Bản chất ngầm: Khi RestTemplate chọc vào ID không tồn tại, Spring tự động bẻ lái bánh xe,
     * nạp tin nhắn văn minh vào xe đẩy Model rồi kích nổ hiển thị trang HTML error/404.html.
     */
    @ExceptionHandler(HttpClientErrorException.NotFound.class)  // Đón nhận chính xác lỗi 404 từ luồng mạng
    public String handleNotFoundError(HttpClientErrorException.NotFound e, Model model){
        // 1. Nạp mác thùng hàng "message" trùng khớp 100% với thẻ th:text="${message}" trong file 404.html
        model.addAttribute("message", "Sản phẩm hoặc nội dung trang bạn đang tìm kiếm không " +
                "tồn tại trên hệ thống!");

        // 2. Kết thúc hàm
        return "error/404";    // Mở file templates/error/404.html
    }

    /**
     * BẨY LỖI 3: Phòng thủ tối cao cho toàn bộ lỗi sập luồng code Java Core còn lại (Mã lỗi 500)
     * Bản chất ngầm: Khi nổ ra các lỗi tính toán, null pointer nội bộ, Java tự động điều hướng
     * người dùng sang màn hình giao diện hiển thị trang lỗi HTML error/500.html thân thiện.
     */
    @ExceptionHandler(Exception.class)
    public String handlerAllOtherErrors(Exception e){
        // Tự động chuyển hướng bẻ lái màn hình hiển thị trang HTML templates/error/500.html thân thiện
        return "error/500";     // Mở file templates/error/500.html
    }
}
