FROM openjdk:11-jre-slim
COPY "./target/prjApiGateway-0.0.1-SNAPSHOT.jar" "app.jar"
EXPOSE 8089
ENTRYPOINT ["java", "-jar","app.jar"]