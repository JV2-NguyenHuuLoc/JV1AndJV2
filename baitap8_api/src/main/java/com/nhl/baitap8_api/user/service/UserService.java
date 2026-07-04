package com.nhl.baitap8_api.user.service;


import com.nhl.baitap8_api.user.dto.UserPage;
import com.nhl.baitap8_api.user.model.UserForm;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j      // Lombok tự tiêm biến "log" tĩnh, bất biến và kích hoạt hệ thống ghi nhật ký hành trình (Logging)
@Service    // Đăng ký Class này vào kho của Spring là Trạm xử lý logic, nghiệp vụ (Service Layer)
public class UserService {

    // 1. KHAI BÁO KHO LƯU TRỮ DỮ LIỆU TRÊN RAM: Dùng bản nâng cấp ConcurrentHashMap để đáp ứng nhu cầu có hàng
    // trăm người cùng lúc truy cập để chỉnh sửa dữ liệu.
    private final Map<Long, UserForm> store = new java.util.concurrent.ConcurrentHashMap<>();


    // 2. KHAI BÁO CỔ MÁY TẠO ID TỰ ĐỘNG: Có 12 dữ liệu mẫu tự động tạo -> dữ liệu mới do user tạo: từ 13
    private final AtomicLong idSequence = new AtomicLong(13);


    // 3. KHAI BÁO BIẾN CẤU HÌNH PHÂN TRANG
    @Value("${app.users.page-size:5}")    // Dự phòng tình huống lở tay xóa app.users.page-size=5 trong file properties
    private int pageSize;


    // 4. HÀM TỰ ĐỘNG TẠO DỮ LIỆU MẪU 12 USERS: DÙNG NHÃN @PostConstruct
    @PostConstruct
    public void initSampleData() {
        // User 1
        UserForm u1 = new UserForm();
        u1.setId(1L);
        u1.setFirstName("Emily");
        u1.setLastName("Johnson");
        u1.setEmail("emily.johnson@gmail.com");
        u1.setPhone("0912345671");
        u1.setAvatarUrl("/images/default-avatar.webp");
        store.put(u1.getId(), u1);

        // User 2
        UserForm u2 = new UserForm();
        u2.setId(2L);
        u2.setFirstName("Michael");
        u2.setLastName("Smith");
        u2.setEmail("michael.smith@gmail.com");
        u2.setPhone("0912345672");
        u2.setAvatarUrl("/images/default-avatar.webp");
        store.put(u2.getId(), u2);

        // User 3
        UserForm u3 = new UserForm();
        u3.setId(3L);
        u3.setFirstName("Isabella");
        u3.setLastName("Anderson");
        u3.setEmail("isabella.anderson@gmail.com");
        u3.setPhone("0912345673");
        u3.setAvatarUrl("/images/default-avatar.webp");
        store.put(u3.getId(), u3);

        // User 4
        UserForm u4 = new UserForm();
        u4.setId(4L);
        u4.setFirstName("David");
        u4.setLastName("Brown");
        u4.setEmail("david.brown@gmail.com");
        u4.setPhone("0912345674");
        u4.setAvatarUrl("/images/default-avatar.webp");
        store.put(u4.getId(), u4);

        // User 5
        UserForm u5 = new UserForm();
        u5.setId(5L);
        u5.setFirstName("Emma");
        u5.setLastName("Jones");
        u5.setEmail("emma.jones@gmail.com");
        u5.setPhone("0912345675");
        u5.setAvatarUrl("/images/default-avatar.webp");
        store.put(u5.getId(), u5);

        // User 6
        UserForm u6 = new UserForm();
        u6.setId(6L);
        u6.setFirstName("James");
        u6.setLastName("Miller");
        u6.setEmail("james.miller@gmail.com");
        u6.setPhone("0912345676");
        u6.setAvatarUrl("/images/default-avatar.webp");
        store.put(u6.getId(), u6);

        // User 7
        UserForm u7 = new UserForm();
        u7.setId(7L);
        u7.setFirstName("Olivia");
        u7.setLastName("Davis");
        u7.setEmail("olvia.davis@gmail.com");
        u7.setPhone("0912345677");
        u7.setAvatarUrl("/images/default-avatar.webp");
        store.put(u7.getId(), u7);

        // User 8
        UserForm u8 = new UserForm();
        u8.setId(8L);
        u8.setFirstName("Alexander");
        u8.setLastName("Garcia");
        u8.setEmail("alex.garcia@gmail.com");
        u8.setPhone("0912345678");
        u8.setAvatarUrl("/images/default-avatar.webp");
        store.put(u8.getId(), u8);

        // User 9
        UserForm u9 = new UserForm();
        u9.setId(9L);
        u9.setFirstName("Sophia");
        u9.setLastName("Rodriguez");
        u9.setEmail("sophia.rod@gmail.com");
        u9.setPhone("0912345679");
        u9.setAvatarUrl("/images/default-avatar.webp");
        store.put(u9.getId(), u9);

        // User 10
        UserForm u10 = new UserForm();
        u10.setId(10L);
        u10.setFirstName("William");
        u10.setLastName("Martinez");
        u10.setEmail("william.mtz@gmail.com");
        u10.setPhone("0912345680");
        u10.setAvatarUrl("/images/default-avatar.webp");
        store.put(u10.getId(), u10);

        // User 11
        UserForm u11 = new UserForm();
        u11.setId(11L);
        u11.setFirstName("Charlotte");
        u11.setLastName("Hernandez");
        u11.setEmail("charlotte.h@gmail.com");
        u11.setPhone("0912345681");
        u11.setAvatarUrl("/images/default-avatar.webp");
        store.put(u11.getId(), u11);

        // User 12
        UserForm u12 = new UserForm();
        u12.setId(12L);
        u12.setFirstName("Daniel");
        u12.setLastName("Lopez");
        u12.setEmail("daniel.lopez@gmail.com");
        u12.setPhone("0912345682");
        u12.setAvatarUrl("/images/default-avatar.webp");
        store.put(u12.getId(), u12);

        log.info("Đã nạp thành công {} users mẫu vào bộ nhớ RAM!", store.size());
    }


