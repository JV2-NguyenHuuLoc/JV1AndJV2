package com.nhl.baitap8_api.upload.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;


@Slf4j  // Lombok tự tiêm biến "log" tĩnh, bất biến và kích hoạt hệ thống camera ghi nhật ký hành trình (Logging)
@Service    // Đăng ký Class này vào kho của Spring, biến nó thành một Trạm xử lý logic, nghiệp vụ (Service Layer)

public class FileStorageService {
    // 1. Khai báo hằng số cho phép định dạng file upload
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    // 2. Khai báo biến đường dẫn gốc
    private final Path uploadRoot;

    // 3. Tiêm đường dẫn động từ file Application.properties vào biến uploadDir
    // -> xử lý tạo mới thư mục đúng vị trí quy định: app.upload.dir=D:/JV2_intelliJ/baitap8_externalFiles
    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) throws IOException{
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadRoot);           // Tạo thư mục gốc nếu chưa có.
        log.info("Upload root initialize: {}", this.uploadRoot);
    }

    // 4. Hàm nhận file nhị phân từ Client đi vào --> bị phẫu thuật đổi tên bằng UUID, hạ cánh an toàn xuống ổ D:
    /*
    Hàm này sẽ nhận vào 2 tham số: MultipartFile file (File do client gửi lên) và String subFolder
    (Tên thư mục con bạn muốn phân loại, ví dụ: "avatars", "products").
    Hàm sẽ trả về một chuỗi String chính là đường dẫn URL ảo
    (ví dụ: /uploads/avatars/uuid-ngau-nhien.png) để sau này trả về cho client hiển thị lên trình duyệt.
    */
    public String store (MultipartFile file, String subFolder) throws IOException{
        // Bắt buộc phải có throws IOException vì thao tác ghi file trực tiếp vào ổ đĩ có thể sinh lỗi hệ thống

        // BƯỚC 1: Bẫy lỗi file trống (Validation)
        if(file == null || file.isEmpty()){
            throw new IllegalArgumentException("File rỗng!");
        }

        // BƯỚC 2: Bẫy lỗi mã độc định dạng file (Content-Type)
        // -> Tránh hacker gửi file bẩn .exe NHƯNG đổi đuôi file thành .png, .... để đánh lừa hệ thống
        String contentType = file.getContentType();     // Khởi tạo biến tạm lưu content-type
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)){
            throw new IllegalArgumentException("Chỉ chấp nhập ảnh JPEG, PNG, GIF, WEBP");
        }

        // BƯỚC 3: Phẫu thuật cắt đuôi file lấy phần mở rộng file, Lấy tên file gốc an toàn
        String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String extension = "";                          // Khai báo biến tạm để lưu đuôi file
        int dot = originalName.lastIndexOf('.');    // Tìm vị trí dấu chấm cuối cùng của tên file
        if(dot > 0){
            extension = originalName.substring(dot);    // Cắt an toàn lấy phần đuôi file
        }

        // BƯỚC 4: Áp sát mã UUID độc bản vào phần tên file: tránh trùng lấp, tạo tên file siêu sạch, an toàn
        String savedName = UUID.randomUUID() + extension;   // Tạo tên file mới siêu sạch, an toàn

        // BƯỚC 5: Định vị tọa độ ổ cứng và lưu file vật lý: Xác định thư mục đích (gồm thư mục gốc ổ D + tên thư mục con)
        Path targetDir = uploadRoot.resolve(subFolder);     // Khai báo thư mục con truyền vào hàm
        Files.createDirectories(targetDir);     // Tạo thư mục con này nếu nó chưa tồn tại trên ổ đĩa vật lý
        Path targetFile = targetDir.resolve(savedName);     // Xác định tọa độ file đích cuối cùng

        // BƯỚC 6: Đẩy luồng byte nhị phân từ Server vào ổ cứng: Lưu file vật lý (ghi đè nếu trùng tên file)
        Files.copy(file.getInputStream(),targetFile, StandardCopyOption.REPLACE_EXISTING);

        // BƯỚC 7: Xuất xưởng URL -> Tạo chuỗi đường dẫn URL ảo để trả về
        String publicUrl = "/uploads/" + subFolder + "/" + savedName;

        // BƯỚC 8: Dùng camera giám sát ghi lại vết thành công
        log.debug("Store file {} tại đường dẫn: {}", originalName, publicUrl);

        // BƯỚC 9: Kết thúc
        return publicUrl;
    }

}
