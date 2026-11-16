<%-- ===================================================================
 LỚP      : orderDetailView4.jsp (tầng giao diện)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp orderDetailView4.jsp (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrder4, tblCustomer4, tblInvoice4, tblPayment4 và các dòng/mặt hàng qua DAO4.
 KỊCH BẢN : module 2, tk-v.3 33–34, 38–43, 47–48; v.2 23–29.
 NGOẠI LỆ : E2b.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*,java.util.Date" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%@ include file="/WEB-INF/auth.jspf" %>
<%@ include file="/WEB-INF/statistics.jspf" %>
<%
int idOrder=id(p(request,"idOrder")),idCustomer=id(p(request,"idCustomer"));
Date periodStart=day(p(request,"periodStart")),periodEnd=day(p(request,"periodEnd"));
Order4 order=null;
CustomerStatistic4 customerStatistic=null;
OrderSubmissionProduct4[] orderSubmissionProducts=new OrderSubmissionProduct4[0];
String outMessage="";
try {
    if(idOrder>0 && idCustomer>=0 && periodStart!=null && periodEnd!=null && !periodStart.after(periodEnd)) {
        // [tk-v3-33][tk-v2-23] Trang orderDetailView4.jsp gọi lớp OrderDAO4 yêu cầu lấy thông tin đơn đã chọn.
        OrderDAO4 orderDAO=new OrderDAO4();
        // [tk-v3-34][tk-v2-24] Lớp OrderDAO4 gọi hàm getOrder().
        order=orderDAO.getOrder(idOrder,idCustomer,periodStart,periodEnd);
    }
    // [tk-v3-38][tk-v2-25] Hàm getOrder() trả kết quả cho trang orderDetailView4.jsp.
    if(order==null) {
        // [tk-v3-38/E2b] Không hiển thị chi tiết cũ, quay về danh sách đơn cùng ngữ cảnh.
        outMessage="Data has changed: the selected order is no longer valid.";
    } else {
        // [tk-v3-39][tk-v2-25] Trang orderDetailView4.jsp gọi lớp CustomerStatistic4 yêu cầu đóng gói thông tin khách hàng và kỳ.
        customerStatistic=new CustomerStatistic4();
        // [tk-v3-40][tk-v2-25] Lớp CustomerStatistic4 gọi các hàm set thuộc tính tương ứng.
        customerStatistic.setId(idCustomer);
        Customer4 customer=order.getCustomer();
        customerStatistic.setCustomerCode(customer==null ? "" : customer.getCustomerCode());
        customerStatistic.setFullName(customer==null ? "No name" : customer.getFullName());
        customerStatistic.setPhoneNumber(customer==null ? null : customer.getPhoneNumber());
        customerStatistic.setProfileStatus(customer==null ? null : customer.getProfileStatus());
        customerStatistic.setMembershipCards(new MembershipCard4[0]); // Không tải vì trang không xem thẻ.
        customerStatistic.setPeriodStart(periodStart); customerStatistic.setPeriodEnd(periodEnd);
        customerStatistic.setTotalSpending(0); // Trang không hiển thị tổng chi tiêu cả kỳ.
        // [tk-v3-41][tk-v2-25] Lớp CustomerStatistic4 trả đối tượng về cho trang orderDetailView4.jsp.
        request.setAttribute("customerStatistic",customerStatistic);
        // [tk-v3-42][tk-v2-26] Trang orderDetailView4.jsp gọi lớp OrderSubmissionProductDAO4 yêu cầu lấy các dòng tính tiền của đơn.
        OrderSubmissionProductDAO4 orderSubmissionProductDAO=new OrderSubmissionProductDAO4();
        // [tk-v3-43][tk-v2-27] Lớp OrderSubmissionProductDAO4 gọi hàm getOrderSubmissionProducts().
        orderSubmissionProducts=orderSubmissionProductDAO.getOrderSubmissionProducts(idOrder,idCustomer,periodStart,periodEnd);
        // [tk-v3-47][tk-v2-28] Hàm getOrderSubmissionProducts() trả kết quả cho trang orderDetailView4.jsp.
        if(orderSubmissionProducts.length==0) {
            // [tk-v3-47/E2b] Kiểm tra lại sau khi lấy thông tin đơn, không hiển thị dữ liệu dở dang.
            order=null;customerStatistic=null;
            outMessage="Data has changed: the selected order is no longer valid.";
        }
    }
} catch(IllegalStateException e) { order=null;customerStatistic=null;orderSubmissionProducts=new OrderSubmissionProduct4[0];outMessage="The report could not be generated."; }
// Các thuộc tính ẩn đúng Hình 6, không giữ bản sao trong session.
request.setAttribute("order",order);request.setAttribute("customerStatistic",customerStatistic);
request.setAttribute("orderSubmissionProducts",orderSubmissionProducts);
%>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Order details</title>
<% if(order==null) { %><meta http-equiv="refresh" content="2;url=customerSpendingView4.jsp?idCustomer=<%= idCustomer %>&amp;periodStart=<%= q(p(request,"periodStart")) %>&amp;periodEnd=<%= q(p(request,"periodEnd")) %>"><% } %>
</head><body><h1>Order details</h1>
<%-- outMessage : Label --%><p id="outMessage" name="outMessage" role="status"><%= h(outMessage) %></p>
<% if(order!=null) { %>
<%-- [tk-v3-48][tk-v2-29] Trang orderDetailView4.jsp hiển thị chi tiết đơn cho nhân viên quản lý. Use case kết thúc. --%>
<%-- outOrderCode, outCustomerInfo, outPeriod, outInvoiceCode, outPaidAt, outTotalAmount : Label --%>
<p>Order code: <span id="outOrderCode" name="outOrderCode"><%= h(order.getOrderCode()) %></span></p>
<p>Customer: <span id="outCustomerInfo" name="outCustomerInfo"><%= h(customerStatistic.getCustomerCode()) %> <%= h(customerStatistic.getFullName()) %></span></p>
<p>Period: <span id="outPeriod" name="outPeriod"><%= date(periodStart) %> – <%= date(periodEnd) %></span></p>
<p>Invoice code: <span id="outInvoiceCode" name="outInvoiceCode"><%= h(order.getInvoice().getInvoiceCode()) %></span></p>
<p>Paid at: <span id="outPaidAt" name="outPaidAt"><%= time(paidAt(order.getInvoice())) %></span></p>
<%-- outOrderLines : Table --%>
<table id="outOrderLines" name="outOrderLines"><thead><tr><th>No.</th><th>Product code</th><th>Dish or combo</th><th>Quantity</th><th>Unit price (VND)</th><th>Line total (VND)</th></tr></thead><tbody>
<% int row=0;for(OrderSubmissionProduct4 line:orderSubmissionProducts) { %>
<tr><td><%= ++row %></td><td><%= h(line.getProduct().getProductCode()) %></td><td><%= h(line.getProduct().getName()) %></td><td><%= line.getQuantity() %></td><td><%= money(line.getRecordedUnitPrice()) %></td><td><%= money(line.getQuantity()*line.getRecordedUnitPrice()) %></td></tr>
<% } %></tbody></table>
<p>Total amount: <span id="outTotalAmount" name="outTotalAmount"><%= money(order.getInvoice().getAmountDue()) %></span></p><% } %>
<%-- subBackToOrderList : submit, giữ khách/nhóm và kỳ bằng tham số DAO4. --%>
<form action="customerSpendingView4.jsp"><input type="hidden" name="idCustomer" value="<%= idCustomer %>"><input type="hidden" name="periodStart" value="<%= h(p(request,"periodStart")) %>"><input type="hidden" name="periodEnd" value="<%= h(p(request,"periodEnd")) %>"><input type="submit" id="subBackToOrderList" name="subBackToOrderList" value="Back to order list"></form>
</body></html>
