#!/usr/bin/env python3
"""Apply the student-number suffix mappings from rules/12, UC-N1..UC-N5.

The script deliberately edits content only.  Java/JSP filenames are renamed in
the following rule step after references have been updated.  It is idempotent:
already-renamed identifiers do not match the old names.
"""

from __future__ import annotations

from collections import Counter
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]

USE_CASES = {
    "Đăng nhập": "Login 4",
    "Nhân viên quản lý đăng nhập": "Management staff login 4",
    "Quản lý thông tin món ăn": "Manage dish information 4",
    "Thêm món ăn mới": "Add new dish 4",
    "Tạo combo thực đơn": "Create combo menu 4",
    "Xem báo cáo thống kê": "View statistics reports 4",
    "Xem thống kê khách hàng theo doanh thu": "View customer statistics by revenue 4",
    "Xem chi tiết chi tiêu khách hàng và danh sách đơn hàng": "View customer spending and order list 4",
    "Xem chi tiết đơn hàng": "View order details 4",
    "Quản lý nhà cung cấp": "Manage suppliers 4",
    "Nhập nguyên liệu": "Import ingredients 4",
    "Tiếp nhận khách": "Receive customers 4",
    "Ghi nhận gọi món": "Take orders 4",
    "Thanh toán tại bàn": "Receive payment at table 4",
    "Cấp thẻ thành viên": "Issue membership card 4",
    "Xác nhận đặt bàn": "Confirm table reservation 4",
    "Xác nhận đơn trực tuyến": "Confirm online order 4",
    "Tra cứu món ăn": "Search dishes 4",
    "Đặt bàn trực tuyến": "Book table online 4",
    "Đặt món trực tuyến": "Order food online 4",
}

ENTITY_CLASSES = {
    name: f"{name}4"
    for name in (
        "Staff", "ManagementStaff", "WarehouseStaff", "SalesStaff", "Account",
        "Customer", "MembershipCard", "Supplier", "Product", "Dish",
        "DishCategory", "Combo", "ComboDish", "Ingredient", "GoodsReceipt",
        "GoodsReceiptIngredient", "DiningTable", "Reservation", "ServiceSession",
        "Order", "OrderSubmission", "OrderSubmissionProduct", "Invoice", "Payment",
        "CustomerStatistic", "ProductStatistic", "IngredientStatistic",
        "SupplierStatistic",
    )
}

VIEW_CLASSES = {
    "GDChinh": "ManagerMainView4",
    "GDQuanLyMonAn": "DishManagementView4",
    "GDThemMonAn": "AddDishView4",
    "GDBaoCaoThongKe": "StatisticsReportView4",
    "GDThongKeKhachHang": "CustomerStatisticsView4",
    "GDChiTietChiTieuKhachHang": "CustomerSpendingView4",
    "GDChiTietDonHang": "OrderDetailView4",
}

JSP_PAGES = {
    "gdChinhQuanLy.jsp": "managerMainView4.jsp",
    "gdQuanLyMonAn.jsp": "dishManagementView4.jsp",
    "gdThemMonAn.jsp": "addDishView4.jsp",
    "doLuuMonAn.jsp": "doSaveDish4.jsp",
    "gdBaoCaoThongKe.jsp": "statisticsReportView4.jsp",
    "gdThongKeKhachHang.jsp": "customerStatisticsView4.jsp",
    "gdChiTietChiTieuKhachHang.jsp": "customerSpendingView4.jsp",
    "gdChiTietDonHang.jsp": "orderDetailView4.jsp",
    "gdDangNhap.jsp": "loginView4.jsp",
    "doDangNhap.jsp": "doLogin4.jsp",
}

DAO_CLASSES = {
    "OrderSubmissionProductDAO": "OrderSubmissionProductDAO4",
    "CustomerStatisticDAO": "CustomerStatisticDAO4",
    "DishCategoryDAO": "DishCategoryDAO4",
    "DishDAO": "DishDAO4",
    "OrderDAO": "OrderDAO4",
    "StaffDAO": "StaffDAO4",
    "DAO": "DAO4",
}

TEST_CLASSES = {
    "DishCategoryDAOTest": "DishCategoryDAOTest4",
    "DishDAOTest": "DishDAOTest4",
    "StaffDAOTest": "StaffDAOTest4",
    "StatisticsDAOTest": "StatisticsDAOTest4",
    "DatabaseTest": "DatabaseTest4",
}

# Rule 05's twenty designed tables.  Mapping all class-derived table names is
# harmless and also catches prose/SQL references to supporting classes.
TABLES = {f"tbl{name}": f"tbl{name}4" for name in ENTITY_CLASSES}

