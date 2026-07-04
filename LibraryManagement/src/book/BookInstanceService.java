package book;

import java.io.*;
import java.util.ArrayList;

public class BookInstanceService implements Serializable {
    // KHAI BÁO BIẾN HẰNG ĐƯỜNG DẪN CỐ ĐỊNH
    public static final String BOOK_INSTANCES_DAT = "data/book_instances.dat";      // Cố định đường dẫn

    // KHAI BÁO BIẾN HẰNG TRẠNG THÁI SÁCH VẬT LÝ
    public static final int AVAILABLE = 0;          // Sẵn sàng trên kệ để cho mượn
    public static final int RENTED = 1;             // Đang cho mượn
    public static final int LOST_OR_DAMAGED = 2;    // Đã mất hay bị hỏng

    // KHAI BÁO BIẾN HẰNG CHỈ MỤC TÌM KIẾM
    public static final int SEARCH_BY_ID = 1;    // Tìm chính xác theo mã ID Sách vật lý (Ví dụ: SGK001-000)
    public static final int SEARCH_AVAILABLE_FIFO = 2;  // Tìm cuốn RẢNH đầu tiên theo mã đầu sách (Ví dụ: SGK001)

    // KHAI BÁO BIẾN TOÀN CỤC
    private static final ArrayList<BookInstance> bookInstanceList = new ArrayList<>();

    // HÀM GETTER
    public static ArrayList<BookInstance> getBookInstanceList(){
        return bookInstanceList;
    }

    // HÀM SETTER
    private static void addBookInstance(BookInstance bookInstance) {
        bookInstanceList.add(bookInstance);
    }


