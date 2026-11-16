#!/usr/bin/env python3
"""[NGOÀI THIẾT KẾ] Kiểm tra hai chiều kịch bản v.3 ↔ mã ↔ thông điệp."""
import argparse
from collections import defaultdict, Counter
from pathlib import Path
import re
import sys

APP = Path(__file__).resolve().parents[1]
ROOT = APP.parents[1]

def collect(prefix=""):
    """Thu riêng mỗi namespace, không đếm tk-v3 thành v3 của module thêm món."""
    steps, branches, branch_steps = defaultdict(list), defaultdict(list), set()
    for file in sorted((APP / 'src/main').rglob('*')):
        if file.suffix not in ('.java', '.jsp', '.jspf'):
            continue
        for line_no, line in enumerate(file.read_text().splitlines(), 1):
            location = f'{file.relative_to(ROOT)}:{line_no}'
            for tag in re.findall(r'\[([^\]]+)\]', line):
                for number, branch in re.findall(rf'(?<![\w-]){re.escape(prefix)}v3-(\d+)(?:/(A1|A2|E1|E2a|E2b|E2|E3))?', tag):
                    (branches[branch] if branch else steps[int(number)]).append(location)
                    if branch: branch_steps.add((int(number), branch))
                if not prefix and tag in ('A1', 'A2', 'E1', 'E2'):
                    branches[tag].append(location)
    return steps, branches, branch_steps

