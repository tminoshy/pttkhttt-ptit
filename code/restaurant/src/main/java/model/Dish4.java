/* ===================================================================
 * LỚP      : Dish4 (tầng thực thể)
 * THIẾT KẾ : class-design-add-dish.puml – hộp Dish4 (Hình 5, desing.md §4a)
 * MODULE 2 : Dish4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: lớp Dish4 trong class-add-dish.puml. Ba phương thức getDishes(), validateDish(),
 *            saveDish() của Dish4 ở pha phân tích đã chuyển sang DishDAO4, giữ nguyên tên.
 * QUAN HỆ  : Product4 <|-- Dish4; DishCategory4 o-- Dish4; DishDAO4–Dish4.
 * CSDL     : tblDish4 (tblProduct4id, tblDishCategory4id) + các cột kế thừa ở tblProduct4.
 * KỊCH BẢN : v.3 5–7, 27–29, 48–50 (được tạo và đóng gói thông tin thực thể);
 *            v.2 4, 16, 25.
 * KỊCH BẢN MODULE 2: tk-v.3 45; đóng gói mặt hàng của OrderSubmissionProduct4.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public class Dish4 extends Product4 {

    /** Dish4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public Dish4() {}
}
