#!/usr/bin/env bash
# Сборка, импорт образов в k3d, секреты, манифесты.
#   deploy-local.sh              собрать и выкатить
#   SKIP_BUILD=1 deploy-local.sh только применить манифесты и перезапустить сервисы
set -euo pipefail
cd "$(dirname "$0")"

CLUSTER=${CLUSTER:-pet-cluster}
SERVICES=(apigateway identity notification catalog booking payment web crm)
kubectl config use-context "k3d-$CLUSTER" > /dev/null

if [[ -z ${SKIP_BUILD:-} ]]; then
  ./build-images.sh
fi
images=(); for s in "${SERVICES[@]}"; do images+=("courtbook/$s:local"); done
k3d image import -c "$CLUSTER" "${images[@]}"

./create-secrets.sh
kubectl apply -k ../overlays/local

kubectl -n courtbook wait --for=condition=Ready cluster/courtbook-pg --timeout=300s
kubectl -n courtbook rollout status statefulset/kafka --timeout=300s

kubectl -n courtbook rollout restart "${SERVICES[@]/#/deployment/}"
for service in "${SERVICES[@]}"; do
  kubectl -n courtbook rollout status "deployment/$service" --timeout=300s
done

echo 'Клиент: http://courtbook.localhost'
echo 'CRM:    http://crm.courtbook.localhost'
