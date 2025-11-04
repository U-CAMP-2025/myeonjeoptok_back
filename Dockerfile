FROM eclipse-temurin:21-jre
WORKDIR /app
COPY app.jar /app/app.jar
EXPOSE 8080
CMD ["java","-Xmx384m","-jar","/app/app.jar"]