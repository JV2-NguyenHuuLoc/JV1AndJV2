package library;

import book.Book;
import book.BookInstance;
import book.BookInstanceService;
import book.BookService;
import loanSlip.LoanSlip;
import loanSlip.LoanSlipService;
import reader.Reader;
import reader.ReaderService;
import user.User;
import user.UserService;
import java.util.ArrayList;
import java.util.Scanner;


public class LibraryManagement {
    // Khai báo biến toàn cục
    public static Scanner sc = new Scanner(System.in);

    // 1. HÀM MENU CHÍNH
    // Hàm xuất Menu chính
    private static int chooseMainMenu() throws Exception{
        System.out.println("============= MENU QUẢN LÝ THƯ VIỆN ==============");
        System.out.println("1. Chức năng quản lý người dùng");
        System.out.println("2. Chức năng quản lý đọc giả");
        System.out.println("3. Chức năng quản lý sách");
        System.out.println("4. Lập phiếu mượn sách");
        System.out.println("5. Lập phiếu trả sách");
        System.out.println("6. Các thống kê cơ bản");
        System.out.println("0. THOÁT");
        int choiceMainMenu=0;
        do{
            try{
                System.out.print("Hãy chọn chức năng (từ 0 - 6): ");
                choiceMainMenu = Integer.parseInt(sc.nextLine());
                if (choiceMainMenu<0 || choiceMainMenu>6){
                    System.out.print("Lỗi nhập dữ liệu < 0 hoặc > 6. ");
                }
            }catch(Exception e){
                System.out.println("Lỗi nhập sai kiểu dữ liệu. " + e.getMessage() + ". ");
            }
        }while(choiceMainMenu<0 || choiceMainMenu>6);
        return choiceMainMenu;
    }


    // 2. CÁC HÀM SUBMENU
    // Hàm xuất SubMenu Quản trị hệ thống Users
    private static int chooseAdminSystemUser() throws Exception{
        System.out.println("---------- Menu Quản trị hệ thống người dùng ----------");
        System.out.println("1. Xem danh sách người dùng");
        System.out.println("2. Tạo người dùng mới");
        System.out.println("3. Cập nhật thông tin người dùng");
        System.out.println("4. Phân quyền người dùng");
        System.out.println("5. Thay đổi mật khẩu");
        System.out.println("6. Đóng băng / Kích hoạt lại tài khoản nhân sự");
        System.out.println("7. Đăng xuất");
        System.out.println("0. TRỞ VỀ MENU CHÍNH");
        int choiceAdminSystemUser = 0;
        do{
            try{
                System.out.print("Hãy chọn chức năng (từ 0 - 7): ");
                choiceAdminSystemUser = Integer.parseInt(sc.nextLine());
                if (choiceAdminSystemUser<0 || choiceAdminSystemUser>7){
                    System.out.print("Lỗi nhập dữ liệu < 0 hoặc > 7. ");
                }
            }catch(Exception e){
                System.out.println("Lỗi nhập sai kiểu dữ liệu. " + e.getMessage() + ". ");
            }
        }while(choiceAdminSystemUser<0 || choiceAdminSystemUser>7);
        return choiceAdminSystemUser;
    }

    // Hàm xuất SubMenu Thông tin người dùng
    private static int chooseInfoUser() throws Exception{
        System.out.println("---------- Menu thông tin tài khoản người dùng ----------");
        System.out.println("1. Cập nhật thông tin tài khoản");
        System.out.println("2. Thay đổi mật khẩu");
        System.out.println("3. Đăng xuất");
        System.out.println("0. TRỞ VỀ MENU CHÍNH");
        int choiceInfoUser = 0;
        do{
            try{
                System.out.print("Hãy chọn chức năng (từ 0 - 3): ");
                choiceInfoUser = Integer.parseInt(sc.nextLine());
                if (choiceInfoUser<0 || choiceInfoUser>3){
                    System.out.print("Lỗi nhập dữ liệu < 0 hoặc > 3. ");
                }
            }catch(Exception e){
                System.out.println("Lỗi nhập sai kiểu dữ liệu. " + e.getMessage() + ". ");
            }
        }while(choiceInfoUser<0 || choiceInfoUser>3);
        return choiceInfoUser;
    }


    // Hàm xuất SubMenu Quản trị hệ thống Độc giả tối cao (Admin/Manager)
    private static int chooseAdminSystemReader() throws Exception {
        System.out.println("---------- Hệ thống Quản trị phân hệ Độc giả (Admin/Manager) ----------");
        System.out.println("1. Xem danh sách Độc giả trong hệ thống");
        System.out.println("2. Lập thẻ Độc giả mới");
        System.out.println("3. Chỉnh sửa hồ sơ thông tin Độc giả");
        System.out.println("4. Tìm kiếm Độc giả theo Căn cước / Điện thoại / Họ tên");
        System.out.println("5. Đóng băng / Mở khóa hiệu lực thẻ Độc giả");
        System.out.println("6. Đăng xuất tài khoản");
        System.out.println("0. TRỞ VỀ MENU CHÍNH");
        int choiceAdminSystemReader = 0;
        do {
            try {
                System.out.print("Hãy chọn chức năng (từ 0 - 6): ");
                choiceAdminSystemReader = Integer.parseInt(sc.nextLine());
                if (choiceAdminSystemReader < 0 || choiceAdminSystemReader > 6) {
                    System.out.print("Lựa chọn không nằm trong phạm vi (0 - 6). Vui lòng chọn lại: ");
                }
            } catch (Exception e) {
                System.out.println("Định dạng nhập vào không hợp lệ. Vui lòng chỉ nhập số! ");
            }
        } while (choiceAdminSystemReader < 0 || choiceAdminSystemReader > 6);
        return choiceAdminSystemReader;
    }


