package com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto;

import com.nhl.baitap4_spring_mongodb_thymeleaf_import.model.GradeModel;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.model.RestaurantModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/* CHỨC NĂNG CLASS: Lọc sạch Entity thô dưới dạng Model, chỉ bốc các trường văn bản ngắn gọn
ném ra bảng danh sách (list.html) để giải phóng băng thông card mạng và bảo mật dữ liệu
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDto {

    // 1. KHAI BÁO THUỘC TÍNH
    private String id;          // ID do MongoDB tự động tạo dưới dạng ObjectId
    private String restaurantId;       // Mã restaurant do TP New York (Đơn vị tạo database) cấp
    private String name;        // Tên restaurant
    private String borough;     // Quận (District)
    private String cuisine;     // Thể loại ẩm thực/Phong cách nấu ăn đặc trưng (American, Chinese, Italian, ...)
    private String fullAddress; // Địa chỉ, bao gồm số nhà, tên đường, quận, TP, ....
    private List<Double> coord; // Khai sinh trường riêng biệt hứng mảng tọa độ [Kinh độ, Vĩ độ]

    // TIÊM MỚI BIẾN CHỮ THƯƠNG MẠI: Lưu cất link dẫn đường Google Maps bốc từ đĩa NoSQL
    private String mapUrl;

    /* Khai sinh 2 thuộc tính totalGrades và averageScore. CPU tự động sẽ chạy một vòng lặp for mạch thẳng
    tính toán trung bình cộng điểm số và dùng hàm toán học Math.round ép làm tròn lấy đúng 1 chữ số thập phân
    trực tiếp trên RAM trước khi trả ra giao diện
    */
    private int totalGrades;
    private double averageScore;

    // 2. HÀM: Chuyển đổi hai chiều (Mapping), lột vỏ bọc thực thể Model thô từ đĩa cứng MongoDB,
    // phẳng hóa thông tin chuỗi địa chỉ, trích xuất mảng tọa độ vật lý, đồng thời kích nổ vòng lặp tính toán
    // số lượt chấm điểm và điểm trung bình cộng làm tròn ngay trên bộ nhớ RAM để trả ra DTO sạch cho UI.
    public static RestaurantDto fromEntity(RestaurantModel restaurantModel){
        // 1. Kiểm tra dữ liệu rỗng
        if(restaurantModel == null){
            return null;
        }

        // 2. Khai báo biến tạm chứa fullAddress
        String addressText = "Chưa cập nhật địa chỉ";

        // 3. Khởi tạo biến tạm chứa link bản đồ
        String mapUrlText = "";

        // 4. Khởi tạo mảng rỗng chứa Kinh độ và Vĩ độ, có chặn lỗi NullPointerException
        List<Double> coordList = new ArrayList<>();

        // 5. Nối chuỗi số nhà + tên đường + ... thành fullAddress
        if(restaurantModel.getAddress() != null){
            // 5.1. Lấy số nhà
            String building = (restaurantModel.getAddress().getBuilding() != null)
                    ? restaurantModel.getAddress().getBuilding().trim()
                    : "";

            // 5.2. Lấy tên đường
            String street = (restaurantModel.getAddress().getStreet() != null)
                    ? restaurantModel.getAddress().getStreet().trim()
                    : "";

            // 5.3. Lấy mã bưu chính
            String zipcode = (restaurantModel.getAddress().getZipcode() != null)
                    ? restaurantModel.getAddress().getZipcode().trim()
                    : "";

            // 5.4. Lấy quận
            String boroughText = (restaurantModel.getBorough() != null)
                    ? restaurantModel.getBorough().trim()
                    : "";

            // 5.5. Lấy full địa chỉ thuần
            addressText = building + " " + street + " " + boroughText + ", New York," + zipcode;

            // 5.6. Bốc riêng mã tọa độ (kinh độ longitude; vĩ độ latitude) vật lý ra riêng để hứng dữ liệu
            if(restaurantModel.getAddress().getCoord() != null){
                coordList = restaurantModel.getAddress().getCoord();
            }

            // 5.7. Bốc trọn vẹn chuỗi link Google Maps xịn từ đĩa cứng MongoDB lên RAM
            if(restaurantModel.getAddress().getMapUrl() != null){
                mapUrlText = restaurantModel.getAddress().getMapUrl().trim();
            }
        }

        // 6. Tính toán số lượt chấm điểm (bình chọn) và điểm trung bình
        // 6.1. Khai báo 2 biến tạm
        int gradeCount = 0;         // Số lượt chấm điểm (bình chọn)
        double sumScore = 0.0;      // Tổng số điểm

        // 6.2. Duyệt vòng lập để tính số lượt chấm điểm (bình chọn) và tổng số điểm
        if(restaurantModel.getGrades() != null && !restaurantModel.getGrades().isEmpty()){
            gradeCount = restaurantModel.getGrades().size();    // Lấy tổng số lượt chấm điểm
            for(GradeModel gradeModel: restaurantModel.getGrades()){
                if(gradeModel.getScore() != null){
                    sumScore += gradeModel.getScore();
                }
            }
        }

        // 6.3. Tính điểm trung bình: làm tròn toán học lấy chuẩn xác 1 chữ số thập phân
        double average = (gradeCount > 0) ? (double) (Math.round((sumScore/gradeCount) * 10 ) / 10) : 0.0;

        // 7. Đóng gói hộp quà DTO tinh khiết vẹn toàn
        RestaurantDto dto = new RestaurantDto();
        dto.setId(restaurantModel.getId());
        dto.setRestaurantId(restaurantModel.getRestaurantId());
        dto.setName(restaurantModel.getName());
        dto.setBorough(restaurantModel.getBorough());
        dto.setCuisine(restaurantModel.getCuisine());
        dto.setFullAddress(addressText.trim());
        dto.setCoord(coordList);
        dto.setMapUrl(mapUrlText);          // Găm chặt dải băng thông link bản đồ vào hộp quà
        dto.setTotalGrades(gradeCount);
        dto.setAverageScore(average);

        // 8. Kết thúc hàm: Trả về hộp quà DTO tinh khiết
        return dto;
    }
}
