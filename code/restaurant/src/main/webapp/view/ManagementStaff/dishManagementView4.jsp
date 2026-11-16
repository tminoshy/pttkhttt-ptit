<%-- ===================================================================
 LỚP      : dishManagementView4.jsp (tầng giao diện)
 THIẾT KẾ : class-design-add-dish.puml – hộp dishManagementView4.jsp (Hình 5, desing.md §4a)
 PHÂN TÍCH: lớp DishManagementView4 trong class-add-dish.puml; giữ nguyên tên các thuộc tính hiện.
 GIAO DIỆN: khung "Manage dish information" của Hình 3.
 THUỘC TÍNH HIỆN: outDishList : Table; subAddDish, subBackToMain : submit.
 THUỘC TÍNH ẨN: dishes : Dish4[], dishCategories : DishCategory4[] (request);
            managementStaff : ManagementStaff4 (session, WEB-INF/auth.jspf).
 LIÊN KẾT : gdQuanLyMonAn–DishDAO4; gdQuanLyMonAn–DishCategoryDAO4;
            gdQuanLyMonAn–gdChinhQuanLy; gdQuanLyMonAn–gdThemMonAn.
 KỊCH BẢN : v.3 3, 4, 9, 10, 15–17, 46, 47, 52, 53, 58; v.2 3, 4, 6–8, 24, 25, 27.
 NGOẠI LỆ : không có nhánh A1, A2, E1, E2 tại trang này.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%@ include file="/WEB-INF/auth.jspf" %>
<%
// [v3-3, v3-46][v2-3, v2-24] Trang dishManagementView4.jsp gọi lớp DishDAO4 yêu cầu lấy danh sách món ăn.
DishDAO4 dishDAO = new DishDAO4();
// [v3-4, v3-47][v2-4, v2-25] Lớp DishDAO4 gọi hàm getDishes().
Dish4[] dishes = dishDAO.getDishes();
// dishes : Dish4[] (thuộc tính ẩn, request).
request.setAttribute("dishes", dishes);
// [v3-9, v3-52] Trang dishManagementView4.jsp gọi lớp DishCategoryDAO4 yêu cầu lấy danh sách loại món ăn.
DishCategoryDAO4 categoryDAO = new DishCategoryDAO4();
// [v3-10, v3-53] Lớp DishCategoryDAO4 gọi hàm getDishCategories().
DishCategory4[] dishCategories = categoryDAO.getDishCategories();
// dishCategories : DishCategory4[] (thuộc tính ẩn, request).
request.setAttribute("dishCategories", dishCategories);
%>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Manage dish information</title></head><body>
<h1>Manage dish information</h1>
<%-- [v3-15][v2-6] Trang dishManagementView4.jsp hiển thị danh sách món ăn cho nhân viên quản lý.
     [v3-58][v2-27] Trang dishManagementView4.jsp hiển thị danh sách có món vừa thêm cho nhân viên quản lý. Use case kết thúc.
     outDishList : Table --%>
<table id="outDishList" name="outDishList" border="1" cellpadding="8">
<thead><tr><th>No.</th><th>Dish code</th><th>Dish name</th><th>Dish category</th><th>Sale price (VND)</th><th>Sale status</th></tr></thead><tbody>
<% int tt=0; for (Dish4 dish : dishes) {
    String categoryName="";
    for (DishCategory4 category : dishCategories) for (Dish4 member : category.getDishes())
        if (member.getId()==dish.getId()) categoryName=category.getName();
%><tr><td><%= ++tt %></td><td><%= h(dish.getProductCode()) %></td><td><%= h(dish.getName()) %></td><td><%= h(categoryName) %></td><td><%= java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("vi-VN")).format(dish.getSalePrice()) %></td><td><%= h(dish.getSaleStatus()) %></td></tr><% } %>
</tbody></table>
<%-- [v3-17][v2-8] Trang dishManagementView4.jsp gọi trang addDishView4.jsp. --%>
<form action="addDishView4.jsp" method="get">
<%-- [v3-16][v2-7] Nhân viên quản lý click Add dish. subAddDish : submit --%>
<input type="submit" name="subAddDish" id="subAddDish" value="Add dish"></form>
<form action="managerMainView4.jsp" method="get">
<%-- subBackToMain : submit --%>
<input type="submit" name="subBackToMain" id="subBackToMain" value="Back to main page"></form>
</body></html>