    // Hàm xuất SubMenu Thông tin Độc giả dành cho Nhân viên quầy (Staff/User)
    private static int chooseInfoReader() throws Exception {
        System.out.println("---------- Danh mục Quản lý Thẻ Độc giả (Staff/User) ----------");
        System.out.println("1. Xem danh sách Độc giả trong hệ thống");
        System.out.println("2. Lập thẻ Độc giả mới");
        System.out.println("3. Chỉnh sửa hồ sơ thông tin Độc giả");
        System.out.println("4. Tìm kiếm Độc giả theo Căn cước / Điện thoại / Họ tên");
        System.out.println("5. Đăng xuất tài khoản");
        System.out.println("0. TRỞ VỀ MENU CHÍNH");
        int choiceInfoReader = 0;
        do {
            try {
                System.out.print("Hãy chọn chức năng (từ 0 - 5): ");
                choiceInfoReader = Integer.parseInt(sc.nextLine());
                if (choiceInfoReader < 0 || choiceInfoReader > 5) {
                    System.out.print("Lựa chọn không nằm trong phạm vi (0 - 5). Vui lòng chọn lại: ");
                }
            } catch (Exception e) {
                System.out.println("Định dạng nhập vào không hợp lệ. Vui lòng chỉ nhập số! ");
            }
        } while (choiceInfoReader < 0 || choiceInfoReader > 5);
        return choiceInfoReader;
    }

    // Hàm xuất SubMenu cho Reader
    private static int chooseSubMenuForReader() throws Exception {
        System.out.println("---------- Menu Thông tin thẻ đọc giả ----------");
        System.out.println("1. Tra cứu tìm kiếm sách theo ISBN / Tên / Nhà xuất bản");
        System.out.println("2. Chỉnh sửa hồ sơ thông tin cá nhân");
        System.out.println("3. Thay đổi mật khẩu thẻ thành viên");
        System.out.println("4. Tự gia hạn hiệu lực Thẻ thành viên (Miễn phí 2 năm)");
        System.out.println("0. ĐĂNG XUẤT");
        int choiceSubMenuForReader = 0;
        do {
            try {
                System.out.print("Hãy chọn chức năng (từ 0 - 4): ");
                choiceSubMenuForReader = Integer.parseInt(sc.nextLine());
                if (choiceSubMenuForReader < 0 || choiceSubMenuForReader > 4) {
                    System.out.print("Lỗi nhập dữ liệu < 0 hoặc > 4. Vui lòng chọn lại: ");
                }
            } catch (Exception e) {
                System.out.println("Lỗi nhập sai kiểu dữ liệu. " + e.getMessage() + ". ");
            }
        } while (choiceSubMenuForReader < 0 || choiceSubMenuForReader > 4);
        return choiceSubMenuForReader;
    }


    // Hàm xuất SubMenu Quản trị hệ thống sách
    private static int chooseAdminSystemBook() throws Exception{
        System.out.println("---------- Menu Quản trị hệ thống sách  ----------");
        System.out.println("1. Xem danh sách Sách trong thư viện");
        System.out.println("2. Thêm sách");
        System.out.println("3. Chỉnh sửa thông tin sách");
        System.out.println("4. Tìm kiếm sách theo ID / ISBN / Tên sách");
        System.out.println("5. Xóa sách");
        System.out.println("6. Đăng xuất");
        System.out.println("0. TRỞ VỀ MENU CHÍNH");
        int choiceAdminSystemBook = 0;
        do{
            try{
                System.out.print("Hãy chọn chức năng (từ 0 - 6): ");
                choiceAdminSystemBook = Integer.parseInt(sc.nextLine());
                if (choiceAdminSystemBook<0 || choiceAdminSystemBook>6){
                    System.out.print("Lỗi nhập dữ liệu < 0 hoặc > 6. ");
                }
            }catch(Exception e){
                System.out.println("Lỗi nhập sai kiểu dữ liệu. " + e.getMessage() + ". ");
            }
        }while(choiceAdminSystemBook<0 || choiceAdminSystemBook>6);
        return choiceAdminSystemBook;
    }