def check_activity(activity, class_design):
    """Đối chiếu biểu đồ hoạt động với rule 09 và Hình 5: vùng, 13 hoạt động, 5 nút DAO4, 17 chuyển tiếp."""
    if not activity.exists():
        return [f'Không tìm thấy biểu đồ hoạt động: {activity}']
    if not class_design.exists():
        return [f'Không tìm thấy biểu đồ lớp: {class_design}']
    errors, text = [], activity.read_text()
    pages = {
        'CHINH': 'managerMainView4.jsp', 'QLMA': 'dishManagementView4.jsp',
        'THEM': 'addDishView4.jsp', 'LUU': 'doSaveDish4.jsp',
    }
    activities = {
        'CHINH_NhanQL': 'Get manager info', 'CHINH_HienGD': 'Show UI',
        'QLMA_NhanQL': 'Get manager info', 'QLMA_LayMon': 'Get dish list',
        'QLMA_LayLoai': 'Get dish category list', 'QLMA_HienGD': 'Show UI',
        'THEM_NhanQL': 'Get manager info', 'THEM_LayLoai': 'Get dish category list',
        'THEM_HienGD': 'Show UI', 'THEM_TB': 'Show success message',
        'LUU_NhanMon': 'Get dish info', 'LUU_KiemTra': 'Validate dish info',
        'LUU_Luu': 'Save dish',
    }
    decisions = set()  # rule 09 HD-04: không dùng nút quyết định
    declarations, owners, groups = {}, {}, {}
    owner = None
    for line in text.splitlines():
        line = line.strip()
        if line == '}':
            owner = None
        match = re.match(r'(rectangle|diamond|circle|label)\s+"([^"]*)"\s+as\s+(\w+)', line)
        if not match:
            diamond = re.match(r'<>\s+(\w+)\s*$', line)
            if diamond:
                alias = diamond.group(1)
                if alias in declarations: errors.append(f'Alias trùng: {alias}')
                declarations[alias] = ('diamond', '')
                if owner: owners[alias] = owner
            continue
        shape, label, alias = match.groups()
        if alias in declarations: errors.append(f'Alias trùng: {alias}')
        declarations[alias] = (shape, label)
        if line.endswith('{'):
            groups[alias] = label
            owner = alias
        elif owner:
            owners[alias] = owner
    if groups != {alias: f'Process at {page}' for alias, page in pages.items()}:
        errors.append('Phải có đúng bốn vùng JSP, đúng tên và alias')
    actual_activities = {alias: declarations[alias][1] for alias in owners if declarations[alias][0] == 'rectangle'}
    if actual_activities != activities: errors.append('Danh sách 13 hoạt động khác rule 09 HD-02')
    for alias in activities:
        if owners.get(alias) != alias.split('_')[0]: errors.append(f'Hoạt động sai vùng: {alias}')
    if {alias for alias, (shape, _) in declarations.items() if shape == 'diamond'} != decisions:
        errors.append('Không dùng nút quyết định (rule 09 HD-04)')
    for alias in decisions:
        if owners.get(alias) != alias.split('_')[0]: errors.append(f'Quyết định sai vùng: {alias}')
    if declarations.get('Start') != ('label', r'Start\n<size:20>●</size>') or declarations.get('End') != ('label', r'<size:22>◉</size>\nEnd'):
        errors.append('Phải có đúng một Bắt đầu ● và một Kết thúc ◉, nhãn có dấu')

    designed = set()
    class_text = class_design.read_text()
    for cls, body in re.findall(r'class\s+(\w+DAO4)\s*\{(.*?)\}', class_text, re.S):
        designed |= {(cls, m) for m in re.findall(r'\+(\w+)\(', body) if m != cls}
    dao_nodes = {}
    for alias, (_, label) in declarations.items():
        match = re.fullmatch(r'(\w+DAO4):\\n(\w+)\(\)', label)
        if match:
            dao_nodes[alias] = match.groups()
            if alias in owners: errors.append(f'Nút DAO4 phải ngoài vùng: {alias}')
    expected_aliases = set(pages) | set(activities) | decisions | set(dao_nodes) | {'Start', 'End'}
    if set(declarations) != expected_aliases: errors.append('Có phần tử ngoài thiết kế hoặc thiếu phần tử')
    nodes = set(dao_nodes.values())
    if nodes != designed: errors.append(f'Lệch phương thức DAO4 ↔ Hình 5: {sorted(nodes ^ designed)}')
    if Counter(dao_nodes.values()) != Counter({('DishDAO4', 'getDishes'): 1, ('DishCategoryDAO4', 'getDishCategories'): 2,
                                               ('DishDAO4', 'validateDish'): 1, ('DishDAO4', 'saveDish'): 1}):
        errors.append('Phải có đúng năm nút DAO4, getDishCategories xuất hiện hai lần')
    for cls, method in nodes:
        source = APP / f'src/main/java/dao/{cls}.java'
        if not source.exists() or not re.search(rf'public\s+[\w\[\]]+\s+{method}\s*\(', source.read_text()):
            errors.append(f'Nút {cls}.{method}() không có trong mã')

    edges, dao_edges = [], []
    for line in text.splitlines():
        match = re.match(r'^\s*(\w+)\s+(<?-[^\s]*[->])\s+(\w+)(?:\s*:\s*(.+))?\s*$', line)
        if not match:
            continue
        a, arrow, b, guard = match.groups()
        if '[hidden]' in arrow: continue
        guard = re.sub(r'</?back(?::white)?>', '', (guard or '')).replace(r'\n', ' ').strip()
        if a not in declarations or b not in declarations: errors.append(f'Đường nối tới alias chưa khai báo: {a}, {b}')
        if a in dao_nodes or b in dao_nodes:
            if not (arrow.startswith('<') and arrow.endswith('>')) or guard:
                errors.append(f'Đường DAO4 phải hai đầu mũi tên, không nhãn: {a} {arrow} {b}')
            dao_edges.append((b, a) if a in dao_nodes else (a, b))
        else:
            if arrow.startswith('<') or not arrow.endswith('>'): errors.append(f'Chuyển tiếp phải một chiều: {a} {arrow} {b}')
            if guard and not re.fullmatch(r'\[[^\[\]]+\]', guard): errors.append(f'Guard phải trong ngoặc vuông: {guard}')
            edges.append((a, b, guard))
    expected_calls = {
        'QLMA_LayMon': 'DAO_getDishes', 'QLMA_LayLoai': 'DAO_getDishCategories_QLMA',
        'THEM_LayLoai': 'DAO_getDishCategories_THEM', 'LUU_KiemTra': 'DAO_validateDish', 'LUU_Luu': 'DAO_saveDish',
    }
    if Counter(dao_edges) != Counter(expected_calls.items()): errors.append('Liên kết hoạt động–DAO4 thiếu, trùng hoặc sai đích')
    # Đường gọi DAO4 phải tồn tại trên đúng trang JSP, không chỉ trong lớp Java.
    for action, node in expected_calls.items():
        if node not in dao_nodes: continue
        cls, method = dao_nodes[node]
        source = APP / 'src/main/webapp/view/ManagementStaff' / pages[action.split('_')[0]]
        page_text = source.read_text()
        variables = re.findall(rf'\b(\w+)\s*=\s*new\s+{cls}\s*\(', page_text)
        if not any(re.search(rf'\b{variable}\.{method}\s*\(', page_text) for variable in variables):
            errors.append(f'Trang {source.name} không gọi {cls}.{method}()')
    expected_edges = [  # rule 09 HD-04, đúng 17 chuyển tiếp
        ('Start', 'CHINH_NhanQL', ''), ('CHINH_NhanQL', 'CHINH_HienGD', ''),
        ('CHINH_HienGD', 'QLMA_NhanQL', '[click Manage dish information]'),
        ('QLMA_NhanQL', 'QLMA_LayMon', ''),
        ('QLMA_LayMon', 'QLMA_LayLoai', '[data loaded]'),
        ('QLMA_LayLoai', 'QLMA_HienGD', '[data loaded]'),
        ('QLMA_HienGD', 'THEM_NhanQL', '[click Add dish]'),
        ('QLMA_HienGD', 'End', '[new dish added]'),
        ('THEM_NhanQL', 'THEM_LayLoai', ''),
        ('THEM_LayLoai', 'THEM_HienGD', '[data loaded]'),
        ('THEM_HienGD', 'LUU_NhanMon', '[click Save]'),
        ('LUU_NhanMon', 'LUU_KiemTra', ''),
        ('LUU_KiemTra', 'LUU_Luu', '[valid]'),
        ('LUU_KiemTra', 'THEM_HienGD', '[invalid]'),
        ('LUU_Luu', 'THEM_TB', '[saved]'),
        ('LUU_Luu', 'THEM_HienGD', '[save failed]'),
        ('THEM_TB', 'QLMA_NhanQL', '[click OK]'),
    ]
    if Counter(edges) != Counter(expected_edges): errors.append('Chuyển tiếp/guard thiếu, trùng hoặc khác rule 09 HD-04')
    page_links = set()
    page_names = {
        alias: label[:-4]
        for label, alias in re.findall(r'class\s+"([^"]+\.jsp)"\s+as\s+(\w+)', class_text)
    }
    for a, arrow, b in re.findall(r'^\s*(gd\w+|doLuuMonAn)\s+(-[^\s]+-)\s+(gd\w+|doLuuMonAn)\s*$', class_text, re.M):
        if '[hidden]' not in arrow:
            page_links.add(frozenset((page_names.get(a, a), page_names.get(b, b))))
    actual_links = {frozenset((pages[owners[a]][:-4], pages[owners[b]][:-4]))
                    for a, b, _ in edges if a in owners and b in owners and owners[a] != owners[b]}
    if actual_links != page_links: errors.append('Chuyển trang hoạt động không khớp đường trang–trang Hình 5')
    return errors

