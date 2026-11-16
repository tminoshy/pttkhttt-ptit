<%-- ===================================================================
 LỚP      : index.jsp
 [NGOÀI THIẾT KẾ] Điểm vào của ứng dụng; chuyển tới loginView4.jsp.
 KỊCH BẢN : không thuộc v.3/tk-v.3, phục vụ đăng nhập cả hai module.  NGOẠI LỆ: không thuộc A1, A2, E1, E2.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*" %>
<%@ include file="/WEB-INF/common.jspf" %>
<% response.sendRedirect("loginView4.jsp"); %>
