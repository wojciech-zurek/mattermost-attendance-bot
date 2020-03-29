FROM openjdk:8-jre-alpine

ENV APPLICATION_USER mattermost-attendance-bot
#RUN groupadd --gid 1000 $APPLICATION_USER && adduser -g 1000 $APPLICATION_USER

RUN mkdir /app
#RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY ./build/libs/mattermost-attendance-bot-*-SNAPSHOT.jar /app/mb.jar
WORKDIR /app

CMD ["java", "-server", \
"-Djava.security.egd=file:/dev/./urandom", \
"-XX:+UnlockExperimentalVMOptions", \
"-XX:+UseCGroupMemoryLimitForHeap", \
"-XX:InitialRAMFraction=2", \
"-XX:MinRAMFraction=2", \
"-XX:MaxRAMFraction=2", \
"-XX:+UseG1GC", \
"-XX:MaxGCPauseMillis=100", \
"-XX:+UseStringDeduplication", \
"-jar", "mb.jar"]