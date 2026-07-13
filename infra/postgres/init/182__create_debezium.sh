#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 \
     --username "$POSTGRES_USER" \
     --dbname "$POSTGRES_DB" <<EOF

GRANT CONNECT ON DATABASE $POSTGRES_DB TO debezium;

GRANT USAGE ON SCHEMA base_data TO debezium;

GRANT SELECT ON TABLE
    base_data.user,
    base_data.chat
TO debezium;

create table base_data.debezium_heartbeat (id int primary key default 1, ts timestamptz);

CREATE PUBLICATION dbz_publication
FOR TABLE
    base_data.chat,
    base_data.user
WITH (
    publish_via_partition_root = true
);

GRANT SELECT, insert, update on base_data.debezium_heartbeat to debezium;

EOF