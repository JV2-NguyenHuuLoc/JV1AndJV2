package com.nhl.baitap8_api.upload.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    // 1. Dùng nhãn @Value để tiêm app.upload.dir từ properties vào biến uploadDir
    @Value("${app.upload.dir}")
    private String uploadDir;

    // 2. Ánh xạ đường dẫn ảo "/upload/**" vào ổ đĩa vật lý
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    // .addResourceHandler("/uploads/**"): Định nghĩa một đường dẫn ảo trên URL. Bất kỳ khi nào trình duyệt
    // gọi một địa chỉ có dạng http://localhost:8080/uploads/tên_ảnh.png thì Spring sẽ bắt lấy request đó.

    // .addResourceLocations("file:" + uploadDir + "/"): Định nghĩa độ tọa thực tế trên ổ cứng.
    // Tiền tố "file:" báo cho Java biết đây là một vị trí vật lý nằm ngoài hệ điều hành (không phải trong static)

}
