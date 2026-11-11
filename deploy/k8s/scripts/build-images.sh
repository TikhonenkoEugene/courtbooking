#!/usr/bin/env bash
# Собирает образы сервисов: courtbook/<сервис>:local
set -euo pipefail
cd "$(dirname "$0")/../../.."

for service in apigateway identity notification catalog booking payment; do
  docker build -t "courtbook/$service:local" --build-arg SERVICE="$service" .
done

for app in web crm; do
  docker build -t "courtbook/$app:local" "frontend/$app"
done
