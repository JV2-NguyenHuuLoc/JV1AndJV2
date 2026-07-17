package com.nhl.baitap4_spring_mongodb_thymeleaf_import.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice   // Khai báo: Trạm radar bao phủ tầm xa, quét ngầm 100% RAM toàn cục dự án
@Slf4j              // Tiêm thần công Logger ghi vết biến cố hệ thống
public class GlobalExceptionHandler {

    // 1. HÀM 1: CỖ MÁY ĐÁNH CHẶN TOÀN DIỆN VẠN NĂNG (CÙNG ĐỔ VỀ 1 FILE HTML VIEW ĐỘNG)
    @ExceptionHandler(Exception.class)  // Bắt sống tất cả các loại quả mìn ngoại lệ kích nổ dọc đường
    public String handleGlobalException(Exception ex, Model model){
        // 1. Kích nổ Logger ghi vết trần trụi nguyên nhân văng lỗi vào file Log Server
        log.error("&#128721; SỰ CỐ HẠ TẦNG TOÀN CỤC KÍCH NỔ: ", ex);        // &#128721; = 🛑

        // 2. Khai báo 3 biến thịt dữ liệu cốt tủy để đẩy lên xe tải Model
        String errorCode = "500 - SERVER BIẾN CỐ";
        String errorTitle = "&#128721; - LỖI XỬ LÝ HẠ TẦNG HỆ THỐNG NGẦM";
        String errorMessage = ex.getMessage();

        // 3. THUẬT TOÁN PHÂN LOẠI NHÓM LỖI ĐỘNG ĐỂ TÁI SỬ DỤNG VIEW VẠN NĂNG
        String exName = ex.getClass().getSimpleName();

        if(exName.contains("NullPointerException")){
            errorCode = "400 — RAM KHUYẾT CHỈ MỤC";
            errorTitle = "&#9888; - LỖI CHỌC SAI ỐNG DẪN BỘ NHỚ (NULL POINTER)";    // &#9888; = ⚠️
        } else if (exName.contains("IOException") || exName.contains("MongoException")) {
            errorCode = "503 — ĐĨA CỨNG ĐỨT MẠCH";
            errorTitle = "&#128190; - SỰ CỐ GÃY ĐƯỜNG TRUYỀN Ổ CỨNG HOẶC CARD MẠNG";      // &#128190; = 💾
        } else if (exName.contains("IllegalArgumentException")) {
            errorCode = "422 — ĐẠN DỮ LIỆU BẨN";
            errorTitle = "&#10060; - SAI LỆCH CẤU TRÚC THAM SỐ ĐẦU VÀO HỆ THỐNG";    // &#10060; = ❌
        }

        // 4. Nhét trọn gói siêu dữ liệu lỗi động vào xe tải Model để bắn ra ngoài đồ họa
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorMessage", errorMessage);

        // 5. ÉP CHUYỂN HƯỚNG ĐƯỜNG ỐNG DẪN: Đổ bộ duy nhất vào 1 file HTML vạn năng để xuất lỗi cho toàn dự án
        return "error/global-error";        // Trả về file templates/error/global-error.html
    }

    // 2. HÀM 2: BỎ QUA LỖI HIỂN THỊ DO KHÔNG TÌM ĐƯỢC ICON: favicon.ico
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleFaviconException(Exception ex) {
        // Trình duyệt đòi icon vớ vẩn, im lặng bẻ lái cho quay xe về trang chủ không ghi log lỗi rác RAM
        return "redirect:/restaurants";
    }
}
