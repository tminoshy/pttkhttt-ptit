/* ===================================================================
 * LỚP      : DishDAO4 (tầng thao tác dữ liệu)
 * THIẾT KẾ : class-design-add-dish.puml – hộp DishDAO4 (Hình 5, desing.md §4a)
 * PHÂN TÍCH: ba phương thức của lớp Dish4 trong class-add-dish.puml – getDishes(),
 *            validateDish(), saveDish() – chuyển sang DAO4, giữ nguyên tên và thứ tự.
 * QUAN HỆ  : DAO4 <|-- DishDAO4; DishDAO4–Dish4; được gọi từ dishManagementView4.jsp, doSaveDish4.jsp.
 * CSDL     : tblProduct4 (đọc, ghi), tblDish4 (đọc, ghi), tblDishCategory4 (đọc khi kiểm tra).
 * KỊCH BẢN : v.3 4–8, 31–32, 34–35, 47–51; v.2 4–5, 17–18, 20–21, 25–26.
 * NGOẠI LỆ : A2, E1 (validateDish, bước 32); E2 (saveDish, bước 35).
 * =================================================================== */
package dao;

import model.Dish4;
import java.sql.*;
import java.util.ArrayList;

public class DishDAO4 extends DAO4 {
    /** DishDAO4(): dùng kết nối của DAO4; không truy vấn bảng. */
    public DishDAO4() { super(); }

