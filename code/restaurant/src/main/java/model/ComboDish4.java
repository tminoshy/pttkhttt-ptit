/* ===================================================================
 LỚP      : ComboDish4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp ComboDish4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : không đọc tblComboDish4, thuộc tính comboDishes được gán mảng rỗng.
 KỊCH BẢN : module 2, không có bước riêng; kiểu tham chiếu không tải trong báo cáo.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;

public class ComboDish4 {
    private int id;
    private int quantity;
    private Dish4 dish;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public ComboDish4() {}
    /** Trả thuộc tính id, không đọc CSDL. */
    public int getId() { return id; }
    /** Đóng gói thuộc tính id, không ghi CSDL. */
    public void setId(int id) { this.id = id; }
    /** Trả thuộc tính quantity, không đọc CSDL. */
    public int getQuantity() { return quantity; }
    /** Đóng gói thuộc tính quantity, không ghi CSDL. */
    public void setQuantity(int quantity) { this.quantity = quantity; }
    /** Trả thuộc tính dish, không đọc CSDL. */
    public Dish4 getDish() { return dish; }
    /** Đóng gói thuộc tính dish, không ghi CSDL. */
    public void setDish(Dish4 dish) { this.dish = dish; }
}
