# YourPetHealth API - Sistema de Gestão Veterinária

Aplicação web e API REST para gestão de clínica veterinária, desenvolvida em Java com Spring Boot, Maven, JPA/Hibernate e Oracle Database. O sistema permite que tutores cadastrem seus pets e agendem consultas, e que veterinários consultem sua agenda, realizem atendimentos e registrem o histórico clínico dos animais.

---

# Desenvolvido por

- Guilherme Cintra RM562850
- Erick de Faria Gama RM561951
- Matheus Nascimento Corregio RM563765
- Pedro Fonseca de Almeida RM563466
- Daniel Fonseca de Almeida RM563045

---

# Descrição do projeto

O YourPetHealth é uma plataforma que centraliza a rotina de clínicas veterinárias e de responsáveis por pets em um único sistema, substituindo controles dispersos em papel, planilhas e agendas separadas.

O sistema é composto por três partes integradas: o sistema principal, desenvolvido em Java com Spring, que guarda todos os dados, aplica as regras de negócio e controla o acesso; um serviço de análise, que avalia a situação do acompanhamento clínico de cada animal; e um aplicativo móvel, por onde tutores e veterinários usam a plataforma no dia a dia.

O sistema principal é o núcleo da solução. Ele é responsável pelo cadastro de pessoas e pets, pelo agendamento de consultas com validação automática das regras de disponibilidade, pelo registro do histórico clínico e pela autenticação com controle de acesso por perfil. Cada usuário enxerga apenas o que lhe diz respeito: um responsável nunca acessa os dados de outro, e não entra nas áreas do veterinário.

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

# Benefícios para o negócio

Redução de erros e retrabalho. O histórico clínico é gerado automaticamente na conclusão do atendimento, eliminando a digitação duplicada e o risco de divergência entre o que foi feito e o que foi registrado.

Menos conflitos de agenda. O sistema valida antecedência mínima, horário de funcionamento e disponibilidade do veterinário antes de confirmar qualquer marcação, evitando sobreposições que hoje só aparecem no dia.

Otimização da rotina administrativa. O veterinário acessa a agenda do dia e registra o atendimento pelo próprio aplicativo, sem intermediários e sem depender da recepção.

Informação centralizada e acessível. Todo o histórico do animal fica disponível para tutor e clínica a qualquer momento, acabando com a perda de dados e a busca por fichas antigas.

Melhoria no atendimento ao cliente. O tutor agenda, remarca e consulta o histórico sozinho, sem telefonema, o que reduz a carga da recepção e aumenta a autonomia dele.

---

# Arquitetura da Solução

## Visão geral

A solução segue um modelo de **aplicação monolítica hospedada em PaaS com banco de dados externo gerenciado**. Não há infraestrutura de servidor sob nossa responsabilidade: o Azure App Service cuida do runtime Java e o Oracle Cloud cuida do banco. O que gerenciamos é o *código* e a *configuração*.

![Arquitetura da solução YourPetHealth](/docs/architecture.svg)

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

Dentro do App Service, a aplicação segue arquitetura em camadas — requisição HTTP → Spring Security (filtro JWT) → Controllers (`/api/**`) → Services → Repositories (Spring Data JPA) → Hibernate → Oracle Database, com o Flyway atuando à parte, aplicando as migrations no banco a cada inicialização:

- **Spring Security** valida o token JWT antes de qualquer controller ser alcançado; rotas de autenticação e a interface web ficam fora dessa exigência.
- **Controllers** expõem os endpoints REST e traduzem HTTP em chamadas de serviço — sem regra de negócio.
- **Services** concentram as validações (ex.: disponibilidade de horário no agendamento) e a orquestração entre entidades.
- **Repositories** abstraem o acesso ao banco via Spring Data JPA.
- **Flyway** roda na subida da aplicação e garante que o schema do Oracle esteja na versão esperada pelo código — o deploy do `.jar` e a evolução do banco acontecem juntos, sem passo manual.

---

# Fluxos de Funcionamento

## 1. Fluxo de deploy (CI/CD)

É o fluxo que caracteriza a esteira de DevOps: da alteração no código até a aplicação no ar, sem intervenção manual — corresponde à seta ② do diagrama de arquitetura. Na prática: o desenvolvedor faz `git push` na branch `main` → o GitHub Actions dispara o workflow → roda `mvn clean package` (build e testes) → publica o `.jar` no App Service via SCM → o App Service reinicia o container Java 21 → o Flyway valida/aplica as migrations no Oracle → a nova versão fica disponível na URL pública.

**Provisionamento inicial** (executado uma única vez, via `azure/deploy.sh`):

1. Cria o Resource Group
2. Cria o Application Insights
3. Cria o App Service Plan (Linux, F1)
4. Cria o Web App com runtime Java 21
5. Habilita a autenticação básica no endpoint SCM — **é o que permite ao GitHub Actions publicar o artefato**
6. Recupera a connection string do Application Insights
7. Injeta as variáveis de ambiente da aplicação (banco, JWT, agente de telemetria)
8. Vincula o Web App ao Application Insights
9. Cria o workflow do GitHub Actions no repositório

A partir daí, um simples `git push` na branch `main` é suficiente para publicar novas versões — os 9 passos acima não se repetem.

## 2. Fluxo de observabilidade

Corresponde à seta ④ do diagrama: a aplicação envia métricas (latência, throughput), logs/exceções e dependências (chamadas JDBC ao Oracle) automaticamente para o Application Insights. A instrumentação é habilitada apenas por variáveis de ambiente (`ApplicationInsightsAgent_EXTENSION_VERSION`, `XDT_MicrosoftApplicationInsights_Mode`) — **nenhuma linha de código da aplicação precisa mudar**. O agente intercepta requisições HTTP e chamadas JDBC automaticamente, o que permite identificar se uma lentidão vem da aplicação ou da latência até o Oracle Cloud.

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

O script de provisionamento está em **`azure/deploy.sh`**.

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
chmod +x deploy.sh
```

```bash
./deploy.sh
```

Ele executa os nove passos de provisionamento em sequência. Cada comando só roda após o anterior concluir, e o script aborta se qualquer etapa falhar.

**3. Autorize o GitHub quando solicitado**

O último comando (`az webapp deployment github-actions add`) abre um fluxo de autorização do GitHub no navegador. Sem essa autorização o workflow não é criado no repositório.

**4. Adicione as variáveis de ambiente necessárias dentro de workflows**

Altere o arquivo `.github/workflows/*.yml` adicionando:

```
- name: Build with Maven
      run: mvn clean install
      env: 
        SPRING_DATASOURCE_URL: ${{ secrets.SPRING_DATASOURCE_URL }}
        SPRING_DATASOURCE_USERNAME: ${{ secrets.SPRING_DATASOURCE_USERNAME }}
        SPRING_DATASOURCE_PASSWORD: ${{ secrets.SPRING_DATASOURCE_PASSWORD }}
        JWT_SECRET: ${{ secrets.JWT_SECRET }}
```

**5. Acompanhe o primeiro deploy**

Acompanhe em **GitHub → Actions**.

## Depois do primeiro deploy

- Todo push na branch `main` dispara build e deploy automaticamente — o script **não** precisa ser executado de novo.
- Para trocar segredos (senha do banco, `JWT_SECRET`), altere em **App Service → Configuration → Application settings**; o Web App reinicia sozinho.
- Métricas e logs ficam no recurso de Application Insights criado.
- Para destruir todo o ambiente: `az group delete --name $RESOURCE_GROUP_NAME`. O banco no Oracle Cloud permanece intacto.