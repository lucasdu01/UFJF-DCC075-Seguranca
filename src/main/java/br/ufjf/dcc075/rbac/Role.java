package br.ufjf.dcc075.rbac;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * R (Roles) do modelo RBAC do NIST.
 *
 * Um papel é um agrupamento de permissões que corresponde a uma função
 * na organização (Vendedor, Estoquista, Gerente).
 *
 * O conjunto "permissions" é a relação PA (Permission Assignment):
 * papel -> permissões. No banco vira a tabela "papel_permissoes".
 */
@Entity
@Table(name = "papeis")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(nullable = false)
    private String description;

    // @ElementCollection: coleção de valores simples (não entidades).
    // EnumType.STRING grava o NOME da permissão ("REGISTRAR_VENDA") e não
    // a posição (0, 1, 2...). Com ORDINAL, reordenar o enum corromperia
    // silenciosamente as permissões já gravadas — um bug de segurança.
    // LAZY: só carrega quando for pedido (via @EntityGraph nos repositórios).
    @ElementCollection
    @CollectionTable(name = "papel_permissoes", joinColumns = @JoinColumn(name = "papel_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permissao", nullable = false, length = 40)
    private Set<Permission> permissions = new HashSet<>();

    /** Construtor exigido pelo JPA; não usar no código da aplicação. */
    protected Role() {
    }

    public Role(String name, String description, Set<Permission> permissions) {
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>(permissions);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /** Visão somente leitura: ninguém de fora altera as permissões sem passar pela entidade. */
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    /** Conveniência para a tela da matriz papel x permissão. */
    public boolean grants(Permission permission) {
        return permissions.contains(permission);
    }

    /** Cópia defensiva em EnumSet (estrutura mais eficiente para enums). */
    Set<Permission> permissionsCopy() {
        return permissions.isEmpty() ? EnumSet.noneOf(Permission.class) : EnumSet.copyOf(permissions);
    }
}