def check_sequence(diagram, steps, count=58):
    """Luồng chuẩn gồm đúng count thông điệp N: …, khớp hai chiều với tag mã."""
    if not diagram.exists():
        return [f'Không tìm thấy sơ đồ: {diagram}']
    # Comment PlantUML không phải thông điệp hay khung tương tác.
    text = '\n'.join(line for line in diagram.read_text().splitlines()
                     if not line.lstrip().startswith("'"))
    errors = []
    if re.search(r'^\s*(?:alt|opt|loop|note|autonumber)\b|^\s*(?:==|\.\.\.)', text, re.M):
        errors.append('Biểu đồ tuần tự chỉ có luồng chuẩn, không dùng alt/opt/loop, note hay autonumber')
    messages = re.findall(r'^\s*(\w+)\s*(--?>)\s*(\w+)\s*:\s*(\d+):\s*(.+?)\s*$', text, re.M)
    numbers = [int(message[3]) for message in messages]
    counts = Counter(numbers)
    if set(counts) != set(steps):
        errors.append(f'Lệch mã ↔ sơ đồ: mã thiếu {sorted(set(counts)-steps.keys())}, sơ đồ thiếu {sorted(steps.keys()-set(counts))}')
    if any(count != 1 for count in counts.values()):
        errors.append('Mỗi bước chính phải có đúng một thông điệp')
    if numbers != list(range(1, count+1)):
        errors.append(f'Thông điệp phải tăng liên tục từ 1 đến {count}, nhãn dạng N: …')
    arrows = re.findall(r'^\s*\w+\s*--?>\s*\w+\s*:', text, re.M)
    if len(arrows) != len(messages):
        errors.append('Có thông điệp không mang đúng số câu v.3')
    return errors

