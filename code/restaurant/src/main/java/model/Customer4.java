/* ===================================================================
 LỚP      : Customer4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp Customer4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblCustomer4.
 KỊCH BẢN : module 2, tk-v.3 24, 36; thông tin khách trong Order4.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;

public class Customer4 {
    private int id;
    private String customerCode;
    private String fullName;
    private String phoneNumber;
    private String profileStatus;
    private MembershipCard4[] membershipCards;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public Customer4() {}
    /** Trả thuộc tính id, không đọc CSDL. */
    public int getId() { return id; }
    /** Đóng gói thuộc tính id, không ghi CSDL. */
    public void setId(int id) { this.id = id; }
    /** Trả thuộc tính customerCode, không đọc CSDL. */
    public String getCustomerCode() { return customerCode; }
    /** Đóng gói thuộc tính customerCode, không ghi CSDL. */
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    /** Trả thuộc tính fullName, không đọc CSDL. */
    public String getFullName() { return fullName; }
    /** Đóng gói thuộc tính fullName, không ghi CSDL. */
    public void setFullName(String fullName) { this.fullName = fullName; }
    /** Trả thuộc tính phoneNumber, không đọc CSDL. */
    public String getPhoneNumber() { return phoneNumber; }
    /** Đóng gói thuộc tính phoneNumber, không ghi CSDL. */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    /** Trả thuộc tính profileStatus, không đọc CSDL. */
    public String getProfileStatus() { return profileStatus; }
    /** Đóng gói thuộc tính profileStatus, không ghi CSDL. */
    public void setProfileStatus(String profileStatus) { this.profileStatus = profileStatus; }
    /** Trả thuộc tính membershipCards, không đọc CSDL. */
    public MembershipCard4[] getMembershipCards() { return membershipCards; }
    /** Đóng gói thuộc tính membershipCards, không ghi CSDL. */
    public void setMembershipCards(MembershipCard4[] membershipCards) { this.membershipCards = membershipCards; }
}
