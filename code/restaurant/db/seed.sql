-- Dữ liệu minh họa nạp một lần vào schema rỗng. Tài khoản chỉ công bố tại đây.
INSERT INTO tblStaff4 (fullName,phoneNumber,email,workingStatus,username,password,accountStatus,vaitro)
VALUES ('Nguyễn Minh','0900000000','minh@example.test','Đang làm việc','quanly','QuanLy2026!','Đang hoạt động','ManagementStaff4');
INSERT INTO tblDishCategory4 (categoryCode,name,description) VALUES
 ('LM01','Món khai vị',NULL),('LM02','Món chính',NULL),('LM03','Đồ uống',NULL);
INSERT INTO tblProduct4 (productCode,name,salePrice,saleStatus,createdAt) VALUES
 ('D001','Lẩu hải sản',650000,'Đang kinh doanh',NOW()),
 ('D002','Cơm rang hải sản',150000,'Đang kinh doanh',NOW()),
 ('D003','Trà đào',35000,'Đang kinh doanh',NOW());
INSERT INTO tblDish4 (tblDishCategory4id,tblProduct4id)
 SELECT c.id,p.id FROM tblProduct4 p JOIN tblDishCategory4 c
 ON c.categoryCode = CASE WHEN p.productCode='D003' THEN 'LM03' ELSE 'LM02' END
 WHERE p.productCode IN ('D001','D002','D003');

-- Dữ liệu module thống kê, giữ nguyên món và tài khoản module thêm món.
INSERT INTO tblStaff4 (fullName,phoneNumber,email,workingStatus,username,password,accountStatus,vaitro)
VALUES ('Nhân viên bán hàng','0900000001','sales@example.test','Đang làm việc','banhang','BanHang2026!','Đang hoạt động','SalesStaff4');
INSERT INTO tblProduct4(productCode,name,salePrice,saleStatus,createdAt) VALUES
 ('D005','Gỏi cuốn',115000,'Đang kinh doanh','2026-01-01'),
 ('C001','Combo nước',75000,'Đang kinh doanh','2026-01-01');
INSERT INTO tblDish4(tblDishCategory4id,tblProduct4id)
 SELECT c.id,p.id FROM tblProduct4 p JOIN tblDishCategory4 c ON c.categoryCode='LM01' WHERE p.productCode='D005';
INSERT INTO tblCombo4(startsAt,endsAt,tblProduct4id)
 SELECT '2026-01-01 00:00:00','2026-12-31 23:59:59',id FROM tblProduct4 WHERE productCode='C001';
INSERT INTO tblCustomer4(customerCode,fullName,phoneNumber,profileStatus) VALUES
 ('KH001','Nguyễn Văn An','0910000001','Đang hoạt động'),
 ('KH002','Trần Thị Bình','0910000002','Đang hoạt động'),
 ('KH003','Lê Văn Cường','0910000003','Đang hoạt động');
