# Cài đặt hệ thống quản lý nhà hàng

Tài liệu này trình bày mã Java/JSP cho các lớp của biểu đồ lớp thiết kế (câu 4b): cấu trúc chương trình, mẫu lớp, thuộc tính và phương thức, cách thực hiện từng phương thức, kiểm thử và bảng truy vết từ thiết kế tới mã. Các hình được dẫn chiếu (Hình 5, Hình 6 cho biểu đồ lớp thiết kế; Hình 7–10 cho biểu đồ hoạt động và tuần tự) nằm trong [desing.md](desing.md). Mã nguồn đầy đủ ở [code/restaurant](../code/restaurant).


**Quy ước cài đặt chung cho hai module.**

- Lớp `DAO4` giữ `{static} #con : Connection` như biểu đồ thiết kế. Kết nối là trường static, được mở khi chưa có hoặc đã đóng; các thao tác JDBC được đồng bộ theo `DAO4.class` để không xen giao dịch giữa các yêu cầu.
- Giá trị trạng thái lưu trong CSDL: `vaitro` nhận tên lớp `ManagementStaff4`, `WarehouseStaff4` hoặc `SalesStaff4`; tài khoản hoạt động dùng `accountStatus = "Đang hoạt động"`; trạng thái món dùng `Đang kinh doanh` hoặc `Ngừng kinh doanh`.
- Mỗi bước kịch bản v.3 (desing.md §5b, §6b) được đánh dấu trong mã ngay trên dòng thực hiện: module Thêm món ăn mới dùng tag `[v3-NN]`, module Xem thống kê khách hàng dùng `[tk-v3-NN]`. Số NN trùng số thông điệp trên Hình 8 và Hình 10.

## 1 Cài đặt module Thêm món ăn mới

Module được cài đặt bằng Java/JSP theo cấu trúc ba tầng của Hình 5. Maven đóng gói WAR, biên dịch lớp Java đích 17; Jakarta Servlet 6.0/JSP 3.1 chạy trên Tomcat 10.1, kết nối MySQL 8 bằng JDBC. Mảng là kiểu kết quả đúng thiết kế; ArrayList chỉ gom dòng trong DAO4 trước khi chuyển thành mảng. Module thống kê chưa được cài đặt.

### a Cấu trúc chương trình

```text
code/restaurant/
  pom.xml
  db/schema.sql, seed.sql
  src/main/java/model/{Product4,Dish4,DishCategory4,Staff4,ManagementStaff4,Account4}.java
  src/main/java/dao/{DAO4,DishDAO4,DishCategoryDAO4,StaffDAO4}.java
  src/main/resources/db.properties
  src/main/webapp/
    index.jsp, loginView4.jsp, doLogin4.jsp
    quanly/{gdChinhQuanLy,gdQuanLyMonAn,gdThemMonAn,doLuuMonAn}.jsp
    WEB-INF/{common,auth}.jspf
    META-INF/context.xml
    images/dishes/
  src/test/java/dao/{DatabaseTest4,DishDAOTest4,DishCategoryDAOTest4,StaffDAOTest4}.java
  tools/trace.py
```

Phần module 1 trong `schema.sql` tạo bốn bảng, dùng InnoDB, unique của mã sản phẩm và khóa ngoại của bảng con. `seed.sql` giữ ba loại món, ba món minh họa ban đầu và tài khoản quản lý; dữ liệu bổ sung của module thống kê nằm ở cuối tệp. Account4 gộp vào tblStaff4 nên Account4.id dùng cùng id với Staff4. Thông tin đăng nhập minh họa chỉ được ghi trong seed.sql.

`db.properties` chứa cấu hình phát triển; có thể truyền `-Ddb.config=/đường/dẫn/db.properties` cho JVM Tomcat để dùng cấu hình khác. Ảnh đặt trong thư mục triển khai `images/dishes`, CSDL chỉ giữ đường dẫn tương đối. Cần triển khai WAR dạng thư mục và cấp quyền ghi thư mục này; ảnh sẽ mất khi triển khai lại. Mã món dùng làm tên ảnh, các ký tự không an toàn được mã hóa, chỉ nhận phần mở rộng ảnh, không ghi đè file có sẵn và dọn file mới khi giao dịch thất bại. Trình duyệt yêu cầu chọn lại file khi sửa lỗi biểu mẫu.

`index.jsp`, hai trang đăng nhập và StaffDAO4 chỉ thực hiện tiền điều kiện nhân viên quản lý đã đăng nhập. WEB-INF/auth.jspf kiểm tra session, WEB-INF/common.jspf đọc UTF-8 và mã hóa dữ liệu trước khi đưa vào HTML. Đây là phần hạ tầng ngoài biểu đồ module, không thêm chức năng nghiệp vụ. Tương tự, cấu hình, dữ liệu SQL, lưu file, kiểm thử và công cụ truy vết nằm ngoài biểu đồ.

### b Mẫu lớp thực thể, thuộc tính và phương thức

Các mẫu dưới đây khai báo đầy đủ trường private đúng thứ tự Hình 5. Hàm tạo rỗng tạo đối tượng để DAO4 hoặc JSP điền; getter trả giá trị của đúng trường, setter gán tham số vào đúng trường. Các hàm này không truy vấn CSDL, không kiểm tra hoặc lưu nghiệp vụ. Product4 và Staff4 là lớp trừu tượng; Dish4 và ManagementStaff4 kế thừa toàn bộ thuộc tính từ lớp cha.

**Lớp Product4** — [mã nguồn](../code/restaurant/src/main/java/model/Product4.java).

```java
/* ===================================================================
 * LỚP      : Product4 (tầng thực thể, abstract)
 * THIẾT KẾ : class-design-add-dish.puml – hộp Product4 (Hình 5, desing.md §4a)
 * MODULE 2 : Product4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: lớp Product4 trong class-add-dish.puml; giữ tên và thuộc tính nghiệp vụ.
 *            id và kiểu Java bổ sung theo class-entity-design.puml (desing.md §1).
 * QUAN HỆ  : Product4 <|-- Dish4; Product4 <|-- Combo4.
 * CSDL     : tblProduct4 (id, productCode, name, description, salePrice, image,
 *            saleStatus, createdAt, updatedAt).
 * KỊCH BẢN : không tự tham gia; các thuộc tính được Dish4 kế thừa và đóng gói ở v.3 6, 28, 49.
 * KỊCH BẢN MODULE 2: tk-v.3 45; đóng gói mặt hàng của OrderSubmissionProduct4.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

import java.util.Date;

public abstract class Product4 {
    private int id;
    private String productCode;
    private String name;
    private String description;
    private float salePrice;
    private String image;
    private String saleStatus;
    private Date createdAt;
    private Date updatedAt;

    /** Product4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public Product4() {}

    /** getId() : int; trả thuộc tính id, không truy vấn CSDL. */
    public int getId() { return id; }

    /** setId(id : int); đóng gói thuộc tính id, không truy vấn CSDL. */
    public void setId(int id) { this.id = id; }

    /** getProductCode() : String; trả thuộc tính productCode, không truy vấn CSDL. */
    public String getProductCode() { return productCode; }

    /** setProductCode(productCode : String); đóng gói thuộc tính productCode, không truy vấn CSDL. */
    public void setProductCode(String productCode) { this.productCode = productCode; }

    /** getName() : String; trả thuộc tính name, không truy vấn CSDL. */
    public String getName() { return name; }

    /** setName(name : String); đóng gói thuộc tính name, không truy vấn CSDL. */
    public void setName(String name) { this.name = name; }

    /** getDescription() : String; trả thuộc tính description, không truy vấn CSDL. */
    public String getDescription() { return description; }

    /** setDescription(description : String); đóng gói thuộc tính description, không truy vấn CSDL. */
    public void setDescription(String description) { this.description = description; }

    /** getSalePrice() : float; trả thuộc tính salePrice, không truy vấn CSDL. */
    public float getSalePrice() { return salePrice; }

    /** setSalePrice(salePrice : float); đóng gói thuộc tính salePrice, không truy vấn CSDL. */
    public void setSalePrice(float salePrice) { this.salePrice = salePrice; }

    /** getImage() : String; trả thuộc tính image, không truy vấn CSDL. */
    public String getImage() { return image; }

    /** setImage(image : String); đóng gói thuộc tính image, không truy vấn CSDL. */
    public void setImage(String image) { this.image = image; }

    /** getSaleStatus() : String; trả thuộc tính saleStatus, không truy vấn CSDL. */
    public String getSaleStatus() { return saleStatus; }

    /** setSaleStatus(saleStatus : String); đóng gói thuộc tính saleStatus, không truy vấn CSDL. */
    public void setSaleStatus(String saleStatus) { this.saleStatus = saleStatus; }

    /** getCreatedAt() : Date; trả thuộc tính createdAt, không truy vấn CSDL. */
    public Date getCreatedAt() { return createdAt; }

    /** setCreatedAt(createdAt : Date); đóng gói thuộc tính createdAt, không truy vấn CSDL. */
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    /** getUpdatedAt() : Date; trả thuộc tính updatedAt, không truy vấn CSDL. */
    public Date getUpdatedAt() { return updatedAt; }

    /** setUpdatedAt(updatedAt : Date); đóng gói thuộc tính updatedAt, không truy vấn CSDL. */
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

`Product4()` khởi tạo đối tượng rỗng; các getter/setter thao tác trực tiếp những trường khai báo phía trên.

**Lớp Dish4** — [mã nguồn](../code/restaurant/src/main/java/model/Dish4.java).

```java
/* ===================================================================
 * LỚP      : Dish4 (tầng thực thể)
 * THIẾT KẾ : class-design-add-dish.puml – hộp Dish4 (Hình 5, desing.md §4a)
 * MODULE 2 : Dish4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: lớp Dish4 trong class-add-dish.puml. Ba phương thức getDishes(), validateDish(),
 *            saveDish() của Dish4 ở pha phân tích đã chuyển sang DishDAO4, giữ nguyên tên.
 * QUAN HỆ  : Product4 <|-- Dish4; DishCategory4 o-- Dish4; DishDAO4–Dish4.
 * CSDL     : tblDish4 (tblProduct4id, tblDishCategory4id) + các cột kế thừa ở tblProduct4.
 * KỊCH BẢN : v.3 5–7, 27–29, 48–50 (được tạo và đóng gói thông tin thực thể);
 *            v.2 4, 16, 25.
 * KỊCH BẢN MODULE 2: tk-v.3 45; đóng gói mặt hàng của OrderSubmissionProduct4.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public class Dish4 extends Product4 {

    /** Dish4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public Dish4() {}
}
```

`Dish4()` khởi tạo đối tượng rỗng; các phương thức truy cập dữ liệu được kế thừa từ lớp cha.

**Lớp DishCategory4** — [mã nguồn](../code/restaurant/src/main/java/model/DishCategory4.java).

```java
/* ===================================================================
 * LỚP      : DishCategory4 (tầng thực thể)
 * THIẾT KẾ : class-design-add-dish.puml – hộp DishCategory4 (Hình 5, desing.md §4a)
 * PHÂN TÍCH: lớp DishCategory4 trong class-add-dish.puml. Phương thức getDishCategories()
 *            đã chuyển sang DishCategoryDAO4, giữ nguyên tên.
 * QUAN HỆ  : DishCategory4 o-- Dish4 (thuộc tính dishes : Dish4[]); DishCategoryDAO4–DishCategory4.
 * CSDL     : tblDishCategory4; dishes lấy từ tblDish4.tblDishCategory4id.
 * KỊCH BẢN : v.3 11–13, 20–22, 54–56 (được tạo và đóng gói thông tin thực thể); v.2 10.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public class DishCategory4 {
    private int id;
    private String categoryCode;
    private String name;
    private String description;
    private Dish4[] dishes;

    /** DishCategory4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public DishCategory4() {}

    /** getId() : int; trả thuộc tính id, không truy vấn CSDL. */
    public int getId() { return id; }

    /** setId(id : int); đóng gói thuộc tính id, không truy vấn CSDL. */
    public void setId(int id) { this.id = id; }

    /** getCategoryCode() : String; trả thuộc tính categoryCode, không truy vấn CSDL. */
    public String getCategoryCode() { return categoryCode; }

    /** setCategoryCode(categoryCode : String); đóng gói thuộc tính categoryCode, không truy vấn CSDL. */
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    /** getName() : String; trả thuộc tính name, không truy vấn CSDL. */
    public String getName() { return name; }

    /** setName(name : String); đóng gói thuộc tính name, không truy vấn CSDL. */
    public void setName(String name) { this.name = name; }

    /** getDescription() : String; trả thuộc tính description, không truy vấn CSDL. */
    public String getDescription() { return description; }

    /** setDescription(description : String); đóng gói thuộc tính description, không truy vấn CSDL. */
    public void setDescription(String description) { this.description = description; }

    /** getDishes() : Dish4[]; trả thuộc tính dishes, không truy vấn CSDL. */
    public Dish4[] getDishes() { return dishes; }

    /** setDishes(dishes : Dish4[]); đóng gói thuộc tính dishes, không truy vấn CSDL. */
    public void setDishes(Dish4[] dishes) { this.dishes = dishes; }
}
```

`DishCategory4()` khởi tạo đối tượng rỗng; các getter/setter thao tác trực tiếp những trường khai báo phía trên.

**Lớp Staff4** — [mã nguồn](../code/restaurant/src/main/java/model/Staff4.java).

```java
/* ===================================================================
 * LỚP      : Staff4 (tầng thực thể, abstract)
 * THIẾT KẾ : class-design-add-dish.puml – hộp Staff4 (Hình 5, desing.md §4a)
 * MODULE 2 : Staff4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: không có trên class-add-dish.puml; lấy từ class-entity-design.puml. Có mặt
 *            vì là lớp cha của ManagementStaff4 – kiểu của thuộc tính ẩn managementStaff.
 * QUAN HỆ  : Staff4 <|-- ManagementStaff4; Staff4 <|-- SalesStaff4; Staff4 *-- Account4.
 * CSDL     : tblStaff4 (Account4 gộp 1–1 vào tblStaff4; cột vaitro phân loại lớp con).
 * KỊCH BẢN : không thuộc v.3; chỉ phục vụ tiền điều kiện đã đăng nhập.
 * KỊCH BẢN MODULE 2: không có bước riêng; phục vụ tiền điều kiện đăng nhập, managementStaff trong session.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public abstract class Staff4 {
    private int id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String workingStatus;
    private Account4 account;

    /** Staff4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public Staff4() {}

    /** getId() : int; trả thuộc tính id, không truy vấn CSDL. */
    public int getId() { return id; }

    /** setId(id : int); đóng gói thuộc tính id, không truy vấn CSDL. */
    public void setId(int id) { this.id = id; }

    /** getFullName() : String; trả thuộc tính fullName, không truy vấn CSDL. */
    public String getFullName() { return fullName; }

    /** setFullName(fullName : String); đóng gói thuộc tính fullName, không truy vấn CSDL. */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /** getPhoneNumber() : String; trả thuộc tính phoneNumber, không truy vấn CSDL. */
    public String getPhoneNumber() { return phoneNumber; }

    /** setPhoneNumber(phoneNumber : String); đóng gói thuộc tính phoneNumber, không truy vấn CSDL. */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /** getEmail() : String; trả thuộc tính email, không truy vấn CSDL. */
    public String getEmail() { return email; }

    /** setEmail(email : String); đóng gói thuộc tính email, không truy vấn CSDL. */
    public void setEmail(String email) { this.email = email; }

    /** getWorkingStatus() : String; trả thuộc tính workingStatus, không truy vấn CSDL. */
    public String getWorkingStatus() { return workingStatus; }

    /** setWorkingStatus(workingStatus : String); đóng gói thuộc tính workingStatus, không truy vấn CSDL. */
    public void setWorkingStatus(String workingStatus) { this.workingStatus = workingStatus; }

    /** getAccount() : Account4; trả thuộc tính account, không truy vấn CSDL. */
    public Account4 getAccount() { return account; }

    /** setAccount(account : Account4); đóng gói thuộc tính account, không truy vấn CSDL. */
    public void setAccount(Account4 account) { this.account = account; }
}
```

`Staff4()` khởi tạo đối tượng rỗng; các getter/setter thao tác trực tiếp những trường khai báo phía trên.

**Lớp ManagementStaff4** — [mã nguồn](../code/restaurant/src/main/java/model/ManagementStaff4.java).

```java
/* ===================================================================
 * LỚP      : ManagementStaff4 (tầng thực thể)
 * THIẾT KẾ : class-design-add-dish.puml – hộp ManagementStaff4 (Hình 5, desing.md §4a)
 * MODULE 2 : ManagementStaff4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: không có trên class-add-dish.puml; là actor Nhân viên quản lý. Có mặt vì là
 *            kiểu của thuộc tính ẩn managementStaff trên mọi trang gd…jsp (desing.md §3).
 * QUAN HỆ  : Staff4 <|-- ManagementStaff4.
 * CSDL     : dòng tblStaff4 có vaitro = 'ManagementStaff4'.
 * KỊCH BẢN : không thuộc v.3; đặt vào session khi đăng nhập (doLogin4.jsp), đọc ở
 *            WEB-INF/auth.jspf cho tiền điều kiện đã đăng nhập.
 * KỊCH BẢN MODULE 2: không có bước riêng; phục vụ tiền điều kiện đăng nhập, managementStaff trong session.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public class ManagementStaff4 extends Staff4 {

    /** ManagementStaff4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public ManagementStaff4() {}
}
```

`ManagementStaff4()` khởi tạo đối tượng rỗng; các phương thức truy cập dữ liệu được kế thừa từ lớp cha.

**Lớp Account4** — [mã nguồn](../code/restaurant/src/main/java/model/Account4.java).

```java
/* ===================================================================
 * LỚP      : Account4 (tầng thực thể)
 * THIẾT KẾ : class-design-add-dish.puml – hộp Account4 (Hình 5, desing.md §4a)
 * MODULE 2 : Account4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: không có trên class-add-dish.puml; lấy từ class-entity-design.puml. Có mặt
 *            vì Staff4 có thuộc tính account : Account4.
 * QUAN HỆ  : Staff4 *-- Account4.
 * CSDL     : các cột username, password, accountStatus của tblStaff4 (gộp 1–1).
 * KỊCH BẢN : không thuộc v.3; chỉ dùng khi đăng nhập (StaffDAO4.checkLogin).
 * KỊCH BẢN MODULE 2: không có bước riêng; phục vụ tiền điều kiện đăng nhập, managementStaff trong session.
 * NGOẠI LỆ : không xử lý nhánh nào; POJO chỉ chứa dữ liệu.
 * =================================================================== */
