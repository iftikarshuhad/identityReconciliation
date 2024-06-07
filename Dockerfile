FROM maven:3.8.6-openjdk-20.ea-b9 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:20-jdk-slim
WORKDIR /app
COPY --from=build /app/target/recon-0.0.1-SNAPSHOT.jar recon.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "recon.jar"]
