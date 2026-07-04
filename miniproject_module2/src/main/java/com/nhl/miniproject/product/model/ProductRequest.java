package com.nhl.miniproject.product.model;


import com.nhl.miniproject.product.validation.MaxWords;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String title;

    @NotBlank(message = "Mô tả sản phẩm không được để trống")
    @MaxWords(value = 255, message = "Mô tả không được quá 255 từ!")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "1.0", message = "Giá sản phẩm phải từ 1 USD trở lên")
    @DecimalMax(value = "200.0", message = "Giá sản phẩm không vượt quá 200 USD")
    private Double price;

//    private Double rating;

    @NotBlank(message = "Danh mục không được để trống")
    private String category;

    private String thumbnail;

    @NotBlank(message = "Thương hiệu không được để trống")
    private String brand;
}