    // Hàm xuất SubMenu Thông tin sách
    private static int chooseInfoBook() throws Exception{
        System.out.println("---------- Menu Thông tin sách  ----------");
        System.out.println("1. Xem danh sách Sách trong thư viện");
        System.out.println("2. Thêm sách");
        System.out.println("3. Chỉnh sửa thông tin sách");
        System.out.println("4. Tìm kiếm sách theo ISBN / tên");
        System.out.println("5. Đăng xuất");
        System.out.println("0. TRỞ VỀ MENU CHÍNH");
        int choiceInfoBook = 0;
        do{
            try{
                System.out.print("Hãy chọn chức năng (từ 0 - 5): ");
                choiceInfoBook = Integer.parseInt(sc.nextLine());
                if (choiceInfoBook<0 || choiceInfoBook>5){
                    System.out.print("Lỗi nhập dữ liệu < 0 hoặc > 5. ");
                }
            }catch(Exception e){
                System.out.println("Lỗi nhập sai kiểu dữ liệu. " + e.getMessage() + ". ");
            }
        }while(choiceInfoBook<0 || choiceInfoBook>5);
        return choiceInfoBook;
    }


    // 3. CÁC HÀM MENU THỐNG KÊ, BÁO CÁO

    // Hàm xuất SubMenu Thống kê và Báo cáo cấp quản trị
    private static int chooseStatistics() throws Exception {
        System.out.println("---------- Hệ thống Báo cáo Thống kê tổng hợp (Admin/Manager) ----------");
        System.out.println("1. Xem báo cáo tổng kho và tình hình sử dụng các đầu sách");
        System.out.println("2. Xem danh sách chi tiết các cuốn sách đang cho mượn");
        System.out.println("3. Xem danh sách sách bị mất hoặc hư hỏng cần đền bù");
        System.out.println("4. Xem tình hình mượn trả tổng hợp của tất cả Độc giả");
        System.out.println("5. Tra cứu lịch sử mượn trả cá biệt của một Độc giả");
        System.out.println("6. Xem thống kê số lượng phiếu lập của từng Nhân viên");
        System.out.println("7. Tổng kết doanh thu tiền thuê, tiền phạt và nợ tồn đọng");
        System.out.println("8. Quét kiểm tra danh sách thẻ sắp hết hạn và thẻ bị khóa");
        System.out.println("0. TRỞ VỀ MENU CHÍNH");
        int choiceStatistics = 0;
        do {
            try {
                System.out.print("Mời bạn chọn hạng mục báo cáo (từ 0 - 8): ");
                choiceStatistics = Integer.parseInt(sc.nextLine());
                if (choiceStatistics < 0 || choiceStatistics > 8) {
                    System.out.print("Lựa chọn không nằm trong phạm vi (0 - 8). Vui lòng chọn lại: ");
                }
            } catch (Exception e) {
                System.out.println("Định dạng nhập vào không hợp lệ. Vui lòng chỉ nhập số! ");
            }
        } while (choiceStatistics < 0 || choiceStatistics > 8);
        return choiceStatistics;
    }


    // Hàm xuất SubMenu Thống kê Cơ bản dành cho Nhân viên quầy
    private static int chooseStatisticsBasic() throws Exception {
        System.out.println("---------- Danh mục Thống kê & Hỗ trợ Nghiệp vụ quầy (Staff/User) ----------");
        System.out.println("1. Xem nhanh danh sách sách đang cho mượn ngoài quầy");
        System.out.println("2. Xem nhanh danh sách thẻ độc giả sắp hết hiệu lực hoặc bị khóa");
        System.out.println("3. Tra cứu lịch sử mượn trả chi tiết của một Độc giả cụ thể");
        System.out.println("4. Xem báo cáo tồn kho và tình hình sử dụng của các đầu sách");
        System.out.println("0. TRỞ VỀ MENU CHÍNH");
        int choiceStatisticsBasic = 0;
        do {
            try {
                System.out.print("Mời bạn chọn hạng mục hỗ trợ tác nghiệp (từ 0 - 4): ");
                choiceStatisticsBasic = Integer.parseInt(sc.nextLine());
                if (choiceStatisticsBasic < 0 || choiceStatisticsBasic > 4) {
                    System.out.print("Lựa chọn không nằm trong phạm vi (0 - 4). Vui lòng chọn lại: ");
                }
            } catch (Exception e) {
                System.out.println("Định dạng nhập vào không hợp lệ. Vui lòng chỉ nhập số! ");
            }
        } while (choiceStatisticsBasic < 0 || choiceStatisticsBasic > 4);
        return choiceStatisticsBasic;
    }



    // 4. CÁC HÀM ĐIỀU PHỐI CHO NHÂN VIÊN

