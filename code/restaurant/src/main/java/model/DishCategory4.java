/* ===================================================================
 * LỚP      : DishCategory4 (tầng thực thể)
 * THIẾT KẾ : class-design-add-dish.puml – hộp DishCategory4 (Hình 5, desing.md §4a)
 * PHÂN TÍCH: lớp DishCategory4 trong class-add-dish.puml. Phương thức getDishCategories()
 *            đã chuyển sang DishCategoryDAO4, giữ nguyên tên.
 * QUAN HỆ  : DishCategory4 o-- Dish4 (thuộc tính dishes : Dish4[]); DishCategoryDAO4–DishCategory4.
 * CSDL     : tblDishCategory4; dishes lấy từ tblDish4.tblDishCategory4id.
 * KỊCH BẢN : v.3 11–13, 20–22, 54–56 (được tạo và đóng gói thông tin thực thể); v.2 10.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public class DishCategory4 {
    private int id;
    private String categoryCode;
    private String name;
    private String description;
    private Dish4[] dishes;

    /** DishCategory4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public DishCategory4() {}

    /** getId() : int; trả thuộc tính id, không truy vấn CSDL. */
    public int getId() { return id; }

    /** setId(id : int); đóng gói thuộc tính id, không truy vấn CSDL. */
    public void setId(int id) { this.id = id; }

    /** getCategoryCode() : String; trả thuộc tính categoryCode, không truy vấn CSDL. */
    public String getCategoryCode() { return categoryCode; }

    /** setCategoryCode(categoryCode : String); đóng gói thuộc tính categoryCode, không truy vấn CSDL. */
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    /** getName() : String; trả thuộc tính name, không truy vấn CSDL. */
    public String getName() { return name; }

    /** setName(name : String); đóng gói thuộc tính name, không truy vấn CSDL. */
    public void setName(String name) { this.name = name; }

    /** getDescription() : String; trả thuộc tính description, không truy vấn CSDL. */
    public String getDescription() { return description; }

    /** setDescription(description : String); đóng gói thuộc tính description, không truy vấn CSDL. */
    public void setDescription(String description) { this.description = description; }

    /** getDishes() : Dish4[]; trả thuộc tính dishes, không truy vấn CSDL. */
    public Dish4[] getDishes() { return dishes; }

    /** setDishes(dishes : Dish4[]); đóng gói thuộc tính dishes, không truy vấn CSDL. */
    public void setDishes(Dish4[] dishes) { this.dishes = dishes; }
}
