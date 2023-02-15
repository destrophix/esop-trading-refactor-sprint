#!/bin/bash

sudo su

#kill process running in port 80
lsof -t -i:80 | xargs kill -9

#start the application
export MICRONAUT_SERVER_PORT=80
nohup java -jar /app/esop-0.1-all.jar > log.txt
