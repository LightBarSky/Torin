#!/bin/sh
set -eu

ES_URL="http://elasticsearch:9200"

bootstrap() {
  INDEX_PREFIX="$1"
  WRITE_ALIAS="$2"
  READ_ALIAS="$3"

  LAST_INDEX=$(curl -sS \
    "$ES_URL/_cat/indices/${INDEX_PREFIX}-*?h=index" \
    | sort | tail -n 1)

  if [ -z "$LAST_INDEX" ]; then
    NEXT="000001"
  else
    NUM=$(echo "$LAST_INDEX" | grep -oE '[0-9]+$')
    NEXT=$(printf "%06d" $((10#$NUM + 1)))
  fi

  NEW="${INDEX_PREFIX}-${NEXT}"

  curl -sS -X PUT "$ES_URL/$NEW" \
    -H "Content-Type: application/json" \
    -d "{
      \"aliases\": {
        \"$WRITE_ALIAS\": { \"is_write_index\": true },
        \"$READ_ALIAS\": {}
      }
    }"
}

echo "Waiting Elasticsearch..."

until curl -s "$ES_URL" >/dev/null; do
  sleep 2
done

echo "Elasticsearch is ready"

# =========================================================
# SNAPSHOT AND SLM
# =========================================================

echo "Applying snapshot repository..."

curl --fail -sS -X PUT \
  "$ES_URL/_snapshot/my_backup" \
  -H "Content-Type: application/json" \
  -d @/init/slm/snapshot.json

echo "Snapshot repository applied"

echo "Applying SLM..."

curl --fail -sS -X PUT \
  "$ES_URL/_slm/policy/daily-backup" \
  -H "Content-Type: application/json" \
  -d @/init/slm/slm.json

#curl --fail -sS -X PUT \
#  "$ES_URL/_slm/policy/daily-backup/_execute"

echo "SLM applied"

# =========================================================
# LOGS HANDLERS
# =========================================================

echo "Applying logs_handlers ilm..."

curl --fail -sS -X PUT \
  "$ES_URL/_ilm/policy/logs_handlers_policy" \
  -H "Content-Type: application/json" \
  -d @/init/ilm/logs_handlers_policy.json

echo "logs_handlers ilm applied"

echo "Applying logs_handlers template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/logs_handlers_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/logs_handlers_template.json

echo "logs_handlers template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/logs_handlers_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index logs_handlers + aliases..."

  bootstrap "logs_handlers" "logs_handlers_write" "logs_handlers_read"

  echo "logs_handlers initialization completed"
else
  echo "Index logs_handlers already initialized"
fi

# =========================================================
# PARTICIPANT CHANGED
# =========================================================

echo "Applying participant_changed ilm..."

curl --fail -sS -X PUT \
  "$ES_URL/_ilm/policy/participant_changed_policy" \
  -H "Content-Type: application/json" \
  -d @/init/ilm/participant_changed_policy.json

echo "participant_changed ilm applied"

echo "Applying participant_changed template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/participant_changed_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/participant_changed_template.json

echo "participant_changed template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/participant_changed_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index participant_changed + aliases..."

  bootstrap "participant_changed" "participant_changed_write" "participant_changed_read"

  echo "participant_changed initialization completed"
else
  echo "Index participant_changed already initialized"
fi

# =========================================================
# DELETE 90D POLICY
# =========================================================

echo "Applying delete_90d ilm..."

curl --fail -sS -X PUT \
  "$ES_URL/_ilm/policy/delete_90d_policy" \
  -H "Content-Type: application/json" \
  -d @/init/ilm/delete_90d_policy.json

echo "delete_90d ilm applied"

# =========================================================
# REACTIONS
# =========================================================

echo "Applying reactions template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/reactions_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/reactions_template.json

echo "reactions template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/reactions_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index reactions + aliases..."

  #bootstrap "reactions" "reactions_write" "reactions_read"

  echo "reactions initialization completed"
else
  echo "Index reactions already initialized"
fi

# =========================================================
# MESSAGES
# =========================================================

echo "Applying messages template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/messages_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/messages_template.json

echo "messages template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/messages_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index messages + aliases..."

  #bootstrap "messages" "messages_write" "messages_read"

  echo "messages initialization completed"
else
  echo "Index messages already initialized"
fi

# =========================================================
# CHAT
# =========================================================

echo "Applying chat ilm..."

curl --fail -sS -X PUT \
  "$ES_URL/_ilm/policy/chat_policy" \
  -H "Content-Type: application/json" \
  -d @/init/ilm/chat_policy.json

echo "chat ilm applied"

echo "Applying chat template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/chat_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/chat_template.json

echo "chat template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/chat_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index chat + aliases..."

  bootstrap "chat" "chat_write" "chat_read"

  echo "chat initialization completed"
else
  echo "Index chat already initialized"
fi

# =========================================================
# USER
# =========================================================

echo "Applying user ilm..."

curl --fail -sS -X PUT \
  "$ES_URL/_ilm/policy/user_policy" \
  -H "Content-Type: application/json" \
  -d @/init/ilm/user_policy.json

echo "user ilm applied"

echo "Applying user template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/user_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/user_template.json

echo "user template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/user_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index user + aliases..."

  bootstrap "user" "user_write" "user_read"

  echo "user initialization completed"
else
  echo "Index user already initialized"
fi

# =========================================================
# REACTIONS GENERAL
# =========================================================

echo "Applying reactions_general template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/reactions_general_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/reactions_general_template.json

echo "reactions_general template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/reactions_general_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index reactions_general + aliases..."

  #bootstrap "reactions_general" "reactions_general_write" "reactions_general_read"

  echo "reactions_general initialization completed"
else
  echo "Index reactions_general already initialized"
fi

# =========================================================
# GIFTS
# =========================================================

echo "Applying gifts ilm..."

curl --fail -sS -X PUT \
  "$ES_URL/_ilm/policy/gifts_policy" \
  -H "Content-Type: application/json" \
  -d @/init/ilm/gifts_policy.json

echo "gifts ilm applied"

echo "Applying gifts template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/gifts_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/gifts_template.json

echo "gifts template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/gifts_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index gifts + aliases..."

  bootstrap "gifts" "gifts_write" "gifts_read"

  echo "gifts initialization completed"
else
  echo "Index gifts already initialized"
fi

# =========================================================
# MESSAGES PROPERTIES
# =========================================================

echo "Applying messages_properties template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/messages_properties_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/messages_properties_template.json

echo "messages_properties template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/messages_properties_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index messages_properties + aliases..."

  #bootstrap "messages_properties" "messages_properties_write" "messages_properties_read"

  echo "messages_properties initialization completed"
else
  echo "Index messages_properties already initialized"
fi

# =========================================================
# MESSAGES ENTITIES
# =========================================================

echo "Applying messages_entities template..."

curl --fail -sS -X PUT \
  "$ES_URL/_index_template/messages_entities_template" \
  -H "Content-Type: application/json" \
  -d @/init/templates/messages_entities_template.json

echo "messages_entities template applied"

WRITE_ALIAS_EXISTS=$(curl -sS -o /dev/null -w "%{http_code}" \
  $ES_URL/_alias/messages_entities_write)

if [ "$WRITE_ALIAS_EXISTS" != "200" ]; then
  echo "Creating first index messages_entities + aliases..."

  #bootstrap "messages_entities" "messages_entities_write" "messages_entities_read"

  echo "messages_entities initialization completed"
else
  echo "Index messages_entities already initialized"
fi

echo "Elasticsearch initialization completed"