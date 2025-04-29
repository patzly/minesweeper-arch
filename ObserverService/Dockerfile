FROM sbtscala/scala-sbt:eclipse-temurin-alpine-21.0.6_7_1.10.11_3.6.4 as build

WORKDIR /app
COPY ./ /app

RUN sbt dist

FROM alpine/java:21-jdk

WORKDIR /app

COPY --from=build /app/target/universal/minesweeper-1.0-SNAPSHOT.zip /app

EXPOSE 8080
RUN unzip minesweeper-1.0-SNAPSHOT.zip && rm minesweeper-1.0-SNAPSHOT.zip
