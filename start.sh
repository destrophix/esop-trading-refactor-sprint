#!/bin/bash

#kill process running in port 80
sudo lsof -t -i:80 | sudo xargs kill -9

#start the application
nohup sudo MICRONAUT_SERVER_PORT=80 java -jar /app/esop-0.1-all.jar > log.txt &
