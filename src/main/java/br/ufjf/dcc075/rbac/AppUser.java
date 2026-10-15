package br.ufjf.dcc075.rbac;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * U (Users) do modelo RBAC do NIST.
 *
 * O conjunto "roles" é a relação UA (User Assignment): usuário -> papéis.
 * É N:N — um usuário pode acumular papéis (ex.: Diego é Vendedor e
 * Estoquista) e um papel pode ter vários usuários. No banco vira a
 * tabela "usuario_papeis".
 *
 * Nome "AppUser" (e tabela "usuarios") porque USER é palavra reservada no H2.
 */
@Entity
@Table(name = "usuarios")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String displayName;

    @ManyToMany
    @JoinTable(
            name = "usuario_papeis",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "papel_id"))
    private Set<Role> roles = new HashSet<>();

    /** Construtor exigido pelo JPA; não usar no código da aplicação. */
    protected AppUser() {
    }

    public AppUser(String username, String displayName, Set<Role> roles) {
        this.username = username;
        this.displayName = displayName;
        this.roles = new HashSet<>(roles);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    /** Papéis em ordem alfabética, para exibição estável na tela. */
    public List<String> getRoleNames() {
        return roles.stream().map(Role::getName).sorted(Comparator.naturalOrder()).toList();
    }

    /**
     * Permissões efetivas = UNIÃO das permissões de todos os papéis do usuário.
     *
     * Este é o cálculo central do RBAC: o usuário não recebe permissões
     * diretamente, ele as "herda" dos papéis que possui.
     * Usuário sem papel => conjunto vazio => nega tudo (default deny).
     */
    public Set<Permission> effectivePermissions() {
        Set<Permission> result = EnumSet.noneOf(Permission.class);
        for (Role role : roles) {
            result.addAll(role.permissionsCopy());
        }
        return result;
    }
}