INSERT INTO tblMembershipCard4(cardCode,issuedAt,status,tblCustomer4id)
 SELECT 'TV001','2026-01-01','Đang hoạt động',id FROM tblCustomer4 WHERE customerCode='KH001';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH101','2026-08-03 20:05','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH001'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD101','2026-08-03 20:05',750000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH101';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 750000,'Tiền mặt','2026-08-03 20:05','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD101';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-03 20:05',id FROM tblOrder4 WHERE orderCode='DH101';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 3,150000,'Đã chấp nhận','2026-08-03 20:05',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D002' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH101';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 4,75000,'Đã chấp nhận','2026-08-03 20:05',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='C001' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH101';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH102','2026-08-08 11:42','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH001'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD102','2026-08-08 11:42',620000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH102';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 620000,'Tiền mặt','2026-08-08 11:42','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD102';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-08 11:42',id FROM tblOrder4 WHERE orderCode='DH102';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 1,620000,'Đã chấp nhận','2026-08-08 11:42',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D001' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH102';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH103','2026-08-15 20:36','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH001'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD103','2026-08-15 20:36',1100000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH103';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 1100000,'Tiền mặt','2026-08-15 20:36','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD103';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 1100000,'Tiền mặt','2026-08-15 20:30','Thất bại',id,tblStaff4id FROM tblInvoice4 WHERE invoiceCode='HD103';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-15 20:36',id FROM tblOrder4 WHERE orderCode='DH103';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 1,650000,'Đã chấp nhận','2026-08-15 20:36',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D001' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH103';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 2,75000,'Đã chấp nhận','2026-08-15 20:36',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='C001' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH103';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 2,150000,'Đã chấp nhận','2026-08-15 20:36',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D002' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH103';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 1,35000,'Từ chối','2026-08-15 20:36',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D003' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH103';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH104','2026-08-22 18:02','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH001'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD104','2026-08-22 18:02',880000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH104';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 880000,'Tiền mặt','2026-08-22 18:02','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD104';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-22 18:02',id FROM tblOrder4 WHERE orderCode='DH104';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 1,650000,'Đã chấp nhận','2026-08-22 18:02',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D001' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH104';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 2,115000,'Đã chấp nhận','2026-08-22 18:02',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D005' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH104';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH105','2026-08-29 13:25','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH001'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD105','2026-08-29 13:25',900000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH105';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 900000,'Tiền mặt','2026-08-29 13:25','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD105';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-29 13:25',id FROM tblOrder4 WHERE orderCode='DH105';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 6,150000,'Đã chấp nhận','2026-08-29 13:25',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D002' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH105';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH106','2026-07-31 23:50','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH001'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD106','2026-07-31 23:50',450000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH106';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 450000,'Tiền mặt','2026-07-31 23:50','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD106';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-07-31 23:50',id FROM tblOrder4 WHERE orderCode='DH106';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 3,150000,'Đã chấp nhận','2026-07-31 23:50',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D002' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH106';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH107','2026-08-10 19:35','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH001'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD107','2026-08-10 19:35',650000,'Chưa thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH107';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 650000,'Tiền mặt','2026-08-10 19:35','Thất bại',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD107';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-10 19:35',id FROM tblOrder4 WHERE orderCode='DH107';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 1,650000,'Đã chấp nhận','2026-08-10 19:35',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D001' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH107';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH201','2026-08-05 19:10','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH002'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD201','2026-08-05 19:10',1300000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH201';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 1300000,'Tiền mặt','2026-08-05 19:10','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD201';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-05 19:10',id FROM tblOrder4 WHERE orderCode='DH201';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 2,650000,'Đã chấp nhận','2026-08-05 19:10',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D001' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH201';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH202','2026-08-31 23:59','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH002'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD202','2026-08-31 23:59',880000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH202';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 880000,'Tiền mặt','2026-08-31 23:59','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD202';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-31 23:59',id FROM tblOrder4 WHERE orderCode='DH202';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 1,650000,'Đã chấp nhận','2026-08-31 23:59',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D001' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH202';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 2,115000,'Đã chấp nhận','2026-08-31 23:59',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D005' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH202';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH203','2026-09-01 00:00','Hoàn tất',(SELECT id FROM tblCustomer4 WHERE customerCode='KH002'));
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD203','2026-09-01 00:00',300000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH203';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 300000,'Tiền mặt','2026-09-01 00:00','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD203';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-09-01 00:00',id FROM tblOrder4 WHERE orderCode='DH203';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 2,150000,'Đã chấp nhận','2026-09-01 00:00',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D002' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH203';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH301','2026-08-01 00:00','Hoàn tất',NULL);
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD301','2026-08-01 00:00',1050000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH301';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 1050000,'Tiền mặt','2026-08-01 00:00','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD301';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-01 00:00',id FROM tblOrder4 WHERE orderCode='DH301';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 4,150000,'Đã chấp nhận','2026-08-01 00:00',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D002' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH301';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 6,75000,'Đã chấp nhận','2026-08-01 00:00',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='C001' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH301';
INSERT INTO tblOrder4(orderCode,createdAt,status,tblCustomer4id) VALUES('DH302','2026-08-12 12:15','Hoàn tất',NULL);
INSERT INTO tblInvoice4(invoiceCode,issuedAt,amountDue,paymentStatus,tblOrder4id,tblStaff4id) SELECT 'HD302','2026-08-12 12:15',300000,'Đã thanh toán',o.id,st.id FROM tblOrder4 o JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH302';
INSERT INTO tblPayment4(amount,method,paidAt,result,tblInvoice4id,tblStaff4id) SELECT 300000,'Tiền mặt','2026-08-12 12:15','Thành công',i.id,i.tblStaff4id FROM tblInvoice4 i WHERE invoiceCode='HD302';
INSERT INTO tblOrderSubmission4(submittedAt,tblOrder4id) SELECT '2026-08-12 12:15',id FROM tblOrder4 WHERE orderCode='DH302';
INSERT INTO tblOrderSubmissionProduct4(quantity,recordedUnitPrice,processingStatus,processedAt,tblOrderSubmission4id,tblProduct4id,tblStaff4id) SELECT 2,150000,'Đã chấp nhận','2026-08-12 12:15',os.id,p.id,st.id FROM tblOrderSubmission4 os JOIN tblOrder4 o ON o.id=os.tblOrder4id JOIN tblProduct4 p ON p.productCode='D002' JOIN tblStaff4 st ON st.username='banhang' WHERE o.orderCode='DH302';