    // Hàm điều phối chính cho Nhân viên
    private static void handleEmployeeFlow(){
        // Đảm bảo getCurrentUser không null trước khi lấy Role
        if (UserService.getCurrentUser() == null){
            return;
        }

        // Lấy Role của currentUser
        int role = UserService.getCurrentUser().getRole();
        boolean isRunning = true;

        // Thực thi menu chính
        while (isRunning){
            // KIỂM TRA: Nếu Submenu vừa làm sạch currentUserId ĐĂNG XUẤT --> Menu chính cũng phải dừng NGAY LẬP TỨC
            if(UserService.currentUserId == 0){
                break;
            }

            try{
                System.out.println();
                int choice = chooseMainMenu(); // Menu: 1.User; 2.Reader; 3.Book; 4.LoanSlip; 5.ReturnSlip; 6.Statistics
                switch (choice){
                    case 1: {          // Chức năng quản lý User
                        handleAdminUserWork(role);
                        if(UserService.currentUserId == 0){
                            continue;
                        }
                        break;
                    }
                    case 2:{            // Chức năng quản lý Reader
                        handleAdminReaderWork(role);
                        if(UserService.currentUserId == 0){
                            continue;
                        }
                        break;
                    }
                    case 3:{            // Chức năng quản lý Book
                        handleAdminBookWork(role);
                        break;
                    }
                    case 4:{            // Lập phiếu mượn sách
                        handleLoanSlipWork(role);
                        break;
                    }
                    case 5:{            // Lập phiếu trả sách
                        handleReturnSlipWork(role);
                        break;
                    }
                    case 6:{            // Các thống kê cơ bản
                        handleStatisticsWork(role);
                        break;
                    }
                    case 0:{
                        isRunning = false;
                        break;          // Quay lại cổng đăng nhập
                    }
                }
            } catch (Exception e) {
                System.out.println("Có lỗi xãy ra " + e.getMessage());
            }
        }
    }


    // 4. HÀM ĐIỀU PHỐI LẬP PHIẾU MƯỢN SÁCH (MỞ KHÓA CASE 4) [🛡️]
    private static void handleLoanSlipWork(int role) throws Exception {
        System.out.println("\n=== TIẾN TRÌNH LẬP PHIẾU MƯỢN SÁCH TẠI QUẦY ===");
        // Gọi liên thông siêu hàm giao dịch lập phiếu mượn từ lớp dịch vụ
        loanSlip.LoanSlipService.createLoanSlip();
    }

    // 5. HÀM ĐIỀU PHỐI LẬP PHIẾU TRẢ SÁCH (MỞ KHÓA CASE 5) [🛡️]
    private static void handleReturnSlipWork(int role) throws Exception {
        System.out.println("\n=== TIẾN TRÌNH SOÁT XÉT & TẤT TOÁN PHIẾU TRẢ ===");
        // Gọi liên thông siêu hàm tất toán tài chính từ lớp dịch vụ
        loanSlip.LoanSlipService.processReturnLoanSlip("Nhập mã phiếu mượn cần tất toán " +
                "(Gõ ENTER để thoát nhanh): ");
    }


