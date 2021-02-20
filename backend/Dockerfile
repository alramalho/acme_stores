FROM openjdk:8-jdk-alpine

# Prep image with gradle cache to use as mount point for host cache when container runs
RUN mkdir /root/.gradle

# We can mount the host code repo here if we to update with every run, but the copy below makes mounting unnecessary
# if we only want the repo state at build time.
RUN mkdir /app

WORKDIR /app
COPY . .

CMD ["./gradlew", "build"]
CMD ["java", "-jar", "build/libs/backend-1.0-SNAPSHOT.jar"]