package model;

public class Account4 {
    private int id;
    private String username;
    private String password;
    private String accountStatus;

    /** Account4(): khởi tạo đối tượng rỗng để DAO4/JSP đóng gói; không truy vấn CSDL. */
    public Account4() {}

    /** getId() : int; trả thuộc tính id, không truy vấn CSDL. */
    public int getId() { return id; }

    /** setId(id : int); đóng gói thuộc tính id, không truy vấn CSDL. */
    public void setId(int id) { this.id = id; }

    /** getUsername() : String; trả thuộc tính username, không truy vấn CSDL. */
    public String getUsername() { return username; }

    /** setUsername(username : String); đóng gói thuộc tính username, không truy vấn CSDL. */
    public void setUsername(String username) { this.username = username; }

    /** getPassword() : String; trả thuộc tính password, không truy vấn CSDL. */
    public String getPassword() { return password; }

    /** setPassword(password : String); đóng gói thuộc tính password, không truy vấn CSDL. */
    public void setPassword(String password) { this.password = password; }

    /** getAccountStatus() : String; trả thuộc tính accountStatus, không truy vấn CSDL. */
    public String getAccountStatus() { return accountStatus; }

    /** setAccountStatus(accountStatus : String); đóng gói thuộc tính accountStatus, không truy vấn CSDL. */
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
}
```

`Account4()` khởi tạo đối tượng rỗng; các getter/setter thao tác trực tiếp những trường khai báo phía trên.

### c Mẫu lớp DAO4 và cách thực hiện từng phương thức

**Lớp DAO4** — [mã nguồn](../code/restaurant/src/main/java/dao/DAO4.java).

```java
/* ===================================================================
 * LỚP      : DAO4 (tầng thao tác dữ liệu, abstract)
 * THIẾT KẾ : class-design-add-dish.puml – hộp DAO4: {static} #con : Connection, +DAO4()
 *            (Hình 5, desing.md §4a).
 * MODULE 2 : DAO4 dùng chung ở Hình 6 (class-design-customer-statistics.puml), desing.md §4b.
 * PHÂN TÍCH: không có trên class-add-dish.puml; bổ sung theo §4.3.1 Bước 3 của giáo trình
 *            (lớp cha dùng chung kết nối CSDL).
 * QUAN HỆ  : DAO4 <|-- DishDAO4; DAO4 <|-- DishCategoryDAO4.
 * CSDL     : không đọc bảng nào; chỉ mở kết nối tới CSDL restaurant.
 * KỊCH BẢN : không có bước v.3 riêng; hàm tạo chạy khi trang tạo DishDAO4/DishCategoryDAO4
 *            (v.3 3, 9, 18, 30, 37, 46, 52).
 * KỊCH BẢN MODULE 2: tk-v.3 9–17, 21–26, 33–38, 42–47; dùng chung kết nối.
 * NGOẠI LỆ : không xử lý nhánh nào; lỗi kết nối ném IllegalStateException.
 * [NGOÀI THIẾT KẾ] Nạp db.properties; khóa DAO4.class bảo vệ kết nối dùng chung.
 * =================================================================== */
package dao;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

public abstract class DAO4 {
    protected static Connection con;

    /** DAO4(): mở một kết nối dùng chung khi chưa có/đã đóng.
     * Không có phương thức phân tích tương ứng; không trả dữ liệu nghiệp vụ.
     * Đọc db.properties; lỗi kết nối ném IllegalStateException, không giả danh A1.
     */
    public DAO4() {
        synchronized (DAO4.class) {
            try {
                if (con == null || con.isClosed()) {
                    Properties properties = new Properties();
                    String config = System.getProperty("db.config");
                    try (InputStream input = config == null
                            ? DAO4.class.getResourceAsStream("/db.properties") : Files.newInputStream(Path.of(config))) {
                        if (input == null) throw new IOException("Thiếu db.properties");
                        properties.load(input);
                    }
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    con = DriverManager.getConnection(properties.getProperty("url"),
                            properties.getProperty("user"), properties.getProperty("password"));
                }
            } catch (IOException | SQLException | ClassNotFoundException e) {
                throw new IllegalStateException("Không thể kết nối CSDL.", e);
            }
        }
    }
}
```

`DAO4()` tải cấu hình, nạp JDBC driver và mở kết nối chỉ khi chưa có hoặc kết nối đã đóng. `protected static Connection con` dùng chung cho DAO4 con; khóa DAO4.class bao quanh cả mở kết nối và từng thao tác dữ liệu. Vì dùng một kết nối, yêu cầu JDBC thực hiện tuần tự, phù hợp phạm vi bài tập. Không mở thêm kết nối theo từng lần tải trang. Lỗi cấu hình/kết nối báo IllegalStateException. Hàm tạo không thuộc bước nghiệp vụ v.2/v.3.

**Lớp DishDAO4** — [mã nguồn](../code/restaurant/src/main/java/dao/DishDAO4.java).

```java
/* ===================================================================
 * LỚP      : DishDAO4 (tầng thao tác dữ liệu)
 * THIẾT KẾ : class-design-add-dish.puml – hộp DishDAO4 (Hình 5, desing.md §4a)
 * PHÂN TÍCH: ba phương thức của lớp Dish4 trong class-add-dish.puml – getDishes(),
 *            validateDish(), saveDish() – chuyển sang DAO4, giữ nguyên tên và thứ tự.
 * QUAN HỆ  : DAO4 <|-- DishDAO4; DishDAO4–Dish4; được gọi từ dishManagementView4.jsp, doSaveDish4.jsp.
 * CSDL     : tblProduct4 (đọc, ghi), tblDish4 (đọc, ghi), tblDishCategory4 (đọc khi kiểm tra).
 * KỊCH BẢN : v.3 4–8, 31–32, 34–35, 47–51; v.2 4–5, 17–18, 20–21, 25–26.
 * NGOẠI LỆ : A2, E1 (validateDish, bước 32); E2 (saveDish, bước 35).
 * =================================================================== */
package dao;

import model.Dish4;
import java.sql.*;
import java.util.ArrayList;

public class DishDAO4 extends DAO4 {
    /** DishDAO4(): dùng kết nối của DAO4; không truy vấn bảng. */
    public DishDAO4() { super(); }

