#!/usr/bin/env bash
# ==============================================================
# deploy.sh — Build, push e deploy do Cofry em Kubernetes
#
# Uso:
#   ./k8s/deploy.sh <REGISTRY> [TAG]
#
# Exemplos:
#   ./k8s/deploy.sh docker.io/meuusuario        # usa tag 'latest'
#   ./k8s/deploy.sh docker.io/meuusuario v1.2.0
#
# Pré-requisitos:
#   - docker logado no registry
#   - kubectl configurado apontando para o cluster
#   - kustomize (ou kubectl >= 1.14 com -k)
# ==============================================================

set -euo pipefail

REGISTRY="${1:?Uso: $0 <REGISTRY> [TAG]}"
TAG="${2:-latest}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

BACKEND_IMAGE="$REGISTRY/cofry-backend:$TAG"
FRONTEND_IMAGE="$REGISTRY/cofry-frontend:$TAG"

echo "========================================"
echo "  Cofry Kubernetes Deploy"
echo "  Registry : $REGISTRY"
echo "  Tag      : $TAG"
echo "========================================"

# ── 1. Build das imagens ──────────────────────────────────────
echo ""
echo "▶ [1/4] Construindo imagens Docker..."
docker build -t "$BACKEND_IMAGE"  "$PROJECT_DIR/Cofry-Backend-patrick"
docker build -t "$FRONTEND_IMAGE" "$PROJECT_DIR/Cofry-Front-main"

# ── 2. Push para o registry ──────────────────────────────────
echo ""
echo "▶ [2/4] Enviando imagens para o registry..."
docker push "$BACKEND_IMAGE"
docker push "$FRONTEND_IMAGE"

# ── 3. Atualizar tags nos manifests (patch inline) ───────────
echo ""
echo "▶ [3/4] Atualizando tags nos Deployments..."
kubectl set image deployment/cofry-backend \
  cofry-backend="$BACKEND_IMAGE" \
  -n cofry --record 2>/dev/null || true

kubectl set image deployment/cofry-frontend \
  cofry-frontend="$FRONTEND_IMAGE" \
  -n cofry --record 2>/dev/null || true

# ── 4. Aplicar manifests base ─────────────────────────────────
echo ""
echo "▶ [4/4] Aplicando manifests Kubernetes..."

# Substitui as tags de imagem nos arquivos antes de aplicar
sed -i \
  "s|image: cofry-backend:.*|image: $BACKEND_IMAGE|g" \
  "$SCRIPT_DIR/base/deployment-backend.yaml"

sed -i \
  "s|image: cofry-frontend:.*|image: $FRONTEND_IMAGE|g" \
  "$SCRIPT_DIR/base/deployment-frontend.yaml"

kubectl apply -k "$SCRIPT_DIR/base"

# ── 5. Aguardar rollout ───────────────────────────────────────
echo ""
echo "▶ Aguardando rollout do backend..."
kubectl rollout status deployment/cofry-backend -n cofry --timeout=120s

echo "▶ Aguardando rollout do frontend..."
kubectl rollout status deployment/cofry-frontend -n cofry --timeout=120s

# ── 6. Resumo ─────────────────────────────────────────────────
echo ""
echo "========================================"
echo "  ✅ Deploy concluído com sucesso!"
echo "========================================"
echo ""
kubectl get pods -n cofry
echo ""
echo "IP externo do frontend:"
kubectl get svc cofry-frontend-svc -n cofry \
  -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null \
  && echo "" || echo "(pendente — aguarde o LoadBalancer ser provisionado)"
