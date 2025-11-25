#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="docker-compose.kafka.yml"

if [[ ! -f "${COMPOSE_FILE}" ]]; then
  echo "Cannot find ${COMPOSE_FILE} in $(pwd)" >&2
  exit 1
fi

case "${1:-up}" in
  up)
    echo "Starting Kafka broker with ${COMPOSE_FILE}..."
    docker compose -f "${COMPOSE_FILE}" up -d
    ;;
  down)
    echo "Stopping Kafka broker..."
    docker compose -f "${COMPOSE_FILE}" down
    ;;
  logs)
    echo "Streaming Kafka container logs..."
    docker compose -f "${COMPOSE_FILE}" logs -f
    ;;
  *)
    echo "Usage: $0 [up|down|logs]" >&2
    exit 1
    ;;
esac
