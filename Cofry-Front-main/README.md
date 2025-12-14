# CofryFrontEnd

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.0.0.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Automação Git

O projeto inclui scripts automatizados para facilitar o processo de commit e push para o GitHub.

### Usando npm scripts:

**Atualização rápida (adiciona, commita e faz push automaticamente):**
```bash
npm run git:update
```

**Comandos individuais:**
```bash
npm run git:status    # Verifica o status do repositório
npm run git:add       # Adiciona todos os arquivos ao stage
npm run git:commit "sua mensagem"  # Cria um commit com mensagem personalizada
npm run git:push      # Envia para o GitHub
```

### Usando scripts diretos:

**PowerShell:**
```powershell
.\git-update.ps1
.\git-update.ps1 "feat: adiciona nova funcionalidade"
```

**CMD/Batch:**
```cmd
git-update.bat
git-update.bat "feat: adiciona nova funcionalidade"
```

### Convenções de Commit

Siga as convenções de commit para mensagens mais descritivas:
- `feat:` - Nova funcionalidade
- `fix:` - Correção de bug
- `refactor:` - Refatoração de código
- `style:` - Mudanças de formatação/estilo
- `chore:` - Tarefas de manutenção/atualização
- `docs:` - Mudanças na documentação

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
