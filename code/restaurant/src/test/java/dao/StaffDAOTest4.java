/* ===================================================================
 * LỚP      : StaffDAOTest4 (kiểm thử)
 * THIẾT KẾ : [NGOÀI THIẾT KẾ] Kiểm thử lớp DAO4, không có hộp trên Hình 5/Hình 6
 * PHÂN TÍCH: Không có lớp phân tích riêng, kiểm chứng các hành vi DAO4 theo kịch bản.
 * CSDL     : tblProduct4, tblDish4, tblDishCategory4, tblStaff4 theo lớp tương ứng.
 * KỊCH BẢN : Không thuộc luồng tương tác trực tiếp
 * NGOẠI LỆ : Sai tài khoản, vai trò hoặc trạng thái, không thuộc nhánh nghiệp vụ.
 * [NGOÀI THIẾT KẾ] Kiểm tra tiền điều kiện đăng nhập và quyền quản lý.
 * =================================================================== */
package dao;
import model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StaffDAOTest4 extends DatabaseTest4 {
    ManagementStaff4 seeded() throws Exception {
        ManagementStaff4 staff=new ManagementStaff4();Account4 account=new Account4();
        try(var s=DAO4.con.createStatement();var rs=s.executeQuery("SELECT username,password FROM tblStaff4")) {rs.next();account.setUsername(rs.getString(1));account.setPassword(rs.getString(2));}
        staff.setAccount(account);return staff;
    }
    @Test void validLoginFillsStaff() throws Exception {var staff=seeded();assertTrue(new StaffDAO4().checkLogin(staff));assertTrue(staff.getId()>0);assertNotNull(staff.getFullName());assertNull(staff.getAccount().getPassword());}
    @Test void wrongPassword() throws Exception {var staff=seeded();staff.getAccount().setPassword("incorrect");assertFalse(new StaffDAO4().checkLogin(staff));}
    @Test void inactiveOrWrongRole() throws Exception {
        var staff=seeded();try(var s=DAO4.con.createStatement()) {s.executeUpdate("UPDATE tblStaff4 SET accountStatus='Khóa'");}assertFalse(new StaffDAO4().checkLogin(staff));
        try(var s=DAO4.con.createStatement()) {s.executeUpdate("UPDATE tblStaff4 SET accountStatus='Đang hoạt động',vaitro='SalesStaff4'");}assertFalse(new StaffDAO4().checkLogin(staff));
    }
}
