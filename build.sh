#!/bin/bash
echo "Stopping dockernovinet/smtp-mailer docker containers that are currently running"
docker ps | grep 'dockernovinet/smtp-mailer' | cut -c1-12 | xargs docker stop
	
./gradlew buildDocker
	
echo "Starting new dockernovinet/smtp-mailer docker container"
docker run -d -p 80:8080 -p25:8025 -t dockernovinet/smtp-mailer
	
echo "Waiting for application to finish starting up"
until [ "$(curl -sL -w '%{http_code}\n' 'http://localhost:80' -o /dev/null)" -eq "400" ]; do
    printf '.'
    sleep 1
done

echo "Running tests"
./gradlew test

if [ $? -eq 0 ] 
	then
	echo "Tests passed, so pushing new docker image to repository"
	docker push dockernovinet/smtp-mailer:latest
fi

echo "Shutting down docker container"
docker ps | grep 'dockernovinet/smtp-mailer' | cut -c1-12 | xargs docker stop