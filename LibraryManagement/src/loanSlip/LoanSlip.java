package loanSlip;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class LoanSlip implements Serializable {
    // KHAI BÁO BIẾN HẰNG THỜI GIAN CHO THUÊ MẶC ĐỊNH
    public static final int DEFAULT_RENTAL_DAYS = 7;        // Số ngày cho thuê mặc định
    public static final int DEFAULT_RENEW_DAYS = 7;         // Số ngày gia hạn thêm thời gian mượn sách
    public static final int MAX_RENEW_LIMITS = 2;           // Số lần gia hạn tối đa

    // KHAI BÁO BIẾN HẰNG TRẠNG THÁI PHIẾU MƯỢN
    public static final int BORROWING = 0;      // Phiếu mượn: Đang mượn
    public static final int COMPLETED = 1;      // Phiếu mượn: Đã hoàn tất trả (tất toán)

    // KHAI BÁO CÁO THUỘC TÍNH
    // 1. ĐỊNH DANH VÀ LIÊN KẾT
    private String id;                      // Mã phiếu mượn
    private String readerId;                // Mã người mượn
    private ArrayList<LoanDetail> details;  // Danh sách các đối tượng chi tiết sách

    // 2. QUẢN LÝ THỜI GIAN
    private LocalDate rentalDate;           // Ngày mượn
    private LocalDate expectedReturnDate;   // expectedReturnDate = rentalDate + DEFAULT_RENTAL_DAYS (Auto khi lập phiếu)
    private LocalDate actualReturnDate;     // Ngày trả thực tế = null khi mới lập phiếu

    // 3. QUẢN LÝ NHÂN SỰ, DÒNG TIỀN
    private int createdByUserId;            // Người Lập phiếu, thu tiền đặt cọc: Luôn tạo ra khi lập phiếu
    private Integer processedByUserId;      // Người nhận trả sách, thu phí, tiền phạt = null khi mới lập phiếu

    // 4. QUẢN LÝ TÀI CHÍNH
    private double deposit;                 // Tiền đặt cọc lúc mượn
    private double totalRentalFee;          // Tổng tiền thuê: Tính 1 lần khi lập phiếu
    private double overduePenaltyFee;       // Tiền phạt trễ hạn: 5.000 đồng/ngày + tính cho cả phiếu
    private double totalLostBookFee;        // Tiền phạt mất sách: Sum của các loanDetail.fine

    // 5. TRẠNG THÁI
    private int status;                     // 0: đang mượn; 1: đã hoàn thành
    private boolean isBlock;                // True: Block; false: hoạt động bình thường

    // 6. QUẢN LÝ GIA HẠN PHIẾU MƯỢN
    private int renewCount;                 // Số lần gia hạn phiếu mượn

    public LoanSlip() {
    }

    public LoanSlip(String id, String readerId, ArrayList<LoanDetail> details, LocalDate rentalDate,
                    LocalDate expectedReturnDate, LocalDate actualReturnDate, int createdByUserId,
                    Integer processedByUserId, double deposit, double totalRentalFee, double overduePenaltyFee,
                    double totalLostBookFee, int status, boolean isBlock, int renewCount) {
        this.id = id;
        this.readerId = readerId;
        this.details = details;
        this.rentalDate = rentalDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = actualReturnDate;
        this.createdByUserId = createdByUserId;
        this.processedByUserId = processedByUserId;
        this.deposit = deposit;
        this.totalRentalFee = totalRentalFee;
        this.overduePenaltyFee = overduePenaltyFee;
        this.totalLostBookFee = totalLostBookFee;
        this.status = status;
        this.isBlock = isBlock;
        this.renewCount = renewCount;
    }


    // Constructor dùng khi LẬP PHIẾU MỚI (chỉ truyền những thuộc tính do người dùng nhập)
    public LoanSlip(String id, String readerId, int createdByUserId, ArrayList<LoanDetail> details,
                    double deposit, double totalRentalFee) {
        this.id = id;
        this.readerId = readerId;
        this.createdByUserId = createdByUserId;
        this.details = details;
        this.deposit = deposit;
        this.totalRentalFee = totalRentalFee;

        // Các giá trị mặc định hệ thống tự tính
        this.rentalDate = LocalDate.now();
        this.expectedReturnDate = this.rentalDate.plusDays(DEFAULT_RENTAL_DAYS);
        this.status = BORROWING;                // Đang mượn
        this.isBlock = false;                   // Mặc định hoạt động bình thường (không bị block)

        // Các giá trị khởi tạo là null hoặc 0
        this.actualReturnDate = null;
        this.processedByUserId = null;
        this.overduePenaltyFee = 0.0;
        this.totalLostBookFee = 0.0;
        this.renewCount = 0;                    // Mặc định số lần gia hạn Phiếu ban đầu = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public ArrayList<LoanDetail> getDetails() {
        return details;
    }

    public void setDetails(ArrayList<LoanDetail> details) {
        this.details = details;
    }

    public LocalDate getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(LocalDate rentalDate) {
        this.rentalDate = rentalDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public int getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public Integer getProcessedByUserId() {
        return processedByUserId;
    }

    public void setProcessedByUserId(Integer processedByUserId) {
        this.processedByUserId = processedByUserId;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public double getTotalRentalFee() {
        return totalRentalFee;
    }

    public void setTotalRentalFee(double totalRentalFee) {
        this.totalRentalFee = totalRentalFee;
    }

    public double getOverduePenaltyFee() {
        return overduePenaltyFee;
    }

    public void setOverduePenaltyFee(double overduePenaltyFee) {
        this.overduePenaltyFee = overduePenaltyFee;
    }

    public double getTotalLostBookFee() {
        return totalLostBookFee;
    }

    public void setTotalLostBookFee(double totalLostBookFee) {
        this.totalLostBookFee = totalLostBookFee;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isBlock() { return isBlock;    }

    public void setBlock(boolean block) { this.isBlock = block;    }

    public int getRenewCount() { return renewCount;    }

    public void setRenewCount(int renewCount) { this.renewCount = renewCount;    }

}