    // 6. HÀM ĐIỀU PHỐI CHỨC NĂNG THỐNG KÊ, BÁO CÁO VÀ KIỂM TOÁN
    private static void handleStatisticsWork(int role) throws Exception {
        boolean isRunning = true;
        while (isRunning && UserService.currentUserId != 0) {

            // KỊCH BẢN A: BỘ LỌC PHÂN QUYỀN CHO NHÂN VIÊN THƯỜNG (ROLE_USER) -> NÂNG CAO TÍNH CHỦ ĐỘNG TÁC NGHIỆP
            if (role == UserService.ROLE_USER) {
                System.out.println();
                int basicChoice = chooseStatisticsBasic(); // Gọi SubMenu cơ bản chuẩn mới (0 - 4)
                switch (basicChoice) {
                    case 1: {
                        System.out.println("\n>>> DANH SÁCH SÁCH ĐANG CHO MƯỢN NGOÀI QUẦY <<<");
                        report.LibraryReportService.reportRentedBooks();
                        break;
                    }
                    case 2: {
                        System.out.println("\n>>> DANH SÁCH THẺ ĐỘC GIẢ SẮP HẾT HẠN / BỊ KHÓA <<<");
                        report.LibraryReportService.reportExpiryAndBlockedReaders();
                        break;
                    }
                    case 3: {
                        System.out.println("\n>>> TRA CỨU LỊCH SỬ MƯỢN TRẢ CHI TIẾT CỦA 1 ĐỘC GIẢ <<<");
                        System.out.print("Nhập mã số thẻ Độc giả cần tra cứu dữ liệu: ");
                        String rId = sc.nextLine().trim();
                        report.LibraryReportService.reportReaderHistoryDetail(rId); // Chỉ hiển thị hỗ trợ tại quầy
                        break;
                    }
                    case 4: {
                        System.out.println("\n>>> BÁO CÁO TỒN KHO VÀ TÌNH HÌNH SỬ DỤNG ĐẦU SÁCH <<<");
                        report.LibraryReportService.reportBookInventory(); // Chỉ hiển thị hỗ trợ kiểm kho tại quầy
                        break;
                    }
                    case 0: {
                        System.out.println("TRỞ VỀ MENU CHÍNH");
                        isRunning = false;
                        break;
                    }
                }
            } else {
                // KỊCH BẢN B: PHÂN QUYỀN CHO ADMIN / MANAGER -> GIỮ NGUYÊN TRỌN GÓI 8 CỔNG EXCEL THƯƠNG MẠI
                System.out.println();
                int advChoice = chooseStatistics(); // Gọi Menu quản trị (0 - 8)
                switch (advChoice) {
                    case 1: {
                        System.out.println("\n>>> XEM BÁO CÁO TỔNG KHO VÀ TÌNH HÌNH SỬ DỤNG ĐẦU SÁCH <<<");
                        report.LibraryReportService.reportBookInventory();
                        report.LibraryReportService.exportBookInventoryToExcel();
                        break;
                    }
                    case 2: {
                        System.out.println("\n>>> XEM CHI TIẾT CÁC CUỐN SÁCH ĐANG CHO MƯỢN <<<");
                        report.LibraryReportService.reportRentedBooks();
                        report.LibraryReportService.exportRentedBooksToExcel();
                        break;
                    }
                    case 3: {
                        System.out.println("\n>>> XEM DANH SÁCH SÁCH BỊ MẤT HOẶC HƯ HỎNG <<<");
                        report.LibraryReportService.reportLostOrDamagedBooks();
                        report.LibraryReportService.exportLostOrDamagedBooksToExcel();
                        break;
                    }
                    case 4: {
                        System.out.println("\n>>> XEM TÌNH HÌNH MƯỢN TRẢ TỔNG HỢP CỦA TẤT CẢ ĐỘC GIẢ <<<");
                        report.LibraryReportService.reportTotalReaders();
                        report.LibraryReportService.exportTotalReadersToExcel();
                        break;
                    }
                    case 5: {
                        System.out.println("\n>>> TRA CỨU LỊCH SỬ MƯỢN TRẢ CHI TIẾT CỦA MỘT ĐỘC GIẢ <<<");
                        System.out.print("Nhập mã số thẻ Độc giả cần trích xuất dữ liệu: ");
                        String rId = sc.nextLine().trim();
                        report.LibraryReportService.reportReaderHistoryDetail(rId);
                        report.LibraryReportService.exportReaderHistoryDetailToExcel(rId);
                        break;
                    }
                    case 6: {
                        System.out.println("\n>>> XEM THỐNG KÊ SỐ LƯỢNG PHIẾU LẬP CỦA TỪNG NHÂN VIÊN <<<");
                        report.LibraryReportService.reportStaffPerformance();
                        report.LibraryReportService.exportStaffPerformanceToExcel();
                        break;
                    }
                    case 7: {
                        System.out.println("\n>>> TỔNG KẾT DOANH THU TIỀN THUÊ, TIỀN PHẠT VÀ NỢ TỒN ĐỌNG <<<");
                        report.LibraryReportService.reportLibraryFinance();
                        report.LibraryReportService.exportLibraryFinanceToExcel();
                        break;
                    }
                    case 8: {
                        System.out.println("\n>>> QUÉT KIỂM TRA DANH SÁCH THẺ SẮP HẾT HẠN VÀ THẺ BỊ KHÓA <<<");
                        reader.ReaderService.scanAndBlockInactiveReader();
                        report.LibraryReportService.reportExpiryAndBlockedReaders();
                        report.LibraryReportService.exportExpiryAndBlockedReadersToExcel();
                        break;
                    }
                    case 0: {
                        System.out.println("TRỞ VỀ MENU CHÍNH");
                        isRunning = false;
                        break;
                    }
                }
            }
        }
    }


    // Hàm điều phối chức năng quản lý User
    private static void handleAdminUserWork(int role) throws Exception{
        boolean isRunning = true;
        while (isRunning && UserService.currentUserId != 0){

            // 1. PHÂN QUYỀN CHO MANAGER VÀ USER
            if (role == UserService.ROLE_USER || role == UserService.ROLE_MANAGER){
                // PHÂN QUYỀN CHO MANAGER VÀ USER
                System.out.println();
                int choiceForManagerAndUser = chooseInfoUser();
                switch (choiceForManagerAndUser){
                    case 1: {
                        System.out.println("CẬP NHẬT THÔNG TIN TÀI KHOẢN");
                        UserService.updateUser();
                        break;
                    }
                    case 2:{
                        System.out.println("THAY ĐỔI MẬT KHẨU");
                        UserService.changePassword();
                        break;
                    }
                    case 3:{
                        System.out.println("ĐĂNG XUẤT");
                        UserService.logoutUser();
                        UserService.currentUserId = 0;
                        isRunning = false;
                        System.out.println("\n\n");
                        break;
                    }
                    case 0:{
                        System.out.println("TRỞ VỀ MENU CHÍNH");
                        isRunning = false;
                        break;
                    }
                }
            }else{
                // 2. PHÂN QUYỀN CHO ADMIN
                System.out.println();
                int choiceForAdmin = chooseAdminSystemUser();;
                switch (choiceForAdmin){
                    case 1:{
                        System.out.println("XEM DANH SÁCH NGƯỜI DÙNG");
                        UserService.printUserList();
                        break;
                    }
                    case 2:{
                        System.out.println("TẠO NGƯỜI DÙNG MỚI");
                        UserService.registerUser();
                        break;
                    }
                    case 3:{
                        System.out.println("CẬP NHẬT THÔNG TIN NGƯỜI DÙNG");
                        UserService.updateUser();
                        break;
                    }
                    case 4:{
                        System.out.println("PHÂN QUYỀN NGƯỜI DÙNG");    // Cá nhân / bảo mật
                        UserService.manageUserAuthorization();
                        break;
                    }
                    case 5:{
                        System.out.println("THAY ĐỔI MẬT KHẨU");
                        UserService.changePassword();
                        break;
                    }
                    case 6:{
                        System.out.println("\n=== TIẾN TRÌNH ĐIỀU TIẾT AN NINH: ĐÓNG BĂNG / KÍCH HOẠT TÀI KHOẢN ===");
                        UserService.BlockOrUnblockUser();
                        break;
                    }
                    case 7:{
                        System.out.println("ĐĂNG XUẤT");
                        UserService.logoutUser();
                        UserService.currentUserId = 0;
                        isRunning = false;
                        System.out.println("\n\n");
                        break;
                    }
                    case 0:{
                        System.out.println("TRỞ VỀ MENU CHÍNH");
                        isRunning = false;
                        break;
                    }
                }
            }
        }
    }


