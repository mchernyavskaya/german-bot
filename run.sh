#!/usr/bin/env bash

docker stop germanbot
docker pull juntacr/tk.germanbot

docker run --rm -p8080:8080 --name germanbot \
-e MESSENGER_VERIFY_TOKEN=$MESSENGER_VERIFY_TOKEN \
-e MESSENGER_APP_SECRET=$MESSENGER_APP_SECRET \
-e MESSENGER_PAGE_ACCESS_TOKEN=$MESSENGER_PAGE_ACCESS_TOKEN \
juntacr/tk.germanbot
