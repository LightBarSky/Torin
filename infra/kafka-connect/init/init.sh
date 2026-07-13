#!/bin/sh
set -eu

CONNECT_URL="http://kafka-connect:8083"

echo "Waiting Kafka Connect..."

until curl -fsS "$CONNECT_URL/connectors"; do
  sleep 2
done

echo "Kafka Connect ready"

register_connector() {
  FILE="$1"

  NAME=$(basename "$FILE" .json)

  echo "Applying connector: $NAME"

  if curl -fsS "$CONNECT_URL/connectors/$NAME" >/dev/null 2>&1; then
    echo "Connector exists -> updating"

    #curl -fsS -X PUT \
    #  "$CONNECT_URL/connectors/$NAME/config" \
    #  -H "Content-Type: application/json" \
    #  -d @"$FILE"

  else
    echo "Connector does not exist -> creating"

    curl -fsS -X POST \
      "$CONNECT_URL/connectors" \
      -H "Content-Type: application/json" \
      -d @"$FILE"
  fi

  echo "Connector applied: $NAME"
}

for file in /connectors/*.json; do
  register_connector "$file"
done

echo "All connectors applied"