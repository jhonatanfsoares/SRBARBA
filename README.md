# Sr. Barba — Sistema de Agendamento e Gestão de Barbearia

Sistema web em **Java 21 + Spring Boot 3.5** para a barbearia **Sr. Barba**: clientes criam conta,
fazem login e marcam horário; a gerência controla a agenda, os clientes, os serviços, a equipe e o faturamento.

> Todos os dados da empresa, clientes e atendimentos são **fictícios** (gerados na primeira execução).

---

## 1. Como executar

Pré-requisitos: **JDK 21** e **Maven 3.9+** (ou abra a pasta no IntelliJ/Eclipse/VS Code como projeto Maven).

```bash
mvn spring-boot:run
# ou
mvn package && java -jar target/srbarba-1.0.0.jar
```

Acesse **http://localhost:8080**

| Perfil        | E-mail                 | Senha    |
|---------------|------------------------|----------|
| Administrador | admin@srbarba.com.br   | admin123 |
| Cliente demo  | cliente@email.com      | 123456   |

Os outros 28 clientes fictícios também usam a senha `123456`
(ex.: `rafael.souza@email.com`).

O banco de desenvolvimento é um H2 em arquivo (`./data/srbarba.mv.db`); apague a pasta `data/` para recriar os dados.
Para produção com PostgreSQL:

```bash
DB_URL=jdbc:postgresql://host:5432/srbarba DB_USER=... DB_PASSWORD=... \
java -jar target/srbarba-1.0.0.jar --spring.profiles.active=prod
```

Testes: `mvn test` (regras de agenda, validação de CPF e teste de integração das telas e da segurança).

---

## 2. Dados da empresa (fictícios)

| Item                    | Valor |
|-------------------------|-------|
| Razão social            | Sr. Barba Barbearia e Estética Masculina Ltda. |
| Nome fantasia           | Sr. Barba |
| CNPJ                    | 45.723.174/0001-10 |
| Inscrição municipal     | 1.234.567-8 |
| CNAE                    | 9602-5/01 – Cabeleireiros, manicure e pedicure |
| Regime tributário       | Simples Nacional |
| Alvará de funcionamento | ALV-2021/004587 (válido até 12/2027) |
| Licença sanitária       | CMVS 355030801-960-000123-1-0 |
| Responsável legal       | Antônio Barbosa |
| Fundação                | 15/03/2021 · Capital social R$ 80.000,00 |
| Endereço                | Rua Augusta, 1520 – Consolação, São Paulo/SP – CEP 01304-001 |
| Contato                 | (11) 3456-7890 · WhatsApp (11) 98765-4321 · contato@srbarba.com.br · @srbarba.oficial |
| Funcionamento           | Terça a sábado, 09:00 às 20:00 · 3 cadeiras · agenda em blocos de 15 min |
| Equipe                  | Antônio Barbosa (barba e navalha, 50% de comissão), Jefferson Silva (degradê, 40%), Marcos Vinícius (freestyle, 40%) |
| Clientes cadastrados    | 29 clientes iniciais + histórico de ~45 dias de atendimentos |

**Tabela de serviços**

| Serviço               | Preço    | Duração |
|-----------------------|----------|---------|
| Corte masculino       | R$ 45,00 | 30 min  |
| Barba completa        | R$ 35,00 | 30 min  |
| Corte + Barba         | R$ 70,00 | 60 min  |
| Degradê (fade)        | R$ 50,00 | 45 min  |
| Sobrancelha           | R$ 15,00 | 15 min  |
| Pigmentação de barba  | R$ 40,00 | 30 min  |
| Corte infantil        | R$ 35,00 | 30 min  |
| Hidratação capilar    | R$ 30,00 | 30 min  |

---

## 3. Requisitos

### Requisitos funcionais

| ID    | Requisito |
|-------|-----------|
| RF01  | O visitante vê a página pública com serviços, preços, equipe, endereço e horário. |
| RF02  | O cliente se cadastra com nome, e-mail, CPF, telefone, data de nascimento e senha, aceitando os termos (LGPD) e opcionalmente as comunicações. |
| RF03  | Login por e-mail e senha; cada perfil (cliente/admin) é levado para sua área. Logout. |
| RF04  | O cliente agenda escolhendo serviço → barbeiro → data → horário livre, com observação opcional. |
| RF05  | O cliente vê seus agendamentos (histórico e futuros) e cancela dentro do prazo. |
| RF06  | O admin vê o painel: total de clientes, novos no mês, agendados hoje e na semana, faturamento, ticket médio, cancelamentos, serviços mais vendidos e produção/comissão por barbeiro. |
| RF07  | O admin consulta a agenda por dia e dá baixa: concluir (com forma de pagamento), falta ou cancelamento. |
| RF08  | O admin busca clientes por nome, e-mail, telefone ou CPF e bloqueia/reativa contas. Aniversariantes do mês são destacados. |
| RF09  | O admin cadastra/edita serviços (preço, duração, ativo). |
| RF10  | O admin cadastra barbeiros (especialidade, comissão) e os ativa/desativa. |
| RF11  | O admin consulta os dados cadastrais e operacionais da empresa. |

