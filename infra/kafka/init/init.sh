#!/bin/bash

set -e

echo "Creating topics..."

kafka-topics \
  --create \
  --if-not-exists \
  --topic admin_chats \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic chat \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic gifts \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic gui_notifications \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=3600000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic logs_handlers \
  --partitions 6 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.bytes=21474836480 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic messages \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic messages_entities \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic messages_properties \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic notifications \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=3600000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic reactions \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic reactions_general \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic service-heartbeat \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=3600000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic status_listeners \
  --partitions 3 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=3600000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic task_chats \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic user \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic word_group_all \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=604800000 \
  --config max.message.bytes=5242880 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic debezium.base_data.chat \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=172800000 \
  --bootstrap-server kafka:29092

kafka-topics \
  --create \
  --if-not-exists \
  --topic debezium.base_data.user \
  --partitions 1 \
  --replication-factor 1 \
  --config segment.bytes=1073741824 \
  --config retention.ms=172800000 \
  --bootstrap-server kafka:29092

echo "Topics created"