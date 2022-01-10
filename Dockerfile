FROM docker-proxy.tcsbank.ru/maven:3.6.3-jdk-11 as build
COPY . .
RUN mvn package --batch-mode clean package -s settings.xml
RUN mv /target/piapi-robot.jar app.jar

FROM docker-internal.tcsbank.ru/integration/openjdk:11
COPY --from=build app.jar /opt/piapi-robot.jar