### Regras de negócio

| ID    | Regra |
|-------|-------|
| RN01  | E-mail e CPF são únicos; CPF validado pelos dígitos verificadores. |
| RN02  | Idade mínima de 12 anos para cadastro. Senha com no mínimo 6 caracteres, guardada com BCrypt. |
| RN03  | Agendamentos só em dias de funcionamento (terça a sábado), dentro do expediente, em blocos de 15 min. |
| RN04  | O serviço precisa terminar até o horário de fechamento (a duração vem do serviço). |
| RN05  | Um barbeiro não pode ter atendimentos sobrepostos; o cliente também não. |
| RN06  | Agenda aberta para os próximos 60 dias; não se agenda no passado. |
| RN07  | Máximo de 3 agendamentos em aberto por cliente. |
| RN08  | Cancelamento pelo cliente só com 2 h de antecedência (configurável na empresa). |
| RN09  | O preço é congelado no agendamento; mudar a tabela não altera agendamentos antigos. |
| RN10  | Só agendamentos "Agendado" podem mudar de status; concluir exige forma de pagamento. |
| RN11  | Faturamento e comissões consideram apenas atendimentos concluídos. |
| RN12  | Cliente bloqueado não consegue entrar nem agendar. |

### Requisitos não funcionais

- **Segurança:** Spring Security, senhas com BCrypt, proteção CSRF em todos os formulários, controle de acesso por perfil (`/admin/**` só ADMIN, `/cliente/**` só CLIENTE).
- **Privacidade (LGPD):** aceite explícito de termos e consentimento separado para comunicações.
- **Usabilidade:** interface responsiva (celular e desktop), mensagens de erro em português.
- **Portabilidade:** H2 em desenvolvimento, PostgreSQL em produção, só trocando o perfil.
- **Testabilidade:** relógio (`Clock`) injetado para testar regras de horário; testes unitários e de integração.
- **Fuso horário:** toda a agenda trabalha no horário de Brasília (`America/Sao_Paulo`).

---

## 4. Arquitetura

Arquitetura **em camadas (MVC)**, monolito modular, renderização no servidor com Thymeleaf.

```
 Navegador
    │  HTTP (HTML + formulários)
    ▼
┌──────────────────────────────────────────────────────────────┐
│ Segurança (Spring Security)  login · sessão · CSRF · perfis  │
├──────────────────────────────────────────────────────────────┤
│ web/          Controllers + forms (DTOs)  → templates/       │
│   HomeController · AuthController · ClienteController ·      │
│   AdminController · GlobalControllerAdvice                   │
├──────────────────────────────────────────────────────────────┤
│ service/      Regras de negócio, transações                  │
│   UsuarioService · AgendamentoService · DashboardService ·   │
│   EmpresaService · CpfUtil · RegraNegocioException           │
├──────────────────────────────────────────────────────────────┤
│ repository/   Spring Data JPA (consultas JPQL)               │
├──────────────────────────────────────────────────────────────┤
│ domain/       Entidades JPA e enums                          │
└──────────────────────────────────────────────────────────────┘
    │  JDBC / Hibernate
    ▼
 H2 (dev)  |  PostgreSQL (prod)
```

**Responsabilidade de cada camada**

- **domain** — o modelo do negócio: `Empresa`, `Usuario`, `Barbeiro`, `Servico`, `Agendamento` e os enums
  `Perfil`, `StatusAgendamento`, `FormaPagamento`. Regras pequenas e próprias da entidade ficam nela
  (ex.: `Agendamento.conflitaCom()`, `Usuario.isAniversarianteDoMes()`).
- **repository** — acesso a dados com interfaces Spring Data; consultas de agenda, conflito, faturamento e rankings.
- **service** — as regras de negócio (RN01–RN12). Os controllers nunca falam direto com regras; um erro de regra
  vira `RegraNegocioException`, que a tela mostra ao usuário.
- **web** — recebe a requisição, valida o formulário (Bean Validation), chama o serviço e escolhe a tela.
  Os *forms* (`CadastroClienteForm`, `AgendamentoForm`) separam o que vem da tela das entidades.
- **security** — `UsuarioDetailsService` carrega o usuário pelo e-mail; `SecurityConfig` define rotas e perfis.
- **config** — beans (`PasswordEncoder`, `Clock`) e `DataInitializer`, que gera os dados fictícios.

### Modelo de dados

