package br.ufjf.dcc075.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * PDP (Policy Decision Point): o ÚNICO lugar do sistema que decide se um
 * usuário pode ou não executar uma operação.
 *
 * Centralizar a decisão evita regras espalhadas e inconsistentes pelo
 * código. Os serviços de negócio (ex.: InventoryService) são os PEPs
 * (Policy Enforcement Points): eles chamam checkPermission() antes de agir.
 *
 * Princípios aplicados:
 *  - Default deny: sem usuário, usuário inexistente ou sem papel => negado.
 *  - Menor privilégio: cada papel recebe só as permissões da sua função.
 *  - Auditoria: toda decisão de acesso é registrada em log.
 */
@Service
@Transactional(readOnly = true)
public class AccessControlService {

    private static final Logger log = LoggerFactory.getLogger(AccessControlService.class);

    private final AppUserRepository users;
    private final RoleRepository roles;

    public AccessControlService(AppUserRepository users, RoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }

    public Optional<AppUser> findUser(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return users.findByUsername(username);
    }

    /** Permissões efetivas do usuário (união das permissões dos seus papéis). */
    public Set<Permission> permissionsOf(String username) {
        return findUser(username)
                .map(AppUser::effectivePermissions)
                .orElseGet(() -> EnumSet.noneOf(Permission.class)); // default deny
    }

    /** Pergunta "pode?" sem lançar exceção — útil para a interface. */
    public boolean hasPermission(String username, Permission permission) {
        return permissionsOf(username).contains(permission);
    }

    /**
     * Exige a permissão: se o usuário não a possui, registra e lança exceção.
     * É este método que os serviços de negócio chamam ANTES de executar a ação.
     */
    public void checkPermission(String username, Permission permission) {
        if (!hasPermission(username, permission)) {
            log.warn("[RBAC] NEGADO   usuario={} permissao={}", username, permission);
            throw new PermissionDeniedException(username, permission);
        }
        log.info("[RBAC] PERMITIDO usuario={} permissao={}", username, permission);
    }

    public List<AppUser> listUsers() {
        return users.findAllByOrderByUsernameAsc();
    }

    public List<Role> listRoles() {
        return roles.findAllByOrderByIdAsc();
    }
}
