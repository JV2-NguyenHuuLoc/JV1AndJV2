package book;

import java.io.Serializable;
import java.time.Year;

public class Book implements Serializable {
    private String id;              // Mã book cá biệt có đuôi: ví dụ: TH97860412342026
    private String isbn;            //ISBN: viết tắt của International Standard Book Number
    private String title;
    private String volume;           // Tập 1, ....
    private String author;
    private String publisher;
    private Year publishYear;
    private String category;
    private double originalPrice;           // Đơn giá sách
    private double rentalPrice;             // Đơn giá thuê 1 ngày
    private int quantity;
    private int availableQuantity;

    public Book() {
    }

    public Book(String id, String isbn, String title, String volume, String author, String publisher, Year publishYear,
                String category, double originalPrice, double rentalPrice, int quantity, int availableQuantity) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.volume = volume;
        this.author = author;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.category = category;
        this.originalPrice = originalPrice;
        this.rentalPrice = rentalPrice;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
    }

    public String getId() { return id;    }

    public void setId(String id) { this.id = id;    }

    public String getIsbn() { return isbn;    }

    public void setIsbn(String isbn) { this.isbn = isbn;    }

    public String getTitle() { return title;    }

    public void setTitle(String title) { this.title = title;    }

    public String getVolume() { return volume;    }

    public void setVolume(String volume) { this.volume = volume;    }

    public String getAuthor() { return author;    }

    public void setAuthor(String author) { this.author = author;    }

    public String getPublisher() { return publisher;    }

    public void setPublisher(String publisher) { this.publisher = publisher;    }

    public Year getPublishYear() { return publishYear;    }

    public void setPublishYear(Year publishYear) { this.publishYear = publishYear;    }

    public String getCategory() { return category;    }

    public void setCategory(String category) { this.category = category;    }

    public double getOriginalPrice() { return originalPrice;    }

    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice;    }

    public double getRentalPrice() { return rentalPrice;    }

    public void setRentalPrice(double rentalPrice) { this.rentalPrice = rentalPrice;    }

    public int getQuantity() { return quantity;    }

    public void setQuantity(int quantity) { this.quantity = quantity;    }

    public int getAvailableQuantity() { return availableQuantity;    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    @Override
    public String toString() {
        // Trạng thái: Nếu hết sách thì hiện thông báo đặc biệt
        String status = (availableQuantity > 0)
                ? String.format("%d/%d cuốn", availableQuantity, quantity)
                : "\u274C HẾT SÁCH";

        return String.format(
                "ID: %-8s | ISBN: %-17s | %-25s | Tập: %-5s\n" +
                        "Tác giả: %-15s | NXB: %-15s | Năm: %s | Loại: %-10s\n" +
                        "Giá thuê: %,.0f VNĐ/ngày | Giá gốc: %,.0f VNĐ | Trạng thái: %s\n" +
                        "--------------------------------------------------------------------------------",
                id, isbn, truncate(title, 25), volume,
                truncate(author, 15), truncate(publisher, 15), publishYear, category,
                rentalPrice, originalPrice, status
        );
    }


    // Hàm bổ trợ để tránh việc tên sách quá dài làm vỡ khung (Layout)
    private String truncate(String text, int size) {
        if (text == null){
            return "";
        }
        return (text.length() > size) ? text.substring(0, size - 3) + "..." : text;
    }


    // Hàm bổ trợ việc in ISBN rà màn hình
    private String formatISBN(String isbn){
        // Kiểm tra ISBN đúng chuẩn 13 số: Trả về nguyên mẫu
        if (isbn == null || isbn.length() != 13){
            return isbn;
        }
        // Tự động chèn thêm dấu gạch ngang "-" vào các vị trí phổ biến
        // Chuẩn 1: 978-X-XXXX-XXXX-X       hoặc Chuẩn 2: 979-X-XXXX-XXXX-X
        return String.format("%s-%s-%s-%s-%s",
                isbn.substring(0,3),
                isbn.substring(3,4),
                isbn.substring(4,8),
                isbn.substring(8,12),
                isbn.substring(12)
        );
    }


}
