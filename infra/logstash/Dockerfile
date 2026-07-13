FROM docker.elastic.co/logstash/logstash:9.1.5

RUN mkdir -p /usr/share/logstash/drivers

RUN curl -L https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.10/postgresql-42.7.10.jar \
    -o /usr/share/logstash/drivers/postgresql.jar