package com.nhl.baitap4_spring_mongodb_thymeleaf_import.service;

import com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto.ImportResultDto;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto.PagedListView;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto.RestaurantDto;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto.RestaurantFormDto;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.model.RestaurantModel;
import com.nhl.baitap4_spring_mongodb_thymeleaf_import.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service            // Đăng ký căn phòng ban Nghiệp vụ, ép Spring quản lý vòng đời Bean ngầm
@RequiredArgsConstructor   // Khí tài Lombok tự động đúc ngầm Constructor tiêm tài nguyên (DI) cứng lên RAM
@Slf4j          // Tiêm khẩu thần công Logger, kích nổ các dòng ghi vết hệ thống kiểm chứng mạch dữ liệu
public class RestaurantService {
    // 1. KHAI BÁO TIÊM CÔNG CỤ TỪ REPOSITORY
    private final RestaurantRepository restaurantRepository;

    // 2. KHAI BÁO CÔNG CỤ OBJECT_MAPPER: Đóng vai trò là cỗ máy giải mã (Deserializer) tối cao,
    // chuyên trách băm chuỗi văn bản dạng JSON thô đúc khuôn ép thành thực thể Class Java Model sạch.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 3. KHAI BÁO BIẾN HẰNG
    // 3.1. Kích thước gói dữ liệu mỗi lần Save/Upload xuống ổ cứng
    private static final int BATCH_SIZE = 10;

    // 3.2. Giới hạn số pages hiển thị trên màn hình giao diện
    private static final int MAX_PAGES_TO_SHOW = 5;

