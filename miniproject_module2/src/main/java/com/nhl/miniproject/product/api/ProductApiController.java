package com.nhl.miniproject.product.api;


import com.nhl.miniproject.product.model.Product;
import com.nhl.miniproject.product.model.ProductRequest;
import com.nhl.miniproject.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController                     // Trạm REST API nội bộ trả về dữ liệu JSON thô
@RequestMapping("/api/products")    // Định vị trục đường dẫn gốc http://localhost:8081/api/products
@RequiredArgsConstructor            // Cỗ máy tiêm tự động bộ não nghiệp vụ lõi
public class ProductApiController {
    // 1. KHAI BÁO THỰC THỂ PRODUCT_SERVICE
    private final ProductService productService;

    // 2. HÀM 1: POST /api/products -> Tiếp nhận dữ liệu Form tạo mới, bẫy lỗi 400 hoặc Proxy sang API quốc tế lưu hàng
    @PostMapping()  // Đón nhận chính xác giao thức HTTP POST lên trục đường dẫn gốc http://localhost:8081/
    public ResponseEntity<?> create(
            @RequestBody @Valid ProductRequest request,
            BindingResult result
            ){
        // 1. Chốt chặn an ninh: Nếu phát hiện lỗi Validation từ Bean Validation
        if(result.hasErrors()){
            Map<String, String> errors = new HashMap<>();   // Khởi tạo Map lưu lỗi

            // 2. Vòng lặp duyệt qua các biên bản lỗi để bóc tách tên trường và tin nhắn tiếng Việt
            for(FieldError error: result.getFieldErrors()){
                errors.put(error.getField(), error.getDefaultMessage());
            }

            // 3. Xuất xưởng cục JSON lỗi kèm mã trạng thái HTTP 400 Bad Request cho JavaScript xử lý
            return ResponseEntity.badRequest().body(errors);
        }

        // 4. Nếu sạch lỗi, gọi bộ não Proxy bắn dữ liệu sang DummyJSON tạo mới sản phẩm
        Product createProduct = productService.createProduct(request);

        // 5. Trả về sản phẩm hoàn chỉnh vừa tạo kèm mã trạng thái 200 OK
        return ResponseEntity.ok(createProduct);
    }

    // 3. HÀM 2: PUT /api/products/{id} -> Tiếp nhận dữ liệu chỉnh sửa đè theo trục ID
    @PutMapping("/{id}")    // Đục lỗ đón số ID trên trục tọa độ URL
    public ResponseEntity<?> update(
            @PathVariable("id") Long id,
            @RequestBody @Valid ProductRequest request,
            BindingResult result){
        // 1. Chốt chặn an ninh bẫy lỗi Validation tương tự hàm tạo mới
        if(result.hasErrors()){
            Map<String, String> errors = new HashMap<>();   // Khởi tạo Map lưu lỗi

            // 2. Vòng lặp duyệt qua các biên bản lỗi để bóc tách tên trường và tin nhắn tiếng Việt
            for(FieldError error: result.getFieldErrors()){
                errors.put(error.getField(), error.getDefaultMessage());
            }

            // 3. Xuất xưởng cục JSON lỗi kèm mã trạng thái HTTP 400 Bad Request cho JavaScript xử lý
            return ResponseEntity.badRequest().body(errors);
        }

        // 4. Gọi bộ não Proxy phát lệnh PUT đè dữ liệu ra máy chủ ngoài quốc tế
        Product updatedProduct = productService.updateProduct(id, request);

        // 5. Kết thúc hàm
        return ResponseEntity.ok(updatedProduct);
    }

    // 4. HÀM 3: DELETE /api/products/{id} -> Phát súng lệnh trảm cứng sản phẩm khỏi hệ thống
    @DeleteMapping("/{id}")     // Đục lỗ đón số ID để thực hiện lệnh xóa cứng
    public ResponseEntity<?> delete(@PathVariable("id") Long id){
        // 1. Gọi bộ não Proxy kích nổ giao thức DELETE xóa bản ghi xuyên biên giới
        productService.deleteProduct(id);

        // 2. Trả về thông điệp thành công rỗng dạng JSON báo hiệu cho JavaScript quay đầu xe về trang chủ
        return ResponseEntity.ok().build();
    }
}
