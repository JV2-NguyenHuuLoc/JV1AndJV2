package loanSlip;


import book.Book;
import book.BookInstance;
import book.BookInstanceService;
import book.BookService;
import reader.Reader;
import reader.ReaderService;
import report.LibraryReportService;
import user.User;
import user.UserService;

import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class LoanSlipService implements Serializable {

    // KHAI BÁO CÁC BIẾN HẰNG VỀ TÊN
    private static final int GIVEN_NAME = 0;
    private static final int SURNAME = 1;

    // KHAI BÁO BIẾN HẰNG TỔNG SỐ SÁCH CHO THUÊ TRONG 1 PHIẾU VÀ TỔNG SỐ SÁCH TỐI ĐA 1 READER MƯỢN
    private static final int MAX_BOOKS_PER_SLIP = 3;    // Tổng số sách trong 1 phiếu mượn
    private static final int MAX_BOOKS_LIMIT = 5;       // Tổng số sách 1 Đọc giả có thể mượn

    // KHAI BÁO BIẾN HẰNG VỀ TIỀN ĐẶT CỌC TỐI THIỂU VÀ TIỀN THUÊ SÁCH
    public static final int MIN_DEPOSIT_INDEX = 0;
    public static final int TOTAL_RENTAL_FEE_INDEX = 1;

    // BIẾN HẰNG PHÂN QUYỀN TÌM KIẾM PHIẾU MƯỢN (CHỐNG MAGIC NUMBERS)
    public static final int SEARCH_BY_LOAN_ID = 0;  // Tìm phiếu mượn theo loanId để làm hàm returnLoanSlip
    public static final int SEARCH_BY_READER_ID_OR_NAME = 1;    // Tìm phiếu mượn phục vụ thống kê, báo cáo
    public static final int SEARCH_BY_USER_ID_OR_USERNAME = 2;    // Tìm phiếu mượn phục vụ thống kê, báo cáo
    public static final int SEARCH_BY_BOOK_ID_OR_TITLE = 3;    // Tìm phiếu mượn phục vụ thống kê, báo cáo

    // BIẾN HẰNG TÌM KIẾM MÃ PHIẾU MƯỢN SÁCH ĐỘC BẢN (DUY NHẤT)
    public static final int UNIQUE_RESULT_INDEX = 0;        // Kết quả tìm kiếm trả về DUY NHẤT 1 INDEX

    // BIẾN HẰNG ĐƠN GIÁ PHẠT TRẢ SÁCH TRỄ HẠN
    public static final double DAILY_OVERDUE_PENALTY_FEE = 5000.0;     // Đơn giá tiền phạt trễ hạn: 5.000 đ/ngày

    // BIẾN HẰNG HỆ SỐ PHẠT TIỀN MẤT SÁCH
    public static final double LOST_BOOK_PENALTY_MULTIPLIER = 2.0;      // Tiền phát mất sách: 200% * Đơn giá sách

    // BIẾN HẰNG TRẠNG THÁI,TÌNH TRẠNG SÁCH KHI TRẢ
    public static final int BOOK_CONDITION_GOOD = 0;     // Trạng thái sách khi được trả: TỐT

    // KHAI BÁO BIẾN TOÀN CỤC
    private static final Scanner sc = new Scanner(System.in);
    private static final ArrayList<LoanSlip> loanList = new ArrayList<>();

    // Phương thức Getter

    public static ArrayList<LoanSlip> getLoanList() { return loanList;    }

    // Phương thức Setter
    public static void addLoanList(LoanSlip loanSlip) { loanList.add(loanSlip);    }


    // 1. HÀM TÍNH TIỀN ĐẶT CỌC TỐI THIỂU VÀ TỔNG TIỀN THUÊ SÁCH
    private static double[] calculateFinancials(ArrayList<LoanDetail> details) throws Exception{

        // 1. Khai báo tiền đặt cọc tối thiểu và tiền thuê sách
        double minDeposit = 0;
        double totalRentalFee = 0;

        // 2. Truy xuất ngược từ bookInstanceId về bookId
        for (LoanDetail loanDetail: details){
            String currentBookInstId = loanDetail.getBookInstanceId();

            // 3. Tách chuỗi bookInstanceId ra thành bookId theo ký tự đặc biệt "-"
            String currentBookId = currentBookInstId.split("-")[0];

            // 4. Tìm sách từ danh sách
            Book book = BookService.findBookById(currentBookId);

            // 5. Tính toán tiền đặt cọc tối thiểu và tiền thuê sách
            if(book != null){
                minDeposit += book.getOriginalPrice();
                totalRentalFee += book.getRentalPrice() * LoanSlip.DEFAULT_RENTAL_DAYS;
            }
        }

        // 6. Khai báo và xuất kết quả
        double[] financialResult = new double[2];
        financialResult[MIN_DEPOSIT_INDEX] = minDeposit;
        financialResult[TOTAL_RENTAL_FEE_INDEX] = totalRentalFee;

        return financialResult;
    }


    // 2. CÁC HÀM TÍNH TIỀN PHẠT

    // HÀM TÍNH TIỀN PHẠT TRỄ HẠN
    private static double calculateOverduePenalty(LoanSlip loan, LocalDate actualDate) throws Exception{
        // 1. Kiểm tra phiếu rỗng hoặc Phiếu không có ngày trả
        if(loan == null || loan.getExpectedReturnDate() == null){
            return 0.0;
        }

        // 2. Tính số ngày trễ hạn
        long daysOverdue = ChronoUnit.DAYS.between(loan.getExpectedReturnDate(),actualDate);

        // 3. Dùng toán tử 3 ngôi (toán tử điều kiện) để xuất kết quả
        return daysOverdue > 0 ? (daysOverdue * DAILY_OVERDUE_PENALTY_FEE) : 0.0;
    }


    // HÀM TÍNH TIỀN PHẠT MẤT SÁCH
    private static double calculateLostBookPenalty(String bookInstance) throws Exception{
        // 1. Kiểm tra phiếu rỗng hoặc Phiếu không có ngày trả
        if(bookInstance == null || bookInstance.isEmpty()){
            return 0.0;
        }

        // 2. Duyệt và tìm Sách vật lý
        BookInstance resultBookInst = BookInstanceService.findBookInstance(bookInstance.trim().toUpperCase(),
                BookInstanceService.SEARCH_BY_ID);

        // 3. Kiểm tra không tìm thấy Sách vật lý
        if(resultBookInst == null){
            return 0.0;
        }

        // 4. Cắt chuỗi lấy BookId
        String bookId = resultBookInst.getBookId().split("-")[0];

        // 5. Tìm đầu sách chứa giá bìa
        Book resultBook = BookService.findBook(bookId);

        // 6. Kiểm tra đầu sách không có
        if(resultBook == null){
            return 0.0;
        }

        // 7. Các điều kiện hợp lệ
        return resultBook.getOriginalPrice() * LOST_BOOK_PENALTY_MULTIPLIER;
    }


    // 3. HÀM KHỞI TẠO TỰ ĐỘNG ID CHO LoanSlip
    private static String autoCreateLoanSlipId() throws Exception{
        String prefix = "", prefixErr = "PM-FALLBACK-", dateStr ="000000";
        int nextSequence = 0;
        String nextSequenceStr = "000";
        try{
            // 1. Lấy thời gian thực để dập khuôn chuỗi ngày hôm nay
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
            dateStr = now.format(formatter);     // Xuất chuỗi dưới dạng: 260529

            // 2. Thiết lập tiền tố cố định: 10 ký tự.
            prefix = "PM-" + dateStr + "-";  // VD: PM-260529-
            int prefixLength = prefix.length();     // Lấy độ dài động để cắt chuỗi an toàn

            // 3. Khởi tạo biến đếm động và quét tìm số lớn nhất trong ngày với màng bọc bảo vệ
            int maxSequence = 0;    // Nếu loanList trống, biến này giữ nguyên = 0, tự động sinh ra "001" ở bước sau

            for(LoanSlip currentLoan: loanList){
                // 3.1 Chỉ lọc những phiếu được tạo ra trong ngày hôm nay
                if (currentLoan != null && currentLoan.getId() != null && currentLoan.getId().startsWith(prefix)){
                    try{
                        if (currentLoan.getId().startsWith(prefix)){
                            // 3.1.1. Cắt chuỗi dựa trên độ dài động của prefix (Ví dụ: "002")
                            String seqStr = currentLoan.getId().substring(prefixLength).trim();

                            // 3.1.2. Bẫy Regex: Chỉ xử lý nếu phần đuôi hoàn toàn là chữ số
                            if(seqStr.matches("\\d+")){
                                int intSeqStr = Integer.parseInt(seqStr);
                                if (intSeqStr > maxSequence){
                                    maxSequence = intSeqStr;
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Phòng hờ các trường hợp lỗi cắt chuỗi ngoài ý muốn, bỏ qua để không sập hệ thống
                        continue;
                    }
                }
            }

            // 4. Tăng số thứ tự và dập khuôn bù số 0 đầu để thêm vào phiếu
            nextSequence = maxSequence + 1;
            nextSequenceStr = String.format("%03d", nextSequence);

            // 5. Trả về mã phiếu mượn thông minh hoàn chỉnh
            return prefix + nextSequenceStr;        // Kết quả: "PM-260529-001"
        } catch (Exception e) {
            System.out.println("\u274C Lỗi hệ thống tự sinh mã phiếu: " + e.getMessage());
            return prefixErr + dateStr + "-" + nextSequenceStr;  // VD: PM-FALLBACK-260529-
        }
    }


    // 4. HÀM NHẬP THẺ ĐỌC GIẢ
    private static String inputReaderId(String prompt, boolean isUpdate) throws Exception{
        while (true){
            try{
                System.out.print(prompt);
                String input = sc.nextLine().trim().toUpperCase();

                // 1. Cơ chế thoát nhanh: nếu người dùng chọn nhầm chức năng
                if(input.equalsIgnoreCase("exit")){
                    return "EXIT";
                }

                // 2. Nếu là Update và để trống: Người dùng giữ nguyên mã thẻ đọc giả cũ (chạy trong hàm updateLoanSlip)
                if (isUpdate && input.isEmpty()) {
                    return "";
                }

                // 3. Nếu chạy trong hàm createLoanSlip
                if(!ReaderService.checkReaderIdExist(input)){
                    System.out.println("\u274C Lỗi thẻ đọc giả không tồn tại");
                    continue;
                }
                return input;
            } catch (Exception e) {
                System.out.println("\u274C Lỗi " + e.getMessage());
            }
        }
    }


    // 5. CÁC HÀM HỖ TRỢ KHÁC

    // Hàm hỏi người dùng có tiếp tục không?
    private static int askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim().toLowerCase();
            if (input.isEmpty()) {
                continue;
            }
            if (input.equals("y")) {
                return 1;
            }
            if (input.equals("n")) {
                return 0;
            }
            System.out.println("Vui lòng chỉ nhập Y hoặc N. ");
        }
    }

    // Hàm hiển thị tiền đồng VN
    public static String formatVND(Double amount){
        // 1. Tạo Locale cấu hình cho Việt Nam
        Locale localVN = new Locale("vi","VN");

        // 2. Lấy bộ định dạng số theo chuẩn Locale Việt Nam
        NumberFormat numberFormat = NumberFormat.getNumberInstance(localVN);

        // 3. Cấu hình bắt buộc luôn hiển thị 2 chữ số thập phân (,00)
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        // 4. Định dạng số và thêm chữ "đồng" ở cuối
        return numberFormat.format(amount) + "đồng";
    }


    // Hàm đổi ngày sang định dạng VN (dd/MM/yyyy)
    public static String formatLocalDateVN(java.time.LocalDate date) {
        if (date == null) {
            return "-";
        }
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    // Hàm bổ trợ để tránh việc tên sách quá dài làm vỡ khung (Layout)
    public static String truncate(String text, int size) {
        if (text == null){
            return "";
        }
        return (text.length() > size) ? text.substring(0, size - 3) + "..." : text;
    }


    // 6. HÀM NHẬP TIỀN ĐẶT CỌC
    private static double inputDeposit(String prompt, ArrayList<LoanDetail> details) throws Exception{

        // 1. Tính số tiền đặt cọc tối thiểu và Tổng tiền thuê sách
        double[] financials = calculateFinancials(details);

        // 2. Nhập tiền đặt cọc
        while (true){
            try{
                System.out.print(prompt);
                double deposit = Double.parseDouble(sc.nextLine());
                if(deposit < financials[MIN_DEPOSIT_INDEX]){
                    System.out.println("\u274C Lỗi: số tiền đặt cọc tối thiểu từ " + financials[MIN_DEPOSIT_INDEX]);
                    continue;
                }
                return deposit;
            } catch (Exception e) {
                System.out.println("\u274C Lỗi " + e.getMessage());
            }
        }
    }


    // 7. CÁC HÀM VỀ READ - WRITE FILE
    // Hàm ghi danh sách loanList vào file
    public static void saveListToFile(ArrayList<LoanSlip> loanList, String filePath) {
        File folder = new File("data");
        if (!folder.exists()) folder.mkdir();       // Tự tạo thư mục data nếu chưa có.

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))){
            oos.writeObject(loanList);
        } catch (IOException e) {
            System.out.println("Lỗi ghi file " + e.getMessage());
        }
    }

    // Hàm đọc file ra danh sách ArrayList<LoanSlip> loanList
    public static ArrayList<LoanSlip> readFileToList(String filePath) {
        ArrayList<LoanSlip> tempList = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))){
            tempList = (ArrayList<LoanSlip>) ois.readObject();

        } catch (FileNotFoundException e) {
            System.out.println("Khởi tạo dữ liệu mới.");
        } catch (Exception e) {
            System.out.println("Lỗi nạp dữ liệu: " + e.getMessage());
        }
        return tempList;
    }


    // 8. HÀM NHẬP TỪNG ĐẦU SÁCH VẬT LÝ
    public static LoanDetail inputSingleLoanDetail(ArrayList<LoanDetail> currentCart)throws Exception{
        while (true){
            try{
                System.out.println("Nhập ISBN sách cần mượn (Enter để kết thúc): ");
                String inputIsbn = sc.nextLine().trim().toUpperCase();

                // 1. Trường hợp 1: Dừng chọn sách, kết thúc sớm
                if(inputIsbn.isEmpty()){
                    return null;
                }

                // 2. Gọi siêu hàm đa hình tìm đầu sách gốc tổng (Tìm được cả theo ID, ISBN, Tên sách)
                Book book = BookService.findBook(inputIsbn);

                // 3. Nếu tìm không được đầu sách có isbn
                if (book == null){
                    System.out.println("\u274C Không tìm thấy thông tin sách tương ứng trong thư viện. " +
                            "Vui lòng chọn sách khác.");
                    continue;
                }

                // 4. Kiểm tra chống đọc giả mượn trùng đầu sách trong 1 phiếu
                boolean isDuplicate = false;
                if(currentCart.size() != 0){
                    for (LoanDetail loanDetail: currentCart){
                        if(loanDetail.getBookInstanceId().contains(book.getIsbn().replaceAll("-",""))){
                            System.out.println("\u274C Không được nhập trùng sách trong cùng 1 phiếu mượn. " +
                                    "Vui lòng chọn sách khác");
                            isDuplicate = true;
                            break;
                        }
                    }
                }

                // 5. Trường hợp bị trùng sách trong 1 phiếu mượn, thoát ra để nhập lại ISBN
                if(isDuplicate){
                    continue;
                }

                // 6. BỘ LỌC PHÂN LUỒNG: Thủ thư quét mã sách vật lý hoặc nhập ISBN từ bàn phím
                BookInstance bookInstance = null;       // Khai báo biến tạm

                // Kiểm tra xem chuỗi nhập vào có chứa chữ cái và dính dấu gạch ngang của mã vật lý hay không
                boolean isPhysicalScan = inputIsbn.contains("-") && inputIsbn.matches(".*[A-Z].*");
                
                if(isPhysicalScan){
                    // 6.1. KỊCH BẢN A: Quét mã vạch tại quầy -> Gọi thẳng chế độ SEARCH_BY_ID để bốc chính xác cuốn đó.
                    bookInstance = BookInstanceService.findBookInstance(inputIsbn,BookInstanceService.SEARCH_BY_ID);

                    // Kiểm tra an toàn: Nếu cuốn sách cụ thể này đang bị ai đó mượn hoặc báo mất (status != AVAILABLE)
                    if(bookInstance != null && bookInstance.getStatus() != BookInstanceService.AVAILABLE){
                        System.out.println("\u274C Lỗi: Cuốn sách vật lý có mã " + inputIsbn + " hiện không sẵn " +
                                "sàng trên kệ (Đang cho mượn hoặc đã mất)!");
                        continue;
                    }
                } else {
                    // 6.2. KỊCH BẢN B: Nhập ISBN/Đặt online -> Gọi chế độ SEARCH_AVAILABLE_FIFO để tự động bốc cuốn rảnh
                    bookInstance = BookInstanceService.findBookInstance(book.getId(),
                            BookInstanceService.SEARCH_AVAILABLE_FIFO);
                }

                // 7. Chốt chặn cuối cùng cho kho bãi vật lý
                if(bookInstance == null){
                    System.out.println("\u274C Sách đã được mượn hết trên kệ. Vui lòng chọn sách khác.");
                    continue;
                }

                // 8. Tất cả đều hoàn hảo: Khởi tạo đối tượng và trả về kết quả mang mã vật lý chính xác
                return new LoanDetail(bookInstance.getId(),false,0);

            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 9. HÀM TẠO GIỎ HÀNG SÁCH VẬT LÝ
    public static ArrayList<LoanDetail> buildLoanDetailsCart(int oldBooksCount) throws Exception{
        // 1. Khởi tạo danh sách giỏ hàng SÁCH VẬT LÝ
        ArrayList<LoanDetail> cart = new ArrayList<>();

        // 2. Giới hạn cho mượn tối đa 3 cuốn/phiếu
        while (cart.size() < MAX_BOOKS_PER_SLIP){
            try {
                System.out.println("\n------ Nhập cuốn sách thứ " + (cart.size() + 1) + ": ");
                // 3. Kiểm tra hạn mức kép: Đếm sách nợ cũ ở nhà + Số sách đã quét vào giỏ hiện tại
                if(oldBooksCount + cart.size() >= MAX_BOOKS_LIMIT){
                    System.out.println("\uD83D\uDED1 TỪ CHỐI QUÉT THÊM! Độc giả đã chạm ngưỡng hạn mức tối đa "
                            + MAX_BOOKS_LIMIT + " cuốn sách/đọc giả của thư viện.");
                    System.out.println("\u26A0\uFE0F Số sách đang nợ ở nhà: " + oldBooksCount + " | Đã quét trong " +
                            "phiếu này: " + cart.size() + " | Cộng: " + (oldBooksCount+cart.size()) + "cuốn.");
                    break;
                }

                // 4. Nhập sách vật lý
                LoanDetail detail = inputSingleLoanDetail(cart);

                // 5. Kiểm tra trường hợp Thủ thư chốt sớm (Enter): bước 1 của hàm inputSingleLoanDetail()
                if (detail == null) {
                    break;
                }

                // 6. Sách hợp lệ
                cart.add(detail);

                // 7. Tính hạn mức sách thực tế còn lại của Đọc giả
                int remainingSlipQuota = MAX_BOOKS_PER_SLIP - cart.size();  // Hạn mức còn lại trong 1 Phiếu
                int remainingTotalQuota = MAX_BOOKS_LIMIT - (oldBooksCount + cart.size()); // Tổng hạn mức còn lại
                int actualQuota = Math.min(remainingSlipQuota,remainingTotalQuota); // Hạn mức còn lại trong Phiếu
                System.out.println("\u2705 Thêm sách vào phiếu thành công. Bạn có thể chọn thêm "
                        + actualQuota + " quyển nữa.");
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
        if(cart.size() == 0){
            System.out.println("\u274C Bạn đã không chọn được sách nào!");
            return null;
        }else {
            return cart;
        }
    }

    // 10. HÀM IN PHIẾU MƯỢN ĐỂ XEM TRƯỚC
    private static void previewLoanSlip(String readerId, ArrayList<LoanDetail> details, double deposit,
                                        double totalRentalFee) throws Exception{
        System.out.println("TÓM TẮT THÔNG TIN PHIẾU MƯỢN SÁCH");
        System.out.println("-------------------");

        // 1. Tìm và in thông tin Reader
        Reader reader = ReaderService.findReaderById(readerId);
        if (reader != null){
            System.out.println("Họ tên người mượn sách: " + reader.getSurname() + " " + reader.getGivenName()
                    + "| ID: " + readerId.toUpperCase());
        }

        // 2. In thông tin tiền đặt cọc và tiền thuê sách theo chuẩn Việt Nam
        String txtRental = formatVND(totalRentalFee);
        String txtDeposit = formatVND(deposit);
        System.out.println("Tổng tiền thuê sách: " + txtRental + " | Số tiền đặt cọc: " + txtDeposit);

        // 3. In danh sách Sách mượn
        System.out.println(String.format("%-8s | %-17s | %-25s | %-5s", "STT", "ISBN","TÊN SÁCH","TẬP"));
        int count = 0;
        for (LoanDetail detail: details){
            Book book = BookService.findBook(detail.getBookInstanceId());
            if(book != null){
                count ++;
                System.out.println(String.format("%-8s | %-17s | %-25s | %-5s", count, book.getIsbn(),
                        truncate(book.getTitle(),25), book.getVolume()));
            }
        }
    }


    // 11. HÀM KHỞI TẠO PHIẾU MƯỢN SÁCH
    public static void createLoanSlip() throws Exception{
        while (true){
            try{
                // 1. Tạo tự động ID cho Phiếu mượn sách
                String id = autoCreateLoanSlipId();
                // Kiểm tra hàm sinh mã tự động bị lỗi
                if(id != null && id.contains("FALLBACK")){
                    System.out.println("\u274C Không thể lập phiếu do sự cố hệ thống.");
                    System.out.println("\u26A0\uFE0F Vui lòng ghi lại mã sự cố này và báo Admin xử lý: " + id);
                    return;
                }

                // 2. Nhập mã thẻ đọc giả
                String readerId = inputReaderId("Nhập thẻ đọc giả (Exit để THOÁT NHANH): ", false);
                if(readerId.equals("EXIT")){
                    return;
                }

                // 3. Kiểm tra Đọc giả còn phiếu mượn chưa trả
                if(isReaderHasOverdueLoan(readerId)){
                    System.out.println("\uD83D\uDED1 TỪ CHỐI TẠO PHIẾU! Độc giả hiện đang có phiếu mượn SÁCH " +
                            "QUÁ HẠN chưa trả.");
                    System.out.println("\u26A0\uFE0F Vui lòng yêu cầu độc giả hoàn trả sách cũ trước khi " +
                            "thực hiện mượn mới.");
                    continue;   // Quay lên nhập thẻ độc giả khác
                }

                // 4. Kiểm tra hạn mức tối đa của Đọc giả
                int oldBooksCount = LibraryReportService.getReaderBorrowingCount(readerId);
                if(oldBooksCount >= MAX_BOOKS_LIMIT){
                    System.out.println("\uD83D\uDED1 TỪ CHỐI TẠO PHIẾU! Độc giả đã mượn đủ hạn mức tối đa "
                            + MAX_BOOKS_LIMIT + " cuốn sách trong hệ thống.");
                    continue;   // Quay lên nhập thẻ độc giả khác
                }

                // 5. Tạo giỏ hàng sách vật lý: thông qua nhập ISBN
                ArrayList<LoanDetail> details = buildLoanDetailsCart(oldBooksCount);

                // 6. Kiểm tra giỏ hàng trống (Thủ thư HỦY nhập) --> thoát tạo phiếu mượn
                if(details == null){
                    return;
                }

                // 7. Nhập tiền đặt cọc và tính tiền thuê sách
                double deposit = inputDeposit("Nhập số tiền đặt cọc: ",details);
                double totalRentalFee = calculateFinancials(details)[TOTAL_RENTAL_FEE_INDEX];

                // 8. Xuất xem trước thông tin phiếu mượn
                previewLoanSlip(readerId,details,deposit,totalRentalFee);

                // 9. Hỏi đọc giả chốt phiếu mượn
                if (askYesNo("Hãy xác lập Phiếu mượn sách và thu tiền? (Y/N): ") == 1){
                    // 10. Cập nhật giảm số lượng sách tồn kho: Trừ Sách vật lý dựa trên BookInstanceId
                    for (LoanDetail detail: details){
                        // 10.1 Lấy mã sách vật lý cụ thể đã chốt trong Giỏ hàng
                        String targetInstanceId = detail.getBookInstanceId();

                        // 10.2 Cắt tách chuỗi thành mà đầu sách: bookId
                        String bookId = "";
                        if(targetInstanceId != null && targetInstanceId.contains("-")){
                            bookId = targetInstanceId.split("-")[0];
                        }else {
                            // CHỦ ĐỘNG NÉM LỖI ĐỂ HỦY TIẾN TRÌNH [🛡️]
                            throw new Exception("Mã sách vật lý không đúng định dạng gạch ngang!");
                        }

                        // 10.3 Tìm đầu sách và giảm trừ kho đầu sách
                        Book book = BookService.findBook(bookId);
                        if(book != null){
                            book.setAvailableQuantity(book.getAvailableQuantity()-1);
                        }

                        // 10.4 Khóa ngầm sách vật lý
                        BookInstance inst = BookInstanceService.findBookInstance(targetInstanceId,
                                BookInstanceService.SEARCH_BY_ID);
                        if(inst != null){
                            inst.setStatus(BookInstanceService.RENTED);
                        }else {
                            // CHỦ ĐỘNG NÉM LỖI ĐỂ HỦY TIẾN TRÌNH [🛡️]
                            throw new Exception("Không tìm thấy cuốn sách có mã vật lý: " + targetInstanceId +
                                    " trong kho!");
                        }
                    }

                    // 11. Lấy Id của người lập phiếu
                    int createdByUserId = UserService.currentUserId;

                    // 12. Tạo phiếu mượn sách
                    LoanSlip newLoanSlip = new LoanSlip(
                            id,
                            readerId,
                            createdByUserId,
                            details,
                            deposit,
                            totalRentalFee
                    );

                    // 13. Thêm Phiếu mượn sách vào Danh sách
                    addLoanList(newLoanSlip);

                    // 14. Lưu danh sách phiếu mượn ra file
                    saveListToFile(loanList,"data/loanSlips.dat");      // Save to file

                    // 15. Lưu đầu Sách (đã cập nhật Kho sách) ra file
                    BookService.saveListToFile(BookService.getBookList(),"data/books.dat"); // Save to file

                    // 16. Lưu Sách vật lý (đã cập nhật trên kệ thư viện) ra file
                    BookInstanceService.saveBookInstanceListToFile();       // Save to file

                    // 17. Hiển thị thông báo
                    System.out.println("Đã tạo Phiếu mượn sách thành công.");

                }else {     // askYesNo("Bạn có muốn xác lập Phiếu mượn sách và thu tiền (Y/N): ") == 0
                    System.out.println("Đã HỦY giao dịch.");
                }

                // 18. Hõi có tiếp tục tạo mới Phiếu mượn sách khác không
                if (askYesNo("Bạn có muốn tạo mới Phiếu mượn sách khác không? (Y/N): ") == 0) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("\u274C Lỗi " + e.getMessage());
            }
        }
    }


    // 12. HÀM HOÀN TÁC KHO SÁCH VÀ ĐỒNG BỘ DỮ LIỆU XUỐNG Ổ CỨNG
    private static void rollbackInventoryAndSync(LoanSlip loan) throws Exception{
        try{
            // 1. Kiểm tra Phiếu rỗng hoặc Phiếu không có Sách vật lý
            if(loan == null || loan.getDetails() == null){
                return;
            }

            // 2. Duyệt Giỏ hàng
            for(LoanDetail detail: loan.getDetails()){
                // 2.1. Kiểm tra phần tử Sách nào null
                if(detail == null){
                    continue;
                }
                String targetInstanceId = detail.getBookInstanceId();

                // 2.2. Kiểm tra mã vật lý bị null hoặc KHÔNG chứa ký tự "-"
                if(targetInstanceId == null || !targetInstanceId.contains("-")){
                    continue;
                }

                // 2.3. Điều kiện trả lại Giỏ hàng: Hợp lệ
                // 2.3.1. Các bước thực hiện hoàn sách lại kho
                String bookId = targetInstanceId.split("-")[0];     // Cắt chuỗi
                Book book = BookService.findBook(bookId);                 // Tìm đầu sách
                if(book != null){
                    book.setAvailableQuantity(book.getAvailableQuantity() +1);  // Hoàn kho đầu sách
                }
                BookInstance inst = BookInstanceService.findBookInstance(targetInstanceId,
                        BookInstanceService.SEARCH_BY_ID);                  // Tìm sách vật lý
                if(inst != null){                                           // Mở khóa: Đưa sách lên kệ
                    inst.setStatus(BookInstanceService.AVAILABLE);
                }
            }

            // 3. Lưu loạt 3 file: đồng bộ với RAM
            saveListToFile(loanList,"data/loanSlips.dat");      // Lưu danh sách phiếu mượn
            BookService.saveListToFile(BookService.getBookList(),"data/books.dat"); // Lưu DS đầu sách
            BookInstanceService.saveBookInstanceListToFile();       // Lưu DS Sách vật lý
        } catch (Exception e) {
            System.out.println("\u274C Lỗi " + e.getMessage());
        }
    }



    // 13. HÀM DÙNG ĐỂ BLOCK CÁC PHIẾU MƯỢN BỊ LỖI: DÀNH CHO ADMIN
    public static void blockLoanSlip(String fallbackId) throws Exception{
        try{
            // 1. Kiểm tra chuỗi rỗng
            if(fallbackId == null || fallbackId.isEmpty()){
                System.out.println("\u274C Lỗi không thể Block mã phiếu rỗng.");
                return;
            }

            // 2. Chuẩn hóa chuỗi nhập
            String cleanFallbackId = fallbackId.trim().toUpperCase();

            // 3. Tìm phiếu bị lỗi
            for (LoanSlip currentLoan:loanList){
                // 3.1. Kiểm tra phiếu rỗng, Id của phiếu rỗng, phiếu không tồn tại
                if(currentLoan == null || currentLoan.getId() == null ||
                        !currentLoan.getId().toUpperCase().equals(cleanFallbackId)){
                    continue;
                }

                // 3.2. Hoàn (ngầm) kho sách và đồng bộ dữ liệu xuống ổ cứng
                rollbackInventoryAndSync(currentLoan);

                // 3.3. Block phiếu bị lỗi
                currentLoan.setBlock(true);
                System.out.println("\u2705 Block phiếu " + cleanFallbackId + " thành công.");
                return;
            }
            System.out.println("\u274C Lỗi không tìm thấy mã phiếu " + cleanFallbackId);
        } catch (Exception e) {
            System.out.println("\u274C Lỗi " + e.getMessage());
        }
    }


    // 14. HÀM TÌM KIẾM PHIẾU MƯỢN ĐA NĂNG
    public static ArrayList<LoanSlip> findLoanSlip(String keyword, int searchType) throws Exception{
        // 1. Kiểm tra chuỗi rỗng hay null
        if(keyword == null || keyword.trim().isEmpty()){
            return null;
        }

        // 2. Chuẩn hóa chuỗi nhập
        String cleanKeyword = keyword.trim().toUpperCase();

        // 3. Khởi tạo danh sách kết quả tạm
        ArrayList<LoanSlip> result = new ArrayList<>();

        // 4. Phân loại tìm kiếm theo chế độ SearchType, sau đó duyệt vòng for
        switch (searchType){
            case SEARCH_BY_LOAN_ID:{
                // 4.1. Tìm theo loanSlipId: làm returnLoan
                for(LoanSlip currentLoan: loanList){
                    // 4.1.1. Kiểm tra phiếu rỗng, Id của phiếu rỗng
                    if(currentLoan == null || currentLoan.getId() ==null){
                        continue;
                    }

                    // 4.1.2. Thêm vào danh sách kết quả
                    if(currentLoan.getId().toUpperCase().equals(cleanKeyword)){
                        result.add(currentLoan);
                        break;
                    }
                }
                break;
            }
            case SEARCH_BY_READER_ID_OR_NAME:{
                // 4.2. Tìm theo readerId: thống kê, báo cáo
                for(LoanSlip currentLoan: loanList){
                    // 4.2.1. Kiểm tra phiếu rỗng, readerId rỗng
                    if(currentLoan == null || currentLoan.getReaderId() ==null){
                        continue;
                    }

                    // 4.2.2. Thêm vào danh sách kết quả
                    if(currentLoan.getReaderId().toUpperCase().contains(cleanKeyword)){
                        result.add(currentLoan);
                    }
                }
                break;
            }
            case SEARCH_BY_USER_ID_OR_USERNAME:{
                // 4.3. Tìm theo userId: thống kê, báo cáo
                // 4.3.1. Khai báo biến mã UserId tìm thấy
                int targetUserId = -1;      // Mặc định không tìm được UserId

                // 4.3.2. KỊCH BẢN A: Admin nhập chuỗi thuần số -> Tìm theo UserId
                if(cleanKeyword.matches("\\d+")){
                    targetUserId = Integer.parseInt(cleanKeyword);
                }else {
                    // 4.3.2. KỊCH BẢN B: Admin nhập chuỗi bao gồm chữ + số -> Tìm theo username
                    User targetUser = UserService.findUserByUsernameOrId(cleanKeyword);
                    if(targetUser != null){
                        targetUserId = targetUser.getId();
                    }
                }

                // 4.3.3. Chốt chặn tối cao: nếu không tìm được userId
                if(targetUserId == -1){
                    break;
                }

                // 4.3.4. Duyệt vòng for đề tìm User Lập phiếu và User Return phiếu
                for(LoanSlip currentLoan: loanList){
                    // 4.3.4.1. Kiểm tra phiếu rỗng
                    if(currentLoan == null){
                        continue;
                    }

                    // 4.3.4.2. Tìm thấy User Lập phiếu và User Return phiếu
                    if(currentLoan.getCreatedByUserId() == targetUserId ||
                            (currentLoan.getProcessedByUserId() != null && currentLoan.getProcessedByUserId() == targetUserId)){
                        result.add(currentLoan);        // Thêm vào danh sách
                    }
                }
                break;
            }
            case SEARCH_BY_BOOK_ID_OR_TITLE:{
                // 4.4. Tìm theo bookId, title: thống kê, báo cáo
                // Bước 1: Khai báo tập hợp mã đầu Sách sạch
                ArrayList<String> targetBookIds = new ArrayList<>();

                // Bước 2: Quét tìm đầu Sách
                // Duyệt qua danh sách Sách để tìm sách theo ID và Title
                for(Book currentBook: BookService.getBookList()){
                    if(currentBook.getId().toUpperCase().contains(cleanKeyword) ||
                            currentBook.getTitle().toUpperCase().contains(cleanKeyword)){
                        // Thêm vào danh sách targetBookId
                        targetBookIds.add(currentBook.getId().toUpperCase());
                    }
                }

                // Bước 3: Chốt chặn bảo vệ tối cao (Guard Clause) -> Nếu không tìm được đầu Sách
                if(targetBookIds.isEmpty()){
                    break;
                }

                // Bước 4: Vòng lặp quét phiếu mượn (Vòng lặp 2 tầng dùng Guard Clause chặn rác)
                for(LoanSlip currentLoan: loanList){
                    // B4.1. Kiểm tra danh sách phiếu rõng hoặc có nhưng chi tiết rỗng
                    if(currentLoan == null || currentLoan.getDetails() == null){
                        continue;
                    }

                    // B4.2. Duyệt vòng lập trong để quét Gỏi hảng: targetBookIds
                    for(LoanDetail detail: currentLoan.getDetails()){
                        // B4.2.1 Kiểm tra Sách vật lý không có
                        if(detail == null || detail.getBookInstanceId() == null){
                            continue;
                        }

                        // B4.2.2. Bóc tách mã Sách vật lý để so sánh
                        String instId = detail.getBookInstanceId().toUpperCase();

                        // B4.2.3. Cắt chuỗi lấy mã đầu sách
                        String currentBookId = instId.contains("-") ? instId.split("-")[0] : instId;

                        // B4.2.4. So sánh thông minh
                        if(targetBookIds.contains(currentBookId) || instId.contains(cleanKeyword)){
                            // Thêm phiếu vào Kết quả
                            result.add(currentLoan);
                            break;      // Để phòng trường hợp một phiếu mượn có 2 cuốn sách cùng loại
                        }
                    }
                }
                break;
            }
        }
        return result;
    }


    // 15. HÀM SOÁT XÉT TRẢ SÁCH
    public static void processReturnLoanSlip(String prompt) throws Exception{
        try{
            System.out.println(prompt);         // Enter để thoát
            String inputLoanId = sc.nextLine().trim();

            // 1. Kiểm tra chuỗi nhập
            if(inputLoanId.isEmpty()){
                System.out.println("\uD83D\uDED1 Dừng tìm kiếm phiếu mượn sách.");  // ký tự STOP: \uD83D\uDED1
                return;
            }

            // 2. Chuẩn hóa dữ liệu nhập
            String cleanInputLoanId = inputLoanId.toUpperCase();

            // 3. Tìm kiếm phiếu mượn từ danh sách loanList
            ArrayList<LoanSlip> searchResult = findLoanSlip(cleanInputLoanId, SEARCH_BY_LOAN_ID);

            // 4. Kiểm tra kết quả tìm kiếm: rỗng hoặc trống
            if(searchResult == null || searchResult.isEmpty()){
                System.out.println("\u274C Phiếu mượn " + cleanInputLoanId + " không tồn tại");
                return;
            }

            // 5. Lây phiếu mượn tìm được
            LoanSlip loan = searchResult.get(UNIQUE_RESULT_INDEX);    // Tìm được phiếu mượn theo ID

            // 6. Lớp bảo vệ 2: Kiểm soát phiếu bị Block (True) hoặc Đã hoàn thành thủ tục trả (1)
            if(loan.isBlock() || loan.getStatus() == LoanSlip.COMPLETED){
                System.out.println("\uD83D\uDED1 Phiếu mượn đã hoàn thành hoặc đang bị khóa, không thể xử lý trả sách!");
                return;
            }

            // 7. Phiếu mượn hợp lệ --> Tính tiền phạt trễ hạn và khởi tạo biến Tính TỔNG tiền phạt mất sách
            double overdueFee = calculateOverduePenalty(loan,LocalDate.now());  // Tiền phạt trễ hạn
            double totalLostFee = 0.0;              // Tiền phạt mất/hỏng sách

            // 8. Kiểm tra Giỏ hàng null hoặc trống -> Đóng băng Khóa phiếu lập tức [🛡️]
            if(loan.getDetails() == null || loan.getDetails().isEmpty()){
                System.out.println("\u26A0 Cảnh báo: Phiếu mượn dị thường (Trống giỏ hàng)! " +
                        "Hệ thống tự động khóa phiếu.");
                loan.setBlock(true);
                saveListToFile(loanList,"data/loanSlips.dat");      // Save to file
                return;     // Thoát luồng an toàn, bảo vệ menu chính
            }

            // 9. Giỏ hàng hợp lệ: Duyệt chi tiết Giỏ hàng để tính tiền phạt
            for(LoanDetail detail: loan.getDetails()){
                // 10. Kiểm tra từng hạn mục trong Giỏ hàng: rỗng hoặc booKInstanceId trống
                if(detail == null || detail.getBookInstanceId() == null){
                    continue;
                }

                // 11. Từng hạn mục trong Giỏ hàng hợp lệ --> lấy mã sách vật lý
                String instId = detail.getBookInstanceId();

                // 12. Tìm OOP BookInstane để xử lý ở bước ....
                BookInstance currentInst = BookInstanceService.findBookInstance(instId,BookInstanceService.SEARCH_BY_ID);

                // 13. Kiểm tra sách vật lý = null.
                if(currentInst == null){
                    continue;
                }

                // 14. Thủ thư hỏi Đọc giả: Sách vật lý này có bị mất/hỏng nặng không -> Để tính tiền phạt mất sách
                int isLost = askYesNo("Cuốn sách có mã " + instId + ", Đọc giả có làm mất hoặc hư hỏng nặng " +
                        "không? (Y/N): ");

                // 15.1. Phân nhánh xử lý: KICH BẢN A -> tình trạng sách OK, không tính tiền phạt mất sách
                if(isLost == BOOK_CONDITION_GOOD){
                    returnBookToInventory(instId);
                    currentInst.setStatus(BookInstanceService.AVAILABLE);
                }else {
                    // 15.2. Phân nhánh xử lý: KICH BẢN B -> Sách bị mất/hỏng --> tình tiền phạt mất sách
                    totalLostFee += calculateLostBookPenalty(instId);           // Tính tiền phạt mất/hỏng sách
                    currentInst.setStatus(BookInstanceService.LOST_OR_DAMAGED);  // Đánh dấu sách vật lý: mất/hỏng
                }
            }

            // 16. ĐÓNG HỒ SƠ PHIẾU TRẢ SÁCH
            // 16.1. Cập nhật thời gian thực hiện hoàn phiếu
            loan.setActualReturnDate(LocalDate.now());

            // 16.2. Cập nhật và truy xuất kết quả tài chính:
            double totalRentalFee = loan.getTotalRentalFee();                   // Tiền thuê sách theo mặc định
            loan.setOverduePenaltyFee(overdueFee);                              // Tiền phạt trễ hạn
            loan.setTotalLostBookFee(totalLostFee);                             // Tiền phạt mất sách
            double finalFine = loan.getOverduePenaltyFee() + loan.getTotalLostBookFee();    // Tổng tiền phạt
            double totalFee = totalRentalFee + finalFine;                       // Tổng phí mượn sách
            double netBalance = totalFee - loan.getDeposit();                   // Số tiền còn lại phải thanh toán

            // 16.3 Cập nhật nhân sự thực hiện
            loan.setProcessedByUserId(UserService.currentUserId);

            // 16.4 Tìm tên Đọc giả
            String readerName = "";
            Reader currentReader = ReaderService.findReaderById(loan.getReaderId());
            if(currentReader != null){
                readerName = currentReader.getSurname() + " " + currentReader.getGivenName();
            }

            // 16.5. Cập nhật trạng thái đóng phiếu
            loan.setStatus(LoanSlip.COMPLETED);

            // 17. In hóa đơn và hoàn tất (UX đỉnh cao tại quềy)
            printFinancialReceipt(loan, readerName, totalRentalFee, totalFee,netBalance);
            // 18. Lưu đồng bộ 3 file xuống ổ cứng
            // 18.1. Lưu danh sách phiếu mượn ra file
            saveListToFile(loanList,"data/loanSlips.dat");      // Save to file

            // 18.2. Lưu đầu Sách (đã cập nhật Kho sách) ra file
            BookService.saveListToFile(BookService.getBookList(),"data/books.dat"); // Save to file

            // 18.3. Lưu Sách vật lý (đã cập nhật trên kệ thư viện) ra file
            BookInstanceService.saveBookInstanceListToFile();       // Save to file

        } catch (Exception e) {
            System.out.println("\u274C Lỗi " + e.getMessage());
        }
    }


    // 16. HÀM HOÀN TRẢ SÁCH VẬT LÝ VÀO KHO
    private static void returnBookToInventory(String bookInstanceId) throws Exception{
        // 1. Kiểm tra mã sách vật lý null hoặc empplty
        if(bookInstanceId == null || bookInstanceId.isEmpty()){
            return;
        }

        // 2. Tách mã sách vật lý thành mã đầu sách --> tìm đầu sách
        String bookId = bookInstanceId.split("-")[0];
        Book book = BookService.findBook(bookId);

        // 3. Trả đầu sách vào kho
        if(book != null) {
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        }
    }


    // 17. HÀM XUẤT BIÊN LAI TÀI CHÍNH KHI QUYẾT TOÁN PHIẾU MƯỢN
    private static void printFinancialReceipt(LoanSlip loan, String readerName, double totalRentalFee,
                                              double totalFee, double netBalance) throws Exception{
        System.out.println("---------- BIÊN LAI THANH TOÁN PHIẾU MƯỢN SÁCH ---------");
        System.out.println(String.format("\u2705 %-35s %25s", "Họ tên đọc giả:", readerName));
        System.out.println(String.format("\u2705 %-35s %25s", "Mã phiếu mượn: ", loan.getId()));
        System.out.println(String.format("\u2705 %-35s %-25s", "Tiền thuê sách theo định mức:",
                formatVND(totalRentalFee)));
        System.out.println(String.format("\u2705 %-35s %-25s", "Tiền phạt trễ hạn:",
                formatVND(loan.getOverduePenaltyFee())));
        System.out.println(String.format("\u2705 %-35s %-25s", "Tiền phạt mất/hỏng sách:",
                formatVND(loan.getTotalLostBookFee())));
        System.out.println(String.format("\u2705 %-35s %-25s", "Tổng chi phí thuê sách:",
                formatVND(totalFee)));
        System.out.println(String.format("\u2705 %-35s %-25s", "Số tiền đặt cọc:",
                formatVND(loan.getDeposit())));
        if(netBalance >= 0){
            System.out.println(String.format("\u2705 %-35s %-25s", "Số tiền còn lại phải thanh toán:",
                    formatVND(netBalance)));
        } else {
            System.out.println(String.format("\u2705 %-35s %-25s", "Số tiền thừa trả lại Đọc giả:",
                    formatVND(-netBalance)));
        }
        System.out.println("Xin cám ơn và rất mong tiếp tục được phục vụ Quý khách.");
    }


    // 18. HÀM KIỂM TRA ĐỌC GIẢ CÓ PHIẾU MƯỢN SÁCH BỊ QUÁ HẠN TRẢ.
    public static boolean isReaderHasOverdueLoan(String readerId) throws Exception{
        // 1. Kiểm tra readerId bị rỗng hoặc emplty
        if(readerId == null || readerId.isEmpty()){
            return false;
        }

        // 2. Khởi tạo mốc thời gian đối chiếu thực tế
        LocalDate today = LocalDate.now();

        for(LoanSlip loan: loanList){
            // 3. Kiểm tra danh sách phiếu mượn rỗng, có phiếu mượn nhưng readerId rỗng hoặc ngày trả rỗng
            if(loan == null || loan.getReaderId() == null || loan.getExpectedReturnDate() == null){
                continue;
            }

            // 4. Khởi tạo 3 biến xác thực điều kiện
            boolean isTargetReader = false, isNotReturned = false, isOverdue = false;

            // 5. Bộ lọc điều kiện Phiếu mượn vi phạm: khi đáp ứng ĐỦ 3 điều kiện sau:
            // 5,1, Phiếu mượn Đúng của Đọc giả này
            if(loan.getReaderId().toUpperCase().equals(readerId.toUpperCase())){
                isTargetReader = true;
            }
            // 5.2. Phiếu mượn chưa được trả sách
            if(loan.getStatus() == LoanSlip.BORROWING){
                isNotReturned = true;
            }

            // 5.3. Ngày trả nằm trước ngày hôm nay, tức là trả quá hạn
            if(loan.getExpectedReturnDate().isBefore(today)){
                isOverdue = true;
            }
            if((isTargetReader && isNotReturned && isOverdue)){
                return true;
            }
        }
        return false;
    }


    // 19. HÀM GIA HẠN PHIẾU MƯỢN SÁCH
    public static boolean renewLoanSlip (String loanSlipId) throws Exception{
        // 1. Kiểm tra input rỗng hoặc null
        if(loanSlipId == null || loanSlipId.isEmpty()){
            return false;
        }

        // 2. Chuẩn hóa chuỗi nhập
        String cleanLoanSlipId = loanSlipId.trim().toUpperCase();

        // 3. Tìm phiếu mượn trong danh sách
        ArrayList<LoanSlip> searchResult = findLoanSlip(cleanLoanSlipId,SEARCH_BY_LOAN_ID);

        // 4. Kiểm tra phiếu mượn rỗng hoặc null
        if(searchResult == null || searchResult.isEmpty()){
            System.out.println("\u274C Lỗi phiếu mượn không tồn tại.");
            return false;
        }

        // 5. Bốc thực thể loanSlip duy nhất ra khỏi ArrayList<LoanSlip> vừa tìm được
        LoanSlip loan = searchResult.get(UNIQUE_RESULT_INDEX);

        // 6. Kích hoạt 3 trạm gác
        // 6.1. Kiểm tra phiếu bị Block hoặc đã hoàn tất thủ tục trả
        if(loan.isBlock() || loan.getStatus() == LoanSlip.COMPLETED){
            System.out.println("\u274C Lỗi phiếu đã đóng hoặc khóa.");
            return false;
        }

        // 6.2. Kiểm tra phiếu bị quá hạn trả
        if(loan.getExpectedReturnDate() != null && loan.getExpectedReturnDate().isBefore(java.time.LocalDate.now())){
            System.out.println("\u274C Lỗi phiếu quá hạn trả, không thể gia hạn.");
            return false;
        }

        // 6.3. Kiểm tra tần suất số lần gia hạn < hạn mức tối đa: MAX_RENEW_LIMITS
        if(loan.getRenewCount() >= LoanSlip.MAX_RENEW_LIMITS){
            System.out.println("\u274C Lỗi phiếu đã đạt giới hạn số lần gia hạn tối đa " +
                    "(" + LoanSlip.MAX_RENEW_LIMITS + " lần)!");
            return false;
        }

        // 7. Cập nhật gia hạn phiếu
        // 7.1. Cập nhật lại ngày hẹn trả phiếu
        loan.setExpectedReturnDate(loan.getExpectedReturnDate().plusDays(LoanSlip.DEFAULT_RENEW_DAYS));

        // 7.2. Cập nhật lại số lần gia hạn của phiếu
        loan.setRenewCount(loan.getRenewCount() + 1);

        // 8. In thông báo UI tại quầy
        System.out.println("\u2705 Gia hạn phiếu mượn thành công.");
        System.out.println("Tần suất gia hạn phiếu mượn: " + loan.getRenewCount() + "/" + LoanSlip.MAX_RENEW_LIMITS);
        System.out.println("Thời hạn trả sách mới: " + formatLocalDateVN(loan.getExpectedReturnDate()));

        // 9. Lưu file danh sách LoanSlip để đồng bộ RAM và Ổ cứng
        saveListToFile(loanList,"data/loanSlips.dat");      // Save to file

        return true;
    }



}
