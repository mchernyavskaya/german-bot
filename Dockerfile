FROM openjdk:8

# set DNS TTL to 60 (recommended by AWS)
RUN sed -i s/#networkaddress\.cache\.ttl=-1/networkaddress\.cache\.ttl=60/ /etc/java-8-openjdk/security/java.security

COPY build/libs/german-bot-*.jar /app/
COPY startup.sh /

CMD ["/bin/bash","startup.sh"]