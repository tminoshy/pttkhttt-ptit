/* ===================================================================
 LỚP      : CustomerStatistic4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp CustomerStatistic4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblCustomer4, tblOrder4, tblInvoice4, tblPayment4 (kết quả thống kê không lưu bảng riêng).
 KỊCH BẢN : module 2, tk-v.3 15, 28, 40.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class CustomerStatistic4 extends Customer4 {
    private Date periodStart;
    private Date periodEnd;
    private float totalSpending;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public CustomerStatistic4() {}
    /** Trả thuộc tính periodStart, không đọc CSDL. */
    public Date getPeriodStart() { return periodStart; }
    /** Đóng gói thuộc tính periodStart, không ghi CSDL. */
    public void setPeriodStart(Date periodStart) { this.periodStart = periodStart; }
    /** Trả thuộc tính periodEnd, không đọc CSDL. */
    public Date getPeriodEnd() { return periodEnd; }
    /** Đóng gói thuộc tính periodEnd, không ghi CSDL. */
    public void setPeriodEnd(Date periodEnd) { this.periodEnd = periodEnd; }
    /** Trả thuộc tính totalSpending, không đọc CSDL. */
    public float getTotalSpending() { return totalSpending; }
    /** Đóng gói thuộc tính totalSpending, không ghi CSDL. */
    public void setTotalSpending(float totalSpending) { this.totalSpending = totalSpending; }
}
