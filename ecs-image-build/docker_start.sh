#!/bin/bash

# Start script for psc-statement-delta-consumer

PORT=8080
exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "psc-statement-delta-consumer.jar"
