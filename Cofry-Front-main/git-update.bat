@echo off
REM Script de automação Git para atualizar o repositório
REM Uso: git-update.bat [mensagem-de-commit]

setlocal

set "COMMIT_MSG=%~1"
if "%COMMIT_MSG%"=="" set "COMMIT_MSG=chore: atualização automática"

echo === Automação Git - Cofry FrontEnd ===
echo.

REM Verifica se há alterações
echo Verificando status do repositório...
git status --short >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    git status --short | findstr /R "." >nul
    if %ERRORLEVEL% EQU 1 (
        echo Nenhuma alteração pendente. Repositório está atualizado.
        exit /b 0
    )
)

echo.
echo Alterações encontradas:
git status --short
echo.

REM Adiciona todos os arquivos
echo Adicionando arquivos ao stage...
git add .
if %ERRORLEVEL% NEQ 0 (
    echo Erro ao adicionar arquivos
    exit /b 1
)
echo Arquivos adicionados
echo.

REM Faz commit
echo Criando commit...
echo Mensagem: %COMMIT_MSG%
git commit -m "%COMMIT_MSG%"
if %ERRORLEVEL% NEQ 0 (
    echo Erro ao criar commit
    exit /b 1
)
echo Commit criado com sucesso
echo.

REM Faz push
echo Enviando para o GitHub...
git push origin main
if %ERRORLEVEL% NEQ 0 (
    echo Erro ao enviar para o repositório remoto
    exit /b 1
)
echo Atualização enviada com sucesso!
echo.

echo === Processo concluído ===

endlocal


