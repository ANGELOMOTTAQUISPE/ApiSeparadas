FROM openjdk:11-jre-slim
COPY "./target/Credit-0.0.1-SNAPSHOT.jar" "app.jar"
EXPOSE 8087
ENTRYPOINT ["java", "-jar","app.jar"]