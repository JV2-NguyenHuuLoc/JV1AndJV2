package book;

import java.io.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.Scanner;

public class BookService implements Serializable {

    // KHAI BÁO BIẾN HẰNG PHÂN LOẠI SÁCH
    private static final int TEXT_BOOK = 1;             // Sách giáo khoa
    private static final int REFERENCE = 2;             // Sách tham khảo
    private static final int JOURNAL_OF_SCIENCE = 3;    // Tạp chí khoa học
    private static final int FICTION = 4;               // Sách văn học, giải trí
    private static final int OTHER = 5;

    // KHAI BÁO CÁC BIẾN HẰNG VỀ TÊN
    private static final int GIVEN_NAME = 0;
    private static final int SURNAME = 1;

    // KHAI BÁO BIẾN TOÀN CỤC
    private static final Scanner sc = new Scanner(System.in);
    private static final ArrayList<Book> bookList = new ArrayList<>();

    public static int nextBookId = 1;           // Cấp ID tự động sau khi Create Book thành công


    // PHƯƠNG THỨC PUBLIC STATIC ĐỂ LẤY readerList (Getter và Setter)
    // Phương thức Getter
    public static ArrayList<Book> getBookList() {
        return bookList;
    }

    // Phương thức Setter
    public static void addBook(Book book) {
        bookList.add(book);
    }


