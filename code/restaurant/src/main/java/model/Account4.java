/* ===================================================================
 * LỚP      : Account4 (tầng thực thể)
 * THIẾT KẾ : class-design-add-dish.puml – hộp Account4 (Hình 5, desing.md §4a)
 * MODULE 2 : Account4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: không có trên class-add-dish.puml; lấy từ class-entity-design.puml. Có mặt
 *            vì Staff4 có thuộc tính account : Account4.
 * QUAN HỆ  : Staff4 *-- Account4.
 * CSDL     : các cột username, password, accountStatus của tblStaff4 (gộp 1–1).
 * KỊCH BẢN : không thuộc v.3; chỉ dùng khi đăng nhập (StaffDAO4.checkLogin).
 * KỊCH BẢN MODULE 2: không có bước riêng; phục vụ tiền điều kiện đăng nhập, managementStaff trong session.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public class Account4 {
    private int id;
    private String username;
    private String password;
    private String accountStatus;

    /** Account4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public Account4() {}

    /** getId() : int; trả thuộc tính id, không truy vấn CSDL. */
    public int getId() { return id; }

    /** setId(id : int); đóng gói thuộc tính id, không truy vấn CSDL. */
    public void setId(int id) { this.id = id; }

    /** getUsername() : String; trả thuộc tính username, không truy vấn CSDL. */
    public String getUsername() { return username; }

    /** setUsername(username : String); đóng gói thuộc tính username, không truy vấn CSDL. */
    public void setUsername(String username) { this.username = username; }

    /** getPassword() : String; trả thuộc tính password, không truy vấn CSDL. */
    public String getPassword() { return password; }

    /** setPassword(password : String); đóng gói thuộc tính password, không truy vấn CSDL. */
    public void setPassword(String password) { this.password = password; }

    /** getAccountStatus() : String; trả thuộc tính accountStatus, không truy vấn CSDL. */
    public String getAccountStatus() { return accountStatus; }

    /** setAccountStatus(accountStatus : String); đóng gói thuộc tính accountStatus, không truy vấn CSDL. */
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
}
