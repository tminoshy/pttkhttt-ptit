# [NGOÀI THIẾT KẾ] Đóng gói chạy thử hai module và JUnit bằng Docker; không thuộc biểu đồ triển khai.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
COPY db ./db
RUN mvn -B -DskipTests package

FROM build AS test
CMD ["mvn", "-B", "verify", "-Dtest.db.url=jdbc:mysql://db:3306/restaurant_test?characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh", "-Dtest.db.user=restaurant", "-Dtest.db.password=restaurant_local"]

FROM tomcat:10.1-jdk17-temurin AS runtime
ENV TZ=Asia/Ho_Chi_Minh
ENV CATALINA_OPTS="-Ddb.config=/usr/local/tomcat/conf/restaurant-db.properties -Duser.timezone=Asia/Ho_Chi_Minh -Dfile.encoding=UTF-8"
COPY docker/db.properties /usr/local/tomcat/conf/restaurant-db.properties
COPY --from=build /src/target/restaurant /usr/local/tomcat/webapps/restaurant
EXPOSE 8080
