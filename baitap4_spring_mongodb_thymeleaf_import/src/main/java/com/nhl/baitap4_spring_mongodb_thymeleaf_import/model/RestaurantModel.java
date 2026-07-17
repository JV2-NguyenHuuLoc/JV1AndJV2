package com.nhl.baitap4_spring_mongodb_thymeleaf_import.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

/*  @JsonIgnoreProperties(ignoreUnknown = true): Đây là tấm khiên bảo hiểm bộ dịch. Trong tệp tin JSON
có thể chứa rất nhiều trường thông tin rác hệ thống (ví dụ trường ngày tháng bị lệch cấu trúc). Nhãn này
ra lệnh cho CPU: "Nếu thấy trường nào lạ trong file JSON mà trong Class Java không khai báo, hãy im lặng
bỏ qua, cấm nổ lỗi sập nguồn"
 */
@JsonIgnoreProperties(ignoreUnknown = true)

/* @Document(collection = "restaurants"): Nhãn chốt chặn định vị đĩa cứng. Ép Spring Boot ánh xạ
Class Java này trực tiếp xuống đích danh bộ sưu tập (Collection) tên là restaurants dưới MongoDB Server
 */
@Document(collection = "restaurants")
public class RestaurantModel {
    @Id
    private String id;      // ID do MongoDB tự động tạo dưới dạng ObjectId

    /* @Field("restaurant_id"): Đại sứ Không gian Đĩa cứng (MongoDB)
    - Chức năng vật lý: Khi bạn gọi hàm lưu (.save()) hoặc tìm kiếm (.find()), nhãn này đóng vai trò
    là chiếc ống dẫn ánh xạ, ra lệnh cho CPU: "Này Spring Data, khi chọc xuống đĩa cứng MongoDB, hãy lấy
    thịt dữ liệu từ biến lạc đà restaurantId trên RAM để ghi đè vào đúng cái cột gạch dưới mang tên
    restaurant_id dưới cung từ ổ đĩa"
    - Chiều hoạt động: Java App <--> MongoDB Database
     */

    /* @JsonProperty("restaurant_id"): Đại sứ Không gian Tệp tin (Jackson Parser)
    - Chức năng vật lý: Nhãn này hoạt động độc lập, không biết gì về database. Khi bạn chạy cỗ máy nạp lô
    (Import file), bộ đọc luồng BufferedReader sẽ bốc từng dòng chữ JSON thô từ file restaurants.json
    lên RAM. Jackson nhìn thấy nhãn này sẽ hiểu:"À, trong file văn bản đang ghi chữ gạch dưới
    "restaurant_id": "40356151", ta phải bốc cái mã số "40356151" này nhét gọn vào đúng cái hộp biến
    restaurantId của Class Java"
    - Chiều hoạt động: File JSON thô (NDJSON) <--> RAM Java App
     */

    /* Khi 2 nhãn (@Field("restaurant_id") và @JsonProperty("restaurant_id")) cùng đặt NGAY TRÊN biến
    restaurantId, thì dữ liệu của biến restaurantId sẽ được đồng bộ thông suốt giữa 4 đối tượng:
    Ổ cứng - MongoDB - JSON - RAM thông qua lệnh Java.
     */
    @Indexed        // Khởi tạo B-tree để tăng tốc tìm kiếm
    @Field("restaurant_id")
    @JsonProperty("restaurant_id")
    private String restaurantId;       // Mã restaurant do TP New York (Đơn vị tạo database) cấp

    private String name;        // Tên restaurant
    private String borough;     // Quận (District)
    private String cuisine;     // Thể loại ẩm thực/Phong cách nấu ăn đặc trưng (American, Chinese, Italian, ...)

    // KÍCH NỔ THIẾT KẾ LỒNG NHAU TRÊN RAM (EMBEDDED CORES)
    private AddressModel address;       // Địa chỉ Restaurant được nhúng từ 1 AddressModel.java
    private List<GradeModel> grades;    // Cấp độ: nhúng 1 List gồm nhiều GradeModel.java
}
