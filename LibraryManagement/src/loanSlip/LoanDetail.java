package loanSlip;

import java.io.Serializable;

public class LoanDetail implements Serializable {

    // KHAI BÁO THUỘC TÍNH
    private String bookInstanceId;      //  bookInstanceId: Mã của sách vật lý
    private boolean isReturned;         // True: đã trả; false: báo mất hoặc chưa trả
    private double fine;                // Tiền phạt riêng cho cuốn sách này (nếu làm mất sách).

    // KHỞI TẠO CONSTRUCTOR
    public LoanDetail() {
    }

    public LoanDetail(String bookInstanceId, boolean isReturned, double fine) {
        this.bookInstanceId = bookInstanceId;
        this.isReturned = isReturned;
        this.fine = fine;
    }

    // KHỞI TẠO GETTER , SETTER
    public String getBookInstanceId() {
        return bookInstanceId;
    }

    public void setBookInstanceId(String bookIsbn) {
        this.bookInstanceId = bookIsbn;
    }

    public boolean isReturned() {
        return isReturned;
    }

    public void setReturned(boolean returned) {
        isReturned = returned;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }


}
