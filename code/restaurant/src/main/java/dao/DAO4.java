/* ===================================================================
 * LỚP      : DAO4 (tầng thao tác dữ liệu, abstract)
 * THIẾT KẾ : class-design-add-dish.puml – hộp DAO4: {static} #con : Connection, +DAO4()
 *            (Hình 5, desing.md §4a).
 * MODULE 2 : DAO4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: không có trên class-add-dish.puml; bổ sung theo §4.3.1 Bước 3 của giáo trình
 *            (lớp cha dùng chung kết nối CSDL).
 * QUAN HỆ  : DAO4 <|-- DishDAO4; DAO4 <|-- DishCategoryDAO4.
 * CSDL     : không đọc bảng nào; chỉ mở kết nối tới CSDL restaurant.
 * KỊCH BẢN : không có bước v.3 riêng; hàm tạo chạy khi trang tạo DishDAO4/DishCategoryDAO4
 *            (v.3 3, 9, 18, 30, 37, 46, 52).
 * KỊCH BẢN MODULE 2: tk-v.3 9–17, 21–26, 33–38, 42–47; dùng chung kết nối.
 * NGOẠI LỆ : không xử lý nhánh nào; lỗi kết nối ném IllegalStateException.
 * [NGOÀI THIẾT KẾ] Nạp db.properties; khóa DAO4.class bảo vệ kết nối dùng chung.
 * =================================================================== */
package dao;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

public abstract class DAO4 {
    protected static Connection con;

    /** DAO4(): mở một kết nối dùng chung khi chưa có/đã đóng.
     * Không có phương thức phân tích tương ứng; không trả dữ liệu nghiệp vụ.
     * Đọc db.properties; lỗi kết nối ném IllegalStateException, không giả danh A1.
     */
    public DAO4() {
        synchronized (DAO4.class) {
            try {
                if (con == null || con.isClosed()) {
                    Properties properties = new Properties();
                    String config = System.getProperty("db.config");
                    try (InputStream input = config == null
                            ? DAO4.class.getResourceAsStream("/db.properties") : Files.newInputStream(Path.of(config))) {
                        if (input == null) throw new IOException("Thiếu db.properties");
                        properties.load(input);
                    }
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    con = DriverManager.getConnection(properties.getProperty("url"),
                            properties.getProperty("user"), properties.getProperty("password"));
                }
            } catch (IOException | SQLException | ClassNotFoundException e) {
                throw new IllegalStateException("Không thể kết nối CSDL.", e);
            }
        }
    }
}