```
 EMPRESA (1 registro)          USUARIO                         BARBEIRO
 ─────────────────────         ──────────────────────          ─────────────────────
 id                            id                              id
 razao_social, cnpj ...        nome, email (único)             nome, especialidade
 endereço, contatos            senha (BCrypt), cpf (único)     telefone
 horario_abertura/fechamento   telefone, data_nascimento       percentual_comissao
 dias_funcionamento            perfil (CLIENTE|ADMIN)          ativo
 intervalo_agenda_minutos      ativo, aceita_comunicacoes
 antecedencia_cancelamento     data_cadastro                   SERVICO
 quantidade_cadeiras                 │ 1                       ─────────────────────
                                     │                         id, nome, descricao
                                     │ N                       preco, duracao_minutos
                               AGENDAMENTO                     ativo
                               ──────────────────────────            │ 1
                               id                                    │
                               cliente_id   → USUARIO     N ─────────┘
                               barbeiro_id  → BARBEIRO    N ── 1 BARBEIRO
                               servico_id   → SERVICO
                               data_hora_inicio, data_hora_fim
                               valor (preço congelado)
                               status (AGENDADO|CONCLUIDO|CANCELADO|NAO_COMPARECEU)
                               forma_pagamento, observacao, criado_em
```

### Ciclo de vida do agendamento

```
            cliente agenda
                 │
                 ▼
            ┌──────────┐  admin conclui (+ pagamento)  ┌───────────┐
            │ AGENDADO │──────────────────────────────▶│ CONCLUIDO │ → entra no faturamento
            └──────────┘                               └───────────┘
              │      │  admin marca falta              ┌────────────────┐
              │      └────────────────────────────────▶│ NAO_COMPARECEU │
              │ cliente (≥ 2h antes) ou admin          └────────────────┘
              ▼
        ┌───────────┐
        │ CANCELADO │ → libera o horário
        └───────────┘
```

### Fluxo de um agendamento

1. `GET /cliente/agendar?servicoId&barbeiroId&data` → `AgendamentoService.horariosDisponiveis()` monta a grade de
   15 em 15 min do expediente, remove o que conflita com agendamentos do barbeiro e o que já passou.
2. O cliente escolhe o horário → `POST /cliente/agendar` → `AgendamentoService.agendar()` revalida tudo
   (RN03–RN07), porque outro cliente pode ter pego o horário entre a consulta e a confirmação.
3. Salvo com o preço atual do serviço e status `AGENDADO`.

### Rotas

| Rota | Acesso | Função |
|------|--------|--------|
| `GET /` | público | Página da barbearia |
| `GET/POST /login`, `POST /logout` | público | Login / sair |
| `GET/POST /cadastro` | público | Cadastro de cliente |
| `GET /cliente` | CLIENTE | Meus agendamentos e meus dados |
| `GET/POST /cliente/agendar` | CLIENTE | Novo agendamento |
| `POST /cliente/agendamentos/{id}/cancelar` | CLIENTE | Cancelar |
| `GET /admin` | ADMIN | Painel com indicadores |
| `GET /admin/agenda?data=` · `POST /admin/agendamentos/{id}/status` | ADMIN | Agenda do dia e baixa |
| `GET /admin/clientes?q=` · `POST /admin/clientes/{id}/ativo` | ADMIN | Clientes |
| `GET/POST /admin/servicos` · `GET /admin/servicos/{id}` | ADMIN | Serviços |
| `GET/POST /admin/barbeiros` · `POST /admin/barbeiros/{id}/ativo` | ADMIN | Equipe |
| `GET /admin/empresa` | ADMIN | Dados da empresa |

### Estrutura de pastas

```
srbarba/
├── pom.xml
├── README.md
└── src/
    ├── main/java/br/com/srbarba/
    │   ├── SrBarbaApplication.java
    │   ├── config/      SecurityConfig, DataInitializer
    │   ├── domain/      Empresa, Usuario, Barbeiro, Servico, Agendamento, enums
    │   ├── repository/  *Repository (Spring Data JPA)
    │   ├── security/    UsuarioDetailsService
    │   ├── service/     UsuarioService, AgendamentoService, DashboardService, EmpresaService, CpfUtil
    │   └── web/         controllers, GlobalControllerAdvice, form/ (DTOs)
    ├── main/resources/
    │   ├── application.yml
    │   ├── static/css/app.css
    │   └── templates/   index, auth/, cliente/, admin/, fragments/layout
    └── test/java/br/com/srbarba/
        ├── SrBarbaApplicationTests.java    (integração: telas e segurança)
        └── service/                         (regras de agenda e CPF)
```

### Tecnologias

Java 21 · Spring Boot 3.5 (Web, Data JPA, Security, Validation, Thymeleaf) · Hibernate · H2 · PostgreSQL ·
JUnit 5 · Mockito.

---

## 5. Próximos passos sugeridos

- Lembrete automático por WhatsApp/e-mail na véspera (`@Scheduled` + API de mensagens).
- Recuperação de senha por e-mail.
- Tela para o admin editar os dados da empresa e o horário de funcionamento.
- Folga/bloqueio de agenda por barbeiro e feriados.
- API REST (`/api/**`) para um app de celular.
- Migrações de banco com Flyway no lugar de `ddl-auto=update`.
- Programa de fidelidade (ex.: a cada 10 cortes, 1 grátis).
