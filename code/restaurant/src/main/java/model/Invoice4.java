/* ===================================================================
 LỚP      : Invoice4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp Invoice4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblInvoice4.
 KỊCH BẢN : module 2, tk-v.3 24, 36; thuộc tính invoice của Order4.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class Invoice4 {
    private int id;
    private String invoiceCode;
    private Date issuedAt;
    private float amountDue;
    private String paymentStatus;
    private SalesStaff4 salesStaff;
    private Payment4[] payments;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public Invoice4() {}
    /** Trả thuộc tính id, không đọc CSDL. */
    public int getId() { return id; }
    /** Đóng gói thuộc tính id, không ghi CSDL. */
    public void setId(int id) { this.id = id; }
    /** Trả thuộc tính invoiceCode, không đọc CSDL. */
    public String getInvoiceCode() { return invoiceCode; }
    /** Đóng gói thuộc tính invoiceCode, không ghi CSDL. */
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    /** Trả thuộc tính issuedAt, không đọc CSDL. */
    public Date getIssuedAt() { return issuedAt; }
    /** Đóng gói thuộc tính issuedAt, không ghi CSDL. */
    public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }
    /** Trả thuộc tính amountDue, không đọc CSDL. */
    public float getAmountDue() { return amountDue; }
    /** Đóng gói thuộc tính amountDue, không ghi CSDL. */
    public void setAmountDue(float amountDue) { this.amountDue = amountDue; }
    /** Trả thuộc tính paymentStatus, không đọc CSDL. */
    public String getPaymentStatus() { return paymentStatus; }
    /** Đóng gói thuộc tính paymentStatus, không ghi CSDL. */
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    /** Trả thuộc tính salesStaff, không đọc CSDL. */
    public SalesStaff4 getSalesStaff() { return salesStaff; }
    /** Đóng gói thuộc tính salesStaff, không ghi CSDL. */
    public void setSalesStaff(SalesStaff4 salesStaff) { this.salesStaff = salesStaff; }
    /** Trả thuộc tính payments, không đọc CSDL. */
    public Payment4[] getPayments() { return payments; }
    /** Đóng gói thuộc tính payments, không ghi CSDL. */
    public void setPayments(Payment4[] payments) { this.payments = payments; }
}
