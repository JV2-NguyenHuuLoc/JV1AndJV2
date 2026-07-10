package com.nhl.baitap3_spring_mongodb_api_thymeleaf.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException (String message){
        super(message);     // todo
    }
}
