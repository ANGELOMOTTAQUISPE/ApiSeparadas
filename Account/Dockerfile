FROM openjdk:11-jre-slim
COPY "./target/Account-0.0.1-SNAPSHOT.jar" "app.jar"
EXPOSE 8086
ENTRYPOINT ["java", "-jar","app.jar"]