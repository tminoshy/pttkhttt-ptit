"""Kiểm tra truy vết bằng các lỗi sơ đồ có thể xuất hiện khi chỉnh sửa."""
from pathlib import Path
import importlib.util
import re
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location('restaurant_trace', Path(__file__).with_name('trace.py'))
TRACE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(TRACE)


class ActivityTraceTest(unittest.TestCase):
    def setUp(self):
        self.activity = TRACE.ROOT / 'diagrams/activity-add-dish.puml'
        self.design = TRACE.ROOT / 'diagrams/class-design-add-dish.puml'
        self.source = self.activity.read_text()

    def check_modified(self, text):
        with tempfile.TemporaryDirectory() as directory:
            file = Path(directory) / 'activity.puml'
            file.write_text(text)
            return TRACE.check_activity(file, self.design)

    def test_current_activity_matches(self):
        self.assertEqual([], self.check_modified(self.source))

    def test_rejects_semantic_regressions(self):
        mutations = {
            'missing category node': self.source.replace('rectangle "DishCategoryDAO4:\\ngetDishCategories()" as DAO_getDishCategories_THEM', ''),
            'wrong DAO4 endpoint': self.source.replace('QLMA_LayMon <-[norank]-> DAO_getDishes', 'QLMA_LayMon <-[norank]-> DAO_saveDish'),
            'one headed DAO4 link': self.source.replace('QLMA_LayMon <-[norank]->', 'QLMA_LayMon -[norank]->'),
            'wrong guard': self.source.replace('[invalid]', '[wrong data]'),
            'added decision': self.source.replace('    rectangle "Save dish" as LUU_Luu', '    rectangle "Save dish" as LUU_Luu\n    <> LUU_Chon'),
            'renamed activity': self.source.replace('"Validate dish info"', '"Check dish"'),
            'wrong page': self.source.replace('Process at doSaveDish4.jsp', 'Process at save.jsp'),
            'save directly to list': self.source.replace('LUU_Luu -[norank]-> THEM_TB : [saved]', 'LUU_Luu -[norank]-> QLMA_NhanQL : [saved]'),
            'duplicate transition': self.source.replace('@enduml', 'LUU_Luu -[norank]-> THEM_HienGD : [save failed]\n@enduml'),
            'extra activity': self.source.replace('@enduml', 'rectangle "Log in" as Extra\n@enduml'),
            'back button drawn': self.source.replace('@enduml', 'THEM_HienGD -[norank]-> QLMA_NhanQL : [click Back to dish list]\n@enduml'),
        }
        for name, source in mutations.items():
            with self.subTest(name=name):
                self.assertNotEqual(self.source, source)
                self.assertTrue(self.check_modified(source), name)

    def test_rejects_moved_activity(self):
        line = '    rectangle "Show success message" as THEM_TB'
        moved = self.source.replace(line, '').replace(
            'rectangle "Process at doSaveDish4.jsp" as LUU {',
            'rectangle "Process at doSaveDish4.jsp" as LUU {\n' + line)
        self.assertTrue(self.check_modified(moved))

    def test_v3_tags_remain_complete(self):
        steps, branches, branch_steps = TRACE.collect()
        self.assertEqual(set(range(1, 59)), set(steps))
        self.assertEqual({'A1', 'A2', 'E1', 'E2'}, set(branches))
        self.assertEqual({(24, 'A1'), (32, 'A2'), (32, 'E1'), (35, 'E2')}, branch_steps)


