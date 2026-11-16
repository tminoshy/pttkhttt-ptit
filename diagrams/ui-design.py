#!/usr/bin/env python3
"""Sinh diagrams/ui-design.xlsx và hai hình PNG giao diện từ MỘT bộ dữ liệu bố cục.

Quy tắc: rules/06-thiet-ke-giao-dien.md (UI-00 .. UI-09).
Chạy: python3 diagrams/ui-design.py   (cần openpyxl, Pillow)
"""
import os
from dataclasses import dataclass, field
from openpyxl import Workbook
from openpyxl.styles import Alignment, Border, Font, PatternFill, Side
from openpyxl.utils import get_column_letter
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))

# --------------------------------------------------------------------------
# Hằng số bố cục (UI-05, UI-06)
# --------------------------------------------------------------------------
COL_CHARS = 2.5          # độ rộng cột trong Excel
CELL_W = 23              # px mỗi cột (1x)
CELL_H = 24              # px mỗi hàng (18 pt = 24 px)
ROW_PT = 18
FRAME_W = 32             # số cột mỗi khung
GAP = 5                  # số cột giữa hai khung cùng hàng
SCALE = 2                # tỉ lệ PNG
BLUE_FILL = "9DC3E6"
LINK_BLUE = "0563C1"
ARROW_GRAY = "808080"
FONT = "Arial"
FONT_FILES = {
    (False, False): "/System/Library/Fonts/Supplemental/Arial.ttf",
    (True, False): "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
    (False, True): "/System/Library/Fonts/Supplemental/Arial Italic.ttf",
    (True, True): "/System/Library/Fonts/Supplemental/Arial Bold Italic.ttf",
}
SYMBOL_FONT = "/System/Library/Fonts/Supplemental/Arial Unicode.ttf"  # có ⇨ ⇩ ⇦ ▼


@dataclass
class E:
    """Một thành phần = một vùng ô gộp (tọa độ cục bộ hoặc toàn cục, 0-based)."""
    r: int
    c: int
    rs: int
    cs: int
    text: str = ""
    size: float = 10
    bold: bool = False
    italic: bool = False
    color: str = "000000"
    underline: bool = False
    fill: str | None = None
    border: bool = False
    halign: str = "left"
    wrap: bool = False
    symbol: bool = False          # dùng font có glyph mũi tên
    clip_ok: bool = False         # bỏ kiểm tra chữ bị cắt (chỉ dùng cho mũi tên)

    def moved(self, dr, dc):
        import copy
        n = copy.copy(self)
        n.r += dr
        n.c += dc
        return n


@dataclass
class Frame:
    name: str            # tên trang .jsp
    cls: str             # lớp GD
    step: int
    elems: list = field(default_factory=list)
    h: int = 0           # số hàng của khung

    @property
    def caption(self):
        return f"{self.name} · {self.cls} · step {self.step}"


# --------------------------------------------------------------------------
# Hàm dựng thành phần trong khung (cột 0 và FRAME_W-1 là lề khung)
# --------------------------------------------------------------------------
class Builder:
    def __init__(self, name, cls, step):
        self.f = Frame(name, cls, step)
        self.r = 1
        self.f.elems.append(None)  # giữ chỗ, bỏ khi hoàn tất

    def _add(self, e):
        self.f.elems.append(e)

    def title(self, text):
        self._add(E(self.r, 1, 2, FRAME_W - 2, text, size=13, bold=True, halign="center"))
        self.r += 2
        return self

    def gap(self, n=1):
        self.r += n
        return self

    def context(self, label, value):
        self._add(E(self.r, 1, 1, FRAME_W - 2, f"{label}: {value}"))
        self.r += 1
        return self

    def field_in(self, label, value, dropdown=False):
        self._add(E(self.r, 1, 1, 9, label + ":"))
        if dropdown:
            self._add(E(self.r, 10, 1, 14, value, border=True))
            self._add(E(self.r, 24, 1, 1, "▼", size=8, border=True, halign="center", symbol=True))
        else:
            self._add(E(self.r, 10, 1, 15, value, border=True))
        self.r += 1
        return self

    def field_file(self, label):
        self._add(E(self.r, 1, 1, 9, label + ":"))
        self._add(E(self.r, 10, 1, 4, "Choose file", fill=BLUE_FILL, border=True, halign="center"))
        self._add(E(self.r, 15, 1, 10, "No file chosen"))
        self.r += 1
        return self

    def button(self, text, cells, col=1):
        self._add(E(self.r, col, 1, cells, text, fill=BLUE_FILL, border=True, halign="center"))
        self.r += 1
        return self

    def link(self, text):
        self._add(E(self.r, 1, 1, FRAME_W - 2, text, color=LINK_BLUE, underline=True))
        self.r += 1
        return self

    def notice(self, text=""):
        self._add(E(self.r, 1, 1, FRAME_W - 2, ("Message: " + text).rstrip() if text else "Message:",
                    italic=True))
        self.r += 1
        return self

    def total(self, text):
        self._add(E(self.r, 1, 1, FRAME_W - 2, text, bold=True, halign="right"))
        self.r += 1
        return self

    def table(self, cols, rows, link_col=None):
        """cols: [(tiêu đề, số cột, căn lề)]; rows: list[list[str]]; link_col: chỉ số cột liên kết."""
        c = 1
        for title, span, _ in cols:
            self._add(E(self.r, c, 2, span, title, bold=True, italic=True, border=True,
                        halign="center", wrap=True))
            c += span
        assert c == FRAME_W - 1, f"bảng rộng {c - 1} cột, cần {FRAME_W - 2}"
        self.r += 2
        for row in rows:
            c = 1
            for i, (_, span, al) in enumerate(cols):
                is_link = link_col == i and row[i] != ""
                self._add(E(self.r, c, 1, span, row[i], border=True, halign=al,
                            color=LINK_BLUE if is_link else "000000", underline=is_link))
                c += span
            self.r += 1
        return self

    def done(self):
        self.f.elems = [e for e in self.f.elems if e is not None]
        self.f.h = self.r + 1  # hàng lề dưới
        return self.f


