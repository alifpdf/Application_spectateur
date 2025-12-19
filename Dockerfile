FROM eclipse-temurin:17-jre
WORKDIR /app
COPY mon-app5.jar app.jar
CMD ["java","-jar","app.jar"]
