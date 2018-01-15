#!/usr/bin/env bash

java -jar -Dspring.profiles.active=live $(ls app/german-bot-*.jar)