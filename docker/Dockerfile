FROM tlaura/reboarding:latest

# copy all from directory in to the image
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw test && ./mvnw package -DskipTests
EXPOSE 8080
ENTRYPOINT java -jar target/reboarding-0.0.1-SNAPSHOT.jar