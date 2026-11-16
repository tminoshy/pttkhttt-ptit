/* ===================================================================
 * LỚP      : ManagementStaff4 (tầng thực thể)
 * THIẾT KẾ : class-design-add-dish.puml – hộp ManagementStaff4 (Hình 5, desing.md §4a)
 * MODULE 2 : ManagementStaff4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: không có trên class-add-dish.puml; là actor Nhân viên quản lý. Có mặt vì là
 *            kiểu của thuộc tính ẩn managementStaff trên mọi trang gd…jsp (desing.md §3).
 * QUAN HỆ  : Staff4 <|-- ManagementStaff4.
 * CSDL     : dòng tblStaff4 có vaitro = 'ManagementStaff4'.
 * KỊCH BẢN : không thuộc v.3; đặt vào session khi đăng nhập (doLogin4.jsp), đọc ở
 *            WEB-INF/auth.jspf cho tiền điều kiện đã đăng nhập.
 * KỊCH BẢN MODULE 2: không có bước riêng; phục vụ tiền điều kiện đăng nhập, managementStaff trong session.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public class ManagementStaff4 extends Staff4 {

    /** ManagementStaff4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public ManagementStaff4() {}
}
