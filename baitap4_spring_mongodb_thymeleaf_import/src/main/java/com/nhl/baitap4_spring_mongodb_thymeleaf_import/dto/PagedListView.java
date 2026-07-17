package com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/* CHỨC NĂNG CLASS: Đóng vai trò là một Adapter trung gian bọc lót giao diện (UI Asset), chuyên trách:
- Thu thập thịt dữ liệu DTO, siêu dữ liệu phân trang, trạng thái sắp xếp URL
- Và kích nổ thuật toán tính toán cửa sổ để vẽ bộ nút lật trang Thymeleaf co giãn Responsive.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedListView<T> {

    // 1. KHAI BÁO THUỘC TÍNH
    private List<T> content;                // Chứa thịt dữ liệu DS restaurants dưới dạng DtoModel
    private PagedResponse<T> pagination;    // Nhúng Hàm lấy DS Restaurants dười dạng DTO + tự động phân trang
    private String borough;                 // Lưu giữ keyword lọc theo Quận: khi users thao tác chuyển trang
    private String name;                    // TIÊM MỚI: Lưu cất từ khóa tìm kiếm tên nhà hàng để giữ trang khi lật
    private String sortBy;                  // Cột (field) đang thực hiện sắp xếp
    private String dir;                     // Hướng sắp xếp: tăng (asc) hay giảm dần (desc)
    private int startPage;                  // Tọa độ trang giới hạn bên trái: của Cửa sổ hiển thị
    private int endPage;                    // Tọa độ trang giới hạn bên phải: của Cửa sổ hiển thị

    // 2. HÀM ĐÚC KHUÔN ADAPTER VẠN NĂNG (BUILD FROM PAGE MODEL NGẦM TRÊN RAM)
    public static <M,D> PagedListView<D> buildFromPageModel (
            Page<M> pageModel,      // Hộp chứa thịt DS Restaurants dưới dạng Model
            Function<M,D> mapper,   // Hàm chuyển đổi DS Restaurants dưới dạng Model --> sang DTO
            String borough,         // Lưu giữ keyword lọc theo Quận: khi users thao tác chuyển trang
            String name,            // TIÊM MỚI: Hứng từ khóa tìm kiếm tên nhà hàng bốc từ RAM xuống
            String sortBy,          // Cột (field) đang thực hiện sắp xếp
            String dir,             // Hướng sắp xếp: tăng (asc) hay giảm dần (desc)
            int maxPagesToShow      // Giới hạn tổng số trang được phép hiển thị trên màn hình giao diện = 5
    ){
        // 1. Chuyển đổi dữ liệu thịt (thuộc tính List<T> content) Restaurant từ dạng Model sang DTO
        List<D> dtoList = pageModel.getContent().stream().map(mapper).toList();

        // 2. Khởi tạo và nạp thực thể chứa DS Restauarants dưới dạng DTO + Phân trang tự động thông qua Constructor
        PagedResponse<D> resultPagedResponse = new PagedResponse<>(
                dtoList,                        // Gán thịt dữ liệu DS restaurants dưới dạng DTO
                pageModel.getNumber(),          // Gán số trang hiện hành từ Spring Data (Hệ 0-indexed)
                pageModel.getSize(),            // Gán số items/page
                pageModel.getTotalElements(),   // Gán tổng số items
                pageModel.getTotalPages(),      // Gán tổng số trang
                pageModel.isLast()              // Gán trạng thái trang cuối
        );

        // 3. Tính: tổng số trang cần có để hiển thị hết DS Restaurants và số trang hiện hành
        int totalPages = resultPagedResponse.getTotalPages();
        int currentPage = resultPagedResponse.getPage();

        // 4. Tính giới hạn trang bên trái và bên phải. Tứ đó:
        // --> dịch chuyển Cửa sổ phân trang theo hướng: Đặt trang hiện hành vào giữa cửa sổ phân trang
        int start = Math.max(0,currentPage - maxPagesToShow/2);
        int end = Math.min(totalPages-1, start + maxPagesToShow - 1);

        // 5. Dồn Cửa sổ phân trang về biên: Nếu DS Restaurant không điền đầy trang cuối
        if(end - start + 1  < maxPagesToShow){
            start = Math.max(0, end - maxPagesToShow + 1);
        }

        // 6. Kết thúc hàm: Trả ra kết quả phân trang tự động bọc khít vẹn toàn thêm biến name vào Constructor
        return new PagedListView<>(dtoList, resultPagedResponse, borough, name, sortBy, dir, start, end);
    }
}