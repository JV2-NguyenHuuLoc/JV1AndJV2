package com.nhl.baitap3_spring_mongodb_api.controller;

import com.nhl.baitap3_spring_mongodb_api.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/* @RestControllerAdvice:
- Spring Boot sẽ dựng một Trạm radar quét sóng (Global Interceptor) bọc xung quanh tầng Controller
- Khi bất kỳ dòng code nào ở dưới Service kích nổ quả mìn ResourceNotFoundException, @RestControllerAdvice
sẽ lập tức dùng ống dẫn Tóm gọn cái lỗi đó ngay trên bộ nhớ RAM
- Ngăn không cho lỗi văng ra ngoài, băm nhỏ nội dung, đóng gói vào một JSON sạch đẹp và trả về cho Client
 mã trạng thái mong muốn (Mã 404 cho Không tìm thấy, Mã 400 cho Dữ liệu bẩn) cực kỳ chuyên nghiệp.
 */
@RestControllerAdvice(basePackageClasses = MovieRestController.class)
public class RestExceptionHandler {

    // 1. HÀM 1: ĐÁNH CHẶN LỖI 404: KHI KHÔNG TÌM THẤY PHIM CÓ ID DƯỚI DATABASE
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(ResourceNotFoundException ex){
        // 1. Khởi tạo HasMap để gom lỗi 404
        Map<String, Object> errorBody = new HashMap<>();

        // 2. Gom các thông số cấu trúc lỗi cố định của hệ thóng báo lỗi + message lỗi vào HashMap
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.NOT_FOUND.value());
        errorBody.put("error", "not found");
        errorBody.put("message", ex.getMessage());  // Bốc dòng chữ báo lỗi từ Service lên RAM

        // 3. Kết thúc hàm: Xuất mã 404 khi không tìm thấy phim
        return new ResponseEntity<>(errorBody, HttpStatus.NOT_FOUND);
    }

    // ANNOTATION CHỐT CHẶN: @ExceptionHandler(MethodArgumentNotValidException.class)
    // ⚙️ BẢN CHẤT DI CHUYỂN DỮ LIỆU NGẦM TRÊN MẠNG:
    // 1. Khi Client gửi dữ liệu JSON bẩn vi phạm các chốt chặn bảo mật (@NotBlank, @Min, @Max) ở DTO.
    // 2. Nhãn @Valid ở Controller sẽ đánh thức radar, ép Spring Boot chặn đứng tiến trình và tự động kích nổ
    //    đích danh quả mìn ngoại lệ của hệ thống mang tên: MethodArgumentNotValidException.
    // 3. Nhãn @ExceptionHandler này đóng vai trò là "Cột thu lôi", chọc ống dẫn ôm gọn toàn bộ khối lỗi đa tầng đó
    //    ngay trên RAM để chuyển hướng dòng chảy xuống Hàm 2 xử lý, băm nhỏ cấu trúc ném về mã 400 Bad Request.

    // 2. HÀM 2: ĐÁNH CHẶN LỖI 400: KHI CLIENT VI PHẠM CÁC CHỐT CHẶN @NotBlank, @Min, @Max, ..
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex){
        // 1. Khởi tạo HasMap để gom lỗi 404
        Map<String, Object> errorBody = new HashMap<>();

        // 2. Gom các thông số cấu trúc lỗi cố định của hệ thống báo lỗi vào HashMap
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("error", "bad request");

        // 3. Khởi tạo Map phụ để chứa tất cả các tên field bị lỗi + message lỗi (cặp key - value)
        Map<String, String> fieldErrors = new HashMap<>();

        // 4. Duyệt vòng lặp for để bắt lấy tất cả tên field + message lỗi (nếu xuất hiện): key - value
        for(ObjectError error: ex.getBindingResult().getAllErrors()){
            // 4.1. Ép kiểu về FieldError để lấy ra tên field bị lỗi
            String fieldName = ((FieldError) error).getField();

            // 4.2. Lấy dòng message đã đặt tại MovieDto (các nhãn @NotBlank, @Min, @Max, ..)
            String errorMessage = error.getDefaultMessage();

            // 4.3. Gán vào Map phụ
            fieldErrors.put(fieldName,errorMessage);
        }

        // 5. Gán dữ liệu lỗi từ Map phụ vào HashMap chính: thông báo lỗi
        errorBody.put("validationErrors", fieldErrors);

        // 6. Kết thúc hàm: Xuất mã 400, bẻ khóa dữ liệu bẫn do người dùng nhập vào
        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }
}
