package com.nhl.baitap8_api.user.dto;


import com.nhl.baitap8_api.user.model.UserForm;
import lombok.Data;

import java.util.List;

@Data
public class UserPage {
    private List<UserForm> users;
    private String query;
    private int page;
    private int pageSize;
    private int totalItems;
    private int totalPages;

    public boolean hasPrevious(){
        return page > 1;
    }

    public boolean hasNext(){
        return page < totalPages;
    }
}
