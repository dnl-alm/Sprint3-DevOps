# YourPetHealth API - Sistema de Gestão Veterinária

Projeto desenvolvido em Java utilizando Spring Boot, Maven, JPA/Hibernate e Oracle Database para gerenciamento de responsáveis, pets, consultas veterinárias e histórico clínico através de operações CRUD (Create, Read, Update e Delete).

---

# Desenvolvido por

- Guilherme Cintra RM562850
- Erick de Faria Gama RM561951
- Matheus Nascimento Corregio RM563765
- Pedro Fonseca de Almeida RM563466
- Daniel Fonseca de Almeida RM563045

---

# Objetivo

A aplicação tem como objetivo auxiliar no gerenciamento clínico veterinário, permitindo:

- Cadastro de responsáveis
- Cadastro de veterinários
- Cadastro de pets
- Agendamento e gerenciamento de consultas
- Controle de histórico clínico dos pets

A API foi desenvolvida utilizando arquitetura REST e persistência em banco de dados Oracle.

---

## Arquitetura

- **API**: Java 21 + Spring Boot, arquitetura REST em camadas (controller/service/repository), com Spring Security (JWT) protegendo os endpoints `/api/**` e Flyway versionando o schema.
- **Banco de dados**: Oracle **externo** da FIAP — não é provisionado por este script nem roda dentro do App Service. A API apenas se conecta a ele via variáveis de ambiente (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`), configuradas diretamente no Web App.
- **Hospedagem**: Azure App Service (Linux), plano F1 (free tier), runtime Java 21.
- **Observabilidade**: Azure Application Insights, conectado ao Web App para telemetria e logs.
- **CI/CD**: GitHub Actions, configurado automaticamente pelo Azure CLI — build e deploy disparados a cada push na branch `main`.

O script que provisiona tudo isso está em `azure/deploy-azure.sh`.

---

## Como criar o Web App no Azure

### Pré-requisitos

- [Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli) instalado e autenticado (`az login`).
- Permissão de criação de recursos na assinatura do Azure.
- Acesso de owner/admin ao repositório no GitHub (o comando final abre um fluxo de login do GitHub para autorizar o Actions).
- Uma instância Oracle já existente e acessível (o script **não** cria o banco — só configura a API para apontar pra ele).

### Passo a passo

1. **Entre na pasta do script:**
   ```bash
   cd azure
   ```

2. **Exporte as variáveis de ambiente** usadas pelo script (nomes e valores reais já preenchidos no arquivo, não versionados em texto puro — trate como segredo):
   ```bash
   export RESOURCE_GROUP_NAME="..."
   export WEBAPP_NAME="..."
   export APP_SERVICE_PLAN="..."
   export LOCATION="..."
   export RUNTIME="JAVA:21-java21"
   export GITHUB_REPO_NAME="..."
   export BRANCH="main"
   export APP_INSIGHTS_NAME="..."
   ```

3. **Rode o script:**
   ```bash
   ./deploy-azure.sh
   ```
   Ele executa, em sequência: criação do Resource Group → Application Insights → App Service Plan (F1, Linux) → Web App → habilitação da autenticação básica do SCM → configuração das variáveis de ambiente da aplicação (incluindo a connection string do Oracle e o `JWT_SECRET`) → conexão do Web App com o Application Insights → configuração do GitHub Actions.

4. **Autorize o GitHub quando solicitado.** O último comando (`az webapp deployment github-actions add`) abre um prompt de login/autorização do GitHub no navegador — sem isso o workflow de deploy não é criado no repositório.

5. **Acompanhe o primeiro deploy.** Após a autorização, o Azure cria um workflow (`.github/workflows/...yml`) no repositório e dispara automaticamente o primeiro build/deploy. Acompanhe em GitHub → Actions.

6. **Valide a aplicação no ar:**
   ```
   https://<WEBAPP_NAME>.azurewebsites.net
   ```

### Depois do primeiro deploy

- Qualquer push na branch `main` dispara build + deploy automaticamente — não é necessário rodar o script de novo.
- Se precisar trocar algum segredo (senha do banco, `JWT_SECRET`), atualize direto em **App Service → Configuration → Application settings**, sem precisar recriar o Web App.
- Logs e métricas ficam disponíveis no recurso do Application Insights criado (`APP_INSIGHTS_NAME`), já conectado ao Web App.