# --------------------------------------------------------------------------
# Dữ liệu (UI-04) — các số được kiểm tra bằng assert
# --------------------------------------------------------------------------
def money(n):
    return f"{n:,}".replace(",", ".")


DISHES = [  # dữ liệu minh họa (N-05); D004 là món nhập ở bước 5
    ("D001", "Lẩu hải sản", "Món chính", 650000, "Đang kinh doanh"),
    ("D002", "Cơm rang hải sản", "Món chính", 150000, "Đang kinh doanh"),
    ("D003", "Trà đào", "Đồ uống", 35000, "Đang kinh doanh"),
]
NEW_DISH = ("D004", "Cơm gà", "Món chính", 65000, "Đang kinh doanh")
assert len({d[0] for d in DISHES + [NEW_DISH]}) == 4 and all(d[3] > 0 for d in DISHES + [NEW_DISH])

STATS = [("KH001", "Nguyễn Văn An", 4250000), ("KH002", "Trần Thị Bình", 2180000), ("", "No name", 1350000)]
assert [s[2] for s in STATS] == sorted((s[2] for s in STATS), reverse=True)
ORDERS = [("DH101", "03/08/2026 20:05", 750000), ("DH102", "08/08/2026 11:42", 620000),
          ("DH103", "15/08/2026 20:36", 1100000), ("DH104", "22/08/2026 18:02", 880000),
          ("DH105", "29/08/2026 13:25", 900000)]
assert sum(o[2] for o in ORDERS) == STATS[0][2]
LINES = [("Lẩu hải sản", 1, 650000), ("Combo nước", 2, 75000), ("Cơm rang hải sản", 2, 150000)]
LINE_TOTALS = [q * p for _, q, p in LINES]
assert sum(LINE_TOTALS) == ORDERS[2][2] == 1100000

PERIOD = "01/08/2026 – 31/08/2026"
L, R, C = "left", "right", "center"


def dish_rows(dishes):
    return [[str(i + 1), d[0], d[1], d[2], money(d[3]), d[4]] for i, d in enumerate(dishes)]


DISH_COLS = [("No.", 2, C), ("Dish code", 4, L), ("Dish name", 8, L), ("Dish category", 6, L),
             ("Sale price (VND)", 5, R), ("Sale status", 5, L)]


def main_frame():
    b = Builder("managerMainView4.jsp", "ManagerMainView4", 1)
    b.title("Management staff main page").gap()
    b.link("Manage dish information").link("View statistics reports")
    return b.done()


def dish_list_frame(step, dishes):
    b = Builder("dishManagementView4.jsp", "DishManagementView4", step)
    b.title("Manage dish information").gap()
    b.table(DISH_COLS, dish_rows(dishes)).gap()
    b.button("Add dish", 6, col=1).gap()
    b.button("Back to main page", 8)
    return b.done()


def add_dish_frame():
    b = Builder("addDishView4.jsp", "AddDishView4", 22)
    b.title("Add new dish").gap()
    d = NEW_DISH
    b.field_in("Dish code", d[0]).field_in("Dish name", d[1]).field_in("Description", "Cơm gà xé")
    b.field_in("Sale price (VND)", money(d[3]))
    b.field_in("Sale status", d[4], dropdown=True)
    b.field_in("Dish category", "LM02 – Món chính", dropdown=True)
    b.field_file("Image").gap()
    b.button("Save", 4, col=10).gap()
    b.notice("Dish added successfully.")
    b.button("Back to dish list", 8)
    return b.done()


def report_frame():
    b = Builder("statisticsReportView4.jsp", "StatisticsReportView4", 3)
    b.title("Statistics reports").gap()
    for t in ("Dish statistics", "Ingredient statistics", "Customer statistics by revenue",
              "Supplier statistics"):
        b.link(t)
    b.gap().button("Back to main page", 8)
    return b.done()


