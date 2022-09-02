FROM openjdk:11-jre-slim
COPY "./target/Movement-0.0.1-SNAPSHOT.jar" "app.jar"
EXPOSE 8088
ENTRYPOINT ["java", "-jar","app.jar"]