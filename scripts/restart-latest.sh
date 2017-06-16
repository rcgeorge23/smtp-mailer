#!/bin/bash
sudo docker ps | grep 'dockernovinet/smtp-mailer-brochureware' | cut -c1-12 | xargs sudo docker stop
sudo docker ps | grep 'dockernovinet/smtp-mailer' | cut -c1-12 | xargs sudo docker stop
sudo docker ps | grep 'jwilder/nginx-proxy' | cut -c1-12 | xargs sudo docker stop

sudo docker pull dockernovinet/smtp-mailer-brochureware
sudo docker pull dockernovinet/smtp-mailer
sudo docker pull jwilder/nginx-proxy

sudo docker run -d --restart unless-stopped -e VIRTUAL_HOST=fakesmtp-brochureware.novinet.co.uk -e FAKE_SMTP_HOST=fakesmtp.novinet.co.uk -e FAKE_SMTP_PORT=25 -e VIRTUAL_PORT=9090 -e SERVER_PORT=9090 --log-driver=syslog -p9090:9090 -t dockernovinet/smtp-mailer-brochureware
sudo docker run -d --restart unless-stopped -e VIRTUAL_HOST=fakesmtp.novinet.co.uk -e VIRTUAL_PORT=8080 -e SERVER_PORT=8080 --log-driver=syslog -p8080:8080 -p25:8025 -t dockernovinet/smtp-mailer
sudo docker run -d --restart unless-stopped -p80:80 -v /var/run/docker.sock:/tmp/docker.sock:ro jwilder/nginx-proxy