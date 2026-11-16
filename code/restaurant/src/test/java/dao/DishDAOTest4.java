/* ===================================================================
 * LỚP      : DishDAOTest4 (kiểm thử)
 * THIẾT KẾ : [NGOÀI THIẾT KẾ] Kiểm thử lớp DAO4, không có hộp trên Hình 5/Hình 6
 * PHÂN TÍCH: Không có lớp phân tích riêng, kiểm chứng các hành vi DAO4 theo kịch bản.
 * CSDL     : tblProduct4, tblDish4, tblDishCategory4, tblStaff4 theo lớp tương ứng.
 * KỊCH BẢN : Không thuộc luồng tương tác trực tiếp
 * NGOẠI LỆ : A1, A2, E1, E2 theo phương thức; POJO chỉ đóng gói dữ liệu.
 * [NGOÀI THIẾT KẾ] Kiểm tra E1,A2,E2 trên MySQL thật.
 * =================================================================== */
package dao;

import model.Dish4;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class DishDAOTest4 extends DatabaseTest4 {
    Dish4 valid() {
        Dish4 d=new Dish4(); d.setProductCode("D004");d.setName("Cơm gà");d.setSalePrice(65000);d.setSaleStatus("Đang kinh doanh");return d;
    }
    @Test void validDish() throws Exception { assertArrayEquals(new String[0],new DishDAO4().validateDish(valid(),category())); }
    @Test void duplicateCode() throws Exception {
        Dish4 d=valid();d.setProductCode("D001");assertArrayEquals(new String[]{"inDishCode"},new DishDAO4().validateDish(d,category()));
    }
    @Test void productCodeIsSharedWithOtherProducts() throws Exception {
        try(var s=DAO4.con.createStatement()) { s.executeUpdate("INSERT INTO tblProduct4(productCode,name,salePrice,saleStatus,createdAt) VALUES('C999','Combo4',100,'Đang kinh doanh',NOW())"); }
        Dish4 d=valid();d.setProductCode("C999"); assertArrayEquals(new String[]{"inDishCode"},new DishDAO4().validateDish(d,category()));
    }
    @Test void emptyName() throws Exception { Dish4 d=valid();d.setName("  ");assertArrayEquals(new String[]{"inDishName"},new DishDAO4().validateDish(d,category())); }
    @ParameterizedTest @ValueSource(floats={0,-1,Float.NaN,Float.POSITIVE_INFINITY})
    void invalidPrice(float price) throws Exception { Dish4 d=valid();d.setSalePrice(price);assertArrayEquals(new String[]{"inSalePrice"},new DishDAO4().validateDish(d,category())); }
    @Test void unknownCategory() { assertArrayEquals(new String[]{"inoutDishCategory"},new DishDAO4().validateDish(valid(),999999)); }
    @Test void formOrder() {
        Dish4 d=valid();d.setProductCode("");d.setName("");d.setSalePrice(0);d.setSaleStatus("");
        assertArrayEquals(new String[]{"inDishCode","inDishName","inSalePrice","inSaleStatus","inoutDishCategory"},new DishDAO4().validateDish(d,999999));
    }
    @Test void noCategoriesOnlyError() throws Exception {
        try(var s=DAO4.con.createStatement()) { s.executeUpdate("DELETE FROM tblDish4");s.executeUpdate("DELETE FROM tblDishCategory4"); }
        assertArrayEquals(new String[]{"khongConLoaiMon"},new DishDAO4().validateDish(new Dish4(),0));
    }
    @Test void saveSuccess() throws Exception {
        Dish4 d=valid();assertSame(d,new DishDAO4().saveDish(d,category()));assertTrue(d.getId()>0);assertNotNull(d.getCreatedAt());assertNull(d.getUpdatedAt());
        assertEquals(1,count("SELECT COUNT(*) FROM tblDish4 WHERE tblProduct4id="+d.getId()));
        assertEquals(1,count("SELECT COUNT(*) FROM tblProduct4 WHERE id="+d.getId()+" AND productCode='D004' AND createdAt IS NOT NULL AND updatedAt IS NULL"));
        assertTrue(DAO4.con.getAutoCommit());
    }
    @Test void failedForeignKeyRollsBackProduct() throws Exception {
        Dish4 d=valid();assertNull(new DishDAO4().saveDish(d,999999));assertEquals(0,d.getId());assertNull(d.getCreatedAt());
        assertEquals(0,count("SELECT COUNT(*) FROM tblProduct4 WHERE productCode='D004'"));assertEquals(4,count("SELECT COUNT(*) FROM tblDish4"));assertTrue(DAO4.con.getAutoCommit());
        assertNotNull(new DishDAO4().saveDish(valid(),category()));
    }
    @Test void duplicateBetweenValidationAndSave() throws Exception {
        DishDAO4 dao=new DishDAO4();Dish4 first=valid();Dish4 second=valid();
        assertArrayEquals(new String[0],dao.validateDish(first,category()));assertNotNull(dao.saveDish(second,category()));assertNull(dao.saveDish(first,category()));
        assertEquals(1,count("SELECT COUNT(*) FROM tblProduct4 WHERE productCode='D004'"));assertEquals(5,count("SELECT COUNT(*) FROM tblDish4"));
    }
    @Test void seedDishesInCodeOrder() {
        Dish4[] dishes=new DishDAO4().getDishes();assertEquals(4,dishes.length);
        assertArrayEquals(new String[]{"D001","D002","D003","D005"},java.util.Arrays.stream(dishes).map(Dish4::getProductCode).toArray(String[]::new));
        assertEquals("Lẩu hải sản",dishes[0].getName());assertEquals(650000,dishes[0].getSalePrice());assertNotNull(dishes[0].getCreatedAt());assertNull(dishes[0].getImage());
    }
}
