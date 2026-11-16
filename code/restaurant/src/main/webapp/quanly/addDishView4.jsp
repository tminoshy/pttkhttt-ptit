<%-- ===================================================================
 LỚP      : addDishView4.jsp (tầng giao diện)
 THIẾT KẾ : class-design-add-dish.puml – hộp addDishView4.jsp (Hình 5, desing.md §4a)
 PHÂN TÍCH: lớp AddDishView4 trong class-add-dish.puml; giữ nguyên tên các thuộc tính hiện.
 GIAO DIỆN: khung "Add dish" của Hình 3 (ui-design.xlsx, tab Add dish).
 THUỘC TÍNH HIỆN: inDishCode, inDishName, inDescription : text; inSalePrice : number;
            inSaleStatus, inoutDishCategory : Select; inImage : file;
            subSave, subBackToDishList : submit; outMessage : Label.
 THUỘC TÍNH ẨN: dishCategories : DishCategory4[], dish : Dish4 (request);
            managementStaff : ManagementStaff4 (session, WEB-INF/auth.jspf).
 LIÊN KẾT : gdThemMonAn–DishCategoryDAO4; gdThemMonAn–doLuuMonAn; gdThemMonAn–gdQuanLyMonAn.
 KỊCH BẢN : v.3 18–19, 24–26, 37–38, 43–45; v.2 9–10, 12–15, 22–23.
 NGOẠI LỆ : A1 (bước 24, không có loại món ăn); hiển thị thông báo của A2, E1, E2
            do doSaveDish4.jsp gửi lại qua outMessage.
 Dữ liệu doSaveDish4.jsp trả lại chỉ dùng tên trên biểu đồ: dish (ẩn), outMessage
 (nội dung nhãn), inoutDishCategory và inSalePrice (tham số của chính biểu mẫu này).
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%@ include file="/WEB-INF/auth.jspf" %>
<%
// [v3-18, v3-37][v2-9] Trang addDishView4.jsp gọi lớp DishCategoryDAO4 yêu cầu lấy danh sách loại món ăn.
DishCategoryDAO4 categoryDAO = new DishCategoryDAO4();
// [v3-19, v3-38][v2-10] Lớp DishCategoryDAO4 gọi hàm getDishCategories().
DishCategory4[] dishCategories = categoryDAO.getDishCategories();
// dishCategories : DishCategory4[] (thuộc tính ẩn, request).
request.setAttribute("dishCategories", dishCategories);
// dish : Dish4 (thuộc tính ẩn, request). Có sẵn khi doSaveDish4.jsp trả lại (E1, A2, E2, bước 36);
// lần đầu mở trang thì tạo món rỗng.
Dish4 dish = (Dish4) request.getAttribute("dish");
if (dish == null) { dish = new Dish4(); dish.setSaleStatus("Đang kinh doanh"); request.setAttribute("dish", dish); }
// saveDish() gán id cho món vừa tạo, nên dish.id > 0 nghĩa là đã lưu thành công (bước 35–36).
boolean success = dish.getId() > 0;
// outMessage : Label – nội dung do doSaveDish4.jsp gửi (thành công, E1, A2, E2); rỗng khi mở lần đầu.
String outMessage = (String) request.getAttribute("outMessage");
// inoutDishCategory, inSalePrice: giá trị đã nhập, đọc lại từ tham số của chính biểu mẫu khi được trả về.
String selectedCategory = p(request, "inoutDishCategory");
String enteredPrice = p(request, "inSalePrice");
%>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Add new dish</title>
<%-- [v3-45][v2-23] Trang addDishView4.jsp gọi trang dishManagementView4.jsp. --%>
<%-- [v3-44] Tự chuyển sau 2 giây; tương đương nhân viên quản lý click OK. --%>
<% if (success) { %><meta http-equiv="refresh" content="2;url=dishManagementView4.jsp"><% } %>
</head><body><h1>Add new dish</h1>
<%-- outMessage : Label
     [v3-43][v2-22] Trang addDishView4.jsp thông báo thêm món ăn thành công cho nhân viên quản lý.
     Cũng hiển thị thông báo của A1, A2, E1, E2. --%>