    /** getDishes() : Dish4[]; từ Dish4.getDishes() ở phân tích.
     * v.3 4–8, 47–51; v.2 4–5,25–26. Đọc tblProduct4 JOIN tblDish4.
     * Mảng rỗng khi không có món; lỗi SQL ném IllegalStateException.
     */
    // [v3-4, v3-47][v2-4, v2-25] Lớp DishDAO4 gọi hàm getDishes().
    public Dish4[] getDishes() {
        synchronized (DAO4.class) {
            ArrayList<Dish4> list = new ArrayList<>();
            try (PreparedStatement statement = con.prepareStatement(
                    "SELECT p.* FROM tblProduct4 p JOIN tblDish4 d ON d.tblProduct4id=p.id ORDER BY p.productCode");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    // [v3-5, v3-48][v2-4, v2-25] Hàm getDishes() gọi lớp Dish4 để đóng gói thông tin.
                    Dish4 dish = new Dish4();
                    // [v3-6, v3-49][v2-4, v2-25] Lớp Dish4 đóng gói thông tin thực thể.
                    dish.setId(rs.getInt("id"));
                    dish.setProductCode(rs.getString("productCode"));
                    dish.setName(rs.getString("name"));
                    dish.setDescription(rs.getString("description"));
                    dish.setSalePrice(rs.getFloat("salePrice"));
                    dish.setImage(rs.getString("image"));
                    dish.setSaleStatus(rs.getString("saleStatus"));
                    dish.setCreatedAt(rs.getTimestamp("createdAt"));
                    dish.setUpdatedAt(rs.getTimestamp("updatedAt"));
                    // [v3-7, v3-50][v2-4, v2-25] Lớp Dish4 trả kết quả cho hàm getDishes().
                    list.add(dish);
                }
                // [v3-8, v3-51][v2-5, v2-26] Hàm getDishes() trả kết quả cho trang dishManagementView4.jsp.
                return list.toArray(new Dish4[0]);
            } catch (SQLException e) { throw new IllegalStateException("Không thể lấy danh sách món ăn.", e); }
        }
    }

    /** validateDish(dish : Dish4, idDishCategory : int) : String[]; từ Dish4.validateDish().
     * v.3 31–32; v.2 17–18; E1/A2. Đọc tblProduct4, tblDishCategory4.
     * [] là hợp lệ; mã trường lỗi theo thứ tự biểu mẫu; khongConLoaiMon đứng riêng.
     */
    // [v3-31][v2-17] Lớp DishDAO4 gọi hàm validateDish().
    public String[] validateDish(Dish4 dish, int idDishCategory) {
        synchronized (DAO4.class) {
            ArrayList<String> errors = new ArrayList<>();
            try {
                try (PreparedStatement s = con.prepareStatement("SELECT 1 FROM tblDishCategory4"); ResultSet rs = s.executeQuery()) {
                    // [A2][v3-32/A2] Không cho lưu. Cần tạo loại món ăn trước khi thêm món.
                    if (!rs.next()) return new String[]{"khongConLoaiMon"};
                }
                boolean duplicate;
                try (PreparedStatement s = con.prepareStatement("SELECT 1 FROM tblProduct4 WHERE productCode=?")) {
                    s.setString(1, dish.getProductCode());
                    try (ResultSet rs = s.executeQuery()) { duplicate = rs.next(); }
                }
                // [E1][v3-32/E1] Không lưu món ăn. Chỉ rõ trường lỗi: mã món bị trùng, thiếu dữ liệu bắt buộc, giá bán phải lớn hơn 0 hoặc loại món ăn không tồn tại.
                if (dish.getProductCode() == null || dish.getProductCode().isBlank() || duplicate) errors.add("inDishCode");
                if (dish.getName() == null || dish.getName().isBlank()) errors.add("inDishName");
                if (!Float.isFinite(dish.getSalePrice()) || dish.getSalePrice() <= 0) errors.add("inSalePrice");
                if (dish.getSaleStatus() == null || dish.getSaleStatus().isBlank()
                        || !(dish.getSaleStatus().equals("Đang kinh doanh") || dish.getSaleStatus().equals("Ngừng kinh doanh"))) errors.add("inSaleStatus");
                try (PreparedStatement s = con.prepareStatement("SELECT 1 FROM tblDishCategory4 WHERE id=?")) {
                    s.setInt(1, idDishCategory);
                    try (ResultSet rs = s.executeQuery()) { if (!rs.next()) errors.add("inoutDishCategory"); }
                }
                // [v3-32][v2-18] Hàm validateDish() trả kết quả hợp lệ cho trang doSaveDish4.jsp.
                return errors.toArray(new String[0]);
            } catch (SQLException e) { throw new IllegalStateException("Không thể kiểm tra món ăn.", e); }
        }
    }

    /** saveDish(dish : Dish4, idDishCategory : int) : Dish4; từ Dish4.saveDish().
     * v.3 34–35; v.2 20–21; E2. Ghi tblProduct4 và tblDish4 trong một giao dịch.
     * Thành công trả Dish4 có id/createdAt; lỗi rollback, trả null; khôi phục autoCommit.
     */
    // [v3-34][v2-20] Lớp DishDAO4 gọi hàm saveDish().
    public Dish4 saveDish(Dish4 dish, int idDishCategory) {
        synchronized (DAO4.class) {
            int id;
            Timestamp createdAt = new Timestamp(System.currentTimeMillis());
            try {
                con.setAutoCommit(false);
                try (PreparedStatement s = con.prepareStatement(
                        "INSERT INTO tblProduct4(productCode,name,description,salePrice,image,saleStatus,createdAt,updatedAt) VALUES(?,?,?,?,?,?,?,NULL)", Statement.RETURN_GENERATED_KEYS)) {
                    s.setString(1, dish.getProductCode()); s.setString(2, dish.getName());
                    s.setString(3, dish.getDescription()); s.setFloat(4, dish.getSalePrice());
                    s.setString(5, dish.getImage()); s.setString(6, dish.getSaleStatus()); s.setTimestamp(7, createdAt);
                    s.executeUpdate();
                    try (ResultSet rs = s.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Không có khóa sinh tự động.");
                        id = rs.getInt(1);
                    }
                }
                try (PreparedStatement s = con.prepareStatement("INSERT INTO tblDish4(tblDishCategory4id,tblProduct4id) VALUES(?,?)")) {
                    s.setInt(1, idDishCategory); s.setInt(2, id); s.executeUpdate();
                }
                con.commit();
                dish.setId(id); dish.setCreatedAt(createdAt); dish.setUpdatedAt(null);
                // [v3-35][v2-21] Hàm saveDish() trả kết quả món ăn vừa tạo cho trang doSaveDish4.jsp.
                return dish;
            } catch (Exception e) {
                // [E2][v3-35/E2] Chưa tạo được món ăn.
                try { con.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                return null;
            } finally {
                try { con.setAutoCommit(true); }
                catch (SQLException e) { throw new IllegalStateException("Không thể khôi phục kết nối.", e); }
            }
        }
    }
}
```

`DishDAO4()` gọi hàm tạo DAO4, dùng lại kết nối. `getDishes()` thực hiện SELECT JOIN tblProduct4–tblDish4, sắp theo productCode, tạo một Dish4 cho mỗi dòng, điền chín trường kế thừa và trả Dish4[]. Đây là v.2 bước 4–5,25–26 và v.3 bước 4–8,47–51. Không có món thì trả mảng rỗng; lỗi SQL báo lỗi thay vì hiển thị danh sách rỗng giả.

`validateDish(dish, idDishCategory)` kiểm tra bảng loại trước; nếu không còn loại, trả duy nhất khongConLoaiMon (A2). Nếu có loại, kiểm tra mã trên toàn tblProduct4, tên, giá dương hữu hạn, trạng thái trong danh sách cố định và id loại còn tồn tại. Mã lỗi trả theo thứ tự trường trên biểu mẫu; mảng rỗng là hợp lệ. Phương thức thực hiện v.2 bước 17–18, v.3 bước 31–32 và nhánh E1/A2; không ghi CSDL.

`saveDish(dish, idDishCategory)` tắt autoCommit, ghi tblProduct4 với createdAt hiện tại/updatedAt NULL, lấy khóa sinh tự động rồi ghi tblDish4. Commit xong mới điền id/createdAt vào Dish4 và trả chính đối tượng đó. Lỗi khóa ngoại, mã trùng do hai lần gửi xen nhau hoặc lỗi ghi khác đều rollback và trả null; finally bật lại autoCommit. Đây là v.2 bước 20–21, v.3 bước 34–35 và E2. Khóa chung giữ trọn hai INSERT trong cùng giao dịch, không xen câu SQL từ request khác.

**Lớp DishCategoryDAO4** — [mã nguồn](../code/restaurant/src/main/java/dao/DishCategoryDAO4.java).

```java
/* ===================================================================
 * LỚP      : DishCategoryDAO4 (tầng thao tác dữ liệu)
 * THIẾT KẾ : class-design-add-dish.puml – hộp DishCategoryDAO4 (Hình 5, desing.md §4a)
 * PHÂN TÍCH: phương thức getDishCategories() của lớp DishCategory4 trong class-add-dish.puml,
 *            chuyển sang DAO4, giữ nguyên tên.
 * QUAN HỆ  : DAO4 <|-- DishCategoryDAO4; DishCategoryDAO4–DishCategory4; được gọi từ
 *            dishManagementView4.jsp, addDishView4.jsp.
 * CSDL     : tblDishCategory4 (đọc), tblDish4 (đọc, để điền DishCategory4.dishes).
 * KỊCH BẢN : v.3 10–14, 19–23, 38–42, 53–57; v.2 10–11.
 * NGOẠI LỆ : A1 – trả mảng rỗng khi không có loại món ăn (addDishView4.jsp xử lý ở bước 24).
 * =================================================================== */
package dao;

import model.Dish4;
import model.DishCategory4;
import java.sql.*;
import java.util.ArrayList;

public class DishCategoryDAO4 extends DAO4 {
    /** DishCategoryDAO4(): dùng kết nối của DAO4; không truy vấn bảng. */
    public DishCategoryDAO4() { super(); }

    /** getDishCategories() : DishCategory4[]; từ DishCategory4.getDishCategories().
     * v.3 10–14,19–23,38–42,53–57; v.2 10–11. Đọc tblDishCategory4, tblDish4.
     * A1: mảng rỗng khi không có loại; dishes chỉ chứa id để tìm loại của món.
     * Lỗi SQL ném IllegalStateException, không trả danh sách rỗng giả.
     */
    // [v3-10, v3-19, v3-38, v3-53][v2-10] Lớp DishCategoryDAO4 gọi hàm getDishCategories().
    public DishCategory4[] getDishCategories() {
        synchronized (DAO4.class) {
            ArrayList<DishCategory4> list = new ArrayList<>();
            try {
                try (PreparedStatement s = con.prepareStatement("SELECT * FROM tblDishCategory4 ORDER BY categoryCode"); ResultSet rs = s.executeQuery()) {
                    while (rs.next()) {
                        // [v3-11, v3-20, v3-39, v3-54][v2-10] Hàm getDishCategories() gọi lớp DishCategory4 để đóng gói thông tin.
                        DishCategory4 category = new DishCategory4();
                        // [v3-12, v3-21, v3-40, v3-55][v2-10] Lớp DishCategory4 đóng gói thông tin thực thể.
                        category.setId(rs.getInt("id")); category.setCategoryCode(rs.getString("categoryCode"));
                        category.setName(rs.getString("name")); category.setDescription(rs.getString("description"));
                        category.setDishes(new Dish4[0]);
                        // [v3-13, v3-22, v3-41, v3-56][v2-10] Lớp DishCategory4 trả kết quả cho hàm getDishCategories().
                        list.add(category);
                    }
                }
                try (PreparedStatement s = con.prepareStatement("SELECT tblDishCategory4id,tblProduct4id FROM tblDish4"); ResultSet rs = s.executeQuery()) {
                    java.util.Map<Integer, ArrayList<Dish4>> members = new java.util.HashMap<>();
                    while (rs.next()) {
                        Dish4 dish = new Dish4(); dish.setId(rs.getInt("tblProduct4id"));
                        members.computeIfAbsent(rs.getInt("tblDishCategory4id"), key -> new ArrayList<>()).add(dish);
                    }
                    // [v3-12, v3-21, v3-40, v3-55][v2-10] Lớp DishCategory4 đóng gói thông tin thực thể.
                    for (DishCategory4 category : list) category.setDishes(members.getOrDefault(category.getId(), new ArrayList<>()).toArray(new Dish4[0]));
                }
                // [v3-14, v3-57] Hàm getDishCategories() trả kết quả cho trang dishManagementView4.jsp.
                // [v3-23, v3-42][v2-11] Hàm getDishCategories() trả kết quả cho trang addDishView4.jsp.
                return list.toArray(new DishCategory4[0]);
            } catch (SQLException e) { throw new IllegalStateException("Không thể lấy danh sách loại món ăn.", e); }
        }
    }
}
```

`DishCategoryDAO4()` gọi DAO4() để dùng kết nối chung. `getDishCategories()` đọc loại theo categoryCode, đóng gói DishCategory4; truy vấn thứ hai đọc tblDish4 để gom Dish4 chỉ mang id, rồi gán vào từng category.dishes. Mảng trả về là DishCategory4[]; loại không có món nhận dishes rỗng. Đây là v.2 bước 10–11, v.3 bước 10–14,19–23,38–42,53–57. Không có loại thì trả mảng rỗng, trang thêm món xử lý A1; lỗi SQL được báo riêng.

**Lớp StaffDAO4** — [mã nguồn](../code/restaurant/src/main/java/dao/StaffDAO4.java).

```java
/* ===================================================================
 * LỚP      : StaffDAO4 (đăng nhập)
 * [NGOÀI THIẾT KẾ] Không có hộp trên class-design-add-dish.puml hay biểu đồ nào khác;
 *            chỉ cung cấp tiền điều kiện "nhân viên quản lý đã đăng nhập" của use case.
 * PHÂN TÍCH: không có; tương tự ThanhvienDAO.kiemtraDangnhap() của giáo trình (Chương 5).
 * QUAN HỆ  : kế thừa DAO4; được gọi từ doLogin4.jsp; điền ManagementStaff4 và Account4.
 * CSDL     : tblStaff4 (đọc).
 * KỊCH BẢN : không thuộc v.3.
 * NGOẠI LỆ : không thuộc A1, A2, E1, E2; sai thông tin trả false.
 * =================================================================== */
package dao;

import model.Account4;
import model.ManagementStaff4;
import java.sql.*;

public class StaffDAO4 extends DAO4 {
    /** StaffDAO4(): dùng kết nối của DAO4; ngoài biểu đồ module. */
    public StaffDAO4() { super(); }

