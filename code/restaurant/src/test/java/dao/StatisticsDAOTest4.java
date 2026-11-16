/* ===================================================================
 LỚP      : StatisticsDAOTest4 (kiểm thử ngoài biểu đồ thiết kế)
 THIẾT KẾ : [NGOÀI THIẾT KẾ] Kiểm thử ba DAO4 thống kê, không có hộp trên Hình 6.
 PHÂN TÍCH: Không có lớp phân tích riêng, kiểm chứng các hành vi DAO4 theo kịch bản.
 QUAN HỆ  : Các tham chiếu và kế thừa theo Hình 6.
 CSDL     : restaurant_test, các bảng thống kê.
 KỊCH BẢN : module 2, không thuộc luồng trực tiếp, kiểm tra 1–48.
 NGOẠI LỆ : E1, A1, E3, A2, E2a, E2b.
 =================================================================== */
package dao;
import model.*;
import java.sql.Date;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatisticsDAOTest4 extends DatabaseTest4 {
    Date start=Date.valueOf("2026-08-01"),end=Date.valueOf("2026-08-31");
    int customer(String code) throws Exception { return count("SELECT id FROM tblCustomer4 WHERE customerCode='"+code+"'"); }
    int order(String code) throws Exception { return count("SELECT id FROM tblOrder4 WHERE orderCode='"+code+"'"); }
    void update(String sql) throws Exception { synchronized(DAO4.class) { try(var s=DAO4.con.createStatement()) {s.executeUpdate(sql);} } }

    @Test void validAndInvalidPeriods() {
        CustomerStatisticDAO4 dao=new CustomerStatisticDAO4();
        assertTrue(dao.validatePeriod(start,end)); assertTrue(dao.validatePeriod(start,start));
        assertFalse(dao.validatePeriod(end,start));assertFalse(dao.validatePeriod(null,end));assertFalse(dao.validatePeriod(start,null));
    }
    @Test void augustTotalsAreRankedAndNoNameHasNoFakeCode() {
        CustomerStatistic4[] rows=new CustomerStatisticDAO4().getCustomerStatistics(start,end);
        assertEquals(3,rows.length);
        assertArrayEquals(new String[]{"KH001","KH002",""},Arrays.stream(rows).map(CustomerStatistic4::getCustomerCode).toArray(String[]::new));
        assertEquals(4250000,rows[0].getTotalSpending());assertEquals(2180000,rows[1].getTotalSpending());assertEquals(1350000,rows[2].getTotalSpending());
        assertEquals(0,rows[2].getId());assertEquals("No name",rows[2].getFullName());
        assertEquals(start,rows[0].getPeriodStart());assertEquals(end,rows[0].getPeriodEnd());
        assertEquals(0,rows[0].getMembershipCards().length);
    }
    @Test void emptyPeriodDoesNotInventNoName() {
        assertEquals(0,new CustomerStatisticDAO4().getCustomerStatistics(Date.valueOf("2027-01-01"),Date.valueOf("2027-01-02")).length);
    }
    @Test void failedAggregationReturnsNull() throws Exception {
        CustomerStatisticDAO4 dao=new CustomerStatisticDAO4();
        update("RENAME TABLE tblPayment4 TO unavailablePayment");
        try { assertNull(dao.getCustomerStatistics(start,end)); }
        finally { update("RENAME TABLE unavailablePayment TO tblPayment4"); }
    }
    @Test void boundariesIncludeFirstAndLastDayButExcludeOutside() throws Exception {
        OrderDAO4 dao=new OrderDAO4();
        assertEquals("DH301",dao.getOrders(0,start,start)[0].getOrderCode());
        assertEquals("DH202",dao.getOrders(customer("KH002"),end,end)[0].getOrderCode());
        assertNull(dao.getOrder(order("DH106"),customer("KH001"),start,end));
        assertNull(dao.getOrder(order("DH203"),customer("KH002"),start,end));
    }
    @Test void firstSuccessfulPaymentControlsPeriodAndInvoiceCountsOnce() throws Exception {
        update("INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT amountDue,'Tiền mặt','2026-09-01','Thành công',id,tblStaff4id FROM tblInvoice4 WHERE invoiceCode='HD103'");
        CustomerStatistic4[] rows=new CustomerStatisticDAO4().getCustomerStatistics(start,end);
        assertEquals(4250000,rows[0].getTotalSpending());
        assertNull(new OrderDAO4().getOrder(order("DH103"),customer("KH001"),Date.valueOf("2026-09-01"),Date.valueOf("2026-09-01")));
        update("INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT amountDue,'Tiền mặt','2026-07-31','Thành công',id,tblStaff4id FROM tblInvoice4 WHERE invoiceCode='HD103'");
        assertEquals(3150000,new CustomerStatisticDAO4().getCustomerStatistics(start,end)[0].getTotalSpending());
        assertNull(new OrderDAO4().getOrder(order("DH103"),customer("KH001"),start,end));
        assertEquals(0,new OrderSubmissionProductDAO4().getOrderSubmissionProducts(order("DH103"),customer("KH001"),start,end).length);
    }
    @Test void listOrdersHasCustomerInvoiceAndAllPayments() throws Exception {
        Order4[] rows=new OrderDAO4().getOrders(customer("KH001"),start,end);
        assertArrayEquals(new String[]{"DH101","DH102","DH103","DH104","DH105"},Arrays.stream(rows).map(Order4::getOrderCode).toArray(String[]::new));
        Order4 o=rows[2];assertEquals("KH001",o.getCustomer().getCustomerCode());assertEquals("HD103",o.getInvoice().getInvoiceCode());
        assertEquals(2,o.getInvoice().getPayments().length);assertEquals("Thất bại",o.getInvoice().getPayments()[0].getResult());
        assertEquals("Thành công",o.getInvoice().getPayments()[1].getResult());
        assertEquals(0,o.getOrderSubmissions().length);assertNull(o.getInvoice().getSalesStaff());assertNull(o.getInvoice().getPayments()[0].getSalesStaff());
        assertEquals(0,o.getCustomer().getMembershipCards().length);
        assertEquals(0,new OrderDAO4().getOrders(customer("KH003"),start,end).length);
    }
    @Test void noNameDrillDownAndContextIsolation() throws Exception {
        OrderDAO4 dao=new OrderDAO4();Order4[] rows=dao.getOrders(0,start,end);
        assertEquals(2,rows.length);assertNull(rows[0].getCustomer());
        assertNotNull(dao.getOrder(order("DH301"),0,start,end));
        assertNull(dao.getOrder(order("DH301"),customer("KH001"),start,end));
        assertNull(dao.getOrder(order("DH103"),customer("KH002"),start,end));
    }
    @Test void orderAndLinesRecheckPaymentValidity() throws Exception {
        OrderDAO4 dao=new OrderDAO4();int id=order("DH103"),customer=customer("KH001");
        assertNotNull(dao.getOrder(id,customer,start,end));
        update("UPDATE tblInvoice4 SET paymentStatus='Chưa thanh toán' WHERE invoiceCode='HD103'");
        assertNull(dao.getOrder(id,customer,start,end));
        assertEquals(0,new OrderSubmissionProductDAO4().getOrderSubmissionProducts(id,customer,start,end).length);
        assertNull(dao.getOrder(order("DH107"),customer,start,end));
    }
    @Test void noSuccessfulPaymentIsNotValidEvenWhenInvoiceMarkedPaid() throws Exception {
        update("UPDATE tblInvoice4 SET paymentStatus='Đã thanh toán' WHERE invoiceCode='HD107'");
        assertNull(new OrderDAO4().getOrder(order("DH107"),customer("KH001"),start,end));
        assertEquals(4250000,new CustomerStatisticDAO4().getCustomerStatistics(start,end)[0].getTotalSpending());
    }
    @Test void detailUsesRecordedPriceAndConcreteDishOrCombo() throws Exception {
        OrderSubmissionProductDAO4 dao=new OrderSubmissionProductDAO4();
        OrderSubmissionProduct4[] lines=dao.getOrderSubmissionProducts(order("DH103"),customer("KH001"),start,end);
        assertEquals(3,lines.length);assertInstanceOf(Dish4.class,lines[0].getProduct());assertInstanceOf(Combo4.class,lines[1].getProduct());
        assertEquals(0,((Combo4)lines[1].getProduct()).getComboDishes().length);
        assertNotNull(((Combo4)lines[1].getProduct()).getStartsAt());
        float total=0;for(var line:lines){assertEquals("Đã chấp nhận",line.getProcessingStatus());assertNull(line.getSalesStaff());total+=line.getQuantity()*line.getRecordedUnitPrice();}
        assertEquals(1100000,total);
        assertEquals(620000,dao.getOrderSubmissionProducts(order("DH102"),customer("KH001"),start,end)[0].getRecordedUnitPrice());
        assertEquals(0,dao.getOrderSubmissionProducts(order("DH103"),customer("KH002"),start,end).length);
        assertEquals(0,dao.getOrderSubmissionProducts(-1,0,start,end).length);
    }
    @Test void pendingCancelledAndRejectedLinesAreExcluded() throws Exception {
        update("UPDATE tblOrderSubmissionProduct4 SET processingStatus='Đã hủy' WHERE tblProduct4id=(SELECT id FROM tblProduct4 WHERE productCode='C001')");
        update("UPDATE tblOrderSubmissionProduct4 SET processingStatus='Chờ xử lý' WHERE tblProduct4id=(SELECT id FROM tblProduct4 WHERE productCode='D002')");
        assertEquals(1,new OrderSubmissionProductDAO4().getOrderSubmissionProducts(order("DH103"),customer("KH001"),start,end).length);
    }
    @Test void changedCustomerAndMissingOrderDoNotLeakDetails() throws Exception {
        int customer=customer("KH001"),id=order("DH103");
        update("UPDATE tblOrder4 SET tblCustomer4id=NULL WHERE orderCode='DH103'");
        assertNull(new OrderDAO4().getOrder(id,customer,start,end));
        assertEquals(0,new OrderSubmissionProductDAO4().getOrderSubmissionProducts(id,customer,start,end).length);
        assertNotNull(new OrderDAO4().getOrder(id,0,start,end));
        assertNull(new OrderDAO4().getOrder(-1,customer,start,end));
    }
    @Test void readFailuresAreNotReportedAsEmptyOrdersOrLines() throws Exception {
        OrderDAO4 orders=new OrderDAO4();OrderSubmissionProductDAO4 lines=new OrderSubmissionProductDAO4();
        update("RENAME TABLE tblPayment4 TO unavailablePayment");
        try {
            assertThrows(IllegalStateException.class,()->orders.getOrders(0,start,end));
            assertThrows(IllegalStateException.class,()->orders.getOrder(1,0,start,end));
            assertThrows(IllegalStateException.class,()->lines.getOrderSubmissionProducts(1,0,start,end));
        } finally { update("RENAME TABLE unavailablePayment TO tblPayment4"); }
    }
}