def check_statistics_activity(activity, class_design):
    """Đối chiếu module 2: 5 vùng, 15 hoạt động, 5 DAO4, 19 chuyển tiếp (rule 09 B)."""
    if not activity.exists():
        return []  # Biểu đồ module 2 được đối chiếu khi đã có.
    if not class_design.exists():
        return [f'Không tìm thấy biểu đồ lớp: {class_design}']
    text, errors = activity.read_text(), []
    pages = {'CHINH': 'managerMainView4.jsp', 'BC': 'statisticsReportView4.jsp', 'TK': 'customerStatisticsView4.jsp', 'CT': 'customerSpendingView4.jsp', 'DH': 'orderDetailView4.jsp'}
    activities = {'CHINH_NhanQL': 'Get manager info', 'CHINH_HienGD': 'Show UI', 'BC_NhanQL': 'Get manager info', 'BC_HienGD': 'Show UI', 'TK_NhanQL': 'Get manager info', 'TK_KiemTra': 'Validate period', 'TK_LayTK': 'Get customer statistics', 'TK_HienGD': 'Show UI', 'CT_NhanQL': 'Get manager info', 'CT_LayDon': 'Get order list', 'CT_HienGD': 'Show UI', 'DH_NhanQL': 'Get manager info', 'DH_LayDon': 'Get order info', 'DH_LayDong': 'Get order lines', 'DH_HienGD': 'Show UI'}
    calls = {'TK_KiemTra': ('CustomerStatisticDAO4', 'validatePeriod'), 'TK_LayTK': ('CustomerStatisticDAO4', 'getCustomerStatistics'), 'CT_LayDon': ('OrderDAO4', 'getOrders'), 'DH_LayDon': ('OrderDAO4', 'getOrder'), 'DH_LayDong': ('OrderSubmissionProductDAO4', 'getOrderSubmissionProducts')}
    expected_edges = [('Start', 'CHINH_NhanQL', ''), ('CHINH_NhanQL', 'CHINH_HienGD', ''), ('CHINH_HienGD', 'BC_NhanQL', 'click View statistics reports'), ('BC_NhanQL', 'BC_HienGD', ''), ('BC_HienGD', 'TK_NhanQL', 'click Customer statistics by revenue'), ('TK_NhanQL', 'TK_HienGD', 'no period chosen'), ('TK_NhanQL', 'TK_KiemTra', 'period chosen'), ('TK_KiemTra', 'TK_LayTK', 'valid'), ('TK_KiemTra', 'TK_HienGD', 'invalid'), ('TK_LayTK', 'TK_HienGD', 'data loaded'), ('TK_HienGD', 'TK_NhanQL', 'click View statistics'), ('TK_HienGD', 'CT_NhanQL', 'click 1 customer'), ('CT_NhanQL', 'CT_LayDon', ''), ('CT_LayDon', 'CT_HienGD', 'data loaded'), ('CT_HienGD', 'DH_NhanQL', 'click 1 order'), ('DH_NhanQL', 'DH_LayDon', ''), ('DH_LayDon', 'DH_LayDong', 'data loaded'), ('DH_LayDong', 'DH_HienGD', 'data loaded'), ('DH_HienGD', 'End', 'done viewing')]

    groups, nodes, owners = {}, {}, {}
    owner = None
    for line in text.splitlines():
        if line.strip() == '}': owner = None
        match = re.match(r'\s*(rectangle|label) "([^"]*)" as (\w+)', line)
        if not match: continue
        shape, label, alias = match.groups()
        if alias in nodes or alias in groups: errors.append(f'Alias trùng: {alias}')
        if line.rstrip().endswith('{'):
            groups[alias] = label
            owner = alias
        else:
            nodes[alias] = (shape, label)
            if owner: owners[alias] = owner
    if groups != {alias: f'Process at {page}' for alias, page in pages.items()}:
        errors.append('Module 2 phải có đúng năm vùng JSP')
    if {alias: nodes[alias][1] for alias in owners} != activities:
        errors.append('Module 2 phải có đúng 15 hoạt động')
    if any(owners.get(alias) != alias.split('_')[0] for alias in activities):
        errors.append('Hoạt động module 2 sai vùng')
    dao_nodes = {'DAO_'+method: (cls, method) for cls, method in calls.values()}
    for alias, (cls, method) in dao_nodes.items():
        if nodes.get(alias) != ('rectangle', cls+':\\n'+method+'()') or alias in owners:
            errors.append(f'Sai nút DAO4 module 2: {alias}')
        source = APP / f'src/main/java/dao/{cls}.java'
        if not source.exists() or not re.search(rf'public\s+[\w\[\]]+\s+{method}\s*\(', source.read_text()):
            errors.append(f'Không có phương thức {cls}.{method} trong mã')
    designed = set()
    for cls, body in re.findall(r'class\s+(\w+DAO4)\s*\{(.*?)\}', class_design.read_text(), re.S):
        designed |= {(cls, method) for method in re.findall(r'\+(\w+)\(', body) if method != cls}
    if set(dao_nodes.values()) != designed: errors.append('Nút DAO4 module 2 khác Hình 6')
    if set(nodes) != set(activities) | set(dao_nodes) | {'Start','End'}:
        errors.append('Module 2 có phần tử thừa hoặc thiếu')
    if nodes.get('Start') != ('label', r'Start\n<size:20>●</size>') or nodes.get('End') != ('label', r'<size:22>◉</size>\nEnd'):
        errors.append('Module 2 thiếu bắt đầu/kết thúc')
    edges, dao_edges = [], []
    for line in text.splitlines():
        match = re.match(r'^\s*(\w+)\s+(<?-[^\s]*[->])\s+(\w+)(?:\s*:\s*(.+))?\s*$', line)
        if not match: continue
        a, arrow, b, guard = match.groups()
        if '[hidden]' in arrow: continue
        guard = re.sub(r'</?back(?::white)?>', '', (guard or '')).replace(r'\n', ' ').strip()
        if a in dao_nodes or b in dao_nodes:
            if not arrow.startswith('<') or not arrow.endswith('>') or guard:
                errors.append('Module 2: đường DAO4 phải hai đầu, không nhãn')
            dao_edges.append((b, a) if a in dao_nodes else (a, b))
        else:
            if arrow.startswith('<') and not arrow.endswith('>'): a, b = b, a  # HD-B6: khai báo ngược X <-[norank]- Y
            elif arrow.startswith('<'): errors.append('Chuyển tiếp module 2 phải một chiều')
            edges.append((a,b,guard[1:-1] if guard.startswith('[') and guard.endswith(']') else guard))
    if Counter(edges) != Counter(expected_edges): errors.append('Module 2: sai 19 chuyển tiếp/guard')
    if Counter(dao_edges) != Counter((action,'DAO_'+method) for action,(_,method) in calls.items()):
        errors.append('Module 2: sai liên kết hoạt động–DAO4')
    for action,(cls,method) in calls.items():
        source = APP / 'src/main/webapp/view/ManagementStaff' / pages[action.split('_')[0]]
        variables = re.findall(rf'\b(\w+)\s*=\s*new\s+{cls}\s*\(', source.read_text())
        if not any(re.search(rf'\b{variable}\.{method}\s*\(',source.read_text()) for variable in variables):
            errors.append(f'Trang {source.name} không gọi {cls}.{method}()')
    return errors