def customer_stats_frame():
    b = Builder("customerStatisticsView4.jsp", "CustomerStatisticsView4", 14)
    b.title("Customer statistics by revenue").gap()
    b.field_in("Start date", "01/08/2026").field_in("End date", "31/08/2026")
    b.button("View statistics", 6, col=10).gap()
    rows = [[str(i + 1), s[0], s[1], money(s[2])] for i, s in enumerate(STATS)]
    b.table([("No.", 2, C), ("Customer code", 5, L), ("Display name", 12, L), ("Total spending (VND)", 11, R)],
            rows, link_col=2).gap()
    b.notice().button("Back to statistics reports", 8)
    return b.done()


def spending_frame():
    b = Builder("customerSpendingView4.jsp", "CustomerSpendingView4", 20)
    b.title("Customer spending details").gap()
    b.context("Customer code", "KH001").context("Display name", "Nguyễn Văn An").context("Period", PERIOD).gap()
    rows = [[str(i + 1), o[0], o[1], money(o[2])] for i, o in enumerate(ORDERS)]
    b.table([("No.", 2, C), ("Order code", 7, L), ("Paid at", 12, L), ("Amount (VND)", 9, R)],
            rows, link_col=1)
    b.total("Total spending: " + money(STATS[0][2])).gap()
    b.notice().button("Back to statistic list", 8)
    return b.done()


def order_frame():
    b = Builder("orderDetailView4.jsp", "OrderDetailView4", 29)
    b.title("Order details").gap()
    b.context("Order code", "DH103").context("Customer", "KH001 – Nguyễn Văn An").context("Period", PERIOD)
    b.context("Invoice code", "HD103").context("Paid at", "15/08/2026 20:36").gap()
    rows = [[str(i + 1), n, str(q), money(p), money(q * p)] for i, (n, q, p) in enumerate(LINES)]
    b.table([("No.", 2, C), ("Dish or combo", 12, L), ("Quantity", 4, R), ("Unit price (VND)", 6, R),
             ("Line total (VND)", 6, R)], rows)
    b.total("Total amount: " + money(sum(LINE_TOTALS))).gap()
    b.notice().button("Back to order list", 8)
    return b.done()


TABS = [
    dict(name="Thêm món ăn", title="Thiết kế giao diện module Thêm món ăn mới",
         png="ui-add-dish.png",
         frames=[main_frame(), dish_list_frame(6, DISHES), add_dish_frame(),
                 dish_list_frame(27, DISHES + [NEW_DISH])]),
    dict(name="Thống kê khách hàng", title="Thiết kế giao diện module Xem thống kê khách hàng theo doanh thu",
         png="ui-customer-statistics.png",
         frames=[main_frame(), report_frame(), customer_stats_frame(), spending_frame(), order_frame()]),
]

# --------------------------------------------------------------------------
# Xếp khung zíc zắc và mũi tên (UI-06)
# --------------------------------------------------------------------------
LEFT_C = 1                       # cột B (0-based = 1)
RIGHT_C = LEFT_C + FRAME_W + GAP
TOTAL_COLS = RIGHT_C + FRAME_W + 1
HEAD_ROWS = 2                    # hàng 1 tiêu đề tab, hàng 2 trống


def layout(frames):
    """Trả về (elements toàn cục, danh sách hình chữ nhật khung, tổng số hàng)."""
    elems, rects = [], []
    top = HEAD_ROWS
    k = 0
    n = len(frames)
    rows = [frames[i:i + 2] for i in range(0, n, 2)]
    for ri, row in enumerate(rows):
        ltr = ri % 2 == 0
        cols = [LEFT_C, RIGHT_C] if ltr else [RIGHT_C, LEFT_C]
        placed = []
        for fi, fr in enumerate(row):
            c0 = cols[fi]
            placed.append((fr, c0))
            rects.append((top, c0, fr.h, FRAME_W))
            for e in fr.elems:
                elems.append(e.moved(top, c0))
            elems.append(E(top + fr.h, c0, 1, FRAME_W, fr.caption, size=9, italic=True, color=ARROW_GRAY))
        maxh = max(f.h for f, _ in placed)
        if len(placed) == 2:                       # mũi tên ngang giữa hai khung
            gap_c = LEFT_C + FRAME_W
            g = "⇨" if ltr else "⇦"
            mid = top + (min(f.h for f, _ in placed) - 3) // 2
            elems.append(E(mid, gap_c, 3, GAP, g, size=36, color=ARROW_GRAY, halign="center",
                           symbol=True, clip_ok=True))
        top += maxh + 1                            # hàng chú thích
        if ri < len(rows) - 1:                     # mũi tên xuống dưới khung cuối của hàng
            c0 = placed[-1][1]
            elems.append(E(top, c0, 3, FRAME_W, "⇩", size=36, color=ARROW_GRAY, halign="center",
                           symbol=True, clip_ok=True))
            top += 3 + 1
    return elems, rects, top


# --------------------------------------------------------------------------
# Đo chữ (kiểm tra bị cắt) và font
# --------------------------------------------------------------------------
_font_cache = {}


