package com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto;

import com.nhl.baitap4_spring_mongodb_thymeleaf_import.model.AddressModel;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.model.RestaurantModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/* CHỨC NĂNG CLASS: Làm vỏ bọc trung gian biểu mẫu (Form Asset), phẳng hóa toàn bộ cấu trúc địa chỉ
lồng sâu dưới đĩa cứng thành các trường độc lập, gài bẫy bảo hiểm Validation chặn dữ liệu bẩn,
giúp thông mạch liên kết các thẻ nhập liệu ngoài giao diện HTML mà không làm lộ thực thể Model thô.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantFormDto {
    // 1. KHAI BÁO CÁC THUỘC TÍNH
    private String id;      // ID do MongoDB tự động tạo dưới dạng ObjectId
    private String restaurantId;       // Mã restaurant do TP New York (Đơn vị tạo database) cấp

    @NotBlank(message = "Tên nhà hàng không được để trống!")
    private String name;        // Tên restaurant

    @NotBlank(message = "Tên quận không được để trống!")
    private String borough;     // Quận (District)

    @NotBlank(message = "Thể loại ẩm thực không được để trống!")
    private String cuisine;     // Thể loại ẩm thực/Phong cách nấu ăn đặc trưng (American, Chinese, Italian, ...)

    @NotBlank(message = "Số nhà không được để trống!")
    private String building;    // Số nhà

    @NotBlank(message = "Tên đường không được để trống!")
    private String street;      // Tên đường

    @NotBlank(message = "Mã bưu điện không được để trống!")
    private String zipcode;      // Mã bưu chính (bưu điện)

    @NotEmpty(message = "Tọa độ địa lý - kinh dộ, vĩ độ - không được để trống!")
    private List<Double> coordList; // Khai sinh trường riêng biệt hứng mảng tọa độ [Kinh độ, Vĩ độ]

    // TIÊM MỚI BIẾN CHỮ THƯƠNG MẠI: Lưu giữ link bản đồ dẫn đường xịn bọc lót cho trang chi tiết
    private String mapUrl;

    // 2. HÀM 1: BỐC DỮ LIỆU TỪ DATABASE DƯỚI DẠNG MODEL --> CHUYỂN THÀNH DẠNG DTO
    public static RestaurantFormDto fromEntity(RestaurantModel restaurantModel){
        // 1. Kiểm tra dữ liệu rỗng
        if(restaurantModel == null){
            return null;
        }

        // 2. Khai báo 5 biến tạm để lưu dữ liệu
        String b = "";                              // Building
        String s = "";                              // Street
        String z = "";                              // Zipcode
        String url = "";                            // Map URL
        List<Double> coordList = new ArrayList<>(); // Tọa độ vị trí

        // 3. Lấy dữ liệu từ database RestaurantModel: Có kiểm tra Address rỗng
        if(restaurantModel.getAddress() != null){
            b = restaurantModel.getAddress().getBuilding();
            s = restaurantModel.getAddress().getStreet();
            z = restaurantModel.getAddress().getZipcode();
            if(restaurantModel.getAddress().getCoord() != null){
                coordList = restaurantModel.getAddress().getCoord();
            }

            // Bốc link Google Maps chuẩn từ đĩa cứng lưu tạm lên RAM
            if(restaurantModel.getAddress().getMapUrl() != null){
                url = restaurantModel.getAddress().getMapUrl();
            }
        }

        // 4. Đóng gói hộp quà Form DTO phẳng lỳ, bọc khít khao trường mapUrl
        RestaurantFormDto dto = new RestaurantFormDto();
        dto.setId(restaurantModel.getId());
        dto.setRestaurantId(restaurantModel.getRestaurantId());
        dto.setName(restaurantModel.getName());
        dto.setBorough(restaurantModel.getBorough());
        dto.setCuisine(restaurantModel.getCuisine());
        dto.setBuilding(b);
        dto.setStreet(s);
        dto.setZipcode(z);
        dto.setCoordList(coordList);
        dto.setMapUrl(url);             // Găm link bản đồ lên RAM DTO

        // 4. Kết thúc hàm: Trả về hộp quà DTO tinh khiết
        return dto;
    }

    // 3. HÀM 2: ĐÓNG GÓI DỮ LIỆU RESTAURANT TỪ DẠNG DTO SANG MODEL ĐỂ LƯU DATATBASE
    public RestaurantModel toEntity(){
        // 1. Khởi tạo đối tượng AddressModel thô phẳng theo hệ nhãn Setter để tránh bẫy lệch pha Constructor
        AddressModel addressModel = new AddressModel();
        addressModel.setBuilding(this.building);
        addressModel.setZipcode(this.zipcode);
        addressModel.setStreet(this.street);
        addressModel.setCoord(this.coordList);
        addressModel.setMapUrl(this.mapUrl);   // Nạp đạn link bản đồ dẫn đường chuẩn chuẩn hóa xuống Entity

        // 2. Kết thúc hàm: Trả về Restaurant dưới dạng Model
        return new RestaurantModel(
                this.id,
                this.restaurantId,
                this.name,
                this.borough,
                this.cuisine,
                addressModel,
                new ArrayList<>()  // Khởi tạo List rỗng cho thực thể GradeModel để tầng Service xử lý điểm
        );
    }
}
