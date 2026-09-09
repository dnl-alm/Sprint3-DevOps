#!/usr/bin/env bash
set -euo pipefail

RESOURCE_GROUP_NAME="rg-yourpethealth"
WEBAPP_NAME="yourpethealth-rm563045"
APP_SERVICE_PLAN="yourpethealth"
LOCATION="canadacentral"
RUNTIME="JAVA:21-java21"
GITHUB_REPO_NAME="dnl-alm/Sprint3-DevOps"
BRANCH="main"
APP_INSIGHTS_NAME="ai-yourpethealth"

echo ">> Criação do Grupo de Recursos"
az group create \
  --name "$RESOURCE_GROUP_NAME" \
  --location "$LOCATION"

echo ">> Criar Application Insights"
az monitor app-insights component create \
  --app "$APP_INSIGHTS_NAME" \
  --location "$LOCATION" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --application-type web

echo ">> Criar o Plano de Serviço"
az appservice plan create \
  --name "$APP_SERVICE_PLAN" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --location "$LOCATION" \
  --sku F1 \
  --is-linux

echo ">> Criar o Serviço de Aplicativo"
az webapp create \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --plan "$APP_SERVICE_PLAN" \
  --runtime "$RUNTIME"

echo ">> Habilitar a autenticação Básica (SCM)"
az resource update \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --namespace Microsoft.Web \
  --resource-type basicPublishingCredentialsPolicies \
  --name scm \
  --parent "sites/$WEBAPP_NAME" \
  --set properties.allow=true

echo ">> Recuperar a String de Conexão do Application Insights"
CONNECTION_STRING=$(az monitor app-insights component show \
  --app "$APP_INSIGHTS_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --query connectionString \
  --output tsv)

echo ">> Configurar as Variáveis de Ambiente do App e do Application Insights"
az webapp config appsettings set \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --settings \
    APPLICATIONINSIGHTS_CONNECTION_STRING="$CONNECTION_STRING" \
    ApplicationInsightsAgent_EXTENSION_VERSION="~3" \
    XDT_MicrosoftApplicationInsights_Mode="Recommended" \
    XDT_MicrosoftApplicationInsights_PreemptSdk="1" \
    SPRING_DATASOURCE_USERNAME="rm563045" \
    SPRING_DATASOURCE_PASSWORD="241203" \
    SPRING_DATASOURCE_URL="jdbc:oracle:thin:@//oracle.fiap.com.br:1521/ORCL" \
    JWT_SECRET="troque-este-segredo-por-um-valor-longo-e-aleatorio-em-producao"

echo ">> Criar a conexão do Web App com o Application Insights"
az monitor app-insights component connect-webapp \
  --app "$APP_INSIGHTS_NAME" \
  --web-app "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME"

echo ">> Configurar GitHub Actions para Build e Deploy automático"
az webapp deployment github-actions add \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --repo "$GITHUB_REPO_NAME" \
  --branch "$BRANCH" \
  --login-with-github

echo ">> Concluído."