    /** checkLogin(managementStaff : ManagementStaff4) : boolean.
     * [NGOÀI THIẾT KẾ] Không có bước v.3; đọc tblStaff4, lọc vai trò/tài khoản hoạt động.
     * true: điền Staff4 và Account4 (id dùng chung sau gộp bảng); false: sai thông tin.
     */
    public boolean checkLogin(ManagementStaff4 managementStaff) {
        synchronized (DAO4.class) {
            Account4 account = managementStaff.getAccount();
            try (PreparedStatement s = con.prepareStatement("SELECT * FROM tblStaff4 WHERE username=? AND password=? AND vaitro=? AND accountStatus=?")) {
                s.setString(1, account.getUsername()); s.setString(2, account.getPassword());
                s.setString(3, "ManagementStaff4"); s.setString(4, "Đang hoạt động");
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) return false;
                    managementStaff.setId(rs.getInt("id")); managementStaff.setFullName(rs.getString("fullName"));
                    managementStaff.setPhoneNumber(rs.getString("phoneNumber")); managementStaff.setEmail(rs.getString("email"));
                    managementStaff.setWorkingStatus(rs.getString("workingStatus"));
                    account.setId(rs.getInt("id")); account.setAccountStatus(rs.getString("accountStatus"));
                    account.setPassword(null);
                    return true;
                }
            } catch (SQLException e) { throw new IllegalStateException("Không thể đăng nhập.", e); }
        }
    }
}
```

`StaffDAO4()` dùng kết nối của DAO4. `checkLogin(managementStaff)` kiểm tra username/password bằng PreparedStatement, đồng thời lọc đúng ManagementStaff4 và tài khoản đang hoạt động. Nếu tìm được, điền các trường Staff4, Account4.id/accountStatus, xóa mật khẩu trong đối tượng trước khi đưa lên session và trả true; không khớp trả false. Lỗi SQL báo lỗi. Hai hàm nằm ngoài kịch bản module, chỉ thực hiện tiền điều kiện đăng nhập.

### d Các lớp giao diện JSP

Các tệp JSP là lớp giao diện trong Hình 5; Tomcat sinh lớp servlet tương ứng khi chạy. Chúng không khai báo phương thức nghiệp vụ riêng. Mẫu bên dưới là mã JSP thực thi; tên và kiểu thuộc tính hiện được ghi cạnh HTML, còn thuộc tính ẩn được đặt trong request/session đúng tên thiết kế. Thông tin người dùng từ session được kiểm tra trước khi truy vấn hoặc lưu.

**managerMainView4.jsp** — [mã nguồn](../code/restaurant/src/main/webapp/quanly/managerMainView4.jsp).

```jsp
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
```

Đọc managementStaff từ session, thiếu thì chuyển đăng nhập với err=timeout. Link subManageDish mở danh sách (v.2 1–2, v.3 1–2). subStatisticsReport mở trang statisticsReportView4.jsp dùng chung với module thống kê.

**dishManagementView4.jsp** — [mã nguồn](../code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp).

```jsp
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
```

Gọi getDishes và getDishCategories, đặt hai mảng vào request rồi hiển thị bảng. Cột loại món đối chiếu từng DishCategory4.dishes[].id với Dish4.id. Các nút mở biểu mẫu thêm hoặc quay về trang chính (v.2 3–8,24–27; v.3 3–17,46–58). Danh sách được truy vấn lại sau khi lưu.

**addDishView4.jsp** — [mã nguồn](../code/restaurant/src/main/webapp/quanly/addDishView4.jsp).

```jsp
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
```

Đọc danh sách loại và hiển thị form multipart. A1: danh sách loại rỗng thì thông báo và vô hiệu hóa Lưu. Khi doSaveDish4.jsp trả về, trang chỉ nhận các tên có trên biểu đồ: điền lại các ô từ thuộc tính ẩn dish, chọn lại loại và giá từ chính tham số inoutDishCategory, inSalePrice của biểu mẫu, và hiển thị nội dung outMessage. Khi dish.id > 0 (saveDish đã gán id), trang hiển thị thông báo thành công, khóa biểu mẫu và chuyển về danh sách sau hai giây hoặc khi click OK (v.2 9–15,22–23; v.3 18–26,37–45). Nút quay lại không gửi dữ liệu tới trang lưu.

**doSaveDish4.jsp** — [mã nguồn](../code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp).

```jsp
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
```

Chỉ chấp nhận POST với session quản lý. Đọc tham số UTF-8, đóng gói Dish4 và id loại; giá hoặc id sai cú pháp được chuyển về giá trị không hợp lệ để validateDish trả mã trường lỗi. Gọi kiểm tra, nếu E1/A2 thì đổi từng mã lỗi thành câu của bảng Thông báo và forward với dish và outMessage. Hợp lệ mới ghi ảnh tùy chọn và gọi saveDish. Kết quả null hoặc lỗi ảnh trả E2, dọn ảnh mới, đặt lại dish.id = 0; thành công thì dish đã có id, đặt outMessage là thông báo thành công rồi forward. Việc forward kết quả nằm ngoài khối lưu để lỗi render sau commit không bị hiểu thành lỗi giao dịch (v.2 16–21; v.3 27–36).

### e Kiểm thử module

JUnit 5 thực hiện kiểm thử tích hợp với MySQL `restaurant_test`. Trước từng ca, DatabaseTest4 kiểm tra tên database, đóng kết nối cũ, xóa bốn bảng rồi nạp schema.sql và seed.sql. Sau mỗi ca đóng kết nối. URL không trỏ chính xác restaurant_test bị từ chối trước mọi thao tác xóa bảng. Không dùng database của ứng dụng để chạy JUnit.

| Ca | Dữ liệu vào / điều kiện | Kết quả mong đợi |
| --- | --- | --- |
| T01 | D004, Cơm gà, 65000, Đang kinh doanh, LM02 | validateDish trả [] |
| T02 | Mã D001 đã có | [inDishCode] |
| T03 | Mã C001 chỉ có trong tblProduct4, không có tblDish4 | [inDishCode], mã dùng chung với sản phẩm khác |
| T04 | Tên chỉ có khoảng trắng | [inDishName] |
| T05–T08 | Giá 0, −1, NaN, +Infinity | [inSalePrice] trong từng ca |
| T09 | Id loại không tồn tại | [inoutDishCategory] |
| T10 | Trống mã, tên, trạng thái; giá 0; id loại sai | inDishCode, inDishName, inSalePrice, inSaleStatus, inoutDishCategory theo thứ tự |
| T11 | Bảng loại rỗng, dữ liệu các trường cũng thiếu | Chỉ [khongConLoaiMon] |
| T12 | Lưu T01 | Cùng Dish4 có id dương và createdAt; đủ hai bản ghi; updatedAt NULL |
| T13 | Lưu với khóa ngoại loại sai | null, không có tblProduct4 dở dang; autoCommit phục hồi; lần lưu kế tiếp thành công |
| T14 | Mã hợp lệ lúc kiểm tra nhưng đã bị lưu trước đó | null ở lần lưu thứ hai; chỉ một sản phẩm/món |
| T15 | getDishes trên seed | 3 món D001,D002,D003 đúng thứ tự; đủ trường |
| T16 | getDishCategories trên seed | 3 loại, LM01 rỗng, LM02 chứa id của D001 và D002 |
| T17 | getDishCategories khi bảng loại rỗng | DishCategory4[] rỗng |
| T18 | Tài khoản minh họa hoạt động | true, điền Staff4/Account4, không giữ password trong session |
| T19 | Mật khẩu sai | false |
| T20 | Tài khoản không hoạt động hoặc vai trò SalesStaff4 | false trong cả hai lần kiểm tra |

Mã kiểm thử: [DishDAOTest4](../code/restaurant/src/test/java/dao/DishDAOTest4.java), [DishCategoryDAOTest4](../code/restaurant/src/test/java/dao/DishCategoryDAOTest4.java), [StaffDAOTest4](../code/restaurant/src/test/java/dao/StaffDAOTest4.java). Chạy bằng `mvn test` trong code/restaurant sau khi tạo database kiểm thử. Có thể ghi đè test.db.url, test.db.user, test.db.password bằng thuộc tính JVM Maven.

Kiểm thử giao diện gồm luồng D004 Cơm gà; lỗi trùng mã, tên rỗng, giá 0 và giữ dữ liệu; loại bị xóa sau khi mở form (A2); mở form khi không có loại (A1); gây lỗi INSERT tblDish4 để kiểm tra E2 không để lại tblProduct4; hai nút quay lại và chuyển đăng nhập khi mất session. CSDL minh họa được phục hồi sau ca xóa loại và gỡ điều kiện gây lỗi sau ca E2.

### f Truy vết thiết kế đến mã

Mọi lớp, trường và chữ ký của Hình 5 có cùng tên trong Java/JSP. Constructor/getter/setter chỉ phục vụ đóng gói; các phần ngoài hình đã liệt kê ở mục a. Các comment v.3 chỉ tới câu kịch bản; cùng dòng có thể phục vụ nhiều lần gọi. Công cụ chạy từ thư mục bất kỳ:

```sh
python3 code/restaurant/tools/trace.py
```

Công cụ kiểm tra đủ 58 số chính và bốn nhánh, đồng thời so sánh hai chiều với số thông điệp trong sequence-add-dish.puml. Với biểu đồ hoạt động, công cụ đối chiếu bốn vùng JSP, 13 hoạt động, năm nút DAO4 và đúng 17 chuyển tiếp; mỗi đường gọi DAO4 phải có một đường hai đầu mũi tên, đúng trang gọi và đúng phương thức trên Hình 5 cũng như trong mã. Biểu đồ tuần tự chỉ mô tả luồng chuẩn; các nhánh thay thế và ngoại lệ được kiểm tra bằng tag trong mã và trình bày riêng ở mục 6c. Bảng sau được xuất trực tiếp từ công cụ; đường dẫn có số dòng tương ứng mã nguồn.

**Bảng truy vết kịch bản v.3 → mã**

| Bước / nhánh | Vị trí mã (file:dòng) |
| --- | --- |
| 1 | `code/restaurant/src/main/webapp/quanly/managerMainView4.jsp:18` |
| 2 | `code/restaurant/src/main/webapp/quanly/managerMainView4.jsp:19` |
| 3 | `code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:18` |
| 4 | `code/restaurant/src/main/java/dao/DishDAO4.java:25`<br>`code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:20` |
| 5 | `code/restaurant/src/main/java/dao/DishDAO4.java:33` |
| 6 | `code/restaurant/src/main/java/dao/DishDAO4.java:35` |
| 7 | `code/restaurant/src/main/java/dao/DishDAO4.java:45` |
| 8 | `code/restaurant/src/main/java/dao/DishDAO4.java:48` |
| 9 | `code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:24` |
| 10 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:28`<br>`code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:26` |
| 11 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:35` |
| 12 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:37`<br>`code/restaurant/src/main/java/dao/DishCategoryDAO4.java:51` |
| 13 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:41` |
| 14 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:54` |
| 15 | `code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:33` |
| 16 | `code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:46` |
| 17 | `code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:44` |
| 18 | `code/restaurant/src/main/webapp/quanly/addDishView4.jsp:22` |
| 19 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:28`<br>`code/restaurant/src/main/webapp/quanly/addDishView4.jsp:24` |
| 20 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:35` |
| 21 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:37`<br>`code/restaurant/src/main/java/dao/DishCategoryDAO4.java:51` |
| 22 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:41` |
| 23 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:55` |
| 24 | `code/restaurant/src/main/webapp/quanly/addDishView4.jsp:57` |
| 25 | `code/restaurant/src/main/webapp/quanly/addDishView4.jsp:61` |
| 26 | `code/restaurant/src/main/webapp/quanly/addDishView4.jsp:58` |
| 27 | `code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:20` |
| 28 | `code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:25` |
| 29 | `code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:34` |
| 30 | `code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:35` |
| 31 | `code/restaurant/src/main/java/dao/DishDAO4.java:58`<br>`code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:37` |
| 32 | `code/restaurant/src/main/java/dao/DishDAO4.java:82`<br>`code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:39` |
| 33 | `code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:76` |
| 34 | `code/restaurant/src/main/java/dao/DishDAO4.java:92`<br>`code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:77` |
| 35 | `code/restaurant/src/main/java/dao/DishDAO4.java:115`<br>`code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:79` |
| 36 | `code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:91` |
| 37 | `code/restaurant/src/main/webapp/quanly/addDishView4.jsp:22` |
| 38 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:28`<br>`code/restaurant/src/main/webapp/quanly/addDishView4.jsp:24` |
| 39 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:35` |
| 40 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:37`<br>`code/restaurant/src/main/java/dao/DishCategoryDAO4.java:51` |
| 41 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:41` |
| 42 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:55` |
| 43 | `code/restaurant/src/main/webapp/quanly/addDishView4.jsp:46` |
| 44 | `code/restaurant/src/main/webapp/quanly/addDishView4.jsp:42`<br>`code/restaurant/src/main/webapp/quanly/addDishView4.jsp:53` |
| 45 | `code/restaurant/src/main/webapp/quanly/addDishView4.jsp:41`<br>`code/restaurant/src/main/webapp/quanly/addDishView4.jsp:54` |
| 46 | `code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:18` |
| 47 | `code/restaurant/src/main/java/dao/DishDAO4.java:25`<br>`code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:20` |
| 48 | `code/restaurant/src/main/java/dao/DishDAO4.java:33` |
| 49 | `code/restaurant/src/main/java/dao/DishDAO4.java:35` |
| 50 | `code/restaurant/src/main/java/dao/DishDAO4.java:45` |
| 51 | `code/restaurant/src/main/java/dao/DishDAO4.java:48` |
| 52 | `code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:24` |
| 53 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:28`<br>`code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:26` |
| 54 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:35` |
| 55 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:37`<br>`code/restaurant/src/main/java/dao/DishCategoryDAO4.java:51` |
| 56 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:41` |
| 57 | `code/restaurant/src/main/java/dao/DishCategoryDAO4.java:54` |
| 58 | `code/restaurant/src/main/webapp/quanly/dishManagementView4.jsp:34` |
| A1 | `code/restaurant/src/main/webapp/quanly/addDishView4.jsp:50` |
| A2 | `code/restaurant/src/main/java/dao/DishDAO4.java:64`<br>`code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:41` |
| E1 | `code/restaurant/src/main/java/dao/DishDAO4.java:72`<br>`code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:41` |
| E2 | `code/restaurant/src/main/java/dao/DishDAO4.java:118`<br>`code/restaurant/src/main/webapp/quanly/doSaveDish4.jsp:84` |

**Đối chiếu biểu đồ hoạt động – mã (Hình 7)**

| Vùng | Hoạt động | Phương thức (Hình 5) | Mã cài đặt |
| --- | --- | --- | --- |
| managerMainView4.jsp | Nhận thông tin QL | — | `WEB-INF/auth.jspf` (managementStaff) |
| | Hiện GD | — | `managerMainView4.jsp` – subManageDish |
| dishManagementView4.jsp | Nhận thông tin QL | — | `WEB-INF/auth.jspf` (managementStaff) |
| | Lấy danh sách món ăn | `DishDAO4.getDishes()` | `dishManagementView4.jsp`, `DishDAO4.getDishes()` |
| | Lấy danh sách loại món ăn | `DishCategoryDAO4.getDishCategories()` | `dishManagementView4.jsp`, `DishCategoryDAO4.getDishCategories()` |
| | Hiện GD | — | `dishManagementView4.jsp` – outDishList, subAddDish |
| addDishView4.jsp | Nhận thông tin QL | — | `WEB-INF/auth.jspf` (managementStaff) |
| | Lấy danh sách loại món ăn | `DishCategoryDAO4.getDishCategories()` | `addDishView4.jsp`, `DishCategoryDAO4.getDishCategories()` |
| | Hiện GD | — | `addDishView4.jsp` – biểu mẫu, outMessage, subSave |
| | Thông báo thành công | — | `addDishView4.jsp` – outMessage |
| doSaveDish4.jsp | Nhận thông tin món ăn | — | `doSaveDish4.jsp` (dish) |
| | Kiểm tra thông tin món ăn | `DishDAO4.validateDish()` | `doSaveDish4.jsp`, `DishDAO4.validateDish()` |
| | Lưu món ăn | `DishDAO4.saveDish()` | `doSaveDish4.jsp`, `DishDAO4.saveDish()` |