    // Hàm điều phối chức năng quản lý Độc giả
    private static void handleAdminReaderWork(int role) throws Exception {
        boolean isRunning = true;
        while (isRunning && UserService.currentUserId != 0) {

            // 1. BỘ LỌC PHÂN QUYỀN TRUY CẬP CHO MANAGER VÀ NHÂN VIÊN THƯỜNG (USER)
            if (role == UserService.ROLE_MANAGER || role == UserService.ROLE_USER) {
                System.out.println();
                int choiceForManagerAndUser = chooseInfoReader();
                switch (choiceForManagerAndUser) {
                    case 1: {
                        System.out.println("\n=== DANH SÁCH ĐỘC GIẢ TRONG HỆ THỐNG ===");
                        ReaderService.printReaderList();
                        break;
                    }
                    case 2: {
                        System.out.println("\n=== TIẾN TRÌNH ĐĂNG KÝ: LẬP THẺ ĐỘC GIẢ MỚI ===");
                        ReaderService.createReader();
                        break;
                    }
                    case 3: {
                        System.out.println("\n=== TIẾN TRÌNH CẬP NHẬT: CHỈNH SỬA HỒ SƠ ĐỘC GIẢ ===");
                        ReaderService.updateReader();
                        break;
                    }
                    case 4: {
                        System.out.println("\n=== TIẾN TRÌNH TRA CỨU: TÌM KIẾM THÔNG TIN ĐỘC GIẢ ===");
                        ReaderService.printOneReaderList(ReaderService.findReaderIndexByKeyword(
                                "Mời bạn nhập từ khóa tra cứu (Số Căn cước / Điện thoại / Họ tên Độc giả): "));
                        break;
                    }
                    case 5: {
                        System.out.println("ĐĂNG XUẤT");
                        UserService.logoutUser();
                        UserService.currentUserId = 0;
                        isRunning = false;
                        System.out.println("\n\n");
                        break;
                    }
                    case 0: {
                        System.out.println("TRỞ VỀ MENU CHÍNH");
                        isRunning = false;
                        break;
                    }
                }
            } else {
                // 2. BỘ LỌC PHÂN QUYỀN TRUY CẬP RIÊNG CHO QUẢN TRỊ VIÊN TỐI CAO (ADMIN)
                System.out.println();
                int choiceForAdmin = chooseAdminSystemReader();
                switch (choiceForAdmin) {
                    case 1: {
                        System.out.println("\n=== DANH SÁCH ĐỘC GIẢ TRONG HỆ THỐNG ===");
                        ReaderService.printReaderList();
                        break;
                    }
                    case 2: {
                        System.out.println("\n=== TIẾN TRÌNH ĐĂNG KÝ: LẬP THẺ ĐỘC GIẢ MỚI ===");
                        ReaderService.createReader();
                        break;
                    }
                    case 3: {
                        System.out.println("\n=== TIẾN TRÌNH CẬP NHẬT: CHỈNH SỬA HỒ SƠ ĐỘC GIẢ ===");
                        ReaderService.updateReader();
                        break;
                    }
                    case 4: {
                        System.out.println("\n=== TIẾN TRÌNH TRA CỨU: TÌM KIẾM THÔNG TIN ĐỘC GIẢ ===");
                        ReaderService.printOneReaderList(ReaderService.findReaderIndexByKeyword(
                                "Mời bạn nhập từ khóa tra cứu (Số Căn cước / Điện thoại / Họ tên Độc giả): "));
                        break;
                    }
                    case 5: {
                        System.out.println("\n=== TIẾN TRÌNH AN NINH: ĐÓNG BĂNG / MỞ KHÓA THẺ ĐỘC GIẢ ===");
                        // Gọi siêu hàm hai chiều an toàn tuyệt đối của bạn
                        ReaderService.blockOrUnblockReader("Mời bạn nhập từ khóa định vị tài khoản Độc giả " +
                                "mục tiêu (ENTER để thoát): ");
                        break;
                    }
                    case 6: {
                        System.out.println("ĐĂNG XUẤT");
                        UserService.logoutUser();
                        UserService.currentUserId = 0;
                        isRunning = false;
                        System.out.println("\n\n");
                        break;
                    }
                    case 0: {
                        System.out.println("TRỞ VỀ MENU CHÍNH");
                        isRunning = false;
                        break;
                    }
                }
            }
        }
    }



