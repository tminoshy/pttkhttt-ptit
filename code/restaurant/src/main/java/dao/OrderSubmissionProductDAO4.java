/* ===================================================================
 LỚP      : OrderSubmissionProductDAO4 (tầng thao tác dữ liệu)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp OrderSubmissionProductDAO4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrderSubmissionProduct4, tblOrderSubmission4, tblOrder4, tblInvoice4, tblPayment4, tblProduct4, tblDish4, tblCombo4.
 KỊCH BẢN : module 2, tk-v.3 43–47.
 NGOẠI LỆ : E2b, lỗi SQL báo IllegalStateException.
 =================================================================== */
package dao;

import model.*;
import java.sql.*;
import java.util.Date;
import java.util.ArrayList;

public class OrderSubmissionProductDAO4 extends DAO4 {
    /** OrderSubmissionProductDAO4(): dùng kết nối chung, không truy vấn bảng. */
    public OrderSubmissionProductDAO4() { super(); }

    /** getOrderSubmissionProducts(idOrder : int, idCustomer : int, periodStart : Date, periodEnd : Date) : OrderSubmissionProduct4[].
     * Từ OrderSubmissionProduct4.getOrderSubmissionProducts(), tk-v.3 43–47, v.2 27–28.
     * Đọc các bảng trong header, kiểm tra lại khách/nhóm và kỳ cùng hóa đơn hợp lệ.
     * Chỉ lấy dòng Đã chấp nhận, đóng gói Product4 thành Dish4 hoặc Combo4.
     * Mảng rỗng E2b khi đơn không còn hợp lệ, lỗi SQL ném IllegalStateException.
     */
    // [tk-v3-43][tk-v2-27] Lớp OrderSubmissionProductDAO4 gọi hàm getOrderSubmissionProducts().
    public OrderSubmissionProduct4[] getOrderSubmissionProducts(int idOrder, int idCustomer, Date periodStart, Date periodEnd) {
        synchronized (DAO4.class) {
            ArrayList<OrderSubmissionProduct4> lines=new ArrayList<>();
            String sql="SELECT line.*,p.*,line.id lineId,d.tblProduct4id dishId,c.tblProduct4id comboId,c.startsAt,c.endsAt "
                + "FROM tblOrderSubmissionProduct4 line JOIN tblOrderSubmission4 os ON os.id=line.tblOrderSubmission4id "
                + "JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblInvoice4 i ON i.tblOrder4id=o.id "
                + "JOIN (SELECT tblInvoice4id,MIN(paidAt) paidAt FROM tblPayment4 WHERE result='Thành công' GROUP BY tblInvoice4id) paid ON paid.tblInvoice4id=i.id "
                + "JOIN tblProduct4 p ON p.id=line.tblProduct4id LEFT JOIN tblDish4 d ON d.tblProduct4id=p.id LEFT JOIN tblCombo4 c ON c.tblProduct4id=p.id "
                + "WHERE o.id=? AND COALESCE(o.tblCustomer4id,0)=? AND i.paymentStatus='Đã thanh toán' "
                + "AND paid.paidAt>=? AND paid.paidAt<DATE_ADD(?,INTERVAL 1 DAY) AND line.processingStatus='Đã chấp nhận' ORDER BY os.id,line.id";
            try (PreparedStatement statement=con.prepareStatement(sql)) {
                statement.setInt(1,idOrder); statement.setInt(2,idCustomer);
                statement.setDate(3,new java.sql.Date(periodStart.getTime())); statement.setDate(4,new java.sql.Date(periodEnd.getTime()));
                try (ResultSet rs=statement.executeQuery()) {
                    while(rs.next()) {
                        // [tk-v3-44][tk-v2-27] Hàm getOrderSubmissionProducts() gọi lớp OrderSubmissionProduct4 để đóng gói thông tin.
                        OrderSubmissionProduct4 line=new OrderSubmissionProduct4();
                        // [tk-v3-45][tk-v2-27] Lớp OrderSubmissionProduct4 đóng gói thông tin thực thể.
                        line.setId(rs.getInt("lineId")); line.setQuantity(rs.getInt("quantity"));
                        line.setRecordedUnitPrice(rs.getFloat("recordedUnitPrice")); line.setNote(rs.getString("note"));
                        line.setProcessingStatus(rs.getString("processingStatus")); line.setProcessedAt(rs.getTimestamp("processedAt"));
                        line.setRejectionReason(rs.getString("rejectionReason"));
                        line.setSalesStaff(null); // Không tải vì chi tiết không hiển thị nhân viên.
                        Product4 product;
                        if(rs.getObject("dishId")!=null) product=new Dish4();
                        else if(rs.getObject("comboId")!=null) {
                            Combo4 combo=new Combo4(); combo.setStartsAt(rs.getTimestamp("startsAt")); combo.setEndsAt(rs.getTimestamp("endsAt"));
                            combo.setComboDishes(new ComboDish4[0]); // Không tải vì báo cáo chỉ xem combo đã gọi, không xem thành phần.
                            product=combo;
                        } else throw new SQLException("Mặt hàng không có lớp con Dish4 hoặc Combo4.");
                        product.setId(rs.getInt("tblProduct4id")); product.setProductCode(rs.getString("productCode"));
                        product.setName(rs.getString("name")); product.setDescription(rs.getString("description"));
                        product.setSalePrice(rs.getFloat("salePrice")); product.setImage(rs.getString("image"));
                        product.setSaleStatus(rs.getString("saleStatus")); product.setCreatedAt(rs.getTimestamp("createdAt"));
                        product.setUpdatedAt(rs.getTimestamp("updatedAt")); line.setProduct(product);
                        // [tk-v3-46][tk-v2-27] Lớp OrderSubmissionProduct4 trả kết quả cho hàm getOrderSubmissionProducts().
                        lines.add(line);
                    }
                }
                // [tk-v3-47/E2b] Đơn không còn hợp lệ hoặc không còn dòng tính tiền.
                // [tk-v3-47][tk-v2-28] Hàm getOrderSubmissionProducts() trả kết quả cho trang orderDetailView4.jsp.
                return lines.toArray(new OrderSubmissionProduct4[0]);
            } catch (SQLException e) { throw new IllegalStateException("Không thể lấy chi tiết đơn.",e); }
        }
    }
}
