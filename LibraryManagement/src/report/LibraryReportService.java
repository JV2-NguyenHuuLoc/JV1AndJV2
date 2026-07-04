package report;

import java.io.Serializable;
import book.Book;
import book.BookService;
import book.BookInstance;
import book.BookInstanceService;
import loanSlip.LoanSlip;
import loanSlip.LoanSlipService;
import loanSlip.LoanDetail;
import reader.Reader;
import reader.ReaderService;
import user.User;
import user.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class LibraryReportService implements Serializable {
    // KHAI BÁO BIẾN HẰNG PHÂN LOẠI PHIẾU MƯỢN DÙNG TRONG BÁO CÁO
    private static final int TYPE_OTHER = -1;       // Các trường hợp khác: Phiếu lỗi
    private static final int TYPE_BORROWING = 0;    // Phiếu đang mượn
    private static final int TYPE_OVERDUE = 1;      // Phiếu trả bị trễ hạn
    private static final int TYPE_LOST_BOOK = 2;    // Phiếu trả bị mất/hỏng sách


    // 1. HÀM BÁO CÁO SỐ LƯỢNG SÁCH ĐANG MƯỢN
    public static void reportRentedBooks(){
        try{
            // 1. Gọi danh sách kho vật lý
            ArrayList<BookInstance> instList = BookInstanceService.getBookInstanceList();

            // 2. Kiểm tra danh sách kho vật lý rỗng hoặc trống
            if(instList == null || instList.isEmpty()){
                System.out.print("\u274C Kho sách trống!");
                return;
            }

            // 3. Khởi tạo 3 biến tích lũy tổng toàn cục cho dòng SUM cuối bảng
            int totalBooksRenting = 0;      // Tổng số thứ tự dòng = tổng số sách đang cho thuê
            int inTimeBooksSum = 0;         // Tổng số sách đang cho thuê CÒN TRONG HẠN
            int overdueBooksSum = 0;        // Tổng số sách đang cho thuê QUÁ HẠN NHƯNG READER CHƯA TRẢ

            // 4. In tiêu đề cột báo cáo kết quả
            System.out.println("BÁO CÁO TỔNG HỢP SÁCH ĐANG CHO THUÊ");
            System.out.println(String.format("%-6s | %-15s | %-35s | %-12s | %15s | %15s",
                    "STT", "MÃ VẠCH", "TÊN SÁCH", "MÃ ĐỘC GIẢ", "SÁCH CÒN HẠN", "SÁCH TRỄ HẠN"));

            // 5. Quét vòng lập danh sách Sách vật lý
            for(BookInstance inst: instList){
                // 6. Kiểm tra từng phần tử sách vật lý rỗng hoặc trống
                if(inst == null || inst.getId() == null){
                    continue;       // Bỏ qua -> quét phần tử tiếp theo
                }

                // 7. Kiểm tra trạng thái sách vật lý : KHÔNG PHẢI ĐANG MƯỢN
                if(inst.getStatus() != BookInstanceService.RENTED){
                    continue;       // Bỏ qua -> quét phần tử tiếp theo
                }

                // 8. Các diều kiện sách vật lý hợp lệ --> Lấy tiêu đề sách
                String bookId = inst.getBookId();           // Lấy mã đầu sách
                Book book = BookService.findBook(bookId);   // Tìm đầu sách
                String title = (book != null) ? book.getTitle() : "Đầu sách đã bị xóa ngầm"; // Lấy tên sách

                // 9. Tìm người mượn sách
                String readerId = "Không rõ";       //  Khởi tạo biến tạm hứng readerId

                // 10. Khai báo 2 biến Str hiển thị dòng hiện tại
                String colInTime = "-", colOverdue = "-";

                // 11. Mở vòng lập phụ quét danh sách phiếu mượn
                for(LoanSlip loan: LoanSlipService.getLoanList()){
                    // 12. Kiểm tra các phiếu rỗng hoặc phiếu đang mượn (chưa trả)
                    if(loan == null || loan.getDetails() == null || loan.getStatus() != LoanSlip.BORROWING){
                        continue;       // Bỏ qua -> quét phần tử tiếp theo
                    }

                    // 13. Các điều kiện hợp lệ -> Quét giỏ hàng trong phiếu đó
                    for(LoanDetail detail: loan.getDetails()) {
                        // 14. Kiểm tra trùng mà sách vật lý và khác null
                        if (detail != null && detail.getBookInstanceId().equalsIgnoreCase(inst.getId())) {
                            // 15. Lấy mã đọc giá
                            readerId = loan.getReaderId();

                            // 16. Kiểm tra phiếu mượn có ngày hạn trả và ngày đó nằm TRƯỚC ngày hôm nay (Quá hạn)
                            if(loan.getExpectedReturnDate() != null
                                    && loan.getExpectedReturnDate().isBefore(java.time.LocalDate.now())){
                                colOverdue = "X";
                                overdueBooksSum ++;
                            }else {
                                colInTime = "X";
                                inTimeBooksSum ++;
                            }
                            break;      // Thoát sớm vòng lập duyệt danh sách chi tiết NGAY KHI tìm được ReaderId
                        }
                    }

                    // 17. Kiểm tra thoát vòng lặp phiếu mượn NGAY KHI đã tìm thấy đúng người mượn [🛡️]
                    if (!readerId.equalsIgnoreCase("Không rõ")) {
                        break;
                    }
                }

                // 18. Tăng số thứ tự = Tổng số sách đang cho thuê
                totalBooksRenting ++;

                // 19. In thông báo dòng dữ liệu
                System.out.println(String.format("%-6d | %-15s | %-35s | %-12s | %15s | %15s",
                        totalBooksRenting, inst.getId(), LoanSlipService.truncate(title, 35), readerId,
                        colInTime, colOverdue));
            }

            // 20. Kiểm tra không tìm được sách vật lý nào đang cho mượn
            if(totalBooksRenting == 0){
                System.out.println("\u274C Chưa có sách đang cho thuê.");
                return;
            }

            // 21. In đường kẻ ngang trước dòng SUM
            System.out.println("-------------------------------------------------------------------------------------" +
                    "----------------------");

            // 22. Khai báo hằng số mã màu ANSI nội bộ dòng SUM
            String ANSI_BOLD = "\u001B[1m";     // Mở chế độ in đậm
            String ANSI_RESET = "\u001B[0m";    // Reset chế độ in đậm

            // 23 Bật mã đậm ở đầu dòng và tắt mã đậm ở cuối dòng.
            System.out.print(ANSI_BOLD);            // Bật mã in đậm

            // 24. In dòng SUM
            System.out.println(String.format("%-6d | %-15s | %-35s | %-12s | %15s | %15s",
                    totalBooksRenting, "TỔNG CỘNG", "", "",inTimeBooksSum, overdueBooksSum));
            System.out.print(ANSI_RESET);           // Reset mã in đậm

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc xuất báo cáo tổng hợp sách đang cho mượn "+ e.getMessage());
        }
    }


    // 2. HÀM XUẤT BÁO CÁO SỐ LƯỢNG SÁCH ĐANG MƯỢN RA FILE EXCEL
    public static void exportRentedBooksToExcel(){
        try{
            // 1. Gọi danh sách kho vật lý
            ArrayList<BookInstance> instList = BookInstanceService.getBookInstanceList();

            // 2. Kiểm tra danh sách kho vật lý rỗng hoặc trống
            if(instList == null || instList.isEmpty()){
                System.out.print("\u274C Kho sách trống!");
                return;
            }

            // 3. Khởi tạo 3 biến tích lũy tổng toàn cục cho dòng SUM cuối bảng
            int totalBooksRenting = 0;      // Tổng số thứ tự dòng = tổng số sách đang cho thuê
            int inTimeBooksSum = 0;         // Tổng số sách đang cho thuê CÒN TRONG HẠN
            int overdueBooksSum = 0;        // Tổng số sách đang cho thuê QUÁ HẠN NHƯNG READER CHƯA TRẢ

            // 4. KHỞI TẠO CẤU TRÚC FILE EXCEL
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Thống kê sách đang mượn");
            int rowIndex = 0;       // Khởi tạo biến đếm dòng

            // 5. Ghi tiêu đề cột vào dòng 0
            org.apache.poi.ss.usermodel.Row rowTitle = sheet.createRow(rowIndex++);
            rowTitle.createCell(0).setCellValue("BÁO CÁO TỔNG HỢP SÁCH ĐANG CHO THUÊ");
            org.apache.poi.ss.usermodel.Row rowHeader = sheet.createRow(rowIndex++);
            rowHeader.createCell(0).setCellValue("STT");
            rowHeader.createCell(1).setCellValue("MÃ VẠCH SÁCH");
            rowHeader.createCell(2).setCellValue("TÊN SÁCH");
            rowHeader.createCell(3).setCellValue("MÃ ĐỘC GIẢ");
            rowHeader.createCell(4).setCellValue("SÁCH CHƯA ĐẾN HẠN TRẢ");
            rowHeader.createCell(5).setCellValue("SÁCH TRỄ HẠN");

            // 6. Quét vòng lập danh sách Sách vật lý
            for(BookInstance inst: instList){
                // 6. Kiểm tra từng phần tử sách vật lý rỗng hoặc trống
                if(inst == null || inst.getId() == null){
                    continue;       // Bỏ qua -> quét phần tử tiếp theo
                }

                // 7. Kiểm tra trạng thái sách vật lý : KHÔNG PHẢI ĐANG MƯỢN
                if(inst.getStatus() != BookInstanceService.RENTED){
                    continue;       // Bỏ qua -> quét phần tử tiếp theo
                }

                // 8. Các diều kiện sách vật lý hợp lệ --> Lấy tiêu đề sách
                String bookId = inst.getBookId();           // Lấy mã đầu sách
                Book book = BookService.findBook(bookId);   // Tìm đầu sách
                String title = (book != null) ? book.getTitle() : "Đầu sách đã bị xóa ngầm"; // Lấy tên sách

                // 9. Tìm người mượn sách
                String readerId = "Không rõ";       //  Khởi tạo biến tạm hứng readerId

                // 10. Khai báo 2 biến Str hiển thị dòng hiện tại
                String colInTime = "-", colOverdue = "-";

                // 11. Mở vòng lập phụ quét danh sách phiếu mượn
                for(LoanSlip loan: LoanSlipService.getLoanList()){
                    // 12. Kiểm tra các phiếu rỗng hoặc phiếu đang mượn (chưa trả)
                    if(loan == null || loan.getDetails() == null || loan.getStatus() != LoanSlip.BORROWING){
                        continue;       // Bỏ qua -> quét phần tử tiếp theo
                    }

                    // 13. Các điều kiện hợp lệ -> Quét giỏ hàng trong phiếu đó
                    for(LoanDetail detail: loan.getDetails()) {
                        // 14. Kiểm tra trùng mà sách vật lý và khác null
                        if (detail != null && detail.getBookInstanceId().equalsIgnoreCase(inst.getId())) {
                            // 15. Lấy mã đọc giá
                            readerId = loan.getReaderId();

                            // 16. Kiểm tra phiếu mượn có ngày hạn trả và ngày đó nằm TRƯỚC ngày hôm nay (Quá hạn)
                            if(loan.getExpectedReturnDate() != null
                                    && loan.getExpectedReturnDate().isBefore(java.time.LocalDate.now())){
                                colOverdue = "X";
                                overdueBooksSum ++;
                            }else {
                                colInTime = "X";
                                inTimeBooksSum ++;
                            }
                            break;      // Thoát sớm vòng lập duyệt danh sách chi tiết NGAY KHI tìm được ReaderId
                        }
                    }

                    // 17. Kiểm tra thoát vòng lặp phiếu mượn NGAY KHI đã tìm thấy đúng người mượn [🛡️]
                    if (!readerId.equalsIgnoreCase("Không rõ")) {
                        break;
                    }
                }

                // 18. Tăng số thứ tự = Tổng số sách đang cho thuê
                totalBooksRenting ++;

                // 19. In thông báo dòng dữ liệu
                org.apache.poi.ss.usermodel.Row rowData = sheet.createRow(rowIndex++);
                rowData.createCell(0).setCellValue(totalBooksRenting);
                rowData.createCell(1).setCellValue(inst.getId());
                rowData.createCell(2).setCellValue(title);
                rowData.createCell(3).setCellValue(readerId);
                rowData.createCell(4).setCellValue(colInTime);
                rowData.createCell(5).setCellValue(colOverdue);
            }

            // 20. Kiểm tra không tìm được sách vật lý nào đang cho mượn
            if(totalBooksRenting == 0){
                System.out.println("\u274C Chưa có sách đang cho thuê.");
                return;
            }

            // 21. In đường kẻ ngang trước dòng SUM
            org.apache.poi.ss.usermodel.Row rowLine = sheet.createRow(rowIndex++);
            rowLine.createCell(0).setCellValue("---------------------------------------------------------------" +
                    "--------------------------------------------");

            // 22. KHỞI TẠO PHÔNG CHỮ IN ĐẬM TRONG EXCEL
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();      // Khởi tạo font mới
            boldFont.setBold(true);             // Gán font in đậm
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();   // Khởi tạo style cho Cell
            boldStyle.setFont(boldFont);        // Gán style của cell là in đậm

            // 23. Tạo dòng SUM (IN ĐẬM) tổng kết đối soát cuối bảng Excel
            org.apache.poi.ss.usermodel.Row rowSum = sheet.createRow(rowIndex++);

            // 23.1. Ô 0 (STT): Đổ tổng số sách vật lý đang cho thuê và đặt kiểu in đậm
            org.apache.poi.ss.usermodel.Cell cell0 = rowSum.createCell(0);  // Khởi tạo cell 0
            cell0.setCellValue(totalBooksRenting);        // Gán giá trị cho cell0
            cell0.setCellStyle(boldStyle);

            // 23.2. Ô 1 (TỔNG CỘNG): Đổ chữ tổng cộng
            org.apache.poi.ss.usermodel.Cell cell1 = rowSum.createCell(1);  // Khởi tạo cell 1
            cell1.setCellValue("TỔNG CỘNG");        // Gán giá trị cho cell1
            cell1.setCellStyle(boldStyle);

            // 23.3. Ô 2 và 3 (TÊM SÁCH và MÃ ĐỌC GIẢ):
            rowSum.createCell(2).setCellValue("");
            rowSum.createCell(3).setCellValue("");

            // 23.4. Ô 4 (CỘNG SỐ SÁCH CÒN HẠN)
            org.apache.poi.ss.usermodel.Cell cell4 = rowSum.createCell(4);  // Khởi tạo cell 4
            cell4.setCellValue(inTimeBooksSum);        // Gán giá trị cho cell4
            cell4.setCellStyle(boldStyle);

            // 23.5. Ô 5 (CỘNG SỐ SÁCH TRỄ HẠN)
            org.apache.poi.ss.usermodel.Cell cell5 = rowSum.createCell(5);  // Khởi tạo cell 5
            cell5.setCellValue(overdueBooksSum);        // Gán giá trị cho cell5
            cell5.setCellStyle(boldStyle);

            // 24. MỞ LUỒNG TỰ ĐỘNG GHI FILE XUỐNG Ổ CỨNG (Bọc giáp try-with-resources chống rác bộ nhớ)
            String filePath = "data/BaoCaoTongHopSachDangChoMuon.xlsx";
            try(java.io.FileOutputStream fileOut = new java.io.FileOutputStream(filePath)){
                workbook.write(fileOut);            // Xuất file thực tế
                System.out.println("\u2705 Xuất file Excel báo cáo tổng hợp sách đang cho mượn thành công! " +
                        "Kiểm tra tại: " + filePath);
            }

            workbook.close();

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc xuất báo cáo tổng hợp sách đang cho mượn ra file excel " +
                    e.getMessage());
        }

    }


    // 3. HÀM BÁO CÁO SỐ LƯỢNG SÁCH BỈ HỎNG NẶNG HOẶC BỊ MẤT
    public static void reportLostOrDamagedBooks(){
        try{
            // 1. Gọi danh sách kho vật lý
            ArrayList<BookInstance> instList = BookInstanceService.getBookInstanceList();

            // 2. Kiểm tra danh sách kho vật lý rỗng hoặc trống
            if(instList == null || instList.isEmpty()){
                System.out.print("\u274C Kho sách trống!");
                return;
            }

            // 3. Khởi tạo biến đếm và in bảng tiêu đề
            int count = 0;
            System.out.println(String.format("%-8s | %-25s | %-35s | %-15s", "STT", "MÃ VẠCH", "TÊN SÁCH",
                    "NGƯỜI LÀM HỎNG/MẤT"));

            for(BookInstance inst: instList){
                // 4. Kiểm tra từng phần tử sách vật lý rỗng hoặc trống
                if(inst == null || inst.getId() == null){
                    continue;       // Bỏ qua -> quét phần tử tiếp theo
                }

                // 5. Kiểm tra trạng thái sách vật lý : BỊ HỎNG NẶNG/MẤT
                if(inst.getStatus() != BookInstanceService.LOST_OR_DAMAGED){
                    continue;       // Bỏ qua -> quét phần tử tiếp theo
                }

                // 6. Các diều kiện sách vật lý hợp lệ --> Lấy tiêu đề sách
                String bookId = inst.getBookId();           // Lấy mã đầu sách
                Book book = BookService.findBook(bookId);   // Tìm đầu sách
                String title = (book != null) ? book.getTitle() : "Đầu sách đã bị xóa ngầm"; // Lấy tên sách

                // 7. Tìm người mượn sách
                String readerId = "Không rõ";   // Khởi tạo biến tạm hứng readerId

                // 8. Mở vòng lập phụ quét danh sách phiếu mượn
                for(LoanSlip loan: LoanSlipService.getLoanList()){
                    // 9. Kiểm tra các phiếu rỗng hoặc phiếu đã hoàn tất thủ tục trả (lúc đó mới xác nhận Sách bị mất/hỏng)
                    if(loan == null || loan.getDetails() == null || loan.getStatus() != LoanSlip.COMPLETED){
                        continue;       // Bỏ qua -> quét phần tử tiếp theo
                    }

                    // 10. Các điều kiện hợp lệ -> Quét giỏ hàng trong phiếu đó
                    for(LoanDetail detail: loan.getDetails()) {
                        // 11. Kiểm tra trùng mà sách vật lý và khác null
                        if (detail != null && detail.getBookInstanceId().equalsIgnoreCase(inst.getId())) {
                            // 12. Lấy mã đọc giá
                            readerId = loan.getReaderId();
                            break;
                        }
                    }

                    // 13. Thoát sớm vòng lặp phiếu mượn NGAY KHI đã tìm thấy người làm mất sách [🛡️]
                    if(!readerId.equalsIgnoreCase("Không rõ")){
                        break;
                    }
                }

                // 14. Tăng biến đếm
                count ++;

                // 15. In thông báo và tăng biến đếm
                System.out.println(String.format("%-8s | %-25s | %-35s | %-15s", count, inst.getId(),
                        LoanSlipService.truncate(title,35),readerId));
            }
            System.out.println("Tổng số lượng sách đang bị hỏng/mất: " + count + " quyển.");

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc xuất báo cáo tổng hợp sách bị mất/hỏng "+ e.getMessage());
        }
    }


    // 4. HÀM XUẤT BÁO CÁO SỐ LƯỢNG SÁCH MẤT/HỎNG RA FILE EXCEL
    public static void exportLostOrDamagedBooksToExcel(){
        try{
            // 1. Gọi danh sách kho vật lý
            ArrayList<BookInstance> instList = BookInstanceService.getBookInstanceList();

            // 2. Kiểm tra danh sách kho vật lý rỗng hoặc trống
            if(instList == null || instList.isEmpty()){
                System.out.print("\u274C Kho sách trống!");
                return;
            }

            // 3. Khởi tạo biến đếm
            int count = 0;

            // 4. KHỞI TẠO CẤU TRÚC FILE EXCEL
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Thống kê sách bị mất");
            int rowIndex = 0;       // Khởi tạo biến đếm dòng

            // 5. Ghi tiêu đề cột vào dòng 0
            org.apache.poi.ss.usermodel.Row rowTitle = sheet.createRow(rowIndex++);
            rowTitle.createCell(0).setCellValue("BÁO CÁO TỔNG HỢP SÁCH BỊ MẤT/HỎNG");

            org.apache.poi.ss.usermodel.Row rowHeader = sheet.createRow(rowIndex++);
            rowHeader.createCell(0).setCellValue("STT");
            rowHeader.createCell(1).setCellValue("MÃ VẠCH SÁCH");
            rowHeader.createCell(2).setCellValue("TÊN SÁCH");
            rowHeader.createCell(3).setCellValue("NGƯỜI LÀM HỎNG/MẤT");

            // 6. Duyệt vòng lập danh sách Sách vật lý
            for(BookInstance inst: instList){
                // 7. Kiểm tra từng phần tử sách vật lý rỗng hoặc trống
                if(inst == null || inst.getId() == null){
                    continue;       // Bỏ qua -> quét phần tử tiếp theo
                }

                // 8. Kiểm tra trạng thái sách vật lý : BỊ HỎNG NẶNG/MẤT
                if(inst.getStatus() != BookInstanceService.LOST_OR_DAMAGED){
                    continue;       // Bỏ qua -> quét phần tử tiếp theo
                }

                // 9. Các diều kiện sách vật lý hợp lệ --> Lấy tiêu đề sách
                String bookId = inst.getBookId();           // Lấy mã đầu sách
                Book book = BookService.findBook(bookId);   // Tìm đầu sách
                String title = (book != null) ? book.getTitle() : "Đầu sách đã bị xóa ngầm"; // Lấy tên sách

                // 10. Tìm người mượn sách
                String readerId = "Không rõ";   // Khởi tạo biến tạm hứng readerId

                // 11. Mở vòng lập phụ quét danh sách phiếu mượn
                for(LoanSlip loan: LoanSlipService.getLoanList()){
                    // 12. Kiểm tra các phiếu rỗng hoặc phiếu đã hoàn tất thủ tục trả (lúc đó mới xác nhận Sách bị mất/hỏng)
                    if(loan == null || loan.getDetails() == null || loan.getStatus() != LoanSlip.COMPLETED){
                        continue;       // Bỏ qua -> quét phần tử tiếp theo
                    }

                    // 13. Các điều kiện hợp lệ -> Quét giỏ hàng trong phiếu đó
                    for(LoanDetail detail: loan.getDetails()) {
                        // 14. Kiểm tra trùng mà sách vật lý và khác null
                        if (detail != null && detail.getBookInstanceId().equalsIgnoreCase(inst.getId())) {
                            // 15. Lấy mã đọc giá
                            readerId = loan.getReaderId();
                            break;
                        }
                    }

                    // 16. Thoát sớm vòng lặp phiếu mượn NGAY KHI đã tìm thấy người làm mất sách [🛡️]
                    if(!readerId.equalsIgnoreCase("Không rõ")){
                        break;
                    }
                }

                // 17. Tăng biến đếm
                count ++;

                // 18. In thông báo dòng dữ liệu
                org.apache.poi.ss.usermodel.Row rowData = sheet.createRow(rowIndex++);
                rowData.createCell(0).setCellValue(count);
                rowData.createCell(1).setCellValue(inst.getId());
                rowData.createCell(2).setCellValue(title);
                rowData.createCell(3).setCellValue(readerId);
            }

            // 19. In đường kẻ ngang trước dòng SUM
            org.apache.poi.ss.usermodel.Row rowLine = sheet.createRow(rowIndex++);
            rowLine.createCell(0).setCellValue("---------------------------------------------------------------" +
                    "--------------------------------------------");

            // 20. KHỞI TẠO PHÔNG CHỮ IN ĐẬM TRONG EXCEL
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();      // Khởi tạo font mới
            boldFont.setBold(true);             // Gán font in đậm
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();   // Khởi tạo style cho Cell
            boldStyle.setFont(boldFont);        // Gán style của cell là in đậm

            // 21. Tạo dòng SUM (IN ĐẬM) tổng kết đối soát cuối bảng Excel
            org.apache.poi.ss.usermodel.Row rowSum = sheet.createRow(rowIndex++);

            // 21.1. Ô 0 (STT): Đổ tổng số sách vật lý bị mất/hỏng và đặt kiểu in đậm
            org.apache.poi.ss.usermodel.Cell cell0 = rowSum.createCell(0);  // Khởi tạo cell 0
            cell0.setCellValue(count);        // Gán giá trị cho cell0
            cell0.setCellStyle(boldStyle);

            // 21.2. Ô 1 (TỔNG CỘNG): Đổ chữ tổng cộng
            org.apache.poi.ss.usermodel.Cell cell1 = rowSum.createCell(1);  // Khởi tạo cell 1
            cell1.setCellValue("TỔNG CỘNG");        // Gán giá trị cho cell1
            cell1.setCellStyle(boldStyle);

            // 21.3. Ô 2 và 3 (TÊN SÁCH và MÃ ĐỌC GIẢ):
            rowSum.createCell(2).setCellValue("");
            rowSum.createCell(3).setCellValue("");

            // 22. MỞ LUỒNG TỰ ĐỘNG GHI FILE XUỐNG Ổ CỨNG (Bọc giáp try-with-resources chống rác bộ nhớ)
            String filePath = "data/BaoCaoTongHopSachBiMat.xlsx";
            try(java.io.FileOutputStream fileOut = new java.io.FileOutputStream(filePath)){
                workbook.write(fileOut);            // Xuất file thực tế
                System.out.println("\u2705 Xuất file Excel báo cáo tổng hợp sách bị mất/hỏng thành công! " +
                        "Kiểm tra tại: " + filePath);
            }
            workbook.close();

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc xuất báo cáo tổng hợp sách bị mất/hỏng ra file excel "
                    + e.getMessage());
        }
    }


    // 5. HÀM ĐẾM SỐ SÁCH BỊ MẤT/HỎNG NẶNG TRONG 1 PHIẾU
    private static int countLostBooksInSlip(LoanSlip loan) throws Exception{
        // 1. Kiểm tra phiếu rỗng, phiếu chưa hoàn tất thủ tục trả, giỏ hàng rỗng
        if(loan == null || loan.getStatus() != LoanSlip.COMPLETED || loan.getDetails() == null){
            return 0;
        }

        // 2. Khởi tạo biến đếm cục bộ
        int lostCount = 0;

        // 3. Duyệt vòng lập giỏ hàng chi tiết của riêng phiếu này
        for(LoanDetail detail: loan.getDetails()){
            // 4. Kiểm tra từng phần từ trong giỏ hàng rỗng, hoặc mã sch vật lý rỗng
            if(detail == null || detail.getBookInstanceId() == null){
                continue;       // Bỏ qua, quét phần tử tiếp theo trong giỏ hàng
            }

            // 5. Tìm sách vật lý
            BookInstance inst = BookInstanceService.findBookInstance(detail.getBookInstanceId(),
                    BookInstanceService.SEARCH_BY_ID);

            // 6. Nếu tìm được sách vật lý + kiểm tra trạng thái sách vật lý bị MẤT/HỎNG --> ĐẾM TĂNG LÊN 1
            if(inst != null && inst.getStatus() == BookInstanceService.LOST_OR_DAMAGED){
                lostCount ++;
            }
        }
        return lostCount;
    }


    // 6. HÀM ĐẾM SỐ SÁCH TRẢ TRỄ HẠN TRONG 1 PHIẾU
    private static int countOverdueBooksInSlip(LoanSlip loan) throws Exception{
        // 1. Kiểm tra phiếu rỗng, phiếu có tiền phạt trễ hạn <= 0 (tức là trả đúng hạn), giỏ hàng rỗng
        if(loan == null || loan.getOverduePenaltyFee() <= 0 || loan.getDetails() == null){
            return 0;
        }

        // 2. Trả về kết quả bằng size của phiếu
        return loan.getDetails().size();
    }


    // 7. HÀM PHÂN LOẠI TÍNH CHẤT PHIẾU MƯỢN
    private static int checkLoanSlipType(LoanSlip loan) throws Exception{
        // 1. Kiểm tra phiếu rỗng
        if(loan == null){
            return TYPE_OTHER;
        }

        // 2. Kiểm tra có phải là phiếu đang mượn không
        if(loan.getStatus() == LoanSlip.BORROWING){
            return TYPE_BORROWING;
        }

        // 3. Kiểm tra phiếu có phải là Phiếu trả trễ hạn không
        if(loan.getStatus() == LoanSlip.COMPLETED && loan.getOverduePenaltyFee() > 0){
            return TYPE_OVERDUE;
        }

        // 4. Kiểm tra phiếu có trả có Sách bị mất không
        if(loan.getStatus() == LoanSlip.COMPLETED && loan.getTotalLostBookFee() > 0){
            return TYPE_LOST_BOOK;
        }

        // 5. Nếu vượt qua chốt chặn 2, 3, 4 --> Phiếu sẽ là Completed TỐT: không bị trễ hạn, không sách
        return TYPE_OTHER;
    }


    // 8. HÀM BÁO CÁO TỔNG QUÁT KẾT QUẢ GIAO DỊCH CỦA TẤT CẢ ĐỘC GIẢ
    public static void reportTotalReaders(){
        try{
            // 1. Gọi danh sách Đọc giả
            ArrayList<Reader> readerList = ReaderService.getReaderList();

            // 2. Kiểm tra danh sách rỗng hoặc null
            if (readerList == null || readerList.isEmpty()) {
                System.out.println("\u274C Danh sách đọc giả trống!");
                return;
            }

            // 3. Khởi tạo 3 biến tổng kết và in bảng tiêu đề báo cáo
            int totalReaders = 0, maleCount = 0, femaleCount = 0;

            // ĐỒNG BỘ CỜ %s CĂN PHẢI CHO CHỮ TIÊU ĐỀ (ĐÃ VÁ CHỮ d THÀNH CHỮ s) [🛡️]
            System.out.println(String.format("%6s | %-12s | %-25s | %-10s | %12s | %15s | %15s | %15s | %15s | %15s |" +
                            " %15s | %15s","STT", "MÃ ĐỘC GIẢ", "HỌ TÊN ĐỘC GIẢ", "GIỚI TÍNH", "TỔNG PHIẾU",
                    "PHIẾU ĐANG MƯỢN", "PHIẾU TRẢ TỐT", "PHIẾU TRỄ HẠN", "PHIẾU MẤT SÁCH", "SÁCH ĐANG GIỮ",
                    "SÁCH TRỄ HẠN", "SÁCH BỊ MẤT"));

            // 4. Quét toàn bộ vòng lập lớn readerList
            for (Reader reader : readerList) {
                // 5. Kiểm tra từng phần tử reader là null hoặc empty
                if (reader == null || reader.getId() == null) {
                    continue; // Bỏ qua, quét phần tử tiếp theo
                }

                // 6. Khởi tạo 8 biến thống kê
                int totalSlip = 0;              // Tổng số phiếu của 1 reader
                int borrowingSlip = 0;          // Số phiếu đang mượn (chưa trả)
                int totalGoodSlip = 0;          // Số phiếu TỐT
                int overdueSlip = 0;            // Số phiếu trễ hạn
                int lostBookSlip = 0;           // Số phiếu có mất/hỏng sách
                int totalBookBorrowing = 0;     // Số sách đang mượn
                int totalOverdueBooks = 0;      // Số sách trả trễ hạn
                int totalLostBooks = 0;         // Số sách bị mất/hỏng

                // 6. Khởi tạo OOP để truy xuất 8 chỉ tiêu thống kê
                ReaderLoanStats currentStats = getReaderLoanStats(reader.getId());
                if(currentStats != null){
                    totalSlip = currentStats.totalSlip;
                    borrowingSlip = currentStats.borrowingSlip;
                    totalGoodSlip = currentStats.goodSlip;
                    overdueSlip = currentStats.overdueSlip;
                    lostBookSlip = currentStats.lostBookSlip;
                    totalBookBorrowing = currentStats.borrowingBooks;
                    totalOverdueBooks = currentStats.overdueBooks;
                    totalLostBooks = currentStats.lostBooks;
                }

                // 7. Phân loại giới tính của reader
                String gender = "";
                if (reader.getGender()) { // Giới tính "nam"
                    gender = "Nam";
                    maleCount++;
                } else {
                    gender = "Nữ";
                    femaleCount++;
                }

                // 8. Hợp nhất họ tên an toàn
                String readerName = reader.getSurname() + " " + reader.getGivenName();

                // 9. Tăng biến đếm tổng số Đọc giả
                totalReaders++;

                // 10. Xuất dữ liệu của từng Đọc giả (ĐỒNG BỘ ĐÚNG 12 BIẾN TÀI CHÍNH HÀNH CHÍNH) [🛡️]
                System.out.println(String.format("%6d | %-12s | %-25s | %-10s | %12d | %15d | %15d | %15d | %15d | %15d |" +
                                " %15d | %15d",
                        totalReaders,
                        reader.getId(),
                        LoanSlipService.truncate(readerName, 25),
                        gender,
                        totalSlip,
                        borrowingSlip,
                        totalGoodSlip,
                        overdueSlip,
                        lostBookSlip,
                        totalBookBorrowing,
                        totalOverdueBooks,
                        totalLostBooks));
            }
            System.out.println("---------------------------------------------------------------------------------------");
            System.out.println("TÓM LƯỢC BÁO CÁO:");
            System.out.println(String.format("Tổng số lượng đọc giả: %5d | Nam: %5d | Nữ: %5d", totalReaders, maleCount,
                    femaleCount));
        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc lập báo cáo tổng hợp " + e.getMessage());
        }
    }


    // 9. HÀM XUẤT BÁO CÁO TỔNG HỢP DANH SÁCH READER RA FILE EXCEL
    public static void exportTotalReadersToExcel(){
        try{
            // 1. Gọi danh sách Đọc giả
            ArrayList<Reader> readerList = ReaderService.getReaderList();

            // 2. Kiểm tra danh sách rỗng hoặc null
            if (readerList == null || readerList.isEmpty()) {
                System.out.println("\u274C Danh sách đọc giả trống!");
                return;
            }

            // 3. Khởi tạo 11 biến tổng kết (SUM) để in dòng cuối cùng của bảng tổng hợp
            int totalReaders = 0, maleSum = 0, femaleSum = 0;
            int totalSlipSum = 0, borrowingSlipSum = 0, goodSlipSum = 0, overdueSlipSum = 0, lostBookSlipSum = 0;
            int totalBookBorrowingSum = 0, totalOverdueBooksSum = 0, totalLostBooksSum = 0;

            // 4. KHỞI TẠO CẤU TRÚC FILE EXCEL
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Thống kê Đọc giả");
            int rowIndex = 0;       // Khởi tạo biến đếm dòng

            // 5. Ghi tiêu đề cột vào dòng 0
            org.apache.poi.ss.usermodel.Row rowHeader1 = sheet.createRow(rowIndex++);
            rowHeader1.createCell(0).setCellValue("STT");
            rowHeader1.createCell(1).setCellValue("MÃ ĐỘC GIẢ");
            rowHeader1.createCell(2).setCellValue("HỌ TÊN ĐỘC GIẢ");
            rowHeader1.createCell(3).setCellValue("GIỚI TÍNH");
            rowHeader1.createCell(4).setCellValue("");
            rowHeader1.createCell(5).setCellValue("TỔNG PHIẾU");
            rowHeader1.createCell(6).setCellValue("PHIẾU ĐANG MƯỢN");
            rowHeader1.createCell(7).setCellValue("PHIẾU TRẢ TỐT");
            rowHeader1.createCell(8).setCellValue("PHIẾU TRỄ HẠN");
            rowHeader1.createCell(9).setCellValue("PHIẾU MẤT SÁCH");
            rowHeader1.createCell(10).setCellValue("SÁCH ĐANG GIỮ");
            rowHeader1.createCell(11).setCellValue("SÁCH TRỄ HẠN");
            rowHeader1.createCell(12).setCellValue("SÁCH BỊ MẤT");

            org.apache.poi.ss.usermodel.Row rowHeader2 = sheet.createRow(rowIndex++);
            rowHeader2.createCell(3).setCellValue("NAM");
            rowHeader2.createCell(4).setCellValue("NỮ");

            // 6. Quét toàn bộ vòng lập lớn readerList
            for (Reader reader : readerList) {
                // 7. Kiểm tra từng phần tử reader là null hoặc empty
                if (reader == null || reader.getId() == null) {
                    continue; // Bỏ qua, quét phần tử tiếp theo
                }

                // 8. Khởi tạo 8 biến thống kê
                int totalSlip = 0;              // Tổng số phiếu của 1 reader
                int borrowingSlip = 0;          // Số phiếu đang mượn (chưa trả)
                int goodSlip = 0;               // Số phiếu TỐT
                int overdueSlip = 0;            // Số phiếu trễ hạn
                int lostBookSlip = 0;           // Số phiếu có mất/hỏng sách
                int totalBookBorrowing = 0;     // Số sách đang mượn
                int totalOverdueBooks = 0;      // Số sách trả trễ hạn
                int totalLostBooks = 0;         // Số sách bị mất/hỏng

                // 9. Khởi tạo OOP để truy xuất 8 chỉ tiêu thống kê
                ReaderLoanStats currentStats = getReaderLoanStats(reader.getId());
                if(currentStats != null){
                    totalSlip = currentStats.totalSlip;
                    borrowingSlip = currentStats.borrowingSlip;
                    goodSlip = currentStats.goodSlip;
                    overdueSlip = currentStats.overdueSlip;
                    lostBookSlip = currentStats.lostBookSlip;
                    totalBookBorrowing = currentStats.borrowingBooks;
                    totalOverdueBooks = currentStats.overdueBooks;
                    totalLostBooks = currentStats.lostBooks;

                    // 10. Tăng các biến SUM
                    totalSlipSum += totalSlip;
                    borrowingSlipSum += borrowingSlip;
                    goodSlipSum += goodSlip;
                    overdueSlipSum += overdueSlip;
                    lostBookSlipSum += lostBookSlip;
                    totalBookBorrowingSum += totalBookBorrowing;
                    totalOverdueBooksSum += totalOverdueBooks;
                    totalLostBooksSum += totalLostBooks;
                }

                // 11. Phân loại giới tính của reader
                String maleStr = "", femaleStr = "";
                if (reader.getGender()) { // Giới tính "nam"
                    maleSum++;
                    maleStr = "X";
                } else {
                    femaleSum++;
                    femaleStr = "X";
                }

                // 12. Hợp nhất họ tên an toàn
                String readerName = reader.getSurname() + " " + reader.getGivenName();

                // 13. Tăng biến đếm tổng số Đọc giả
                totalReaders++;

                // 14. Xuất dữ liệu của từng Đọc giả (ĐỒNG BỘ ĐÚNG 13 BIẾN TÀI CHÍNH HÀNH CHÍNH) [🛡️]
                org.apache.poi.ss.usermodel.Row rowData = sheet.createRow(rowIndex++);
                rowData.createCell(0).setCellValue(totalReaders);
                rowData.createCell(1).setCellValue(reader.getId());
                rowData.createCell(2).setCellValue(readerName);
                rowData.createCell(3).setCellValue(maleStr);
                rowData.createCell(4).setCellValue(femaleStr);
                rowData.createCell(5).setCellValue(totalSlip);
                rowData.createCell(6).setCellValue(borrowingSlip);
                rowData.createCell(7).setCellValue(goodSlip);
                rowData.createCell(8).setCellValue(overdueSlip);
                rowData.createCell(9).setCellValue(lostBookSlip);
                rowData.createCell(10).setCellValue(totalBookBorrowing);
                rowData.createCell(11).setCellValue(totalOverdueBooks);
                rowData.createCell(12).setCellValue(totalLostBooks);
            }

            // 15. Tạo dòng nét đứt phân cách thẩm mỹ trong Excel
            org.apache.poi.ss.usermodel.Row rowLine = sheet.createRow(rowIndex++);
            rowLine.createCell(0).setCellValue("------------------------------------------------------------------" +
                    "----------------------------------");

            // 16. KHỞI TẠO PHÔNG CHỮ IN ĐẬM TRONG EXCEL
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();      // Khởi tạo font mới
            boldFont.setBold(true);             // Gán font in đậm
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();   // Khởi tạo style cho Cell
            boldStyle.setFont(boldFont);        // Gán style của cell là in đậm

            // 17. Tạo dòng SUM (IN ĐẬM) tổng kết đối soát cuối bảng Excel
            org.apache.poi.ss.usermodel.Row rowSum = sheet.createRow(rowIndex++);

            // 17.1. Ô 0 (STT): Đổ tổng số độc giả và đặt kiểu in đậm
            org.apache.poi.ss.usermodel.Cell cell0 = rowSum.createCell(0);  // Khởi tạo cell 0
            cell0.setCellValue(totalReaders);        // Gán giá trị cho cell0
            cell0.setCellStyle(boldStyle);

            // 17.2. Ô 1 (TỔNG CỘNG): Đổ chữ tổng cộng
            org.apache.poi.ss.usermodel.Cell cell1 = rowSum.createCell(1);  // Khởi tạo cell 1
            cell1.setCellValue("TỔNG CỘNG");        // Gán giá trị cho cell1
            cell1.setCellStyle(boldStyle);

            // 17.3. Ô 2 (""):
            rowSum.createCell(2).setCellValue("");

            // 17.4. Ô 3 : Đổ tổng cộng reader Nam
            org.apache.poi.ss.usermodel.Cell cell3 = rowSum.createCell(3);  // Khởi tạo cell 3
            cell3.setCellValue(maleSum);        // Gán giá trị cho cell3
            cell3.setCellStyle(boldStyle);

            // 17.5. Ô 4 : Đổ tổng cộng reader Nữ
            org.apache.poi.ss.usermodel.Cell cell4 = rowSum.createCell(4);  // Khởi tạo cell 4
            cell4.setCellValue(femaleSum);        // Gán giá trị cho cell4
            cell4.setCellStyle(boldStyle);

            // 17.6. Ô 5 : Đổ tổng cộng số phiếu
            org.apache.poi.ss.usermodel.Cell cell5 = rowSum.createCell(5);  // Khởi tạo cell 5
            cell5.setCellValue(totalSlipSum);        // Gán giá trị cho cell5
            cell5.setCellStyle(boldStyle);

            // 17.7. Ô 6 : Đổ tổng số phiếu đang mượn
            org.apache.poi.ss.usermodel.Cell cell6 = rowSum.createCell(6);  // Khởi tạo cell 6
            cell6.setCellValue(borrowingSlipSum);        // Gán giá trị cho cell6
            cell6.setCellStyle(boldStyle);

            // 17.8. Ô 7 : Đổ tổng số phiếu tốt
            org.apache.poi.ss.usermodel.Cell cell7 = rowSum.createCell(7);  // Khởi tạo cell 7
            cell7.setCellValue(goodSlipSum);        // Gán giá trị cho cell7
            cell7.setCellStyle(boldStyle);

            // 17.9. Ô 8 : Đổ tổng số phiếu trễ hạn
            org.apache.poi.ss.usermodel.Cell cell8 = rowSum.createCell(8);  // Khởi tạo cell 8
            cell8.setCellValue(overdueSlipSum);        // Gán giá trị cho cell8
            cell8.setCellStyle(boldStyle);

            // 17.10. Ô 9 : Đổ tổng số phiếu mất/hỏng sách
            org.apache.poi.ss.usermodel.Cell cell9 = rowSum.createCell(9);  // Khởi tạo cell 9
            cell9.setCellValue(lostBookSlipSum);        // Gán giá trị cho cell9
            cell9.setCellStyle(boldStyle);

            // 17.11. Ô 10 : Đổ tổng số sách đang mượn
            org.apache.poi.ss.usermodel.Cell cell10 = rowSum.createCell(10);  // Khởi tạo cell 10
            cell10.setCellValue(totalBookBorrowingSum);        // Gán giá trị cho cell10
            cell10.setCellStyle(boldStyle);

            // 17.12. Ô 11 : Đổ tổng số sách bị trẽ hạn
            org.apache.poi.ss.usermodel.Cell cell11 = rowSum.createCell(11);  // Khởi tạo cell 11
            cell11.setCellValue(totalOverdueBooksSum);        // Gán giá trị cho cell11
            cell11.setCellStyle(boldStyle);

            // 17.13. Ô 12 : Đổ tổng số sách bị mất/hỏng
            org.apache.poi.ss.usermodel.Cell cell12 = rowSum.createCell(12);  // Khởi tạo cell 12
            cell12.setCellValue(totalLostBooksSum);        // Gán giá trị cho cell12
            cell12.setCellStyle(boldStyle);

            // 18. MỞ LUỒNG TỰ ĐỘNG GHI FILE XUỐNG Ổ CỨNG (Bọc giáp try-with-resources chống rác bộ nhớ)
            String filePath = "data/BaoCaoTongHopDocGia.xlsx";
            try(java.io.FileOutputStream fileOut = new java.io.FileOutputStream(filePath)){
                workbook.write(fileOut);            // Xuất file thực tế
                System.out.println("\u2705 Xuất file Excel báo cáo tổng hợp Đọc giả thành công! Kiểm tra tại: " + filePath);
            }
            workbook.close();

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc xuất báo cáo tổng hợp đọc giả ra file Excel. " + e.getMessage());
        }
    }


    // 10. HÀM BÁO CÁO TỔNG QUÁT KẾT QUẢ GIAO DỊCH CỦA 1 ĐỘC GIẢ
    public static ReaderLoanStats reportReaderHistoryDetail(String readerId){
        try{
            // 1. Kiểm tra null hay empty
            if(readerId == null || readerId.isEmpty()){
                return null;
            }

            // 2. Chuẩn hóa readerId
            String cleanReaderId = readerId.trim().toUpperCase();

            // 3. Tìm reader trong danh sách
            Reader reader = ReaderService.findReaderById(cleanReaderId);

            // 4. Kiểm tra không tìm thấy reader
            if(reader == null){
                System.out.println("\u274C Lỗi đọc giả chưa đăng ký!");
                return null;
            }

            // 5. Khai báo các biến tích lũy tổng giao dịch của Reader
            int totalSlip = 0;                  // Tổng số phiếu của 1 reader
            int borrowingSlipSum = 0;           // Số phiếu đang mượn (chưa trả)
            int goodSlipSum = 0;                // Số phiếu TỐT
            int overdueSlipSum = 0;             // Số phiếu trễ hạn
            int lostBookSlipSum = 0;            // Số phiếu có mất/hỏng sách
            int borrowingBooksSum = 0;          // Số sách đang mượn
            int overdueBooksSum = 0;            // Số sách trả trễ hạn
            int lostBooksSum = 0;               // Số sách bị mất/hỏng
            int goodBooksSum = 0;               // Số sách tốt

            // 6. In thông tin đọc giả
            printReaderProfile(reader);

            // 7. In tiêu đề báo cáo lịch sử giao dịch của Đọc giả
            printHistoryHeader();

            // 8. Quét vòng lập danh sách phiếu mượn
            for (LoanSlip loan : LoanSlipService.getLoanList()){
                // 9. Kiểm tra từng phần tử phiếu rỗng, không có readerId, phiếu bị Block
                if (loan == null || loan.getReaderId() == null || loan.isBlock()) {
                    continue; // Bỏ qua, quét phần tử tiếp theo
                }

                // 10. Các điều kiện hợp lệ: So sánh trùng readerId để tổng hợp dữ liệu vào 1 dòng
                if (loan.getReaderId().equalsIgnoreCase(reader.getId())) {
                    totalSlip++;                                // Tăng tổng số phiếu

                    // 11. Xử lý lấy username
                    // 11.1. Khởi tạo biến username của nhân viên lập phiếu
                    String staffUsername = "Không rõ";

                    // 11.2. Chuyển Id của nhân viên lập phiếu từ int sang String
                    String searchKey = String.valueOf(loan.getCreatedByUserId());

                    // 11.3 Tìm kiếm OOP User
                    User staff = UserService.findUserByUsernameOrId(searchKey);

                    // 11.4 Kiểm tra không tìm được User
                    if(staff != null){
                        staffUsername = staff.getUsername();
                    }

                    // 12. Khởi tạo và truy xuất dữ liệu biến ngày mượn và ngày trả phiếu
                    String realReturnDate = "-", rentalDate = "-";
                    DateTimeFormatter dateFomatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                     // 12.1. Truy xuất ngày mượn
                    if(loan.getRentalDate() != null){
                        rentalDate = loan.getRentalDate().format(dateFomatter);
                    }

                    // 12.2. Truy xuất ngày trả phiếu
                    if(loan.getStatus() == LoanSlip.COMPLETED && loan.getActualReturnDate() != null){
                        realReturnDate = loan.getActualReturnDate().format(dateFomatter);
                    }

                    // 13. Khởi tạo các biến chữ đại diện cột của dòng hiện tại
                    String colBorrowing = "-", colGood = "-", colOverdue = "-", colLost = "-";

                    // 14. Khởi tạo các biến số lượng sách của dòng hiện tại
                    int bookBorrowing = 0, bookOverdue = 0, bookLost = 0, bookGood = 0;

                    // 15. Phân loại trạng thái phiếu hiện tại bằng switch-case, gọi checkLoanSlipType(loan)
                    switch (checkLoanSlipType(loan)) {
                        case TYPE_BORROWING: {
                            borrowingSlipSum++;          // Tăng số phiếu đang mượn (chưa trả)
                            colBorrowing = "x";         // Đánh dấu cột loại phiếu mượn
                            if(loan.getDetails() != null){
                                bookBorrowing = loan.getDetails().size();   // Tính số sách mượn trong phiếu mượn
                                borrowingBooksSum += bookBorrowing;         // Cộng dồn tổng số sách mượn
                            }
                            break;
                        }
                        case TYPE_OVERDUE: {
                            overdueSlipSum++;                      // Tăng số phiếu trả trễ hạn
                            colOverdue = "x";                       // Đánh dấu cột loại phiếu trễ hạn
                            if(loan.getDetails() != null){
                                bookOverdue = countOverdueBooksInSlip(loan); // Tính số sách trễ hạn trong phiếu mượn
                                overdueBooksSum += bookOverdue;             // Cộng dồn tổng số sách trễ hạn
                            }
                            break;
                        }
                        case TYPE_LOST_BOOK: {
                            lostBookSlipSum++;                     // Tăng số phiếu bị mất/hỏng sách
                            colLost = "x";                          // Đánh dấu cột loại phiếu mất sách
                            if(loan.getDetails() != null){
                                bookLost = countLostBooksInSlip(loan);     // Tính số sách mất trong phiếu mượn
                                lostBooksSum += bookLost;                  // Cộng dồn tổng số sách bị mất
                                bookGood += (loan.getDetails().size() - bookLost);  // Tính số sách tốt
                                goodBooksSum += bookGood;               // Cộng dồn tổng số sách tốt
                            }
                            break;
                        }
                        default:{   // Phiếu trả tốt
                            goodSlipSum ++;         // Tăng số phiếu tốt
                            colGood = "x";          // Đánh dấu cột loại phiếu tốt
                            if(loan.getDetails() != null){
                                bookGood= loan.getDetails().size();     // Tính số sách tốt trong phiếu
                                goodBooksSum += bookGood;               // Tính số sách tốt trong phiếu
                            }
                            break;

                        }
                    }

                    // 16. In dòng chi tiết phiếu mượn ra
                    printHistoryRow(totalSlip,loan.getId(),staffUsername,rentalDate,realReturnDate,colBorrowing,
                            colGood, colOverdue,colLost,bookBorrowing,bookGood,bookOverdue,bookLost);
                }
            }

            // 17. Kiểm tra totalSlip khác 0;
            if(totalSlip == 0){
                System.out.println("Đọc giả chưa có giao dịch.");
                return null;
            }

            // 18. In đường kẻ dài và xuất số SUM ĐỒNG BỘ IN ĐẬM TOÀN DÒNG AN TOÀN [🛡️]
            System.out.println("---------------------------------------------------------------------------------------" +
                    "------------------------------------------------------------------------------------------------" +
                    "-------------------------");

            // 18.1. Khai báo hằng số mã màu ANSI nội bộ dòng SUM
            String ANSI_BOLD = "\u001B[1m";     // Mở chế độ in đậm
            String ANSI_RESET = "\u001B[0m";    // Reset chế độ in đậm

            // 18.2. Bật mã đậm ở đầu dòng và tắt mã đậm ở cuối dòng. Cột STT truyền "", cột Mã Phiếu truyền "TỔNG CỘNG"
            System.out.print(ANSI_BOLD);            // Bật mã in đậm
            System.out.println(String.format("%6s | %-15s | %-12s | %12s | %18s | %15d | %15d | %15d | %15d |" +
                            " %16d | %16d | %16d | %16d",
                    "", "TỔNG CỘNG", "-", "-", "-", borrowingSlipSum, goodSlipSum, overdueSlipSum, lostBookSlipSum,
                    borrowingBooksSum, goodBooksSum, overdueBooksSum, lostBooksSum));
            System.out.print(ANSI_RESET);           // Reset mã in đậm

            // 19. Khởi tạo và gán giá trị vào biến Kết quả [🛡️]
            ReaderLoanStats finalStats = new ReaderLoanStats();
            finalStats.totalSlip = totalSlip;
            finalStats.borrowingSlip = borrowingSlipSum;
            finalStats.overdueSlip = overdueSlipSum;
            finalStats.lostBookSlip = lostBookSlipSum;
            finalStats.goodSlip = goodSlipSum;
            finalStats.overdueBooks = overdueBooksSum;
            finalStats.lostBooks = lostBooksSum;
            finalStats.borrowingBooks = borrowingBooksSum;
            finalStats.goodBooks = goodBooksSum;

            // 20. Trả kết quả
            return finalStats;

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc lập báo cáo lịch sử giao dịch một Đọc giả " + e.getMessage());
        }
        return null;
    }


    // 11. HÀM XUẤT BÁO CÁO TỔNG QUÁT KẾT QUẢ GIAO DỊCH CỦA 1 ĐỘC GIẢ RA FILE EXCEL
    public static ReaderLoanStats exportReaderHistoryDetailToExcel(String readerId){
        try {
            // 1. Kiểm tra null hay empty
            if(readerId == null || readerId.isEmpty()){
                return null;
            }

            // 2. Chuẩn hóa readerId
            String cleanReaderId = readerId.trim().toUpperCase();

            // 3. Tìm reader trong danh sách
            Reader reader = ReaderService.findReaderById(cleanReaderId);

            // 4. Kiểm tra không tìm thấy reader
            if(reader == null){
                System.out.println("\u274C Lỗi đọc giả chưa đăng ký!");
                return null;
            }

            // 5. Khai báo các biến tích lũy tổng giao dịch của Reader
            int totalSlip = 0;                  // Tổng số phiếu của 1 reader
            int borrowingSlipSum = 0;           // Số phiếu đang mượn (chưa trả)
            int goodSlipSum = 0;                // Số phiếu TỐT
            int overdueSlipSum = 0;             // Số phiếu trễ hạn
            int lostBookSlipSum = 0;            // Số phiếu có mất/hỏng sách
            int borrowingBooksSum = 0;          // Số sách đang mượn
            int overdueBooksSum = 0;            // Số sách trả trễ hạn
            int lostBooksSum = 0;               // Số sách bị mất/hỏng
            int goodBooksSum = 0;               // Số sách tốt

            // 6. Khởi tạo OOP trả kết quả
            ReaderLoanStats stats = new ReaderLoanStats();

            // 7. KHỞI TẠO CẤU TRÚC FILE EXCEL BÁO CÁO CHI TIẾT LỊCH SỬ GIAO DỊCH CỦA USER
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Giao dịch Reader " + reader.getId());
            int rowIndex = 0;

            // 8. Truy xuất và Ghi phần readerProfile vào phần đầu file excel
            String fullname = reader.getGivenName() + " " + reader.getSurname();
            String gender = reader.getGender() ? "Nam" : "Nữ";
            String job = (reader.getType() == ReaderService.TYPE_STUDENT)? "Sinh viên":
                    (reader.getType() == ReaderService.TYPE_TEACHER) ? "Giáo viên" : "Khác";

            // 9. In dòng tiêu đề báo cáo
            org.apache.poi.ss.usermodel.Row rowTitle = sheet.createRow(rowIndex++);
            rowTitle.createCell(0).setCellValue("BÁO CÁO LỊCH SỬ GIAO DỊCH CỦA ĐỌC GIẢ");

            // 10. In phần 1 trong báo cáo: Thông tin đọc giả
            org.apache.poi.ss.usermodel.Row rowLine1 = sheet.createRow(rowIndex++);
            rowLine1.createCell(0).setCellValue("PHẦN 1: THÔNG TIN CHUNG VỀ ĐỌC GIẢ");

            org.apache.poi.ss.usermodel.Row rowReaderProfile1 = sheet.createRow(rowIndex++);
            rowReaderProfile1.createCell(0).setCellValue("Mã đọc giả: ");
            rowReaderProfile1.createCell(1).setCellValue(reader.getId());
            rowReaderProfile1.createCell(2).setCellValue("Họ tên: ");
            rowReaderProfile1.createCell(3).setCellValue(fullname);
            rowReaderProfile1.createCell(4).setCellValue("Nghề nghiệp: ");
            rowReaderProfile1.createCell(5).setCellValue(job);

            org.apache.poi.ss.usermodel.Row rowReaderProfile2 = sheet.createRow(rowIndex++);
            rowReaderProfile2.createCell(0).setCellValue("Ngày sinh: ");
            rowReaderProfile2.createCell(1).setCellValue(reader.getBirthday());
            rowReaderProfile2.createCell(2).setCellValue("CCCD: ");
            rowReaderProfile2.createCell(3).setCellValue(reader.getCitizenId());
            rowReaderProfile2.createCell(4).setCellValue("Số điện thoại: ");
            rowReaderProfile2.createCell(5).setCellValue(reader.getPhone());

            org.apache.poi.ss.usermodel.Row rowReaderProfile3 = sheet.createRow(rowIndex++);
            rowReaderProfile3.createCell(0).setCellValue("Địa chỉ: ");
            rowReaderProfile3.createCell(1).setCellValue(reader.getAddress());
            rowReaderProfile3.createCell(2).setCellValue("Email: ");
            rowReaderProfile3.createCell(3).setCellValue(reader.getEmail());

            org.apache.poi.ss.usermodel.Row rowReaderProfile4 = sheet.createRow(rowIndex++);
            rowReaderProfile4.createCell(0).setCellValue("Giới tính: ");
            rowReaderProfile4.createCell(1).setCellValue(gender);
            rowReaderProfile4.createCell(2).setCellValue("Ngày lập thẻ: ");
            rowReaderProfile4.createCell(3).setCellValue(reader.getCreatedDate());
            rowReaderProfile4.createCell(4).setCellValue("Ngày hết hạn: ");
            rowReaderProfile4.createCell(5).setCellValue(reader.getExpiryDate());

            // 11. In tiêu đề báo cáo lịch sử giao dịch của Đọc giả
            org.apache.poi.ss.usermodel.Row rowLine2 = sheet.createRow(rowIndex++);
            rowLine2.createCell(0).setCellValue("PHẦN 2: TỔNG HỢP GIAO DỊCH CỦA ĐỌC GIẢ");

            org.apache.poi.ss.usermodel.Row rowHeader = sheet.createRow(rowIndex++);
            rowHeader.createCell(0).setCellValue("STT");
            rowHeader.createCell(1).setCellValue("MÃ PHIẾU");
            rowHeader.createCell(2).setCellValue("USERNAME NHÂN VIÊN");
            rowHeader.createCell(3).setCellValue("NGÀY MƯỢN");
            rowHeader.createCell(4).setCellValue("NGÀY TRẢ THỰC TẾ");
            rowHeader.createCell(5).setCellValue("PHIẾU ĐANG MƯỢN");
            rowHeader.createCell(6).setCellValue("PHIẾU TRẢ TỐT");
            rowHeader.createCell(7).setCellValue("PHIẾU TRỄ HẠN");
            rowHeader.createCell(8).setCellValue("PHIẾU MẤT SÁCH");
            rowHeader.createCell(9).setCellValue("SỐ SÁCH ĐANG MƯỢN");
            rowHeader.createCell(10).setCellValue("SỐ SÁCH TỐT");
            rowHeader.createCell(11).setCellValue("SỐ SÁCH TRỄ HẠN");
            rowHeader.createCell(12).setCellValue("SỐ SÁCH BỊ MẤT");

            // 12. Quét vòng lập danh sách phiếu mượn
            for (LoanSlip loan : LoanSlipService.getLoanList()){
                // 13. Kiểm tra từng phần tử phiếu rỗng, không có readerId, phiếu bị Block
                if (loan == null || loan.getReaderId() == null || loan.isBlock()) {
                    continue;       // Bỏ qua, quét phần tử tiếp theo
                }

                // 14. Các điều kiện hợp lệ: So sánh trùng readerId để tổng hợp dữ liệu vào 1 dòng
                if (loan.getReaderId().equalsIgnoreCase(reader.getId())) {
                    totalSlip++;                                // Tăng tổng số phiếu

                    // 15. Xử lý lấy username
                    // 15.1. Khởi tạo biến username của nhân viên lập phiếu
                    String staffUsername = "Không rõ";

                    // 15.2. Chuyển Id của nhân viên lập phiếu từ int sang String
                    String searchKey = String.valueOf(loan.getCreatedByUserId());

                    // 15.3 Tìm kiếm OOP User
                    User staff = UserService.findUserByUsernameOrId(searchKey);

                    // 15.4 Kiểm tra không tìm được User
                    if(staff != null){
                        staffUsername = staff.getUsername();
                    }

                    // 16. Khởi tạo và truy xuất dữ liệu biến ngày mượn và ngày trả phiếu
                    String realReturnDate = "-", rentalDate = "-";
                    DateTimeFormatter dateFomatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    // 16.1. Truy xuất ngày mượn
                    if(loan.getRentalDate() != null){
                        rentalDate = loan.getRentalDate().format(dateFomatter);
                    }

                    // 16.2. Truy xuất ngày trả phiếu
                    if(loan.getStatus() == LoanSlip.COMPLETED && loan.getActualReturnDate() != null){
                        realReturnDate = loan.getActualReturnDate().format(dateFomatter);
                    }

                    // 17. Khởi tạo các biến chữ đại diện cột của dòng hiện tại
                    String colBorrowing = "-", colGood = "-", colOverdue = "-", colLost = "-";

                    // 18. Khởi tạo các biến số lượng sách của dòng hiện tại
                    int bookBorrowing = 0, bookOverdue = 0, bookLost = 0, bookGood = 0;

                    // 19. Phân loại trạng thái phiếu hiện tại bằng switch-case, gọi checkLoanSlipType(loan)
                    switch (checkLoanSlipType(loan)) {
                        case TYPE_BORROWING: {
                            borrowingSlipSum++;          // Tăng số phiếu đang mượn (chưa trả)
                            colBorrowing = "x";         // Đánh dấu cột loại phiếu mượn
                            if(loan.getDetails() != null){
                                bookBorrowing = loan.getDetails().size();   // Tính số sách mượn trong phiếu mượn
                                borrowingBooksSum += bookBorrowing;         // Cộng dồn tổng số sách mượn
                            }
                            break;
                        }
                        case TYPE_OVERDUE: {
                            overdueSlipSum++;                      // Tăng số phiếu trả trễ hạn
                            colOverdue = "x";                       // Đánh dấu cột loại phiếu trễ hạn
                            if(loan.getDetails() != null){
                                bookOverdue = countOverdueBooksInSlip(loan); // Tính số sách trễ hạn trong phiếu mượn
                                overdueBooksSum += bookOverdue;             // Cộng dồn tổng số sách trễ hạn
                            }
                            break;
                        }
                        case TYPE_LOST_BOOK: {
                            lostBookSlipSum++;                     // Tăng số phiếu bị mất/hỏng sách
                            colLost = "x";                          // Đánh dấu cột loại phiếu mất sách
                            if(loan.getDetails() != null){
                                bookLost = countLostBooksInSlip(loan);     // Tính số sách mất trong phiếu mượn
                                lostBooksSum += bookLost;                  // Cộng dồn tổng số sách bị mất
                                bookGood += (loan.getDetails().size() - bookLost);  // Tính số sách tốt
                                goodBooksSum += bookGood;               // Cộng dồn tổng số sách tốt
                            }
                            break;
                        }
                        default:{   // Phiếu trả tốt
                            goodSlipSum ++;         // Tăng số phiếu tốt
                            colGood = "x";          // Đánh dấu cột loại phiếu tốt
                            if(loan.getDetails() != null){
                                bookGood= loan.getDetails().size();     // Tính số sách tốt trong phiếu
                                goodBooksSum += bookGood;               // Tính số sách tốt trong phiếu
                            }
                            break;

                        }
                    }

                    // 20. In dòng chi tiết từng phiếu mượn ra
                    org.apache.poi.ss.usermodel.Row rowdata = sheet.createRow(rowIndex++);
                    rowdata.createCell(0).setCellValue(totalSlip);
                    rowdata.createCell(1).setCellValue(loan.getId());
                    rowdata.createCell(2).setCellValue(staffUsername);
                    rowdata.createCell(3).setCellValue(rentalDate);
                    rowdata.createCell(4).setCellValue(realReturnDate);
                    rowdata.createCell(5).setCellValue(colBorrowing);
                    rowdata.createCell(6).setCellValue(colGood);
                    rowdata.createCell(7).setCellValue(colOverdue);
                    rowdata.createCell(8).setCellValue(colLost);
                    rowdata.createCell(9).setCellValue(bookBorrowing);
                    rowdata.createCell(10).setCellValue(bookGood);
                    rowdata.createCell(11).setCellValue(bookOverdue);
                    rowdata.createCell(12).setCellValue(bookLost);
                }
            }

            // 21. Kiểm tra totalSlip khác 0;
            if(totalSlip == 0){
                System.out.println("Đọc giả chưa có giao dịch.");
                workbook.close();
                return null;
            }

            // 22. Tạo dòng nét đứt phân cách thẩm mỹ trong Excel
            org.apache.poi.ss.usermodel.Row rowLine = sheet.createRow(rowIndex++);
            rowLine.createCell(0).setCellValue("----------------------------------------------------------------" +
                    "-------------------------------------------------------------------------------------------");

            // 23. KHỞI TẠO PHÔNG CHỮ IN ĐẬM TRONG EXCEL
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();      // Khởi tạo font mới
            boldFont.setBold(true);             // Gán font in đậm
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();   // Khởi tạo style cho Cell
            boldStyle.setFont(boldFont);        // Gán style của cell là in đậm

            // 24. Tạo dòng SUM (IN ĐẬM) tổng kết đối soát cuối bảng Excel
            org.apache.poi.ss.usermodel.Row rowSum = sheet.createRow(rowIndex++);

            // 24.1. Ô 0 (STT): Đổ tổng số phiếu và đặt kiểu in đậm
            org.apache.poi.ss.usermodel.Cell cell0 = rowSum.createCell(0);  // Khởi tạo cell 0
            cell0.setCellValue(totalSlip);        // Gán giá trị cho cell0
            cell0.setCellStyle(boldStyle);

            // 24.2. Ô 1 (Mã phiếu): Đổ chữ "TỔNG CỘNG" và đặt kiểu in đậm
            org.apache.poi.ss.usermodel.Cell cell1 = rowSum.createCell(1);  // Khởi tạo cell 1
            cell1.setCellValue("TỔNG CỘNG");        // Gán giá trị cho cell1
            cell1.setCellStyle(boldStyle);

            // 24.3. Ô 2 (username của Nhân viên)
            rowSum.createCell(2).setCellValue("");

            // 24.4. Ô 3 (Ngày mượn)
            rowSum.createCell(3).setCellValue("");

            // 24.5. Ô 4 (Ngày trả)
            rowSum.createCell(4).setCellValue("");

            // 24.6. Ô 5 (Phiếu mượn): Đổ tổng số phiếu mượn do Reader thực hiện
            org.apache.poi.ss.usermodel.Cell cell5 = rowSum.createCell(5);  // Khởi tạo cell 5
            cell5.setCellValue(borrowingSlipSum);        // Gán giá trị cho cell5
            cell5.setCellStyle(boldStyle);

            // 24.7. Ô 6 (Phiếu tốt): Đổ tổng số phiếu tốt do Reader thực hiện
            org.apache.poi.ss.usermodel.Cell cell6 = rowSum.createCell(6);  // Khởi tạo cell 6
            cell6.setCellValue(goodSlipSum);        // Gán giá trị cho cell6
            cell6.setCellStyle(boldStyle);

            // 24.8. Ô 7 (Phiếu trễ hạn): Đổ tổng số phiếu trễ hạn do Reader thực hiện
            org.apache.poi.ss.usermodel.Cell cell7 = rowSum.createCell(7);  // Khởi tạo cell 7
            cell7.setCellValue(overdueSlipSum);        // Gán giá trị cho cell7
            cell7.setCellStyle(boldStyle);

            // 24.9. Ô 8 (Phiếu mất sách): Đổ tổng số phiếu mất sách do Reader thực hiện
            org.apache.poi.ss.usermodel.Cell cell8 = rowSum.createCell(8);  // Khởi tạo cell 8
            cell8.setCellValue(lostBookSlipSum);        // Gán giá trị cho cell8
            cell8.setCellStyle(boldStyle);

            // 24.10. Ô 9 (Sách đang mượn): Đổ tổng số sách hiện do Reader đang mượn
            org.apache.poi.ss.usermodel.Cell cell9 = rowSum.createCell(9);  // Khởi tạo cell 9
            cell9.setCellValue(borrowingBooksSum);        // Gán giá trị cho cell9
            cell9.setCellStyle(boldStyle);

            // 24.11. Ô 10 (Sách trả tốt): Đổ tổng số sách do Reader trả tốt
            org.apache.poi.ss.usermodel.Cell cell10 = rowSum.createCell(10);  // Khởi tạo cell 10
            cell10.setCellValue(goodBooksSum);        // Gán giá trị cho cell10
            cell10.setCellStyle(boldStyle);

            // 24.12. Ô 11 (Sách trễ hạn): Đổ tổng số sách trễ hạn do Reader thực hiện
            org.apache.poi.ss.usermodel.Cell cell11 = rowSum.createCell(11);  // Khởi tạo cell 11
            cell11.setCellValue(overdueBooksSum);        // Gán giá trị cho cell11
            cell11.setCellStyle(boldStyle);

            // 24.13. Ô 12 (Sách mất): Đổ tổng số sách mất do Reader thực hiện
            org.apache.poi.ss.usermodel.Cell cell12 = rowSum.createCell(12);  // Khởi tạo cell 12
            cell12.setCellValue(lostBooksSum);        // Gán giá trị cho cell12
            cell12.setCellStyle(boldStyle);

            // 25. Khởi tạo và gán giá trị vào biến Kết quả [🛡️]
            ReaderLoanStats finalStats = new ReaderLoanStats();
            finalStats.totalSlip = totalSlip;
            finalStats.borrowingSlip = borrowingSlipSum;
            finalStats.overdueSlip = overdueSlipSum;
            finalStats.lostBookSlip = lostBookSlipSum;
            finalStats.goodSlip = goodSlipSum;
            finalStats.overdueBooks = overdueBooksSum;
            finalStats.lostBooks = lostBooksSum;
            finalStats.borrowingBooks = borrowingBooksSum;
            finalStats.goodBooks = goodBooksSum;

            // 26. MỞ LUỒNG TỰ ĐỘNG GHI FILE XUỐNG Ổ CỨNG (Bọc giáp try-with-resources chống rác bộ nhớ)
            String filePath = "data/BaoCaoLichSuGiaoDichDocGia-" + reader.getId() + ".xlsx";
            try(java.io.FileOutputStream fileOut = new java.io.FileOutputStream(filePath)){
                workbook.write(fileOut);            // Xuất file thực tế
                System.out.println("\u2705 Xuất file Excel báo cáo tổng hợp lịch sử giao dịch Đọc giả thành công!" +
                        " Kiểm tra tại: " + filePath);
            }
            workbook.close();

            // 27. Trả kết quả
            return finalStats;

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc xuất báo cáo lịch sử giao dịch một Đọc giả ra file excel"
                    + e.getMessage());
        }
        return null;
    }


    // 12. HÀM BỔ TRỢ ĐƠN NHIỆM: TÍNH TOÁN TOÀN BỘ SỐ LIỆU GIAO DỊCH CỦA 1 ĐỘC GIẢ
    public static ReaderLoanStats getReaderLoanStats(String readerId) throws Exception {
        try{
            // 1. Kiểm tra null hay empty
            if(readerId == null || readerId.isEmpty()){
                return null;
            }

            // 2. Chuẩn hóa readerId
            String cleanReaderId = readerId.trim().toUpperCase();

            // 3. Tìm reader trong danh sách
            Reader reader = ReaderService.findReaderById(cleanReaderId);

            // 4. Kiểm tra không tìm thấy reader
            if(reader == null){
                return null;
            }

            // 5. Khởi tạo biến kết quả và các biến tạm
            ReaderLoanStats stats = new ReaderLoanStats();

            int totalSlip = 0;          // Tổng số phiếu 1 reader thực hiện
            int borrowingSlip = 0;      // Số phiếu reader còn đang mượn
            int overdueSlip = 0;        // Số phiếu bị trễ hạn
            int lostBookSlip = 0;       // Số phiếu có sách bị mất/hỏng nặng
            int goodSlip = 0;           // Số phiếu được trả TỐT
            int overdueBooks = 0;       // Số sách mà reader trả trễ hạn
            int lostBooks = 0;          // Số sách mà reader làm mất/hỏng nặng
            int borrowingBooks = 0;     // Số sách mà reader đang mượn

            // 6. Quét vòng lập danh sách phiếu mượn
            for (LoanSlip loan : LoanSlipService.getLoanList()){
                // 7. Kiểm tra từng phần tử phiếu rỗng, không có readerId, phiếu bị Block
                if (loan == null || loan.getReaderId() == null || loan.isBlock()) {
                    continue; // Bỏ qua, quét phần tử tiếp theo
                }

                // 8. Các điều kiện hợp lệ: So sánh trùng readerId để tổng hợp dữ liệu vào 1 dòng
                if (loan.getReaderId().equalsIgnoreCase(reader.getId())) {
                    totalSlip++;                                // Tăng tổng số phiếu

                    // 9. Khởi tạo biến phân loại loanSlip
                    switch (checkLoanSlipType(loan)) {
                        case TYPE_BORROWING: {
                            borrowingSlip++;                    // Tăng số phiếu đang mượn (chưa trả)
                            break;
                        }
                        case TYPE_OVERDUE: {
                            overdueSlip++;                      // Tăng số phiếu trả trễ hạn
                            break;
                        }
                        case TYPE_LOST_BOOK: {
                            lostBookSlip++;                     // Tăng số phiếu bị mất/hỏng sách
                            break;
                        }
                    }

                    // 10. Tính số lượng sách bị trễ hạn
                    overdueBooks += countOverdueBooksInSlip(loan);

                    // 11. Tính số lượng sách bị mất
                    lostBooks += countLostBooksInSlip(loan);

                    // 12. Tính số sách đọc giả đang mượn
                    if(loan.getStatus() == LoanSlip.BORROWING && loan.getDetails() != null){
                        borrowingBooks += loan.getDetails().size();
                    }
                }
            }

            // 13. Tính số lượng phiếu trả tốt
            goodSlip = totalSlip - (borrowingSlip + overdueSlip + lostBookSlip);

            // 14. Gán giá trị biến vào thuộc tính của stats
            stats.totalSlip = totalSlip;
            stats.borrowingSlip = borrowingSlip;
            stats.overdueSlip = overdueSlip;
            stats.lostBookSlip = lostBookSlip;
            stats.goodSlip = goodSlip;
            stats.overdueBooks = overdueBooks;
            stats.lostBooks = lostBooks;
            stats.borrowingBooks = borrowingBooks;

            // 15. Return kết quả
            return stats;

        } catch (Exception e) {
            System.out.println("Lỗi " + e.getMessage());
        }
        return null;
    }


    // 13. HÀM HỖ TRỢ 1: IN THÔNG TIN HÀNH CHÍNH ĐỌC GIẢ
    private static void printReaderProfile(Reader reader){
        String fullname = reader.getGivenName() + " " + reader.getSurname();
        String gender = reader.getGender() ? "Nam" : "Nữ";
        String job = (reader.getType() == ReaderService.TYPE_STUDENT)? "Sinh viên":
                    (reader.getType() == ReaderService.TYPE_TEACHER) ? "Giáo viên" : "Khác";
        System.out.println("---------------------------------------------------------------------------------------" +
                "------------------------------------------------------------------------------------------------" +
                "-------------------------");
        System.out.println("BÁO CÁO CHI TIẾT LỊCH SỬ GIAO DỊCH CỦA ĐỘC GIẢ");
        System.out.println(String.format("ID: %s | Họ tên: %s | Nghề nghiệp: %s\n"
                        + "Ngày sinh: %s | CCCD: %s | ĐT: %s\n"
                        + "Địa chỉ: %s | Email: %s\n"
                        + "Giới tính: %s | Ngày lập thẻ: %s | Ngày hết hạn: %s\n",
                reader.getId(), fullname, job, reader.getBirthday(), reader.getCitizenId(),
                reader.getPhone(), reader.getAddress(), reader.getEmail(), gender,
                reader.getCreatedDate(), reader.getExpiryDate()));
    }


    // 14. HÀM HỖ TRỢ 2: IN TIÊU ĐỀ 13 CỘT
    private static void printHistoryHeader(){
        System.out.println(String.format("%6s | %-15s | %-12s | %12s | %18s | %15s | %15s | %15s | %15s " +
                        "| %16s | %16s | %16s | %16s",
                "STT", "MÃ PHIẾU", "TÊN USER", "NGÀY MƯỢN", "NGÀY TRẢ THỰC TẾ",
                "PHIẾU ĐANG MƯỢN", "PHIẾU TRẢ TỐT", "PHIẾU TRỄ HẠN", "PHIẾU MẤT SÁCH",
                "SỐ SÁCH ĐANG GIỮ", "SỐ SÁCH TỐT", "SỐ SÁCH TRỄ HẠN", "SỐ SÁCH BỊ MẤT"));
    }


    // 15. HÀM HỖ TRỢ 3: IN TỪNG DÒNG DỮ LIỆU CHI TIẾT
    private static void printHistoryRow(int stt, String loanId, String username, String borrowDate, String returnDate,
                                        String colBorrow, String colGood, String colOverdue, String colLost,
                                        int booKsBorrow, int booksGood, int booksOverdue, int booksLost) {
        System.out.println(String.format("%6d | %-15s | %-12s | %12s | %18s | %15s | %15s | %15s | %15s |" +
                        " %16d | %16d | %16d | %16d",
                stt, loanId, username, borrowDate, returnDate,
                colBorrow, colGood, colOverdue, colLost,
                booKsBorrow, booksGood, booksOverdue, booksLost));
    }


    // 16. HÀM TÍNH TỔNG SỐ SÁCH 1 ĐỌC GIẢ ĐÃ MƯỢN VÀ CHƯA TRẢ
    public static int getReaderBorrowingCount(String readerId) throws Exception{
        LibraryReportService.ReaderLoanStats stats = LibraryReportService.getReaderLoanStats(readerId);
        return (stats != null) ? stats.borrowingBooks : 0;
    }


    // 17. HÀM BÁO CÁO TỔNG HỢP ĐẦU SÁCH TRONG THƯ VIỆN
    public static void reportBookInventory(){
        try{
            // 1. Gọi danh sách bookList
            ArrayList<Book> bookList = BookService.getBookList();

            // 2. Kiểm tra kho sách null hoặc trống
            if(bookList == null || bookList.isEmpty()){
                System.out.println("Kho sách rỗng");
                return;
            }

            // 3. Khởi tạo các biến tích lũy cho dòng SUM cuối bảng báo cáo
            int totalBooksCount = 0;        // Tổng số đầu sách
            int totalCopiesSum = 0;         // Tổng tất cả các bản sao
            int availableSum = 0, rentedSum = 0, lostSum = 0;   // Số sách trên kệ, số sách đang mượn, số sách bị mất

            // 4. In dòng tiêu dề báo cáo
            System.out.println("--------------------------------------------------------------------------------"
            +"--------------------------------------------------------------------------------"
            +"-------------------------");
            System.out.println(String.format("%6s | %-12s | %-35s | %-20s | %15s | %15s | %15s | %15s",
                    "STT", "MÃ SÁCH", "TÊN ĐẦU SÁCH", "TÁC GIẢ", "TỔNG BẢN SAO", "BẢN TRONG KHO", "BẢN ĐANG MƯỢN",
                    "BẢN HỎNG/MẤT"));

            // 5. Quét qua vòng lập for danh sách bookList
            for(Book book: bookList){
                // 6. Kiểm tra từng đầu sách rỗng hoặc Id rỗng
                if(book == null || book.getId() == null){
                    continue;       // Bỏ qua, quét tiếp đầu sách sau
                }

                // 7. Tăng biến đếm tổng số đầu sách
                totalBooksCount++;

                // 8. Khởi tạo 4 biến đếm bản sao cục bộ cho đầu sách hiện tại
                int totalCopies = 0;        // Tổng số bản copy của 1 đầu sách
                int availableCopies = 0;    // Tổng số sách có sẵn trên kệ của 1 đầu sách
                int rentedCopies = 0;       // Tổng số sách đang cho mượn của 1 đầu sách
                int lostCopies = 0;         // Tổng số sách bị mất của 1 đầu sách

                // 9. Quét vòng lập phụ qua danh sách Sách vật lý để truy xuất 4 biến ở bước 8
                for(BookInstance inst: BookInstanceService.getBookInstanceList()){
                    // 10. Kiểm tra từng sách vật lý rỗng hoặc mã đầu sách null
                    if(inst == null || inst.getBookId() == null){
                        continue;       // Bỏ qua, quét tiếp sách vật lý sau
                    }

                    // 11. So sánh: Sách vật lý CÓ ID KHỚP VỚI ID của đầu sách
                    if(inst.getBookId().equalsIgnoreCase(book.getId())){
                        // 12. Tăng số bản sao của đầu sách
                        totalCopies++;

                        // 13. Phân loại trạng thái bản sao -> tính 3 biến đếm bản sao cục bộ cho đầu sách hiện tại
                        if(inst.getStatus() == BookInstanceService.AVAILABLE){
                            availableCopies++;         // Tăng số sách có sẵn trên kệ của 1 đầu sách
                        }
                        if(inst.getStatus() == BookInstanceService.RENTED){
                            rentedCopies ++;            // Tăng số sách đang cho mượn của 1 đầu sách
                        }
                        if(inst.getStatus() == BookInstanceService.LOST_OR_DAMAGED){
                            lostCopies++;               // Tăng số sách bị mất của 1 đầu sách
                        }
                    }
                }

                // 14. Tích lũy vào các biến tích lũy cho dòng SUM cuối bảng báo cáo
                totalCopiesSum += totalCopies;      // Tổng số bản copy của tất cả đầu sách (SUM sách vật lý)
                availableSum += availableCopies;    // Tổng số sách trên kệ của tất cả đầu sách (SUM sách vật lý trên kệ)
                rentedSum += rentedCopies;      // Tổng số sách đang mượn của tất cả đầu sách (SUM sách vật lý đang mượn)
                lostSum += lostCopies;          // Tổng số sách bị mất của tất cả đầu sách (SUM sách vật lý bị mất)

                // 15. In dòng chi tiết thông tin đầu sách
                System.out.println(String.format("%6d | %-12s | %-35s | %-20s | %15d | %15d | %15d | %15d",
                        totalBooksCount, book.getId(), LoanSlipService.truncate(book.getTitle(), 35), LoanSlipService.truncate(book.getAuthor(), 20),
                        totalCopies, availableCopies, rentedCopies, lostCopies));
            }

            // 16. In đường kẻ ngang cuối bản báo cáo --> chuẩn bị in dòng SUM cuối cùng
            System.out.println("--------------------------------------------------------------------------------"
                    +"--------------------------------------------------------------------------------"
                    +"-------------------------");

            // 17. IN ĐẬM SUM TỔNG CỘNG (kết thúc báo cáo): bằng bộ đôi mã màu ANSI (\u001B[1m và \u001B[0m)
            // 17.1. Khai báo hằng số mã màu ANSI nội bộ dòng SUM
            String ANSI_BOLD = "\u001B[1m";     // Mở chế độ in đậm
            String ANSI_RESET = "\u001B[0m";    // Reset chế độ in đậm

            // 17.2. Bật mã đậm ở đầu dòng và tắt mã đậm ở cuối dòng. Cột STT truyền "", cột Mã sách "TỔNG CỘNG"
            System.out.print(ANSI_BOLD);            // Bật mã in đậm
            System.out.println(String.format("%6d | %-12s | %-35s | %-20s | %15d | %15d | %15d | %15d",
                    totalBooksCount, "TỔNG CỘNG", "", "",totalCopiesSum, availableSum, rentedSum, lostSum));
            System.out.print(ANSI_RESET);           // Reset mã in đậm

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc lập báo cáo tổng hợp đầu sách trong thư viện. " + e.getMessage());
        }
    }


    // 18. HÀM XUẤT BÁO CÁO TỒN KHO SÁCH RA FILE EXCEL
    public static void exportBookInventoryToExcel(){
        try{
            // 1. Gọi danh sách bookList
            ArrayList<Book> bookList = BookService.getBookList();

            // 2. Kiểm tra kho sách null hoặc trống
            if(bookList == null || bookList.isEmpty()){
                System.out.println("Kho sách rỗng");
                return;
            }

            // 3. Khởi tạo các biến tích lũy cho dòng SUM cuối bảng báo cáo
            int totalBooksCount = 0;        // Tổng số đầu sách
            int totalCopiesSum = 0;         // Tổng tất cả các bản sao
            int availableSum = 0, rentedSum = 0, lostSum = 0;   // Số sách trên kệ, số sách đang mượn, số sách bị mất

            // 4. KHỞI TẠO CẤU TRÚC FILE EXCEL
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Kho sách");

            int rowIndex = 0;       // Khởi tạo biến đếm dòng trong excel

            // 4.1. Ghi Tiêu đề cột vào Dòng số 0 (Tự tăng rowIndex lên 1 sau khi tạo xong)
            org.apache.poi.ss.usermodel.Row rowHeader = sheet.createRow(rowIndex++);
            rowHeader.createCell(0).setCellValue("STT");
            rowHeader.createCell(1).setCellValue("MÃ SÁCH");
            rowHeader.createCell(2).setCellValue("TÊN ĐẦU SÁCH");
            rowHeader.createCell(3).setCellValue("TÁC GIẢ");
            rowHeader.createCell(4).setCellValue("TỔNG BẢN SAO");
            rowHeader.createCell(5).setCellValue("BẢN TRONG KHO");
            rowHeader.createCell(6).setCellValue("BẢN ĐANG MƯỢN");
            rowHeader.createCell(7).setCellValue("BẢN HỎNG/MẤT");

            // 5. Quét qua vòng lập for danh sách bookList
            for(Book book: bookList){
                // 6. Kiểm tra từng đầu sách rỗng hoặc Id rỗng
                if(book == null || book.getId() == null){
                    continue;       // Bỏ qua, quét tiếp đầu sách sau
                }

                // 7. Tăng biến đếm tổng số đầu sách
                totalBooksCount++;

                // 8. Khởi tạo 4 biến đếm bản sao cục bộ cho đầu sách hiện tại
                int totalCopies = 0;        // Tổng số bản copy của 1 đầu sách
                int availableCopies = 0;    // Tổng số sách có sẵn trên kệ của 1 đầu sách
                int rentedCopies = 0;       // Tổng số sách đang cho mượn của 1 đầu sách
                int lostCopies = 0;         // Tổng số sách bị mất của 1 đầu sách

                // 9. Quét vòng lập phụ qua danh sách Sách vật lý để truy xuất 4 biến ở bước 8
                for(BookInstance inst: BookInstanceService.getBookInstanceList()){
                    // 10. Kiểm tra từng sách vật lý rỗng hoặc mã đầu sách null
                    if(inst == null || inst.getBookId() == null){
                        continue;       // Bỏ qua, quét tiếp sách vật lý sau
                    }

                    // 11. So sánh: Sách vật lý CÓ ID KHỚP VỚI ID của đầu sách
                    if(inst.getBookId().equalsIgnoreCase(book.getId())){
                        // 12. Tăng số bản sao của đầu sách
                        totalCopies++;

                        // 13. Phân loại trạng thái bản sao -> tính 3 biến đếm bản sao cục bộ cho đầu sách hiện tại
                        if(inst.getStatus() == BookInstanceService.AVAILABLE){
                            availableCopies++;         // Tăng số sách có sẵn trên kệ của 1 đầu sách
                        }
                        if(inst.getStatus() == BookInstanceService.RENTED){
                            rentedCopies ++;            // Tăng số sách đang cho mượn của 1 đầu sách
                        }
                        if(inst.getStatus() == BookInstanceService.LOST_OR_DAMAGED){
                            lostCopies++;               // Tăng số sách bị mất của 1 đầu sách
                        }
                    }
                }

                // 14. Tích lũy vào các biến tích lũy cho dòng SUM cuối bảng báo cáo
                totalCopiesSum += totalCopies;      // Tổng số bản copy của tất cả đầu sách (SUM sách vật lý)
                availableSum += availableCopies;    // Tổng số sách trên kệ của tất cả đầu sách (SUM sách vật lý trên kệ)
                rentedSum += rentedCopies;          // Tổng số sách đang mượn của tất cả đầu sách (SUM sách vật lý đang mượn)
                lostSum += lostCopies;              // Tổng số sách bị mất của tất cả đầu sách (SUM sách vật lý bị mất)

                // 15. In dòng chi tiết thông tin đầu sách
                org.apache.poi.ss.usermodel.Row rowData = sheet.createRow(rowIndex++);
                rowData.createCell(0).setCellValue(totalBooksCount);
                rowData.createCell(1).setCellValue(book.getId());
                rowData.createCell(2).setCellValue(book.getTitle());
                rowData.createCell(3).setCellValue(book.getAuthor());
                rowData.createCell(4).setCellValue(totalCopies);
                rowData.createCell(5).setCellValue(availableCopies);
                rowData.createCell(6).setCellValue(rentedCopies);
                rowData.createCell(7).setCellValue(lostCopies);
            }

            // 16. Tạo dòng nét đứt phân cách thẩm mỹ trong Excel
            org.apache.poi.ss.usermodel.Row rowLine = sheet.createRow(rowIndex++);
            rowLine.createCell(0).setCellValue("--------------------------------------------------------------------------------"
                    +"--------------------------------------------------------------------------------"
                    +"-------------------------");

            // 17. KHỞI TẠO PHÔNG CHỮ IN ĐẬM TRONG EXCEL
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();      // Khởi tạo font mới
            boldFont.setBold(true);             // Gán font in đậm
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();   // Khởi tạo style cho Cell
            boldStyle.setFont(boldFont);        // Gán style của cell là in đậm

            // 18. Tạo dòng SUM (IN ĐẬM) tổng kết đối soát cuối bảng Excel
            org.apache.poi.ss.usermodel.Row rowSum = sheet.createRow(rowIndex++);

            // 18.1. Ô 0 (STT): Đổ tổng số độc giả và đặt kiểu in đậm
            org.apache.poi.ss.usermodel.Cell cell0 = rowSum.createCell(0);  // Khởi tạo cell 0
            cell0.setCellValue(totalBooksCount);        // Gán giá trị cho cell0
            cell0.setCellStyle(boldStyle);

            // 18.2. Ô 1 (TỔNG CỘNG): Đổ chữ tổng cộng
            org.apache.poi.ss.usermodel.Cell cell1 = rowSum.createCell(1);  // Khởi tạo cell 1
            cell1.setCellValue("TỔNG CỘNG");        // Gán giá trị cho cell1
            cell1.setCellStyle(boldStyle);

            // 18.3. Ô 2 và 3 (""):
            rowSum.createCell(2).setCellValue("");
            rowSum.createCell(3).setCellValue("");

            // 18.4. Ô 4 : Đổ tổng cộng bản copy sách vật lý
            org.apache.poi.ss.usermodel.Cell cell4 = rowSum.createCell(4);  // Khởi tạo cell 4
            cell4.setCellValue(totalCopiesSum);        // Gán giá trị cho cell4
            cell4.setCellStyle(boldStyle);

            // 18.5. Ô 5: Đổ tổng cộng bản sách vật lý sẵn trên kệ
            org.apache.poi.ss.usermodel.Cell cell5 = rowSum.createCell(5);  // Khởi tạo cell 5
            cell5.setCellValue(availableSum);        // Gán giá trị cho cell5
            cell5.setCellStyle(boldStyle);

            // 18.6. Ô 6 : Đổ tổng cộng bản sách vật lý đang mượn
            org.apache.poi.ss.usermodel.Cell cell6 = rowSum.createCell(6);  // Khởi tạo cell 6
            cell6.setCellValue(rentedSum);        // Gán giá trị cho cell6
            cell6.setCellStyle(boldStyle);

            // 18.7. Ô 7 : Đổ tổng cộng bản sách vật lý bị mất/hỏng nặng
            org.apache.poi.ss.usermodel.Cell cell7 = rowSum.createCell(7);  // Khởi tạo cell 7
            cell7.setCellValue(lostSum);        // Gán giá trị cho cell7
            cell7.setCellStyle(boldStyle);

            // 19. MỞ LUỒNG TỰ ĐỘNG GHI FILE XUỐNG Ổ CỨNG (Bọc giáp try-with-resources chống rác bộ nhớ)
            String filePath = "data/BaoCaoKhoSach.xlsx";
            try(java.io.FileOutputStream fileOut = new java.io.FileOutputStream(filePath)){
                workbook.write(fileOut);        // Xuất file thực tế
                System.out.println("\u2705 Xuất file Excel báo cáo kho sách thành công! Kiểm tra tại: " + filePath);
            }
            workbook.close();

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc xuất báo cáo tổng hợp đầu sách ra file Excel. " + e.getMessage());
        }
    }


    // 19. HÀM BÁO CÁO KPI CỦA TẤT CẢ USER
    public static void reportStaffPerformance(){
        try{
            // 1. Gọi danh sách Phiếu
            ArrayList<LoanSlip> loanList = LoanSlipService.getLoanList();

            // 2. Kiểm tra danh sách Phiếu rỗng hoặc trống
            if(loanList == null || loanList.isEmpty()){
                System.out.println("Hệ thống chưa phát sinh phiếu mượn.");
                return;
            }

            // 3. Khai báo và khởi tạo danh sách HashMap gom tất cả User đã lập phiếu
            HashMap<Integer, StaffOrderStats> staffMap = new HashMap<>();

            // 4. Quét qua vòng lập danh sách phiếu mượn để tích lũy số liệu báo cáo
            for(LoanSlip loan: loanList){
                // 5. Kiểm tra từng Phiếu mượn rỗng hoặc bị block
                if(loan == null || loan.isBlock()){
                    continue;       // Bỏ qua, quét phiếu mượn tiếp theo
                }

                // 6. Lấy mã User làm phiếu mượn
                int staffId = loan.getCreatedByUserId();

                // 7. Kiểm tra nhân viên rác
                if(staffId <=0){
                    continue;       // Bỏ qua nhân viên rác, quét phiếu mượn tiếp theo
                }

                // 8. Nếu UserId chưa có trong danh sách HashMap thì thêm OOP StaffOrderStats vào danh sách
                // Mục đích: Chống trùng lập user
                if(!staffMap.containsKey(staffId)){
                    staffMap.put(staffId,new StaffOrderStats());
                }

                // 9. Khởi tạo inner class StaffOrderStats và lấy dữ liệu HashMap --> gán vào (mặc định = 0)
                StaffOrderStats stats = staffMap.get(staffId);

                // 10. Tăng tổng số phiếu so User lập
                stats.totalSlip ++;

                // 11. Phân loại trạng thái phiếu mượn
                switch (checkLoanSlipType(loan)) {
                    case TYPE_BORROWING: {
                        stats.borrowingSlip ++;               // Tăng số phiếu đang mượn do User lập
                        break;
                    }
                    case TYPE_OVERDUE: {
                        stats.overdueSlip ++;                // Tăng số phiếu trả trễ hạn do User lập
                        stats.violationSlip ++;              // Tăng số phiếu vi phạm do User lập
                        break;
                    }
                    case TYPE_LOST_BOOK: {
                        stats.lostSlip ++;                  // Tăng số phiếu bị mất/hỏng sách do User lập
                        stats.violationSlip ++;             // Tăng số phiếu vi phạm do User lập
                        break;
                    }
                    default:{
                        if(loan.getStatus() == LoanSlip.COMPLETED){
                            stats.goodSlip++;           // Tăng số phiếu tốt do User lập
                        }
                        break;
                    }
                }
            }

            // 12. Khởi tạo các biến tổng cộng toàn cục cho dòng SUM
            int sttCount = 0; int totalSlipSum = 0;
            int borrowingSlipSum = 0, goodSlipSum = 0, overdueSlipSum = 0, lostSlipSum = 0, violationSlipSum = 0;

            // 13. In dòng gạch ngang và dòng tiêu đề
            System.out.println("--------------------------------------------------------------------------------"
                    +"-------------------------------------------------------");

            System.out.println(String.format("%6s | %-15s | %18s | %18s | %18s | %18s | %18s | %18s",
                    "STT", "TÊN USERNAME", "TỔNG PHIẾU LẬP", "PHIẾU ĐANG MƯỢN", "PHIẾU TRẢ TỐT", "PHIẾU TRỄ HẠN",
                    "PHIẾU MẤT SÁCH", "TỔNG PHIẾU VI PHẠM"));

            // 14. Chạy vòng lặp duyệt qua các phần tử của staffMap để bắc cầu in dữ liệu:
            for(Integer statId: staffMap.keySet()){
                sttCount ++;         // Tăng biến STT
                StaffOrderStats stats = staffMap.get(statId);

                // 15. Bắc cầu qua hàm tìm kiếm đa năng findUserByUsernameOrId để lấy username
                String username = "Xóa ngầm";
                User staff = UserService.findUserByUsernameOrId(String.valueOf(statId));

                // 16. Truy xuất tên User
                if(staff != null){
                    username = staff.getUsername();
                }

                // 17. Tích lũy số liệu vào bộ tổng dòng SUM
                totalSlipSum += stats.totalSlip;
                borrowingSlipSum += stats.borrowingSlip;
                goodSlipSum += stats.goodSlip;
                overdueSlipSum += stats.overdueSlip;
                lostSlipSum += stats.lostSlip;
                violationSlipSum += stats.violationSlip;

                // 18. In dòng chi tiết của nhân viên này ra màn hình
                System.out.println(String.format("%6d | %-15s | %18d | %18d | %18d | %18d | %18d | %18d",
                        sttCount, username, stats.totalSlip, stats.borrowingSlip, stats.goodSlip, stats.overdueSlip,
                        stats.lostSlip, stats.violationSlip));
            }

            // 19. In đường kẻ ngang cuối bản báo cáo --> chuẩn bị in dòng SUM cuối cùng
            System.out.println("--------------------------------------------------------------------------------"
            +"-------------------------------------------------------");

            // 20. IN ĐẬM SUM TỔNG CỘNG (kết thúc báo cáo): bằng bộ đôi mã màu ANSI (\u001B[1m và \u001B[0m)
            // 20.1. Khai báo hằng số mã màu ANSI nội bộ dòng SUM
            String ANSI_BOLD = "\u001B[1m";     // Mở chế độ in đậm
            String ANSI_RESET = "\u001B[0m";    // Reset chế độ in đậm

            // 20.2. Bật mã đậm ở đầu dòng và tắt mã đậm ở cuối dòng. Cột STT truyền sttCount,
            // cột Username truyền "TỔNG CỘNG"
            System.out.print(ANSI_BOLD);            // Bật mã in đậm
            System.out.println(String.format("%6d | %-15s | %18d | %18d | %18d | %18d | %18d | %18d",
                    sttCount, "TỔNG CỘNG", totalSlipSum, borrowingSlipSum, goodSlipSum, overdueSlipSum,
                    lostSlipSum, violationSlipSum));
            System.out.print(ANSI_RESET);           // Reset mã in đậm

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc lập báo cáo tổng hợp KPI của nhân viên. " + e.getMessage());
        }
    }


    // 20. HÀM XUẤT BÁO CÁO KPI CỦA TẤT CẢ USER RA FILE EXCEL
    public static void exportStaffPerformanceToExcel(){
        try{
            // 1. Gọi danh sách Phiếu
            ArrayList<LoanSlip> loanList = LoanSlipService.getLoanList();

            // 2. Kiểm tra danh sách Phiếu rỗng hoặc trống
            if(loanList == null || loanList.isEmpty()){
                System.out.println("Hệ thống chưa phát sinh phiếu mượn.");
                return;
            }

            // 3. Khai báo và khởi tạo danh sách HashMap gom tất cả User đã lập phiếu
            HashMap<Integer, StaffOrderStats> staffMap = new HashMap<>();

            // 4. Quét qua vòng lập danh sách phiếu mượn để tích lũy số liệu báo cáo
            for(LoanSlip loan: loanList){
                // 5. Kiểm tra từng Phiếu mượn rỗng hoặc bị block
                if(loan == null || loan.isBlock()){
                    continue;       // Bỏ qua, quét phiếu mượn tiếp theo
                }

                // 6. Lấy mã User làm phiếu mượn
                int staffId = loan.getCreatedByUserId();

                // 7. Kiểm tra nhân viên rác
                if(staffId <=0){
                    continue;       // Bỏ qua nhân viên rác, quét phiếu mượn tiếp theo
                }

                // 8. Nếu UserId chưa có trong danh sách HashMap thì thêm OOP StaffOrderStats vào danh sách
                // Mục đích: Chống trùng lập user
                if(!staffMap.containsKey(staffId)){
                    staffMap.put(staffId,new StaffOrderStats());
                }

                // 9. Khởi tạo inner class StaffOrderStats và lấy dữ liệu HashMap --> gán vào (mặc định = 0)
                StaffOrderStats stats = staffMap.get(staffId);

                // 10. Tăng tổng số phiếu so User lập
                stats.totalSlip ++;

                // 11. Phân loại trạng thái phiếu mượn
                switch (checkLoanSlipType(loan)) {
                    case TYPE_BORROWING: {
                        stats.borrowingSlip ++;               // Tăng số phiếu đang mượn do User lập
                        break;
                    }
                    case TYPE_OVERDUE: {
                        stats.overdueSlip ++;                // Tăng số phiếu trả trễ hạn do User lập
                        stats.violationSlip ++;              // Tăng số phiếu vi phạm do User lập
                        break;
                    }
                    case TYPE_LOST_BOOK: {
                        stats.lostSlip ++;                  // Tăng số phiếu bị mất/hỏng sách do User lập
                        stats.violationSlip ++;             // Tăng số phiếu vi phạm do User lập
                        break;
                    }
                    default:{
                        if(loan.getStatus() == LoanSlip.COMPLETED){
                            stats.goodSlip++;           // Tăng số phiếu tốt do User lập
                        }
                        break;
                    }
                }
            }

            // 12. Khởi tạo các biến tổng cộng toàn cục cho dòng SUM
            int sttCount = 0; int totalSlipSum = 0;
            int borrowingSlipSum = 0, goodSlipSum = 0, overdueSlipSum = 0, lostSlipSum = 0, violationSlipSum = 0;

            // 13. KHỞI TẠO CẤU TRÚC FILE EXCEL KPI NHÂN VIÊN
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("KPI Nhân viên");
            int rowIndex = 0;

            // 14. Ghi tiêu đề cột 8 ô tính vuông vức [🛡️]
            org.apache.poi.ss.usermodel.Row rowHeader = sheet.createRow(rowIndex++);
            rowHeader.createCell(0).setCellValue("STT");
            rowHeader.createCell(1).setCellValue("TÊN USERNAME");
            rowHeader.createCell(2).setCellValue("TỔNG PHIẾU LẬP");
            rowHeader.createCell(3).setCellValue("PHIẾU ĐANG MƯỢN");
            rowHeader.createCell(4).setCellValue("PHIẾU TRẢ TỐT");
            rowHeader.createCell(5).setCellValue("PHIẾU TRỄ HẠN");
            rowHeader.createCell(6).setCellValue("PHIẾU MẤT SÁCH");
            rowHeader.createCell(7).setCellValue("TỔNG PHIẾU VI PHẠM");

            // 15. Chạy vòng lặp duyệt qua các phần tử của staffMap để bắc cầu in dữ liệu:
            for(Integer statId: staffMap.keySet()){
                sttCount ++;         // Tăng biến STT
                StaffOrderStats stats = staffMap.get(statId);

                // 16. Bắc cầu qua hàm tìm kiếm đa năng findUserByUsernameOrId để lấy username
                String username = "Xóa ngầm";
                User staff = UserService.findUserByUsernameOrId(String.valueOf(statId));

                // 17. Truy xuất tên User
                if(staff != null){
                    username = staff.getUsername();
                }

                // 18. Tích lũy số liệu vào bộ tổng dòng SUM
                totalSlipSum += stats.totalSlip;
                borrowingSlipSum += stats.borrowingSlip;
                goodSlipSum += stats.goodSlip;
                overdueSlipSum += stats.overdueSlip;
                lostSlipSum += stats.lostSlip;
                violationSlipSum += stats.violationSlip;

                // 19. Tạo dòng dữ liệu chi tiết cho nhân viên hiện tại trong Excel
                org.apache.poi.ss.usermodel.Row rowData = sheet.createRow(rowIndex++);
                rowData.createCell(0).setCellValue(sttCount);
                rowData.createCell(1).setCellValue(username);
                rowData.createCell(2).setCellValue(stats.totalSlip);
                rowData.createCell(3).setCellValue(stats.borrowingSlip);
                rowData.createCell(4).setCellValue(stats.goodSlip);
                rowData.createCell(5).setCellValue(stats.overdueSlip);
                rowData.createCell(6).setCellValue(stats.lostSlip);
                rowData.createCell(7).setCellValue(stats.violationSlip);
            }

            // 20. Tạo dòng nét đứt phân cách thẩm mỹ trong Excel
            org.apache.poi.ss.usermodel.Row rowLine = sheet.createRow(rowIndex++);
            rowLine.createCell(0).setCellValue("------------------------------------------------------------------" +
                    "----------------------------------");

            // 21. KHỞI TẠO PHÔNG CHỮ IN ĐẬM TRONG EXCEL
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();      // Khởi tạo font mới
            boldFont.setBold(true);             // Gán font in đậm
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();   // Khởi tạo style cho Cell
            boldStyle.setFont(boldFont);        // Gán style của cell là in đậm

            // 22. Tạo dòng SUM (IN ĐẬM) tổng kết đối soát cuối bảng Excel
            org.apache.poi.ss.usermodel.Row rowSum = sheet.createRow(rowIndex++);

            // 22.1. Ô 0 (STT): Đổ tổng số độc giả và đặt kiểu in đậm
            org.apache.poi.ss.usermodel.Cell cell0 = rowSum.createCell(0);  // Khởi tạo cell 0
            cell0.setCellValue(sttCount);        // Gán giá trị cho cell0
            cell0.setCellStyle(boldStyle);

            // 22.2. Ô 1 (TỔNG CỘNG): Đổ chữ tổng cộng
            org.apache.poi.ss.usermodel.Cell cell1 = rowSum.createCell(1);  // Khởi tạo cell 1
            cell1.setCellValue("TỔNG CỘNG");        // Gán giá trị cho cell1
            cell1.setCellStyle(boldStyle);

            // 22.3. Ô 2 : Đổ tổng cộng số phiếu
            org.apache.poi.ss.usermodel.Cell cell2 = rowSum.createCell(2);  // Khởi tạo cell 2
            cell2.setCellValue(totalSlipSum);        // Gán giá trị cho cell2
            cell2.setCellStyle(boldStyle);

            // 22.4. Ô 3 : Đổ tổng số phiếu mượn
            org.apache.poi.ss.usermodel.Cell cell3 = rowSum.createCell(3);  // Khởi tạo cell 3
            cell3.setCellValue(borrowingSlipSum);        // Gán giá trị cho cell3
            cell3.setCellStyle(boldStyle);

            // 22.5. Ô 4 : Đổ tổng số phiếu tốt
            org.apache.poi.ss.usermodel.Cell cell4 = rowSum.createCell(4);  // Khởi tạo cell 4
            cell4.setCellValue(goodSlipSum);        // Gán giá trị cho cell4
            cell4.setCellStyle(boldStyle);

            // 22.6. Ô 5 : Đổ tổng số phiếu trễ hạn
            org.apache.poi.ss.usermodel.Cell cell5 = rowSum.createCell(5);  // Khởi tạo cell 5
            cell5.setCellValue(overdueSlipSum);        // Gán giá trị cho cell5
            cell5.setCellStyle(boldStyle);

            // 22.7. Ô 6 : Đổ tổng số phiếu bị mất/hỏng sách
            org.apache.poi.ss.usermodel.Cell cell6 = rowSum.createCell(6);  // Khởi tạo cell 6
            cell6.setCellValue(lostSlipSum);        // Gán giá trị cho cell6
            cell6.setCellStyle(boldStyle);

            // 22.8. Ô 7 : Đổ tổng số phiếu vi phạm
            org.apache.poi.ss.usermodel.Cell cell7 = rowSum.createCell(7);  // Khởi tạo cell 7
            cell7.setCellValue(violationSlipSum);        // Gán giá trị cho cell7
            cell7.setCellStyle(boldStyle);


            // 23. MỞ LUỒNG TỰ ĐỘNG GHI FILE XUỐNG Ổ CỨNG (Bọc giáp try-with-resources chống rác bộ nhớ)
            String filePath = "data/BaoCaoKPINhânVien.xlsx";
            try(java.io.FileOutputStream fileOut = new java.io.FileOutputStream(filePath)){
                workbook.write(fileOut);            // Xuất file thực tế
                System.out.println("\u2705 Xuất file Excel báo cáo tổng hợp KPI nhân viên thành công! Kiểm tra tại: "
                        + filePath);
            }
            workbook.close();

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc xuất báo cáo tổng hợp KPI của nhân viên ra file excel. "
                    + e.getMessage());
        }
    }


    // 21. HÀM TÍNH TOÁN TỔNG HỢP TÀI CHÍNH
    public static void reportLibraryFinance(){
        try{
            // 1. Gọi danh sách Phiếu
            ArrayList<LoanSlip> loanList = LoanSlipService.getLoanList();

            // 2. Kiểm tra danh sách Phiếu rỗng hoặc trống
            if(loanList == null || loanList.isEmpty()){
                System.out.println("Hệ thống chưa phát sinh dữ liệu tài chính.");
                return;
            }

            // 3. Khởi tạo 4 biến dữ liệu tích lũy dòng tiền toàn cục
            double totalRentalFeeSum = 0.0;         // Tổng tiền thuê sách theo định mức
            double totalPenaltyFeeSum = 0.0;        // Tổng tiền phạt trễ hạn
            double totalLostBookFeeSum = 0.0;       // Tổng tiền phạt mất sách
            double totalPendingFeeSum = 0.0;        // Tổng tiền nợ đọng chưa thu từ các phiếu chưa hoàn thành

            // 4. Quét qua vòng lập danh sách phiếu mượn để tích lũy số liệu báo cáo tài chính
            for(LoanSlip loan: loanList) {
                // 5. Kiểm tra từng Phiếu mượn rỗng hoặc bị block
                if (loan == null || loan.isBlock()) {
                    continue;       // Bỏ qua, quét phiếu mượn tiếp theo
                }

                // 6. Cộng dồn tiền thuê sách theo định mức vào biến tích lũy tiền thuê sách
                totalRentalFeeSum += loan.getTotalRentalFee();

                // 7. Phân nhánh trạng thái phiếu mượn để cộng dồn vào các biến tích lũy tương ứng
                if(loan.getStatus() == LoanSlip.COMPLETED){
                    // 8. Cộng dồn tiền phạt trễ hạn vào biến tích lũy tiền phạt trễ hạn
                    totalPenaltyFeeSum += loan.getOverduePenaltyFee();

                    // 9. Cộng dồn tiền phạt mất sách vào biến tích lũy tiền phạt mất sách
                    totalLostBookFeeSum += loan.getTotalLostBookFee();
                }else {
                    // 10. Cộng dồn tiền phạt mất sách + tiền phạt trễ hạn: vào biến tích lũy tiền nợ đọng
                    totalPendingFeeSum += (loan.getOverduePenaltyFee() + loan.getTotalLostBookFee());
                }
            }

            // 11. Tổng kết tài chính
            double totalSystemFinance = totalRentalFeeSum + totalPenaltyFeeSum + totalLostBookFeeSum
                    + totalPendingFeeSum;

            // 12. Khai báo hằng số mã màu ANSI nội bộ dòng SUM
            String ANSI_BOLD = "\u001B[1m";     // Mở chế độ in đậm
            String ANSI_RESET = "\u001B[0m";    // Reset chế độ in đậm

            // 13. In báo cáo tài chính
            System.out.println("=================================================================");
            System.out.println("              BÁO CÁO TÀI CHÍNH TOÀN DIỆN THƯ VIỆN               ");
            System.out.println("=================================================================");
            System.out.println(String.format(" \u2705 %-35s %25s", "Tổng doanh thu tiền thuê sách gốc:"
                    ,LoanSlipService.formatVND(totalRentalFeeSum)));
            System.out.println(String.format(" \u2705 %-35s %25s", "Tổng doanh thu tiền phạt trễ hạn :"
                    , LoanSlipService.formatVND(totalPenaltyFeeSum)));
            System.out.println(String.format(" \u2705 %-35s %25s", "Tổng doanh thu tiền đền sách mất :"
                    ,LoanSlipService.formatVND(totalLostBookFeeSum)));
            System.out.println(String.format(" \u2705 %-35s %25s", "Tổng các khoản nợ đọng chưa thu  :"
                    ,LoanSlipService.formatVND(totalPendingFeeSum)));
            System.out.println("-----------------------------------------------------------------");
            System.out.print(ANSI_BOLD); // Bật mã in đậm
            System.out.println(String.format(" 👉 %-35s %25s", "TỔNG NGHĨA VỤ TÀI CHÍNH HỆ THỐNG :"
                    ,LoanSlipService.formatVND(totalSystemFinance)));
            System.out.print(ANSI_RESET); // Reset mã in đậm
            System.out.println("=================================================================");

            // "\uD83D\uDC49" là mã Unicode của Emoji (Biểu tượng cảm xúc): 👉

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc lập báo cáo tổng hợp tài chính. " + e.getMessage());
        }
    }


    // 22. HÀM XUẤT BÁO CÁO TỔNG HỢP TÀI CHÍNH RA FILE EXCEL
    public static void exportLibraryFinanceToExcel(){
        try{
            // 1. Gọi danh sách Phiếu
            ArrayList<LoanSlip> loanList = LoanSlipService.getLoanList();

            // 2. Kiểm tra danh sách Phiếu rỗng hoặc trống
            if(loanList == null || loanList.isEmpty()){
                System.out.println("Hệ thống chưa phát sinh dữ liệu tài chính.");
                return;
            }

            // 3. Khởi tạo 4 biến dữ liệu tích lũy dòng tiền toàn cục
            double totalRentalFeeSum = 0.0;         // Tổng tiền thuê sách theo định mức
            double totalPenaltyFeeSum = 0.0;        // Tổng tiền phạt trễ hạn
            double totalLostBookFeeSum = 0.0;       // Tổng tiền phạt mất sách
            double totalPendingFeeSum = 0.0;        // Tổng tiền nợ đọng chưa thu từ các phiếu chưa hoàn thành

            // 4. Quét qua vòng lập danh sách phiếu mượn để tích lũy số liệu báo cáo tài chính
            for(LoanSlip loan: loanList) {
                // 5. Kiểm tra từng Phiếu mượn rỗng hoặc bị block
                if (loan == null || loan.isBlock()) {
                    continue;       // Bỏ qua, quét phiếu mượn tiếp theo
                }

                // 6. Cộng dồn tiền thuê sách theo định mức vào biến tích lũy tiền thuê sách
                totalRentalFeeSum += loan.getTotalRentalFee();

                // 7. Phân nhánh trạng thái phiếu mượn để cộng dồn vào các biến tích lũy tương ứng
                if(loan.getStatus() == LoanSlip.COMPLETED){
                    // 8. Cộng dồn tiền phạt trễ hạn vào biến tích lũy tiền phạt trễ hạn
                    totalPenaltyFeeSum += loan.getOverduePenaltyFee();

                    // 9. Cộng dồn tiền phạt mất sách vào biến tích lũy tiền phạt mất sách
                    totalLostBookFeeSum += loan.getTotalLostBookFee();
                }else {
                    // 10. Cộng dồn tiền phạt mất sách + tiền phạt trễ hạn: vào biến tích lũy tiền nợ đọng
                    totalPendingFeeSum += (loan.getOverduePenaltyFee() + loan.getTotalLostBookFee());
                }
            }

            // 11. Tổng kết tài chính
            double totalSystemFinance = totalRentalFeeSum + totalPenaltyFeeSum + totalLostBookFeeSum
                    + totalPendingFeeSum;

            // 12. KHỞI TẠO CẤU TRÚC FILE EXCEL
            // 12.1. Khởi tạo cuốn sổ Excel trên bộ nhớ RAM
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();

            // 12.2. Tạo một trang tính (sheet) mới đặt tên là "Doanh Thu"
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Doanh Thu");

            // 12.3. Khởi tạo biến chạy để quản lý số thứ tự dòng trong Excel (Bắt đầu từ dòng 0)
            int rowIndex = 0;

            // 12.4. Ghi Tiêu đề báo cáo vào Dòng 0
            org.apache.poi.ss.usermodel.Row rowHeader = sheet.createRow(rowIndex++);
            rowHeader.createCell(0).setCellValue("BÁO CÁO TÀI CHÍNH TOÀN DIỆN THƯ VIỆN");

            // Dòng 1: Để trống tạo khoảng cách thẩm mỹ
            sheet.createRow(rowIndex++);

            // 12.5. Ghi dòng tiền Thuê Sách theo định mức vào Dòng 2
            org.apache.poi.ss.usermodel.Row row1 = sheet.createRow(rowIndex++);
            row1.createCell(0).setCellValue("Tổng doanh thu tiền thuê sách gốc:");
            // Đổ chuỗi đã định dạng VND vào ô bên cạnh
            row1.createCell(1).setCellValue(LoanSlipService.formatVND(totalRentalFeeSum));

            // 12.6. Ghi dòng tiền Phạt trễ hạn vào Dòng 3
            org.apache.poi.ss.usermodel.Row row2 = sheet.createRow(rowIndex++);
            row2.createCell(0).setCellValue("Tổng doanh thu tiền phạt trễ hạn :");
            row2.createCell(1).setCellValue(LoanSlipService.formatVND(totalPenaltyFeeSum));

            // 12.7. Ghi dòng tiền đền sách mất vào Dòng 4
            org.apache.poi.ss.usermodel.Row row3 = sheet.createRow(rowIndex++);
            row3.createCell(0).setCellValue("Tổng doanh thu tiền đền sách mất :");
            row3.createCell(1).setCellValue(LoanSlipService.formatVND(totalLostBookFeeSum));

            // 12.8. Ghi dòng tiền Nợ Đọng (Đọc giả chưa trả sách) vào Dòng 5
            org.apache.poi.ss.usermodel.Row row4 = sheet.createRow(rowIndex++);
            row4.createCell(0).setCellValue("Tổng các khoản nợ đọng chưa thu  :");
            row4.createCell(1).setCellValue(LoanSlipService.formatVND(totalPendingFeeSum));

            // Dòng 6: Ghi đường nét đứt phân cách dòng SUM
            org.apache.poi.ss.usermodel.Row rowLine = sheet.createRow(rowIndex++);
            rowLine.createCell(0).setCellValue("-----------------------------------------------------------------");

            // 12.9. Ghi dòng TỔNG NGHĨA VỤ vào Dòng 7
            org.apache.poi.ss.usermodel.Row rowSum = sheet.createRow(rowIndex++);
            rowSum.createCell(0).setCellValue("TỔNG NGHĨA VỤ TÀI CHÍNH HỆ THỐNG :");
            rowSum.createCell(1).setCellValue(LoanSlipService.formatVND(totalSystemFinance));

            // 12.10. MỞ LUỒNG TỰ ĐỘNG GHI FILE XUỐNG Ổ CỨNG
            // (Bọc trong try-with-resources để tự động đóng file chống rác bộ nhớ)
            String filePath = "data/BaoCaoTaiChinh.xlsx";               // Lưu vào thư mục data cho gọn gàng
            try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(filePath)) {
                workbook.write(fileOut);                                // Xuất file thực tế!
                System.out.println("✅ Xuất file Excel thành công! Vui lòng kiểm tra tại: " + filePath);
                // "\u2705" là mã Unicode của Emoji (Nút đánh dấu biểu tượng cảm xúc): ✅
            }
            workbook.close(); // Giải phóng bộ nhớ RAM của cuốn sổ ảo

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong quá trình xuất file excel. " + e.getMessage());
        }
    }


    // 23. HÀM BÁO CÁO TỔNG HỢP CÁC THẺ ĐỌC GIẢ SẮP HẾT HẠN VÀ CÁC THẺ ĐỌC GIẢ ĐANG BỊ KHÓA
    public static void reportExpiryAndBlockedReaders(){
        try{
            // PHẦN 1: THIẾT LẬP CẤU TRÚC NỀN MÓNG (TRƯỚC VÒNG LẬP)
            // 1. Thiết lập khuôn mẫu định dạng ngày VN
            DateTimeFormatter fomatterDateVN = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate today = LocalDate.now();      // Ngày hiện hành

            // Ngưỡng ngày cảnh báo thẻ hết hạn
            LocalDate alertThresholdDate = today.plusDays(ReaderService.EXPIRY_ALERT_DAYS);

            // 2. Khởi tạo 2 chiếu hộp Danh sách thẻ sắp hết hạn và thẻ bị Khóa
            ArrayList<Reader> alertList = new ArrayList<>();        // DS thẻ sắp hết hạn
            ArrayList<Reader> blockedList = new ArrayList<>();      // DS thẻ bị Khóa

            // PHẦN 2: QUÉT VÒNG LẬP, DUYỆT DANH SÁCH ĐỘC GIẢ (CHỈ GOM DỮ LIỆU, KHÔNG IN)
            for(Reader reader: ReaderService.getReaderList()){
                // 3. Kiểm tra loại bỏ reader rỗng hoặc có Id null
                if(reader == null || reader.getId() == null){
                    continue;       // Bỏ qua, quét phần tử tiếp theo
                }

                // 4. Bọc try .. catch để chuyển đổi an toàn ngày hết hạn từ String sang LocalDate
                LocalDate expiryDate = null;
                try{
                    expiryDate = LocalDate.parse(reader.getExpiryDate(),fomatterDateVN);
                } catch (Exception e) {
                    System.out.println("\u274C Lỗi ngày hết hạn của thẻ " + reader.getId() + " bị lỗi rác.");
                    continue;       // Bỏ qua, quét phần tử tiếp theo
                }

                // 5. CHÈN CHỐT CHẶN PHÂN LOẠI GOM HÀNG VÀO 2 CHIẾC HỘP TẠM
                // 5.1. NHÓM 1: Thẻ sắp hết hạn --> thỏa mãn 3 điều kiện
                // a) Thẻ không bị block
                // b) Ngày hết hạn: Khác null và bằng hoặc sau ngày hôm nay
                // c) Ngày hết hạn: Bằng hoặc trước mốc thời gian cảnh báo 30 ngày
                if(!reader.isBlock()
                        && expiryDate != null && !expiryDate.isBefore(today)
                        && !expiryDate.isAfter(alertThresholdDate)){
                    alertList.add(reader);      // Gom vào HỘP TẠM: Cảnh báo thẻ sắp hết hạn
                }

                // 5.2. NHÓM 2: Thẻ bị Khóa
                if(reader.isBlock()){       // Thẻ bị khóa
                    blockedList.add(reader);        // // Gom vào HỘP TẠM: Thẻ bị Khóa
                }
            }

            // 6. CHỐT CHẶN AN TOÀN TỔNG: Kiểm tra trưởng hợp cả hai danh sách đều trống rỗng
            if(alertList.isEmpty() && blockedList.isEmpty()){
                System.out.println("\u2705 Quét an ninh hoàn tất: Hệ thống sạch dữ liệu, không phát sinh thẻ sắp " +
                        "hết hạn và không phát sinh thả bị Khóa.");
                return;
            }

            // 7. PHẦN 3: IN TỪNG BẢNG DANH SÁCH BÁO CÁO (NGOÀI VÒNG LẬP)
            // 8. Khai báo tiêu đề chung cho cả 2 bảng báo cáo
            String tableHeaderFormat = "%-6s | %-15s | %-30s | %-15s | %-15s | %-15s";

            // 9. BẢNG 1: IN NHÓM THẺ SẮP HẾT HẠN
            if(!alertList.isEmpty()){
                // 10. In tiêu đề
                System.out.println("\n=== DANH SÁCH THẺ ĐỘC GIẢ SẮP HẾT HẠN (TRONG 30 NGÀY) ===");
                System.out.println(String.format(tableHeaderFormat,"STT", "MÃ ĐỌC GIẢ", "HỌ TÊN", "SỐ ĐIỆN THOẠI",
                        "NGÀY LẬP THẺ", "NGÀY HẾT HẠN"));
                System.out.println("-------------------------------------------------------------------------------" +
                        "--------------------------");
                // 11. In dòng dữ liệu
                int sttAlert = 0;
                for(Reader r: alertList){
                    sttAlert ++;
                    String fullname = r.getSurname() + " " + r.getGivenName();
                    System.out.println(String.format(tableHeaderFormat,sttAlert, r.getId(),fullname,r.getPhone(),
                            r.getCreatedDate(),r.getExpiryDate()));
                }

                // 12. In tổng cộng
                System.out.println("-> Tổng số lượng thẻ sắp hết hạn: " + alertList.size() + " thẻ.");
            }

            // 13. BẢNG 2: IN NHÓM THẺ BỊ KHÓA
            if(!blockedList.isEmpty()){
                // 14. In tiêu đề
                System.out.println("\n=== DANH SÁCH THẺ ĐỘC GIẢ BỊ KHÓA ===");
                System.out.println(String.format(tableHeaderFormat,"STT", "MÃ ĐỌC GIẢ", "HỌ TÊN", "SỐ ĐIỆN THOẠI",
                        "NGÀY LẬP THẺ", "NGÀY HẾT HẠN"));
                System.out.println("-------------------------------------------------------------------------------" +
                        "--------------------------");
                // 15. In dòng dữ liệu
                int sttBlocked = 0;
                for(Reader r: blockedList){
                    sttBlocked ++;
                    String fullname = r.getSurname() + " " + r.getGivenName();
                    System.out.println(String.format(tableHeaderFormat,sttBlocked, r.getId(),fullname,r.getPhone(),
                            r.getCreatedDate(),r.getExpiryDate()));
                }

                // 16. In tổng cộng
                System.out.println("-> Tổng số lượng thẻ bị Khóa: " + blockedList.size() + " thẻ.");
            }

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong báo cáo tổng hợp thẻ Đọc giả sắp hết hạn và thẻ Đọc giả bị Khóa "
                    + e.getMessage());
        }
    }


    // 24. HÀM XUẤT BÁO CÁO TỔNG HỢP CÁC THẺ ĐỌC GIẢ SẮP HẾT HẠN VÀ CÁC THẺ ĐỌC GIẢ ĐANG BỊ KHÓA RA FILE EXCEL
    public static void exportExpiryAndBlockedReadersToExcel(){
        try{
            // PHẦN 1: THIẾT LẬP CẤU TRÚC NỀN MÓNG (TRƯỚC VÒNG LẬP)
            // 1. Thiết lập khuôn mẫu định dạng ngày VN
            DateTimeFormatter fomatterDateVN = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate today = LocalDate.now();      // Ngày hiện hành

            // Ngưỡng ngày cảnh báo thẻ hết hạn
            LocalDate alertThresholdDate = today.plusDays(ReaderService.EXPIRY_ALERT_DAYS);

            // 2. Khởi tạo 2 chiếu hộp Danh sách thẻ sắp hết hạn và thẻ bị Khóa
            ArrayList<Reader> alertList = new ArrayList<>();        // DS thẻ sắp hết hạn
            ArrayList<Reader> blockedList = new ArrayList<>();      // DS thẻ bị Khóa

            // PHẦN 2: QUÉT VÒNG LẬP, DUYỆT DANH SÁCH ĐỘC GIẢ (CHỈ GOM DỮ LIỆU, KHÔNG IN)
            for(Reader reader: ReaderService.getReaderList()){
                // 3. Kiểm tra loại bỏ reader rỗng hoặc có Id null
                if(reader == null || reader.getId() == null){
                    continue;       // Bỏ qua, quét phần tử tiếp theo
                }

                // 4. Bọc try .. catch để chuyển đổi an toàn ngày hết hạn từ String sang LocalDate
                LocalDate expiryDate = null;
                try{
                    expiryDate = LocalDate.parse(reader.getExpiryDate(),fomatterDateVN);
                } catch (Exception e) {
                    System.out.println("\u274C Lỗi ngày hết hạn của thẻ " + reader.getId() + " bị lỗi rác.");
                    continue;       // Bỏ qua, quét phần tử tiếp theo
                }

                // 5. CHÈN CHỐT CHẶN PHÂN LOẠI GOM HÀNG VÀO 2 CHIẾC HỘP TẠM
                // 5.1. NHÓM 1: Thẻ sắp hết hạn --> thỏa mãn 3 điều kiện
                // a) Thẻ không bị block
                // b) Ngày hết hạn: Khác null và bằng hoặc sau ngày hôm nay
                // c) Ngày hết hạn: Bằng hoặc trước mốc thời gian cảnh báo 30 ngày
                if(!reader.isBlock()
                        && expiryDate != null && !expiryDate.isBefore(today)
                        && !expiryDate.isAfter(alertThresholdDate)){
                    alertList.add(reader);      // Gom vào HỘP TẠM: Cảnh báo thẻ sắp hết hạn
                }

                // 5.2. NHÓM 2: Thẻ bị Khóa
                if(reader.isBlock()){       // Thẻ bị khóa
                    blockedList.add(reader);        // // Gom vào HỘP TẠM: Thẻ bị Khóa
                }
            }

            // 6. CHỐT CHẶN AN TOÀN TỔNG: Kiểm tra trưởng hợp cả hai danh sách đều trống rỗng
            if(alertList.isEmpty() && blockedList.isEmpty()){
                System.out.println("\u2705 Quét an ninh hoàn tất: Hệ thống sạch dữ liệu, không phát sinh thẻ sắp " +
                        "hết hạn và không phát sinh thả bị Khóa.");
                return;
            }

            // 7. KHỞI TẠO CẤU TRÚC FILE EXCEL
            // 8. Khởi tạo cuốn sổ Excel trên bộ nhớ RAM
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();

            // 9. KHởi tạo phong chữ in đậm trong Excel
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();      // Khởi tạo font mới
            boldFont.setBold(true);             // Gán font in đậm
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();   // Khởi tạo style cho Cell
            boldStyle.setFont(boldFont);        // Gán style của cell là in đậm

            // 10. XUẤT SHEET "Thẻ sắp hết hạn" và biến chạy
            // 10.1. Tạo một trang tính (sheet1) mới đặt tên là "Thẻ sắp hết hạn" và biến chạy
            org.apache.poi.ss.usermodel.Sheet sheet1 = workbook.createSheet("Thẻ sắp hết hạn");
            int rowIndex1 = 0;

            // 10.2. in dòng tiêu đề báo cáo
            org.apache.poi.ss.usermodel.Row rowTitle1 = sheet1.createRow(rowIndex1++);
            rowTitle1.createCell(0).setCellValue("DANH SÁCH THẺ ĐỘC GIẢ SẮP HẾT HẠN");

            // 10.3. In dòng trắng
            org.apache.poi.ss.usermodel.Row rowHline1 = sheet1.createRow(rowIndex1++);
            rowHline1.createCell(0).setCellValue("");

            // 10.4. In dòng tiêu đề bảng
            org.apache.poi.ss.usermodel.Row rowHeader1 = sheet1.createRow(rowIndex1++);
            rowHeader1.createCell(0).setCellValue("STT");
            rowHeader1.createCell(1).setCellValue("MÃ ĐỌC GIẢ");
            rowHeader1.createCell(2).setCellValue("HỌ TÊN");
            rowHeader1.createCell(3).setCellValue("SỐ ĐIỆN THOẠI");
            rowHeader1.createCell(4).setCellValue("NGÀY LẬP THẺ");
            rowHeader1.createCell(5).setCellValue("NGÀY HẾT HẠN");

            // 10.5. In dòng dữ liệu nhóm thẻ sắp hết hạn
            if(!alertList.isEmpty()){
                int sttAlert = 0;
                // 10.6. Quét vòng lập bảng kết quả
                for(Reader r: alertList){
                    sttAlert ++;
                    String fullname = r.getSurname() + " " + r.getGivenName();

                    // 10.7 Tạo dòng dữ liệu chi tiết cho từng thẻ trong Excel
                    org.apache.poi.ss.usermodel.Row rowData1 = sheet1.createRow(rowIndex1++);
                    rowData1.createCell(0).setCellValue(sttAlert);
                    rowData1.createCell(1).setCellValue(r.getId());
                    rowData1.createCell(2).setCellValue(fullname);
                    rowData1.createCell(3).setCellValue(r.getPhone());
                    rowData1.createCell(4).setCellValue(r.getCreatedDate());
                    rowData1.createCell(5).setCellValue(r.getExpiryDate());
                }

                // 10.8. In tổng cộng
                org.apache.poi.ss.usermodel.Row rowSum1 = sheet1.createRow(rowIndex1++);

                org.apache.poi.ss.usermodel.Cell cell1 = rowSum1.createCell(1);  // Khởi tạo cell 1
                cell1.setCellValue("TỔNG CỘNG");        // Gán giá trị cho cell1
                cell1.setCellStyle(boldStyle);

                org.apache.poi.ss.usermodel.Cell cell2 = rowSum1.createCell(2);  // Khởi tạo cell 2
                cell2.setCellValue(alertList.size());        // Gán giá trị cho cell2
                cell2.setCellStyle(boldStyle);
            }

            // 11. XUẤT SHEET "Thẻ bị Khóa" và biến chạy
            // 11.1. Tạo một trang tính (sheet2) mới đặt tên là "Thẻ bị Khóa" và biến chạy
            org.apache.poi.ss.usermodel.Sheet sheet2 = workbook.createSheet("Thẻ bị Khóa");
            int rowIndex2 = 0;

            // 11.2. in dòng tiêu đề báo cáo
            org.apache.poi.ss.usermodel.Row rowTitle2 = sheet2.createRow(rowIndex2++);
            rowTitle2.createCell(0).setCellValue("DANH SÁCH THẺ ĐỘC GIẢ BỊ KHÓA");

            // 11.3. In dòng trắng
            org.apache.poi.ss.usermodel.Row rowHline2 = sheet2.createRow(rowIndex2++);
            rowHline2.createCell(0).setCellValue("");

            // 11.4. In dòng tiêu đề bảng
            org.apache.poi.ss.usermodel.Row rowHeader2 = sheet2.createRow(rowIndex2++);
            rowHeader2.createCell(0).setCellValue("STT");
            rowHeader2.createCell(1).setCellValue("MÃ ĐỌC GIẢ");
            rowHeader2.createCell(2).setCellValue("HỌ TÊN");
            rowHeader2.createCell(3).setCellValue("SỐ ĐIỆN THOẠI");
            rowHeader2.createCell(4).setCellValue("NGÀY LẬP THẺ");
            rowHeader2.createCell(5).setCellValue("NGÀY HẾT HẠN");

            // 11.5. In dòng dữ liệu nhóm thẻ bị Khóa
            if(!blockedList.isEmpty()){
                int sttBlocked = 0;
                // 11.6. Quét vòng lập bảng kết quả
                for(Reader r: blockedList){
                    sttBlocked ++;
                    String fullname = r.getSurname() + " " + r.getGivenName();

                    // 11.7 Tạo dòng dữ liệu chi tiết cho từng thẻ trong Excel
                    org.apache.poi.ss.usermodel.Row rowData2 = sheet2.createRow(rowIndex2++);
                    rowData2.createCell(0).setCellValue(sttBlocked);
                    rowData2.createCell(1).setCellValue(r.getId());
                    rowData2.createCell(2).setCellValue(fullname);
                    rowData2.createCell(3).setCellValue(r.getPhone());
                    rowData2.createCell(4).setCellValue(r.getCreatedDate());
                    rowData2.createCell(5).setCellValue(r.getExpiryDate());
                }

                // 11.8. In tổng cộng
                org.apache.poi.ss.usermodel.Row rowSum2 = sheet2.createRow(rowIndex2++);

                org.apache.poi.ss.usermodel.Cell cell1 = rowSum2.createCell(1);  // Khởi tạo cell 1
                cell1.setCellValue("TỔNG CỘNG");        // Gán giá trị cho cell1
                cell1.setCellStyle(boldStyle);

                org.apache.poi.ss.usermodel.Cell cell2 = rowSum2.createCell(2);  // Khởi tạo cell 2
                cell2.setCellValue(blockedList.size());        // Gán giá trị cho cell2
                cell2.setCellStyle(boldStyle);
            }

            // 12. MỞ LUỒNG TỰ ĐỘNG GHI FILE XUỐNG Ổ CỨNG (Bọc giáp try-with-resources chống rác bộ nhớ)
            String filePath = "data/BaoCaoCanhBaoVaKhoaThe.xlsx";
            try(java.io.FileOutputStream fileOut = new java.io.FileOutputStream(filePath)){
                workbook.write(fileOut);        // Xuất file thực tế
                System.out.println("\u2705 Xuất file Excel báo cáo thẻ sắp hết hạn và thẻ bị khóa thành công! " +
                        "Kiểm tra tại: " + filePath);
            }
            workbook.close();

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong xuất báo cáo tổng hợp thẻ Đọc giả sắp hết hạn và thẻ Đọc giả bị Khóa" +
                    "ra file Excel " + e.getMessage());
        }
    }


    // INNER CLASS: ĐỂ TẠO OOP KPI USER
    public static final class StaffOrderStats{
        int totalSlip = 0;       // Tổng số phiếu do 1 nhân viên lập.
        int borrowingSlip = 0;   // Số phiếu đang mượn do 1 nhân viên lập
        int goodSlip = 0;        // Số phiếu tốt do 1 nhân viên lập
        int overdueSlip = 0;     // Số phiếu trễ hạn do 1 nhân viên lập
        int lostSlip = 0;        // Số phiếu có sách bị mất do 1 nhân viên lập
        int violationSlip = 0;   // Số phiếu đang mượn do 1 nhân viên lập
    }


    // INNER CLASS: ĐỂ TẠO OOP TỔNG HỢP PHIẾU CỦA 1 READER
    public static final class ReaderLoanStats {
        int totalSlip = 0;          // Tổng số phiếu 1 reader thực hiện
        int borrowingSlip = 0;      // Số phiếu reader còn đang mượn
        int overdueSlip = 0;        // Số phiếu bị trễ hạn
        int lostBookSlip = 0;       // Số phiếu có sách bị mất/hỏng nặng
        int goodSlip = 0;           // Số phiếu được trả TỐT
        int overdueBooks = 0;       // Số sách mà reader trả trễ hạn
        int lostBooks = 0;          // Số sách mà reader làm mất/hỏng nặng
        int borrowingBooks = 0;     // Số sách mà reader đang mượn
        int goodBooks = 0;          // Số sách mà reader trả tốt
    }


}
