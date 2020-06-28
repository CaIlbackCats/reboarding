FROM tlaura/reboarding:latest

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# install maven dependency packages
COPY pom.xml /usr/src/app
COPY mvnw /usr/src/app
COPY .mvn /usr/src/app/.mvn
RUN ./mvnw dependency:resolve && ./mvnw dependency:resolve-plugins && rm -rf target

# copy other source files and create jar
COPY src /usr/src/app/src
RUN ./mvnw package -DskipTests