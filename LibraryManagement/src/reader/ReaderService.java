package reader;

import book.Book;
import loanSlip.LoanSlip;
import loanSlip.LoanSlipService;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class ReaderService implements Serializable {

    // KHAI BÁO CÁC BIẾN HẰNG
    private static final int GIVEN_NAME = 0;
    private static final int SURNAME = 1;

    private static final int MAX_RETRIES = 3;           // Số lần nhập sai
    private static final int MAX_BOOK_ALLOWED = 5;      // Số lượng sách tối đa 1 người có thể mượn

    // Biến hằng để phân loại Đọc giả: sinh viên, giáo viên, khác
    public static final int TYPE_STUDENT = 1;
    public static final int TYPE_TEACHER = 2;
    public static final int TYPE_OTHERS = 3;

    // Các hằng về hệ số giảm giá thuê sách
    private static final double RATE_STUDENT = 0.5;     // Giảm giá 50% cho sinh viên, học sinh
    private static final double RATE_TEACHER = 0.7;     // Giảm giá 30% cho Giáo viên
    private static final double RATE_OTHERS = 1;        // Không áp dụng giảm giá

    // Các biến hằng về thời hạn của thẻ đọc giả: tính bằng tháng
    private static final int EXPIRY_MONTHS_STUDENT = 36;    // Thời hạn sủ dụng thể của Sinh viên
    private static final int EXPIRY_MONTHS_TEACHER = 36;    // Thời hạn sủ dụng thể của Giáo viên
    private static final int EXPIRY_MONTHS_OTHERS = 36;    // Thời hạn sủ dụng thể của Khác

    // Các hằng số thời gian gia hạn thẻ đọc giả và thời gian cảnh báo thẻ đọc giả sắp hết hạn
    public static final int DEFAULT_RENEW_MONTHS = 24;     // Gia hạn thêm 2 năm = 24 tháng/lần
    public static final int EXPIRY_ALERT_DAYS = 30;        // Mốc cảnh báo thẻ gần hết hạn trước 30 ngày


    // Các biến hằng về thời gian Block thẻ Đọc giả nếu không có giao dịch trong vòng 365 ngày
    private static final int INACTIVE_LIMIT_DAYS = 365;     // Số ngày tối đa Thẻ phải giao dịch, nếu không bị Block

    // Các biến hằng về chế độ trong các hàm: CREATE, LOGIN và UPDATE
    private static final int CREATE = 1;
    private static final int LOGIN_OR_UPDATE = 2;

    // KHAI BÁO BIẾN HẰNG GIỚI TÍNH
    private static final boolean MALE = true;
    private static final boolean FEMALE = false;

    // KHAI BÁO BIẾN TOÀN CỤC
    private static final Scanner sc = new Scanner(System.in);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static Reader loggedInReader = null;     // Biến lưu Reader đăng nhập
    public static int nextReaderId = 1;             // Cấp ID tự động cho người đăng nhập thành công


    // KHAI BÁO DANH SÁCH ĐỌC GIẢ TOÀN CỤC: readerList
    private static final ArrayList<Reader> readerList = new ArrayList<>();


    // PHƯƠNG THỨC PUBLIC STATIC ĐỂ LẤY readerList (Getter và Setter)
    // Phương thức Getter
    public static ArrayList<Reader> getReaderList() {
        return readerList;
    }

    // Phương thức Setter
    public static void addReader(Reader reader) {
        readerList.add(reader);
    }


    // 1. CÁC HÀM VỀ PHÂN LOẠI ĐỌC GIẢ

    // Hàm nhập loại đọc giả: Sinh viên,Giáo viên, Khác
    private static int inputType(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String type = sc.nextLine().trim();
                int typeChoice = Integer.parseInt(type);
                if (typeChoice == TYPE_STUDENT || typeChoice == TYPE_TEACHER || typeChoice == TYPE_OTHERS) {
                    return typeChoice;
                }
            } catch (Exception e) {
                System.out.print("Lỗi, vui lòng nhập lại. ");
            }
        }
    }

    // Hàm chuyển Type từ kiểu int sang String
    private static String convertTypeToString(int type) {
        if (type == TYPE_STUDENT) {
            return "SV";
        } else if (type == TYPE_TEACHER) {
            return "GV";
        } else {
            return "KH";
        }
    }


    // 2. CÁC HÀM VỀ ID CỦA READER: TỰ ĐỘNG SINH MÃ CHO READER

    // Hàm tự động tạo ID cho Reader
    private static String autoCreateReaderId(int type) {
        // 1. Lấy phần chữ dựa trên Type người dùng chọn
        String word = convertTypeToString(type);

        // 2. Lấy phần số từ nextId;
        String digit = String.format("%03d", nextReaderId);

        // 3. Cộng 02 chuỗi lại
        String result = word + digit;

        // 4. Tăng nextId để dành cho người sau
        nextReaderId++;
        return result;
    }

    // Hàm xác nhận thẻ đọc giả tồn tại: Sử dụng trong class LoanSlipService
    public static boolean checkReaderIdExist(String readerId) throws Exception{
        // 1. Kiểm tra danh sách thẻ đọc giả rỗng
        if(readerList == null){
            return false;
        }
        for(Reader reader : readerList){
            if(reader.getId().equals(readerId)){
                return true;
            }
        }
        return false;
    }


    // 3. CÁC HÀM VỀ PASSWORD

    /* Hàm kiểm tra password hợp lệ. Password mạnh thỏa mãn:
            1) Độ dài: ít nhất 8 ký tự.
            2) Ký tự hoa: Có ít nhất 1 chữ viết hoa (A - Z)
            3) Ký tự thường: Có ít nhất 1 chữ viết thường (a - z)
            4) Chữ số: Có ít nhất 1 con số (0 - 9)
            5) Ký tự đặc biệt: Có ít nhất 1 ký tự đặc biệt sau: !@#$%^&*()
         */
    private static boolean checkPasswordValidation(char[] password) throws Exception {
        // 1. Kiểm tra rỗng và độ dài
        if (password == null || password.length < 8) {
            throw new Exception("mật khẩu phải có ít nhất 8 ký tự");
        }

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        String specailChars = "!@#$%^&*()_+-=";

        for (char c : password) {
            // 2. Kiểm tra khoảng trắng
            if (Character.isWhitespace(c)) {
                throw new Exception("mật khẩu không được chứa khoảng trắng");
            }
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (specailChars.indexOf(c) > -1) {
                hasSpecial = true;
            }
        }

        // 3. Kiểm tra xem có đủ 4 nhóm ký tự không
        if (!(hasUpper && hasLower && hasDigit && hasSpecial)) {
            throw new Exception("mật khẩu phải bao gồm: chữ thường, chữ hoa, số và ký tự đặc biệt !@#$%^&*()_+-=");
        }
        return true;
    }

    // Hàm xóa password: Thực hiện sau khi đăng nhập thành công hay thất bại
    private static void clearPassword(char[] password) {
        if (password != null) {
            Arrays.fill(password, '0');
        }
    }

    // Hàm input password
    private static char[] inputPassword(String prompt, int index) throws Exception{
        int retries = 0;
        while(retries < MAX_RETRIES){
            System.out.print(prompt);
            char[] inputPassword = sc.nextLine().toCharArray();      // Đọc và chuyển String thành char[]
            if (index >=0){         // Chế độ LOGIN/UPDATE (Xác thực người cũ)
                if (Arrays.equals(readerList.get(index).getPassword(), inputPassword)){
                    return inputPassword;       // Mật khẩu đúng
                }else {
                    retries++;
                    clearPassword(inputPassword);
                    if (retries == MAX_RETRIES){
                        System.out.println("Quá số lần nhập!");
                        return null;
                    }
                    System.out.println("Sai mật khẩu! Còn " + (MAX_RETRIES - retries) + " lần thử. ");
                    continue;
                }
            }else {             // Chế độ CREATE: Kiểm tra độ mạnh yếu của password
                try{
                    checkPasswordValidation(inputPassword);          // Nếu lỗi password sẽ nhảy xuống catch
                    return inputPassword;                            // Nếu passwprd OK sẽ thoát vòng lập
                } catch (Exception e) {
                    System.out.println("Lỗi " + e.getMessage());
                }
            }
        }
        return null;
    }


    // 4. CÁC HÀM VỀ FULLNAME, GIVENAME, SURNAME

    // Hàm kiểm tra họ tên có rổng hay không
    private static boolean checkNameEmpty(String name) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("họ tên không được để trống");
        } else {
            return true;
        }
    }

    // Hàm chuẩn hóa chuỗi hoTen
    private static String standardizeName(String name) {
        String[] words = name.split("\\s+");              //Tách từ theo khoảng trắng
        StringBuilder result = new StringBuilder();             //Khai báo chuỗi kết quả
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    // Hàm nhập họ tên
    private static String inputFullName(String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String name = sc.nextLine().trim();

                // Nếu là Update và để trống: Người dùng giữ nguyên Fullname cũ (chạy trong hàm UpdateReader)
                if (isUpdate && name.isEmpty()) {
                    return "";
                }

                // Nếu chạy trong hàm createReader
                checkNameEmpty(name);
                return standardizeName(name);
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }

    // Hàm tách chuỗi fullName = surname + givenName
    private static String[] splitFullName(String fullName) {
        String[] nameParts = new String[2];
        // givenName = nameParts[0]
        // surname = nameParts[1]
        if (fullName.lastIndexOf(" ") > 0) {
            nameParts[GIVEN_NAME] = fullName.substring(fullName.lastIndexOf(" ") + 1);
            nameParts[SURNAME] = fullName.substring(0, fullName.lastIndexOf(" "));
        } else {
            nameParts[GIVEN_NAME] = fullName;
            nameParts[SURNAME] = "";
        }
        return nameParts;
    }


    // 5. CÁC HÀM VỀ THỜI GIAN

    // Hàm format ngày thành chuỗi để hiển thị
    private static String formatDateToStr(LocalDate date) {
        return date.format(formatter);
    }

    // Hàm lấy ngày hiện tại dưới dạng LocalDate: Để dễ tính toán.
    private static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    // Hàm lấy ngày hiện tại dưới dạng String: Để hiển thị
    private static String getCurrentDateToStr() {
        return formatDateToStr(getCurrentDate());
    }

    // Hàm trả về ngày hết hạn = ngày hiện tại + 48 tháng --> dưới dạng LocalDate: Để dễ tính toán
    private static LocalDate getExpiryDate(String typeReader) {
        int months = EXPIRY_MONTHS_OTHERS;                  // Mặc định Reader là Khác
        if (typeReader.equalsIgnoreCase("SV")) {
            months = EXPIRY_MONTHS_STUDENT;
        } else if (typeReader.equalsIgnoreCase("GV")) {
            months = EXPIRY_MONTHS_TEACHER;
        }
        return getCurrentDate().plusMonths(months);
    }

    // Hàm trả về ngày hết hạn = ngày hiện tại + 48 tháng --> dưới dạng String: Để hiển thị
    private static String getExpiryDateToStr(String typeReader) {
        return formatDateToStr(getExpiryDate(typeReader));
    }

    // Hàm kiểm tra ngày hợp lệ
    private static void checkDateValidation(String date) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(date);
        } catch (Exception e) {
            throw new Exception("ngày không đúng định dạng dd/MM/yyyy hoặc ngày không có thực");
        }
    }

    // Hàm nhập ngày
    private static String inputDate(String prompt, boolean isUpdate) {
        while (true) {
            try {
                System.out.print(prompt);
                String date = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Date cũ (chạy trong hàm UpdateReader)
                if (isUpdate && date.isEmpty()) {
                    return "";
                }
                // Nếu chạy trong hàm createReader
                checkDateValidation(date);
                return date;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 6. CÁC HÀM VỀ CCCD

    // Hàm kiểm tra CCCD hợp lệ: 12 sô, không khoảng trắng, không chữ, không ký tự đặc biệt
    private static boolean checkCitizenIdValidation(String citizenId) throws Exception {
        // 1. Kiểm tra độ dài phải là 12 ký tự
        if (citizenId.length() != 12) {
            throw new Exception("số CCCD phải đúng 12 số");
        }

        // 2. Kiểm không có: chữ, khoảng trắng hoặc ký tự đặc biệt
        for (char c : citizenId.toCharArray()) {
            if (!(Character.isDigit(c))) {
                throw new Exception("số CCCD không được chứa: khoảng trắng, chữ, ký tự đặc biệt");
            }
        }
        return true;
    }

    // Hàm nhập CCCD
    private static String inputCitizenId(String prompt, boolean isUpdate) {
        while (true) {
            try {
                System.out.print(prompt);
                String citizenId = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên ID cũ (chạy trong hàm updateReader)
                if (isUpdate && citizenId.isEmpty()) {
                    return "";
                }
                // Nếu chạy trong hàm createReader
                checkCitizenIdValidation(citizenId);
                return citizenId;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 7. CÁC HÀM VỀ SỐ ĐIỆN THOẠI

    // Hàm kiểm tra số diện thoại hợp lệ
    private static boolean checkPhoneValidation(String phone) throws Exception {
        String regex = "^(\\+84|0)[1-9]{1}[0-9]{8}$";
        if (!(phone.matches(regex))) {
            throw new Exception("số ĐT không hợp lệ");
        }
        return true;
    }

    // Hàm nhập số điện thoại
    private static String inputPhone(String prompt, boolean isUpdate) {
        while (true) {
            try {
                System.out.print(prompt);
                String phone = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên phone cũ (chạy trong hàm updateReader)
                if (isUpdate && phone.isEmpty()) {
                    return "";
                }
                // Nếu chạy trong hàm createReader
                checkPhoneValidation(phone);
                return phone;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 8. CÁC HÀM VỀ ĐỊA CHỈ

    // Hàm nhập địa chỉ
    private static String inputAddress(String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String address = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Address cũ (chạy trong hàm updateReader)
                if (isUpdate && address.isEmpty()) {
                    return "";
                }
                // Nếu chạy trong hàm createReader
                return address;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }


    // 9. CÁC HÀM VỀ EMAIL

    // Hàm kiểm tra email hợp lệ: Username@Domain.Extension
    private static boolean checkEmailValidation(String email) throws Exception {
        String regex = "^(\\S+)@(\\S+)\\.(\\S+)$";
        if (!email.matches(regex)) {
            throw new Exception("email không hợp lệ");
        }
        return true;
    }

    // Hàm kiểm tra email tồn tại khi Đăng ký Reader mới
    private static boolean checkEmailDuplicate(String email) throws Exception {
        for (Reader r : readerList) {
            if (r.getEmail().equalsIgnoreCase(email)) {
                throw new Exception("email đã tồn tại");
            }
        }
        return true;
    }

    // Hàm lấy indexReader từ email
    private static int getIndexReader(String email) {
        int indexReader = -1;
        for (int i = 0; i < readerList.size(); i++) {
            if (readerList.get(i).getEmail().equalsIgnoreCase(email)) {
                indexReader = i;
                break;
            }
        }
        return indexReader;
    }

    // Hàm nhập email = username
    public static String inputEmail(String prompt, int mode) {
        while (true) {
            System.out.print(prompt);
            String email = sc.nextLine().trim();
            int index = getIndexReader(email);
            try{
                if(mode == LOGIN_OR_UPDATE){       // Chế độ LOGIN / UPDATE
                    if (index >=0){
                        return email;
                    }else {
                        System.out.println("Lỗi: Email không tồn tại! ");
                    }
                }else {             // mode = 1: chế độ CREATE
                    checkEmailValidation(email);
                    checkEmailDuplicate(email);
                    return email;
                }
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 10. CÁC HÀM VỀ GIỚI TÍNH

    // Hàm nhập giới tính
    private static int inputGender(String prompt, boolean isUpdate){
        while (true){
            try{
                System.out.print(prompt);
                String input = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên giới tính cũ (chạy trong hàm updateReader)
                if(isUpdate && input.isEmpty()){
                    return -1;
                }
                // Nếu chạy trong hàm createReader
                int genderChoice = Integer.parseInt(input);
                if (genderChoice == 0 || genderChoice ==1){
                    return genderChoice;
                }
            } catch (Exception e) {
                System.out.print("Lỗi, vui lòng nhập lại. ");
            }
        }
    }

    // Hàm chuyển giới tính từ kiểu int sang boolean
    private static boolean convertGenderToBoolean(int tempGender){
        if (tempGender == 1){
            return MALE;
        }else {
            return FEMALE;
        }
    }


    // 11. CÁC HÀM VỀ READ - WRITE FILE

    // Hàm ghi danh sách Readers vào file
    public static void saveListToFile(ArrayList<Reader> readerList, String filePath) {
        File folder = new File("data");
        if (!folder.exists()) folder.mkdir();       // Tự tạo thư mục data nếu chưa có.

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))){
            oos.writeObject(readerList);
            System.out.println("Đã lưu thông tin.");
        } catch (IOException e) {
            System.out.println("Lỗi ghi file " + e.getMessage());
        }
    }

    // Hàm đọc file ra danh sách Reader ArrayList<Reader>
    public static ArrayList<Reader> readFileToList(String filePath) {
        ArrayList<Reader> tempList = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))){
            tempList = (ArrayList<Reader>) ois.readObject();

        } catch (FileNotFoundException e) {
            System.out.println("Khởi tạo dữ liệu mới.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempList;
    }


    // 12. HÀM IN DANH SÁCH READER_LIST
    public static void printReaderList () {
        for (int i = 0; i < readerList.size(); i++) {
            System.out.print((i + 1) + ") ");
            System.out.print(readerList.get(i));
        }
    }


    // 13. CÁC HÀM HỖ TRỢ KHÁC

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

    // Hàm in thông tin 1 Reader
    public static void printOneReaderList (int index) throws Exception{
        try{
            if (index >=0){
                System.out.print(readerList.get(index));
            }
        } catch (Exception e) {
            System.out.println("Có lỗi xãy ra " + e.getMessage());
        }
    }


    // 14. CÁC HÀM TỈM KIẾM READER

    // HÀM TÌM READER THEO: CCCD, PHONE, HỌ TÊN
    public static int findReaderIndexByKeyword(String prompt) throws Exception{
        System.out.println(prompt);
        String keyword = sc.nextLine().trim();

        // 1. Xử lý Fullname
        String fullname = standardizeName(keyword);
        String[] nameParts = splitFullName(fullname);
        String surname = nameParts[SURNAME];
        String givenName = nameParts[GIVEN_NAME];

        // 2. Tìm kiếm
        int index = -1;
        try {
            if (!keyword.isEmpty()) {
                for (int i = 0; i < readerList.size(); i++) {
                    // 2.1. Tìm theo CCCD
                    if (readerList.get(i).getCitizenId().equalsIgnoreCase(keyword)) {
                        index = i;
                        break;
                    }

                    // 2.2. Tìm theo số điện thoại
                    if (readerList.get(i).getPhone().equalsIgnoreCase(keyword)) {
                        index = i;
                        break;
                    }

                    // 2.3. Tìm theo họ tên
                    // 2.3.1. Nếu người dùng nhập đủ họ tên (surname trong ReaderList không rỗng)
                    if (!surname.isEmpty()) {
                        if (readerList.get(i).getSurname().equalsIgnoreCase(surname)
                                && readerList.get(i).getGivenName().equalsIgnoreCase(givenName)) {
                            index = i;
                            break;
                        }
                    } else {
                        // 2.3.2. Nếu người dùng chỉ nhập 1 từ (chương trình hiểu đó là givenName)
                        // Ta chỉ cần so khớp với phần Tên (givenName) trong danh sách là đủ
                        if (readerList.get(i).getGivenName().equalsIgnoreCase(givenName)) {
                            index = i;
                            break;
                        }
                    }

                    // 2.4. Tìm theo Id
                    if (readerList.get(i).getId().equalsIgnoreCase(keyword)) {
                        index = i;
                        break;
                    }
                }
            }
            if (index < 0 && !keyword.isEmpty()) {
                System.out.println(keyword + " chưa đăng ký thẻ đọc giả.");
            }
        } catch (Exception e) {
            System.out.println("Lỗi: nhập liệu rỗng. " + e.getMessage());
        }
        return index;
    }


    // HÀM TÌM OOP READER TỪ ISBN
    public static Reader findReaderById(String readerId){
        if(readerId == null){
            return null;
        }

        // Làm sạch keyword người dùng nhập vào 1 lần trước vòng lập
        for(Reader reader: readerList){
            if (reader.getId().equalsIgnoreCase(readerId)){
                return reader;
            }
        }

        // Nếu không tìm thấy thì trả về null
        return null;
    }


    // 15. SIÊU HÀM ĐỒNG BĂNG / MỞ KHÓA THẺ ĐỘC GIẢ (SOFT-DELETE)
    public static void blockOrUnblockReader(String prompt) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String keyword = sc.nextLine().trim();
                if (keyword.isEmpty()) {
                    System.out.println("👉 Đã hủy thao tác Đóng băng/Mở khóa thẻ Độc giả.");
                    return;
                }

                // 1. Tra cứu chỉ số index của độc giả mục tiêu trên bộ nhớ RAM đệm
                int index = findReaderIndexByKeyword(keyword);
                if (index < 0) {
                    System.out.println("\u274C Lỗi: Không tìm thấy tài khoản Độc giả hợp lệ trên hệ thống!");
                    continue;
                }

                // 2. Bốc thực thể đối tượng lên để chuẩn bị xử lý
                Reader targetReader = readerList.get(index);

                // 3. Hiển thị bảng đối soát danh tính trực quan trước khi chốt lệnh
                System.out.println("--- THÔNG TIN HỒ SƠ ĐỘC GIẢ MỤC TIÊU ---");
                System.out.println("Mã độc giả: " + targetReader.getId() + " | Email: " + targetReader.getEmail());
                System.out.println("Họ và tên : " + targetReader.getSurname() + " " + targetReader.getGivenName());
                System.out.println("Điện thoại : " + targetReader.getPhone() + " | Hạn thẻ: " + targetReader.getExpiryDate());
                System.out.println("Trạng thái hiện tại: " + (targetReader.isBlock() ? "ĐÃ BỊ KHÓA" : "ĐANG HOẠT ĐỘNG ✨"));

                // 4. KIỂM TRA TRẠNG THÁI ĐỂ PHÂN NHÁNH 2 KỊCH BẢN QUẦY
                if (targetReader.isBlock()) {
                    // KỊCH BẢN 1: MỞ KHÓA THÈ (UNBLOCK)
                    int checkOpen = askYesNo("Thẻ này hiện đang bị khóa. Bạn có muốn MỞ KHÓA phục hồi tài " +
                            "khoản không? (Y/N): ");
                    if (checkOpen == 1) {
                        targetReader.setBlock(false); // Lật cờ phục hồi trạng thái hoạt động
                        saveListToFile(readerList, "data/readers.dat"); // Save dữ liệu xuống ổ cứng
                        System.out.println("\u2705 Đã phục hồi và kích hoạt trạng thái ĐANG HOẠT ĐỘNG thành công " +
                                "cho Độc giả!");

                        // Gọi liên thông hàm tiện ích in thông điệp gửi nhanh Zalo/Email cho khách
                        printNotificationTemplate(targetReader, "MỞ KHÓA PHỤC HỒI THẺ THÀNH VIÊN");
                    } else {
                        System.out.println("\u274C Hủy thao tác mở khóa.");
                    }
                } else {
                    // KỊCH BẢN 2: ĐÓNG BĂNG THẺ (BLOCK)
                    int checkBlock = askYesNo("Bạn có chắc chắn muốn ĐÓNG BĂNG vô hiệu hóa thẻ Độc giả " +
                            "này không? (Y/N): ");
                    if (checkBlock == 1) {
                        targetReader.setBlock(true); // Ép cờ đóng băng tài khoản ma/nợ xấu
                        saveListToFile(readerList, "data/readers.dat"); // Đóng băng dữ liệu xuống ổ cứng
                        System.out.println("\u2705 Đã đóng băng thẻ Độc giả thành công! Quyền mượn sách đã bị tước bỏ.");

                        // Gọi liên thông hàm tiện ích in thông điệp gửi nhanh Zalo/Email cho khách
                        printNotificationTemplate(targetReader, "ĐÓNG BĂNG ĐÌNH CHỈ THẺ THÀNH VIÊN");
                    } else {
                        System.out.println("\u274C Hủy thao tác đóng băng.");
                    }
                }

                // 5. Hỏi vòng lặp tiếp tục tác nghiệp hành chính
                if (askYesNo("Bạn có muốn tiếp tục xử lý thẻ Độc giả khác không? (Y/N): ") == 0) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("\u274C Có lỗi xảy ra trong tiến trình đều tiết an ninh thẻ Độc giả: " + e.getMessage());
            }
        }
    }


    // 21. HÀM TIỆN ÍCH IN THÔNG ĐIỆP GỬI NHANH QUA ZALO/EMAIL CHO ĐỘC GIẢ
    public static void printNotificationTemplate(Reader r, String actionType) {
        // 1. Kiểm tra Reader rỗng
        if (r == null) return;


        // 2. Truy xuất tên và nghề nghiệp Reader
        String fullname = r.getSurname() + " " + r.getGivenName();
        String currentJob = (r.getType() == TYPE_STUDENT) ? "Sinh viên" :
                (r.getType() == TYPE_TEACHER) ? "Giáo viên" : "Khác";

        // 3. Xuất thông tin tóm lược giao dịch

        System.out.println("\n====================================================================");
        System.out.println("[THƯ VIỆN ĐIỆN TỬ - THÔNG BÁO TIẾN TRÌNH HỒ SƠ THÀNH VIÊN]");
        System.out.println("--------------------------------------------------------------------");
        System.out.println("Kính gửi Độc giả: " + fullname + " (Mã thẻ: " + r.getId() + ")");
        System.out.println("Hệ thống ghi nhận hồ sơ thành viên của bạn đã được " + actionType + " thành công!");
        System.out.println("- Đối tượng phân loại : " + currentJob);
        System.out.println("- Tình trạng thẻ mạng : " + (r.isBlock() ? "ĐÃ BỊ KHÓA" : "ĐANG HOẠT ĐỘNG ✨"));
        System.out.println("- Ngày kích hoạt hiệu : " + r.getCreatedDate());
        System.out.println("- Thời hạn hiệu lực   : đến ngày " + r.getExpiryDate());
        System.out.println("--------------------------------------------------------------------");
        System.out.println("👉 Mời bạn đăng nhập bằng Email và Password đã đăng ký để sử dụng.");
        System.out.println("Xin cảm ơn và rất mong được đón tiếp bạn tại quầy tư liệu!");
        System.out.println("====================================================================\n");
    }



    // 16. HÀM VỀ TẠO MỚI THẺ ĐỌC GIẢ
    public static void createReader() throws Exception {
        try {
            // 1. Nhập loại đọc giả
            int type = inputType("Loại đọc giả (1: Sinh viên; 2: Giáo viên; 3: Khác): ");

            // 2. Tạo tự động ID cho đọc giả
            String id = autoCreateReaderId(type);

            // 3. Nhập password
            char[] password = inputPassword("Nhập password: ", -1);

            // 4. Nhập họ tên
            String fullname = inputFullName("Họ tên: ", false); // Nhập họ tên + chuẩn hóa họ tên
            String[] nameParts = splitFullName(fullname);
            String givenName = nameParts[GIVEN_NAME];
            String surname = nameParts[SURNAME];

            // 5. Nhập ngày sinh
            String birthDay = inputDate("Ngày sinh: ", false);// Nhập ngày sinh: có kiểm tra ngày hợp lệ

            // 6. Nhập CCCD
            String citizenId = inputCitizenId("CCCD: ", false);

            // 7. Nhập số điện thoại
            String phone = inputPhone("Số điện thoại: ", false);

            // 8. Nhập địa chỉ
            String address = inputAddress("Địa chỉ: ", false);

            // 9. Nhập email
            String email = inputEmail("Email: ", CREATE);

            // 10. Nhập giới tính
            int tempGender = inputGender("Giới tính (1: nam; 0: nữ): ",false); // Nhập giới tính
            boolean gender = convertGenderToBoolean(tempGender);

            // 11. Tự động tạo ngày lập thẻ đọc giả
            String createDate = getCurrentDateToStr();

            // 12. Tự động tạo ngày hết hạn của thẻ đọc giả
            String expiryDate = getExpiryDateToStr(convertTypeToString(type));

            // 13. Tạo đối tượng và nạp vào danh sách readerList: Sử dụng nextReaderId
            Reader newReader = new Reader(
                    id,                         // ID tự động tăng
                    Arrays.copyOf(password,password.length),                   // Mảng char[] mật khẩu
                    type,
                    surname,
                    givenName,
                    birthDay,
                    citizenId,
                    phone,
                    address,
                    email,
                    gender,           // boolean đã convert
                    createDate,
                    expiryDate,
                    false
            );

            addReader(newReader);                          // Lưu vào ArrayList
            saveListToFile(readerList,"data/readers.dat");     // Save to file

            // 14. Hiển thị thông báo cho Đọc giả
            printNotificationTemplate(newReader, "ĐĂNG KÝ TẠO MỚI");

            // 15. Bảo mật: Xóa mảng mật khẩu tạm sau khi đã lưu xong
            clearPassword(password);

        } catch (Exception e) {
            System.out.println("Lỗi " + e.getMessage());
        }
    }


    // 17. CÁC HÀM LOGIN_READER, LOGOUT_READER

    // Hàm cho Đọc giả đăng nhập
    public static boolean loginWithEmail(String email) throws Exception {
        // 1. Kiểm tra email độc giả tồn tại
        int index = -1;
        for (int i = 0; i < readerList.size(); i++) {
            if (readerList.get(i).getEmail().equalsIgnoreCase(email)) {
                index = i;
                break;
            }
        }

        // 2. Kiểm tra không tìm thấy email
        if (index < 0) {
            System.out.println("\u274C Lỗi: Email '" + email + "' chưa được đăng ký thẻ độc giả.");
            return false;
        }

        // 3. Khai báo biến số lần nhập sai mật khẩu
        int retries = 0;

        // 4. Quét vòng lặp bọc giáp giới hạn tối đa 3 lần nhập sai mật khẩu
        while (retries < MAX_RETRIES) {
            System.out.print("Nhập password (Còn " + (MAX_RETRIES - retries) + " lần thử): ");
            char[] inputPassword = sc.nextLine().toCharArray();

            // 5. Đối soát mảng char[] mật khẩu trên bộ nhớ RAM đệm
            if (Arrays.equals(readerList.get(index).getPassword(), inputPassword)) {

                // 6. TRẠM GÁC AN NINH KHÓA THẺ ĐỘC GIẢ: Đặt sau khi mật khẩu thành công
                if (readerList.get(index).isBlock()) {
                    System.out.println("\u26A0\uFE0F TRUY CẬP BỊ TỪ CHỐI: Thẻ độc giả của bạn hiện ĐANG BỊ KHÓA!");
                    System.out.println("    (Do thẻ quá hạn chưa gia hạn hoặc quá 365 ngày không phát sinh giao dịch).");
                    System.out.println("👉 Vui lòng liên hệ Thủ thư tại quầy để xử lý mở khóa thẻ.");
                    clearPassword(inputPassword);       // Xóa mật khẩu lập tức chống lộ mã
                    return false;
                }

                // 7. Vượt qua tất cả trạm gác -> Xác lập phiên làm việc thành công
                loggedInReader = readerList.get(index);
                System.out.println("\u2705 Chào mừng Độc giả " + loggedInReader.getGivenName() + " đăng nhập thành công!");
                clearPassword(inputPassword);       // Xóa mật khẩu lập tức chống lộ mã
                return true;
            } else {
                // 8. Sai mật khẩu
                retries++;
                clearPassword(inputPassword);       // Xóa mật khẩu lập tức chống lộ mã
                if (retries < MAX_RETRIES) {
                    System.out.println("\u274C Sai mật khẩu! Vui lòng thử lại.");
                }
            }
        }

        System.out.println("\u26A0\uFE0F ĐĂNG NHẬP THẤT BẠI: Bạn đã gõ sai mật khẩu quá " + MAX_RETRIES + " lần cho phép!");
        return false;
    }


    // Hàm cho người dùng đăng xuất
    public static void logoutReader(){
        loggedInReader = null;      // Chỉ cần gán biến loggedInReader = null là xem như Reader logout
        System.out.println("Đã đăng xuất thành công. Hẹn gặp bạn lần sau.");
    }


    // 18. CÁC HÀM UPDATE_READER, CHANGE_PASSWORD_READER và DELETE_READER

    // Hàm cập nhật thông tin Reader: updateReader
    public static void updateReader() throws Exception{
        // 1. Kiểm tra quyền truy cập
        if (loggedInReader == null){
            System.out.println("Vui lòng đăng nhập trước khi cập nhật thông tin.");
            return;
        }

        // 2. Xác định đối tượng mục tiêu: Target
        Reader targetReader = loggedInReader;

        System.out.println("\n------ Cập nhật thông tin đọc giả: " + targetReader.getSurname() + " " + targetReader.getGivenName());

        // 3. Cập nhật họ tên
        System.out.println("Họ tên: " + targetReader.getSurname() + " " + targetReader.getGivenName());
        String newFullname = inputFullName("Họ tên mới (Enter để giữ nguyên như cũ): ",true); // Nhập họ tên + chuẩn hóa họ tên
        if(!newFullname.trim().isEmpty()){
            newFullname = standardizeName(newFullname);
            String[] nameParts = splitFullName(newFullname);
            targetReader.setSurname(nameParts[SURNAME]);
            targetReader.setGivenName(nameParts[GIVEN_NAME]);
        }

        // 4. Cập nhật ngày sinh
        System.out.print("Ngày sinh : " + targetReader.getBirthday() + ". ");
        String newBirthday = inputDate("Ngày sinh mới (Enter để giữ nguyên như cũ): ",true);
        if (!newBirthday.isEmpty()){
            targetReader.setBirthday(newBirthday);
        }

        // 5. Nhập CCCD
        System.out.print("CCCD : " + targetReader.getCitizenId() + ". ");
        String newCitizenId = inputCitizenId("CCCD mới (Enter để giữ nguyên như cũ): ", true);
        if (!newCitizenId.isEmpty()){
            targetReader.setCitizenId(newCitizenId);
        }

        // 6. Nhập số điện thoại
        System.out.print("Số ĐT : " +targetReader.getPhone() + ". ");
        String newPhone = inputPhone("Số điện thoại mới (Enter để giữ nguyên như cũ): ", true);
        if (!newPhone.isEmpty()){
            targetReader.setPhone(newPhone);
        }

        // 7. Nhập địa chỉ
        System.out.println("Địa chỉ : " + targetReader.getAddress());
        String newAddress = inputAddress("Địa chỉ mới (Enter để giữ nguyên như cũ): ", true);
        if (!newAddress.isEmpty()){
            targetReader.setAddress(newAddress);
        }

        // 8. Nhập giới tính
        System.out.println("Giới tính : " + ((targetReader.getGender() ? "nam" : "nữ") + ". "));
        int newGender = inputGender("Giới tính mới (1: nam; 0: nữ - Enter để giữ nguyên như cũ): ",true);
        if (newGender != -1){
            targetReader.setGender(convertGenderToBoolean(newGender));
        }

        // 9. Lưu thay đỗi và thông báo
        saveListToFile(readerList,"data/readers.dat");     // Save to file

        // 10. Xuất thông báo
        printNotificationTemplate(targetReader, "CẬP NHẬT THÔNG TIN HỒ SƠ");
    }

    // Hàm đổi password Reader
    public static void changePasswordReader() throws Exception{
        if (loggedInReader == null){
            System.out.println("Vui lòng đăng nhập để đổi mật khẩu.");
            return;
        }

        // 1. Xác thực mật khẩu cũ
        int index = getReaderList().indexOf(loggedInReader);       // Lấy index của Reader
        System.out.println("---- Đổi mật khẩu: " + readerList.get(index).getSurname() + " " + readerList.get(index).getGivenName());
        char[] oldPass = inputPassword("Mật khẩu cũ: ", index);
        if (oldPass == null){
            System.out.println("Xác thực thất bại, không thể đổi mật khẩu!");
            return;
        }

        // 2. Nhập mật khẩu mới
        while (true){
            try{
                char[] newPass_1 = inputPassword("Mật khẩu mới: ", -1);
                char[] newPass_2 = inputPassword("Xác nhận mật khẩu mới: ", -1);

                // Kiểm tra newPassword nhập 2 lần trùng nhau
                if(!(Arrays.equals(newPass_1,newPass_2))){
                    System.out.println("Lỗi: mật khẩu xác nhận không khớp. Hãy thử lại. ");
                    clearPassword(newPass_1);
                    clearPassword(newPass_2);
                    continue;
                }

                // Lưu mật khẩu mới và hiển thị thông báo
                loggedInReader.setPassword(Arrays.copyOf(newPass_1,newPass_1.length));
                saveListToFile(readerList,"data/readers.dat");     // Save to file

                clearPassword(oldPass);         // Clear oldPass, newPass_1, newPass_2
                clearPassword(newPass_2);
                clearPassword(newPass_1);

                System.out.println("=> Đổi mật khẩu thành công.");
                return;
            } catch (Exception e) {
                System.out.println("Lỗi định dạng " + e.getMessage());
                // Cho phép nhập mật khẩu mới mà không phải nhập lại mật khẩu cũ
            }
        }
    }


    // 19. HÀM TÌM CÁC THẺ ĐỌC GIẢ RÁC VÀ TỰ ĐỘNG KHÓA: THẺ KHÔNG CÓ GIAO DỊCH LIÊN TỤC TRONG 365 NGÀY + CÒN HẠN
    public static void scanAndBlockInactiveReader(){
        try{
            // PHẦN 1: THU THẬP NGÀY GIAO DỊCH THÀNH CÔNG GẦN NHẤT TRÊN RAM

            // 1. Khởi tạo 1 HashMap lưu giữ danh sách Thẻ đọc giả rác
            // Key (Str) là ReaderId; Ngày mượn gần nhất = LocalDate
            HashMap<String, java.time.LocalDate> lastActiveMap = new HashMap<>();

            // 2. Quét vòng lập qua danh sách phiếu mượn toàn hệ thống
            for(LoanSlip loan: LoanSlipService.getLoanList()){
                // 3. Kiểm tra 4 lớp: Phiếu rỗng, phiếu chưa hoàn thành, phiếu bị block, phiếu có mã đọc giả = null
                if(loan == null || loan.getStatus() != LoanSlip.COMPLETED ||
                        loan.isBlock() || loan.getReaderId() == null){
                    continue;   // Bỏ qua, quét phiếu tiếp theo
                }

                // 4. Lấy ngày mượn để đối soát
                    LocalDate rentalDate = loan.getRentalDate();

                // 5. Kiểm tra:
                // 5.1. Hoặc là độc giả này mới tinh, chưa từng có tên trong Map --> nạp vào Map
                // 5.2. Hoặc đọc giả đã có giao dịch, nhưng ngày giao dịch mới nằm sau ngày giao dịch cũ đã
                // nạp vào Map trước đó
                if(!lastActiveMap.containsKey(loan.getReaderId()) ||
                        rentalDate.isAfter(lastActiveMap.get(loan.getReaderId()))){
                    lastActiveMap.put(loan.getReaderId(), rentalDate);      // Nạp OOP mới vào Map
                }
            }

            // -> KẾT THÚC PHẦN 1: THU THẬP ĐƯỢC DS HASHMAP <Key: readerId, Value: rentalDate là Ngày giao dịch gần nhất>

            // PHẦN 2: DUYỆT DANH SÁCH ĐỌC GIẢ VÀ ÁP DỤNG GIẢI THUẬT GỘP MỐC THỜI GIAN
            // 6. Khai báo biến đếm số thẻ bị Block
            int blockedCount = 0;

            // 7. Khai báo khuôn định dạng ngày Việt Nam
            DateTimeFormatter formatterDateVN = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // 8. Thiết lập mốc thời gian 1 năm để xác định Block thẻ
            LocalDate oneYearAgo = LocalDate.now().minusDays(INACTIVE_LIMIT_DAYS);

            // 9. Quét vòng lập duyệt qua danh sách Reader
            for(Reader reader: ReaderService.getReaderList()){
                // 10. Kiểm tra reader null hoặc có reader nhưng Id null
                if(reader == null || reader.getId() == null){
                    continue;   // Bỏ qua, quét phần tử tiếp theo
                }

                // 11. Bọc khối try .. catch nội bộ để chuyển đổi chuỗi ngày lập thẻ gốc từ dạng String
                // sang dạng LocalDate + định dạng ngày VN: an toàn bộ nhớ
                LocalDate createdDate = null;
                try{
                    createdDate = LocalDate.parse(reader.getCreatedDate(),formatterDateVN);
                } catch (Exception e) {
                    System.out.println("\u274C Lỗi chuyển đổi ngày lập thẻ từ String sang LocalDate ");
                    continue;   // Bỏ qua, quét phần tử tiếp theo để chống sập chương trình
                }

                // 12. Khai báo mốc hoạt động cuối cùng mặc định ban đầu bằng chính Ngày lập thẻ
                LocalDate lastActiveDate = createdDate;

                // 13. Kiểm tra trong danh sách HashMap có chứa ReaderId đó không thì lấy ngày trong HashMap
                // làm ngày giao dịch gần nhất
                if(lastActiveMap.containsKey(reader.getId())){
                    lastActiveDate = lastActiveMap.get(reader.getId());
                }   // Ngược lại, ngày giao dịch gần nhất là ngày lập thẻ = mục 12

                // 14. Khóa thẻ nếu QUÁ HẠN CHƯA GIA HẠN.
                // HOẶC QUÁ 1 NĂM KHÔNG HOẠT ĐỘNG bằng cách: Nếu mốc giao dịch cuối cùng này nằm trước mốc 1 năm
                // cách đây VÀ thẻ chưa bị Block -> thẻ vi phạm, BLock lại

                // 15. Bọc khối try .. catch nội bộ để chuyển đổi chuỗi ExpiryDate() từ dạng String
                // sang dạng LocalDate + định dạng ngày VN: an toàn bộ nhớ
                LocalDate expiredDate = null;
                try{
                    expiredDate = LocalDate.parse(reader.getExpiryDate(),formatterDateVN);
                } catch (Exception e) {
                    System.out.println("\u274C Lỗi chuyển đổi ngày hết hạn thẻ từ String sang LocalDate ");
                    continue;   // Bỏ qua, quét phần tử tiếp theo để chống sập chương trình
                }

                // 16.1. Trường hợp 1: Thẻ quá hạn
                boolean isExpired = (expiredDate != null && expiredDate.isBefore(LocalDate.now()));

                // 16.2. Trường hợp 2: Thẻ chưa quá hạn nhưng 1 năm không có giao dịch
                boolean isInactiveInOneYear = (lastActiveDate != null && lastActiveDate.isBefore(oneYearAgo));

                if((isExpired || isInactiveInOneYear) && !reader.isBlock()){
                    reader.setBlock(true);      // BLOCK THẺ ĐỌC GIẢ VI PHẠM
                    blockedCount ++;            // Tăng biến đếm số thẻ đọc giả bị Khóa
                }
            }

            // PHẦN 3: ĐỒNG BỘ DỮ LIỆU SẠCH XUỐNG Ổ CỨNG
            // 15. Kiểm tra và Ghi file bảo mật dữ liệu
            if (blockedCount > 0){      // Kiểm tra có Thẻ đoạc giả bị Block
                saveListToFile(readerList,"data/readers.dat");     // Save to file
                System.out.println("\u2705 Hệ thống tự động quét và khóa thành công " + blockedCount +
                        " thẻ độc giả quá 1 năm không hoạt động.");
            }else {
                System.out.println("\u2705 Quét an ninh hoàn tất: Không phát sinh tài khoản Đọc giả vi phạm.");
            }

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc tự động quét thẻ và khóa thẻ Đọc giả rác " + e.getMessage());
        }
    }


    // 20. HÀM GIA HẠN THẺ ĐỌC GIẢ
    public static boolean renewReaderCard(String readerId){
        try{
            // PHẦN 1: Chốt chặn an toàn và Định vị Đọc giả
            // 1. Kiểm tra chuỗi đầu vào rỗng hoặc null
            if(readerId == null || readerId.isEmpty()){
                System.out.println("\u274C Lỗi thẻ đọc giả rỗng");
                return false;
            }

            // 2. Chuẩn hóa dữ liệu nhập
            String cleanReaderId = readerId.trim().toUpperCase();

            // 3. Tìm kiếm reader
            Reader reader = findReaderById(cleanReaderId);

            // 4. Kiểm tra thẻ đọc giả rỗng
            if(reader == null){
                System.out.println("\u274C Lỗi: Thẻ độc giả " + cleanReaderId + " không tồn tại trên hệ thống!");
                return false;
            }

            // PHẦN 2: Xử lý đẩy lùi dòng thời gian hết hạn thẻ 24 tháng và Tự động MỞ khóa thẻ đọc giả
            // 5. Khai báo định dạng khuôn ngày Việt Nam
            DateTimeFormatter formatterDateVN = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // 6. Bọc try .. catch để chuyển đổi an toàn ngày hết hạn thẻ từ String sang LocalDate
            LocalDate currentExpiry = null;
            try{
                currentExpiry = LocalDate.parse(reader.getExpiryDate(),formatterDateVN);

            } catch (Exception e) {
                System.out.println("\u274C Lỗi trong chuyển định dạng ngày hết hạn thẻ từ String sang LocalDate!");
                return false;
            }

            // 7. Tăng thời hạn thẻ thêm 24 tháng
            LocalDate newExpiry = currentExpiry.plusMonths(DEFAULT_RENEW_MONTHS);

            // 8. Chuyển đổi ngày hết hạn mới từ kiểu LocalDate sang String
            String StrNewExpiry = newExpiry.format(formatterDateVN);

            // 9. Gán ngày hết hạn thẻ đọc giả vào danh sách
            reader.setExpiryDate(StrNewExpiry);

            // 10. Mở khóa thẻ đọc giả
            reader.setBlock(false);

            // PHẦN 3: In thông báo UX Quầy miễn phí và Đồng bộ ổ cứng
            // 11. In thông báo kết quả gia hạn thẻ đọc giả
            System.out.println("\u2705 Gia hạn thẻ độc giả MIỄN PHÍ thành công!");
            System.out.println("   -> Độc giả     : " + reader.getSurname() + " " + reader.getGivenName());
            System.out.println("   -> Hạn dùng mới: " + reader.getExpiryDate());
            System.out.println("   \u2728 Trạng thái  : ĐANG HOẠT ĐỘNG");

            // 12. Lưu file và đồng bộ với RAM
            saveListToFile(readerList,"data/readers.dat");     // Save to file

            // 13. Xuất thông báo
            printNotificationTemplate(reader, "GIA HẠN THỜI HẠN SỬ DỤNG THẺ");

        } catch (Exception e) {
            System.out.println("\u274C Lỗi trong việc tự động gia hạn thẻ Đọc giả " + e.getMessage());
        }

        // 13. Thoát chương trình
        return true;
    }


    // 21. HÀM TỰ ĐỘNG NẠP DỮ LIỆU ĐỘC GIẢ MẪU ĐỂ KIỂM THỬ (DATA SEEDING) [🛡️]
    public static void seedReaderData() {
        // Chỉ nạp dữ liệu mồi nếu danh sách hiện hành trống rỗng chống trùng lặp
        if (readerList == null || !readerList.isEmpty()) {
            return;
        }
        try {
            // Độc giả số 1: Sinh viên
            Reader r1 = new Reader();
            r1.setId("DG001");
            r1.setSurname("Nguyen Van");
            r1.setGivenName("An");
            r1.setBirthday("20/10/2004");
            r1.setCitizenId("012345678901");
            r1.setPhone("0912345678");
            r1.setAddress("123 Nguyen Trai, Q5, HCM");
            r1.setEmail("an.nguyen@gmail.com");
            r1.setGender(true); // Nam
            r1.setType(TYPE_STUDENT); // Sinh viên
            r1.setCreatedDate("12/06/2026");
            r1.setExpiryDate("12/06/2028"); // Hiệu lực 2 năm
            r1.setBlock(false); // Hoạt động
            r1.setPassword("12345".toCharArray());
            readerList.add(r1);

            // Độc giả số 2: Sinh viên
            Reader r2 = new Reader();
            r2.setId("DG002");
            r2.setSurname("Le Thi");
            r2.setGivenName("Binh");
            r2.setBirthday("15/05/2005");
            r2.setCitizenId("012345678902");
            r2.setPhone("0987654321");
            r2.setAddress("456 Le Loi, Q1, HCM");
            r2.setEmail("binh.le@gmail.com");
            r2.setGender(false); // Nữ
            r2.setType(TYPE_STUDENT);
            r2.setCreatedDate("12/06/2026");
            r2.setExpiryDate("12/06/2028");
            r2.setBlock(false);
            r2.setPassword("12345".toCharArray());
            readerList.add(r2);

            // Độc giả số 3: Giáo viên
            Reader r3 = new Reader();
            r3.setId("DG003");
            r3.setSurname("Tran Minh");
            r3.setGivenName("Cuong");
            r3.setBirthday("12/12/1985");
            r3.setCitizenId("012345678903");
            r3.setPhone("0909090909");
            r3.setAddress("789 Dien Bien Phu, Q3, HCM");
            r3.setEmail("cuong.tran@gmail.com");
            r3.setGender(true);
            r3.setType(TYPE_TEACHER); // Giáo viên
            r3.setCreatedDate("12/06/2026");
            r3.setExpiryDate("12/06/2028");
            r3.setBlock(false);
            r3.setPassword("12345".toCharArray());
            readerList.add(r3);

            saveListToFile(readerList, "data/readers.dat"); // Đóng băng ghi file vĩnh viễn
            System.out.println("==> Đã nạp thành công 3 Độc giả mẫu để TEST (Mật khẩu mặc định: 12345)");
        } catch (Exception e) {
            System.out.println("❌ Lỗi nạp dữ liệu mồi Độc giả: " + e.getMessage());
        }
    }



}
