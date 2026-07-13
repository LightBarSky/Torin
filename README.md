# Torin

![Reactor CI](https://github.com/LightBarSky/Torin/actions/workflows/reactor.yml/badge.svg)
![Prod CI](https://github.com/LightBarSky/Torin/actions/workflows/prod.yml/badge.svg)
![DBService CI](https://github.com/LightBarSky/Torin/actions/workflows/dbservice.yml/badge.svg)
![TelegramService CI](https://github.com/LightBarSky/Torin/actions/workflows/telegramservice.yml/badge.svg)
# Telegram Analytics Platform

> Distributed event-driven platform for collecting, processing and analyzing Telegram data at scale.

![Architecture](docs/architecture.png)

## Overview

Telegram Analytics Platform is a microservice-based system built around Apache Kafka for asynchronous data processing.

The platform collects Telegram data, stores it in PostgreSQL, synchronizes searchable datasets into Elasticsearch using CDC (Debezium), and provides analytical APIs for querying large volumes of data.

## Architecture

### Services

| Service          | Responsibility                                    |
| ---------------- | ------------------------------------------------- |
| GUI Service      | Manage parsers, consumers, logs and system status |
| Telegram Service | Telegram parsing and data collection              |
| DB Service       | Kafka consumers and PostgreSQL persistence        |
| Reactor Service  | Analytics API and Elasticsearch queries           |

### Infrastructure

* PostgreSQL (Source of Truth)
* Apache Kafka (Event Bus)
* Kafka Connect + Debezium (CDC)
* Logstash (Data Synchronization)
* Elasticsearch (Search & Analytics)
* pgBackRest (Backups)
* Prometheus (Metrics)
* Grafana (Monitoring)

## Data Flow

```text
Telegram Service
        │
        ▼
      Kafka
        │
        ▼
   DB Service
        │
        ▼
   PostgreSQL
        │
        ▼
 Debezium (CDC)
        │
        ▼
      Kafka
        │
        ▼
    Logstash
        │
        ▼
 Elasticsearch
        │
        ▼
 Reactor Service
```

## Technology Stack

**Backend**

* Java Spring
* ASP.NET Core
* REST API

**Data**

* PostgreSQL
* Elasticsearch

**Streaming**

* Apache Kafka
* Kafka Connect
* Debezium

**Observability**

* Prometheus
* Grafana

**Infrastructure**

* Docker
* Docker Compose
* pgBackRest

## Highlights

* Microservices Architecture
* Event-Driven Design
* Change Data Capture (CDC)
* Elasticsearch Analytics
* Automated PostgreSQL Backups
* Distributed Monitoring
* Dockerized Infrastructure
