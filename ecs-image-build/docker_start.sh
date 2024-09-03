#!/bin/bash

# Start script for psc-statement-delta-consumer

PORT=8080
exec java -jar -Dserver.port="${PORT}" "psc-statement-delta-consumer.jar"
