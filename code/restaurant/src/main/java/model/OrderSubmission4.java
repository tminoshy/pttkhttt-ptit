/* ===================================================================
 LỚP      : OrderSubmission4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp OrderSubmission4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrderSubmission4 (không tải đối tượng, chỉ dùng JOIN).
 KỊCH BẢN : module 2, không có bước riêng; kiểu tham chiếu không tải trong báo cáo.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class OrderSubmission4 {
    private int id;
    private Date submittedAt;
    private OrderSubmissionProduct4[] orderSubmissionProducts;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public OrderSubmission4() {}
    /** Trả thuộc tính id, không đọc CSDL. */
    public int getId() { return id; }
    /** Đóng gói thuộc tính id, không ghi CSDL. */
    public void setId(int id) { this.id = id; }
    /** Trả thuộc tính submittedAt, không đọc CSDL. */
    public Date getSubmittedAt() { return submittedAt; }
    /** Đóng gói thuộc tính submittedAt, không ghi CSDL. */
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
    /** Trả thuộc tính orderSubmissionProducts, không đọc CSDL. */
    public OrderSubmissionProduct4[] getOrderSubmissionProducts() { return orderSubmissionProducts; }
    /** Đóng gói thuộc tính orderSubmissionProducts, không ghi CSDL. */
    public void setOrderSubmissionProducts(OrderSubmissionProduct4[] orderSubmissionProducts) { this.orderSubmissionProducts = orderSubmissionProducts; }
}
