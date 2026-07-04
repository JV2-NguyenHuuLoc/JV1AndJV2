package com.nhl.miniproject.product.dto;


import com.nhl.miniproject.product.model.Product;
import lombok.Data;

import java.util.List;

@Data
public class ProductListResponse {
    private List<Product> products;
    private int total;
    private int skip;
    private int limit;
}
