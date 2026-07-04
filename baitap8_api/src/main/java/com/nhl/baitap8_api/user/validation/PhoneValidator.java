package com.nhl.baitap8_api.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        // Nếu người dùng không nhập gì, để nhãn @NotBlank ở ngoài xử lý, hàm này cho qua (true)
        if (phone == null || phone.isBlank()) {
            return true;
        }

        // Mặt nạ Regex số điện thoại chuẩn Việt Nam
        String regex = "^(\\+84|0)[1-9]{1}[0-9]{8}$";

        // Trả về true nếu khớp định dạng, trả về false nếu sai (Spring sẽ tự bốc thông báo lỗi ném ra HTML)
        return phone.matches(regex);
    }
}

