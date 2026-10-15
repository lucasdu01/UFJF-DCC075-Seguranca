# Atividade 01 – RBAC

> Criar um RBAC simples, com código comentado e um cenário com ao menos 3 papéis.

## A ideia

No **RBAC** (Role-Based Access Control), as permissões não são dadas diretamente aos usuários. Elas são agrupadas em **papéis**, que correspondem a funções na organização, e o usuário recebe papéis. O que ele pode fazer é a união das permissões dos seus papéis.

Assim, quando alguém muda de função, basta trocar o papel da pessoa. E quando uma função passa a poder fazer algo novo, basta alterar o papel, sem mexer em cada usuário.

## Cenário

Estoque de uma loja, com três papéis:

| Papel | Ver estoque | Vender | Repor | Alterar preço |
|-------|:-:|:-:|:-:|:-:|
| VENDEDOR   | ✓ | ✓ |   |   |
| ESTOQUISTA | ✓ |   | ✓ |   |
| GERENTE    | ✓ | ✓ | ✓ | ✓ |

Usuários de exemplo, criados ao iniciar a aplicação:

| Usuário | Papéis | O que demonstra |
|---------|--------|-----------------|
| Ana | Vendedor | caso básico |
| Bruno | Estoquista | caso básico |
| Carla | Gerente | acesso total |
| Diego | Vendedor + Estoquista | permissões efetivas = união dos papéis |
| Eva | nenhum | sem papel, tudo é negado (*default deny*) |

## Como funciona

O código segue o modelo **RBAC0 do NIST**:

| Conceito | Classe |
|----------|--------|
| Usuários (U) | `AppUser` |
| Papéis (R) | `Role` |
| Permissões (P) | `Permission` (enum) |
| Usuário → papéis (UA) | `AppUser.roles` |
| Papel → permissões (PA) | `Role.permissions` |
| Sessão | `HttpSession` com o usuário escolhido |

O fluxo de uma ação (por exemplo, "Vender"):

```
Tela ──POST──▶ RbacController ──▶ InventoryService.sell()
                                        │
                                        ├─ 1. AccessControlService.checkPermission(usuário, REGISTRAR_VENDA)
                                        │      └─ nega → PermissionDeniedException (registrada no log)
                                        └─ 2. só então altera o produto
```

- **`AccessControlService`** é o ponto de decisão (PDP), o único lugar que responde "este usuário pode?".
- **`InventoryService`** é o ponto de aplicação (PEP): checa a permissão antes de cada operação.
- A checagem fica no servidor. Na tela, os botões sem permissão aparecem marcados, mas continuam clicáveis, para demonstrar que é o servidor quem bloqueia.

## Arquivos

```
rbac/
├── Permission.java               permissões do sistema
├── Role.java                     papel e suas permissões
├── AppUser.java                  usuário e seus papéis
├── AppUserRepository.java        acesso ao banco
├── RoleRepository.java
├── AccessControlService.java     decide se o acesso é permitido
├── PermissionDeniedException.java
├── RbacController.java           tela e sessão
├── RbacDataLoader.java           cria papéis, usuários e produtos
└── estoque/                      recurso protegido
    ├── Product.java
    ├── ProductRepository.java
    └── InventoryService.java     operações do estoque, cada uma com checagem
```

Tela: `src/main/resources/templates/rbac/index.html`
Testes: `src/test/java/br/ufjf/dcc075/rbac/RbacTest.java`

## Como rodar

Com a aplicação no ar (veja o [README principal](../../../../../../../README.md)), acesse **http://localhost:8080/rbac**:

1. Em **Sessão**, clique em um funcionário para entrar como ele.
2. Em **Estoque**, tente vender, repor ou alterar preço. O resultado aparece no topo da seção: permitido, negado ou inválido.
3. Em **Política de acesso**, veja a matriz papel × permissão e as permissões efetivas do usuário atual.

Sugestão de teste: entre como **Bruno** e tente vender. O servidor nega e o estoque não muda.

Só os testes desta atividade:

```bash
./mvnw test -Dtest=RbacTest
```

## Limitações

- A autenticação é simulada (escolhe-se o usuário, sem senha). A autenticação real é a atividade 02.
- Não há proteção CSRF nos formulários; ela chega com o Spring Security na atividade 02.
- Não há hierarquia de papéis: o Gerente recebe as permissões explicitamente, em vez de herdar de outros papéis.

## Uso de IA

Nesta atividade, a IA (Claude) foi usada para:

- explicar o modelo RBAC do NIST (usuários, papéis, permissões e as relações UA e PA) e conceitos relacionados, como menor privilégio, *default deny*, PDP e PEP;
- discutir a modelagem do cenário: os papéis Vendedor, Estoquista e Gerente, e a matriz de permissões;
- gerar a implementação completa: entidades, repositórios, serviço de controle de acesso, controller, tela e testes;
- revisar a segurança da solução, por exemplo a checagem no servidor e não só na tela, a troca do ID de sessão no login e as limitações conhecidas;
- redigir os comentários do código e este README.

O cenário e as regras de acesso foram definidos por mim. O código foi revisado e testado por mim.
