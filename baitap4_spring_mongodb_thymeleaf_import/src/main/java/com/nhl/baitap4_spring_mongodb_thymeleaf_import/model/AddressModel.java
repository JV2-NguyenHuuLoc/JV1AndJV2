package com.nhl.baitap4_spring_mongodb_thymeleaf_import.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
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
public class AddressModel {
    private String building;        // Số nhà
    private String zipcode;         // Mã bưu chính (bưu điện)
    private String street;          // Tên đường
    private List<Double> coord;     // Mảng lưu tọa độ địa lý [Kinh độ, Vĩ độ] của Restaurant

    /* TIÊM MỚI ĐƯỜNG ỐNG DẪN THƯƠNG MẠI:
    - @Field("map_url"): Ra lệnh cho Spring Data ánh xạ biến này xuống cột map_url dưới đĩa cứng MongoDB.
    - @JsonProperty("map_url"): Ép bộ bọc Jackson bốc thẳng chuỗi URL Google Maps xịn từ file NDJSON nạp vào RAM!
    */
    @Field("map_url")           // ánh xạ xuống đĩa cứng MongoDB
    @JsonProperty("map_url")    // bộ dịch Jackson tự bốc đường dẫn google map từ file dữ liệu NDJSON ném lên RAM
    private String mapUrl;      // Đường dẫn link Google Maps chuẩn chỉ có dấu ghim định vị từ Database
}