Mỗi thông điệp của Hình 8 mang số câu v.3; mã cài đặt đánh dấu cùng số bằng tag `[v3-NN]`.

## 2 Cài đặt module Xem thống kê khách hàng theo doanh thu

Module dùng chung Maven WAR, package model/dao, lớp DAO4, đăng nhập và các lớp thực thể chung với module thêm món. Mỗi bảng, trường và chữ ký phương thức giữ nguyên Hình 6. Module chỉ đọc dữ liệu, không ghi kết quả thống kê vào CSDL.

### a Cấu trúc chương trình

| Thành phần | Tệp bổ sung / dùng chung |
| --- | --- |
| Thực thể | `Customer4`, `CustomerStatistic4`, `MembershipCard4`, `Order4`, `Invoice4`, `Payment4`, `OrderSubmission4`, `OrderSubmissionProduct4`, `SalesStaff4`, `Combo4`, `ComboDish4` trong `src/main/java/model` |
| DAO4 | `CustomerStatisticDAO4`, `OrderDAO4`, `OrderSubmissionProductDAO4` trong `src/main/java/dao` |
| Trang | `statisticsReportView4.jsp`, `customerStatisticsView4.jsp`, `customerSpendingView4.jsp`, `orderDetailView4.jsp` trong `src/main/webapp/quanly` |
| Dùng chung | `DAO4`, `Product4`, `Dish4`, `Staff4`, `ManagementStaff4`, `Account4`, `managerMainView4.jsp`, đăng nhập, `auth.jspf`, `common.jspf` |
| Hạ tầng trình bày | `WEB-INF/statistics.jspf`: đọc ngày/id, mã hóa tham số URL, hiển thị ngày và tiền, lấy lần thanh toán thành công sớm nhất; không tạo thuộc tính request/session mới |
| CSDL | `db/schema.sql`, `db/seed.sql`: bổ sung bảng và dữ liệu ở cuối tệp |
| Kiểm thử | `StatisticsDAOTest4`, `DatabaseTest4` dùng chung; công cụ `tools/trace.py`, `tools/test_trace.py` |

Tám bảng bổ sung là tblCombo4, tblCustomer4, tblMembershipCard4, tblOrder4, tblInvoice4, tblPayment4, tblOrderSubmission4, tblOrderSubmissionProduct4. tblOrder4.tblCustomer4id nullable cho No name; tblInvoice4.tblOrder4id unique để mỗi đơn có tối đa một hóa đơn. Không tạo tblComboDish4 và tblServiceSession4 vì báo cáo không đọc hai bảng đó. Combo4 vẫn có comboDishes đúng thiết kế nhưng mảng không tải. Các khóa và nullable giữ nguyên thiết kế CSDL của những bảng được dùng.

Dữ liệu minh họa cho kỳ 01/08/2026–31/08/2026 cho kết quả KH001 Nguyễn Văn An 4.250.000, KH002 Trần Thị Bình 2.180.000, No name 1.350.000; KH003 không có hóa đơn hợp lệ nên không xuất hiện. KH001 có năm đơn DH101–DH105; DH103 có ba dòng đã chấp nhận tổng 1.100.000, loại dòng D003 bị từ chối. D005 và C001 bổ sung ở cuối seed.sql, không dùng mã D004 của kịch bản thêm món.

Chạy schema/seed một lần trên CSDL rỗng và triển khai WAR như mục Build and run trong README. Bộ kiểm thử chỉ nạp lại restaurant_test, xóa bảng module thống kê trước rồi tới bảng module thêm món theo thứ tự ngược khóa ngoại. Thông tin tài khoản minh họa chỉ ghi trong seed.sql.

### b Mẫu lớp thực thể, thuộc tính và phương thức

Mọi POJO có constructor rỗng, getter/setter, trường private đúng thứ tự và kiểu của Hình 6. CustomerStatistic4 kế thừa Customer4; Combo4 và Dish4 kế thừa Product4; SalesStaff4 kế thừa Staff4. Các lớp dùng chung chỉ có một tệp Java.

| Lớp | Thuộc tính khai báo riêng |
| --- | --- |
| [Customer4](../code/restaurant/src/main/java/model/Customer4.java) | `id : int`, `customerCode : String`, `fullName : String`, `phoneNumber : String`, `profileStatus : String`, `membershipCards : MembershipCard4[]` |
| [CustomerStatistic4](../code/restaurant/src/main/java/model/CustomerStatistic4.java) | `periodStart : Date`, `periodEnd : Date`, `totalSpending : float` |
| [MembershipCard4](../code/restaurant/src/main/java/model/MembershipCard4.java) | `id : int`, `cardCode : String`, `issuedAt : Date`, `status : String` |
| [Order4](../code/restaurant/src/main/java/model/Order4.java) | `id : int`, `orderCode : String`, `createdAt : Date`, `status : String`, `orderSubmissions : OrderSubmission4[]`, `customer : Customer4`, `invoice : Invoice4` |
| [Invoice4](../code/restaurant/src/main/java/model/Invoice4.java) | `id : int`, `invoiceCode : String`, `issuedAt : Date`, `amountDue : float`, `paymentStatus : String`, `salesStaff : SalesStaff4`, `payments : Payment4[]` |
| [Payment4](../code/restaurant/src/main/java/model/Payment4.java) | `id : int`, `amount : float`, `method : String`, `paidAt : Date`, `result : String`, `salesStaff : SalesStaff4` |
| [OrderSubmission4](../code/restaurant/src/main/java/model/OrderSubmission4.java) | `id : int`, `submittedAt : Date`, `orderSubmissionProducts : OrderSubmissionProduct4[]` |
| [OrderSubmissionProduct4](../code/restaurant/src/main/java/model/OrderSubmissionProduct4.java) | `id : int`, `quantity : int`, `recordedUnitPrice : float`, `note : String`, `processingStatus : String`, `processedAt : Date`, `rejectionReason : String`, `product : Product4`, `salesStaff : SalesStaff4` |
| [SalesStaff4](../code/restaurant/src/main/java/model/SalesStaff4.java) | Không có thuộc tính riêng, kế thừa lớp cha |
| [Combo4](../code/restaurant/src/main/java/model/Combo4.java) | `startsAt : Date`, `endsAt : Date`, `comboDishes : ComboDish4[]` |
| [ComboDish4](../code/restaurant/src/main/java/model/ComboDish4.java) | `id : int`, `quantity : int`, `dish : Dish4` |

**CustomerStatistic4.java** — [mã nguồn](../code/restaurant/src/main/java/model/CustomerStatistic4.java).

```java
/* ===================================================================
 LỚP      : CustomerStatistic4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp CustomerStatistic4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblCustomer4, tblOrder4, tblInvoice4, tblPayment4 (kết quả thống kê không lưu bảng riêng).
 KỊCH BẢN : module 2, tk-v.3 15, 28, 40.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class CustomerStatistic4 extends Customer4 {
    private Date periodStart;
    private Date periodEnd;
    private float totalSpending;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public CustomerStatistic4() {}
    /** Trả thuộc tính periodStart, không đọc CSDL. */
    public Date getPeriodStart() { return periodStart; }
    /** Đóng gói thuộc tính periodStart, không ghi CSDL. */
    public void setPeriodStart(Date periodStart) { this.periodStart = periodStart; }
    /** Trả thuộc tính periodEnd, không đọc CSDL. */
    public Date getPeriodEnd() { return periodEnd; }
    /** Đóng gói thuộc tính periodEnd, không ghi CSDL. */
    public void setPeriodEnd(Date periodEnd) { this.periodEnd = periodEnd; }
    /** Trả thuộc tính totalSpending, không đọc CSDL. */
    public float getTotalSpending() { return totalSpending; }
    /** Đóng gói thuộc tính totalSpending, không ghi CSDL. */
    public void setTotalSpending(float totalSpending) { this.totalSpending = totalSpending; }
}
```

**Combo4.java** — [mã nguồn](../code/restaurant/src/main/java/model/Combo4.java).

```java
/* ===================================================================
 LỚP      : Combo4 (tầng thực thể)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp Combo4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblCombo4 và tblProduct4.
 KỊCH BẢN : module 2, tk-v.3 45; mặt hàng combo của dòng gọi món.
 NGOẠI LỆ : không xử lý nhánh, POJO chỉ chứa dữ liệu.
 =================================================================== */
package model;
import java.util.Date;

public class Combo4 extends Product4 {
    private Date startsAt;
    private Date endsAt;
    private ComboDish4[] comboDishes;

    /** Khởi tạo rỗng để DAO4/JSP đóng gói, không đọc CSDL. */
    public Combo4() {}
    /** Trả thuộc tính startsAt, không đọc CSDL. */
    public Date getStartsAt() { return startsAt; }
    /** Đóng gói thuộc tính startsAt, không ghi CSDL. */
    public void setStartsAt(Date startsAt) { this.startsAt = startsAt; }
    /** Trả thuộc tính endsAt, không đọc CSDL. */
    public Date getEndsAt() { return endsAt; }
    /** Đóng gói thuộc tính endsAt, không ghi CSDL. */
    public void setEndsAt(Date endsAt) { this.endsAt = endsAt; }
    /** Trả thuộc tính comboDishes, không đọc CSDL. */
    public ComboDish4[] getComboDishes() { return comboDishes; }
    /** Đóng gói thuộc tính comboDishes, không ghi CSDL. */
    public void setComboDishes(ComboDish4[] comboDishes) { this.comboDishes = comboDishes; }
}
```

CustomerStatistic4.periodStart/periodEnd/totalSpending là thuộc tính tạm của báo cáo. DAO4 gán membershipCards rỗng vì không xem thẻ; Order4.orderSubmissions rỗng vì các dòng được lấy riêng; Invoice4.salesStaff, Payment4.salesStaff và OrderSubmissionProduct4.salesStaff là null vì không hiển thị nhân viên. Mỗi chỗ không tải có comment ngay trong mã. Order4.invoice và invoice.payments được đóng gói đầy đủ cho việc hiển thị thời điểm thanh toán; Product4 của dòng được tạo thành Dish4 hoặc Combo4 theo bảng con.

### c Mẫu lớp DAO4 và cách thực hiện từng phương thức

**CustomerStatisticDAO4.java** — [mã nguồn](../code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java).

```java
/* ===================================================================
 LỚP      : CustomerStatisticDAO4 (tầng thao tác dữ liệu)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp CustomerStatisticDAO4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblCustomer4, tblOrder4, tblInvoice4, tblPayment4.
 KỊCH BẢN : module 2, tk-v.3 10–17.
 NGOẠI LỆ : E1, A1, E3.
 =================================================================== */
package dao;

import model.*;
import java.sql.*;
import java.util.Date;
import java.util.ArrayList;

public class CustomerStatisticDAO4 extends DAO4 {
    /** CustomerStatisticDAO4(): dùng kết nối chung của DAO4, không truy vấn bảng. */
    public CustomerStatisticDAO4() { super(); }

    /** validatePeriod(periodStart : Date, periodEnd : Date) : boolean.
     * Từ CustomerStatistic4.validatePeriod(), tk-v.3 10–11, v.2 9–10.
     * Không đọc CSDL, false khi thiếu ngày hoặc ngày đầu sau ngày cuối (E1).
     */
    // [tk-v3-10][tk-v2-9] Lớp CustomerStatisticDAO4 gọi hàm validatePeriod().
    public boolean validatePeriod(Date periodStart, Date periodEnd) {
        // [tk-v3-11/E1] Không lập báo cáo khi thiếu ngày hoặc khoảng ngày đảo ngược.
        boolean valid = periodStart != null && periodEnd != null && !periodStart.after(periodEnd);
        // [tk-v3-11][tk-v2-10] Hàm validatePeriod() trả kết quả hợp lệ cho trang customerStatisticsView4.jsp.
        return valid;
    }

    /** getCustomerStatistics(periodStart : Date, periodEnd : Date) : CustomerStatistic4[].
     * Từ CustomerStatistic4.getCustomerStatistics(), tk-v.3 13–17, v.2 12–13.
     * Đọc tblCustomer4, tblOrder4, tblInvoice4, tblPayment4. Mỗi hóa đơn đã thanh toán
     * tính một lần tại lần thanh toán thành công sớm nhất, kỳ bao gồm cả ngày cuối.
     * Trả mảng giảm dần, mảng rỗng A1, null E3 khi lỗi SQL, không ghi dữ liệu.
     */
    // [tk-v3-13][tk-v2-12] Lớp CustomerStatisticDAO4 gọi hàm getCustomerStatistics().
    public CustomerStatistic4[] getCustomerStatistics(Date periodStart, Date periodEnd) {
        synchronized (DAO4.class) {
            ArrayList<CustomerStatistic4> list = new ArrayList<>();
            String sql = "SELECT COALESCE(c.id,0) id, COALESCE(c.customerCode,'') customerCode, "
                + "COALESCE(c.fullName,'No name') fullName, c.phoneNumber,c.profileStatus, SUM(i.amountDue) totalSpending "
                + "FROM tblOrder4 o JOIN tblInvoice4 i ON i.tblOrder4id=o.id "
                + "JOIN (SELECT tblInvoice4id, MIN(paidAt) paidAt FROM tblPayment4 WHERE result='Thành công' GROUP BY tblInvoice4id) paid ON paid.tblInvoice4id=i.id "
                + "LEFT JOIN tblCustomer4 c ON c.id=o.tblCustomer4id "
                + "WHERE i.paymentStatus='Đã thanh toán' AND paid.paidAt>=? AND paid.paidAt<DATE_ADD(?,INTERVAL 1 DAY) "
                + "GROUP BY c.id,c.customerCode,c.fullName,c.phoneNumber,c.profileStatus ORDER BY totalSpending DESC,id";
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setDate(1,new java.sql.Date(periodStart.getTime()));
                statement.setDate(2,new java.sql.Date(periodEnd.getTime()));
                try (ResultSet rs=statement.executeQuery()) {
                    while (rs.next()) {
                        // [tk-v3-14][tk-v2-12] Hàm getCustomerStatistics() gọi lớp CustomerStatistic4 để đóng gói thông tin.
                        CustomerStatistic4 statistic = new CustomerStatistic4();
                        // [tk-v3-15][tk-v2-12] Lớp CustomerStatistic4 đóng gói thông tin thực thể.
                        statistic.setId(rs.getInt("id")); statistic.setCustomerCode(rs.getString("customerCode"));
                        statistic.setFullName(rs.getString("fullName")); statistic.setPhoneNumber(rs.getString("phoneNumber"));
                        statistic.setProfileStatus(rs.getString("profileStatus"));
                        // Không tải vì báo cáo chỉ dùng thông tin khách, không xem thẻ thành viên.
                        statistic.setMembershipCards(new MembershipCard4[0]);
                        statistic.setPeriodStart(periodStart); statistic.setPeriodEnd(periodEnd);
                        statistic.setTotalSpending(rs.getFloat("totalSpending"));
                        // [tk-v3-16][tk-v2-12] Lớp CustomerStatistic4 trả kết quả cho hàm getCustomerStatistics().
                        list.add(statistic);
                    }
                }
                // [tk-v3-17/A1] Mảng rỗng khi không có hóa đơn hợp lệ trong kỳ.
                // [tk-v3-17][tk-v2-13] Hàm getCustomerStatistics() trả kết quả cho trang customerStatisticsView4.jsp.
                return list.toArray(new CustomerStatistic4[0]);
            } catch (SQLException e) {
                // [tk-v3-17/E3] Tổng hợp thất bại, không trả số liệu dở dang.
                return null;
            }
        }
    }
}
```