def pil_font(size_pt, bold=False, italic=False, symbol=False, scale=1):
    key = (size_pt, bold, italic, symbol, scale)
    if key not in _font_cache:
        path = SYMBOL_FONT if symbol else FONT_FILES[(bold, italic)]
        _font_cache[key] = ImageFont.truetype(path, size_pt * 96 / 72 * scale)
    return _font_cache[key]


def text_w(e, scale=1):
    return pil_font(e.size, e.bold, e.italic, e.symbol, scale).getlength(e.text)


def check_fit(elems):
    for e in elems:
        if e.clip_ok or not e.text:
            continue
        avail = e.cs * CELL_W - 8
        if e.wrap:
            words, lines, cur = e.text.split(), 1, ""
            f = pil_font(e.size, e.bold, e.italic)
            for w in words:
                t = (cur + " " + w).strip()
                if f.getlength(t) > avail and cur:
                    lines += 1
                    cur = w
                else:
                    cur = t
                assert f.getlength(w) <= avail, f"từ '{w}' quá rộng"
            assert lines <= e.rs, f"'{e.text}' cần {lines} dòng, chỉ có {e.rs} hàng"
        else:
            assert text_w(e) <= avail, f"chữ bị cắt: '{e.text}' {text_w(e):.0f}px > {avail}px ({e.cs} cột)"


# --------------------------------------------------------------------------
# Xuất PNG (2x, bỏ hai hàng tiêu đề tab)
# --------------------------------------------------------------------------
def render_png(elems, rects, total_rows, path):
    s = SCALE
    cw, ch = CELL_W * s, CELL_H * s
    rows_vis = total_rows - HEAD_ROWS
    PAD = 12 * s
    img = Image.new("RGB", (TOTAL_COLS * cw, rows_vis * ch + 2 * PAD), "white")
    d = ImageDraw.Draw(img)
    oy = HEAD_ROWS * ch - PAD

    def box(e):
        x0, y0 = e.c * cw, e.r * ch - oy
        return x0, y0, x0 + e.cs * cw, y0 + e.rs * ch

    for e in elems:
        x0, y0, x1, y1 = box(e)
        if e.fill:
            d.rectangle([x0, y0, x1, y1], fill="#" + e.fill)
    for e in elems:
        x0, y0, x1, y1 = box(e)
        if e.border:
            d.rectangle([x0, y0, x1, y1], outline="black", width=2)
        if not e.text:
            continue
        f = pil_font(e.size, e.bold, e.italic, e.symbol, s)
        pad = 4 * s
        lines = [e.text]
        if e.wrap:
            lines, cur = [], ""
            for w in e.text.split():
                t = (cur + " " + w).strip()
                if f.getlength(t) > (x1 - x0) - 2 * pad and cur:
                    lines.append(cur)
                    cur = w
                else:
                    cur = t
            lines.append(cur)
        lh = f.size * 1.25
        ty = (y0 + y1) / 2 - lh * len(lines) / 2 + lh / 2
        for ln in lines:
            w = f.getlength(ln)
            tx = {"left": x0 + pad, "right": x1 - pad - w, "center": (x0 + x1) / 2 - w / 2}[e.halign]
            if e.symbol:   # căn theo hộp bao thực của glyph để mũi tên nằm giữa vùng gộp
                bb = f.getbbox(ln)
                gw, gh = bb[2] - bb[0], bb[3] - bb[1]
                gx = {"left": x0 + pad, "right": x1 - pad - gw, "center": (x0 + x1) / 2 - gw / 2}[e.halign]
                d.text((gx - bb[0], (y0 + y1) / 2 - gh / 2 - bb[1]), ln, font=f, fill="#" + e.color)
            else:
                d.text((tx, ty), ln, font=f, fill="#" + e.color, anchor="lm")
            if e.underline:
                uy = ty + f.size * 0.42
                d.line([tx, uy, tx + w, uy], fill="#" + e.color, width=max(1, s))
            ty += lh
    for (r, c, h, w) in rects:       # viền ngoài khung, dày vừa
        d.rectangle([c * cw, r * ch - oy, (c + w) * cw, (r + h) * ch - oy], outline="black", width=4)
    img.save(path)


# --------------------------------------------------------------------------
# Xuất .xlsx
# --------------------------------------------------------------------------
THIN = Side(style="thin", color="000000")
MED = Side(style="medium", color="000000")


