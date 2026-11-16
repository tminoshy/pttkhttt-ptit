<%-- ===================================================================
 LỚP      : doSaveDish4.jsp (tầng giao diện – trang xử lý, không hiển thị)
 THIẾT KẾ : class-design-add-dish.puml – hộp doSaveDish4.jsp (Hình 5, desing.md §4a)
 PHÂN TÍCH: không có lớp riêng; bổ sung theo đích của AddDishView4.subSave (UI-02).
            Liên kết AddDishView4–Dish4 của phân tích chuyển thành doLuuMonAn–DishDAO4.
 THUỘC TÍNH ẨN: dish : Dish4 (request) – món đóng gói từ biểu mẫu addDishView4.jsp.
 LIÊN KẾT : doLuuMonAn–DishDAO4; doLuuMonAn–gdThemMonAn (nhận từ subSave, trả về sau xử lý).
 KỊCH BẢN : v.3 27–36; v.2 16–21.
 NGOẠI LỆ : E1, A2 (bước 32, validateDish trả lỗi); E2 (bước 35, saveDish trả null
            hoặc lưu ảnh lỗi). Mọi nhánh đều trả về addDishView4.jsp kèm dish và outMessage.
 [NGOÀI THIẾT KẾ] Chỉ nhận POST; lưu ảnh vào images/dishes và dọn ảnh khi lưu thất bại.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%@ page import="java.nio.file.*,java.io.*,jakarta.servlet.http.Part" %>
<%@ include file="/WEB-INF/auth.jspf" %>
<%
// [NGOÀI THIẾT KẾ] Mở trực tiếp bằng GET thì quay về biểu mẫu.
if (!"POST".equals(request.getMethod())) { response.sendRedirect("addDishView4.jsp"); return; }
// [v3-27][v2-16] Trang doSaveDish4.jsp gọi lớp Dish4 để đóng gói thông tin món ăn đã nhập.
Dish4 dish = new Dish4();
int idDishCategory = 0;
Path uploaded = null;
try {
    // [v3-28][v2-16] Lớp Dish4 gọi các hàm set thuộc tính tương ứng.
    dish.setProductCode(p(request,"inDishCode")); dish.setName(p(request,"inDishName"));
    dish.setDescription(p(request,"inDescription")); dish.setSaleStatus(p(request,"inSaleStatus"));
    // Giá không phải số được coi là 0 để validateDish() báo lỗi inSalePrice (E1).
    try { dish.setSalePrice(Float.parseFloat(p(request,"inSalePrice"))); } catch (NumberFormatException e) { dish.setSalePrice(0); }
    // inoutDishCategory gửi id loại đã chọn; truyền thành tham số idDishCategory của validateDish/saveDish.
    try { idDishCategory = Integer.parseInt(p(request,"inoutDishCategory")); } catch (NumberFormatException ignored) { }
    // dish : Dish4 (thuộc tính ẩn, request) – addDishView4.jsp dùng để điền lại biểu mẫu.
    request.setAttribute("dish", dish);
    // [v3-29][v2-16] Lớp Dish4 trả đối tượng về cho trang doSaveDish4.jsp.
    // [v3-30][v2-16] Trang doSaveDish4.jsp gọi lớp DishDAO4 yêu cầu kiểm tra thông tin món ăn.
    DishDAO4 dishDAO = new DishDAO4();
    // [v3-31][v2-17] Lớp DishDAO4 gọi hàm validateDish().
    String[] errors = dishDAO.validateDish(dish, idDishCategory);
    // [v3-32][v2-18] Hàm validateDish() trả kết quả hợp lệ cho trang doSaveDish4.jsp.
    if (errors.length != 0) {
        // [E1][A2][v3-32/E1][v3-32/A2] Đổi từng mã lỗi thành câu trong bảng Thông báo (ui-design.xlsx),
        // gửi qua outMessage; giữ dish để điền lại; không ghi CSDL hoặc ảnh.
        StringBuilder outMessage = new StringBuilder();
        for (String error : errors) {
            if (outMessage.length() > 0) outMessage.append('\n');
            switch (error) {
                case "khongConLoaiMon": outMessage.append("Cannot save. Create a dish category before adding dishes."); break;
                case "inDishCode": outMessage.append("Dish code is duplicated or missing."); break;
                case "inDishName": outMessage.append("Dish name is required."); break;
                case "inSalePrice": outMessage.append("Sale price must be greater than 0."); break;
                case "inSaleStatus": outMessage.append("Sale status is missing or invalid."); break;
                default: outMessage.append("Dish category does not exist."); break; // inoutDishCategory
            }
        }
        request.setAttribute("outMessage", outMessage.toString());
        request.getRequestDispatcher("addDishView4.jsp").forward(request,response); return;
    }
    // [NGOÀI THIẾT KẾ] Chỉ lưu ảnh sau kiểm tra; tên mã món được mã hóa để không thoát thư mục.
    Part part = request.getPart("inImage");
    if (part != null && part.getSize() > 0) {
        String filename = part.getSubmittedFileName();
        String ext = filename != null && filename.contains(".") ? filename.substring(filename.lastIndexOf('.')+1).toLowerCase(java.util.Locale.ROOT) : "";
        if (!java.util.Set.of("png","jpg","jpeg","gif","webp").contains(ext)) throw new IOException("Unsupported image format.");
        String realPath = application.getRealPath("/images/dishes");
        if (realPath == null) throw new IOException("Deploy the WAR as an exploded directory to store images.");
        Path directory = Path.of(realPath); Files.createDirectories(directory);
        String safeCode = dish.getProductCode().matches("[A-Za-z0-9_-]{1,50}") ? dish.getProductCode() : java.util.HexFormat.of().formatHex(dish.getProductCode().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Path target = directory.resolve(safeCode + "." + ext);
        // CREATE_NEW tránh ghi đè ảnh khi hai yêu cầu gửi cùng mã món.
        try (OutputStream output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
            uploaded = target;
            try (InputStream input = part.getInputStream()) { input.transferTo(output); }
        }
        dish.setImage("images/dishes/" + target.getFileName());
    }
    // [v3-33][v2-19] Trang doSaveDish4.jsp gọi lớp DishDAO4 yêu cầu lưu món ăn.
    // [v3-34][v2-20] Lớp DishDAO4 gọi hàm saveDish().
    Dish4 saved = dishDAO.saveDish(dish, idDishCategory);
    // [v3-35][v2-21] Hàm saveDish() trả kết quả món ăn vừa tạo cho trang doSaveDish4.jsp.
    if (saved == null) throw new IOException("Saving dish failed.");
    // saveDish() đã gán id cho dish; addDishView4.jsp nhận biết thành công qua dish.id > 0.
    request.setAttribute("outMessage", "Dish added successfully.");
} catch (Exception e) {
    // [E2][v3-35/E2] The dish could not be created.
    application.log("Saving dish failed", e);
    if (uploaded != null) { try { Files.deleteIfExists(uploaded); } catch (IOException cleanup) { application.log("Could not clean up image", cleanup); } }
    dish.setId(0); dish.setImage(null);
    request.setAttribute("dish", dish);
    request.setAttribute("outMessage", "The dish could not be created.");
}
// [v3-36][v2-21] Trang doSaveDish4.jsp gọi trang addDishView4.jsp.
request.getRequestDispatcher("addDishView4.jsp").forward(request,response);
%>
