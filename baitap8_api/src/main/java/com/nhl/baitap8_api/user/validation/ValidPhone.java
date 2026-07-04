package com.nhl.baitap8_api.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneValidator.class)     // Chỉ định bộ não vật lý sẽ xử lý hàm này
@Target({ElementType.FIELD})                        // Nhãn này chỉ được phép đính trên đầu các Biến (Field)
@Retention(RetentionPolicy.RUNTIME)                 // Nhãn có hiệu lực xuyên suốt lúc ứng dụng đang chạy
public @interface ValidPhone {
    String message() default "Số điện thoại không đúng định dạng Việt Nam";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
