# ☸️ Cofry — Deploy em Kubernetes

Guia completo para subir o sistema Cofry em qualquer cluster Kubernetes (GKE, EKS, AKS, minikube, kind…).

---

## 📁 Estrutura dos arquivos

```
k8s/
├── deploy.sh                   ← Script automatizado (build + push + apply)
└── base/
    ├── kustomization.yaml      ← Ponto de entrada do Kustomize
    ├── namespace.yaml          ← Namespace "cofry"
    ├── secret-db.yaml          ← Credenciais do Postgres (base64)
    ├── configmap-backend.yaml  ← URL JDBC interna do cluster
    ├── pvc-db.yaml             ← Volume persistente para o Postgres (5Gi)
    ├── statefulset-db.yaml     ← Postgres 15 + Service ClusterIP
    ├── deployment-backend.yaml ← Java/Tomcat + Service ClusterIP + HPA
    ├── deployment-frontend.yaml← Angular SSR + Service LoadBalancer + HPA
    └── ingress.yaml            ← Roteamento HTTP por path (NGINX)
```

---

## 🚀 Deploy rápido (modo automático)

```bash
# 1. Autenticar no seu registry Docker
docker login docker.io   # ou ghcr.io, gcr.io, etc.

# 2. Rodar o script (substitua pelo seu usuário/organização)
./k8s/deploy.sh docker.io/meuusuario

# Com tag de versão específica:
./k8s/deploy.sh docker.io/meuusuario v1.0.0
```

O script faz automaticamente:
1. Build das imagens `cofry-backend` e `cofry-frontend`
2. Push para o registry
3. Substituição das tags nos manifests
4. `kubectl apply -k ./k8s/base`
5. Aguarda o rollout dos Deployments
6. Exibe o IP público do frontend

---

## 🔧 Deploy manual (passo a passo)

### 1. Build e push das imagens

```bash
REGISTRY="docker.io/meuusuario"
TAG="latest"

docker build -t $REGISTRY/cofry-backend:$TAG  ./Cofry-Backend-patrick
docker build -t $REGISTRY/cofry-frontend:$TAG ./Cofry-Front-main

docker push $REGISTRY/cofry-backend:$TAG
docker push $REGISTRY/cofry-frontend:$TAG
```

### 2. Atualizar as tags nos manifests

Edite os arquivos abaixo e substitua `cofry-backend:latest` / `cofry-frontend:latest` pela imagem completa com registry:

- `k8s/base/deployment-backend.yaml` → campo `image:`
- `k8s/base/deployment-frontend.yaml` → campo `image:`

### 3. Aplicar no cluster

```bash
kubectl apply -k ./k8s/base
```

### 4. Verificar os pods

```bash
kubectl get pods -n cofry -w
```

---

## 🔑 Atualizar credenciais do banco

As senhas ficam no `secret-db.yaml` codificadas em base64.  
Para gerar um novo valor:

```bash
echo -n "nova_senha" | base64
```

> ⚠️ **Em produção**, use um gerenciador de segredos como **Vault**, **AWS Secrets Manager** ou **External Secrets Operator** — nunca versione senhas reais no Git.

---

## 🌐 Expor o serviço

### Cloud (GKE / EKS / AKS)
O `Service` do frontend já é do tipo `LoadBalancer`.  
Após o apply, aguarde o IP externo:

```bash
kubectl get svc cofry-frontend-svc -n cofry -w
```

### Minikube (local)

```bash
minikube service cofry-frontend-svc -n cofry
```

### Kind (local)

```bash
kubectl port-forward svc/cofry-frontend-svc 4000:80 -n cofry
# Acesse: http://localhost:4000
```

---

## 📈 Auto-scaling (HPA)

| Componente | Mín | Máx | Gatilho CPU |
|---|---|---|---|
| `cofry-backend` | 2 | 5 | 60% |
| `cofry-frontend` | 2 | 6 | 60% |

Requer o **Metrics Server** instalado no cluster:

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

---

## 🩺 Comandos úteis

```bash
# Ver todos os recursos do namespace
kubectl get all -n cofry

# Logs do backend
kubectl logs -l app=cofry-backend -n cofry -f

# Logs do frontend
kubectl logs -l app=cofry-frontend -n cofry -f

# Reiniciar um deployment (rolling restart)
kubectl rollout restart deployment/cofry-backend -n cofry

# Verificar status do HPA
kubectl get hpa -n cofry

# Remover tudo
kubectl delete namespace cofry
```
