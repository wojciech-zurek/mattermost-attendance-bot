#!/usr/bin/env sh

#./gradlew clean bootJar
docker build -t wojciechzurek/mattermost-attendance-bot:0.0.1 -f Dockerfile .