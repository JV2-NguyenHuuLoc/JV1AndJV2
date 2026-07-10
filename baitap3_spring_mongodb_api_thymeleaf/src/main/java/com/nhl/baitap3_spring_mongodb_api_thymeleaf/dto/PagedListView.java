package com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto;

import java.util.List;

public class PagedListView<T> {

    // 1. Chốt chặn chứa thịt dữ liệu (Danh sách DTO sạch)
    private List<T> content;

    // 2. Chốt chặn chứa siêu dữ liệu phân trang (Metadata) giống cấu trúc mẫu HTML có sẵn
    private PagedResponse<T> pagination;

    // 3. Giữ lại từ khóa tìm kiếm vĩnh viễn trên RAM khi lật trang
    private String keyword;

    // 4. Khởi tạo Constructor đầy đủ tham số để nạp dữ liệu từ Service sang
    public PagedListView(List<T> content, PagedResponse<T> pagination, String keyword){
        this.content = content;
        this.pagination = pagination;
        this.keyword = keyword;
    }

    // 5. Cú bẻ khóa quyết định: Tạo các hàm Getter/Setter chuẩn để gọi ngoài HTML
    public List<T> getContent(){
        return content;
    }

    public PagedResponse<T> getPagination(){
        return pagination;
    }

    public String getKeyword(){
        return keyword;
    }
}
