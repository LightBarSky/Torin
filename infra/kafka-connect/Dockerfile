FROM confluentinc/cp-kafka-connect:7.8.0

RUN mkdir -p /usr/share/confluent-hub-components

RUN curl -L https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/3.4.3.Final/debezium-connector-postgres-3.4.3.Final-plugin.tar.gz \
    -o /tmp/debezium.tar.gz \
 && tar -xzf /tmp/debezium.tar.gz -C /usr/share/confluent-hub-components \
 && rm /tmp/debezium.tar.gz