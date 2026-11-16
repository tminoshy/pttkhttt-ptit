/* ===================================================================
 LỚP      : Combo4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp Combo4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblCombo4 và tblProduct4.
 KỊCH BẢN : module 2, tk-v.3 45; mặt hàng combo của dòng gọi món.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class Combo4 extends Product4 {
    private Date startsAt;
    private Date endsAt;
    private ComboDish4[] comboDishes;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public Combo4() {}
    /** Trả thuộc tính startsAt, không đọc CSDL. */
    public Date getStartsAt() { return startsAt; }
    /** Đóng gói thuộc tính startsAt, không ghi CSDL. */
    public void setStartsAt(Date startsAt) { this.startsAt = startsAt; }
    /** Trả thuộc tính endsAt, không đọc CSDL. */
    public Date getEndsAt() { return endsAt; }
    /** Đóng gói thuộc tính endsAt, không ghi CSDL. */
    public void setEndsAt(Date endsAt) { this.endsAt = endsAt; }
    /** Trả thuộc tính comboDishes, không đọc CSDL. */
    public ComboDish4[] getComboDishes() { return comboDishes; }
    /** Đóng gói thuộc tính comboDishes, không ghi CSDL. */
    public void setComboDishes(ComboDish4[] comboDishes) { this.comboDishes = comboDishes; }
}
