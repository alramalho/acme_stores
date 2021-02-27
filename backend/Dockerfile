FROM gradle:jdk11-openj9 AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test --no-daemon

FROM adoptopenjdk/openjdk11:latest

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/backend.jar

ENTRYPOINT ["java","-jar","/app/backend.jar"]
