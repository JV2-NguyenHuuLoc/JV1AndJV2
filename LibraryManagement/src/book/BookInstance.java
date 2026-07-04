package book;

import java.io.Serializable;

public class BookInstance implements Serializable {
    private String id;          // Mã cá biệt có đuôi: ví dụ: TH97860412342026-001
    private String bookId;      // Mã liên kết ngược về class Book tổ hợp
    private int status;         // (0: Sẵn sàng trên kệ, 1: Đang cho mượn, 2: Đã mất/Hỏng)

    public BookInstance() {
    }

    public BookInstance(String id, String bookId, int status) {
        this.id = id;
        this.bookId = bookId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
