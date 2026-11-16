/* ===================================================================
 * LỚP      : StaffDAO4 (đăng nhập)
 * [NGOÀI THIẾT KẾ] Không có hộp trên class-design-add-dish.puml hay biểu đồ nào khác;
 *            chỉ cung cấp tiền điều kiện "nhân viên quản lý đã đăng nhập" của use case.
 * PHÂN TÍCH: không có; tương tự ThanhvienDAO.kiemtraDangnhap() của giáo trình (Chương 5).
 * QUAN HỆ  : kế thừa DAO4; được gọi từ doLogin4.jsp; điền ManagementStaff4 và Account4.
 * CSDL     : tblStaff4 (đọc).
 * KỊCH BẢN : không thuộc v.3.
 * NGOẠI LỆ : không thuộc A1, A2, E1, E2; sai thông tin trả false.
 * =================================================================== */
package dao;

import model.Account4;
import model.ManagementStaff4;
import java.sql.*;

public class StaffDAO4 extends DAO4 {
    /** StaffDAO4(): dùng kết nối của DAO4; ngoài biểu đồ module. */
    public StaffDAO4() { super(); }

    /** checkLogin(managementStaff : ManagementStaff4) : boolean.
     * [NGOÀI THIẾT KẾ] Không có bước v.3; đọc tblStaff4, lọc vai trò/tài khoản hoạt động.
     * true: điền Staff4 và Account4 (id dùng chung sau gộp bảng); false: sai thông tin.
     */
    public boolean checkLogin(ManagementStaff4 managementStaff) {
        synchronized (DAO4.class) {
            Account4 account = managementStaff.getAccount();
            try (PreparedStatement s = con.prepareStatement("SELECT * FROM tblStaff4 WHERE username=? AND password=? AND vaitro=? AND accountStatus=?")) {
                s.setString(1, account.getUsername()); s.setString(2, account.getPassword());
                s.setString(3, "ManagementStaff4"); s.setString(4, "Đang hoạt động");
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) return false;
                    managementStaff.setId(rs.getInt("id")); managementStaff.setFullName(rs.getString("fullName"));
                    managementStaff.setPhoneNumber(rs.getString("phoneNumber")); managementStaff.setEmail(rs.getString("email"));
                    managementStaff.setWorkingStatus(rs.getString("workingStatus"));
                    account.setId(rs.getInt("id")); account.setAccountStatus(rs.getString("accountStatus"));
                    account.setPassword(null);
                    return true;
                }
            } catch (SQLException e) { throw new IllegalStateException("Không thể đăng nhập.", e); }
        }
    }
}