    // 1. CÁC HÀM SAVE FILE VÀ READ FILE BOOK_INSTANCE
    // Hàm ghi file: Tự lấy danh sách nội bộ để ghi, không cần truyền tham số từ ngoài vào
    public static void saveBookInstanceListToFile() {
        File folder = new File("data");
        if (!folder.exists()) folder.mkdir();       // Tự tạo thư mục data nếu chưa có.

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOK_INSTANCES_DAT))){
            oos.writeObject(bookInstanceList);
        } catch (IOException e) {
            System.out.println("Lỗi ghi file " + e.getMessage());
        }
    }


    // Hàm đọc file: Nạp thẳng dữ liệu vào biến toàn cục khi khởi động hệ thống
    public static ArrayList<BookInstance> readBookInstanceListFromFile() {
        ArrayList<BookInstance> tempList = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BOOK_INSTANCES_DAT))){
            tempList = (ArrayList<BookInstance>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Khởi tạo dữ liệu sách vật lý mới.");
        } catch (Exception e) {
            System.out.println("Lỗi nạp dữ liệu sách vật lý: " + e.getMessage());
        }
        return tempList;
    }


    // 2. HÀM TỰ ĐỘNG TẠO DANH SÁCH SÁCH VÂT LÝ SAU KHI CREATEBOOK THÀNH CÔNG
    public static void createBookInstance(String bookId, int quantity) throws Exception{
        try{
            for(int i = 0; i < quantity; i++){
                String id = String.format("%s-%03d",bookId,i);
                addBookInstance(new BookInstance(id,bookId,AVAILABLE));
            }
        } catch (Exception e) {
            System.out.println("Lỗi " + e.getMessage());
        }
    }


    // 2. HÀM ĐIỀU CHỈNH SỐ LƯỢNG SÁCH VẬT LÝ
    public static boolean adjustBookInstanceQuantity(String bookId, int oldQty, int newQty) throws Exception{
        try{
            if(newQty == oldQty){               // 1. Không thay đổi số lượng
                return true;
            } else if (newQty > oldQty) {       // 2. Điều chỉnh TẰNG số lượng
                for(int i = oldQty; i < newQty; i++){
                    String id = String.format("%s-%03d",bookId,i);
                    addBookInstance(new BookInstance(id,bookId,AVAILABLE));
                }
                return true;
            }else {                             // 3. Điều chỉnh GIẢM số lượng
                // 3.1. Tính toán số lượng cần giảm
                int diff = oldQty - newQty;

                // 3.2. Khai báo biến tạm để chứa các BookInstance dự kiến xóa
                ArrayList<BookInstance> tempBookInstance = new ArrayList<>();

                // 3.3. Duyệt ngược danh sách để đưa các bookInstance dư thừa vào danh sách tạm
                for(int i = bookInstanceList.size() - 1; i >= 0; i--){
                    BookInstance currentInst = bookInstanceList.get(i);
                    // Kiểm tra: Đúng mã đầu sách VÀ sách đang rảnh trên giá (status == 0)
                    if(currentInst.getBookId().equals(bookId) && currentInst.getStatus() == 0){
                        tempBookInstance.add(currentInst);
                    }
                    // Gom đủ số lượng cần giảm thì dừng lại ngay
                    if(tempBookInstance.size() == diff){
                        break;
                    }
                }

                // 3.4. Kiểm tra an toàn trước khi xóa
                if(tempBookInstance.size() < diff){
                    System.out.println("\u274C Lỗi: Lượng sách trong kho CÒN KHÔNG ĐỦ để điều chỉnh GIẢM. " +
                            "Vui lòng kiểm tra lại");
                    return false;
                }
                bookInstanceList.removeAll(tempBookInstance);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Lỗi " + e.getMessage());
        }
        return false;
    }


    // 3. HÀM XÓA SỐ LƯỢNG SÁCH VẬT LÝ
    public static boolean deleteBookInstanceByBookId(String bookId) throws Exception{
        try{
            // 1. QUÉT KIỂM TRA TOÀN BỘ (Chỉ kiểm tra: Đúng mã đầu sách VÀ sách đang cho mượn hoặc bị mất (status != 0)
            for(int i = 0; i < bookInstanceList.size();i++){
                BookInstance currentInst = bookInstanceList.get(i);

                // 1.1. Nếu trùng mã sách VÀ sách KHÔNG ở trên giá (status != 0) -> Hủy giao dịch ngay
                if(currentInst.getBookId().equals(bookId) && currentInst.getStatus() != 0){
                    System.out.println("\u26A0\uFE0F Hệ thống từ chối XÓA sách do vướng sách đang mượn.");
                    // Mã Unicode của ký tự cảnh báo tam giác vàng ⚠️ là \u26A0
                    // Nhưng thêm /uFEOF: để đảm bảo nó hiển thị đúng biểu tượng màu sắc trên mọi hệ điều hành
                    // (Windows/macOS) và không bị lỗi font ô vuông

                    return false;
                }
            }
            // 2. XÓA ĐỒNG LOẠT AN TOÀN (Chỉ chạy khi bước 1 vượt qua an toàn)
            bookInstanceList.removeIf(instance -> instance.getBookId().equals(bookId));
            return true;                // Trả về true báo hiệu kho sách vật lý đã dọn sạch trong bộ nhớ RAM
        } catch (Exception e) {
            System.out.println("Lỗi " + e.getMessage());;
        }
        return false;
    }


    // 4. HÀM TÌM KIẾM ĐA HÌNH
    public static BookInstance findBookInstance(String keyword, int searchType) throws Exception{
        // 1. Kiểm tra chuỗi rõng hoặc null
        if(keyword == null || keyword.trim().isEmpty()){
            return null;
        }

        // 2. Chuẩn hóa chuỗi nhập
        String cleanKeyword = keyword.trim().toUpperCase();

        // 3. Tìm kiếm
        for(BookInstance inst: bookInstanceList){
            // 3.1. Tìm chính xác theo mã ID Sách vật lý (Ví dụ: SGK001-000)
            if (searchType == SEARCH_BY_ID){
                if(inst.getId().equals(cleanKeyword)){
                    return inst;
                }
            } else if (searchType == SEARCH_AVAILABLE_FIFO) {
                // 3.2. Tìm cuốn RẢNH đầu tiên theo mã đầu sách (Ví dụ: SGK001)
                if(inst.getBookId().equals(cleanKeyword) && inst.getStatus() == AVAILABLE){
                    return inst;
                }
            }
        }
        return null;
    }


}
