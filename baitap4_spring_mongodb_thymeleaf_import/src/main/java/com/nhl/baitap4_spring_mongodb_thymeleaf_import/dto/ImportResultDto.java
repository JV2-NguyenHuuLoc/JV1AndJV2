package com.nhl.baitap4_spring_mongodb_thymeleaf_import.dto;

/* CHỨC NĂNG CLASS: Khai sinh cấu trúc bản tin Carrier phục vụ riêng cho tính năng Import tệp tin,
đóng hòm trạng thái thành công/thất bại kèm con số đếm bản ghi vật lý để bắn ra Front-end Thymeleaf
render thông báo trực quan cho người dùng xem .
 */

public record ImportResultDto (boolean success, String message, Integer importCount){
    // 1. HÀM 1: ĐÓNG GÓI TRẠNG THÁI SUCCESS -->  TRẢ VỀ TRUE + SỐ DÒNG BẢN GHI CHUẨN
    public static ImportResultDto ok(int count){
        return new ImportResultDto(
                true,
                "HẠ TẦNG: DS REATAURANTS ĐÃ ĐƯỢC CỖ MÁY BĂM NHỎ THEO LÔ (10 ITEMS/LÔ) VÀ NẠP ĐĨA CỨNG MONGODB " +
                        "ĐẠI THẮNG VẸN TOÀN!",
                count
        );
    }

    // 2. HÀM 2: ĐÁNH CHẶN TRẠNG THÁI THÁT BẠI --> TRẢ VỀ FALSE + THÔNG BÁO LỖI HẠ TẦNG NGẦM
    public static ImportResultDto fail(String msg){
        return new ImportResultDto(false,msg,0);
    }
}
