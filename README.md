# DCC075 – Segurança em Sistemas Computacionais

Atividades práticas da disciplina DCC075 (Ciência da Computação – UFJF), reunidas em uma única aplicação web.

A página inicial mostra um menu com as atividades. Cada uma tem a própria tela, e o código de cada uma fica em um pacote separado, com um README próprio explicando o contexto.

## Tecnologias

- Java 21
- Spring Boot 4 (Spring MVC + Thymeleaf para as telas)
- Spring Data JPA + H2 (banco em memória, recriado a cada execução)
- Spring Security + OAuth2 Client (login com Google na atividade 02)
- Maven (via Maven Wrapper, sem instalação prévia)

## Como rodar

Pré-requisito: JDK 21 instalado (`java -version`).

Para a atividade 02, copie `.env.example` para `.env` e preencha os valores
(detalhes no [README da atividade](src/main/java/br/ufjf/dcc075/oauth/README.md)).

```bash
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
```

Acesse **http://localhost:8080** e escolha uma atividade no menu.

Para rodar os testes automatizados:

```bash
./mvnw test
```

Para inspecionar o banco H2 durante a execução, os comandos SQL gerados aparecem no console (`spring.jpa.show-sql=true`).

## Atividades

| # | Atividade | Rota | Código e explicação |
|---|-----------|------|---------------------|
| 01 | RBAC – controle de acesso baseado em papéis | `/rbac` | [`rbac/`](src/main/java/br/ufjf/dcc075/rbac/README.md) |
| 02 | OAuth2 (Google) + RBAC | `/oauth` | [`oauth/`](src/main/java/br/ufjf/dcc075/oauth/README.md) |
| 03 | Cifra de César + criptoanálise por frequência | `/cesar` | em breve |
| 04 | Cifra de Feistel (16 rodadas) | `/feistel` | em breve |

## Estrutura do projeto

```
src/main/java/br/ufjf/dcc075/
├── Dcc075Application.java     ponto de entrada do Spring Boot
├── home/                      menu inicial com a lista de atividades
├── rbac/                      atividade 01 (README próprio)
└── oauth/                     atividade 02 (README próprio)

src/main/resources/
├── application.properties     configuração (banco H2, JPA)
└── templates/                 telas Thymeleaf, uma pasta por atividade

src/test/java/br/ufjf/dcc075/  testes, um pacote por atividade
```

Cada atividade é um pacote independente (organização por funcionalidade). Para avaliar uma atividade, basta abrir a pasta dela.

## Uso de Inteligência Artificial

Este projeto foi desenvolvido com o apoio do **Claude** (Anthropic), usado como assistente de estudo e de programação. De forma geral, a IA foi usada para:

- **Entender os conceitos:** explicar os temas de cada atividade, com exemplos e relação com a prática de mercado.
- **Modelar e estruturar o projeto:** definir a organização em uma única aplicação Spring Boot, com um pacote por atividade, e discutir a modelagem de cada cenário.
- **Gerar e revisar código:** criar telas, entidades, serviços e configurações, e apontar boas práticas e riscos de segurança durante o desenvolvimento.
- **Documentar:** redigir os comentários do código e os READMEs.

O grau de uso varia por atividade. Cada README de atividade tem uma seção **"Uso de IA"** dizendo o que foi feito com a ajuda da IA naquela entrega.

Todo o código foi lido, executado e testado por mim. As decisões de projeto e a responsabilidade pelo conteúdo entregue são minhas.
