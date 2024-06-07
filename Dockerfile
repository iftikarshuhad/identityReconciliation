FROM maven:3.8.6-openjdk-20.ea-b9 as build
COPY ..
RUN mvn clean package -DskipTests

FROM openjdk:20-jdk-slim
COPY --from=build /target/recon-0.0.1-SNAPSHOT.jar recon.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","recon.jar"]