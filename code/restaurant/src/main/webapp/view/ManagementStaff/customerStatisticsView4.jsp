<%-- ===================================================================
 LỚP      : customerStatisticsView4.jsp (tầng giao diện)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp customerStatisticsView4.jsp (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblCustomer4, tblOrder4, tblInvoice4, tblPayment4 qua CustomerStatisticDAO4.
 KỊCH BẢN : module 2, tk-v.3 6–13, 18–20; v.2 6–16.
 NGOẠI LỆ : E1, A1, E3, A2.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*,java.util.Date" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%@ include file="/WEB-INF/auth.jspf" %>
<%@ include file="/WEB-INF/statistics.jspf" %>
<%
String inStartDate=p(request,"inStartDate"), inEndDate=p(request,"inEndDate");
Date periodStart=day(inStartDate), periodEnd=day(inEndDate);
CustomerStatistic4[] customerStatistics=new CustomerStatistic4[0];
String outMessage="";
if(request.getParameter("inStartDate")!=null || request.getParameter("inEndDate")!=null) {
    try {
        // [tk-v3-9][tk-v2-8] Trang customerStatisticsView4.jsp gọi lớp CustomerStatisticDAO4 yêu cầu kiểm tra khoảng ngày đã chọn.
        CustomerStatisticDAO4 customerStatisticDAO=new CustomerStatisticDAO4();
        // [tk-v3-10][tk-v2-9] Lớp CustomerStatisticDAO4 gọi hàm validatePeriod().
        boolean valid=customerStatisticDAO.validatePeriod(periodStart,periodEnd);
        // [tk-v3-11][tk-v2-10] Hàm validatePeriod() trả kết quả hợp lệ cho trang customerStatisticsView4.jsp.
        if(!valid) {
            // [tk-v3-11/E1] Giữ ngày đã nhập, không gọi getCustomerStatistics().
            outMessage="Start date must not be after end date. Please choose the period again.";
        } else {
            // [tk-v3-12][tk-v2-11] Trang customerStatisticsView4.jsp gọi lớp CustomerStatisticDAO4 yêu cầu lấy thống kê khách hàng theo kỳ đã chọn.
    // [tk-v3-13][tk-v2-12] Lớp CustomerStatisticDAO4 gọi hàm getCustomerStatistics().
            customerStatistics=customerStatisticDAO.getCustomerStatistics(periodStart,periodEnd);
            // [tk-v3-17/E3] Không hiển thị dữ liệu dở dang khi tổng hợp thất bại.
            if(customerStatistics==null) { customerStatistics=new CustomerStatistic4[0]; outMessage="The report could not be generated."; }
            // [tk-v3-17/A1] Giữ kỳ và thông báo danh sách rỗng.
            else if(customerStatistics.length==0) outMessage="No paid invoices in the selected period.";
        }
    } catch(IllegalStateException e) {
        // [tk-v3-17/E3] Bao gồm trường hợp không mở được kết nối CSDL.
        outMessage="The report could not be generated.";
    }
}
// customerStatistics : CustomerStatistic4[] (thuộc tính ẩn, request).
request.setAttribute("customerStatistics",customerStatistics);
%>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Customer statistics</title></head><body>
<h1>Customer statistics by revenue</h1>
<%-- [tk-v3-6][tk-v2-6] Trang customerStatisticsView4.jsp hiển thị ô ngày bắt đầu, ô ngày kết thúc và nút View statistics cho nhân viên quản lý.
     [tk-v3-8][tk-v2-7] Trang customerStatisticsView4.jsp submit gọi chính nó xử lí. --%>
<form action="customerStatisticsView4.jsp" method="get">
<%-- inStartDate, inEndDate : date --%>
<label for="inStartDate">Start date</label><input type="date" id="inStartDate" name="inStartDate" value="<%= h(inStartDate) %>">
<label for="inEndDate">End date</label><input type="date" id="inEndDate" name="inEndDate" value="<%= h(inEndDate) %>">
<%-- [tk-v3-7][tk-v2-7] Nhân viên quản lý chọn ngày bắt đầu, ngày kết thúc và click View statistics. --%>
<%-- subViewStatistics : submit --%>
<input type="submit" id="subViewStatistics" name="subViewStatistics" value="View statistics"></form>
<%-- outMessage : Label --%>
<p id="outMessage" name="outMessage" role="status"><%= h(outMessage) %></p>
<%-- [tk-v3-18][tk-v2-14] Trang customerStatisticsView4.jsp hiển thị danh sách thống kê cho nhân viên quản lý. --%>
<%-- outsubStatisticList : Table --%>
<table id="outsubStatisticList" name="outsubStatisticList"><thead><tr><th>No.</th><th>Customer code</th><th>Display name</th><th>Total spending (VND)</th></tr></thead><tbody>
<% int row=0; for(CustomerStatistic4 statistic:customerStatistics) { %>
<%-- [tk-v3-19][tk-v2-15] Nhân viên quản lý click chọn một khách hàng trong danh sách.
     [tk-v3-20][tk-v2-16] Trang customerStatisticsView4.jsp gọi trang customerSpendingView4.jsp. --%>
<%-- [tk-v3-19/A2] Nhóm No name có idCustomer=0, không tạo mã khách giả. --%>
<tr><td><%= ++row %></td><td><%= h(statistic.getCustomerCode()) %></td><td><a href="customerSpendingView4.jsp?idCustomer=<%= statistic.getId() %>&amp;periodStart=<%= q(inStartDate) %>&amp;periodEnd=<%= q(inEndDate) %>"><%= h(statistic.getFullName()) %></a></td><td><%= money(statistic.getTotalSpending()) %></td></tr>
<% } %></tbody></table>
<%-- subBackToStatisticsReport : submit --%>
<form action="statisticsReportView4.jsp"><input type="submit" id="subBackToStatisticsReport" name="subBackToStatisticsReport" value="Back to statistics reports"></form>
</body></html>
