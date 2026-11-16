<%-- ===================================================================
 LỚP      : loginView4.jsp (đăng nhập)
 [NGOÀI THIẾT KẾ] Không có hộp trên class-design-add-dish.puml; chỉ cung cấp tiền điều kiện
            "nhân viên quản lý đã đăng nhập" của cả hai module Add new dish và View statistics khách hàng.
 THÀNH PHẦN: username, password (ô nhập), gửi tới doLogin4.jsp.
 KỊCH BẢN : không thuộc v.3/tk-v.3, phục vụ đăng nhập cả hai module.  NGOẠI LỆ: không thuộc A1, A2, E1, E2.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*" %>
<%@ include file="/WEB-INF/common.jspf" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Management login</title></head><body>
<h1>Management login</h1>
<% if (request.getParameter("err") != null) { %><p role="alert"><%= "timeout".equals(request.getParameter("err")) ? "Session expired. Please log in again." : "Cannot log in. Check the account or the database connection." %></p><% } %>
<form method="post" action="doLogin4.jsp">
<label>Username <input name="username" required autocomplete="username"></label><br>
<label>Password <input name="password" type="password" required autocomplete="current-password"></label><br>
<button type="submit">Log in</button></form></body></html>
