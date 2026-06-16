#!/usr/bin/env bash
# ==============================================================
# push-to-registry.sh — Push das imagens Cofry para Docker Hub
#                         e atualização dos manifestos K8s
#
# Uso:
#   ./k8s/push-to-registry.sh <DOCKERHUB_USER>
#
# Exemplo:
#   ./k8s/push-to-registry.sh romariojala
# ==============================================================
set -euo pipefail

REGISTRY="${1:?Uso: $0 <SEU_USUARIO_DOCKERHUB>}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

BACKEND_IMAGE="$REGISTRY/cofry-backend:latest"
FRONTEND_IMAGE="$REGISTRY/cofry-frontend:latest"

echo "=============================================="
echo "  Cofry → Docker Hub Push"
echo "  Usuário: $REGISTRY"
echo "=============================================="

# 1. Tag
echo ""
echo "▶ [1/3] Criando tags..."
docker tag cofry-backend:latest  "$BACKEND_IMAGE"
docker tag cofry-frontend:latest "$FRONTEND_IMAGE"
echo "  ✅ cofry-backend:latest  → $BACKEND_IMAGE"
echo "  ✅ cofry-frontend:latest → $FRONTEND_IMAGE"

# 2. Push
echo ""
echo "▶ [2/3] Fazendo push para Docker Hub..."
docker push "$BACKEND_IMAGE"
docker push "$FRONTEND_IMAGE"

# 3. Atualizar manifestos
echo ""
echo "▶ [3/3] Atualizando imagens nos manifestos K8s..."
sed -i "s|image: cofry-backend:.*|image: $BACKEND_IMAGE|g"  "$SCRIPT_DIR/base/deployment-backend.yaml"
sed -i "s|image: cofry-frontend:.*|image: $FRONTEND_IMAGE|g" "$SCRIPT_DIR/base/deployment-frontend.yaml"

# Atualizar imagePullPolicy para Always (registry externo)
sed -i "s|imagePullPolicy: IfNotPresent|imagePullPolicy: Always|g" "$SCRIPT_DIR/base/deployment-backend.yaml"
sed -i "s|imagePullPolicy: IfNotPresent|imagePullPolicy: Always|g" "$SCRIPT_DIR/base/deployment-frontend.yaml"

echo ""
echo "=============================================="
echo "  ✅ Tudo pronto!"
echo ""
echo "  Imagens públicas:"
echo "  🐳 https://hub.docker.com/r/$REGISTRY/cofry-backend"
echo "  🐳 https://hub.docker.com/r/$REGISTRY/cofry-frontend"
echo ""
echo "  Próximo passo — No Killercoda:"
echo "  git clone https://github.com/RomarioSG1998/integracao_cofry.git cofry"
echo "  cd cofry"
echo "  vcluster create cofry-vcluster -n vcluster-cofry --driver helm"
echo "  kubectl apply -k ./k8s/base"
echo "=============================================="