    // 4. HÀM 1: PHÓNG LUỒNG STREAM BĂM NHỎ FILE LÔ (BATCH UPLOAD)
    public ImportResultDto handleImport(MultipartFile file){
        // 1. Kiểm tra file rỗng
        if(file == null || file.isEmpty()){
            return ImportResultDto.fail("Lỗi File rỗng: Vui lòng chọn một file dữ liệu dạng NDJSON sạch!");
        }

        // 2. Khởi tạo BufferedReader đọc băm nhỏ từng dòng chữ đơn: Bọc trong Try ... catch
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // 3. Khai báo biến phụ: Tổng số thịt dữ liệu và số lượt gói (batch) dữ liệu được Save xuống ổ cứng
            int totalCount = 0;
            int batchCount = 0;

            // 4. Khai báo 1 List DS thịt dữ liệu trong 1 gói (batch)
            List<RestaurantModel> batchList = new ArrayList<>();

            // 5. Khai báo biến chạy phụ --> dùng trong quá trình đọc - ghi dữ liệu
            String line = "";

            // 6. Duyệt vòng lập while để đọc/ghi dữ liệu
            while ((line = reader.readLine()) != null){     // Khi dữ liệu vẫn còn (dữ liệu KHÁC rỗng
                // 7. Kiểm tra nếu dữ liệu hết, thì bỏ qua việc đọc/ghi dữ liệu, tránh nhập dữ liệu rác vào HT
                if(line.trim().isEmpty()){
                    continue;
                }

                // 8. Ghi dữ liệu xuống RAM: Bộ dịch Jackson bốc chữ thô JSON đúc khuôn thành Model nhét vào RAM
                RestaurantModel restaurantModel = objectMapper.readValue(line, RestaurantModel.class);

                // 9. Thêm thịt dữ liệu vào List DS
                batchList.add(restaurantModel);

                // 10. Tăng số thịt dữ liệu
                totalCount ++;

                // 11. Kiểm tra đạt ngưỡng giới hạn số lượng thịt dữ liệu trong 1 batch trên RAM
                if(batchList.size() >= BATCH_SIZE){
                    // 12. Tăng số lượt gói (batch) dữ liệu
                    batchCount ++;

                    //13. Dùng công cụ của tầng Repository: Save gói (batch) thịt dữ liệu xuống đĩa cứng
                    restaurantRepository.saveAll(batchList);

                    // 14. In thông báo rực lửa lên Console IntelliJ để kiểm chứng việc Ghi dữ liệu:
                    // Khi Production: Có thể "comment" dòng lệnh này các dòng này.
                    System.out.println("Ghi thành công Lô dữ liệu số " + batchCount + " gồm: "
                            + batchList.size() + " dòng xuống đĩa cứng!");

                    // 15. Xuất thông báo cho file Log
                    log.info("Mạch đĩa cứng thông suốt cho lô số {}", batchCount);

                    // 16. Xóa sạch List DS thịt dữ liệu để giải phóng RAM --> tạo gói dữ liệu mới
                    batchList.clear();
                }
            }

            // 17: Quét sạch số thịt dữ liệu còn sót lại cuối vòng while (do batchList.size() < BATCH_SIZE)
            if(!batchList.isEmpty()){
                // 18. Tăng số lượt gói (batch) dữ liệu
                batchCount ++;

                //19. Dùng công cụ của tầng Repository: Save gói (batch) thịt dữ liệu xuống đĩa cứng
                restaurantRepository.saveAll(batchList);

                // 20. In thông báo rực lửa lên Console IntelliJ để kiểm chứng việc Ghi dữ liệu:
                // Khi Production: Có thể "comment" dòng lệnh này các dòng này.
                System.out.println("Ghi thành công Lô dữ liệu số " + batchCount + " gồm: "
                        + batchList.size() + " dòng xuống đĩa cứng!");

                // 21. Xuất thông báo cho file Log
                log.info("Mạch đĩa cứng thông suốt cho lô số {}", batchCount);

                // 22. Xóa sạch List DS thịt dữ liệu để giải phóng RAM --> tạo gói dữ liệu mới
                batchList.clear();
            }

            // 23. Kết thúc hàm: Gọi hàm ImportResultDto.ok (từ DTO) để trả kết quả import file THÀNH CÔNG
            return ImportResultDto.ok(totalCount);

        } catch (Exception e) {
            // 24. Gọi hàm ImportResultDto.fail (từ DTO) để trả kết quả import file THẤT BẠI
            return ImportResultDto.fail("Lỗi biên dịch gãy ống dẫn trong khi nạp Lô dữ liệu: "
                    + e.getMessage());
        }
    }

    // 5. HÀM 2 NÂNG CẤP TỐI CAO: TÌM KIẾM LỌC KÉP [QUẬN + TÊN] + PHÂN TRANG + SORT ĐA THUỘC TÍNH
    public PagedListView<RestaurantDto> findPage(String borough, String name, Pageable pageable, String sortBy, String dir){
        // 1. Chuẩn hóa chuỗi tìm kiếm kép chặn sạch khoảng trắng rác bảo hiểm bộ nhớ RAM
        String safeBorough = (borough == null) ? "" : borough.trim();
        String safeName = (name == null) ? "" : name.trim();

        // 2. Chọc ống dẫn xuống Repository kích nổ hàm tìm kiếm kép And tuyệt đối
        Page<RestaurantModel> pageModel = restaurantRepository.findByBoroughContainingIgnoreCaseAndNameContainingIgnoreCase(
                safeBorough, safeName, pageable);

        // 3. Trả kết quả tìm kiếm: Được bọc qua hộp quà Adapter, chuyển tải trọn vẹn cả bộ đôi biến lọc ra UI
        return PagedListView.buildFromPageModel(pageModel, RestaurantDto::fromEntity, safeBorough, safeName, sortBy, dir,
                MAX_PAGES_TO_SHOW);
    }

    // 6. HÀM 3: TRUY VẾT BIỂU MẪU SỬA THEO MÃ ID NGHIỆP VỤ NEW YORK (CHỐNG LỆCH PHA ID)
    public RestaurantFormDto getFormByRestaurantId(String restaurantId){
        // 1. Gọi hàm tìm kiếm Restaurant theo restaurantId (dạng Model) từ tầng Repository
        RestaurantModel restaurantModel = restaurantRepository.findFirstByRestaurantId(restaurantId);

        // 2. Kiểm tra tìm không có
        if(restaurantModel == null){
            return null;
        }

        // 3. Kết thúc hàm: Trả về kết quả restaurant dưới dạng DTO (chuyển từ Model --> DTO)
        return RestaurantFormDto.fromEntity(restaurantModel);
    }

    // 7. HÀM 4: CẬP NHẬT TỪNG PHẦN (PARTIAL UPDATE) BẢO HIỂM MẢNG LỒNG SÂU
    public RestaurantFormDto updateDetail(String restaurantId, RestaurantFormDto formDto){
        // 1. Tìm Restaurant dười dạng Model theo tham số restaurantId
        RestaurantModel existing = restaurantRepository.findFirstByRestaurantId(restaurantId);

        // 2. Kiểm tra tìm không có
        if(existing == null){
            return null;
        }

        // 3. Chuyển thông tin restaurant trên giao diện UPDATE RESTAURANT: từ dạng DTO --> sang Model
        RestaurantModel changes = formDto.toEntity();

        // 4. Gán thông tin Restaurant đã cập nhật từ giao diện UPDATE RESTAURANT
        if(changes.getName() != null) existing.setName(changes.getName());
        if(changes.getBorough() != null) existing.setBorough(changes.getBorough());
        if(changes.getCuisine() != null) existing.setCuisine(changes.getCuisine());

        // 5. Cập nhật cấu trúc con địa chỉ lồng sâu
        if(changes.getAddress() != null){
            if(existing.getAddress() == null){     // Nếu địa chỉ cũ == null
                existing.setAddress(changes.getAddress());  // update địa chỉ mới.
            } else {        // địa chỉ cũ >< null
                existing.getAddress().setBuilding(changes.getAddress().getBuilding());  // update số nhà
                existing.getAddress().setStreet(changes.getAddress().getStreet());      // update đường
                existing.getAddress().setZipcode(changes.getAddress().getZipcode());    // update zipCode
                existing.getAddress().setCoord(changes.getAddress().getCoord());        // update vị trí
            }
        }

        // LƯU Ý: Không cho người dùng update Mảng existing.getGrades() --> lý do: do hệ tống tự động tính

        // 6. Kết thúc hàm: Gọi hàm save từ tầng Repository để lưu bản đã cập nhật
        return RestaurantFormDto.fromEntity(restaurantRepository.save(existing));
    }
}