def write_tab(ws, title, elems, rects, total_rows):
    ws.sheet_view.showGridLines = False
    ws.sheet_view.zoomScale = 100
    ws.page_setup.orientation = "landscape"
    ws.page_setup.fitToWidth = 1
    ws.page_setup.fitToHeight = 0
    ws.sheet_properties.pageSetUpPr.fitToPage = True
    for c in range(1, TOTAL_COLS + 1):
        ws.column_dimensions[get_column_letter(c)].width = COL_CHARS
    for r in range(1, total_rows + 2):
        ws.row_dimensions[r].height = ROW_PT
    ws.cell(1, 1, title).font = Font(name=FONT, size=12, bold=True)

    sides = {}      # (r, c) -> {left,right,top,bottom: Side}
    fills = {}

    def mark(r, c, **kw):
        sides.setdefault((r, c), {}).update(kw)

    for (r0, c0, h, w) in rects:
        for rr in range(r0 + 1, r0 + h + 1):          # sang tọa độ 1-based của openpyxl
            mark(rr, c0 + 1, left=MED)
            mark(rr, c0 + w, right=MED)
        for cc in range(c0 + 1, c0 + w + 1):
            mark(r0 + 1, cc, top=MED)
            mark(r0 + h, cc, bottom=MED)

    for e in elems:
        r, c = e.r + 1, e.c + 1
        if e.rs * e.cs > 1:
            ws.merge_cells(start_row=r, start_column=c, end_row=r + e.rs - 1, end_column=c + e.cs - 1)
        cell = ws.cell(r, c, e.text if e.text else None)
        cell.font = Font(name=FONT, size=e.size, bold=e.bold, italic=e.italic, color=e.color,
                         underline="single" if e.underline else None)
        for rr in range(r, r + e.rs):
            for cc in range(c, c + e.cs):
                ws.cell(rr, cc).alignment = Alignment(horizontal=e.halign, vertical="center",
                                                      wrap_text=e.wrap, indent=0)
                if e.fill:
                    fills[(rr, cc)] = e.fill
                if e.border:
                    kw = {}
                    if cc == c:
                        kw["left"] = THIN
                    if cc == c + e.cs - 1:
                        kw["right"] = THIN
                    if rr == r:
                        kw["top"] = THIN
                    if rr == r + e.rs - 1:
                        kw["bottom"] = THIN
                    mark(rr, cc, **kw)
    for (rr, cc), kw in sides.items():
        ws.cell(rr, cc).border = Border(**kw)
    for (rr, cc), col in fills.items():
        ws.cell(rr, cc).fill = PatternFill("solid", start_color=col, end_color=col)


# --------------------------------------------------------------------------
# Tab Đối chiếu (UI-07)
# --------------------------------------------------------------------------
CHUNG, M1, M2 = "Chung", "Thêm món ăn mới", "Xem thống kê khách hàng"
MAIN, LIST, ADD, REP = "managerMainView4.jsp", "dishManagementView4.jsp", "addDishView4.jsp", "statisticsReportView4.jsp"
STT, SPD, DET, ODT = ("customerStatisticsView4.jsp", "customerSpendingView4.jsp", "orderDetailView4.jsp",
                      "orderDetailView4.jsp")
BS = "nút bổ sung theo UI-01"

COMP_HEAD = ["Module", "Trang", "Lớp GD", "Thuộc tính", "Loại", "Nhãn trên giao diện", "Bước v.2",
             "Đích hoặc xử lý"]
