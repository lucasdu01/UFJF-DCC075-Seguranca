# DCC075 – Segurança em Sistemas Computacionais (UFJF)

Atividades práticas da disciplina reunidas em uma única aplicação Spring Boot.
Cada atividade fica em seu próprio pacote em `src/main/java/br/ufjf/dcc075/`.

## Como executar

Requisitos: Java 21 (o Maven Wrapper baixa o Maven automaticamente).

```bash
./mvnw spring-boot:run      # sobe em http://localhost:8080
./mvnw test                 # roda os testes
```

O banco é um H2 em memória, recriado a cada execução.

---

## 01 – RBAC

**Pacote:** `br.ufjf.dcc075.rbac` · **URL:** http://localhost:8080/rbac

Cenário: controle de estoque de uma loja com três papéis.

| Papel      | VISUALIZAR_ESTOQUE | REGISTRAR_VENDA | REGISTRAR_ENTRADA | ALTERAR_PRECO |
|------------|:-:|:-:|:-:|:-:|
| VENDEDOR   | ✔ | ✔ |   |   |
| ESTOQUISTA | ✔ |   | ✔ |   |
| GERENTE    | ✔ | ✔ | ✔ | ✔ |

| Usuário | Papéis | Para que serve no teste |
|---------|--------|-------------------------|
| ana     | VENDEDOR | caso básico |
| bruno   | ESTOQUISTA | caso básico |
| carla   | GERENTE | caso básico |
| diego   | VENDEDOR + ESTOQUISTA | permissões efetivas = união dos papéis |
| eva     | nenhum | default deny |

Mapeamento para o modelo RBAC0 do NIST:

| NIST | Código |
|------|--------|
| U – usuários | `AppUser` |
| R – papéis | `Role` |
| P – permissões | `Permission` (enum) |
| UA – usuário→papel | `AppUser.roles` (tabela `usuario_papeis`) |
| PA – papel→permissão | `Role.permissions` (tabela `papel_permissoes`) |
| Sessão | `HttpSession` com o usuário escolhido |

- `AccessControlService` é o **PDP** (Policy Decision Point): único lugar que decide.
- `InventoryService` é o **PEP** (Policy Enforcement Point): checa a permissão antes de cada operação.
- A autenticação é simulada (escolha do usuário sem senha); a autenticação real é a atividade 02.
