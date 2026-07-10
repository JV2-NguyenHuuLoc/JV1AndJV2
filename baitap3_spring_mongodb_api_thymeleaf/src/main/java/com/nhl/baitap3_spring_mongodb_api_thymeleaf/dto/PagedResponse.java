package com.nhl.baitap3_spring_mongodb_api_thymeleaf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {     // Phân trang Generic chuẩn Enterprise
    private List<T> content;        // Danh sách thịt dữ liệu đã được đổi vỏ sang DTO sạch
    private int pageNo;             // Số thứ tự trang hiện tại (0-indexed)
    private int pageSize;           // Số lượng phần tử giới hạn trên 1 trang
    private long totalElements;     // Tổng số dòng dữ liệu thực tế đang nằm dưới ổ D
    private int totalPages;         // Tổng số trang hệ thống tính toán được trên RAM
    private boolean last;           // Chốt chặn kiểm tra xem đây có phải trang cuối cùng chưa

    // 1. HÀM BĂM NHỎ Page<Model> CỦA Spring Data -> ÉP THÀNH PagedResponse<DTO> PHẲNG SẠCH
    /*
        E (viết tắt của Entity/Model): MovieModel
        D (viết tắt của DTO): MovieDto
        Function<E, D> mapperFunction: Hàm chuyển đổi dữ liệu từ E (MovieModel) --> D (MovieDto), sử dùng
        bằng cách truyền tham số MovieDto::fromEntity
     */
    public static <E,D> PagedResponse<D> fromPage(Page<E> page, Function<E,D> mapperFunction){
        List<D> dtoList = page.getContent().stream()    // biến 1 ds tỉnh (List) thành dòng chảy liên tục qua CPU
                .map(mapperFunction)                    // CPU chuyển từng item (trong List) từ dạng Model --> DTO
                .collect(Collectors.toList());          // Gom hết items DTO (vừa tạo) vào ds tỉnh (List) --> trả KQ

        return new PagedResponse<>(
                dtoList,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