COMP = [
    (CHUNG, MAIN, "ManagerMainView4", "subManageDish", "liên kết", "Manage dish information", "1", LIST),
    (CHUNG, MAIN, "ManagerMainView4", "subStatisticsReport", "liên kết", "View statistics reports", "1", REP),
    (M1, LIST, "DishManagementView4", "outDishList", "bảng",
     "No. | Dish code | Dish name | Dish category | Sale price (VND) | Sale status", "6, 27", "Chỉ đọc; loại món lấy từ dishCategories, đối chiếu DishCategory4.dishes theo id món"),
    (M1, LIST, "DishManagementView4", "subAddDish", "nút", "Add dish", "7", ADD),
    (M1, LIST, "DishManagementView4", "subBackToMain", "nút", "Back to main page", "—",
     MAIN + " (" + BS + ")"),
    (M1, ADD, "AddDishView4", "inDishCode", "ô nhập", "Dish code", "12–14", "Gửi tới doSaveDish4.jsp khi Lưu"),
    (M1, ADD, "AddDishView4", "inDishName", "ô nhập", "Dish name", "12–14", "Gửi tới doSaveDish4.jsp khi Lưu"),
    (M1, ADD, "AddDishView4", "inDescription", "ô nhập", "Description", "12–14", "Gửi tới doSaveDish4.jsp khi Lưu"),
    (M1, ADD, "AddDishView4", "inSalePrice", "ô nhập", "Sale price (VND)", "12–14", "Gửi tới doSaveDish4.jsp khi Lưu"),
    (M1, ADD, "AddDishView4", "inSaleStatus", "ô chọn", "Sale status", "12–14",
     "Danh sách cố định; gửi tới doSaveDish4.jsp khi Lưu"),
    (M1, ADD, "AddDishView4", "inoutDishCategory", "ô chọn", "Dish category", "12–14",
     "Danh sách loại món ăn lấy từ hệ thống (getDishCategories); gửi id loại đã chọn tới doSaveDish4.jsp khi Lưu, truyền idDishCategory cho validateDish và saveDish"),
    (M1, ADD, "AddDishView4", "inImage", "ô tải tệp", "Image", "12–14",
     "Tùy chọn; gửi tới doSaveDish4.jsp khi Lưu"),
    (M1, ADD, "AddDishView4", "subSave", "nút", "Save", "15", "doSaveDish4.jsp"),
    (M1, ADD, "AddDishView4", "outMessage", "thông báo", "Message", "22", "Xem bảng Thông báo"),
    (M1, ADD, "AddDishView4", "subBackToDishList", "nút", "Back to dish list", "—",
     LIST + " (" + BS + "); không lưu dữ liệu đang nhập"),
    (M1, "doSaveDish4.jsp", "—", "—", "trang xử lý", "—", "16–23",
     "Kiểm tra và lưu món ăn (validateDish, saveDish). Thành công: thông báo ở outMessage của "
     + ADD + " rồi chuyển tới " + LIST + ". Không hợp lệ: trả về " + ADD + " cùng thông báo và dữ liệu đã nhập."),
    (M2, REP, "StatisticsReportView4", "outsubStatisticTypes", "bảng có chọn",
     "Dish statistics; Ingredient statistics; Customer statistics by revenue; Supplier statistics",
     "3–4", "Chọn Thống kê khách hàng theo doanh thu → " + STT + ". Ba loại còn lại ngoài phạm vi module."),
    (M2, REP, "StatisticsReportView4", "subBackToMain", "nút", "Back to main page", "—",
     MAIN + " (" + BS + ")"),
    (M2, STT, "CustomerStatisticsView4", "inStartDate", "ô nhập", "Start date", "6–7", "Gửi cùng Xem thống kê; nhận lại vào inStartDate khi quay lại từ " + SPD + " (giữ kỳ, bước 36)"),
    (M2, STT, "CustomerStatisticsView4", "inEndDate", "ô nhập", "End date", "6–7", "Gửi cùng Xem thống kê; nhận lại vào inEndDate khi quay lại từ " + SPD + " (giữ kỳ, bước 36)"),
    (M2, STT, "CustomerStatisticsView4", "subViewStatistics", "nút", "View statistics", "7",
     STT + " (kiểm tra khoảng ngày và lập lại danh sách)"),
    (M2, STT, "CustomerStatisticsView4", "outsubStatisticList", "bảng có chọn",
     "No. | Customer code | Display name | Total spending (VND)", "14–15",
     "Chọn Tên hiển thị → " + SPD + " (kèm khách hoặc nhóm No name, kỳ)"),
    (M2, STT, "CustomerStatisticsView4", "outMessage", "thông báo", "Message", "ngoại lệ 6, 7",
     "Xem bảng Thông báo"),
    (M2, STT, "CustomerStatisticsView4", "subBackToStatisticsReport", "nút", "Back to statistics reports", "—",
     REP + " (" + BS + ")"),
    (M2, SPD, "CustomerSpendingView4", "outCustomerCode", "thông tin", "Customer code", "20", "Chỉ đọc"),
    (M2, SPD, "CustomerSpendingView4", "outDisplayName", "thông tin", "Display name", "20", "Chỉ đọc"),
    (M2, SPD, "CustomerSpendingView4", "outPeriod", "thông tin", "Period", "20", "Chỉ đọc"),
    (M2, SPD, "CustomerSpendingView4", "outTotalSpending", "thông tin", "Total spending", "20",
     "Dòng tổng dưới bảng"),
    (M2, SPD, "CustomerSpendingView4", "outsubOrderList", "bảng có chọn",
     "No. | Order code | Paid at | Amount (VND)", "20–21",
     "Chọn Mã đơn → " + DET + " (kèm đơn, khách hoặc nhóm, kỳ)"),
    (M2, SPD, "CustomerSpendingView4", "subBackToStatisticList", "nút", "Back to statistic list",
     "34", STT + " (giữ kỳ)"),
    (M2, SPD, "CustomerSpendingView4", "outMessage", "thông báo", "Message", "ngoại lệ 8, 10",
     "Xem bảng Thông báo"),
    (M2, DET, "OrderDetailView4", "outOrderCode", "thông tin", "Order code", "29", "Chỉ đọc"),
    (M2, DET, "OrderDetailView4", "outCustomerInfo", "thông tin", "Customer", "29", "Chỉ đọc"),
    (M2, DET, "OrderDetailView4", "outPeriod", "thông tin", "Period", "29", "Chỉ đọc"),
    (M2, DET, "OrderDetailView4", "outInvoiceCode", "thông tin", "Invoice code", "29", "Chỉ đọc"),
    (M2, DET, "OrderDetailView4", "outPaidAt", "thông tin", "Paid at", "29", "Chỉ đọc"),
    (M2, DET, "OrderDetailView4", "outOrderLines", "bảng",
     "No. | Dish or combo | Quantity | Unit price (VND) | Line total (VND)", "29", "Chỉ đọc"),
    (M2, DET, "OrderDetailView4", "outTotalAmount", "thông tin", "Total amount", "29", "Dòng tổng dưới bảng"),
    (M2, DET, "OrderDetailView4", "subBackToOrderList", "nút", "Back to order list", "30",
     SPD + " (giữ khách hoặc nhóm, kỳ)"),
    (M2, DET, "OrderDetailView4", "outMessage", "thông báo", "Message", "ngoại lệ 10", "Xem bảng Thông báo"),
]
HIDDEN = [  # (trang, lớp, thuộc tính ẩn, ghi chú)
    ('managerMainView4.jsp', 'ManagerMainView4', 'managementStaff : ManagementStaff4', 'Nhân viên quản lý đăng nhập, lấy từ session'),
    ('dishManagementView4.jsp', 'DishManagementView4', 'dishes : Dish4[]', 'Dữ liệu của outDishList'),
    ('dishManagementView4.jsp', 'DishManagementView4', 'dishCategories : DishCategory4[]', 'Tên loại món từ getDishCategories(); đối chiếu DishCategory4.dishes theo id món'),
    ('dishManagementView4.jsp', 'DishManagementView4', 'managementStaff : ManagementStaff4', 'Nhân viên quản lý đăng nhập, lấy từ session'),
    ('addDishView4.jsp', 'AddDishView4', 'dishCategories : DishCategory4[]', 'Dữ liệu của inoutDishCategory'),
    ('addDishView4.jsp', 'AddDishView4', 'dish : Dish4', 'Món đã nhập; giữ khi không hợp lệ'),
    ('addDishView4.jsp', 'AddDishView4', 'managementStaff : ManagementStaff4', 'Nhân viên quản lý đăng nhập, lấy từ session'),
    ('doSaveDish4.jsp', '—', 'dish : Dish4', 'Món gửi từ addDishView4.jsp; trang xử lý không có người dùng đăng nhập'),
    ('statisticsReportView4.jsp', 'StatisticsReportView4', 'managementStaff : ManagementStaff4', 'Nhân viên quản lý đăng nhập, lấy từ session'),
    ('customerStatisticsView4.jsp', 'CustomerStatisticsView4', 'customerStatistics : CustomerStatistic4[]', 'Dữ liệu của outsubStatisticList'),
    ('customerStatisticsView4.jsp', 'CustomerStatisticsView4', 'managementStaff : ManagementStaff4', 'Nhân viên quản lý đăng nhập, lấy từ session'),
    ('customerSpendingView4.jsp', 'CustomerSpendingView4', 'customerStatistic : CustomerStatistic4', 'Khách hoặc No name (id = 0), tên hiển thị, kỳ và tổng chi tiêu'),
    ('customerSpendingView4.jsp', 'CustomerSpendingView4', 'orders : Order4[]', 'Dữ liệu của outsubOrderList'),
    ('customerSpendingView4.jsp', 'CustomerSpendingView4', 'managementStaff : ManagementStaff4', 'Nhân viên quản lý đăng nhập, lấy từ session'),
    ('orderDetailView4.jsp', 'OrderDetailView4', 'customerStatistic : CustomerStatistic4', 'Ngữ cảnh khách hoặc nhóm và kỳ, giữ để quay lại'),
    ('orderDetailView4.jsp', 'OrderDetailView4', 'order : Order4', 'Đơn được chọn kèm customer, invoice và invoice.payments'),
    ('orderDetailView4.jsp', 'OrderDetailView4', 'orderSubmissionProducts : OrderSubmissionProduct4[]', 'Dữ liệu của outOrderLines'),
    ('orderDetailView4.jsp', 'OrderDetailView4', 'managementStaff : ManagementStaff4', 'Nhân viên quản lý đăng nhập, lấy từ session'),
]
for pg, cl, nm, note in HIDDEN:
    module = CHUNG if pg == MAIN else M1 if pg in (LIST, ADD, "doSaveDish4.jsp") else M2
    COMP.append((module, pg, cl, "(ẩn) " + nm, "ẩn", "—", "—", note))

