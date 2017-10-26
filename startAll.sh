#!/bin/bash
docker run --name smtp-trap-db -e MYSQL_DATABASE=smtptrap -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_ROOT_HOST=172.17.0.1 -p 3306:3306 -d mysql/mysql-server:8.0
docker run -e DB_USERNAME=root -e DB_PASSWORD=secret -p 8080:8080 -p 8025:8025 -t smtp-mailer