#!/bin/bash
echo "entrypoint run..."

java -jar $JAVA_OPTS -Dserver.port=$SERVER_PORT -Deureka.client.service-url.defaultZone=$EUREKA_CLIENT_DEFAULT_ZONE -Dspring.cloud.config.profile=$SPRING_CLOUD_CONFIG_PROFILE -Dspring.cloud.config.uri=$SPRING_CLOUD_CONFIG_URI app.jar