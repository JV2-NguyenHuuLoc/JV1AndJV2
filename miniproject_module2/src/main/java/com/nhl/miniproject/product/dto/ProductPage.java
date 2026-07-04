package com.nhl.miniproject.product.dto;


import com.nhl.miniproject.product.model.Product;
import lombok.Data;
import java.util.List;

@Data
public class ProductPage {

    // 1. KHAI BÁO BIẾN THỰC THỂ DÙNG CHUNG
    private List<Product> products;     // Danh sách sản phẩm của trang hiện tại
    private String query;               // Từ khóa tìm kiếm đang áp dụng (giữ chữ trên form)
    private String category;            // Nhóm sản phẩm đang lọc (giữ bộ lọc nhóm)
    private int page;                   // Số trang hiện tại (bắt đầu từ 1)
    private int pageSize;               // Số lượng sản phẩm hiển thị trên 1 trang (limit)
    private int totalItems;             // Tổng số sản phẩm khớp bộ lọc trong hệ thống (total)

    // 2. HÀM TỰ ĐỘNG TÍNH TOÁN TỔNG SỐ TRANG XUẤT XƯỞNG
    public int getTotalPages(){
        if(pageSize == 0){
            return 1;
        }
        return (int) Math.ceil((double) totalItems/pageSize);
    }

    // 3. HÀM TRẮC NGHIỆM LOGIC KIỂM TRA CÓ TRANG TRƯỚC KHÔNG?
    public boolean hasPrevious(){
        return page > 1;
    }

    // 4. HÀM TRẮC NGHIỆM LOGIC KIỂM TRA CÓ TRANG SAU KHÔNG?
    public boolean hasNext(){
        return page < getTotalPages();
    }
}
