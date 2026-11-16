/* ===================================================================
 LỚP      : OrderDAO4 (tầng thao tác dữ liệu)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp OrderDAO4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrder4, tblCustomer4, tblInvoice4, tblPayment4.
 KỊCH BẢN : module 2, tk-v.3 22–26, 34–38.
 NGOẠI LỆ : E2a, E2b, lỗi SQL báo IllegalStateException.
 =================================================================== */
package dao;

import model.*;
import java.sql.*;
import java.util.Date;
import java.util.ArrayList;

public class OrderDAO4 extends DAO4 {
    /** OrderDAO4(): dùng kết nối chung, không truy vấn bảng. */
    public OrderDAO4() { super(); }

    /** getOrders(int idCustomer, Date periodStart, Date periodEnd) : Order4[].
     * Từ Order4.getOrders(), tk-v.3 22–26, v.2 18–19.
     * Đọc tblOrder4, tblCustomer4, tblInvoice4, tblPayment4 theo khách/nhóm và kỳ,
     * hóa đơn chỉ tính tại lần thanh toán thành công sớm nhất.
     * Mảng rỗng là E2a, lỗi SQL ném IllegalStateException, không ghi dữ liệu.
     */
    // [tk-v3-22][tk-v2-18] Lớp OrderDAO4 gọi hàm getOrders().
    public Order4[] getOrders(int idCustomer, Date periodStart, Date periodEnd) {
        synchronized (DAO4.class) {
            ArrayList<Order4> orders = new ArrayList<>();
            String sql = "SELECT o.id,o.orderCode,o.createdAt,o.status,c.id customerId,c.customerCode,c.fullName,c.phoneNumber,c.profileStatus, "
                + "i.id invoiceId,i.invoiceCode,i.issuedAt,i.amountDue,i.paymentStatus "
                + "FROM tblOrder4 o JOIN tblInvoice4 i ON i.tblOrder4id=o.id "
                + "JOIN (SELECT tblInvoice4id,MIN(paidAt) paidAt FROM tblPayment4 WHERE result='Thành công' GROUP BY tblInvoice4id) paid ON paid.tblInvoice4id=i.id "
                + "LEFT JOIN tblCustomer4 c ON c.id=o.tblCustomer4id "
                + "WHERE i.paymentStatus='Đã thanh toán' AND paid.paidAt>=? AND paid.paidAt<DATE_ADD(?,INTERVAL 1 DAY) "
                + "AND COALESCE(o.tblCustomer4id,0)=? ORDER BY paid.paidAt,o.id ";
            try (PreparedStatement statement=con.prepareStatement(sql)) {
                statement.setDate(1,new java.sql.Date(periodStart.getTime()));
                statement.setDate(2,new java.sql.Date(periodEnd.getTime()));
                statement.setInt(3,idCustomer);
                try (ResultSet rs=statement.executeQuery()) {
                    while (rs.next()) {
                        // [tk-v3-23][tk-v2-18] Hàm getOrders() gọi lớp Order4 để đóng gói thông tin.
                        Order4 order = new Order4();
                        // [tk-v3-24][tk-v2-18] Lớp Order4 đóng gói thông tin thực thể.
                        fillOrder(order,rs);
                        // Không tải orderSubmissions vì các dòng được lấy riêng ở trang chi tiết.
                        // Không tải salesStaff vì báo cáo không hiển thị nhân viên bán hàng.
                        ArrayList<Payment4> payments = new ArrayList<>();
                        try (PreparedStatement paymentStatement=con.prepareStatement("SELECT id,amount,method,paidAt,result FROM tblPayment4 WHERE tblInvoice4id=? ORDER BY paidAt,id")) {
                            paymentStatement.setInt(1,order.getInvoice().getId());
                            try (ResultSet paymentRows=paymentStatement.executeQuery()) {
                                while (paymentRows.next()) {
                                    Payment4 payment = new Payment4();
                                    payment.setId(paymentRows.getInt("id")); payment.setAmount(paymentRows.getFloat("amount"));
                                    payment.setMethod(paymentRows.getString("method")); payment.setPaidAt(paymentRows.getTimestamp("paidAt"));
                                    payment.setResult(paymentRows.getString("result"));
                                    payment.setSalesStaff(null); // Không tải vì báo cáo không hiển thị nhân viên.
                                    payments.add(payment);
                                }
                            }
                        }
                        order.getInvoice().setPayments(payments.toArray(new Payment4[0]));
                        // [tk-v3-25][tk-v2-18] Lớp Order4 trả kết quả cho hàm getOrders().
                        orders.add(order);
                    }
                }
                // [tk-v3-26/E2a] Dữ liệu đã thay đổi hoặc không phù hợp bộ lọc.
                // [tk-v3-26][tk-v2-19] Hàm getOrders() trả kết quả cho trang customerSpendingView4.jsp.
                return orders.toArray(new Order4[0]);
            } catch (SQLException e) {
                throw new IllegalStateException("Không thể lấy thông tin đơn.",e);
            }
        }
    }

