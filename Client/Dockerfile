FROM openjdk:11-jre-slim
COPY "./target/Client-0.0.1-SNAPSHOT.jar" "app.jar"
EXPOSE 8085
ENTRYPOINT ["java", "-jar","app.jar"]