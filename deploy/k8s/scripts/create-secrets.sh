#!/usr/bin/env bash
# Создаёт секреты со случайными паролями. Существующие секреты не меняются.
set -euo pipefail

NAMESPACE=courtbook

kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f - > /dev/null

exists() { kubectl -n "$NAMESPACE" get secret "$1" > /dev/null 2>&1; }

for name in identity catalog booking payment notification courtbook; do
  exists "pg-$name" && continue
  kubectl -n "$NAMESPACE" create secret generic "pg-$name" --type=kubernetes.io/basic-auth \
    --from-literal=username="$name" \
    --from-literal=password="$(openssl rand -hex 24)"
done

if ! exists jwt-secret; then
  kubectl -n "$NAMESPACE" create secret generic jwt-secret --from-literal=secret="$(openssl rand -hex 32)"
fi
