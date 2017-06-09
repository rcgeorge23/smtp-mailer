#!/bin/bash
sudo docker ps | grep 'dockernovinet/smtp-mailer' | cut -c1-12 | xargs sudo docker stop
sudo docker pull dockernovinet/smtp-mailer
sudo docker run --log-driver=syslog -p 80:8080 -p25:8025 -t dockernovinet/smtp-mailer