    // Hàm điều phối chức năng quản lý Reader
    private static void handleAdminBookWork(int role) throws Exception{
        boolean isRunning = true;
        while (isRunning && UserService.currentUserId != 0){

            // 1. PHÂN QUYỀN CHO ADMIN AND MANAGER
            if (role == UserService.ROLE_ADMIN || role == UserService.ROLE_MANAGER){
                System.out.println();
                int choiceForAdminAndManager = chooseAdminSystemBook();
                switch (choiceForAdminAndManager){
                    case 1:{
                        System.out.println("XEM DANH SÁCH SÁCH TRONG THƯ VIỆN");
                        BookService.printBookList();
                        break;
                    }
                    case 2:{
                        System.out.println("THÊM SÁCH");
                        BookService.createBook();
                        break;
                    }
                    case 3:{
                        System.out.println("CẬP NHẬT THÔNG TIN SÁCH");
                        BookService.updateBook();
                        break;
                    }
                    case 4:{
                        System.out.println("TÌM KIẾM SÁCH THEO ID / ISBN / TÊN SÁCH");
                        int index = BookService.findBookIndexByKeyword("Nhập ID / ISBN / Tên sách cần tìm: ");
                        if (index >= 0){
                            System.out.println("THÔNG TIN SÁCH");
                            BookService.printBookByIndex(index);
                        }
                        break;
                    }
                    case 5:{
                        System.out.println("XÓA SÁCH");
                        BookService.deleteBook("Nhập ID/ ISBN / Tên sách cần xóa: ");
                        break;
                    }
                    case 6: {
                        System.out.println("ĐĂNG XUẤT");
                        UserService.logoutUser();
                        UserService.currentUserId = 0;
                        isRunning = false;
                        System.out.println("\n\n");
                        break;
                    }
                    case 0:{
                        System.out.println("TRỞ VỀ MENU CHÍNH");
                        isRunning = false;
                        break;
                    }
                }
            }else {
                // 2. PHÂN QUYỀN CHO USER
                System.out.println();
                int choiceForUser = chooseInfoBook();
                switch (choiceForUser){
                    case 1:{
                        System.out.println("XEM DANH SÁCH SÁCH TRONG THƯ VIỆN");
                        BookService.printBookList();
                        break;
                    }
                    case 2:{
                        System.out.println("THÊM SÁCH");
                        BookService.createBook();
                        break;
                    }
                    case 3:{
                        System.out.println("CẬP NHẬT THÔNG TIN SÁCH");
                        BookService.updateBook();
                        break;
                    }
                    case 4:{
                        System.out.println("TÌM KIẾM SÁCH THEO ID / ISBN / TÊN SÁCH");
                        int index = BookService.findBookIndexByKeyword("Nhập ID / ISBN / Tên sách cần tìm: ");
                        if (index >= 0){
                            System.out.println("THÔNG TIN SÁCH");
                            BookService.printBookByIndex(index);
                        }
                        break;
                    }
                    case 5: {
                        System.out.println("ĐĂNG XUẤT");
                        UserService.logoutUser();
                        UserService.currentUserId = 0;
                        isRunning = false;
                        System.out.println("\n\n");
                        break;
                    }
                    case 0:{
                        System.out.println("TRỞ VỀ MENU CHÍNH");
                        isRunning = false;
                        break;
                    }
                }
            }
        }
    }