**OrderDAO4.java** — [mã nguồn](../code/restaurant/src/main/java/dao/OrderDAO4.java).

```java
/* ===================================================================
 LỚP      : OrderDAO4 (tầng thao tác dữ liệu)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp OrderDAO4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrder4, tblCustomer4, tblInvoice4, tblPayment4.
 KỊCH BẢN : module 2, tk-v.3 22–26, 34–38.
 NGOẠI LỆ : E2a, E2b, lỗi SQL báo IllegalStateException.
 =================================================================== */
package dao;

import model.*;
import java.sql.*;
import java.util.Date;
import java.util.ArrayList;

public class OrderDAO4 extends DAO4 {
    /** OrderDAO4(): dùng kết nối chung, không truy vấn bảng. */
    public OrderDAO4() { super(); }

    /** getOrders(int idCustomer, Date periodStart, Date periodEnd) : Order4[].
     * Từ Order4.getOrders(), tk-v.3 22–26, v.2 18–19.
     * Đọc tblOrder4, tblCustomer4, tblInvoice4, tblPayment4 theo khách/nhóm và kỳ,
     * hóa đơn chỉ tính tại lần thanh toán thành công sớm nhất.
     * Mảng rỗng là E2a, lỗi SQL ném IllegalStateException, không ghi dữ liệu.
     */
    // [tk-v3-22][tk-v2-18] Lớp OrderDAO4 gọi hàm getOrders().
    public Order4[] getOrders(int idCustomer, Date periodStart, Date periodEnd) {
        synchronized (DAO4.class) {
            ArrayList<Order4> orders = new ArrayList<>();
            String sql = "SELECT o.id,o.orderCode,o.createdAt,o.status,c.id customerId,c.customerCode,c.fullName,c.phoneNumber,c.profileStatus, "
                + "i.id invoiceId,i.invoiceCode,i.issuedAt,i.amountDue,i.paymentStatus "
                + "FROM tblOrder4 o JOIN tblInvoice4 i ON i.tblOrder4id=o.id "
                + "JOIN (SELECT tblInvoice4id,MIN(paidAt) paidAt FROM tblPayment4 WHERE result='Thành công' GROUP BY tblInvoice4id) paid ON paid.tblInvoice4id=i.id "
                + "LEFT JOIN tblCustomer4 c ON c.id=o.tblCustomer4id "
                + "WHERE i.paymentStatus='Đã thanh toán' AND paid.paidAt>=? AND paid.paidAt<DATE_ADD(?,INTERVAL 1 DAY) "
                + "AND COALESCE(o.tblCustomer4id,0)=? ORDER BY paid.paidAt,o.id ";
            try (PreparedStatement statement=con.prepareStatement(sql)) {
                statement.setDate(1,new java.sql.Date(periodStart.getTime()));
                statement.setDate(2,new java.sql.Date(periodEnd.getTime()));
                statement.setInt(3,idCustomer);
                try (ResultSet rs=statement.executeQuery()) {
                    while (rs.next()) {
                        // [tk-v3-23][tk-v2-18] Hàm getOrders() gọi lớp Order4 để đóng gói thông tin.
                        Order4 order = new Order4();
                        // [tk-v3-24][tk-v2-18] Lớp Order4 đóng gói thông tin thực thể.
                        fillOrder(order,rs);
                        // Không tải orderSubmissions vì các dòng được lấy riêng ở trang chi tiết.
                        // Không tải salesStaff vì báo cáo không hiển thị nhân viên bán hàng.
                        ArrayList<Payment4> payments = new ArrayList<>();
                        try (PreparedStatement paymentStatement=con.prepareStatement("SELECT id,amount,method,paidAt,result FROM tblPayment4 WHERE tblInvoice4id=? ORDER BY paidAt,id")) {
                            paymentStatement.setInt(1,order.getInvoice().getId());
                            try (ResultSet paymentRows=paymentStatement.executeQuery()) {
                                while (paymentRows.next()) {
                                    Payment4 payment = new Payment4();
                                    payment.setId(paymentRows.getInt("id")); payment.setAmount(paymentRows.getFloat("amount"));
                                    payment.setMethod(paymentRows.getString("method")); payment.setPaidAt(paymentRows.getTimestamp("paidAt"));
                                    payment.setResult(paymentRows.getString("result"));
                                    payment.setSalesStaff(null); // Không tải vì báo cáo không hiển thị nhân viên.
                                    payments.add(payment);
                                }
                            }
                        }
                        order.getInvoice().setPayments(payments.toArray(new Payment4[0]));
                        // [tk-v3-25][tk-v2-18] Lớp Order4 trả kết quả cho hàm getOrders().
                        orders.add(order);
                    }
                }
                // [tk-v3-26/E2a] Dữ liệu đã thay đổi hoặc không phù hợp bộ lọc.
                // [tk-v3-26][tk-v2-19] Hàm getOrders() trả kết quả cho trang customerSpendingView4.jsp.
                return orders.toArray(new Order4[0]);
            } catch (SQLException e) {
                throw new IllegalStateException("Không thể lấy thông tin đơn.",e);
            }
        }
    }

    /** getOrder(int idOrder, int idCustomer, Date periodStart, Date periodEnd) : Order4.
     * Từ Order4.getOrder(), tk-v.3 34–38, v.2 24–25.
     * Đọc tblOrder4, tblCustomer4, tblInvoice4, tblPayment4 theo khách/nhóm và kỳ,
     * hóa đơn chỉ tính tại lần thanh toán thành công sớm nhất.
     * null là E2b, lỗi SQL ném IllegalStateException, không ghi dữ liệu.
     */
    // [tk-v3-34][tk-v2-24] Lớp OrderDAO4 gọi hàm getOrder().
    public Order4 getOrder(int idOrder, int idCustomer, Date periodStart, Date periodEnd) {
        synchronized (DAO4.class) {
            ArrayList<Order4> orders = new ArrayList<>();
            String sql = "SELECT o.id,o.orderCode,o.createdAt,o.status,c.id customerId,c.customerCode,c.fullName,c.phoneNumber,c.profileStatus, "
                + "i.id invoiceId,i.invoiceCode,i.issuedAt,i.amountDue,i.paymentStatus "
                + "FROM tblOrder4 o JOIN tblInvoice4 i ON i.tblOrder4id=o.id "
                + "JOIN (SELECT tblInvoice4id,MIN(paidAt) paidAt FROM tblPayment4 WHERE result='Thành công' GROUP BY tblInvoice4id) paid ON paid.tblInvoice4id=i.id "
                + "LEFT JOIN tblCustomer4 c ON c.id=o.tblCustomer4id "
                + "WHERE i.paymentStatus='Đã thanh toán' AND paid.paidAt>=? AND paid.paidAt<DATE_ADD(?,INTERVAL 1 DAY) "
                + "AND COALESCE(o.tblCustomer4id,0)=? AND o.id=? ";
            try (PreparedStatement statement=con.prepareStatement(sql)) {
                statement.setDate(1,new java.sql.Date(periodStart.getTime()));
                statement.setDate(2,new java.sql.Date(periodEnd.getTime()));
                statement.setInt(3,idCustomer);
                statement.setInt(4,idOrder);
                try (ResultSet rs=statement.executeQuery()) {
                    while (rs.next()) {
                        // [tk-v3-35][tk-v2-24] Hàm getOrder() gọi lớp Order4 để đóng gói thông tin.
                        Order4 order = new Order4();
                        // [tk-v3-36][tk-v2-24] Lớp Order4 đóng gói thông tin thực thể.
                        fillOrder(order,rs);
                        // Không tải orderSubmissions vì các dòng được lấy riêng ở trang chi tiết.
                        // Không tải salesStaff vì báo cáo không hiển thị nhân viên bán hàng.
                        ArrayList<Payment4> payments = new ArrayList<>();
                        try (PreparedStatement paymentStatement=con.prepareStatement("SELECT id,amount,method,paidAt,result FROM tblPayment4 WHERE tblInvoice4id=? ORDER BY paidAt,id")) {
                            paymentStatement.setInt(1,order.getInvoice().getId());
                            try (ResultSet paymentRows=paymentStatement.executeQuery()) {
                                while (paymentRows.next()) {
                                    Payment4 payment = new Payment4();
                                    payment.setId(paymentRows.getInt("id")); payment.setAmount(paymentRows.getFloat("amount"));
                                    payment.setMethod(paymentRows.getString("method")); payment.setPaidAt(paymentRows.getTimestamp("paidAt"));
                                    payment.setResult(paymentRows.getString("result"));
                                    payment.setSalesStaff(null); // Không tải vì báo cáo không hiển thị nhân viên.
                                    payments.add(payment);
                                }
                            }
                        }
                        order.getInvoice().setPayments(payments.toArray(new Payment4[0]));
                        // [tk-v3-37][tk-v2-24] Lớp Order4 trả kết quả cho hàm getOrder().
                        orders.add(order);
                    }
                }
                // [tk-v3-38/E2b] Dữ liệu đã thay đổi hoặc không phù hợp bộ lọc.
                // [tk-v3-38][tk-v2-25] Hàm getOrder() trả kết quả cho trang orderDetailView4.jsp.
                return orders.isEmpty() ? null : orders.get(0);
            } catch (SQLException e) {
                throw new IllegalStateException("Không thể lấy thông tin đơn.",e);
            }
        }
    }

    /** [NGOÀI THIẾT KẾ] Dùng chung ánh xạ một dòng JDBC, không truy vấn hay thêm nghiệp vụ. */
    private void fillOrder(Order4 order, ResultSet rs) throws SQLException {
        order.setId(rs.getInt("id")); order.setOrderCode(rs.getString("orderCode"));
        order.setCreatedAt(rs.getTimestamp("createdAt")); order.setStatus(rs.getString("status"));
        order.setOrderSubmissions(new OrderSubmission4[0]); // Không tải vì DAO4 chi tiết lấy các dòng riêng.
        Customer4 customer=null;
        if (rs.getObject("customerId")!=null) {
            customer=new Customer4(); customer.setId(rs.getInt("customerId"));
            customer.setCustomerCode(rs.getString("customerCode")); customer.setFullName(rs.getString("fullName"));
            customer.setPhoneNumber(rs.getString("phoneNumber")); customer.setProfileStatus(rs.getString("profileStatus"));
            customer.setMembershipCards(new MembershipCard4[0]); // Không tải vì báo cáo không xem thẻ.
        }
        order.setCustomer(customer);
        Invoice4 invoice=new Invoice4(); invoice.setId(rs.getInt("invoiceId"));
        invoice.setInvoiceCode(rs.getString("invoiceCode")); invoice.setIssuedAt(rs.getTimestamp("issuedAt"));
        invoice.setAmountDue(rs.getFloat("amountDue")); invoice.setPaymentStatus(rs.getString("paymentStatus"));
        invoice.setSalesStaff(null); // Không tải vì báo cáo không hiển thị nhân viên.
        order.setInvoice(invoice);
    }
}
```

**OrderSubmissionProductDAO4.java** — [mã nguồn](../code/restaurant/src/main/java/dao/OrderSubmissionProductDAO4.java).