class SequenceTraceTest(unittest.TestCase):
    def setUp(self):
        self.diagram = TRACE.ROOT / 'diagrams/sequence-add-dish.puml'
        self.source = self.diagram.read_text()
        self.steps, _, _ = TRACE.collect()

    def check_modified(self, text, steps=None):
        with tempfile.TemporaryDirectory() as directory:
            file = Path(directory) / 'sequence.puml'
            file.write_text(text)
            return TRACE.check_sequence(file, self.steps if steps is None else steps)

    def test_current_sequence_matches(self):
        self.assertEqual([], self.check_modified(self.source))

    def test_rejects_numbering_regressions(self):
        line = 'NV -> Add : 44: click OK'
        mutations = {
            'missing message': self.source.replace(line, ''),
            'duplicate message': self.source.replace(line, line + '\n' + line),
            'wrong number': self.source.replace('44: click OK', '59: click OK'),
            'old dot separator': self.source.replace('44: click OK', '44. click OK'),
            'branch in diagram': self.source.replace('44: click OK', '24/A1: cannot save'),
            'out of order': self.source.replace('43: show success message', '44: show success message').replace('44: click OK', '43: click OK'),
            'old 51 steps': '\n'.join(line for line in self.source.splitlines()
                                      if not re.search(r': 5[2-8]:', line)),
        }
        for name, source in mutations.items():
            with self.subTest(name=name):
                self.assertNotEqual(self.source, source)
                self.assertTrue(self.check_modified(source))

    def test_rejects_nonstandard_frames(self):
        for frame in ('alt', 'opt', 'loop'):
            with self.subTest(frame=frame):
                self.assertTrue(self.check_modified(self.source.replace('@enduml', f'{frame} nhánh\nend\n@enduml')))

    def test_checks_code_tags_in_both_directions(self):
        missing = dict(self.steps)
        missing.pop(44)
        extra = dict(self.steps)
        extra[59] = ['unexpected tag']
        for steps in (missing, extra):
            with self.subTest(steps=sorted(steps)):
                self.assertTrue(any('Lệch mã ↔ sơ đồ' in error
                                    for error in self.check_modified(self.source, steps)))

    def test_missing_diagram(self):
        with tempfile.TemporaryDirectory() as directory:
            self.assertTrue(TRACE.check_sequence(Path(directory) / 'missing.puml', self.steps))


class StatisticsTraceTest(unittest.TestCase):
    def setUp(self):
        self.sequence=TRACE.ROOT / 'diagrams/sequence-customer-statistics.puml'
        self.activity=TRACE.ROOT / 'diagrams/activity-customer-statistics.puml'
        self.design=TRACE.ROOT / 'diagrams/class-design-customer-statistics.puml'

    def test_namespace_isolation_and_long_branch_names(self):
        with tempfile.TemporaryDirectory() as directory:
            app=Path(directory)
            source=app / 'src/main/java/Tags.java';source.parent.mkdir(parents=True)
            source.write_text('// [v3-1][tk-v3-2] separate modules\n// [tk-v3-38/E2b][tk-v3-26/E2a][tk-v3-17/E3]\n')
            with patch.object(TRACE,'APP',app), patch.object(TRACE,'ROOT',app):
                dish,_,dish_branches=TRACE.collect()
                statistics,branches,points=TRACE.collect('tk-')
            self.assertEqual({1},set(dish));self.assertFalse(dish_branches)
            self.assertEqual({2},set(statistics))
            self.assertEqual({'E2a','E2b','E3'},set(branches))
            self.assertEqual({(38,'E2b'),(26,'E2a'),(17,'E3')},points)

    def test_current_statistics_tags_and_branches(self):
        steps,branches,points=TRACE.collect('tk-')
        self.assertEqual(set(range(1,49)),set(steps))
        self.assertEqual({'E1','A1','E3','A2','E2a','E2b'},set(branches))
        self.assertEqual({(11,'E1'),(17,'A1'),(17,'E3'),(19,'A2'),(26,'E2a'),(38,'E2b'),(47,'E2b')},points)
        self.assertEqual([],TRACE.check_sequence(self.sequence,steps,48))

    def test_statistics_sequence_uses_48_messages(self):
        steps,_,_=TRACE.collect('tk-')
        with tempfile.TemporaryDirectory() as directory:
            file=Path(directory)/'sequence.puml'
            for source in [self.sequence.read_text().replace('47: return','49: return'),
                           self.sequence.read_text().replace('@enduml','loop repeat\nend\n@enduml')]:
                file.write_text(source)
                self.assertTrue(TRACE.check_sequence(file,steps,48))

    def test_current_statistics_activity(self):
        self.assertEqual([],TRACE.check_statistics_activity(self.activity,self.design))

    def test_rejects_statistics_activity_regressions(self):
        source=self.activity.read_text()
        mutations=[source.replace('Get customer statistics','Get statistics'),
                   source.replace('TK_KiemTra <-[norank]-> DAO_validatePeriod','TK_KiemTra -[norank]-> DAO_validatePeriod'),
                   source.replace('[no period chosen]','[no period]'),
                   source.replace('CT_LayDon <-[norank]-> DAO_getOrders','CT_LayDon <-[norank]-> DAO_getOrder'),
                   source.replace('Process at orderDetailView4.jsp','Process at other.jsp')]
        with tempfile.TemporaryDirectory() as directory:
            file=Path(directory)/'activity.puml'
            for text in mutations:
                self.assertNotEqual(source,text);file.write_text(text)
                self.assertTrue(TRACE.check_statistics_activity(file,self.design))


if __name__ == '__main__':
    unittest.main()
