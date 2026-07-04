package com.nhl.baitap8_api.user.model;


import com.nhl.baitap8_api.user.validation.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data       // tự sinh Getter/Setter và Constructor không tham số
public class UserForm {

    private Long id;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên không được quá 50 ký tự!")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 50, message = "Họ không được quá 50 ký tự!")
    private String lastName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Vui lòng nhập đúng địa chỉ email")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @ValidPhone(message = "Số điện thoại không đúng định dạng Việt Nam! (Ví dụ: 0912345678 hoặc +84912345678)")
    private String phone;

    private String avatarUrl;

    public String getFullName(){
        return (firstName != null ? firstName: "") + " " + (lastName != null ? lastName : "");
    }
}
