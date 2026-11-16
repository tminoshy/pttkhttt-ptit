/* ===================================================================
 LỚP      : CustomerStatisticDAO4 (tầng thao tác dữ liệu)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp CustomerStatisticDAO4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblCustomer4, tblOrder4, tblInvoice4, tblPayment4.
 KỊCH BẢN : module 2, tk-v.3 10–17.
 NGOẠI LỆ : E1, A1, E3.
 =================================================================== */
package dao;

import model.*;
import java.sql.*;
import java.util.Date;
import java.util.ArrayList;

public class CustomerStatisticDAO4 extends DAO4 {
    /** CustomerStatisticDAO4(): dùng kết nối chung của DAO4, không truy vấn bảng. */
    public CustomerStatisticDAO4() { super(); }

    /** validatePeriod(periodStart : Date, periodEnd : Date) : boolean.
     * Từ CustomerStatistic4.validatePeriod(), tk-v.3 10–11, v.2 9–10.
     * Không đọc CSDL, false khi thiếu ngày hoặc ngày đầu sau ngày cuối (E1).
     */
    // [tk-v3-10][tk-v2-9] Lớp CustomerStatisticDAO4 gọi hàm validatePeriod().
    public boolean validatePeriod(Date periodStart, Date periodEnd) {
        // [tk-v3-11/E1] Không lập báo cáo khi thiếu ngày hoặc khoảng ngày đảo ngược.
        boolean valid = periodStart != null && periodEnd != null && !periodStart.after(periodEnd);
        // [tk-v3-11][tk-v2-10] Hàm validatePeriod() trả kết quả hợp lệ cho trang customerStatisticsView4.jsp.
        return valid;
    }

    /** getCustomerStatistics(periodStart : Date, periodEnd : Date) : CustomerStatistic4[].
     * Từ CustomerStatistic4.getCustomerStatistics(), tk-v.3 13–17, v.2 12–13.
     * Đọc tblCustomer4, tblOrder4, tblInvoice4, tblPayment4. Mỗi hóa đơn đã thanh toán
     * tính một lần tại lần thanh toán thành công sớm nhất, kỳ bao gồm cả ngày cuối.
     * Trả mảng giảm dần, mảng rỗng A1, null E3 khi lỗi SQL, không ghi dữ liệu.
     */
    // [tk-v3-13][tk-v2-12] Lớp CustomerStatisticDAO4 gọi hàm getCustomerStatistics().
    public CustomerStatistic4[] getCustomerStatistics(Date periodStart, Date periodEnd) {
        synchronized (DAO4.class) {
            ArrayList<CustomerStatistic4> list = new ArrayList<>();
            String sql = "SELECT COALESCE(c.id,0) id, COALESCE(c.customerCode,'') customerCode, "
                + "COALESCE(c.fullName,'No name') fullName, c.phoneNumber,c.profileStatus, SUM(i.amountDue) totalSpending "
                + "FROM tblOrder4 o JOIN tblInvoice4 i ON i.tblOrder4id=o.id "
                + "JOIN (SELECT tblInvoice4id, MIN(paidAt) paidAt FROM tblPayment4 WHERE result='Thành công' GROUP BY tblInvoice4id) paid ON paid.tblInvoice4id=i.id "
                + "LEFT JOIN tblCustomer4 c ON c.id=o.tblCustomer4id "
                + "WHERE i.paymentStatus='Đã thanh toán' AND paid.paidAt>=? AND paid.paidAt<DATE_ADD(?,INTERVAL 1 DAY) "
                + "GROUP BY c.id,c.customerCode,c.fullName,c.phoneNumber,c.profileStatus ORDER BY totalSpending DESC,id";
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setDate(1,new java.sql.Date(periodStart.getTime()));
                statement.setDate(2,new java.sql.Date(periodEnd.getTime()));
                try (ResultSet rs=statement.executeQuery()) {
                    while (rs.next()) {
                        // [tk-v3-14][tk-v2-12] Hàm getCustomerStatistics() gọi lớp CustomerStatistic4 để đóng gói thông tin.
                        CustomerStatistic4 statistic = new CustomerStatistic4();
                        // [tk-v3-15][tk-v2-12] Lớp CustomerStatistic4 đóng gói thông tin thực thể.
                        statistic.setId(rs.getInt("id")); statistic.setCustomerCode(rs.getString("customerCode"));
                        statistic.setFullName(rs.getString("fullName")); statistic.setPhoneNumber(rs.getString("phoneNumber"));
                        statistic.setProfileStatus(rs.getString("profileStatus"));
                        // Không tải vì báo cáo chỉ dùng thông tin khách, không xem thẻ thành viên.
                        statistic.setMembershipCards(new MembershipCard4[0]);
                        statistic.setPeriodStart(periodStart); statistic.setPeriodEnd(periodEnd);
                        statistic.setTotalSpending(rs.getFloat("totalSpending"));
                        // [tk-v3-16][tk-v2-12] Lớp CustomerStatistic4 trả kết quả cho hàm getCustomerStatistics().
                        list.add(statistic);
                    }
                }
                // [tk-v3-17/A1] Mảng rỗng khi không có hóa đơn hợp lệ trong kỳ.
                // [tk-v3-17][tk-v2-13] Hàm getCustomerStatistics() trả kết quả cho trang customerStatisticsView4.jsp.
                return list.toArray(new CustomerStatistic4[0]);
            } catch (SQLException e) {
                // [tk-v3-17/E3] Tổng hợp thất bại, không trả số liệu dở dang.
                return null;
            }
        }
    }
}
