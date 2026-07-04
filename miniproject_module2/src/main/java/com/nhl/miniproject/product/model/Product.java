package com.nhl.miniproject.product.model;


import lombok.Data;
import java.util.List;

@Data
public class Product {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private Double rating;
    private String category;
    private String thumbnail;
    private String brand;

    // Lưu giữ bộ ảnh phụ của 1 sản phẩm cho trùng khớp với JSON của API quốc tế
    private List<String> images;

    // Bộ 6 vũ khí bọc lót tối tân (sẵn cho tương lai mở rộng: Hóa giải hoàn toàn bẫy lỗi 500 hụt trường của Thymeleaf
    private Integer stock;                  // Tồn kho
    private Double discountPercentage;      // Phần trăm giảm giá
    private String sku;                     // Mã hàng hóa SKU
    private String warrantyInformation;     // Thông tin bảo hành
    private String shippingInformation;     // Thông tin vận chuyển
    private String returnPolicy;            // Chính sách đổi trả
}
