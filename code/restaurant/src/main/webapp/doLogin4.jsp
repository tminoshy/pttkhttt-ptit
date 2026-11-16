<%-- ===================================================================
 LỚP      : doLogin4.jsp (xử lý đăng nhập)
 [NGOÀI THIẾT KẾ] Không có hộp trên class-design-add-dish.puml; gọi StaffDAO4.checkLogin()
            rồi đặt managementStaff : ManagementStaff4 vào session cho các trang gd…jsp.
 KỊCH BẢN : không thuộc v.3/tk-v.3, phục vụ đăng nhập cả hai module.  NGOẠI LỆ: không thuộc A1, A2, E1, E2.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%
if (!"POST".equals(request.getMethod())) { response.sendRedirect("loginView4.jsp"); return; }
ManagementStaff4 managementStaff = new ManagementStaff4();
Account4 account = new Account4(); account.setUsername(p(request,"username")); account.setPassword(request.getParameter("password"));
managementStaff.setAccount(account);
try {
    if (new StaffDAO4().checkLogin(managementStaff)) {
        request.changeSessionId();
        // managementStaff : ManagementStaff4 (thuộc tính ẩn, session).
        session.setAttribute("managementStaff", managementStaff);
        response.sendRedirect("quanly/managerMainView4.jsp"); return;
    }
} catch (IllegalStateException e) { application.log("Login failed", e); }
response.sendRedirect("loginView4.jsp?err=login");
%>
