psql -v pass_deb="$PASSWORD_DEB" -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" <<EOF

CREATE ROLE debezium
WITH LOGIN
PASSWORD :'pass_deb'
REPLICATION;

EOF