#!/usr/bin/env bash

docker run -d -p8080:8080 \
-e MESSENGER_VERIFY_TOKEN=$MESSENGER_VERIFY_TOKEN \
-e MESSENGER_APP_SECRET=$MESSENGER_APP_SECRET \
-e MESSENGER_PAGE_ACCESS_TOKEN=$MESSENGER_PAGE_ACCESS_TOKEN \
juntacr/tk.germanbot