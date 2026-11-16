<%-- ===================================================================
 LỚP      : statisticsReportView4.jsp (tầng giao diện)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp statisticsReportView4.jsp (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : Không truy vấn bảng.
 KỊCH BẢN : module 2, tk-v.3 3–5; v.2 3–5.
 NGOẠI LỆ : không có.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*,java.util.Date" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%@ include file="/WEB-INF/auth.jspf" %>
<%@ include file="/WEB-INF/statistics.jspf" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Statistics reports</title></head><body>
<%-- [tk-v3-3][tk-v2-3] Trang statisticsReportView4.jsp hiển thị các loại thống kê cho nhân viên quản lý. --%>
<h1>Statistics reports</h1>
<%-- outsubStatisticTypes : Table – bốn loại báo cáo đã thiết kế, chỉ thống kê khách hàng có liên kết. --%>
<table id="outsubStatisticTypes" name="outsubStatisticTypes"><thead><tr><th>Statistic type</th></tr></thead><tbody>
<tr><td>Dish statistics</td></tr><tr><td>Ingredient statistics</td></tr>
<%-- [tk-v3-4][tk-v2-4] Nhân viên quản lý click chọn Customer statistics by revenue.
     [tk-v3-5][tk-v2-5] Trang statisticsReportView4.jsp gọi trang customerStatisticsView4.jsp. --%>
<tr><td><a href="customerStatisticsView4.jsp">Customer statistics by revenue</a></td></tr>
<tr><td>Supplier statistics</td></tr></tbody></table>
<%-- subBackToMain : submit --%>
<form action="managerMainView4.jsp"><input id="subBackToMain" name="subBackToMain" type="submit" value="Back to main page"></form>
</body></html>