    // 1. CÁC HÀM THIẾT LẬP ID CHO SÁCH
    // Hàm nhập loại sách: SGK, STK, TCKH, Khác
    private static int inputCategoryType(String prompt) throws Exception{
        while (true) {
            try {
                System.out.print(prompt);
                String input = sc.nextLine().trim();
                if (!input.isEmpty()){
                    int catagoryType = Integer.parseInt(input);
                    if (catagoryType == TEXT_BOOK || catagoryType == REFERENCE || catagoryType == JOURNAL_OF_SCIENCE ||
                            catagoryType == FICTION || catagoryType == OTHER) {
                        return catagoryType;
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi, vui lòng nhập lại. ");
            }
        }
    }


    // Hàm chuyển loại Type từ int --> Code String để thêm vào ID
    private static String categoryToId(int categoryType) {
        return switch (categoryType){
            case TEXT_BOOK -> "SGK";            // Sách giáo khoa
            case REFERENCE -> "STK";            // Sách tham khảo
            case JOURNAL_OF_SCIENCE ->"TCKH";   // Tạp chí khoa học
            case FICTION -> "VHGT";             // Văn học, truyện, giải trí
            default -> "KH";                    // Thể loại khác
        };
    }

    // Hàm chuyển loại Type từ int --> String để xuất menu
    private static String categoryToStr(int categoryType) {
        return switch (categoryType){
            case TEXT_BOOK -> "Sách giáo khoa";
            case REFERENCE -> "Sách tham khảo";
            case JOURNAL_OF_SCIENCE ->"Tạp chí khoa học";
            case FICTION -> "Văn hóa, truyện, giải trí";
            default -> "Thể loại khác";
        };
    }


    // 2. CÁC HÀM VỀ ISBN
    // Hàm kiểm tra ISBN hợp lệ
    private static boolean checkIsbnValidation(String isbn) throws Exception{
        // 1. Kiểm tra chiều dài = 17
        if(isbn.length() != 17){
            System.out.println("chiều dài phải đúng 17 ký tự");
        }

        // 2. Kiểm tra tiền tố bắt đầu ISBN: 978- hoặc 979-
        if(!isbn.startsWith("978-") && !isbn.startsWith("979-")){
            System.out.println("tiền tố bắt đầu phải là 978- hoặc 979-");
        }

        // 3. Dùng Regex kiểm tra: Chỉ cho phép số và dấu gạch; Và phải có đúng 4 dấu gạch
        // Đếm dấu gạch bằng cách xóa hết sô đi
        int countMinus = isbn.replaceAll("[0-9]","").length();
        if(countMinus != 4){
            System.out.println("ISBN phải có đủ 4 dấu trừ (-)");
        }

        // 4. Kiểm tra ký tự lạ
        if(!isbn.matches("[0-9-]+")){
            System.out.println("chỉ được nhập số và dấu trừ (-)");
        }
        return true;
    }

    // Hàm nhập ISBN: International Standard Book Number.
    private static String inputIsbn(String prompt, boolean isUpdate) throws Exception{
        while (true){
            try {
                System.out.print(prompt);
                String isbn = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên ISBN cũ (chạy trong hàm updateBook)
                if (isUpdate && isbn.isEmpty()) {
                    return "";
                }

                // Nếu chạy trong hàm createBook --> Kiểm tra isbn hợp lệ
                checkIsbnValidation(isbn);
                return isbn.toUpperCase();
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage() + ". ");
            }
        }
    }


    // 3. CÁC HÀM NHẬP TÊN SÁCH
    private static String inputTitle(String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String title = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Title cũ (chạy trong hàm updateBook)
                if (isUpdate && title.isEmpty()) {
                    return "";
                }

                // Nếu chạy trong hàm createBook
                if (!isUpdate && title.isEmpty()) {
                    System.out.println("tên sách không được để trống.");
                        continue;
                }
                return title;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }


    // 4. HÀM NHẬP TẬP SÁCH .....
    private static String inputVolume(String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String volume = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Tập cũ (chạy trong hàm updateBook)
                if (isUpdate && volume.isEmpty()) {
                    return "";
                }
                // Nếu chạy trong hàm createBook
                return volume;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }


    // 5. CÁC HÀM VỀ FULLNAME, GIVENAME, SURNAME
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


    // 6. HÀM NHẬP TÊN TÁC GIẢ
    private static String inputFullName(String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String name = sc.nextLine().trim();

                // Nếu là Update và để trống: Người dùng giữ nguyên Fullname cũ (chạy trong hàm UpdateBook)
                if (isUpdate && name.isEmpty()) {
                    return "";
                }

                // Nếu chạy trong hàm createBook
                checkNameEmpty(name);
                return standardizeName(name);
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }


    // 7. HÀM NHẬP NHÀ XUẤT BẢN
    private static String inputPublisher(String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String publisher = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Nhà xuất bản cũ (chạy trong hàm updateBook)
                if (isUpdate && publisher.isEmpty()) {
                    return "";
                }
                // Nếu chạy trong hàm createBook
                return publisher;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }


    // 8.CÁC HÀM VỀ NĂM XUẤT BẢN
    // Hàm kiểm tra năm xuất bản hợp lệ <= năm hiện hành
    private static boolean checkYearValidation(Year year) throws Exception{
        if(year.getValue() > Year.now().getValue()){
            throw new Exception("năm xuất bản phải <= năm hiện tại");
        }
        return true;
    }

    // Hàm nhập năm xuất bản
    private static Year inputYearPublication(String prompt) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String input = sc.nextLine().trim();
                if (input.isEmpty()) {   // Trả về null để báo hiệu không đổi
                    System.out.println("Lỗi: năm không được để trống");
                    continue;
                }
                Year year = Year.parse(input);
                checkYearValidation(year);
                return year;
            } catch (Exception e) {
                System.out.println("Lỗi: định dạng năm không hợp lệ!");
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }

    // 9. HÀM TỰ ĐỘNG TĂNG ID CHO BOOK
    private static String autoCreateBookId(int categoryType, String booKIsbn, Year yearPublistion) {
        // 1. Lấy phần chữ dựa trên Type người dùng chọn
        String word = categoryToId(categoryType);

        // 2. Lấy isbn
        String isbn = booKIsbn.replaceAll("-","");

        //3. Lấy năm xuất bản
        String year = String.valueOf(yearPublistion.getValue());

        // 3. Cộng chuỗi lại
        String result = word + isbn + year;

        return result.toUpperCase();
    }


