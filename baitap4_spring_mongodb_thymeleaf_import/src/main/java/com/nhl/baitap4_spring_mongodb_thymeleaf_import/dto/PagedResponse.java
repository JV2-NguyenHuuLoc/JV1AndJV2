package com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// KHỞI TẠO HÀM: LẤY DỮ LIỆU DS RESTAURANTS TỪ DATABASE (DƯỚI DẠNG DTO) + TỰ ĐỘNG PHÂN TRANG TRÊN WEB

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;    // Chứa thịt dữ liệu DS restaurants dưới dạng DtoModel
    private int page;           // Số trang hiện hành từ Spring Data (Hệ ngầm định bắt đầu từ số 0)
    private int size;           // Số items/page
    private long totalElements; // Tổng số phần từ quét được từ database
    private int totalPages;     // Tổng số trang cần có để tải hết totalElements
    private boolean last;       // Biến chốt chặn đánh dấu trang cuối
}
