FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21_35_1.9.6_3.3.1

WORKDIR /app
COPY ./ /app

RUN sbt clean compile

EXPOSE 8080
