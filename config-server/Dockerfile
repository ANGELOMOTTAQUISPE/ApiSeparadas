FROM openjdk:11-jre-slim
COPY "./target/config-server-1.0.jar" "app.jar"
EXPOSE 8888
ENTRYPOINT ["java", "-jar","app.jar"]