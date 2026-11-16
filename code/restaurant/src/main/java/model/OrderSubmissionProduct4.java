/* ===================================================================
 LỚP      : OrderSubmissionProduct4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp OrderSubmissionProduct4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrderSubmissionProduct4.
 KỊCH BẢN : module 2, tk-v.3 45.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class OrderSubmissionProduct4 {
    private int id;
    private int quantity;
    private float recordedUnitPrice;
    private String note;
    private String processingStatus;
    private Date processedAt;
    private String rejectionReason;
    private Product4 product;
    private SalesStaff4 salesStaff;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public OrderSubmissionProduct4() {}
    /** Trả thuộc tính id, không đọc CSDL. */
    public int getId() { return id; }
    /** Đóng gói thuộc tính id, không ghi CSDL. */
    public void setId(int id) { this.id = id; }
    /** Trả thuộc tính quantity, không đọc CSDL. */
    public int getQuantity() { return quantity; }
    /** Đóng gói thuộc tính quantity, không ghi CSDL. */
    public void setQuantity(int quantity) { this.quantity = quantity; }
    /** Trả thuộc tính recordedUnitPrice, không đọc CSDL. */
    public float getRecordedUnitPrice() { return recordedUnitPrice; }
    /** Đóng gói thuộc tính recordedUnitPrice, không ghi CSDL. */
    public void setRecordedUnitPrice(float recordedUnitPrice) { this.recordedUnitPrice = recordedUnitPrice; }
    /** Trả thuộc tính note, không đọc CSDL. */
    public String getNote() { return note; }
    /** Đóng gói thuộc tính note, không ghi CSDL. */
    public void setNote(String note) { this.note = note; }
    /** Trả thuộc tính processingStatus, không đọc CSDL. */
    public String getProcessingStatus() { return processingStatus; }
    /** Đóng gói thuộc tính processingStatus, không ghi CSDL. */
    public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }
    /** Trả thuộc tính processedAt, không đọc CSDL. */
    public Date getProcessedAt() { return processedAt; }
    /** Đóng gói thuộc tính processedAt, không ghi CSDL. */
    public void setProcessedAt(Date processedAt) { this.processedAt = processedAt; }
    /** Trả thuộc tính rejectionReason, không đọc CSDL. */
    public String getRejectionReason() { return rejectionReason; }
    /** Đóng gói thuộc tính rejectionReason, không ghi CSDL. */
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    /** Trả thuộc tính product, không đọc CSDL. */
    public Product4 getProduct() { return product; }
    /** Đóng gói thuộc tính product, không ghi CSDL. */
    public void setProduct(Product4 product) { this.product = product; }
    /** Trả thuộc tính salesStaff, không đọc CSDL. */
    public SalesStaff4 getSalesStaff() { return salesStaff; }
    /** Đóng gói thuộc tính salesStaff, không ghi CSDL. */
    public void setSalesStaff(SalesStaff4 salesStaff) { this.salesStaff = salesStaff; }
}
