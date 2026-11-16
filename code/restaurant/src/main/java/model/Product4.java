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