# Normalise output from an interrupted/older run of this script.  Keeping the
# repair here makes the migration reproducible instead of requiring hand edits.
REPAIRS = {
    "Order4 food online 4": "Order food online 4",
    "Combo4 nước": "Combo nước",
    "Combo4 (Combo4)": "Combo (Combo4)",
    "tblDish4Category4": "tblDishCategory4",
    "tblCombo4Dish4": "tblComboDish4",
    "tblGoodsReceipt4Ingredient4": "tblGoodsReceiptIngredient4",
    "tblOrder4Submission4Product4": "tblOrderSubmissionProduct4",
    "tblOrder4Submission4": "tblOrderSubmission4",
    "tblCustomer4Statistic4": "tblCustomerStatistic4",
    "tblProduct4Statistic4": "tblProductStatistic4",
    "tblIngredient4Statistic4": "tblIngredientStatistic4",
    "tblSupplier4Statistic4": "tblSupplierStatistic4",
    "tblCombid": "tblCombo4id",
}

TABLE_GROUPS = (
    ("UC-N3 JSP pages", JSP_PAGES, "literal"),
    ("UC-N5 tables and foreign keys", TABLES, "literal"),
    ("UC-N3 view classes", VIEW_CLASSES, "identifier"),
    ("UC-N4 DAO classes", DAO_CLASSES, "identifier"),
    ("UC-N4 test classes", TEST_CLASSES, "identifier"),
    ("UC-N2 entity classes", ENTITY_CLASSES, "identifier"),
)

MARKDOWN_FILES = (
    "spec.md", "analysis.md", "desing.md", "implement.md", "README.md",
    "plan-bao-cao-full.md",
)


def source_files() -> list[Path]:
    files = [ROOT / name for name in MARKDOWN_FILES]
    files += sorted((ROOT / "diagrams").glob("*.puml"))
    files.append(ROOT / "diagrams/ui-design.py")
    files += sorted(
        path for path in (ROOT / "code/restaurant/src").rglob("*")
        if path.is_file()
    )
    files += sorted((ROOT / "code/restaurant/db").glob("*.sql"))
    files += sorted((ROOT / "code/restaurant/tools").glob("*.py"))
    return [path for path in files if path.exists() and "target" not in path.parts]


def protected_ranges(path: Path, text: str) -> list[tuple[int, int]]:
    """Protect source material that rule 12 says must remain verbatim."""
    if path.name != "spec.md":
        return []
    # The whole keyword table is protected, including its English column.
    start = text.find("## 1. BẢNG TỪ KHÓA")
    end = text.find("## 2. MÔ TẢ HỆ THỐNG", start)
    return [(start, end)] if start >= 0 and end >= 0 else []


def replace_segment(
    text: str,
    audit_base: str,
    line_offset: int,
    totals: Counter[str],
) -> str:
    # Tokens protect newly-created use-case/page/table text from later class
    # substitutions (notably "Order food online 4").
    tokens: dict[str, str] = {}
    token_index = 0

    # UC-N1 keeps the module label itself in Vietnamese even when the use case
    # named elsewhere uses English + 4.
    module_pattern = re.compile(
        r"\b([Mm]odule(?:\s+\d+:)?\s+)"
        r"(Thêm món ăn mới|Xem thống kê khách hàng theo doanh thu)\b"
    )

    def protect_module(match: re.Match[str]) -> str:
        nonlocal token_index
        token = f"\x00RENAME4_{token_index}\x00"
        token_index += 1
        tokens[token] = match.group(0)
        return token

    text = module_pattern.sub(protect_module, text)
    for value in ("Combo nước", "Combo (Combo4)"):
        if value not in text:
            continue
        token = f"\x00RENAME4_{token_index}\x00"
        token_index += 1
        tokens[token] = value
        text = text.replace(value, token)
    for value in sorted(USE_CASES.values(), key=len, reverse=True):
        if value not in text:
            continue
        token = f"\x00RENAME4_{token_index}\x00"
        token_index += 1
        tokens[token] = value
        text = text.replace(value, token)

    for group, mapping, mode in TABLE_GROUPS:
        for old in sorted(mapping, key=len, reverse=True):
            new = mapping[old]
            if mode == "identifier":
                pattern = re.compile(rf"(?<![A-Za-z0-9_]){re.escape(old)}(?![A-Za-z0-9_])")
            elif group == "UC-N5 tables and foreign keys":
                # A foreign key continues with "id", while a completed rename
                # continues with "4".  This keeps repeated runs idempotent.
                pattern = re.compile(re.escape(old) + r"(?!4|[A-Z])")
            else:
                pattern = re.compile(re.escape(old))

            def substitution(match: re.Match[str]) -> str:
                nonlocal token_index
                line = line_offset + text.count("\n", 0, match.start()) + 1
                print(f"{audit_base}:{line}: [{group}] {old!r} -> {new!r}")
                totals[group] += 1
                if mode in {"phrase", "literal"}:
                    token = f"\x00RENAME4_{token_index}\x00"
                    token_index += 1
                    tokens[token] = new
                    return token
                return new

            text = pattern.sub(substitution, text)

    for token, value in tokens.items():
        text = text.replace(token, value)
    return text


