-- [NGOÀI THIẾT KẾ] CSDL rỗng cho JUnit; DatabaseTest4 tự tạo bảng trước mỗi ca.
CREATE DATABASE IF NOT EXISTS restaurant_test CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON restaurant_test.* TO 'restaurant'@'%';
FLUSH PRIVILEGES;
