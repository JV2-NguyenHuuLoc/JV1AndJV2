package com.nhl.baitap3_spring_mongodb_api.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException (String message){
        super(message);     // todo
    }
}
