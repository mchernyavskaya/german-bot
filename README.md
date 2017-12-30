# german-bot

## Starting application

Build and publish docker image:
```bash
./gradlew dockerPublish
```

Set env variables:

```bash 
export MESSENGER_VERIFY_TOKEN=...
export MESSENGER_PAGE_ACCESS_TOKEN=...
export MESSENGER_APP_SECRET=...
```

or: 

```bash
source ./setenv.sh
```

Run in docker:
```bash
./run.sh
```