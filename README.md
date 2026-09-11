# YourPetHealth API - Sistema de Gestão Veterinária

Aplicação web e API REST para gestão de clínica veterinária, desenvolvida em Java com Spring Boot, Maven, JPA/Hibernate e Oracle Database. O sistema permite que tutores cadastrem seus pets e agendem consultas, e que veterinários consultem sua agenda, realizem atendimentos e registrem o histórico clínico dos animais.

---

# Objetivo

A aplicação auxilia no acompanhamento clínico contínuo de animais de estimação, permitindo:

- Cadastro e autenticação de tutores e veterinários
- Gestão dos pets de cada tutor
- Agendamento de consultas com validação de disponibilidade
- Registro de atendimentos pelo veterinário
- Histórico clínico gerado automaticamente a cada consulta concluída

A API foi desenvolvida utilizando arquitetura REST e persistência em banco de dados Oracle.

---

# Arquitetura da Solução

## Visão geral

A solução segue um modelo de **aplicação monolítica hospedada em PaaS com banco de dados externo gerenciado**. Não há infraestrutura de servidor sob nossa responsabilidade: o Azure App Service cuida do runtime Java e o Oracle Cloud cuida do banco. O que gerenciamos é o *código* e a *configuração*.

Essa escolha é deliberada. Para uma API CRUD com um único domínio coeso, containerizar manualmente ou quebrar em microsserviços adicionaria custo operacional sem benefício real — o App Service já resolve build, deploy, escala vertical, HTTPS e reinício automático.

```mermaid
graph TB
    subgraph CLIENTES[" CLIENTES  "]
        direction LR
        BROWSER("Navegador<br/><small>interface web</small>")
        APICLIENT("Clientes REST<br/><small>mobile · Postman</small>")
    end

    subgraph GITHUB[" GITHUB  "]
        direction LR
        REPO("Repositório<br/><small>branch main</small>")
        ACTIONS("GitHub Actions<br/><small>build Maven + deploy</small>")
    end

    subgraph AZURE[" MICROSOFT AZURE — Resource Group "]
        direction TB
        WEBAPP("<b>Azure App Service</b><br/><small>Java 21 · Spring Boot · HTTPS</small>")
        PLAN("App Service Plan<br/><small>Linux · SKU F1</small>")
        SCM("Endpoint SCM / Kudu<br/><small>publish profile</small>")
        INSIGHTS("Application Insights<br/><small>métricas · logs · traces</small>")
    end

    subgraph ORACLE[" ORACLE FIAP  "]
        DB[("Oracle Database<br/><small></small>")]
    end

    BROWSER -->|HTTPS 443| WEBAPP
    APICLIENT -->|HTTPS 443<br/>Bearer JWT| WEBAPP

    REPO -->|push na main| ACTIONS
    ACTIONS -->|deploy do .jar| SCM
    SCM --> WEBAPP

    PLAN -.->|hospeda| WEBAPP
    WEBAPP -->|JDBC 1521| DB
    WEBAPP -->|telemetria| INSIGHTS

    classDef cliente fill:#E3F2FD,stroke:#1976D2,stroke-width:2px,color:#0D47A1
    classDef github fill:#EDE7F6,stroke:#5E35B1,stroke-width:2px,color:#311B92
    classDef azure fill:#E1F5FE,stroke:#0288D1,stroke-width:2px,color:#01579B
    classDef principal fill:#B3E5FC,stroke:#0277BD,stroke-width:3px,color:#01579B
    classDef banco fill:#FFEBEE,stroke:#C62828,stroke-width:2px,color:#B71C1C

    class BROWSER,APICLIENT cliente
    class REPO,ACTIONS github
    class PLAN,SCM,INSIGHTS azure
    class WEBAPP principal
    class DB banco

    style CLIENTES fill:#FAFAFA,stroke:#BDBDBD,stroke-width:2px
    style GITHUB fill:#FAFAFA,stroke:#BDBDBD,stroke-width:2px
    style AZURE fill:#F5FBFF,stroke:#0288D1,stroke-width:2px
    style ORACLE fill:#FFF8F8,stroke:#C62828,stroke-width:2px
```