    // 5. HÀM ĐIỀU PHỐI CHO ĐỌC GIẢ: HANDLE_READER_FLOW
    private static void handleReaderFlow(){
        boolean isRunning = true;
        while (isRunning){
            try{
                int choice = chooseSubMenuForReader();      // Menu cá nhân của Reader
                switch (choice){
                    case 1:{            // Tìm sách theo ISBN / tên / nhà xuất bản
                        int index = BookService.findBookIndexByKeyword("Nhập ID / ISBN / Tên sách cần tìm: ");
                        if (index >= 0){
                            System.out.println("THÔNG TIN SÁCH");
                            BookService.printBookByIndex(index);
                        }
                        break;
                    }
                    case 2:{            // Chỉnh sửa thông tin thẻ đọc giả
                        ReaderService.updateReader();
                        break;
                    }
                    case 3:{            // Thay đổi mật khẩu
                        ReaderService.changePasswordReader();
                        break;
                    }
                    case 4: {
                        System.out.println("TỰ GIA HẠN HIỆU LỰC THẺ THÀNH VIÊN");
                        // Bốc chính xác ID của Độc giả đang đăng nhập trên RAM ảo
                        String currentReaderId = reader.ReaderService.loggedInReader.getId();

                        // Gọi hàm gia hạn thẻ miễn phí 2 năm + Auto-Unblock vẹn toàn của bạn
                        reader.ReaderService.renewReaderCard(currentReaderId);
                        break;
                    }
                    case 0:{            // Đăng xuất
                        ReaderService.logoutReader();
                        isRunning = false;
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi thao tác " + e.getMessage());
            }
        }
    }


    // 6. HÀM ĐĂNG NHẬP CHUNG: SMART LOGIN
    public static void smartLogin() throws Exception{
        System.out.print("Nhập username hoặc email: ");
        String input = sc.nextLine().trim();

        if (input.contains("@")){
            // LUỒNG CHO READER
            if (ReaderService.loginWithEmail(input)){
                handleReaderFlow();         // Gọi hàm điều phối Reader
            }
        }else{
            // LUỒNG CHO EMPLOYEE
            if (UserService.loginWithUsername(input)){
                handleEmployeeFlow();
            }
        }
    }


    // 9. CHƯƠNG TRÌNH CHÍNH: QUẢN LÝ THƯ VIỆN
    public static void main(String[] args) throws Exception {

        // 1. NẠP DỮ LIỆU TỪ FILE VÀO SANH DÁCH TỈNH USER_LIST
        ArrayList<User> tempUserList = UserService.readFileToList("data/users.dat");
        if (tempUserList !=null && !tempUserList.isEmpty()){
            UserService.getUserList().clear();                  // Xóa userList trống hiện tại cho chắc chắn
            UserService.getUserList().addAll(tempUserList);     // Nạp dữ liệu từ file vào userList
        }

        // 1A. TỰ ĐỘNG NẠP DỮ LIỆU MẪU VÀO USER_LIST
        UserService.seedUserData();

        // 2. NẠP DỮ LIỆU TỪ FILE VÀO SANH DÁCH TỈNH READER_LIST
        ArrayList<Reader> tempReaderList = ReaderService.readFileToList("data/readers.dat");
        if (tempReaderList !=null && !tempReaderList.isEmpty()){
            ReaderService.getReaderList().clear();                      // Xóa readerList trống hiện tại cho chắc chắn
            ReaderService.getReaderList().addAll(tempReaderList);       // Nạp dữ liệu từ file vào readerList
        }

        // 2A. TỰ ĐỘNG NẠP DỮ LIỆU MẪU VÀO READER_LIST
        ReaderService.seedReaderData();

        // 3. NẠP DỮ LIỆU TỪ FILE VÀO SANH DÁCH TỈNH BOOK_LIST
        ArrayList<Book> tempBookList = BookService.readFileToList("data/books.dat");
        if (tempBookList !=null && !tempBookList.isEmpty()){
            BookService.getBookList().clear();                     // Xóa bookList trống hiện tại cho chắc chắn
            BookService.getBookList().addAll(tempBookList);       // Nạp dữ liệu từ file vào bookList
        }

        // 3A. TỰ ĐỘNG NẠP DỮ LIỆU MẪU VÀO BOOK_LIST
        BookService.seedBookData();

        // 4. NẠP DỮ LIỆU TỪ FILE VÀO SANH DÁCH TỈNH BOOK_INSTANCE_LIST: SÁCH VẬT LÝ
        ArrayList<BookInstance> tempBookInstanceList = BookInstanceService.readBookInstanceListFromFile();
        if (tempBookInstanceList !=null && !tempBookInstanceList.isEmpty()){
            // 4.1. Xóa bookInstanceList trống hiện tại cho chắc chắn
            BookInstanceService.getBookInstanceList().clear();

            // 4.2. Nạp dữ liệu từ file vào bookInstanceList
            BookInstanceService.getBookInstanceList().addAll(tempBookInstanceList);
        }

        // 5. NẠP DỮ LIỆU TỪ FILE VÀO SANH DÁCH TỈNH LOAN_LIST: PHIẾU MƯỢN
        ArrayList<LoanSlip> tempLoanList = LoanSlipService.readFileToList("data/loanSlips.dat");
        if(tempLoanList != null && !tempLoanList.isEmpty()){
            // 5.1. Xóa LoanList trống hiện tại cho chắc chắn
            LoanSlipService.getLoanList().clear();

            // 5.2. Nạp dữ liệu từ file vào LoanList
            LoanSlipService.getLoanList().addAll(tempLoanList);
        }

        // 6. VÒNG LẬP SMART LOGIN
        while(true){
            System.out.println("\n=========== CHƯƠNG TRÌNH QUẢN LÝ THƯ VIỆN =============");
            System.out.println("1. Đăng nhập hệ thống (Smart Login)");
            System.out.println("2. Đăng ký thẻ đọc giả");
            System.out.println("3. Thoát chương trình");
            System.out.print("Mời chọn (từ 1 - 3): ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")){
                smartLogin();
            } else if (choice.equals("2")) {
                ReaderService.createReader();
            } else if (choice.equals("3")) {
                System.out.println("Cám ơn bạn đã sử dụng hệ thống!");
                break;
            }
        }
    }
}