<div id="outMessage" name="outMessage" role="status">
<% if (!success && dishCategories.length == 0) { %>
<%-- [A1][v3-24/A1] Cannot save. Create a dish category before adding dishes. --%>
<p>Cannot save. Create a dish category before adding dishes.</p>
<% } else if (outMessage != null) { for (String line : outMessage.split("\n")) { %><p><%= h(line) %></p><% } } %>
<%-- [v3-44] Nhân viên quản lý click OK của thông báo.
     [v3-45][v2-23] Trang addDishView4.jsp gọi trang dishManagementView4.jsp. --%>
<% if (success) { %><a href="dishManagementView4.jsp">OK</a><% } %>
</div>
<%-- [v3-24][v2-12] Trang addDishView4.jsp hiển thị biểu mẫu thêm món ăn cho nhân viên quản lý.
     [v3-26][v2-15] Trang addDishView4.jsp gọi trang doSaveDish4.jsp. --%>
<form action="doSaveDish4.jsp" method="post" enctype="multipart/form-data">
<fieldset <%= success ? "disabled" : "" %>>
<%-- [v3-25][v2-13, v2-14, v2-15] Nhân viên quản lý nhập thông tin món ăn, chọn loại món ăn, trạng thái kinh doanh và click Lưu.
     inDishCode : text --%>
<p><label for="inDishCode">Dish code</label> <input type="text" name="inDishCode" id="inDishCode" maxlength="50" value="<%= h(dish.getProductCode()) %>"></p>
<%-- inDishName : text --%>
<p><label for="inDishName">Dish name</label> <input type="text" name="inDishName" id="inDishName" maxlength="255" value="<%= h(dish.getName()) %>"></p>
<%-- inDescription : text --%>
<p><label for="inDescription">Description</label> <input type="text" name="inDescription" id="inDescription" maxlength="255" value="<%= h(dish.getDescription()) %>"></p>
<%-- inSalePrice : number – giữ nguyên chuỗi đã nhập (kể cả giá sai) khi được trả về. --%>
<p><label for="inSalePrice">Sale price (VND)</label> <input type="number" step="any" name="inSalePrice" id="inSalePrice" value="<%= h(!enteredPrice.isEmpty() ? enteredPrice : dish.getSalePrice() == 0 ? "" : dish.getSalePrice()) %>"></p>
<%-- inSaleStatus : Select --%>
<p><label for="inSaleStatus">Sale status</label> <select name="inSaleStatus" id="inSaleStatus">
<% for(String status : new String[]{"Đang kinh doanh","Ngừng kinh doanh"}) { %><option value="<%= h(status) %>" <%= status.equals(dish.getSaleStatus()) ? "selected" : "" %>><%= h(status) %></option><% } %></select></p>
<%-- inoutDishCategory : Select – "out": danh sách lấy từ dishCategories; "in": id loại đã chọn gửi tới doSaveDish4.jsp. --%>
<p><label for="inoutDishCategory">Dish category</label> <select name="inoutDishCategory" id="inoutDishCategory"><option value="">Choose dish category</option>
<% for(DishCategory4 category : dishCategories) { %><option value="<%= category.getId() %>" <%= String.valueOf(category.getId()).equals(selectedCategory) ? "selected" : "" %>><%= h(category.getCategoryCode()+" – "+category.getName()) %></option><% } %></select></p>
<%-- inImage : file; [NGOÀI THIẾT KẾ] Trình duyệt không cho điền lại file; chọn lại sau lỗi. --%>
<p><label for="inImage">Image</label> <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" name="inImage" id="inImage"></p>
<%-- subSave : submit – vô hiệu hóa khi A1 (không có loại món ăn). --%>
<input type="submit" name="subSave" id="subSave" value="Save" <%= dishCategories.length==0 ? "disabled" : "" %>>
</fieldset></form>
<form action="dishManagementView4.jsp" method="get">
<%-- subBackToDishList : submit – về dishManagementView4.jsp, không lưu dữ liệu đang nhập. --%>
<input type="submit" name="subBackToDishList" id="subBackToDishList" value="Back to dish list"></form>
</body></html>
