#!/bin/bash
#sudo docker inspect --format='{{.LogPath}}' $(sudo docker ps -q) | xargs sudo less +F
sudo tail -f /var/log/syslog | ccze -A