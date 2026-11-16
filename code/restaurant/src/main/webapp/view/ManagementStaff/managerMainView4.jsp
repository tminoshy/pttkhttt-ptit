<%-- ===================================================================
 LỚP      : managerMainView4.jsp (tầng giao diện)
 THIẾT KẾ : class-design-add-dish.puml – hộp managerMainView4.jsp (Hình 5, desing.md §4a)
 MODULE 2 : hộp managerMainView4.jsp trên class-design-customer-statistics.puml (Hình 6, desing.md §4b).
 PHÂN TÍCH: lớp ManagerMainView4 trong class-add-dish.puml; giữ nguyên tên các thuộc tính hiện.
 GIAO DIỆN: khung "Giao diện chính – Nhân viên quản lý" của Hình 3.
 THUỘC TÍNH HIỆN: subManageDish, subStatisticsReport : link.
 THUỘC TÍNH ẨN: managementStaff : ManagementStaff4 (session, WEB-INF/auth.jspf).
 LIÊN KẾT : gdChinhQuanLy–gdQuanLyMonAn; gdChinhQuanLy–gdBaoCaoThongKe.
 KỊCH BẢN : module 1 v.3 1–2, v.2 1–2; module 2 tk-v.3 1–2, tk-v.2 1–2.
 NGOẠI LỆ : không có nhánh A1, A2, E1, E2 tại trang này.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%@ include file="/WEB-INF/auth.jspf" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Management staff main page</title></head><body>
<h1>Management staff main page</h1><p>Hello, <%= h(managementStaff.getFullName()) %></p>
<%-- [v3-1][v2-1] Tại trang managerMainView4.jsp, sau khi đăng nhập, nhân viên quản lý click chọn Manage dish information.
     [v3-2][v2-2] Trang managerMainView4.jsp gọi trang dishManagementView4.jsp.
     subManageDish : link --%>
<p><a id="subManageDish" name="subManageDish" href="dishManagementView4.jsp">Manage dish information</a></p>
<%-- [tk-v3-1][tk-v2-1] Tại trang managerMainView4.jsp, sau khi đăng nhập, nhân viên quản lý click chọn View statistics reports.
     [tk-v3-2][tk-v2-2] Trang managerMainView4.jsp gọi trang statisticsReportView4.jsp. --%>
<%-- subStatisticsReport : link --%>
<a id="subStatisticsReport" name="subStatisticsReport" href="statisticsReportView4.jsp">View statistics reports</a>
</body></html>
