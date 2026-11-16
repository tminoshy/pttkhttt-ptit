/* ===================================================================
 LỚP      : SalesStaff4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp SalesStaff4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblStaff4 (không tải).
 KỊCH BẢN : module 2, không có bước riêng; kiểu tham chiếu không tải trong báo cáo.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;

public class SalesStaff4 extends Staff4 {

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public SalesStaff4() {}
}
