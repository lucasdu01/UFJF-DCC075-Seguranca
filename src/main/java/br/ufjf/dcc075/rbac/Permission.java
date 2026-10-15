package br.ufjf.dcc075.rbac;

/**
 * P (Permissions) do modelo RBAC do NIST.
 *
 * Uma permissão representa UMA operação sobre UM recurso do sistema
 * (aqui, o estoque de produtos). O código de negócio só pergunta por
 * permissões; nunca pergunta "o usuário é Gerente?". Assim, mudar o que
 * um papel pode fazer não exige alterar nenhum "if" no sistema.
 *
 * Por que enum e não tabela?
 *  - O conjunto de operações é fixo e conhecido em tempo de compilação.
 *  - Um erro de digitação (ex.: REGISTAR_VENDA) nem compila.
 *  - Contra: criar uma permissão nova exige recompilar (aceitável aqui).
 */
public enum Permission {

    VISUALIZAR_ESTOQUE("Ver estoque", "Visualizar produtos, quantidades e preços"),
    REGISTRAR_VENDA("Vender", "Registrar venda (dá baixa no estoque)"),
    REGISTRAR_ENTRADA("Repor", "Registrar entrada de mercadoria (repõe estoque)"),
    ALTERAR_PRECO("Alterar preço", "Alterar o preço de um produto");

    private final String label;        // nome curto, para a interface
    private final String description;  // explicação completa

    Permission(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
