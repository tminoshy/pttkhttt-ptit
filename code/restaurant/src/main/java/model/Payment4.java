/* ===================================================================
 LỚP      : Payment4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp Payment4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblPayment4.
 KỊCH BẢN : module 2, tk-v.3 24, 36; các lần thanh toán của Invoice4.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class Payment4 {
    private int id;
    private float amount;
    private String method;
    private Date paidAt;
    private String result;
    private SalesStaff4 salesStaff;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public Payment4() {}
    /** Trả thuộc tính id, không đọc CSDL. */
    public int getId() { return id; }
    /** Đóng gói thuộc tính id, không ghi CSDL. */
    public void setId(int id) { this.id = id; }
    /** Trả thuộc tính amount, không đọc CSDL. */
    public float getAmount() { return amount; }
    /** Đóng gói thuộc tính amount, không ghi CSDL. */
    public void setAmount(float amount) { this.amount = amount; }
    /** Trả thuộc tính method, không đọc CSDL. */
    public String getMethod() { return method; }
    /** Đóng gói thuộc tính method, không ghi CSDL. */
    public void setMethod(String method) { this.method = method; }
    /** Trả thuộc tính paidAt, không đọc CSDL. */
    public Date getPaidAt() { return paidAt; }
    /** Đóng gói thuộc tính paidAt, không ghi CSDL. */
    public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }
    /** Trả thuộc tính result, không đọc CSDL. */
    public String getResult() { return result; }
    /** Đóng gói thuộc tính result, không ghi CSDL. */
    public void setResult(String result) { this.result = result; }
    /** Trả thuộc tính salesStaff, không đọc CSDL. */
    public SalesStaff4 getSalesStaff() { return salesStaff; }
    /** Đóng gói thuộc tính salesStaff, không ghi CSDL. */
    public void setSalesStaff(SalesStaff4 salesStaff) { this.salesStaff = salesStaff; }
}
