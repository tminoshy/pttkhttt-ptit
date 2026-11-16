#!/usr/bin/env python3
"""Rule 13 (rules/13-hinh-tieng-anh.md): English names on pictures.

Step `attrs` applies EN-01 (35 UI attribute names) to every project file outside rules/, build/, target/.
Prints each file with its replacement count. Idempotent: a second run changes nothing.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

EN01 = {
    "subQuanLyMonAn": "subManageDish", "subBaoCaoThongKe": "subStatisticsReport",
    "outDanhSachMonAn": "outDishList", "subThemMonAn": "subAddDish",
    "subQuayLaiGiaoDienChinh": "subBackToMain", "inMaMon": "inDishCode",
    "inTenMon": "inDishName", "inMoTa": "inDescription", "inGiaBan": "inSalePrice",
    "inTrangThaiKinhDoanh": "inSaleStatus", "inoutLoaiMonAn": "inoutDishCategory",
    "inAnhMinhHoa": "inImage", "subLuu": "subSave", "outThongBao": "outMessage",
    "subQuayLaiDanhSachMon": "subBackToDishList", "outsubLoaiThongKe": "outsubStatisticTypes",
    "inNgayBatDau": "inStartDate", "inNgayKetThuc": "inEndDate",
    "subXemThongKe": "subViewStatistics", "outsubDanhSachThongKe": "outsubStatisticList",
    "subQuayLaiBaoCaoThongKe": "subBackToStatisticsReport", "outMaKhachHang": "outCustomerCode",
    "outTenHienThi": "outDisplayName", "outKyThongKe": "outPeriod",
    "outTongChiTieu": "outTotalSpending", "outsubDanhSachDon": "outsubOrderList",
    "subQuayLaiDanhSachThongKe": "subBackToStatisticList", "outMaDon": "outOrderCode",
    "outThongTinKhachHang": "outCustomerInfo", "outMaHoaDon": "outInvoiceCode",
    "outThoiDiemThanhToan": "outPaidAt", "outChiTietDon": "outOrderLines",
    "outTongTien": "outTotalAmount", "subQuayLaiDanhSachDon": "subBackToOrderList",
}

SKIP_DIRS = {"rules", "build", "target", ".git"}
SUFFIXES = {".md", ".puml", ".py", ".java", ".jsp", ".jspf", ".sql", ".properties", ".xml", ".sh"}


def files():
    for f in sorted(ROOT.rglob("*")):
        if not f.is_file() or f.suffix not in SUFFIXES:
            continue
        rel = f.relative_to(ROOT)
        if any(part in SKIP_DIRS for part in rel.parts) or rel.name == "english5.py":
            continue
        yield f


def apply(table):
    keys = sorted(table, key=len, reverse=True)
    pattern = re.compile(r"(?<![A-Za-z0-9_])(" + "|".join(map(re.escape, keys)) + r")(?![A-Za-z0-9_])")
    total = 0
    for f in files():
        text = f.read_text(encoding="utf-8")
        new, n = pattern.subn(lambda m: table[m.group(1)], text)
        if n:
            f.write_text(new, encoding="utf-8")
            total += n
            print(f"{n:5d}  {f.relative_to(ROOT)}")
    print(f"total {total}")


# EN-02..EN-05: text on diagrams (non-comment lines of diagrams/*.puml). Longest phrases first.
DIAGRAM_PHRASES = {
    # actors, system boundary, association roles (EN-02, EN-05)
    "Nhân viên quản lý": "Management staff", "Nhân viên bán hàng": "Sales staff",
    "Nhân viên kho": "Warehouse staff", '"Nhân viên"': '"Staff"', '"Khách hàng"': '"Customer"',
    "Hệ thống quản lý nhà hàng": "Restaurant management system",
    "người nhận thanh toán": "received by", "người lập hóa đơn": "issued by", "người xử lý": "handled by",
    # state names (EN-05)
    "Hiển thị GD chính": "Show main UI",
    "Hiển thị GD quản lý\\nmón ăn": "Show dish\\nmanagement UI",
    "Hiển thị GD thêm\\nmón ăn": "Show add\\ndish UI",
    "Hiển thị GD chọn loại\\nthống kê": "Show statistic\\ntype UI",
    "Hiển thị GD thống kê\\nkhách hàng": "Show customer\\nstatistics UI",
    "Hiển thị GD chi tiêu và\\ndanh sách đơn hàng": "Show customer\\nspending UI",
    "Hiển thị GD chi tiết\\nđơn hàng": "Show order\\ndetail UI",
    "[Chọn quản lý món ăn]": "[select Manage dish information]", "[Chọn thêm món ăn]": "[click Add dish]",
    "[Chọn lưu]": "[click Save]", "[Chọn xem thống kê]": "[select View statistics reports]",
    "[Chọn thống kê khách hàng]": "[select Customer statistics by revenue]",
    "[Chọn khách hàng/No name]": "[click 1 customer/No name]", "[Chọn xem 1 đơn hàng]": "[click 1 order]",
    # activity (EN-03)
    "Bắt đầu": "Start", "Kết thúc": "End", "Xử lí tại ": "Process at ",
    "Nhận thông tin QL": "Get manager info", "Nhận thông tin món ăn": "Get dish info",
    "Lấy danh sách loại món ăn": "Get dish category list", "Lấy danh sách món ăn": "Get dish list",
    "Kiểm tra thông tin món ăn": "Validate dish info", "Lưu món ăn": "Save dish",
    "Thông báo thành công": "Show success message", "Kiểm tra khoảng ngày": "Validate period",
    "Lấy thống kê khách hàng": "Get customer statistics", "Lấy danh sách đơn": "Get order list",
    "Lấy thông tin đơn": "Get order info", "Lấy chi tiết đơn": "Get order lines", "Hiện GD": "Show UI",
    "[lấy xong dữ liệu]": "[data loaded]", "[không hợp lệ]": "[invalid]", "[hợp lệ]": "[valid]",
    "[lưu xong]": "[saved]", "[lưu lỗi]": "[save failed]", "[đã thêm món mới]": "[new dish added]",
    "[chưa chọn kỳ]": "[no period chosen]", "[đã chọn kỳ]": "[period chosen]", "[xem xong]": "[done viewing]",
    "[click Quản lý thông tin món ăn]": "[click Manage dish information]",
    "[click Quản lý thông tin\\nmón ăn]": "[click Manage dish\\ninformation]",
    "[click Quản lý\\nthông tin món ăn]": "[click Manage dish\\ninformation]",
    "[click Thêm món ăn]": "[click Add dish]", "[click Lưu]": "[click Save]",
    "[click Xem báo cáo thống kê]": "[click View statistics reports]",
    "[click Xem báo cáo\\nthống kê]": "[click View statistics\\nreports]",
    "[click Thống kê khách hàng\\ntheo doanh thu]": "[click Customer statistics\\nby revenue]",
    "[click Thống kê khách\\nhàng theo doanh thu]": "[click Customer statistics\\nby revenue]",
    "[click Xem thống kê]": "[click View statistics]",
    "click 1 khách hàng": "click 1 customer", "[click 1 đơn]": "[click 1 order]",
    # sequence (EN-04) - user actions first, then verbs
    "chọn Quản lý thông tin món ăn": "select Manage dish information",
    "click Thêm món ăn": "click Add dish", "nhập thông tin + click Lưu": "enter data + click Save",
    "chọn Xem báo cáo thống kê": "select View statistics reports",
    "chọn Thống kê khách hàng theo doanh thu": "select Customer statistics by revenue",
    "chọn ngày + click Xem thống kê": "choose dates + click View statistics",
    "click 1 đơn": "click 1 order", "thông báo thành công": "show success message",
    # communication (EN-05)
    "chọn quay lại danh sách thống kê": "click Back to statistic list",
    "chọn quay lại danh sách đơn": "click Back to order list",
    "chọn xem báo cáo thống kê": "select View statistics reports",
    "chọn thống kê khách hàng": "select Customer statistics by revenue",
    "chọn kỳ và xem thống kê": "choose period + click View statistics",
    "đổi kỳ và xem thống kê": "change period + click View statistics",
    "chọn khách/nhóm khác": "select another customer/group", "chọn khách/nhóm": "select customer/group",
    "chọn đơn khác": "select another order", "chọn đơn": "select order",
    "chọn quản lý món ăn": "select Manage dish information", "chọn thêm món ăn": "click Add dish",
    "chọn loại và trạng thái": "choose category and status", "nhập thông tin": "enter dish data",
    "chọn lưu": "click Save", "báo thành công": "show success message",
    "trả về": "return", "hiển thị": "display", "gọi": "call",
}
# state-diagram back transitions depend on the source state (EN-05 + EN-07 labels)
STATE_BACK = {
    ("state-add-dish.puml", "DishManagement -up-> Main"): "[click Back to main page]",
    ("state-add-dish.puml", "AddDish -up-> DishManagement"): "[click Back to dish list]",
    ("state-customer-statistics.puml", "SelectReport -up-> Main"): "[click Back to main page]",
    ("state-customer-statistics.puml", "CustomerStats -up-> SelectReport"): "[click Back to statistics reports]",
    ("state-customer-statistics.puml", "CustomerSpending -up-> CustomerStats"): "[click Back to statistic list]",
    ("state-customer-statistics.puml", "OrderDetail -up-> CustomerSpending"): "[click Back to order list]",
}


def diagrams():
    keys = sorted(DIAGRAM_PHRASES, key=len, reverse=True)
    total = 0
    for f in sorted((ROOT / "diagrams").glob("*.puml")):
        out, n = [], 0
        for line in f.read_text(encoding="utf-8").split("\n"):
            if line.lstrip().startswith("'"):
                out.append(line); continue
            for (name, prefix), label in STATE_BACK.items():
                if f.name == name and line.startswith(prefix) and "[Chọn quay lại]" in line:
                    line = line.replace("[Chọn quay lại]", label); n += 1
            for k in keys:
                if k in line:
                    n += line.count(k); line = line.replace(k, DIAGRAM_PHRASES[k])
            out.append(line)
        if n:
            f.write_text("\n".join(out), encoding="utf-8"); total += n
            print(f"{n:5d}  {f.relative_to(ROOT)}")
    print(f"total {total}")


if __name__ == "__main__":
    step = sys.argv[1] if len(sys.argv) > 1 else "attrs"
    if step == "attrs":
        apply(EN01)
    elif step == "diagrams":
        diagrams()
    else:
        sys.exit(f"unknown step {step}")