    /** getOrder(int idOrder, int idCustomer, Date periodStart, Date periodEnd) : Order4.
     * Từ Order4.getOrder(), tk-v.3 34–38, v.2 24–25.
     * Đọc tblOrder4, tblCustomer4, tblInvoice4, tblPayment4 theo khách/nhóm và kỳ,
     * hóa đơn chỉ tính tại lần thanh toán thành công sớm nhất.
     * null là E2b, lỗi SQL ném IllegalStateException, không ghi dữ liệu.
     */
    // [tk-v3-34][tk-v2-24] Lớp OrderDAO4 gọi hàm getOrder().
    public Order4 getOrder(int idOrder, int idCustomer, Date periodStart, Date periodEnd) {
        synchronized (DAO4.class) {
            ArrayList<Order4> orders = new ArrayList<>();
            String sql = "SELECT o.id,o.orderCode,o.createdAt,o.status,c.id customerId,c.customerCode,c.fullName,c.phoneNumber,c.profileStatus, "
                + "i.id invoiceId,i.invoiceCode,i.issuedAt,i.amountDue,i.paymentStatus "
                + "FROM tblOrder4 o JOIN tblInvoice4 i ON i.tblOrder4id=o.id "
                + "JOIN (SELECT tblInvoice4id,MIN(paidAt) paidAt FROM tblPayment4 WHERE result='Thành công' GROUP BY tblInvoice4id) paid ON paid.tblInvoice4id=i.id "
                + "LEFT JOIN tblCustomer4 c ON c.id=o.tblCustomer4id "
                + "WHERE i.paymentStatus='Đã thanh toán' AND paid.paidAt>=? AND paid.paidAt<DATE_ADD(?,INTERVAL 1 DAY) "
                + "AND COALESCE(o.tblCustomer4id,0)=? AND o.id=? ";
            try (PreparedStatement statement=con.prepareStatement(sql)) {
                statement.setDate(1,new java.sql.Date(periodStart.getTime()));
                statement.setDate(2,new java.sql.Date(periodEnd.getTime()));
                statement.setInt(3,idCustomer);
                statement.setInt(4,idOrder);
                try (ResultSet rs=statement.executeQuery()) {
                    while (rs.next()) {
                        // [tk-v3-35][tk-v2-24] Hàm getOrder() gọi lớp Order4 để đóng gói thông tin.
                        Order4 order = new Order4();
                        // [tk-v3-36][tk-v2-24] Lớp Order4 đóng gói thông tin thực thể.
                        fillOrder(order,rs);
                        // Không tải orderSubmissions vì các dòng được lấy riêng ở trang chi tiết.
                        // Không tải salesStaff vì báo cáo không hiển thị nhân viên bán hàng.
                        ArrayList<Payment4> payments = new ArrayList<>();
                        try (PreparedStatement paymentStatement=con.prepareStatement("SELECT id,amount,method,paidAt,result FROM tblPayment4 WHERE tblInvoice4id=? ORDER BY paidAt,id")) {
                            paymentStatement.setInt(1,order.getInvoice().getId());
                            try (ResultSet paymentRows=paymentStatement.executeQuery()) {
                                while (paymentRows.next()) {
                                    Payment4 payment = new Payment4();
                                    payment.setId(paymentRows.getInt("id")); payment.setAmount(paymentRows.getFloat("amount"));
                                    payment.setMethod(paymentRows.getString("method")); payment.setPaidAt(paymentRows.getTimestamp("paidAt"));
                                    payment.setResult(paymentRows.getString("result"));
                                    payment.setSalesStaff(null); // Không tải vì báo cáo không hiển thị nhân viên.
                                    payments.add(payment);
                                }
                            }
                        }
                        order.getInvoice().setPayments(payments.toArray(new Payment4[0]));
                        // [tk-v3-37][tk-v2-24] Lớp Order4 trả kết quả cho hàm getOrder().
                        orders.add(order);
                    }
                }
                // [tk-v3-38/E2b] Dữ liệu đã thay đổi hoặc không phù hợp bộ lọc.
                // [tk-v3-38][tk-v2-25] Hàm getOrder() trả kết quả cho trang orderDetailView4.jsp.
                return orders.isEmpty() ? null : orders.get(0);
            } catch (SQLException e) {
                throw new IllegalStateException("Không thể lấy thông tin đơn.",e);
            }
        }
    }

    /** [NGOÀI THIẾT KẾ] Dùng chung ánh xạ một dòng JDBC, không truy vấn hay thêm nghiệp vụ. */
    private void fillOrder(Order4 order, ResultSet rs) throws SQLException {
        order.setId(rs.getInt("id")); order.setOrderCode(rs.getString("orderCode"));
        order.setCreatedAt(rs.getTimestamp("createdAt")); order.setStatus(rs.getString("status"));
        order.setOrderSubmissions(new OrderSubmission4[0]); // Không tải vì DAO4 chi tiết lấy các dòng riêng.
        Customer4 customer=null;
        if (rs.getObject("customerId")!=null) {
            customer=new Customer4(); customer.setId(rs.getInt("customerId"));
            customer.setCustomerCode(rs.getString("customerCode")); customer.setFullName(rs.getString("fullName"));
            customer.setPhoneNumber(rs.getString("phoneNumber")); customer.setProfileStatus(rs.getString("profileStatus"));
            customer.setMembershipCards(new MembershipCard4[0]); // Không tải vì báo cáo không xem thẻ.
        }
        order.setCustomer(customer);
        Invoice4 invoice=new Invoice4(); invoice.setId(rs.getInt("invoiceId"));
        invoice.setInvoiceCode(rs.getString("invoiceCode")); invoice.setIssuedAt(rs.getTimestamp("issuedAt"));
        invoice.setAmountDue(rs.getFloat("amountDue")); invoice.setPaymentStatus(rs.getString("paymentStatus"));
        invoice.setSalesStaff(null); // Không tải vì báo cáo không hiển thị nhân viên.
        order.setInvoice(invoice);
    }
}
