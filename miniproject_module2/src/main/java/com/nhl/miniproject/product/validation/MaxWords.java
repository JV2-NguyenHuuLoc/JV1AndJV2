package com.nhl.miniproject.product.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxWordsValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxWords {
    int value() default 255;
    String message() default "Đoạn văn bản vượt quá số từ cho phép!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
