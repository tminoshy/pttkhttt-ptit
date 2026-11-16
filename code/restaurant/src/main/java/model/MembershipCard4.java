/* ===================================================================
 LỚP      : MembershipCard4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp MembershipCard4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblMembershipCard4 (không tải).
 KỊCH BẢN : module 2, không có bước riêng; kiểu tham chiếu không tải trong báo cáo.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class MembershipCard4 {
    private int id;
    private String cardCode;
    private Date issuedAt;
    private String status;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public MembershipCard4() {}
    /** Trả thuộc tính id, không đọc CSDL. */
    public int getId() { return id; }
    /** Đóng gói thuộc tính id, không ghi CSDL. */
    public void setId(int id) { this.id = id; }
    /** Trả thuộc tính cardCode, không đọc CSDL. */
    public String getCardCode() { return cardCode; }
    /** Đóng gói thuộc tính cardCode, không ghi CSDL. */
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }
    /** Trả thuộc tính issuedAt, không đọc CSDL. */
    public Date getIssuedAt() { return issuedAt; }
    /** Đóng gói thuộc tính issuedAt, không ghi CSDL. */
    public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }
    /** Trả thuộc tính status, không đọc CSDL. */
    public String getStatus() { return status; }
    /** Đóng gói thuộc tính status, không ghi CSDL. */
    public void setStatus(String status) { this.status = status; }
}
