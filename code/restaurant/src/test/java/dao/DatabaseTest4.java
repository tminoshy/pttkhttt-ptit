/* ===================================================================
 * LỚP      : DatabaseTest4 (kiểm thử)
 * THIẾT KẾ : [NGOÀI THIẾT KẾ] Hạ tầng kiểm thử dùng chung hai module, không có hộp thiết kế.
 * PHÂN TÍCH: Không có lớp phân tích riêng, kiểm chứng các hành vi DAO4 theo kịch bản.
 * CSDL     : Các bảng của module 1 và 2 trong schema.sql.
 * KỊCH BẢN : Không thuộc luồng tương tác trực tiếp
 * NGOẠI LỆ : A1, A2, E1, E2 theo phương thức; POJO chỉ đóng gói dữ liệu.
 * [NGOÀI THIẾT KẾ] Chỉ xóa/nạp lại restaurant_test trước từng ca.
 * =================================================================== */
package dao;

import java.nio.file.*;
import java.sql.*;
import org.junit.jupiter.api.*;

abstract class DatabaseTest4 {
    @BeforeEach
    void resetDatabase() throws Exception {
        String url=System.getProperty("test.db.url", "jdbc:mysql://127.0.0.1:3306/restaurant_test?characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh");
        // Chặn URL ngoài DB test trước khi thực hiện bất kỳ DDL nào.
        if (!url.matches("jdbc:mysql://[^/]+/restaurant_test(?:\\?.*)?")) throw new IllegalArgumentException("Chỉ được reset restaurant_test");
        synchronized (DAO4.class) {
            if (DAO4.con!=null) DAO4.con.close();
            DAO4.con=DriverManager.getConnection(url,System.getProperty("test.db.user","restaurant"),System.getProperty("test.db.password","restaurant_local"));
            try (Statement s=DAO4.con.createStatement(); ResultSet rs=s.executeQuery("SELECT DATABASE()")) {
                rs.next(); Assertions.assertEquals("restaurant_test",rs.getString(1));
            }
            try (Statement s=DAO4.con.createStatement()) {
                s.executeUpdate("DROP TABLE IF EXISTS tblOrderSubmissionProduct4");
                s.executeUpdate("DROP TABLE IF EXISTS tblOrderSubmission4");
                s.executeUpdate("DROP TABLE IF EXISTS tblPayment4");
                s.executeUpdate("DROP TABLE IF EXISTS tblInvoice4");
                s.executeUpdate("DROP TABLE IF EXISTS tblOrder4");
                s.executeUpdate("DROP TABLE IF EXISTS tblMembershipCard4");
                s.executeUpdate("DROP TABLE IF EXISTS tblCustomer4");
                s.executeUpdate("DROP TABLE IF EXISTS tblCombo4");
                s.executeUpdate("DROP TABLE IF EXISTS tblDish4");
                s.executeUpdate("DROP TABLE IF EXISTS tblProduct4");
                s.executeUpdate("DROP TABLE IF EXISTS tblDishCategory4");
                s.executeUpdate("DROP TABLE IF EXISTS tblStaff4");
                for (String file : new String[]{"schema.sql","seed.sql"}) {
                    String sql=Files.readString(Path.of("db",file)).replaceAll("(?m)^--.*$", "");
                    for (String statement : sql.split(";")) if (!statement.isBlank()) s.executeUpdate(statement);
                }
            }
        }
    }
    @AfterEach void closeConnection() throws Exception { if (DAO4.con!=null) { DAO4.con.close(); DAO4.con=null; } }
    int category() throws Exception {
        try (Statement s=DAO4.con.createStatement();ResultSet rs=s.executeQuery("SELECT id FROM tblDishCategory4 WHERE categoryCode='LM02'")) { rs.next();return rs.getInt(1); }
    }
    int count(String sql) throws Exception {
        try (Statement s=DAO4.con.createStatement();ResultSet rs=s.executeQuery(sql)) { rs.next();return rs.getInt(1); }
    }
}