NOTE_HEAD = ["Trang", "Tình huống", "Nội dung", "Sau thông báo"]
NOTES = [
    (ADD, "Thêm món thành công (bước 22)", "Dish added successfully. (Thêm món ăn thành công.)",
     "Chuyển tới " + LIST + ", danh sách có món vừa thêm."),
    (ADD, "Mã món trùng, thiếu dữ liệu bắt buộc, giá bán không lớn hơn 0 hoặc loại món ăn không tồn tại (bước 7)",
     "Dish not saved. Each invalid field is shown: dish code duplicated or missing, dish name required, "
     "sale price must be greater than 0, dish category does not exist. (Không lưu món ăn; chỉ rõ trường lỗi.)",
     "Ở lại " + ADD + ", giữ dữ liệu đã nhập; nhân viên sửa rồi Lưu lại."),
    (ADD, "Không còn loại món ăn để lựa chọn (bước 7)",
     "Cannot save. Create a dish category before adding dishes. (Không cho lưu. Cần tạo loại món ăn trước khi thêm món.)",
     "Ở lại " + ADD + "; chỉ thêm món khi có ít nhất một loại món ăn."),
    (ADD, "Lưu thất bại (bước 8)", "The dish could not be created. (Chưa tạo được món ăn.)",
     "Ở lại " + ADD + "; không để lại bản ghi món dở dang."),
    (STT, "Ngày bắt đầu sau ngày kết thúc (bước 6)",
     "Start date must not be after end date. Please choose the period again. (Ngày bắt đầu phải không sau ngày kết thúc. Vui lòng chọn lại khoảng ngày.)",
     "Ở lại " + STT + ", giữ các ngày đã nhập; không lập báo cáo."),
    (STT, "Tổng hợp dữ liệu thất bại (bước 6)", "The report could not be generated. (Chưa thể lập báo cáo.)",
     "Ở lại " + STT + ", giữ khoảng ngày; không hiển thị số liệu dở dang."),
    (STT, "Không có hóa đơn hợp lệ trong kỳ — danh sách rỗng (bước 7)",
     "No paid invoices in the selected period. (Không có hóa đơn hợp lệ trong kỳ đã chọn.)",
     "Danh sách rỗng, giữ nguyên khoảng ngày; nhân viên chọn kỳ khác."),
    (SPD, "Khách hoặc nhóm đang chọn không còn phù hợp bộ lọc (bước 8)",
     "Data has changed: the selected customer or group no longer matches. (Dữ liệu đã thay đổi: khách hoặc nhóm đã chọn không còn phù hợp.)",
     "Quay về " + STT + " với danh sách tải lại theo kỳ hiện tại."),
    (DET, "Đơn không còn thuộc khách hoặc nhóm và kỳ đang xem, hoặc hóa đơn không còn hợp lệ (bước 10)",
     "Data has changed: the selected order is no longer valid. (Dữ liệu đã thay đổi: đơn đã chọn không còn hợp lệ.)",
     "Quay về " + SPD + " với danh sách đơn tải lại theo điều kiện hiện tại."),
]