def scope_use_cases(path: Path, text: str, totals: Counter[str]) -> str:
    """Repair UC-N9 and apply UC-N1 only to names, never UI/action text."""
    reverse = {new: old for old, new in USE_CASES.items()}

    def convert(value: str, mapping: dict[str, str], group: str, line: int) -> str:
        keys = sorted(mapping, key=len, reverse=True)
        patterns = {re.escape(k).replace(r"\ ", r"(?: |\\n)+"): k for k in keys}
        pattern = re.compile("|".join(patterns))
        def replace(match: re.Match[str]) -> str:
            key = match.group().replace(r"\n", " ")
            new = mapping[key]
            totals[group] += 1
            print(f"{path.relative_to(ROOT)}:{line}: [{group}] {match.group()!r} -> {new!r}")
            return new
        return pattern.sub(replace, value)

    is_diagram = path.parent.name == "diagrams" and path.name.startswith("use-case-") and path.suffix == ".puml"
    in_spec = False
    intro = False
    name_column = None
    output = []
    for number, line in enumerate(text.splitlines(keepends=True), 1):
        if is_diagram:
            def ellipse(match: re.Match[str]) -> str:
                label = match[1]
                old = label.replace(r"\n", " ")
                if old not in USE_CASES:
                    return match[0]
                new = USE_CASES[old]
                if r"\n" in label:
                    spaces = [i for i, c in enumerate(new) if c == " "]
                    split = min(spaces, key=lambda i: abs(i - len(new) / 2))
                    new = new[:split] + r"\n" + new[split + 1:]
                totals["UC-N1 use cases"] += 1
                print(f"{path.relative_to(ROOT)}:{number}: [UC-N1 use cases] {label!r} -> {new!r}")
                return 'usecase "' + new + '"'
            output.append(re.sub(r'usecase "([^"\n]+)"', ellipse, line))
            continue
        if path.name == "spec.md":
            if line.startswith("## "):
                in_spec = line.startswith("## 3.")
                intro = in_spec
            if line.startswith("### "):
                intro = False
            if line.startswith("|"):
                cells = line.split("|")
                headers = [c.strip() for c in cells]
                if "Tên use case" in headers or "Use case" in headers:
                    name_column = next(i for i, c in enumerate(headers) if c in {"Tên use case", "Use case"})
            else:
                name_column = None
        # Preserve only explicit use-case name fields and the section introduction.
        allowed_row = path.name == "analysis.md" and line.startswith("| Use case |")
        allowed_spec = path.name == "spec.md" and in_spec
        if allowed_row or (allowed_spec and ((intro and not line.startswith(("![", "Hình ", "PlantUML", "## "))) or line.startswith("Tên use case:"))):
            line = convert(line, USE_CASES, "UC-N1 use cases", number)
        elif allowed_spec and line.startswith("|") and name_column is not None:
            cells = line.split("|")
            for i in range(len(cells)):
                cells[i] = convert(cells[i], USE_CASES if i == name_column else reverse,
                                   "UC-N1 use cases" if i == name_column else "UC-N9 reverse", number)
            line = "|".join(cells)
        else:
            line = convert(line, reverse, "UC-N9 reverse", number)
        output.append(line)
    return "".join(output)


def transform(path: Path, totals: Counter[str]) -> bool:
    original = path.read_text(encoding="utf-8")
    prepared = scope_use_cases(path, original, totals)
    for wrong, correct in sorted(REPAIRS.items(), key=lambda item: len(item[0]), reverse=True):
        prepared = prepared.replace(wrong, correct)
    ranges = protected_ranges(path, prepared)
    if not ranges:
        updated = replace_segment(prepared, str(path.relative_to(ROOT)), 0, totals)
    else:
        chunks: list[str] = []
        cursor = 0
        for start, end in ranges:
            chunks.append(replace_segment(
                prepared[cursor:start], str(path.relative_to(ROOT)),
                prepared.count("\n", 0, cursor), totals,
            ))
            chunks.append(prepared[start:end])
            cursor = end
        chunks.append(replace_segment(
            prepared[cursor:], str(path.relative_to(ROOT)),
            prepared.count("\n", 0, cursor), totals,
        ))
        updated = "".join(chunks)
    if updated == original:
        return False
    path.write_text(updated, encoding="utf-8")
    return True


def main() -> None:
    totals: Counter[str] = Counter()
    changed = [path for path in source_files() if transform(path, totals)]
    print("\nChanged files:")
    for path in changed:
        print(path.relative_to(ROOT))
    print("\nCounts by §2 table:")
    for group in ("UC-N1 use cases", "UC-N9 reverse", *(g for g, _, _ in TABLE_GROUPS)):
        print(f"{group}: {totals[group]}")
    print(f"TOTAL: {sum(totals.values())}")


if __name__ == "__main__":
    main()