## Recursos utilizados

**Microsoft Azure**
- **Resource Group** — agrupa todos os recursos da solução
- **App Service Plan** — Linux, SKU F1 (free tier)
- **App Service (Web App)** — hospeda o `.jar` do Spring Boot, runtime Java 21
- **Application Insights** — observabilidade da aplicação

**Externos**
- **GitHub Actions** — pipeline de CI/CD (build Maven e deploy)
- **Oracle FIAP** — banco de dados da aplicação

> O banco **não** é provisionado pelo script de deploy e **não** roda dentro do App Service. Ele já existe no Oracle Cloud e a aplicação se conecta a ele por JDBC, com credenciais injetadas como variáveis de ambiente. Destruir e recriar todo o ambiente Azure não afeta os dados.

## Camadas da aplicação

Dentro do App Service, a aplicação segue arquitetura em camadas:

```mermaid
graph LR
    REQ("Requisição<br/>HTTP") --> SEC("Spring Security<br/><small>filtro JWT</small>")
    SEC --> CTRL("Controllers<br/><small>/api/**</small>")
    CTRL --> SRV("Services<br/><small>regras de negócio</small>")
    SRV --> RPO("Repositories<br/><small>Spring Data JPA</small>")
    RPO --> HIB("Hibernate")
    HIB --> DB[("Oracle<br/>Cloud")]

    FLY("Flyway<br/><small>na inicialização</small>") -.->|migrations versionadas| DB

    classDef entrada fill:#FFF3E0,stroke:#EF6C00,stroke-width:2px,color:#E65100
    classDef seguranca fill:#FCE4EC,stroke:#AD1457,stroke-width:2px,color:#880E4F
    classDef camada fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px,color:#1B5E20
    classDef infra fill:#EDE7F6,stroke:#5E35B1,stroke-width:2px,color:#311B92
    classDef banco fill:#FFEBEE,stroke:#C62828,stroke-width:2px,color:#B71C1C

    class REQ entrada
    class SEC seguranca
    class CTRL,SRV,RPO camada
    class HIB,FLY infra
    class DB banco
```

- **Spring Security** valida o token JWT antes de qualquer controller ser alcançado; rotas de autenticação e a interface web ficam fora dessa exigência.
- **Controllers** expõem os endpoints REST e traduzem HTTP em chamadas de serviço — sem regra de negócio.
- **Services** concentram as validações (ex.: disponibilidade de horário no agendamento) e a orquestração entre entidades.
- **Repositories** abstraem o acesso ao banco via Spring Data JPA.
- **Flyway** roda na subida da aplicação e garante que o schema do Oracle esteja na versão esperada pelo código — o deploy do `.jar` e a evolução do banco acontecem juntos, sem passo manual.

---

# Fluxos de Funcionamento

## 1. Fluxo de deploy (CI/CD)

É o fluxo que caracteriza a esteira de DevOps: da alteração no código até a aplicação no ar, sem intervenção manual.

```mermaid
sequenceDiagram
    autonumber
    actor DEV as Desenvolvedor
    participant GH as GitHub
    participant GA as GitHub Actions
    participant AS as App Service
    participant DB as Oracle Cloud

    DEV->>GH: git push na branch main
    GH->>GA: dispara o workflow
    GA->>GA: mvn clean package (build + testes)
    GA->>AS: publica o .jar via SCM
    AS->>AS: reinicia o container Java 21
    AS->>DB: Flyway valida/aplica migrations
    AS-->>DEV: nova versão no ar na URL pública
```

**Provisionamento inicial** (executado uma única vez, via `azure/deploy-azure.sh`):