    // 5. HÀM TÌM KIẾM TRANG: TÌM LỌC/SEARCH TRONG DS VÀ SO KHỚP -> TRẢ VỀ DỮ LIỆU TRONG 1 TRANG WEB
    // Thuật toán cốt lõi: Tìm kiếm không phân biệt hoa thường + Phân trang toán học vật lý
    public UserPage findPage(String query, int page){
        // BƯỚC 1: Chuẩn hóa từ khóa đầu vào
        String cleanQuery = (query == null) ? "" : query.trim().toLowerCase();

        // BƯỚC 2: Bộ lọc so khớp (Filter/Search)
        // B2.1. Khai báo biến lưu danh sách lọc/tìm kiếm được
        List<UserForm> filteredList = new ArrayList<>();

        // B2.2. Duyệt qua vòng lập để so khớp chuỗi nhập với họ, tên, phone, email
        for(UserForm user: store.values()){
            boolean isMatch = (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(cleanQuery)
            ||(user.getLastName() != null && user.getLastName().toLowerCase().contains(cleanQuery))
            ||(user.getFullName() != null && user.getFullName().toLowerCase().contains(cleanQuery))
            ||(user.getEmail() != null && user.getEmail().toLowerCase().contains(cleanQuery))
            ||(user.getPhone() != null && user.getPhone().toLowerCase().contains(cleanQuery)));

            if(isMatch){
                filteredList.add(user);
            }
        }

        // BƯỚC 3: Tính toán các thông số phân trang bằng toán học phẳng
        // B3.1. Tính số phần tử tìm được và tổng só trang
        int totalItems = filteredList.size();        // Tổng số items tìm được
        int totalPages = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems/pageSize);  // Tổng số trang

        // B3.2. Ép số trang về khoảng an toàn (Bound Check): Đề phòng hacker nhập trên URL trang số âm hoặc quá lớn
        if(page < 1) {
            page = 1;
        }
        if(page > totalPages){
            page = totalPages;
        }

        // BƯỚC 4: Cắt danh sách tìm được, add vào 1 trang (subList)
        int fromindex = (page - 1) * pageSize;      // Chỉ số index item đầu tiên của trang (subList)
        int toIndex = Math.min(fromindex + pageSize, totalItems);       // Chỉ số index item cuối của trang (subList)

        List<UserForm> pageContent = new ArrayList<>(filteredList.subList(fromindex,toIndex));

        // BƯỚC 5. Đóng gói nạp vào hộp UserPage và trả về
        UserPage userPage = new UserPage();
        userPage.setUsers(pageContent);
        userPage.setQuery(query);
        userPage.setPage(page);
        userPage.setPageSize(pageSize);
        userPage.setTotalItems(totalItems);
        userPage.setTotalPages(totalPages);

        // BƯỚC 6: return
        return userPage;
    }


    // 6. HÀM LẤY TOÀN BỘ DS USERS TRONG RAM
    public List<UserForm> findAll(){
        return new ArrayList<>(store.values());
    }


    // 7. HÀM TÌM KIẾM USER THEO ID (BỌC TRONG HỘP AN TOÀN OPTIONAL)
    public Optional<UserForm> findById(Long id){
        return Optional.ofNullable(store.get(id));
    }


    // 8. HÀM TẠO MỚI USER VÀ CẤP ID TỰ DỘNG TĂNG CHO USER TIẾP THEO
    public UserForm create(UserForm userForm){
        Long newId = idSequence.getAndIncrement();      // Dập số ID tự động bắt đầu từ 13
        userForm.setId(newId);          // Gán Id cho UserForm
        store.put(newId, userForm);     // Tạo User mới vào RAM
        log.info("Đã tạo mới thành công User: {}", newId);
        return userForm;
    }


    // 9. HÀM CẬP NHẬT THÔNG TIN USER VÀ BẨY LỖI GIỮ LẠI ẢNH AVATAR CŨ NẾU KHÔNG UPLOAD ẢNH MỚI
    public UserForm update(Long id, UserForm userForm){
        // 1. Lấy thông tin cũ của User
        UserForm oldUser = store.get(id);

        // 2. Kiểm tra User rỗng
        if(oldUser == null){
            throw new NoSuchElementException("Không tìm thấy người dùng có ID: " + id);
        }

        // 3. Bảo vệ trục tọa độ User
        userForm.setId(id);

        // 4. Kiểm tra User mới gửi lên không có ảnh avatar: lấy lại ảnh cũ
        if(userForm.getAvatarUrl() == null || userForm.getAvatarUrl().isEmpty()){
            userForm.setAvatarUrl(oldUser.getAvatarUrl());
        }

        // 5. Ghi đè thông tin User mới vào RAM
        store.put(id,userForm);

        // 6. Báo cáo KQ
        log.info("Đã cập nhật thành công thông tin User ID: " + id);

        // 7. Thoát
        return userForm;
    }


    // 10. HÀM XÓA CỨNG USER KHỎI RAM
    public void delete(Long id){
        // 1. Kiểm tra User có tồn tại trong RAM
        if(!store.containsKey(id)){
            throw new NoSuchElementException("Không tìm thấy người dùng có ID: " + id);
        }

        // 2. Xóa cứng User khỏi RAM
        store.remove(id);

        // 3. Báo cáo KQ
        log.info("Đã xóa sổ hoàn toàn User có ID = {} khỏi hệ thống", id);
    }


}
