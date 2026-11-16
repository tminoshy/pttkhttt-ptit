<%-- ===================================================================
 LỚP      : customerSpendingView4.jsp (tầng giao diện)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp customerSpendingView4.jsp (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrder4, tblCustomer4, tblInvoice4, tblPayment4 qua OrderDAO4.
 KỊCH BẢN : module 2, tk-v.3 21–22, 26–32; v.2 17–22.
 NGOẠI LỆ : E2a, A2.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*,java.util.Date" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%@ include file="/WEB-INF/auth.jspf" %>
<%@ include file="/WEB-INF/statistics.jspf" %>
<%
int idCustomer=id(p(request,"idCustomer"));
Date periodStart=day(p(request,"periodStart")),periodEnd=day(p(request,"periodEnd"));
Order4[] orders=new Order4[0];
CustomerStatistic4 customerStatistic=null;
String outMessage="";
try {
    if(idCustomer>=0 && periodStart!=null && periodEnd!=null && !periodStart.after(periodEnd)) {
        // [tk-v3-21][tk-v2-17] Trang customerSpendingView4.jsp gọi lớp OrderDAO4 yêu cầu lấy danh sách đơn của khách hàng trong kỳ.
        OrderDAO4 orderDAO=new OrderDAO4();
        // [tk-v3-22][tk-v2-18] Lớp OrderDAO4 gọi hàm getOrders().
        orders=orderDAO.getOrders(idCustomer,periodStart,periodEnd);
    }
    // [tk-v3-26][tk-v2-19] Hàm getOrders() trả kết quả cho trang customerSpendingView4.jsp.
    if(orders.length==0) {
        // [tk-v3-26/E2a] Không hiển thị chi tiết cũ, tự quay về thống kê cùng kỳ.
        outMessage="Data has changed: the selected customer or group no longer matches.";
    } else {
        // [tk-v3-27][tk-v2-19] Trang customerSpendingView4.jsp gọi lớp CustomerStatistic4 yêu cầu đóng gói thông tin khách hàng, kỳ và tổng chi tiêu.
        customerStatistic=new CustomerStatistic4();
        // [tk-v3-28][tk-v2-19] Lớp CustomerStatistic4 gọi các hàm set thuộc tính tương ứng.
        customerStatistic.setId(idCustomer);
        Customer4 customer=orders[0].getCustomer();
        customerStatistic.setCustomerCode(customer==null ? "" : customer.getCustomerCode());
        customerStatistic.setFullName(customer==null ? "No name" : customer.getFullName());
        customerStatistic.setPhoneNumber(customer==null ? null : customer.getPhoneNumber());
        customerStatistic.setProfileStatus(customer==null ? null : customer.getProfileStatus());
        customerStatistic.setMembershipCards(new MembershipCard4[0]); // Không tải vì trang không xem thẻ.
        customerStatistic.setPeriodStart(periodStart);customerStatistic.setPeriodEnd(periodEnd);
        float total=0;for(Order4 order:orders) total+=order.getInvoice().getAmountDue();
        customerStatistic.setTotalSpending(total);
        // [tk-v3-29][tk-v2-19] Lớp CustomerStatistic4 trả đối tượng về cho trang customerSpendingView4.jsp.
        request.setAttribute("customerStatistic",customerStatistic);
    }
} catch(IllegalStateException e) { orders=new Order4[0];outMessage="The report could not be generated."; }
// orders : Order4[] (thuộc tính ẩn, request).
request.setAttribute("orders",orders);
%>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Customer spending details</title>
<% if(customerStatistic==null) { %><meta http-equiv="refresh" content="2;url=customerStatisticsView4.jsp?inStartDate=<%= q(p(request,"periodStart")) %>&amp;inEndDate=<%= q(p(request,"periodEnd")) %>"><% } %>
</head><body><h1>Customer spending details</h1>
<%-- outMessage : Label --%><p id="outMessage" name="outMessage" role="status"><%= h(outMessage) %></p>
<% if(customerStatistic!=null) { %>
<%-- [tk-v3-30][tk-v2-20] Trang customerSpendingView4.jsp hiển thị chi tiết chi tiêu và danh sách đơn cho nhân viên quản lý. --%>
<%-- outCustomerCode, outDisplayName, outPeriod, outTotalSpending : Label --%>
<p>Customer code: <span id="outCustomerCode" name="outCustomerCode"><%= h(customerStatistic.getCustomerCode()) %></span></p>
<p>Display name: <span id="outDisplayName" name="outDisplayName"><%= h(customerStatistic.getFullName()) %></span></p>
<p>Period: <span id="outPeriod" name="outPeriod"><%= date(periodStart) %> – <%= date(periodEnd) %></span></p>
<p>Total spending: <span id="outTotalSpending" name="outTotalSpending"><%= money(customerStatistic.getTotalSpending()) %></span></p>
<%-- outsubOrderList : Table --%>
<table id="outsubOrderList" name="outsubOrderList"><thead><tr><th>No.</th><th>Order code</th><th>Paid at</th><th>Amount (VND)</th></tr></thead><tbody>
<% int row=0;for(Order4 order:orders) { %>
<%-- [tk-v3-31][tk-v2-21] Nhân viên quản lý click chọn một đơn trong danh sách.
     [tk-v3-32][tk-v2-22] Trang customerSpendingView4.jsp gọi trang orderDetailView4.jsp. --%>
<tr><td><%= ++row %></td><td><a href="orderDetailView4.jsp?idOrder=<%= order.getId() %>&amp;idCustomer=<%= idCustomer %>&amp;periodStart=<%= q(p(request,"periodStart")) %>&amp;periodEnd=<%= q(p(request,"periodEnd")) %>"><%= h(order.getOrderCode()) %></a></td><td><%= time(paidAt(order.getInvoice())) %></td><td><%= money(order.getInvoice().getAmountDue()) %></td></tr>
<% } %></tbody></table><% } %>
<%-- subBackToStatisticList : submit, giữ kỳ bằng tên ô ngày đã thiết kế. --%>
<form action="customerStatisticsView4.jsp"><input type="hidden" name="inStartDate" value="<%= h(p(request,"periodStart")) %>"><input type="hidden" name="inEndDate" value="<%= h(p(request,"periodEnd")) %>"><input type="submit" id="subBackToStatisticList" name="subBackToStatisticList" value="Back to statistic list"></form>
</body></html>
