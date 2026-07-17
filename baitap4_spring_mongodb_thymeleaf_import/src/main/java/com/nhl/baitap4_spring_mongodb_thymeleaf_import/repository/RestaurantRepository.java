package com.nhl.baitap4_spring_mongodb_thymeleaf_import.repository;

import com.nhl.baitap4_spring_mongodb_thymeleaf_import.model.RestaurantModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository // CHỐT CHẶN HẠ TẦNG: Đánh nhãn định danh cho Spring Boot biết đây là phòng ban chuyên trách
// chọc ống dẫn trực tiếp xuống đĩa cứng, ép hệ thống tự động đúc khuôn các hàm cơ bắp
// lưu/xóa (CRUD) ngầm lên RAM và tự động quản lý vòng đời (Bean) vẹn toàn.

/* 1. EXTENDS MONGOREPOSITORY<RESTAURANTMODEL, STRING>:
- Đây là cú pháp kế thừa quyền lực của Spring Data. Bạn ra chỉ thị tối cao cho CPU: "Tôi muốn interface này
được thừa hưởng toàn bộ kho đạn tính năng có sẵn của framework. RestaurantModel khai báo cho hệ thống biết
Class này ánh xạ xuống đĩa cứng, còn String định vị kiểu dữ liệu của trường @Id vật lý là một chuỗi văn bản sạch".
- Nhờ dòng này, bạn tự động có sẵn các hàm .saveAll(), .findAll(), .delete() mà không cần
viết một dòng code thô nào.
 */
public interface RestaurantRepository extends MongoRepository<RestaurantModel, String> {

    /* 2. HÀM 1 - FINDFIRSTBYRESTAURANTID:
    - Cơ chế vật lý ngầm định: Đây là kỹ thuật tạo hàm thông minh dựa trên tên gọi (Query Method). Khi bạn đặt
    tên hàm bắt đầu bằng cụm từ findFirstBy..., Spring Data MongoDB ở tầng ngầm sẽ tự động phân rã chuỗi chữ,
    dịch chuyển cấu trúc thành một câu lệnh truy vấn vật lý chọc thẳng xuống đĩa cứng:
    db.restaurants.findOne({"restaurant_id": restaurantId}).
    - Mục đích thực chiến: Tìm kiếm theo mã nghiệp vụ New York cấp (ví dụ: "40356151"), nếu bạn dùng
    hàm .findById() mặc định của framework, CPU sẽ bị lừa chọc vào trường _id vật lý dạng ObjectId của
    MongoDB, dẫn đến hệ thống bị mù dữ liệu và văng lỗi not-found trắng xóa màn hình.
    Hàm này chính là tấm khiên bảo hiểm triệt hạ quả mìn đó vĩnh viễn.
     */
    RestaurantModel findFirstByRestaurantId(String restaurantId);

    /* 3. HÀM 2 - TÌM KIẾM LỌC KÉP QUẬN HUYỆN VÀ TÊN THƯƠNG HIỆU NÂNG CAO:
        - Containing: Tương đương toán tử LIKE %từ_khóa% trong SQL giúp tìm gần đúng chuỗi văn bản.
        - IgnoreCase: Triệt hạ phân biệt chữ hoa chữ thường, bảo hiểm sạch lưới dữ liệu đầu vào hành chính.
        - Phối hợp chặt chẽ toán tử logic And: Ép Spring Boot tự động băm nhỏ, đúc khuôn câu lệnh truy vấn kép:
        db.restaurants.find({"borough": /.../i, "name": /.../i}) [2.2].
        - Tham số đầu vào: Bắt buộc truyền đầy đủ bộ đôi biến String borough kẹp String name để hứng thịt dữ liệu từ RAM!
    */
    Page<RestaurantModel> findByBoroughContainingIgnoreCaseAndNameContainingIgnoreCase(
            String borough, String name, Pageable pageable);
}