1. Cria o Resource Group
2. Cria o Application Insights
3. Cria o App Service Plan (Linux, F1)
4. Cria o Web App com runtime Java 21
5. Habilita a autenticação básica no endpoint SCM — **é o que permite ao GitHub Actions publicar o artefato**
6. Recupera a connection string do Application Insights
7. Injeta as variáveis de ambiente da aplicação (banco, JWT, agente de telemetria)
8. Vincula o Web App ao Application Insights
9. Cria o workflow do GitHub Actions no repositório

A partir daí, o passo 1 do diagrama acima é o único necessário para novas versões.

## 2. Fluxo de observabilidade

```mermaid
graph LR
    APP("Aplicação<br/>Spring Boot") -->|agente Java<br/>auto-instrumentado| AI("Application<br/>Insights")
    AI --> M("Métricas<br/><small>latência · throughput</small>")
    AI --> L("Logs<br/><small>e exceções</small>")
    AI --> D("Dependências<br/><small>chamadas JDBC</small>")

    classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px,color:#1B5E20
    classDef ai fill:#B3E5FC,stroke:#0277BD,stroke-width:3px,color:#01579B
    classDef saida fill:#E1F5FE,stroke:#0288D1,stroke-width:2px,color:#01579B

    class APP app
    class AI ai
    class M,L,D saida
```

A instrumentação é habilitada apenas por variáveis de ambiente (`ApplicationInsightsAgent_EXTENSION_VERSION`, `XDT_MicrosoftApplicationInsights_Mode`) — **nenhuma linha de código da aplicação precisa mudar**. O agente intercepta requisições HTTP e chamadas JDBC automaticamente, o que permite identificar se uma lentidão vem da aplicação ou da latência até o Oracle Cloud.

## Segurança e configuração

| Preocupação | Como é tratada |
|---|---|
| Credenciais do banco | Variáveis de ambiente no App Service (`SPRING_DATASOURCE_*`), nunca no repositório |
| Segredo do JWT | App Setting `JWT_SECRET`, injetado em runtime |
| Tráfego externo | HTTPS fornecido pelo App Service |
| Acesso à API | Token JWT obrigatório nas rotas `/api/**` |
| Credencial de deploy | Publish profile armazenado como secret no GitHub, criado automaticamente pelo Azure CLI |

---

# Como criar o Web App no Azure

O script de provisionamento está em **`azure/deploy-azure.sh`**.

## Pré-requisitos

- Azure CLI instalado e autenticado (`az login`)
- Permissão para criar recursos na assinatura do Azure
- Acesso de administrador ao repositório no GitHub
- Banco Oracle Cloud já existente e acessível (o script **não** cria o banco)

## Passo a passo

**1. Entre na pasta do script**

```bash
cd azure
```

**2. Execute o script**

```bash
./deploy-azure.sh
```

Ele executa os nove passos de provisionamento em sequência — cada comando só roda após o anterior concluir, e o script aborta se qualquer etapa falhar.

**3. Autorize o GitHub quando solicitado**

O último comando (`az webapp deployment github-actions add`) abre um fluxo de autorização do GitHub no navegador. Sem essa autorização o workflow não é criado no repositório.

**4. Acompanhe o primeiro deploy**

O Azure cria o arquivo `.github/workflows/*.yml` no repositório e dispara o primeiro build automaticamente. Acompanhe em **GitHub → Actions**.

## Depois do primeiro deploy

- Todo push na branch `main` dispara build e deploy automaticamente — o script **não** precisa ser executado de novo.
- Para trocar segredos (senha do banco, `JWT_SECRET`), altere em **App Service → Configuration → Application settings**; o Web App reinicia sozinho.
- Métricas e logs ficam no recurso de Application Insights criado.
- Para destruir todo o ambiente: `az group delete --name $RESOURCE_GROUP_NAME`. O banco no Oracle Cloud permanece intacto.