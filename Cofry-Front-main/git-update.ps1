# Script de automação Git para atualizar o repositório
# Uso: .\git-update.ps1 [mensagem-de-commit]

param(
    [string]$commitMessage = "chore: atualização automática"
)

Write-Host "=== Automação Git - Cofry FrontEnd ===" -ForegroundColor Cyan
Write-Host ""

# Verifica se há alterações
Write-Host "Verificando status do repositório..." -ForegroundColor Yellow
$status = git status --short

if ([string]::IsNullOrWhiteSpace($status)) {
    Write-Host "✓ Nenhuma alteração pendente. Repositório está atualizado." -ForegroundColor Green
    exit 0
}

Write-Host ""
Write-Host "Alterações encontradas:" -ForegroundColor Yellow
git status --short
Write-Host ""

# Adiciona todos os arquivos
Write-Host "Adicionando arquivos ao stage..." -ForegroundColor Yellow
git add .
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Erro ao adicionar arquivos" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Arquivos adicionados" -ForegroundColor Green
Write-Host ""

# Faz commit
Write-Host "Criando commit..." -ForegroundColor Yellow
Write-Host "Mensagem: $commitMessage" -ForegroundColor Gray
git commit -m "$commitMessage"
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Erro ao criar commit" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Commit criado com sucesso" -ForegroundColor Green
Write-Host ""

# Faz push
Write-Host "Enviando para o GitHub..." -ForegroundColor Yellow
git push origin main
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Erro ao enviar para o repositório remoto" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Atualização enviada com sucesso!" -ForegroundColor Green
Write-Host ""

Write-Host "=== Processo concluído ===" -ForegroundColor Cyan


