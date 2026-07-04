package com.nhl.miniproject.product.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxWordsValidator implements ConstraintValidator<MaxWords,String> {

    private int maxWords;

    @Override
    public void initialize(MaxWords constraintAnnotaion){
        this.maxWords = constraintAnnotaion.value();
    }

    @Override
    public boolean isValid(String text, ConstraintValidatorContext context){
        if(text == null || text.isEmpty()){
            return true;
        }
        String[] words = text.trim().split("\\s+");
        return words.length <= maxWords;
    }
}