    // 10. HÀM NHẬP ĐƠN GIÁ SÁCH
    private static Double inputBookPrice(String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String input = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Đơn giá cũ (chạy trong hàm updateBook)
                if (isUpdate && input.isEmpty()) {
                    return -1.0;
                }

                // Nếu chạy trong hàm createBook
                Double originalPrice = Double.parseDouble(input);
                if(originalPrice < 0){
                    System.out.println("\u274C Lỗi: đơn giá không được âm!");
                    continue;
                }
                return originalPrice;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }


    // 11. HÀM NHẬP ĐƠN GIÁ CHO THUÊ SÁCH TRONG 1 NGÀY
    private static Double inputRentalPrice(String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String input = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Đơn giá cũ (chạy trong hàm updateBook)
                if (isUpdate && input.isEmpty()) {
                    return -1.0;
                }

                // Nếu chạy trong hàm createBook
                Double rentalPrice = Double.parseDouble(input);
                if(rentalPrice < 0){
                    System.out.println("\u274C Lỗi: đơn giá không được âm!");
                    continue;
                }
                return rentalPrice;
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
        }
    }


    // 12. HÀM NHẬP SỐ LƯỢNG SÁCH
    private static int inputQuantity(String prompt, boolean isUpdate) throws Exception {
        while (true) {
            try {
                System.out.print(prompt);
                String input = sc.nextLine().trim();
                // Nếu là Update và để trống: Người dùng giữ nguyên Đơn giá cũ (chạy trong hàm updateBook)
                if (isUpdate && input.isEmpty()) {
                    return -1;
                }
                // Nếu chạy trong hàm createBook
                int quantity = Integer.parseInt(input);
                if(quantity < 0){
                    System.out.println("Lỗi: số lượng không được âm!");
                    continue;
                }
                return quantity;
            } catch (Exception e) {
                System.out.println("\u274C Lỗi " + e.getMessage());
                // Vòng lập while sẽ tiếp tục cho phép nhập lại
            }
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


    // 14. CÁC HÀM VỀ READ - WRITE FILE
    // Hàm ghi danh sách bookList vào file
    public static void saveListToFile(ArrayList<Book> bookList, String filePath) {
        File folder = new File("data");
        if (!folder.exists()) folder.mkdir();       // Tự tạo thư mục data nếu chưa có.

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))){
            oos.writeObject(bookList);
        } catch (IOException e) {
            System.out.println("Lỗi ghi file " + e.getMessage());
        }
    }

    // Hàm đọc file ra danh sách ArrayList<Book> bookList
    public static ArrayList<Book> readFileToList(String filePath) {
        ArrayList<Book> tempList = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))){
            tempList = (ArrayList<Book>) ois.readObject();

        } catch (FileNotFoundException e) {
            System.out.println("Khởi tạo dữ liệu mới.");
        } catch (Exception e) {
            System.out.println("Lỗi nạp dữ liệu: " + e.getMessage());
        }
        return tempList;
    }


    // 15.CÁC HÀM IN DANH SÁCH BOOK_LIST
    // Hàm in toàn bộ danh sách Book
    public static void printBookList(){
        if (bookList.isEmpty()){
            System.out.println("\uD83D\uDCED Danh sách đang trống.");
            return;
        }else {
            System.out.println("\n---------- DANH SÁCH SÁCH TRONG THƯ VIỆN ----------");
            for(Book b: bookList){
                System.out.println(b);
            }
            System.out.println("============================================================");
        }
    }

    // Hàm in toàn bộ Book theo index
    public static void printBookByIndex (int index) throws Exception{
        try{
            if (index >=0 && index < bookList.size()){
                System.out.print(bookList.get(index));
            }else {
                System.out.println("\u26A0\uFE0F Chỉ số sách không hợp lệ.");
            }
        } catch (Exception e) {
            System.out.println("Có lỗi xãy ra " + e.getMessage());
        }
    }


    // 16. CÁC HÀM VỀ TÌM KIẾM SÁCH
    // HÀM TÌM BOOK DA HÌNH
    public static Book findBook(String keyword) throws Exception{
        try{
            // 1. Kiểm tra chuỗi rỗng
            if(keyword.isEmpty()){
                System.out.println("\u274C Lỗi chuỗi tìm kiếm rỗng.");
                return null;
            }

            // 2. Chuẩn hóa chuỗi nhập
            keyword = keyword.toUpperCase().trim();

            // 3. Khai báo biến tạm
            String keywordBookId = keyword;   // Mặc định giữ nguyên keyword nếu không cắt
            String keywordIsbn = keyword.replaceAll("-",""); // Cắt sẵn, phục vụ ISBN

            // 4. Kiểm tra chuỗi có chứa ít nhất 1 chữ cái không
            // Đồng thời check xem chuỗi có dính dấu gạch ngang phân tách vật lý không
            boolean hasLetter = keyword.matches(".*[A-Z].*");
            if(hasLetter && keyword.contains("-")){
                keywordBookId = keyword.split("-")[0];
            }

            // 5. Quét vòng for để tìm kếm đa hình
            for (Book book: bookList){
                // 5.1. Tìm theo booKId
                if (book.getId().equals(keywordBookId)){
                    return book;
                }

                // 5.2. Tìm theo ISBN
                if (book.getIsbn().replaceAll("-","").equals(keywordIsbn)){
                    return book;
                }

                // 5.3. Tìm theo tên sách
                if (book.getTitle().contains(keyword)){
                    return book;
                }
            }
        } catch (Exception e) {
            System.out.println("\u274C Lỗi sách chưa về thư viện." + e.getMessage());
        }
        return null;        // Không tìm thấy
    }


    // HÀM TÌM KIẾM ĐA NĂNG BOOK THEO: ID, ISBN, TÊN SÁCH
    public static int findBookIndexByKeyword(String prompt) throws Exception{
        System.out.println(prompt);
        String keyword = sc.nextLine().trim();

        // 1. Gọi hàm tìm kiếm đa hình để lấy OOP đóng gói
        Book book = findBook(keyword);

        // 2. Kiểm tra không tìm thấy OOP book
        if (book == null){
            return -1;
        }

        // 3. Sử dụng hàm của java ể truy xuất ra index
        return bookList.indexOf(book);
    }


    // HÀM TÌM OOP BOOK TỪ ISBN
    public static Book findBookByIsbn(String isbn) throws Exception{
        return findBook(isbn);
    }


    // HÀM TÌM OOP BOOK TỪ bookId
    public static Book findBookById(String bookId) throws Exception{
        return findBook(bookId);
    }


    // 17. HÀM CREATE_BOOK

    public static void createBook() throws Exception{
        while (true){
            try {
                // 1. Nhập thể loại Sách
                String menu = "Chọn loại sách:\n" +
                        "1. SGK (Sách giáo khoa)\n" +
                        "2. STK (Sách tham khảo)\n" +
                        "3. TCKH (Tạp chí khoa học)\n" +
                        "4. VHGT (Văn hóa, truyện, giải trí)\n" +
                        "5. KH (Thể loại khác)" +
                        "Lựa chọn của bạn: ";
                int categoryType = inputCategoryType(menu);

                // 2. Nhập ISBN
                String isbn = inputIsbn("ISBN: ", false);

                // 3. Nhập tên sách
                String title = inputTitle("Tên sách: ", false);

                // 4. Nhập tập 1, .....
                String volume = inputVolume("Tập (Enter nếu sách chỉ có 1 tập duy nhất): ", false);

                // 5. Nhập Tác giả
                String author = inputFullName("Tác giả: ", false);
                String[] nameParts = splitFullName(author);
                String givenName = nameParts[GIVEN_NAME];
                String surname = nameParts[SURNAME];

                // 6. Nhập Nhà xuất bản
                String publisher = inputPublisher("Nhà xuất bản: ", false);

                // 7. Nhập năm xuất bản
                Year yearPublication = inputYearPublication("Năm xuất bản: ");

                // 8. Tạo tự động ID cho sách
                String id = autoCreateBookId(categoryType, isbn,yearPublication);

                // 9. Nhập thể loại sách
                String category = categoryToStr(categoryType);

                // 10. Nhập đơn giá sách
                Double originalPrice = inputBookPrice("Đơn giá sách: ", false);

                // 11. Nhập đơn giá cho thuê 1 ngày
                Double rentalPrice = inputRentalPrice("Đơn giá cho thuê 1 ngày: ", false);

                // 12. Nhập số lượng
                int quantity = inputQuantity("Số lượng sách: ", false);

                // 13. Tính số lượng sách còn tồn
                int availableQuantity = quantity;

                // 14. Tạo đối tượng và nạp vào danh sách bookList: Sử dụng nextBookId
                Book newBook = new Book(
                        id,                         // ID tự động tăng
                        isbn,
                        title,
                        volume,
                        surname + " " + givenName,
                        publisher,
                        yearPublication,
                        category,
                        originalPrice,
                        rentalPrice,
                        quantity,           // boolean đã convert
                        availableQuantity
                );

                addBook(newBook);                                       // Lưu vào ArrayList
                saveListToFile(bookList,"data/books.dat");      // Save to file

                // 15. Tạo tự động danh sách Sách vật lý bookInstanceList và lưu file
                // 15.1. Tạo tự động danh sách Sách vật lý bookInstanceList
                BookInstanceService.createBookInstance(id,quantity);
                // 15.2. Lưu danh sách Sách vật lý ra file
                BookInstanceService.saveBookInstanceListToFile();

                // 16. Hiển thị thông báo
                System.out.println("Đã tạo sách thành công.");

                // 17. Hỏi có tiếp tục nhập Book khác nữa không?
                if (askYesNo("Bạn có mướn tiếp tục tạo thêm Sách mới không (Y/N): ") == 0){
                   break;
                }
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 18. HÀM CẬP NHẬT BOOK

    public static void updateBook() throws Exception{
        while (true){
            try{
                // 1. Tìm kiếm sách để cập nhật
                int index = findBookIndexByKeyword("Nhập ID (hoặc ISBN hoặc tên sách) cần cập nhật: ");

                if(index < 0){
                    // Thay vì return ngay, hỏi xem có muốn tìm lại cái khác không
                    if (askYesNo("Không tìm thấy. Bạn có muốn thử lại từ khóa khác không? (Y/N): ") == 0) {
                        break; // Thoát vòng lặp while, không phải return
                    }
                    continue; // Quay lại đầu vòng lặp để nhập lại
                }

                // 2. Khai báo biến tạm
                Book book = bookList.get(index);

                // 3. Cập nhật tên sách
                System.out.println("Tên sách: " + book.getTitle());
                String newTitle = inputTitle("Tên mới (Enter để giữ nguyên như cũ): ",true);
                if(!newTitle.isEmpty()){
                    book.setTitle(newTitle);
                }

                // 4. Cập nhật tập 1, .....
                System.out.println("Tập: " + book.getVolume());
                String newVolume = inputVolume("Tập mới (Enter đẻ giữ nguyên như cũ): ", true);
                if(!newVolume.isEmpty()){
                    book.setVolume(newVolume);
                }

                // 5. Cập nhật Tác giả
                System.out.println("Tác giả: " + book.getAuthor());
                String newAuthor = inputFullName("Tác giả mới (Enter để giữ nguyên như cũ): ", true);
                if(!newAuthor.isEmpty()){
                    book.setAuthor(newAuthor);
                }

                // 6. Cập nhập Nhà xuất bản
                System.out.println("Nhà xuất bản: " + book.getPublisher());
                String newPublisher = inputPublisher("Nhà xuất bản mới (Enter để giữ nguyên như cũ): ", true);
                if(!newPublisher.isEmpty()){
                    book.setPublisher(newPublisher);
                }

                // 7. Cập nhập đơn giá sách
                System.out.println("Đơn giá sách: " + book.getOriginalPrice());
                Double newOriginalPrice = inputBookPrice("Đơn giá sách mới (Enter để giữ nguyên như cũ): ", true);
                if(newOriginalPrice != -1){
                    book.setOriginalPrice(newOriginalPrice);
                }

                // 8. Cập nhập đơn giá cho thuê 1 ngày
                System.out.println("Đơn giá cho thuê sách 1 ngày: " + book.getRentalPrice());
                Double newRentalPrice = inputRentalPrice("Đơn giá mới cho thuê 1 ngày (Enter để giữ nguyên như cũ): ",
                        true);
                if(newRentalPrice != -1){
                    book.setRentalPrice(newRentalPrice);
                }

                // 9. Cập nhập số lượng
                System.out.println("Số lượng sách: " + book.getQuantity());
                int newQuantity = inputQuantity("Số lượng sách mới (Enter để giữ nguyên như cũ): ", true);

                // 10. Xử lý đồng bộ số lượng Sách vật lý ĐẠT CHUẨN AN TOÀN TẬP TRUNG và LƯU FILE
                // 10.1. Cập nhật lại số lượng Sách vật lý
                if(newQuantity != -1 && newQuantity != book.getQuantity()){
                    int oldQuantity = book.getQuantity();

                    // 10.2. BƯỚC QUYẾT ĐỊNH: Hỏi ý kiến kho vật lý trước, CHƯA setQuantity vội
                    boolean isAllowed = BookInstanceService.adjustBookInstanceQuantity(book.getId(), oldQuantity, newQuantity);
                    if(isAllowed){

                        // 10.3.0KIỂM SOÁT CHÉO: Tính toán chênh lệch để tự động dịch chuyển số tồn
                        int diff = newQuantity - oldQuantity;
                        book.setQuantity(newQuantity);
                        book.setAvailableQuantity(book.getAvailableQuantity() + diff);

                        // 10.4. Tự động lưu file sách vật lý
                        BookInstanceService.saveBookInstanceListToFile();

                        // 10.5. Tự động lưu file sách
                        saveListToFile(bookList,"data/books.dat");
                    }else {
                        // Kho vật lý từ chối (do vướng sách đang mượn) -> Giữ nguyên thông tin cũ
                        System.out.println("\u26A0\uFE0F Hệ thống từ chối cập nhật số lượng do vướng sách đang mượn.");
                        // Mã Unicode của ký tự cảnh báo tam giác vàng ⚠️ là \u26A0
                        // Nhưng thêm /uFEOF: để đảm bảo nó hiển thị đúng biểu tượng màu sắc trên mọi hệ điều hành
                        // (Windows/macOS) và không bị lỗi font ô vuông
                    }
                }else {
                    // Trường hợp thủ thư không sửa số lượng, nhưng có sửa tên, tác giả... ở các bước trên
                    saveListToFile(bookList,"data/books.dat");
                }

                // 11. Hiển thị thông báo
                System.out.println("Đã cập nhật sách thành công.");

                // 12. Hỏi có tiếp tục cập nhật Book khác nữa không?
                if (askYesNo("Bạn có muốn tiếp tục cập nhật Sách khác không (Y/N): ") == 0){
                    break;
                }
            } catch (Exception e) {
                System.out.println("Lỗi " + e.getMessage());
            }
        }
    }


    // 19. HÀM XÓA BOOK
    public static void deleteBook(String prompt) throws Exception{
        while (true){
            try {
                // 1. Tìm sách cần xóa
                int index = findBookIndexByKeyword(prompt);
                if (index < 0){
                    // Thay vì return ngay, hỏi xem có muốn tìm lại cái khác không
                    if (askYesNo("Không tìm thấy. Bạn có muốn thử lại từ khóa khác không? (Y/N): ") == 0) {
                        break; // Thoát vòng lặp while, không phải return
                    }
                    continue; // Quay lại đầu vòng lặp để nhập lại
                }

                // 2. In thông tin sách cần xóa
                System.out.println("THÔNG TIN SÁCH CẦN XÓA");
                printBookByIndex(index);

                // 3. Khai báo biến tạm
                Book book = bookList.get(index);

                // 4. Hỏi trước khi xóa
                if (askYesNo("Bạn có chắc XÓA sách không (Y/N): ") == 1){
                    boolean isDeleted = BookInstanceService.deleteBookInstanceByBookId(book.getId());
                    if (isDeleted){
                        bookList.remove(index);
                        saveListToFile(bookList,"data/books.dat");      // Save danh sách Sách
                        BookInstanceService.saveBookInstanceListToFile();       // Save sách vật lý
                        System.out.println("\u2705 Đã xóa sách thành công!");
                    }else {
                        System.out.println("\u274C Xóa thát bại.");
                    }

                }else {
                    System.out.println("\u274C Đã hủy thao tác xóa.");
                }

                // 5. Hỏi có tiếp tục xóa Book khác nữa không?
                if (askYesNo("Bạn có thực hiện lại chức năng Xóa sách không (Y/N): ") == 0){
                    break;
                }
            } catch (Exception e) {
                System.out.println("Có lỗi xảy ra " + e.getMessage());
            }
        }
    }


    // 19. HÀM TỰ ĐỘNG NẠP DỮ LIỆU ĐẦU SÁCH VÀ SÁCH VẬT LÝ KỆ KHO MẪU ĐỂ TEST [🛡️]
    public static void seedBookData() {
        // Chỉ nạp nếu danh sách trống rỗng chống trùng lặp dữ liệu RAM
        if (bookList == null || !bookList.isEmpty()) {
            return;
        }
        try {
            // Đầu sách số 1: Lập trình Java
            Book b1 = new Book();
            b1.setId("B001");
            b1.setIsbn("978-3-16-148410-0");
            b1.setTitle("Lap Trinh Java Core Thuong Mai");
            b1.setAuthor("Tech Lead Java");
            b1.setPublisher("NXB Cong Nghe Quoc Te");
            b1.setOriginalPrice(150000.0); // Giá bìa gốc sách vật chất: 150.000đ (Tiền cọc)
            b1.setVolume("Tập 1");
            b1.setAvailableQuantity(2); // Có sẵn 2 quyển trên kệ kho
            bookList.add(b1);

            // Đầu sách số 2: Cấu trúc dữ liệu và giải thuật
            Book b2 = new Book();
            b2.setId("B002");
            b2.setIsbn("978-1-23-456789-7");
            b2.setTitle("Cau Truc Du Lieu Va Giai Thuat Nang Cao");
            b2.setAuthor("Giao Su Thuong Luu");
            b2.setPublisher("NXB Giao Duc Viet Nam");
            b2.setOriginalPrice(200000.0); // Giá bìa gốc: 200.000đ
            b2.setVolume("Tập 1");
            b2.setAvailableQuantity(2);
            bookList.add(b2);

            saveListToFile(bookList, "data/books.dat"); // Lưu file đầu sách

            // -------------------------------------------------------------
            // LIÊN THÔNG: TỰ ĐỘNG SINH 4 CUỐN SÁCH VẬT LÝ CÁ BIỆT (BOOK INSTANCES) TRÊN KỆ [🛡️]
            // Mỗi đầu sách sinh ra 2 cuốn vật lý tương ứng bám sát foreign key cấu trúc mã vạch gạch ngang
            ArrayList<BookInstance> instances = BookInstanceService.getBookInstanceList();
            instances.clear(); // Làm sạch kho đệm vật lý trước

            // Sách vật lý của đầu sách B001
            instances.add(new BookInstance("B001-01", "B001", BookInstanceService.AVAILABLE));
            instances.add(new BookInstance("B001-02", "B001", BookInstanceService.AVAILABLE));

            // Sách vật lý của đầu sách B002
            instances.add(new BookInstance("B002-01", "B002", BookInstanceService.AVAILABLE));
            instances.add(new BookInstance("B002-02", "B002", BookInstanceService.AVAILABLE));

            BookInstanceService.saveBookInstanceListToFile(); // Đóng băng lưu tệp sách vật lý
            System.out.println("==> Đã nạp thành công 2 Đầu sách tổng hợp cùng 4 cuốn sách vật lý cá biệt trên kệ kho!");
        } catch (Exception e) {
            System.out.println("❌ Lỗi nạp dữ liệu mồi kho sách: " + e.getMessage());
        }
    }



}