def print_table(steps, branches, title):
    print(f'**{title}**\n')
    print('| Bước / nhánh | Vị trí mã (file:dòng) |\n| --- | --- |')
    for tag in sorted(steps): print(f'| {tag} | ' + '<br>'.join(f'`{p}`' for p in dict.fromkeys(steps[tag])) + ' |')
    for tag in sorted(branches): print(f'| {tag} | ' + '<br>'.join(f'`{p}`' for p in dict.fromkeys(branches[tag])) + ' |')

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--diagram', type=Path, default=ROOT / 'diagrams/sequence-add-dish.puml')
    parser.add_argument('--activity', type=Path, default=ROOT / 'diagrams/activity-add-dish.puml')
    parser.add_argument('--class-design', type=Path, default=ROOT / 'diagrams/class-design-add-dish.puml')
    parser.add_argument('--module', choices=('all', 'dish', 'statistics'), default='all')
    args = parser.parse_args()
    steps, branches, branch_steps = collect()
    errors = []
    expected = set(range(1, 59))
    if expected - steps.keys(): errors.append(f'Thiếu bước: {sorted(expected - steps.keys())}')
    if steps.keys() - expected: errors.append(f'Tag ngoài kịch bản: {sorted(steps.keys() - expected)}')
    for branch in ('A1', 'A2', 'E1', 'E2'):
        if branch not in branches: errors.append(f'Thiếu nhánh {branch}')
    expected_branches = {(24, "A1"), (32, "A2"), (32, "E1"), (35, "E2")}
    if branch_steps != expected_branches: errors.append("Sai điểm rẽ của nhánh")
    errors.extend(check_sequence(args.diagram, steps))
    errors.extend(check_activity(args.activity, args.class_design))
    if args.module == 'statistics': errors = []
    if args.module != 'statistics':
        print_table(steps, branches, 'Bảng truy vết kịch bản v.3 → mã')
    if args.module != 'dish':
        statistics_steps, statistics_branches, statistics_branch_steps = collect('tk-')
        expected = set(range(1,49))
        if set(statistics_steps) != expected:
            errors.append(f'Module 2: thiếu {sorted(expected-statistics_steps.keys())}, thừa {sorted(statistics_steps.keys()-expected)}')
        if statistics_branch_steps != {(11,'E1'),(17,'A1'),(17,'E3'),(19,'A2'),(26,'E2a'),(38,'E2b'),(47,'E2b')}:
            errors.append('Module 2: sai bảy điểm rẽ')
        diagram = ROOT / 'diagrams/sequence-customer-statistics.puml'
        if diagram.exists(): errors.extend(check_sequence(diagram,statistics_steps,48))
        errors.extend(check_statistics_activity(ROOT / 'diagrams/activity-customer-statistics.puml', ROOT / 'diagrams/class-design-customer-statistics.puml'))
        print_table(statistics_steps,statistics_branches,'Bảng truy vết kịch bản tk-v.3 → mã')
    if errors:
        print('\n'.join(errors), file=sys.stderr)
        return 1
    print('\nTruy vết đạt: '+('; '.join(label for name,label in [('dish','module 1: 58 bước/4 điểm rẽ'),('statistics','module 2: 48 bước/7 điểm rẽ')] if args.module in ('all',name)))+'; biểu đồ hiện có khớp mã.', file=sys.stderr)
    return 0

if __name__ == '__main__':
    raise SystemExit(main())
