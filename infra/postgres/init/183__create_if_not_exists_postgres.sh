#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 \
     --username "$POSTGRES_USER" \
     --dbname "$POSTGRES_DB" <<EOF

DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'postgres') THEN
        EXECUTE format(
            'CREATE ROLE postgres LOGIN SUPERUSER PASSWORD %L',
            '$POSTGRES_PASSWORD'
        );
    END IF;
END \$\$;

EOF