package com.nhl.baitap8_api.user.controller;


import com.nhl.baitap8_api.upload.service.FileStorageService;
import com.nhl.baitap8_api.user.dto.UserPage;
import com.nhl.baitap8_api.user.model.UserForm;
import com.nhl.baitap8_api.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;

@Controller                         // Báo hiệu đây là bộ điều hướng giao diện sang HTML
@RequestMapping("/v1/users")        // Định vị đường dẫn gốc cho trang web: nhóm users
@RequiredArgsConstructor            // Gọi cỗ máy tiêm vũ khí ngầm của Lombok
public class UserViewController {

    // 1. KHAI BÁO TRẠM TIÊM DỊCH VỤ XỬ LÝ UserService
    private final UserService userService;

    private final FileStorageService fileStorageService;

    // 2. HÀM HIỂN THỊ DANH SÁCH USERS, TÍCH HỢP CỔ MÁY TÌM KIẾM VÀ PHÂN TRANG TỰ ĐỘNG
    @GetMapping
    public String list(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model
    ){
        // 1. Gọi bộ não Service xử lý thuật toán tìm kiếm và phân trang tự động
        UserPage userPage = userService.findPage(query, page);

        // 2. Xếp dữ liệu lên xe đẩy Model để chuẩn bị chở sang vùng đất Thymeleaf
        // 2.1. Khởi tạo Thùng hàng chứa trọn gói thông số phân trang tự động
        model.addAttribute("userPage", userPage);

        // 2.2. Nạp danh sách mảng 5 User của trang hiện tại vào thùng hàng
        model.addAttribute("users", userPage.getUsers());

        // 2.3. Giữ lại từ khóa tìm kiếm gốc (input keyword) của người dùng đã nhập
        model.addAttribute("q", query);

        // 3. Chỉ định file list.html nằm trong thư mục templates/users/ ra lệnh render
        return "users/list";
    }


    // 3. HÀM TẠO FORM MỚI TRỐNG TRƠN ĐỂ NGƯỜI DÙNG NHẬP THÔNG TIN USER MỚI
    @GetMapping("/new")
    public String createForm(Model model){
        // Bắt buộc phải nạp một vật thể Form rỗng lên xe đẩy để Thymeleaf thực hiện liên kết form (Binding)
        model.addAttribute("userForm", new UserForm());

        // Gán biến isEdit = false: Tạo mới user
        model.addAttribute("isEdit", false);
        return "users/form";
    }


