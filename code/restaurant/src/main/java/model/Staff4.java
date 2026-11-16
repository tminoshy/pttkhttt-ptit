/* ===================================================================
 * LỚP      : Staff4 (tầng thực thể, abstract)
 * THIẾT KẾ : class-design-add-dish.puml – hộp Staff4 (Hình 5, desing.md §4a)
 * MODULE 2 : Staff4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: không có trên class-add-dish.puml; lấy từ class-entity-design.puml. Có mặt
 *            vì là lớp cha của ManagementStaff4 – kiểu của thuộc tính ẩn managementStaff.
 * QUAN HỆ  : Staff4 <|-- ManagementStaff4; Staff4 <|-- SalesStaff4; Staff4 *-- Account4.
 * CSDL     : tblStaff4 (Account4 gộp 1–1 vào tblStaff4; cột vaitro phân loại lớp con).
 * KỊCH BẢN : không thuộc v.3; chỉ phục vụ tiền điều kiện đã đăng nhập.
 * KỊCH BẢN MODULE 2: không có bước riêng; phục vụ tiền điều kiện đăng nhập, managementStaff trong session.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public abstract class Staff4 {
    private int id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String workingStatus;
    private Account4 account;

    /** Staff4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public Staff4() {}

    /** getId() : int; trả thuộc tính id, không truy vấn CSDL. */
    public int getId() { return id; }

    /** setId(id : int); đóng gói thuộc tính id, không truy vấn CSDL. */
    public void setId(int id) { this.id = id; }

    /** getFullName() : String; trả thuộc tính fullName, không truy vấn CSDL. */
    public String getFullName() { return fullName; }

    /** setFullName(fullName : String); đóng gói thuộc tính fullName, không truy vấn CSDL. */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /** getPhoneNumber() : String; trả thuộc tính phoneNumber, không truy vấn CSDL. */
    public String getPhoneNumber() { return phoneNumber; }

    /** setPhoneNumber(phoneNumber : String); đóng gói thuộc tính phoneNumber, không truy vấn CSDL. */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /** getEmail() : String; trả thuộc tính email, không truy vấn CSDL. */
    public String getEmail() { return email; }

    /** setEmail(email : String); đóng gói thuộc tính email, không truy vấn CSDL. */
    public void setEmail(String email) { this.email = email; }

    /** getWorkingStatus() : String; trả thuộc tính workingStatus, không truy vấn CSDL. */
    public String getWorkingStatus() { return workingStatus; }

    /** setWorkingStatus(workingStatus : String); đóng gói thuộc tính workingStatus, không truy vấn CSDL. */
    public void setWorkingStatus(String workingStatus) { this.workingStatus = workingStatus; }

    /** getAccount() : Account4; trả thuộc tính account, không truy vấn CSDL. */
    public Account4 getAccount() { return account; }

    /** setAccount(account : Account4); đóng gói thuộc tính account, không truy vấn CSDL. */
    public void setAccount(Account4 account) { this.account = account; }
}