HEAD_FILL = PatternFill("solid", start_color="D9D9D9", end_color="D9D9D9")


def write_table(ws, top, head, rows, spans, widths):
    """spans[j] = (cột đầu, cột cuối) 1-based; gộp ô khi span > 1 cột."""
    full = Border(left=THIN, right=THIN, top=THIN, bottom=THIN)
    f10 = pil_font(10)

    def put(r, j, v, bold=False, fill=None, valign="top"):
        a, b = spans[j]
        if b > a:
            ws.merge_cells(start_row=r, start_column=a, end_row=r, end_column=b)
        for cc in range(a, b + 1):
            cell = ws.cell(r, cc)
            cell.border = full
            if fill:
                cell.fill = fill
        c = ws.cell(r, a, v)
        c.font = Font(name=FONT, size=10, bold=bold)
        c.alignment = Alignment(wrap_text=True, vertical=valign)

    def nlines(v, j):
        a, b = spans[j]
        avail = sum(widths[a - 1:b]) * 7 - 6
        n, cur = 1, ""
        for w in str(v).split(" "):
            t = (cur + " " + w).strip()
            if f10.getlength(t) > avail and cur:
                n += 1
                cur = w
            else:
                cur = t
        return n

    for j, h in enumerate(head):
        put(top, j, h, bold=True, fill=HEAD_FILL, valign="center")
    for i, row in enumerate(rows):
        for j, v in enumerate(row):
            put(top + 1 + i, j, v)
        n = max(nlines(v, j) for j, v in enumerate(row))
        ws.row_dimensions[top + 1 + i].height = max(18, 13.5 * n + 4)
    return top + 1 + len(rows)


def write_recon(ws):
    ws.sheet_view.showGridLines = False
    widths = [22, 28, 28, 28, 14, 44, 14, 62]
    for j, w in enumerate(widths):
        ws.column_dimensions[get_column_letter(j + 1)].width = w
    bold = Font(name=FONT, size=12, bold=True)
    ws.cell(1, 1, "Đối chiếu thành phần giao diện với thuộc tính lớp GD").font = bold
    ws.cell(2, 1, "Bảng 1. Thành phần giao diện").font = Font(name=FONT, size=10, bold=True)
    end = write_table(ws, 3, COMP_HEAD, COMP, [(j, j) for j in range(1, 9)], widths)
    ws.cell(end + 1, 1, "Bảng 2. Thông báo").font = Font(name=FONT, size=10, bold=True)
    # bảng 2 dùng lại lưới cột của bảng 1 bằng ô gộp: Trang = A:B, Tình huống = C:D, Nội dung = E:G, Sau thông báo = H
    write_table(ws, end + 2, NOTE_HEAD, NOTES, [(1, 2), (3, 4), (5, 7), (8, 8)], widths)


# --------------------------------------------------------------------------
def main():
    wb = Workbook()
    wb.remove(wb.active)
    for tab in TABS:
        elems, rects, total = layout(tab["frames"])
        check_fit(elems)
        ws = wb.create_sheet(tab["name"])
        write_tab(ws, tab["title"], elems, rects, total)
        render_png(elems, rects, total, os.path.join(HERE, tab["png"]))
    ws = wb.create_sheet("Đối chiếu")
    write_recon(ws)
    out = os.path.join(HERE, "ui-design.xlsx")
    wb.save(out)
    print("OK", out)


if __name__ == "__main__":
    main()