```java
/* ===================================================================
 LỚP      : OrderSubmissionProductDAO4 (tầng thao tác dữ liệu)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp OrderSubmissionProductDAO4 (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrderSubmissionProduct4, tblOrderSubmission4, tblOrder4, tblInvoice4, tblPayment4, tblProduct4, tblDish4, tblCombo4.
 KỊCH BẢN : module 2, tk-v.3 43–47.
 NGOẠI LỆ : E2b, lỗi SQL báo IllegalStateException.
 =================================================================== */
package dao;

import model.*;
import java.sql.*;
import java.util.Date;
import java.util.ArrayList;

public class OrderSubmissionProductDAO4 extends DAO4 {
    /** OrderSubmissionProductDAO4(): dùng kết nối chung, không truy vấn bảng. */
    public OrderSubmissionProductDAO4() { super(); }

    /** getOrderSubmissionProducts(idOrder : int, idCustomer : int, periodStart : Date, periodEnd : Date) : OrderSubmissionProduct4[].
     * Từ OrderSubmissionProduct4.getOrderSubmissionProducts(), tk-v.3 43–47, v.2 27–28.
     * Đọc các bảng trong header, kiểm tra lại khách/nhóm và kỳ cùng hóa đơn hợp lệ.
     * Chỉ lấy dòng Đã chấp nhận, đóng gói Product4 thành Dish4 hoặc Combo4.
     * Mảng rỗng E2b khi đơn không còn hợp lệ, lỗi SQL ném IllegalStateException.
     */
    // [tk-v3-43][tk-v2-27] Lớp OrderSubmissionProductDAO4 gọi hàm getOrderSubmissionProducts().
    public OrderSubmissionProduct4[] getOrderSubmissionProducts(int idOrder, int idCustomer, Date periodStart, Date periodEnd) {
        synchronized (DAO4.class) {
            ArrayList<OrderSubmissionProduct4> lines=new ArrayList<>();
            String sql="SELECT line.*,p.*,line.id lineId,d.tblProduct4id dishId,c.tblProduct4id comboId,c.startsAt,c.endsAt "
                + "FROM tblOrderSubmissionProduct4 line JOIN tblOrderSubmission4 os ON os.id=line.tblOrderSubmission4id "
                + "JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblInvoice4 i ON i.tblOrder4id=o.id "
                + "JOIN (SELECT tblInvoice4id,MIN(paidAt) paidAt FROM tblPayment4 WHERE result='Thành công' GROUP BY tblInvoice4id) paid ON paid.tblInvoice4id=i.id "
                + "JOIN tblProduct4 p ON p.id=line.tblProduct4id LEFT JOIN tblDish4 d ON d.tblProduct4id=p.id LEFT JOIN tblCombo4 c ON c.tblProduct4id=p.id "
                + "WHERE o.id=? AND COALESCE(o.tblCustomer4id,0)=? AND i.paymentStatus='Đã thanh toán' "
                + "AND paid.paidAt>=? AND paid.paidAt<DATE_ADD(?,INTERVAL 1 DAY) AND line.processingStatus='Đã chấp nhận' ORDER BY os.id,line.id";
            try (PreparedStatement statement=con.prepareStatement(sql)) {
                statement.setInt(1,idOrder); statement.setInt(2,idCustomer);
                statement.setDate(3,new java.sql.Date(periodStart.getTime())); statement.setDate(4,new java.sql.Date(periodEnd.getTime()));
                try (ResultSet rs=statement.executeQuery()) {
                    while(rs.next()) {
                        // [tk-v3-44][tk-v2-27] Hàm getOrderSubmissionProducts() gọi lớp OrderSubmissionProduct4 để đóng gói thông tin.
                        OrderSubmissionProduct4 line=new OrderSubmissionProduct4();
                        // [tk-v3-45][tk-v2-27] Lớp OrderSubmissionProduct4 đóng gói thông tin thực thể.
                        line.setId(rs.getInt("lineId")); line.setQuantity(rs.getInt("quantity"));
                        line.setRecordedUnitPrice(rs.getFloat("recordedUnitPrice")); line.setNote(rs.getString("note"));
                        line.setProcessingStatus(rs.getString("processingStatus")); line.setProcessedAt(rs.getTimestamp("processedAt"));
                        line.setRejectionReason(rs.getString("rejectionReason"));
                        line.setSalesStaff(null); // Không tải vì chi tiết không hiển thị nhân viên.
                        Product4 product;
                        if(rs.getObject("dishId")!=null) product=new Dish4();
                        else if(rs.getObject("comboId")!=null) {
                            Combo4 combo=new Combo4(); combo.setStartsAt(rs.getTimestamp("startsAt")); combo.setEndsAt(rs.getTimestamp("endsAt"));
                            combo.setComboDishes(new ComboDish4[0]); // Không tải vì báo cáo chỉ xem combo đã gọi, không xem thành phần.
                            product=combo;
                        } else throw new SQLException("Mặt hàng không có lớp con Dish4 hoặc Combo4.");
                        product.setId(rs.getInt("tblProduct4id")); product.setProductCode(rs.getString("productCode"));
                        product.setName(rs.getString("name")); product.setDescription(rs.getString("description"));
                        product.setSalePrice(rs.getFloat("salePrice")); product.setImage(rs.getString("image"));
                        product.setSaleStatus(rs.getString("saleStatus")); product.setCreatedAt(rs.getTimestamp("createdAt"));
                        product.setUpdatedAt(rs.getTimestamp("updatedAt")); line.setProduct(product);
                        // [tk-v3-46][tk-v2-27] Lớp OrderSubmissionProduct4 trả kết quả cho hàm getOrderSubmissionProducts().
                        lines.add(line);
                    }
                }
                // [tk-v3-47/E2b] Đơn không còn hợp lệ hoặc không còn dòng tính tiền.
                // [tk-v3-47][tk-v2-28] Hàm getOrderSubmissionProducts() trả kết quả cho trang orderDetailView4.jsp.
                return lines.toArray(new OrderSubmissionProduct4[0]);
            } catch (SQLException e) { throw new IllegalStateException("Không thể lấy chi tiết đơn.",e); }
        }
    }
}
```

`CustomerStatisticDAO4()` dùng kết nối DAO4 chung. `validatePeriod(periodStart, periodEnd)` không đọc CSDL; trả false khi thiếu ngày hoặc ngày bắt đầu sau ngày kết thúc. Trang không gọi lập thống kê khi kết quả không hợp lệ.

`getCustomerStatistics(periodStart, periodEnd)` lấy MIN(paidAt) của các Payment4 thành công cho từng hóa đơn trước khi lọc kỳ. Chỉ hóa đơn paymentStatus Đã thanh toán có thời điểm này trong [ngày đầu 00:00, ngày cuối + 1 ngày) được cộng amountDue một lần. Nhóm theo hồ sơ khách hoặc nhóm NULL, sắp tổng giảm dần. Nhóm NULL có id 0, mã rỗng, tên No name. Không có hóa đơn thì mảng rỗng (A1), lỗi SQL trả null (E3), không trả kết quả dở dang.

`OrderDAO4()` dùng DAO4 chung. `getOrders(idCustomer, periodStart, periodEnd)` lấy đơn của đúng khách/nhóm theo cùng điều kiện hóa đơn và kỳ, sắp tăng theo lần thanh toán thành công sớm nhất. Mỗi Order4 được điền Customer4 (hoặc null), Invoice4 và mọi Payment4 của hóa đơn theo thời điểm. Mảng rỗng là E2a. `getOrder(idOrder, idCustomer, periodStart, periodEnd)` thêm điều kiện id đơn vào cùng bộ lọc; không còn đơn phù hợp thì trả null (E2b). Lỗi đọc SQL của hai phương thức ném IllegalStateException. Hàm private fillOrder chỉ dùng chung phép ánh xạ cột, không truy vấn hay thêm phương thức nghiệp vụ.

`OrderSubmissionProductDAO4()` dùng DAO4 chung. `getOrderSubmissionProducts(idOrder, idCustomer, periodStart, periodEnd)` kiểm tra lại đơn, khách/nhóm, hóa đơn và kỳ ngay trong truy vấn, rồi lấy chỉ dòng Đã chấp nhận. Đơn không còn hợp lệ hoặc không có dòng tính tiền thì trả mảng rỗng (E2b); lỗi SQL ném IllegalStateException. Mỗi dòng giữ quantity và recordedUnitPrice lúc gọi, không thay bằng giá bán hiện tại. Product4 trừu tượng được tạo thành Dish4 hoặc Combo4, giữ đủ trường kế thừa và thời gian hiệu lực của Combo4, không tải thành phần combo.

Mọi thao tác JDBC ở ba DAO4 nằm trong synchronized(DAO4.class), dùng PreparedStatement và try-with-resources. Hai module dùng chung một kết nối theo thiết kế, nên đọc và ghi được thực hiện tuần tự trong ứng dụng.

### d Các lớp giao diện JSP

**statisticsReportView4.jsp** — [mã nguồn](../code/restaurant/src/main/webapp/quanly/statisticsReportView4.jsp).

```jsp
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
```

**customerStatisticsView4.jsp** — [mã nguồn](../code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp).

```jsp
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
```

**customerSpendingView4.jsp** — [mã nguồn](../code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp).

```jsp
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
```

**orderDetailView4.jsp** — [mã nguồn](../code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp).

```jsp
<%-- ===================================================================
 LỚP      : orderDetailView4.jsp (tầng giao diện)
 THIẾT KẾ : class-design-customer-statistics.puml – hộp orderDetailView4.jsp (Hình 6, desing.md §4b).
 PHÂN TÍCH: class-customer-statistics.puml; kiểu và thuộc tính theo Hình 1.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : tblOrder4, tblCustomer4, tblInvoice4, tblPayment4 và các dòng/mặt hàng qua DAO4.
 KỊCH BẢN : module 2, tk-v.3 33–34, 38–43, 47–48; v.2 23–29.
 NGOẠI LỆ : E2b.
 =================================================================== --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="model.*,dao.*,java.util.Date" %>
<%@ include file="/WEB-INF/common.jspf" %>
<%@ include file="/WEB-INF/auth.jspf" %>
<%@ include file="/WEB-INF/statistics.jspf" %>
<%
int idOrder=id(p(request,"idOrder")),idCustomer=id(p(request,"idCustomer"));
Date periodStart=day(p(request,"periodStart")),periodEnd=day(p(request,"periodEnd"));
Order4 order=null;
CustomerStatistic4 customerStatistic=null;
OrderSubmissionProduct4[] orderSubmissionProducts=new OrderSubmissionProduct4[0];
String outMessage="";
try {
    if(idOrder>0 && idCustomer>=0 && periodStart!=null && periodEnd!=null && !periodStart.after(periodEnd)) {
        // [tk-v3-33][tk-v2-23] Trang orderDetailView4.jsp gọi lớp OrderDAO4 yêu cầu lấy thông tin đơn đã chọn.
        OrderDAO4 orderDAO=new OrderDAO4();
        // [tk-v3-34][tk-v2-24] Lớp OrderDAO4 gọi hàm getOrder().
        order=orderDAO.getOrder(idOrder,idCustomer,periodStart,periodEnd);
    }
    // [tk-v3-38][tk-v2-25] Hàm getOrder() trả kết quả cho trang orderDetailView4.jsp.
    if(order==null) {
        // [tk-v3-38/E2b] Không hiển thị chi tiết cũ, quay về danh sách đơn cùng ngữ cảnh.
        outMessage="Data has changed: the selected order is no longer valid.";
    } else {
        // [tk-v3-39][tk-v2-25] Trang orderDetailView4.jsp gọi lớp CustomerStatistic4 yêu cầu đóng gói thông tin khách hàng và kỳ.
        customerStatistic=new CustomerStatistic4();
        // [tk-v3-40][tk-v2-25] Lớp CustomerStatistic4 gọi các hàm set thuộc tính tương ứng.
        customerStatistic.setId(idCustomer);
        Customer4 customer=order.getCustomer();
        customerStatistic.setCustomerCode(customer==null ? "" : customer.getCustomerCode());
        customerStatistic.setFullName(customer==null ? "No name" : customer.getFullName());
        customerStatistic.setPhoneNumber(customer==null ? null : customer.getPhoneNumber());
        customerStatistic.setProfileStatus(customer==null ? null : customer.getProfileStatus());
        customerStatistic.setMembershipCards(new MembershipCard4[0]); // Không tải vì trang không xem thẻ.
        customerStatistic.setPeriodStart(periodStart); customerStatistic.setPeriodEnd(periodEnd);
        customerStatistic.setTotalSpending(0); // Trang không hiển thị tổng chi tiêu cả kỳ.
        // [tk-v3-41][tk-v2-25] Lớp CustomerStatistic4 trả đối tượng về cho trang orderDetailView4.jsp.
        request.setAttribute("customerStatistic",customerStatistic);
        // [tk-v3-42][tk-v2-26] Trang orderDetailView4.jsp gọi lớp OrderSubmissionProductDAO4 yêu cầu lấy các dòng tính tiền của đơn.
        OrderSubmissionProductDAO4 orderSubmissionProductDAO=new OrderSubmissionProductDAO4();
        // [tk-v3-43][tk-v2-27] Lớp OrderSubmissionProductDAO4 gọi hàm getOrderSubmissionProducts().
        orderSubmissionProducts=orderSubmissionProductDAO.getOrderSubmissionProducts(idOrder,idCustomer,periodStart,periodEnd);
        // [tk-v3-47][tk-v2-28] Hàm getOrderSubmissionProducts() trả kết quả cho trang orderDetailView4.jsp.
        if(orderSubmissionProducts.length==0) {
            // [tk-v3-47/E2b] Kiểm tra lại sau khi lấy thông tin đơn, không hiển thị dữ liệu dở dang.
            order=null;customerStatistic=null;
            outMessage="Data has changed: the selected order is no longer valid.";
        }
    }
} catch(IllegalStateException e) { order=null;customerStatistic=null;orderSubmissionProducts=new OrderSubmissionProduct4[0];outMessage="The report could not be generated."; }
// Các thuộc tính ẩn đúng Hình 6, không giữ bản sao trong session.
request.setAttribute("order",order);request.setAttribute("customerStatistic",customerStatistic);
request.setAttribute("orderSubmissionProducts",orderSubmissionProducts);
%>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Order details</title>
<% if(order==null) { %><meta http-equiv="refresh" content="2;url=customerSpendingView4.jsp?idCustomer=<%= idCustomer %>&amp;periodStart=<%= q(p(request,"periodStart")) %>&amp;periodEnd=<%= q(p(request,"periodEnd")) %>"><% } %>
</head><body><h1>Order details</h1>
<%-- outMessage : Label --%><p id="outMessage" name="outMessage" role="status"><%= h(outMessage) %></p>
<% if(order!=null) { %>
<%-- [tk-v3-48][tk-v2-29] Trang orderDetailView4.jsp hiển thị chi tiết đơn cho nhân viên quản lý. Use case kết thúc. --%>
<%-- outOrderCode, outCustomerInfo, outPeriod, outInvoiceCode, outPaidAt, outTotalAmount : Label --%>
<p>Order code: <span id="outOrderCode" name="outOrderCode"><%= h(order.getOrderCode()) %></span></p>
<p>Customer: <span id="outCustomerInfo" name="outCustomerInfo"><%= h(customerStatistic.getCustomerCode()) %> <%= h(customerStatistic.getFullName()) %></span></p>
<p>Period: <span id="outPeriod" name="outPeriod"><%= date(periodStart) %> – <%= date(periodEnd) %></span></p>
<p>Invoice code: <span id="outInvoiceCode" name="outInvoiceCode"><%= h(order.getInvoice().getInvoiceCode()) %></span></p>
<p>Paid at: <span id="outPaidAt" name="outPaidAt"><%= time(paidAt(order.getInvoice())) %></span></p>
<%-- outOrderLines : Table --%>
<table id="outOrderLines" name="outOrderLines"><thead><tr><th>No.</th><th>Product code</th><th>Dish or combo</th><th>Quantity</th><th>Unit price (VND)</th><th>Line total (VND)</th></tr></thead><tbody>
<% int row=0;for(OrderSubmissionProduct4 line:orderSubmissionProducts) { %>
<tr><td><%= ++row %></td><td><%= h(line.getProduct().getProductCode()) %></td><td><%= h(line.getProduct().getName()) %></td><td><%= line.getQuantity() %></td><td><%= money(line.getRecordedUnitPrice()) %></td><td><%= money(line.getQuantity()*line.getRecordedUnitPrice()) %></td></tr>
<% } %></tbody></table>
<p>Total amount: <span id="outTotalAmount" name="outTotalAmount"><%= money(order.getInvoice().getAmountDue()) %></span></p><% } %>
<%-- subBackToOrderList : submit, giữ khách/nhóm và kỳ bằng tham số DAO4. --%>
<form action="customerSpendingView4.jsp"><input type="hidden" name="idCustomer" value="<%= idCustomer %>"><input type="hidden" name="periodStart" value="<%= h(p(request,"periodStart")) %>"><input type="hidden" name="periodEnd" value="<%= h(p(request,"periodEnd")) %>"><input type="submit" id="subBackToOrderList" name="subBackToOrderList" value="Back to order list"></form>
</body></html>
```