    // 4. HÀM XEM THÔNG TIN CHI TIẾT 1 USER
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model){
        // 1. Triệu hồi chiếc hộp bảo hiểm từ tầng Service
        Optional<UserForm> userOptional = userService.findById(id);

        // 2. Kiểm tra: Nếu mở hộp ra thấy trống rỗng, đẩy người dùng sang trang báo lỗi 404 văn minh
        if(userOptional.isEmpty()){
            return "users/not-found";
        }

        // 3. Nếu tìm thấ user
        model.addAttribute("user", userOptional.get());

        // 4. Kết thúc hàm
        return "users/detail";
    }


    // 5. HÀM MỞ FORM EDIT VÀ ĐỖ SẴN DATA CŨ USER CHO NGƯỜI DÙNG EDIT
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model){
        // 1. Triệu hồi chiếc hộp bảo hiểm từ tầng Service
        Optional<UserForm> userOptional = userService.findById(id);

        // 2. Kiểm tra: Nếu mở hộp ra thấy trống rỗng, đẩy người dùng sang trang báo lỗi 404 văn minh
        if(userOptional.isEmpty()){
            return "users/not-found";
        }

        // 3. Nạp dữ liệu cũ của User vào nhãn "userForm" để form HTML Thymeleaf tự động hiển thị lại chữ
        model.addAttribute("userForm", userOptional.get());

        // Gàn biến isEdit = true --> Update user
        model.addAttribute("isEdit", true);

        // 4. Kết thúc hàm
        return "users/form";
    }


    // 6. HÀM TẠO MỚI (POST) USER
    @PostMapping
    public String create(
            @ModelAttribute("userForm") @Valid UserForm userForm,  // Đón chữ từ form và kích hoạt thanh tra
            BindingResult result,                           // Hộp chứa bẩy lỗi
            @RequestParam("avatar") MultipartFile avatar,   // Đón luồng byte ảnh nhị phân mới
            RedirectAttributes redirectAttributes,          // Hộp nạp đạn thông báo chớp nhoáng
            Model model                                     // Xe đẩy thùng hàng dự phòng
    ){
        // 1. Kiểm lỗi dữ liệu nhập từ form
        if(result.hasErrors()){
            // Giữ nguyên cờ false khi bắt buộc render lại form lỗi
            model.addAttribute("isEdit",false);
            return "users/form";    // Giữ người dùng lại trang form để bừng sáng chữ đỏ báo lỗi
        }

        // 2. Phẫu thuật lưu file ảnh vật lý ra ổ D nếu người dùng có chọn file ảnh
        try{
            if(avatar != null && !avatar.isEmpty()){
                String avatarPath = fileStorageService.store(avatar,"avatars"); // Lưu vào ổ cứng
                userForm.setAvatarUrl(avatarPath);      // Gán tọa độ URL ảo vào biến form
            }
        }catch (Exception e){
            model.addAttribute("uploadError", "Hệ thống lưu file ảnh thất bại: "
                    + e.getMessage());
            model.addAttribute("isEdit", false);
            return "users/form";
        }

        // 3. Kéo chốt nạp vào RAM và bẻ lái bánh xe chuyển hướng PRG
        userService.create(userForm);       // Đẩy xuống Service dập số ID từ 13 và lưu vào Map
        // Hiện chữ thông báo chớp nhoáng
        redirectAttributes.addFlashAttribute("message", "Thêm mới người dùng thành công!");

        // 4. Kết thúc hàm
        return "redirect:/v1/users";
    }


    // 7. HÀM CẬP NHẬT THAY ĐỔI THÔNG TIN USER VÀO RAM
    @PostMapping("/{id}")
    public String update(
            @PathVariable("id") Long id,                        // Định vị trục tọa độ User chỉnh sửa
            @ModelAttribute("userForm") @Valid UserForm form,   // Đón dữ liệu chữ
            BindingResult result,                               // Hộp bẫy lỗi Validation
            @RequestParam("avatar") MultipartFile avatar,       // Đón file ảnh mới
            RedirectAttributes redirectAttributes,              // Thông báo chớp nhoáng
            Model model                                         // Xe đẩy dự phòng
            ){
        // 1. Kiểm lỗi dữ liệu nhập vào form
        if(result.hasErrors()){
            model.addAttribute("isEdit",true);  // Giữ cờ true để hiển thị đúng giao diện Form Sửa
            return "users/form";        // Giữ người dùng lại màn hình Form để sửa lỗi nhập liệu
        }

        // 2. Phẫu thuật lưu file ảnh mới nếu người dùng chọn thay ảnh
        try{
            if(avatar != null && !avatar.isEmpty()){
                String avatarPath = fileStorageService.store(avatar, "avatars");   // Hạ cánh ảnh mới xuống ổ D
                form.setAvatarUrl(avatarPath);      // Gán tọa độ ảnh mới vào form
            }
        } catch (Exception e) {
            model.addAttribute("uploadError", "Hệ thống lưu file ảnh thất bại: "
                    + e.getMessage());
            model.addAttribute("isEdit", true);
            return "users/form";
        }

        // 3. Kích nổ lệnh ghi đè vào RAM và bẻ lái bánh xe PRG
        userService.update(id,form);    // Tầng Service sẽ tự lo liệu việc giữ ảnh cũ nếu form.avatarUrl bị trống!
        // Nạp đạn thông báo
        redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin người dùng thành công!");

        // 4. Kết thúc hàm
        return "redirect:/v1/users";
    }


    // 8. HÀM DELETE CỨNG USER
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes){
        userService.delete(id);     // Xóa cứng user khỏi RAM
        redirectAttributes.addFlashAttribute("message",  "Xóa người dùng khỏi hệ thống thành công!");

        return "redirect:/v1/users";
    }
}
