FROM gradle:6.3.0-jdk8 AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootJar --no-daemon


FROM openjdk:8-jre-alpine

ENV APPLICATION_USER mattermost-attendance-bot

RUN mkdir /app
COPY --from=builder /home/gradle/src/build/libs/mattermost-attendance-bot-*-SNAPSHOT.jar /app/mab.jar

WORKDIR /app

ENTRYPOINT ["java", "-server", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=1", "-XX:MinRAMFraction=1", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "mab.jar"]