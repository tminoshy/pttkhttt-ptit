/* ===================================================================
 LỚP      : Order4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp Order4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrder4.
 KỊCH BẢN : module 2, tk-v.3 24, 36.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class Order4 {
    private int id;
    private String orderCode;
    private Date createdAt;
    private String status;
    private OrderSubmission4[] orderSubmissions;
    private Customer4 customer;
    private Invoice4 invoice;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public Order4() {}
    /** Trả thuộc tính id, không đọc CSDL. */
    public int getId() { return id; }
    /** Đóng gói thuộc tính id, không ghi CSDL. */
    public void setId(int id) { this.id = id; }
    /** Trả thuộc tính orderCode, không đọc CSDL. */
    public String getOrderCode() { return orderCode; }
    /** Đóng gói thuộc tính orderCode, không ghi CSDL. */
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    /** Trả thuộc tính createdAt, không đọc CSDL. */
    public Date getCreatedAt() { return createdAt; }
    /** Đóng gói thuộc tính createdAt, không ghi CSDL. */
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    /** Trả thuộc tính status, không đọc CSDL. */
    public String getStatus() { return status; }
    /** Đóng gói thuộc tính status, không ghi CSDL. */
    public void setStatus(String status) { this.status = status; }
    /** Trả thuộc tính orderSubmissions, không đọc CSDL. */
    public OrderSubmission4[] getOrderSubmissions() { return orderSubmissions; }
    /** Đóng gói thuộc tính orderSubmissions, không ghi CSDL. */
    public void setOrderSubmissions(OrderSubmission4[] orderSubmissions) { this.orderSubmissions = orderSubmissions; }
    /** Trả thuộc tính customer, không đọc CSDL. */
    public Customer4 getCustomer() { return customer; }
    /** Đóng gói thuộc tính customer, không ghi CSDL. */
    public void setCustomer(Customer4 customer) { this.customer = customer; }
    /** Trả thuộc tính invoice, không đọc CSDL. */
    public Invoice4 getInvoice() { return invoice; }
    /** Đóng gói thuộc tính invoice, không ghi CSDL. */
    public void setInvoice(Invoice4 invoice) { this.invoice = invoice; }
}
