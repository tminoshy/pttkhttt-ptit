/* ===================================================================
 * LỚP      : DishCategoryDAO4 (tầng thao tác dữ liệu)
 * THIẾT KẾ : class-design-add-dish.puml – hộp DishCategoryDAO4 (Hình 5, desing.md §4a)
 * PHÂN TÍCH: phương thức getDishCategories() của lớp DishCategory4 trong class-add-dish.puml,
 *            chuyển sang DAO4, giữ nguyên tên.
 * QUAN HỆ  : DAO4 <|-- DishCategoryDAO4; DishCategoryDAO4–DishCategory4; được gọi từ
 *            dishManagementView4.jsp, addDishView4.jsp.
 * CSDL     : tblDishCategory4 (đọc), tblDish4 (đọc, để điền DishCategory4.dishes).
 * KỊCH BẢN : v.3 10–14, 19–23, 38–42, 53–57; v.2 10–11.
 * NGOẠI LỆ : A1 – trả mảng rỗng khi không có loại món ăn (addDishView4.jsp xử lý ở bước 24).
 * =================================================================== */
package dao;

import model.Dish4;
import model.DishCategory4;
import java.sql.*;
import java.util.ArrayList;

public class DishCategoryDAO4 extends DAO4 {
    /** DishCategoryDAO4(): dùng kết nối của DAO4; không truy vấn bảng. */
    public DishCategoryDAO4() { super(); }

    /** getDishCategories() : DishCategory4[]; từ DishCategory4.getDishCategories().
     * v.3 10–14,19–23,38–42,53–57; v.2 10–11. Đọc tblDishCategory4, tblDish4.
     * A1: mảng rỗng khi không có loại; dishes chỉ chứa id để tìm loại của món.
     * Lỗi SQL ném IllegalStateException, không trả danh sách rỗng giả.
     */
    // [v3-10, v3-19, v3-38, v3-53][v2-10] Lớp DishCategoryDAO4 gọi hàm getDishCategories().
    public DishCategory4[] getDishCategories() {
        synchronized (DAO4.class) {
            ArrayList<DishCategory4> list = new ArrayList<>();
            try {
                try (PreparedStatement s = con.prepareStatement("SELECT * FROM tblDishCategory4 ORDER BY categoryCode"); ResultSet rs = s.executeQuery()) {
                    while (rs.next()) {
                        // [v3-11, v3-20, v3-39, v3-54][v2-10] Hàm getDishCategories() gọi lớp DishCategory4 để đóng gói thông tin.
                        DishCategory4 category = new DishCategory4();
                        // [v3-12, v3-21, v3-40, v3-55][v2-10] Lớp DishCategory4 đóng gói thông tin thực thể.
                        category.setId(rs.getInt("id")); category.setCategoryCode(rs.getString("categoryCode"));
                        category.setName(rs.getString("name")); category.setDescription(rs.getString("description"));
                        category.setDishes(new Dish4[0]);
                        // [v3-13, v3-22, v3-41, v3-56][v2-10] Lớp DishCategory4 trả kết quả cho hàm getDishCategories().
                        list.add(category);
                    }
                }
                try (PreparedStatement s = con.prepareStatement("SELECT tblDishCategory4id,tblProduct4id FROM tblDish4"); ResultSet rs = s.executeQuery()) {
                    java.util.Map<Integer, ArrayList<Dish4>> members = new java.util.HashMap<>();
                    while (rs.next()) {
                        Dish4 dish = new Dish4(); dish.setId(rs.getInt("tblProduct4id"));
                        members.computeIfAbsent(rs.getInt("tblDishCategory4id"), key -> new ArrayList<>()).add(dish);
                    }
                    // [v3-12, v3-21, v3-40, v3-55][v2-10] Lớp DishCategory4 đóng gói thông tin thực thể.
                    for (DishCategory4 category : list) category.setDishes(members.getOrDefault(category.getId(), new ArrayList<>()).toArray(new Dish4[0]));
                }
                // [v3-14, v3-57] Hàm getDishCategories() trả kết quả cho trang dishManagementView4.jsp.
                // [v3-23, v3-42][v2-11] Hàm getDishCategories() trả kết quả cho trang addDishView4.jsp.
                return list.toArray(new DishCategory4[0]);
            } catch (SQLException e) { throw new IllegalStateException("Không thể lấy danh sách loại món ăn.", e); }
        }
    }
}