    /** getDishes() : Dish4[]; từ Dish4.getDishes() ở phân tích.
     * v.3 4–8, 47–51; v.2 4–5,25–26. Đọc tblProduct4 JOIN tblDish4.
     * Mảng rỗng khi không có món; lỗi SQL ném IllegalStateException.
     */
    // [v3-4, v3-47][v2-4, v2-25] Lớp DishDAO4 gọi hàm getDishes().
    public Dish4[] getDishes() {
        synchronized (DAO4.class) {
            ArrayList<Dish4> list = new ArrayList<>();
            try (PreparedStatement statement = con.prepareStatement(
                    "SELECT p.* FROM tblProduct4 p JOIN tblDish4 d ON d.tblProduct4id=p.id ORDER BY p.productCode");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    // [v3-5, v3-48][v2-4, v2-25] Hàm getDishes() gọi lớp Dish4 để đóng gói thông tin.
                    Dish4 dish = new Dish4();
                    // [v3-6, v3-49][v2-4, v2-25] Lớp Dish4 đóng gói thông tin thực thể.
                    dish.setId(rs.getInt("id"));
                    dish.setProductCode(rs.getString("productCode"));
                    dish.setName(rs.getString("name"));
                    dish.setDescription(rs.getString("description"));
                    dish.setSalePrice(rs.getFloat("salePrice"));
                    dish.setImage(rs.getString("image"));
                    dish.setSaleStatus(rs.getString("saleStatus"));
                    dish.setCreatedAt(rs.getTimestamp("createdAt"));
                    dish.setUpdatedAt(rs.getTimestamp("updatedAt"));
                    // [v3-7, v3-50][v2-4, v2-25] Lớp Dish4 trả kết quả cho hàm getDishes().
                    list.add(dish);
                }
                // [v3-8, v3-51][v2-5, v2-26] Hàm getDishes() trả kết quả cho trang dishManagementView4.jsp.
                return list.toArray(new Dish4[0]);
            } catch (SQLException e) { throw new IllegalStateException("Không thể lấy danh sách món ăn.", e); }
        }
    }

    /** validateDish(dish : Dish4, idDishCategory : int) : String[]; từ Dish4.validateDish().
     * v.3 31–32; v.2 17–18; E1/A2. Đọc tblProduct4, tblDishCategory4.
     * [] là hợp lệ; mã trường lỗi theo thứ tự biểu mẫu; khongConLoaiMon đứng riêng.
     */
    // [v3-31][v2-17] Lớp DishDAO4 gọi hàm validateDish().
    public String[] validateDish(Dish4 dish, int idDishCategory) {
        synchronized (DAO4.class) {
            ArrayList<String> errors = new ArrayList<>();
            try {
                try (PreparedStatement s = con.prepareStatement("SELECT 1 FROM tblDishCategory4"); ResultSet rs = s.executeQuery()) {
                    // [A2][v3-32/A2] Không cho lưu. Cần tạo loại món ăn trước khi thêm món.
                    if (!rs.next()) return new String[]{"khongConLoaiMon"};
                }
                boolean duplicate;
                try (PreparedStatement s = con.prepareStatement("SELECT 1 FROM tblProduct4 WHERE productCode=?")) {
                    s.setString(1, dish.getProductCode());
                    try (ResultSet rs = s.executeQuery()) { duplicate = rs.next(); }
                }
                // [E1][v3-32/E1] Không lưu món ăn. Chỉ rõ trường lỗi: mã món bị trùng, thiếu dữ liệu bắt buộc, giá bán phải lớn hơn 0 hoặc loại món ăn không tồn tại.
                if (dish.getProductCode() == null || dish.getProductCode().isBlank() || duplicate) errors.add("inDishCode");
                if (dish.getName() == null || dish.getName().isBlank()) errors.add("inDishName");
                if (!Float.isFinite(dish.getSalePrice()) || dish.getSalePrice() <= 0) errors.add("inSalePrice");
                if (dish.getSaleStatus() == null || dish.getSaleStatus().isBlank()
                        || !(dish.getSaleStatus().equals("Đang kinh doanh") || dish.getSaleStatus().equals("Ngừng kinh doanh"))) errors.add("inSaleStatus");
                try (PreparedStatement s = con.prepareStatement("SELECT 1 FROM tblDishCategory4 WHERE id=?")) {
                    s.setInt(1, idDishCategory);
                    try (ResultSet rs = s.executeQuery()) { if (!rs.next()) errors.add("inoutDishCategory"); }
                }
                // [v3-32][v2-18] Hàm validateDish() trả kết quả hợp lệ cho trang doSaveDish4.jsp.
                return errors.toArray(new String[0]);
            } catch (SQLException e) { throw new IllegalStateException("Không thể kiểm tra món ăn.", e); }
        }
    }

    /** saveDish(dish : Dish4, idDishCategory : int) : Dish4; từ Dish4.saveDish().
     * v.3 34–35; v.2 20–21; E2. Ghi tblProduct4 và tblDish4 trong một giao dịch.
     * Thành công trả Dish4 có id/createdAt; lỗi rollback, trả null; khôi phục autoCommit.
     */
    // [v3-34][v2-20] Lớp DishDAO4 gọi hàm saveDish().
    public Dish4 saveDish(Dish4 dish, int idDishCategory) {
        synchronized (DAO4.class) {
            int id;
            Timestamp createdAt = new Timestamp(System.currentTimeMillis());
            try {
                con.setAutoCommit(false);
                try (PreparedStatement s = con.prepareStatement(
                        "INSERT INTO tblProduct4(productCode,name,description,salePrice,image,saleStatus,createdAt,updatedAt) VALUES(?,?,?,?,?,?,?,NULL)", Statement.RETURN_GENERATED_KEYS)) {
                    s.setString(1, dish.getProductCode()); s.setString(2, dish.getName());
                    s.setString(3, dish.getDescription()); s.setFloat(4, dish.getSalePrice());
                    s.setString(5, dish.getImage()); s.setString(6, dish.getSaleStatus()); s.setTimestamp(7, createdAt);
                    s.executeUpdate();
                    try (ResultSet rs = s.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Không có khóa sinh tự động.");
                        id = rs.getInt(1);
                    }
                }
                try (PreparedStatement s = con.prepareStatement("INSERT INTO tblDish4(tblDishCategory4id,tblProduct4id) VALUES(?,?)")) {
                    s.setInt(1, idDishCategory); s.setInt(2, id); s.executeUpdate();
                }
                con.commit();
                dish.setId(id); dish.setCreatedAt(createdAt); dish.setUpdatedAt(null);
                // [v3-35][v2-21] Hàm saveDish() trả kết quả món ăn vừa tạo cho trang doSaveDish4.jsp.
                return dish;
            } catch (Exception e) {
                // [E2][v3-35/E2] Chưa tạo được món ăn.
                try { con.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                return null;
            } finally {
                try { con.setAutoCommit(true); }
                catch (SQLException e) { throw new IllegalStateException("Không thể khôi phục kết nối.", e); }
            }
        }
    }
}
