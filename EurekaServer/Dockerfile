FROM openjdk:11-jre-slim
COPY "./target/api-gateway-0.0.1-SNAPSHOT.jar" "app.jar"
EXPOSE 8099
ENTRYPOINT ["java", "-jar","app.jar"]