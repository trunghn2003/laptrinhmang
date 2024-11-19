# Sử dụng image OpenJDK nhẹ để chạy ứng dụng
FROM openjdk:17-jdk-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy file JAR từ thư mục hiện tại vào container
COPY tcpip.jar app.jar

# Expose cổng ứng dụng (ví dụ: 8888)
EXPOSE 8888

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
