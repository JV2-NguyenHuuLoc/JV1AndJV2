package com.nhl.baitap4_spring_mongodb_thymeleaf_import.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

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
public class GradeModel {
    private String grade;       // Cấp độ
    private Double score;       // Số điểm
    private Date date;          // Ngày chấm điểm
}
