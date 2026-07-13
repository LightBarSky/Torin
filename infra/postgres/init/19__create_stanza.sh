#!/bin/bash
set -e

echo "Cleaning old backup repository..."

rm -rf /var/lib/pg_back/*
mkdir -p /var/lib/pg_back

chmod 755 /var/lib/pgbackrest
chown -R postgres:postgres /var/lib/pgbackrest

echo "Creating stanza..."

pgbackrest \
  --stanza=main \
  --log-level-console=info \
  stanza-create

echo "Checking stanza..."

pgbackrest \
  --stanza=main \
  check

echo "pgBackRest initialized"