package br.ufjf.dcc075.rbac;

/**
 * Lançada quando o usuário não possui a permissão exigida pela operação.
 *
 * É uma RuntimeException para que a verificação possa ser feita em qualquer
 * camada sem poluir assinaturas com "throws". Quem chama (o controller)
 * decide como apresentar a negação ao usuário.
 */
public class PermissionDeniedException extends RuntimeException {

    private final String username;
    private final Permission permission;

    public PermissionDeniedException(String username, Permission permission) {
        super("Acesso negado: '%s' não possui a permissão %s."
                .formatted(username == null ? "anônimo" : username, permission));
        this.username = username;
        this.permission = permission;
    }

    public String getUsername() {
        return username;
    }

    public Permission getPermission() {
        return permission;
    }
}
