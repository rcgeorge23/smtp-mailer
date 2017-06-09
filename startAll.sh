#!/bin/bash
docker run --name smtp-mailer-db -e MYSQL_ROOT_PASSWORD=secret -d mysql/mysql-server:8.0
docker run -p 8080:8080 -p 8025:8025 -t smtp-mailer
