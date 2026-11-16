/* ===================================================================
 * LỚP      : DishCategoryDAOTest4 (kiểm thử)
 * THIẾT KẾ : [NGOÀI THIẾT KẾ] Kiểm thử lớp DAO4, không có hộp trên Hình 5/Hình 6
 * PHÂN TÍCH: Không có lớp phân tích riêng, kiểm chứng các hành vi DAO4 theo kịch bản.
 * CSDL     : tblProduct4, tblDish4, tblDishCategory4, tblStaff4 theo lớp tương ứng.
 * KỊCH BẢN : Không thuộc luồng tương tác trực tiếp
 * NGOẠI LỆ : A1, A2, E1, E2 theo phương thức; POJO chỉ đóng gói dữ liệu.
 * [NGOÀI THIẾT KẾ] Kiểm tra mảng loại và ánh xạ Dish4.id.
 * =================================================================== */
package dao;

import model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DishCategoryDAOTest4 extends DatabaseTest4 {
    @Test void seedCategoriesAndMemberIds() {
        DishCategory4[] categories=new DishCategoryDAO4().getDishCategories();assertEquals(3,categories.length);
        assertArrayEquals(new String[]{"LM01","LM02","LM03"},java.util.Arrays.stream(categories).map(DishCategory4::getCategoryCode).toArray(String[]::new));
        assertEquals(1,categories[0].getDishes().length);
        int[] actual=java.util.Arrays.stream(categories[1].getDishes()).mapToInt(Dish4::getId).sorted().toArray();
        int[] expected=java.util.Arrays.stream(new DishDAO4().getDishes()).filter(d -> !d.getProductCode().equals("D003") && !d.getProductCode().equals("D005")).mapToInt(Dish4::getId).sorted().toArray();
        assertArrayEquals(expected,actual);
    }
    @Test void noCategoriesReturnsEmptyArray() throws Exception {
        try(var s=DAO4.con.createStatement()) {s.executeUpdate("DELETE FROM tblDish4");s.executeUpdate("DELETE FROM tblDishCategory4");}
        assertEquals(0,new DishCategoryDAO4().getDishCategories().length);
    }
}
