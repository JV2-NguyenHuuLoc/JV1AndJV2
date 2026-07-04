package user;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class UserService implements Serializable{
    // Khai báo biến hằng
    private static final int GIVEN_NAME = 0;
    private static final int SURNAME = 1;

    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_MANAGER = 2;
    public static final int ROLE_USER = 3;

    private static final int MAX_RETRIES = 3;

    private static final boolean ACTIVE_STATUS = true;   // Trạng thái tài khoản người dùng "hoạt động bình thướng"
    private static final boolean BLOCK_STATUS = false;   // Trạng thái tài khoản người dùng "BỊ KHÓA"

    // Các biến hằng về chế độ trong các hàm: CREATE, LOGIN và UPDATE
    private static final int CREATE = 1;
    private static final int LOGIN_OR_UPDATE = 2;

    // Khai báo biến toàn cục
    private static final Scanner sc = new Scanner(System.in);
    public static int currentUserId = 0;        // Chưa ai đăng nhập hoặc đăng nhập không thành công
    public static int nextId = 1;               // Cấp ID tự động cho người đăng nhập thành công

    // Khai báo private static final ArrayList
    private static final ArrayList<User> userList = new ArrayList<>();

    // Phương thức public static để lấy userList (Getter)
    public static ArrayList<User> getUserList(){
        return userList;
    }

    // Phương thức public static void để gán giá trị cho userList (Setter)
    public static void addUser(User user){
        userList.add(user);
    }


    // 1. CÁC HÀM VỀ USERNAME

    // Hàm chỉ nhận số và chữ
    private static boolean isAlphaNumeric(String s) throws Exception{
        for (char c: s.toCharArray()){
            if (!((c >='a' && c <='z') || (c >='A' && c <='Z') || (c >= '0' && c <= '9'))){
                throw new Exception("tên đăng nhập chỉ bao gồm chữ và số");
            }
        }
        return true;
    }

    /* Hàm kiểm tra tên đăng nhập hợp lệ:
            1) Độ dài: ít nhất 6 ký tự
            2) Khoảng trắng: Không được có khoảng trắng
    */
    private static boolean checkUsernameValidation(String username) throws Exception{
        // 1. Kiểm tra độ dài
        if(username == null || username.length() < 6 ){
            throw new Exception("tên đăng nhập phải có ít nhất 6 ký tự");
        }

        // 2. Kiểm tra username chỉ bao gồm chữ và số
        isAlphaNumeric(username);
        return true;
    }

    // Hàm kiểm tra tên đăng nhập tồn tại trong ArrayList<User> userList
    private static int checkExistUsername(String username) throws Exception{
        int indexUsername = -1;
        for (int i = 0; i < userList.size(); i++){
            if (userList.get(i).getUsername().equalsIgnoreCase(username)){
                indexUsername = i;
                break;
            }
        }
        if (indexUsername < 0){
            throw new Exception("sai tên đăng nhập");
        }
        return indexUsername;
    }

    // Hàm kiểm tra tên đăng nhập tồn tại khi Đăng ký user mới
    private static boolean checkUsernameDuplicate(String username) throws Exception{
        for (User u : userList){
            if (u.getUsername().equalsIgnoreCase(username)){
                throw new Exception("tên đăng nhập đã tồn tại");
            }
        }
        return true;
    }

    // 2. CÁC HÀM VỀ PASSWORD

    /* Hàm kiểm tra password hợp lệ. Password mạnh thỏa mãn:
            1) Độ dài: ít nhất 8 ký tự.
            2) Ký tự hoa: Có ít nhất 1 chữ viết hoa (A - Z)
            3) Ký tự thường: Có ít nhất 1 chữ viết thường (a - z)
            4) Chữ số: Có ít nhất 1 con số (0 - 9)
            5) Ký tự đặc biệt: Có ít nhất 1 ký tự đặc biệt sau: !@#$%^&*()
         */
    private static boolean checkPasswordValidation(char[] password) throws Exception{
        // 1. Kiểm tra rỗng và độ dài
        if(password == null || password.length < 8 ){
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
        if(!(hasUpper && hasLower && hasDigit && hasSpecial)){
            throw new Exception("mật khẩu phải bao gồm: chữ thường, chữ hoa, số và ký tự đặc biệt !@#$%^&*()_+-=");
        }
        return true;
    }

    // Hàm xóa password: Thực hiện sau khi đăng nhập thành công hay thất bại
    private static void clearPassword(char[] password){
        if (password != null){
            Arrays.fill(password,'0');
        }
    }

    // Hàm input password
    private static char[] inputPassword(String prompt, int index) throws Exception{
        int retries = 0;
        while(retries < MAX_RETRIES){
            System.out.print(prompt);
            char[] inputPassword = sc.nextLine().toCharArray();      // Đọc và chuyển String thành char[]
            if (index >=0){         // Chế độ LOGIN/UPDATE (Xác thực người cũ)
                if (Arrays.equals(userList.get(index).getPassword(), inputPassword)){
                    return inputPassword;       // Mật khẩu đúng
                }else {
                    retries++;
                    clearPassword(inputPassword);
                    if (retries == MAX_RETRIES){
                        System.out.println("Quá số lần nhập!");
                        return null;
                    }
                    System.out.println("Sai mật khẩu! Còn " + (MAX_RETRIES - retries) + " lần thử.");
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

    // 3. CÁC HÀM VỀ FULLNAME, GIVENAME, SURNAME

    // Hàm kiểm tra họ tên có rổng hay không
    private static boolean checkNameEmpty (String name) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("họ tên không được để trống");
        } else {
            return true;
        }
    }

    // Hàm chuẩn hóa chuỗi hoTen
    private static String standardizeName (String name){
        String[] words = name.split("\\s+");                //Tách từ theo khoảng trắng
        StringBuilder result = new StringBuilder();        //Khai báo chuỗi kết quả
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
    private static String inputFullName (String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try{
                System.out.print(prompt);
                String name = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Fullname cũ (chạy trong hàm UpdateUser)
                if(isUpdate && name.isEmpty()){
                    return "";
                }
                // Nếu chạy trong hàm registerUser
                checkNameEmpty(name);
                return standardizeName(name);
            }catch (Exception e){
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }

    // Hàm tách chuỗi fullName = surname + givenName
    private static String[] splitFullName (String fullName){
        String[] nameParts = new String[2];
        // givenName = resutl[0]
        // surname = nameParts[1]
        if (fullName.lastIndexOf(" ") > 0) {
            nameParts[GIVEN_NAME] = fullName.substring(fullName.lastIndexOf(" ")+1);
            nameParts[SURNAME] = fullName.substring(0,fullName.lastIndexOf(" "));
        }else {
            nameParts[GIVEN_NAME] = fullName;
            nameParts[SURNAME] = "";
        }
        return nameParts;
    }


    // 4. CÁC HÀM VỀ DATE

    // Hàm kiểm tra ngày sinh hợp lệ
    private static void checkDateValidation(String date) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try{
            sdf.parse(date);
        } catch (Exception e) {
            throw new Exception("ngày sinh không đúng định dạng dd/MM/yyyy hoặc ngày không có thực. ");
        }
    }

    // Hàm nhập ngày sinh
    private static String inputBirthday(String prompt, boolean isUpdate){
        while (true){
            try{
                System.out.print(prompt);
                String birthday = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên birthday cũ (chạy trong hàm UpdateUser)
                if(isUpdate && birthday.isEmpty()){
                    return "";
                }
                // Nếu chạy trong hàm registerUser
                checkDateValidation(birthday);
                return birthday;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 5. CÁC HÀM VỀ CCCD

    // Hàm kiểm tra CCCD hợp lệ: 12 sô, không khoảng trắng, không chữ, không ký tự đặc biệt
    private static boolean checkCitizenIdValidation(String citizenId) throws Exception{
        // 1. Kiểm tra độ dài phải là 12 ký tự
        if (citizenId.length() != 12){
            throw new Exception("số CCCD phải đúng 12 số. ");
        }

        // 2. Kiểm không có: chữ, khoảng trắng hoặc ký tự đặc biệt
        for(char c: citizenId.toCharArray()){
            if (!(Character.isDigit(c))){
                throw new Exception("số CCCD không được chứa: khoảng trắng, chữ, ký tự đặc biệt. ");
            }
        }
        return true;
    }

    // Hàm nhập CCCD
    private static String inputCitizenId(String prompt, boolean isUpdate){
        while (true){
            try{
                System.out.print(prompt);
                String citizenId = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên ID cũ (chạy trong hàm UpdateUser)
                if(isUpdate && citizenId.isEmpty()){
                    return "";
                }
                // Nếu chạy trong hàm registerUser
                checkCitizenIdValidation(citizenId);
                return citizenId;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 6. CÁC HÀM VỀ SỐ ĐIỆN THOẠI

    // Hàm kiểm tra số diện thoại hợp lệ
    private static boolean checkPhoneValidation(String phone) throws Exception {
        String regex = "^(\\+84|0)[1-9]{1}[0-9]{8}$";
        if (!(phone.matches(regex))){
            throw new Exception("số ĐT không hợp lệ. ");
        }
        return true;
    }

    // Hàm nhập số điện thoại
    private static String inputPhone(String prompt, boolean isUpdate){
        while (true){
            try{
                System.out.print(prompt);
                String phone = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên phone cũ (chạy trong hàm UpdateUser)
                if(isUpdate && phone.isEmpty()){
                    return "";
                }
                // Nếu chạy trong hàm registerUser
                checkPhoneValidation(phone);
                return phone;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }

    // 7. CÁC HÀM VỀ ĐỊA CHỈ
    // Hàm nhập địa chỉ
    private static String inputAddress (String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try{
                System.out.print(prompt);
                String address = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Address cũ (chạy trong hàm UpdateUser)
                if(isUpdate && address.isEmpty()){
                    return "";
                }
                // Nếu chạy trong hàm registerUser
                return address;
            }catch (Exception e){
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }

    // 8. CÁC HÀM VỀ GIỚI TÍNH

    // Hàm nhập giới tính
    private static int inputGender(String prompt, boolean isUpdate){
        while (true){
            try{
                System.out.print(prompt);
                String input = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên giới tính cũ (chạy trong hàm UpdateUser)
                if(isUpdate && input.isEmpty()){
                    return -1;
                }
                // Nếu chạy trong hàm registerUser
                int genderChoice = Integer.parseInt(input);
                if (genderChoice == 0 || genderChoice ==1){
                    return genderChoice;
                }
            } catch (Exception e) {
                System.out.print("Lỗi, vui lòng nhập lại.");
            }
        }
    }

    // Hàm chuyển giới tính từ kiểu int sang boolean
    private static boolean convertGenderToBoolean(int tempGender){
        if (tempGender == 1){
            return true;
        }else {
            return false;
        }
    }


    // 9. CÁC HÀM HỖ TRỢ KHÁC

    // Hàm hỏi người dùng có tiếp tục không?
    private static int askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            if (input.isEmpty()) {
                continue;
            }
            char answer = input.charAt(0);
            if (answer == 'y' || answer == 'Y') {
                return 1;
            }
            if (answer == 'n' || answer == 'N') {
                return 0;
            }
            System.out.println("Vui lòng chỉ nhập Y hoặc N. ");
        }
    }


    // 10. CÁC HÀM LOGIN, LOGOUT

    // Hàm cho người dùng đăng nhập
    public static boolean loginWithUsername(String username) throws Exception{
        // 1. Kiểm tra username tồn tại trên hệ thống
        int index = -1;
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUsername().equalsIgnoreCase(username)) {
                index = i;
                break;
            }
        }

        // 2. Kiểm tra không tìm được username
        if (index < 0) {
            System.out.println("\u274C Lỗi: Tài khoản '" + username + "' chưa được đăng ký trong hệ thống.");
            return false;
        }

        // 3. Khai báo biến số lần nhập
        int retries = 0;

        // 4. Quét vòng while để nhập và kiểm tra password
        while (retries < MAX_RETRIES) {
            System.out.print("Nhập password (Còn " + (MAX_RETRIES - retries) + " lần thử): ");
            char[] inputPassword = sc.nextLine().toCharArray();

            // 5. Đối soát mảng char[] mật khẩu trên bộ nhớ RAM đệm
            if (Arrays.equals(userList.get(index).getPassword(), inputPassword)) {

                // 6. TRẠM GÁC AN NINH KHÓA TÀI KHOẢN (SOFT-DELETE): Đặt sau khi pass thành công [🛡️]
                if (userList.get(index).getStatus() == BLOCK_STATUS) {
                    System.out.println("\u26A0\uFE0F TRUY CẬP BỊ TỪ CHỐI: Tài khoản của " +
                            userList.get(index).getGivenName() + " hiện đã bị ĐÓNG BĂNG!");
                    System.out.println("👉 Vui lòng liên hệ Admin cấp cao để giải quyết.");
                    clearPassword(inputPassword);   // Xóa mật khẩu lập tức chống lộ mã
                    return false;
                }

                // 7. Vượt qua tất cả trạm gác -> Xác lập phiên làm việc thành công
                currentUserId = userList.get(index).getId();
                System.out.println("\u2705 Chào mừng bạn " + userList.get(index).getGivenName()
                        + " đăng nhập thành công!");
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

        // 9. Đăng nhập thất bại sau 3 lần
        System.out.println("\u26A0\uFE0F ĐĂNG NHẬP THẤT BẠI: Bạn đã gõ sai mật khẩu quá " + MAX_RETRIES +
                " lần cho phép!");
        return false;
    }


    // Hàm cho người dùng đăng xuất
    public static void logoutUser(){
        if (currentUserId == 0){
            System.out.println("Bạn chưa đăng nhập, nên không thể đăng xuất.");
            return;
        }

        // 1. Gỡ cờ
        currentUserId = 0;

        // 2. Xuất thông báo
        System.out.println("Cám ơn bạn đã sử dụng chương trình QUẢN LÝ THƯ VIỆN. Hẹn gặp bạn lần sau.");
    }


    // 11. CÁC HÀM VỀ READ - WRITE FILE

    // Hàm ghi danh sách users vào file
    public static void saveListToFile(ArrayList<User> userList, String filePath) {
        File folder = new File("data");
        if (!folder.exists()) folder.mkdir(); // Tự tạo thư mục data nếu chưa có.

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))){
            oos.writeObject(userList);
            System.out.println("Đã lưu thông tin.");
        } catch (IOException e) {
            System.out.println("Lỗi ghi file " + e.getMessage());
        }
    }

    // Hàm đọc file ra danh sách user ArrayList<User>
    public static ArrayList<User> readFileToList(String filePath) {
        ArrayList<User> tempList = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))){
            tempList = (ArrayList<User>) ois.readObject();

            // Cập nhật lại nextId
            if (!tempList.isEmpty()){
                int maxId = 0;
                for (User u: tempList){
                    if (u.getId() > maxId){
                        maxId = u.getId();          // tìm Id lớn nhất
                    }
                }
                nextId = maxId + 1;                 // Gán cho Id tiếp theo
            }
        } catch (FileNotFoundException e) {
            System.out.println("Khởi tạo dữ liệu mới.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempList;
    }

    // 12. CÁC HÀM LẤY USER VÀ XUẤT USER, XUẤT USER_LIST
    // Hàm lấy User từ currentId
    public static User getCurrentUser(){
        for (User u: userList){
            if(u.getId() == currentUserId){
                return u;
            }
        }
        return null;
    }

    // Hàm xuất danh sách UserList
    public static void printUserList () {
        for (int i = 0; i < userList.size(); i++) {
            System.out.print((i + 1) + ") ");
            System.out.print(userList.get(i));
        }
    }

    // Hàm đăng ký User mới
    public static void registerUser() throws Exception {
        int i = 1;
        while (true) {
            System.out.println("NGƯỜI DÙNG THỨ " + i);
            try {
                // 1. Nhập username
                System.out.print("Tên đăng nhập (ENTER để thoát): ");
                String username = sc.nextLine();
                if (username.trim().isEmpty()) {
                    System.out.println("Kết thúc đăng ký người dùng.");
                    break;
                }
                checkUsernameValidation(username);          // Kiểm tra họ tên hợp lệ
                checkUsernameDuplicate(username);           // Tên đăng nhập đã tồn tại

                // 2. Nhập password
                char[] password;
                while(true){
                    try{
                        System.out.print("Mật khẩu: ");
                        String tempPassword = sc.nextLine();
                        password = tempPassword.toCharArray();      // Chuyển String thành char[]
                        checkPasswordValidation(password);          // Nếu lỗi password sẽ nhảy xuống catch
                        break;                                      // Nếu passwprd OK sẽ thoát vòng lập
                    } catch (Exception e) {
                        System.out.println("Lỗi " + e.getMessage());
                    }
                }

                // 3. Nhập họ tên
                String fullname = inputFullName("Họ tên: ", false);   // Nhập họ tên + chuẩn hóa họ tên
                String[] nameParts = splitFullName(fullname);
                String givenName = nameParts[GIVEN_NAME];
                String surname = nameParts[SURNAME];

                // 4. Nhập ngày sinh
                String birthDay = inputBirthday("Ngày sinh: ", false); // có kiểm tra ngày hợp lệ

                // 5. Nhập CCCD
                String citizenId = inputCitizenId("CCCD: ", false);

                // 6. Nhập số điện thoại
                String phone = inputPhone("Số điện thoại: ", false);

                // 7. Nhập địa chỉ
                String address = inputAddress("Địa chỉ: ", false);

                // 8. Nhập giới tính
                int tempGender = inputGender("Giới tính (1: nam; 0: nữ): ",false);
                boolean gender = convertGenderToBoolean(tempGender);

                // 9. Tạo đối tượng và nạp vào danh sách (Sử dụng nextId, mặc định true cho status, mặc định ROLE_USER)
                User newUser = new User(
                        nextId++,          // ID tự động tăng
                        username,
                        Arrays.copyOf(password,password.length),         // Mảng char[] mật khẩu
                        surname,
                        givenName,
                        birthDay,
                        citizenId,
                        phone,
                        address,
                        gender,           // boolean đã convert
                        ROLE_USER,
                        true            // status mặc định là active
                );

                // 10. Lưu vào List và File
                addUser(newUser);                                       // Lưu vào ArrayList
                saveListToFile(userList,"data/users.dat");      // Save to file

                // 11. Bảo mật: Xóa mảng mật khẩu tạm sau khi đã lưu xong
                clearPassword(password);

                // 12. In thông tin gửi qua zalo
                printStaffNotificationTemplate(newUser, "ĐĂNG KÝ KHỞI TẠO MỚI");

                // Tăng số thứ tự hiển thị cho người tiếp theo
                i++;

            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 13. HÀM CẬP NHẬT THÔNG TIN USER, ĐỔI PASSWORD
    // Hàm cập nhật thông tin người dùng
    public static void updateUser() throws Exception{
        User targetUser = getCurrentUser();
        System.out.println(targetUser.toString());
        if (targetUser != null){
            // 1. Cập nhật họ tên
            System.out.println("Họ tên: " + targetUser.getSurname() + " " + targetUser.getGivenName());
            String newFullname = inputFullName("Nhập họ tên mới (Enter nếu bỏ qua): ", true);
            if(!newFullname.trim().isEmpty()){
                newFullname = standardizeName(newFullname);
                String[] nameParts = splitFullName(newFullname);
                targetUser.setSurname(nameParts[SURNAME]);
                targetUser.setGivenName(nameParts[GIVEN_NAME]);
            }

            // 2. Cập nhật ngày sinh
            System.out.print("Ngày sinh : " + targetUser.getBirthday() + ". ");
            String newBirthday = inputBirthday("Nhập ngày sinh mới 'dd/MM/yyyy' (Enter nếu bỏ qua): ", true);
            if (!newBirthday.isEmpty()){
                targetUser.setBirthday(newBirthday);
            }

            // 3. Nhập CCCD
            System.out.print("CCCD : " + targetUser.getCitizenID() + ". ");
            String newCitizenId = inputCitizenId("Nhập CCCD mới (Enter nếu bỏ qua): ",true);
            if(!newCitizenId.trim().isEmpty()){
                targetUser.setCitizenID(newCitizenId);
            }

            // 4. Nhập số điện thoại
            System.out.print("Số ĐT : " + targetUser.getPhone() + ". ");
            String newPhone = inputPhone("Nhập số ĐT mới (Enter nếu bỏ qua): ",true);
            if(!newPhone.trim().isEmpty()){
                targetUser.setPhone(newPhone);
            }

            // 5. Nhập địa chỉ
            System.out.println("Địa chỉ : " + targetUser.getAddress());
            String newAddress = inputAddress("Nhập địa chỉ mới (Enter nếu bỏ qua): ", true);
            if(!newAddress.trim().isEmpty()){
                targetUser.setAddress(newAddress);
            }

            // 6. Nhập giới tính
            System.out.print("Giới tính : " + ((targetUser.getGender() ? "nam" : "nữ") + ". "));
            int newGender = inputGender("Nhập giới tính mới (1: nam; 0: nữ), Enter nếu bỏ qua: ",true);
            if(newGender !=-1){
                targetUser.setGender(convertGenderToBoolean(newGender));
            }

            // 7. Lưu vào file
            saveListToFile(userList,"data/users.dat");     // Save to file

            // 8. Thông báo
            printStaffNotificationTemplate(targetUser, "CẬP NHẬT THÔNG TIN HỒ SƠ");

        }else {
            System.out.println("Bạn chưa đăng nhập, nên không thể cập nhật thông tin tài khoản.");
        }
    }

    // Hàm đổi password
    public static void changePassword(){
        User currentUser = getCurrentUser();
        if(currentUser == null){
            return;
        }
        int loginAttempts = 0;
        while (loginAttempts < MAX_RETRIES){
            // 1. Xác thực mật khẩu cũ
            try{
                System.out.print("Mật khẩu cũ: ");
                char[] oldPassword = sc.nextLine().toCharArray();      // Chuyển String thành char[]
                if (!(Arrays.equals(oldPassword,currentUser.getPassword()))){
                    clearPassword(oldPassword);
                    loginAttempts++;
                    System.out.println("Xác thực thất bại. Bạn còn " + (MAX_RETRIES - loginAttempts) + " lần");
                    continue;
                }
                clearPassword(oldPassword);

                // 2. Nhập mật khẩu mới, dùng vòng lập riêng để không tính vào loginAttempts
                while (true){
                    try{
                        System.out.print("Mật khẩu mới: ");
                        char[] newPass_1 = sc.nextLine().toCharArray();      // Chuyển String thành char[]
                        checkPasswordValidation(newPass_1);                  // Kiểm tra tính hợp lệ password

                        System.out.print("Xác thực mật khẩu mới: ");
                        char[] newPass_2= sc.nextLine().toCharArray();      // Chuyển String thành char[]

                        // Kiểm tra newPassword nhập 2 lần trùng nhau
                        if(!(Arrays.equals(newPass_1,newPass_2))){
                            clearPassword(newPass_1);
                            clearPassword(newPass_2);
                            loginAttempts++;
                            System.out.println("Lỗi: mật khẩu xác nhận không khớp. Hãy thử lại. ");
                            continue;
                        }

                        // 3. Cập nhật mật khẩu thành công
                        currentUser.setPassword(Arrays.copyOf(newPass_1,newPass_1.length));
                        clearPassword(newPass_1);
                        clearPassword(newPass_2);
                        saveListToFile(userList,"data/users.dat");     // Save to file
                        System.out.println("=> Đổi mật khẩu thành công.");
                        return;
                    } catch (Exception e) {
                        System.out.println("Lỗi định dạng " + e.getMessage());
                        // Cho phép nhập mật khẩu mới mà không phải nhập lại mật khẩu cũ
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi hệ thống " + e.getMessage());
            }
        }
        System.out.println("Bạn đã nhập sai quá nhiều lần, chức năng đổi mật khẩu bị khóa!");
    }


    // 14. CÁC HÀM VỀ PHÂN QUYỀN VÀ QUẢN LÝ TRẠNG THÁI NGƯỜI DÙNG
    public static void manageUserAuthorization(){
        // 1. Kiểm tra xem người đang thao tác có phải Admin không?
        User currentUser = getCurrentUser();
        if(currentUser == null || currentUser.getRole() != ROLE_ADMIN){
            System.out.println("Lỗi: chỉ có Admin mới có quyền thực thi chức năng này!");
            return;
        }

        // 2. Hiển thị danh sách để Admin chọn
        System.out.println("\n--------- DANH SÁCH NGƯỜI DÙNG HIỆN TẠI ----------");
        printUserList();

        try{
            System.out.println("Nhập ID người dùng cần chỉnh sửa (Enter để thoát): ");
            String inputId = sc.nextLine().trim();
            if (inputId.isEmpty()){
                return;
            }
            int id = Integer.parseInt(inputId);

            // Khởi tạo User mới
            User targetUser = null;

            // Tìm User trong List dựa trên Id vừa nhập
            for(User u: userList){
                if (u.getId() == id){
                    targetUser = u;         // Gán User mới = user có Id nhập vào
                    break;
                }
            }

            if(targetUser == null){
                System.out.println("=> Không tìm thấy người dùng có ID = " + id);
                return;
            }

            // Loại trừ khả năng Admin sơ suất tự thay đổi trạng thái của mình
            if(targetUser.getId() == currentUser.getId()){
                System.out.println("Bạn không thể tự thay đổi quyền hoặc trạng thái của chính mình!");
                return;
            }

            // 3. Menu tùy chọn chỉnh sửa
            System.out.println("Đang chọn: " + targetUser.getSurname() + " " + targetUser.getGivenName());
            System.out.println("1. Thay đổi quyền (Role)");
            System.out.println("2. Thay đổi trạng thái (Khóa / Mở khóa)");
            System.out.println("Chọn chức năng (1 hoặc 2): ");
            int action = Integer.parseInt(sc.nextLine());

            // 5. Thực thi chức năng Thay đổi quyền (role)
            if(action == 1){
                System.out.println("Quyền hiện tại: " + targetUser.getRole());
                System.out.println("Chọn quyền mới (1: Admin; 2: Manager; 3: User): ");
                int newRole = Integer.parseInt(sc.nextLine());
                if (newRole >=1 && newRole <=3){
                    targetUser.setRole(newRole);
                    System.out.println("=> Cập nhật quyền thành công");
                }else {
                    System.out.println("Lựa chọn quyền không hợp lệ!");
                }
            } else if (action ==2) {        // Thực thi chức năng thay đổi trạng thái
                // Đảo trạng thái: Nếu đang true --> false và ngược lại
                targetUser.setStatus(!targetUser.getStatus());
                System.out.println("=> Trạng thái mới " + (targetUser.getStatus() ? "KÍCH HOẠT" : "ĐÃ KHÓA"));
            }

            // 4. Lưu thay đổi vào file ngày ập tức
            saveListToFile(userList,"data/users.dat");     // Save to file

            // 5. Xuất thông báo
            printStaffNotificationTemplate(targetUser, "THAY ĐỔI PHÂN QUYỀN HỆ THỐNG");

        } catch (NumberFormatException e) {
            System.out.println("Lỗi: vui lòng chỉ nhập số!");
        }catch (Exception e){
            System.out.println("Có lỗi xãy ra " + e.getMessage());
        }
    }


    // 15. HÀM TÌM USER THEO USERNAME HOẶC ID
    public static User findUserByUsernameOrId(String keyword)throws Exception{
       // 1. Kiểm tra chuỗi rỗng hoặc null
       if(keyword == null || keyword.isEmpty()){
           return null;
       }

       // 2. Chuẩn hóa chuỗi nhập
       String cleanKeyword = keyword.trim().toUpperCase();

       // 3. Tìm kiếm user
       for(User currentUser: userList){
           // 4. Kiểm tra User rỗng hoặc Username rỗng hoặc Id rỗng
           if(currentUser == null || currentUser.getUsername() == null || currentUser.getId() <=0 ){
               continue;
           }

           //5. Tìm User theo username
           if(currentUser.getUsername().toUpperCase().equals(cleanKeyword)){
               return currentUser;
           }

           // 6. Tìm User theo Id
           // 6.1. Kiểm tra chuỗi nhập phải toàn là số
           if(cleanKeyword.matches("\\d+")){
               // 6.2. Ép kiểu dữ liệu từ String sang int
               int searchId = Integer.parseInt(cleanKeyword);

               // 6.3. So sánh Id để tìm User
               if(currentUser.getId() == searchId){
                   return currentUser;
               }
           }
       }
       return null;
    }


    // 16. HÀM KHÓA/MỞ KHÓA TÀI KHOẢN NHÂN SỰ (SOFT-DELETE)
    public static void BlockOrUnblockUser(){
        while (true){
            try{
                // 1. Nhập UserId
                System.out.print("Nhập Username hoặc ID của nhân viên cần Khóa/Mở khóa tài khoản (ENTER để thoát): ");
                String input = sc.nextLine().trim();

                // 2. Kiểm tra dữ liệu nhập rỗng
                if (input.isEmpty()) {
                    System.out.println("👉 Đã hủy thao tác Khóa/Mở khóa tài khoản.");
                    return;
                }

                // 3. Tìm kiếm OOP User
                User targetUser = findUserByUsernameOrId(input);

                // 4. Kiểm tra kết quả tìm kiếm null
                if (targetUser == null) {
                    System.out.println("\u274C Lỗi: Tài khoản nhân viên '" + input + "' không tồn tại trên hệ thống!");
                    continue; // Quay lại đầu vòng lặp cho phép nhập lại an toàn
                }

                // 5. CHỐT CHẶN AN NINH TỐI CAO: Chặn Admin tự khóa chính mình gây treo hệ thống
                if(targetUser.getId() == currentUserId){
                    System.out.println("\u26A0\uFE0F LỖI BẢO MẬT: Bạn tuyệt đối không được phép tự Khóa/Mở khóa " +
                            "tài khoản của chính mình!");
                    continue;
                }

                // 6. Hiển thị thông tin và xác nhận trước khi đóng băng tài khoản (Y/N)
                System.out.println("--- THÔNG TIN TÀI KHOẢN MỤC TIÊU ---");
                System.out.println("Username: " + targetUser.getUsername() + " | Quyền hạn: " + targetUser.getRole());
                System.out.println("Họ tên: " + targetUser.getSurname() + " " + targetUser.getGivenName());
                System.out.println("Số điện thoại: " + targetUser.getPhone());
                System.out.println("Trạng thái hiện tại: " + (targetUser.getStatus() ? "ĐANG HOẠT ĐỘNG" : "ĐÃ KHÓA"));

                // 7. Kiểm tra trạng thái tài khoản trước khi hỏi xác nhận KHÓA/MỞ KHÓA TÀI KHOẢN
                if(targetUser.getStatus() == BLOCK_STATUS){
                    // 8. KỊCH BẢN 1: MỞ KHÓA
                    int checkOpen = askYesNo("Tài khoản này đang bị khóa. Bạn có muốn MỞ KHÓA (Unblock)" +
                            " không? (Y/N): ");

                    if(checkOpen == 1){
                        targetUser.setStatus(ACTIVE_STATUS);
                        saveListToFile(userList, "data/users.dat");     // Lưu đồng bộ ổ cứng
                        printStaffNotificationTemplate(targetUser, "MỞ KHÓA PHỤC HỒI QUYỀN TRUY CẬP");
                    }else {
                        System.out.println("\u274C Hủy thao tác mở khóa tài khoản");
                    }
                }else {         // targetUser.getStatus() == ACTIVE_STATUS
                    // 9. KỊCH BẢN 2: KHÓA
                    int checkBlock = askYesNo("Tài khoản này đang hoạt động bình thường. " +
                            "Bạn có chắc chắn KHÓA (Block) không? (Y/N): ");

                    if(checkBlock == 1){
                        targetUser.setStatus(BLOCK_STATUS);
                        saveListToFile(userList, "data/users.dat");     // Lưu đồng bộ ổ cứng
                        printStaffNotificationTemplate(targetUser, "ĐÓNG BĂNG VÔ HIỆU HÓA TÀI KHOẢN " +
                                "NGHIỆP VỤ");
                    }else {
                        System.out.println("\u274C Hủy thao tác Khóa tài khoản");
                    }
                }

                // 10. Hỏi tiếp tục thực hiện Khóa/Mở khóa tài khoản
                if (askYesNo("Bạn có muốn tiếp tục xử lý tài khoản nhân viên khác không? (Y/N): ") == 0) {
                    break;
                }

            } catch (Exception e) {
                System.out.println("\u274C Có lỗi xảy ra trong tiến trình khóa tài khoản Người dùng: " + e.getMessage());
            }
        }
    }


    // 17. HÀM TIỆN ÍCH IN THÔNG ĐIỆP GỬI NHANH QUA ZALO/EMAIL CHO NHÂN VIÊN TRONG HỆ THỐNG
    public static void printStaffNotificationTemplate(User u, String actionType) {
        // 1. Kiểm tra User null
        if (u == null) return;

        // 2. Truy xuất họ tên và vai trò của User
        String fullname = u.getSurname() + " " + u.getGivenName();
        String roleStr = (u.getRole() == ROLE_ADMIN) ? "Quản trị viên (Admin)" :
                (u.getRole() == ROLE_MANAGER) ? "Quản lý (Manager)" : "Nhân viên (Staff)";

        // 3. Xuất thông tin tóm lược
        System.out.println("\n====================================================================");
        System.out.println("[THƯ VIỆN ĐIỆN TỬ - THÔNG BÁO TIẾN TRÌNH HỒ SƠ TÀI KHOẢN NHÂN SỰ]");
        System.out.println("--------------------------------------------------------------------");
        System.out.println("Kính gửi Nhân viên: " + fullname + " (Mã ID nội bộ: " + u.getId() + ")");
        System.out.println("Hệ thống ghi nhận tài khoản công tác của bạn đã được " + actionType + " thành công!");
        System.out.println("- Tài khoản đăng nhập : " + u.getUsername());
        System.out.println("- Vai trò phân quyền  : " + roleStr);
        System.out.println("- Trạng thái làm việc : " + (u.getStatus() == BLOCK_STATUS ? "TÀI KHOẢN ĐÃ BỊ ĐÓNG BĂNG " +
                "(ĐÃ NGHỈ VIỆC) ❌" : "ĐANG HOẠT ĐỘNG BÌNH THƯỜNG ✨"));
        System.out.println("--------------------------------------------------------------------");
        System.out.println("👉 Vui lòng bảo mật thông tin tài khoản và thực thi đúng thẩm quyền.");
        System.out.println("Ban Giám đốc Thư viện trân trọng thông báo!");
        System.out.println("====================================================================\n");
    }


    // 18. HÀM TỰ ĐỘNG NẠP DỮ LIỆU USER ĐỂ TEST
    public static void seedUserData(){
        // 1. Chỉ nạp danh sách trống để tránh trùng lập
        if (userList.isEmpty()){
            // Tạo User 1: Admin
            userList.add(new User(nextId++, "admin123", "Admin@1234".toCharArray(),
                    "Nguyễn", "Quản Trị", "01/01/1990", "123456789012",
                    "0901234567", "Hồ Chí Minh", true, ROLE_ADMIN, true));
            // Tạo User 2: Thủ thư
            userList.add(new User(nextId++, "manager01", "Manager@123".toCharArray(),
                    "Trần", "Thủ Thư", "15/05/1992", "098765432109",
                    "0912345678", "Hà Nội", false, ROLE_MANAGER, true));
            // Tạo User 3: Người dùng bình thường
            userList.add(new User(nextId++, "usertest", "User@12345".toCharArray(),
                    "Lê", "Văn Test", "20/10/2000", "001122334455",
                    "0355667788", "Đà Nẵng", true, ROLE_USER, true));
        }
        System.out.println("==> Đã nạp đủ 3 người dùng để TEST");
    }

}