managerMainView4.jsp bật subStatisticsReport. statisticsReportView4.jsp liệt kê bốn loại báo cáo, chỉ dòng khách hàng có liên kết. customerStatisticsView4.jsp tự gửi GET bằng inStartDate/inEndDate; mở lần đầu chỉ hiện form, gửi ngày thì kiểm tra trước khi gọi thống kê. E1/A1/E3 hiện tại outMessage và giữ hai ngày, kết quả lỗi không có dòng để chọn.

customerSpendingView4.jsp nhận idCustomer/periodStart/periodEnd, lấy orders rồi tạo customerStatistic từ orders[0].customer hoặc No name, gán kỳ và tổng invoice.amountDue. Khi E2a, không hiển thị chi tiết cũ và tự về thống kê sau hai giây với kỳ hiện tại.

orderDetailView4.jsp nhận thêm idOrder, lấy Order4 đúng ngữ cảnh rồi lấy các dòng tính tiền. Trang tạo customerStatistic từ order.customer hoặc No name và kỳ; totalSpending để 0 vì trang không hiện tổng cả kỳ. Nếu E2b ở lần lấy đơn hoặc lần kiểm tra lại các dòng, trang bỏ chi tiết và tự về danh sách đơn sau hai giây. Các nút quay lại giữ đúng ngữ cảnh bằng tham số DAO4 hoặc tên ô ngày. Tất cả trang quản lý dùng auth.jspf để kiểm tra session managementStaff trước khi đọc dữ liệu. Văn bản và thuộc tính HTML được mã hóa; ngày dd/MM/yyyy, thời điểm dd/MM/yyyy HH:mm, tiền vi-VN.

### e Kiểm thử module

[StatisticsDAOTest4](../code/restaurant/src/test/java/dao/StatisticsDAOTest4.java) chạy trên MySQL restaurant_test và nạp lại dữ liệu trước mỗi ca. URL ngoài database kiểm thử bị chặn trước DDL; truy vấn SELECT DATABASE() xác nhận lại database được chọn.

| Ca kiểm thử | Kết quả mong đợi |
| --- | --- |
| Khoảng ngày hợp lệ, cùng ngày, đảo ngược, thiếu ngày | true cho hai trường hợp đầu; false cho đảo ngược và thiếu ngày (E1) |
| Kỳ 08/2026 | Ba dòng, tổng và thứ tự đúng dữ liệu Hình 4, KH003 vắng |
| Kỳ không có hóa đơn | Mảng rỗng, không tự tạo No name (A1) |
| Lỗi SQL tổng hợp | null, không hiển thị số liệu dở dang (E3) |
| Biên đầu và cuối kỳ, trước và sau kỳ | Bao gồm DH301/DH202, loại DH106/DH203 |
| Hóa đơn nhiều lần thanh toán | Tính một lần theo thành công sớm nhất, không theo thất bại hay lần thành công sau |
| Danh sách đơn KH001 | Đúng DH101–DH105, có khách, hóa đơn và tất cả Payment4 |
| No name và cách ly khách/nhóm | Hai đơn nhóm NULL, idCustomer 0; không trả đơn của khách khác (A2/E2b) |
| Khách không có đơn phù hợp | getOrders trả mảng rỗng (E2a) |
| Hóa đơn đổi trạng thái giữa hai lần xem | getOrder null, lấy dòng trả rỗng (E2b) |
| Hóa đơn đánh dấu đã trả nhưng không có Payment4 thành công | Không đưa vào thống kê hay chi tiết |
| Chi tiết DH103 | Ba dòng chấp nhận, Dish4/Combo4 đúng kiểu, tổng 1.100.000 |
| Đơn giá ghi nhận khác giá hiện tại | DH102 dùng 620.000 theo dòng, không dùng 650.000 hiện tại |
| Dòng chờ, hủy, từ chối | Không nằm trong dòng tính tiền |
| Đơn chuyển khách/nhóm hoặc id không tồn tại | Không trả chi tiết ngoài ngữ cảnh (E2b) |
| Lỗi SQL đọc đơn/dòng | Ném lỗi, không giả danh sách rỗng |

Đã chạy Maven verify: 34 ca JUnit, không lỗi, gồm 14 ca thống kê và 20 ca của module thêm món/đăng nhập. Kiểm tra HTTP trên Tomcat 10.1 với CSDL kiểm thử bao phủ bảo vệ đăng nhập cả bốn trang, điều hướng, biểu mẫu lần đầu, E1/A1/E3, thứ tự và tổng thống kê, KH001/DH103, Combo4, No name, khách/đơn thay đổi sau khi xem, E2b khi không còn dòng tính tiền, ngày và tham số của các nút quay lại. Các thay đổi gây lỗi trên CSDL kiểm thử đã được phục hồi.

### f Truy vết thiết kế đến mã

Module dùng tag tk-v3/tk-v2 để không trùng module thêm món. Mỗi tag chứa câu kịch bản tương ứng; bảng dưới xuất từ công cụ và có số dòng hiện tại.

```sh
python3 code/restaurant/tools/trace.py --module statistics
python3 -m unittest discover -s code/restaurant/tools -p 'test_*.py'
```

Công cụ kiểm tra đủ 48 bước và bảy điểm rẽ, đối chiếu hai chiều thông điệp tuần tự khi có, kiểm tra năm vùng, 15 hoạt động, năm nút DAO4 và 19 chuyển tiếp của biểu đồ hoạt động. Mặc định không chọn module thì kiểm tra cả hai.

**Bảng truy vết kịch bản tk-v.3 → mã**

| Bước / nhánh | Vị trí mã (file:dòng) |
| --- | --- |
| 1 | `code/restaurant/src/main/webapp/quanly/managerMainView4.jsp:22` |
| 2 | `code/restaurant/src/main/webapp/quanly/managerMainView4.jsp:23` |
| 3 | `code/restaurant/src/main/webapp/quanly/statisticsReportView4.jsp:15` |
| 4 | `code/restaurant/src/main/webapp/quanly/statisticsReportView4.jsp:20` |
| 5 | `code/restaurant/src/main/webapp/quanly/statisticsReportView4.jsp:21` |
| 6 | `code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:48` |
| 7 | `code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:54` |
| 8 | `code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:49` |
| 9 | `code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:21` |
| 10 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:25`<br>`code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:23` |
| 11 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:29`<br>`code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:25` |
| 12 | `code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:30` |
| 13 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:39`<br>`code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:31` |
| 14 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:55` |
| 15 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:57` |
| 16 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:65` |
| 17 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:70` |
| 18 | `code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:59` |
| 19 | `code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:63` |
| 20 | `code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:64` |
| 21 | `code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:22` |
| 22 | `code/restaurant/src/main/java/dao/OrderDAO4.java:27`<br>`code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:24` |
| 23 | `code/restaurant/src/main/java/dao/OrderDAO4.java:44` |
| 24 | `code/restaurant/src/main/java/dao/OrderDAO4.java:46` |
| 25 | `code/restaurant/src/main/java/dao/OrderDAO4.java:65` |
| 26 | `code/restaurant/src/main/java/dao/OrderDAO4.java:70`<br>`code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:27` |
| 27 | `code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:32` |
| 28 | `code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:34` |
| 29 | `code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:45` |
| 30 | `code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:57` |
| 31 | `code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:66` |
| 32 | `code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:67` |
| 33 | `code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:23` |
| 34 | `code/restaurant/src/main/java/dao/OrderDAO4.java:84`<br>`code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:25` |
| 35 | `code/restaurant/src/main/java/dao/OrderDAO4.java:102` |
| 36 | `code/restaurant/src/main/java/dao/OrderDAO4.java:104` |
| 37 | `code/restaurant/src/main/java/dao/OrderDAO4.java:123` |
| 38 | `code/restaurant/src/main/java/dao/OrderDAO4.java:128`<br>`code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:28` |
| 39 | `code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:33` |
| 40 | `code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:35` |
| 41 | `code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:45` |
| 42 | `code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:47` |
| 43 | `code/restaurant/src/main/java/dao/OrderSubmissionProductDAO4.java:27`<br>`code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:49` |
| 44 | `code/restaurant/src/main/java/dao/OrderSubmissionProductDAO4.java:43` |
| 45 | `code/restaurant/src/main/java/dao/OrderSubmissionProductDAO4.java:45` |
| 46 | `code/restaurant/src/main/java/dao/OrderSubmissionProductDAO4.java:63` |
| 47 | `code/restaurant/src/main/java/dao/OrderSubmissionProductDAO4.java:68`<br>`code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:51` |
| 48 | `code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:68` |
| A1 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:69`<br>`code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:35` |
| A2 | `code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:65` |
| E1 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:27`<br>`code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:27` |
| E2a | `code/restaurant/src/main/java/dao/OrderDAO4.java:69`<br>`code/restaurant/src/main/webapp/quanly/customerSpendingView4.jsp:29` |
| E2b | `code/restaurant/src/main/java/dao/OrderDAO4.java:127`<br>`code/restaurant/src/main/java/dao/OrderSubmissionProductDAO4.java:67`<br>`code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:30`<br>`code/restaurant/src/main/webapp/quanly/orderDetailView4.jsp:53` |
| E3 | `code/restaurant/src/main/java/dao/CustomerStatisticDAO4.java:73`<br>`code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:33`<br>`code/restaurant/src/main/webapp/quanly/customerStatisticsView4.jsp:39` |

**Đối chiếu biểu đồ hoạt động – mã (Hình 9)**

| Vùng | Hoạt động | Phương thức (Hình 6) | Mã cài đặt |
| --- | --- | --- | --- |
| managerMainView4.jsp | Nhận thông tin QL | — | `WEB-INF/auth.jspf` (managementStaff) |
| managerMainView4.jsp | Hiện GD | — | `managerMainView4.jsp` (hiển thị) |
| statisticsReportView4.jsp | Nhận thông tin QL | — | `WEB-INF/auth.jspf` (managementStaff) |
| statisticsReportView4.jsp | Hiện GD | — | `statisticsReportView4.jsp` (hiển thị) |
| customerStatisticsView4.jsp | Nhận thông tin QL | — | `WEB-INF/auth.jspf` (managementStaff) |
| customerStatisticsView4.jsp | Kiểm tra khoảng ngày | `CustomerStatisticDAO4.validatePeriod()` | `CustomerStatisticDAO4.validatePeriod()` |
| customerStatisticsView4.jsp | Lấy thống kê khách hàng | `CustomerStatisticDAO4.getCustomerStatistics()` | `CustomerStatisticDAO4.getCustomerStatistics()` |
| customerStatisticsView4.jsp | Hiện GD | — | `customerStatisticsView4.jsp` (hiển thị) |
| customerSpendingView4.jsp | Nhận thông tin QL | — | `WEB-INF/auth.jspf` (managementStaff) |
| customerSpendingView4.jsp | Lấy danh sách đơn | `OrderDAO4.getOrders()` | `OrderDAO4.getOrders()` |
| customerSpendingView4.jsp | Hiện GD | — | `customerSpendingView4.jsp` (hiển thị) |
| orderDetailView4.jsp | Nhận thông tin QL | — | `WEB-INF/auth.jspf` (managementStaff) |
| orderDetailView4.jsp | Lấy thông tin đơn | `OrderDAO4.getOrder()` | `OrderDAO4.getOrder()` |
| orderDetailView4.jsp | Lấy chi tiết đơn | `OrderSubmissionProductDAO4.getOrderSubmissionProducts()` | `OrderSubmissionProductDAO4.getOrderSubmissionProducts()` |
| orderDetailView4.jsp | Hiện GD | — | `orderDetailView4.jsp` (hiển thị) |

Mỗi thông điệp của Hình 10 mang số câu v.3; mã cài đặt đánh dấu cùng số bằng tag `[tk-v3-NN]`. Các bước đóng gói thuộc tính con được lược bỏ trên hình; mã vẫn điền các đối tượng cần hiển thị.
