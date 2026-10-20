package br.ufjf.dcc075.oauth;

import br.ufjf.dcc075.rbac.AccessControlService;
import br.ufjf.dcc075.rbac.Permission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Página autenticada da atividade 02: mostra o resultado do controle de acesso.
 *
 * O OidcUser é montado pelo Spring a partir do ID Token JÁ VALIDADO
 * (assinatura, emissor, audiência e expiração) e fica guardado na sessão
 * do servidor. Diferente do tutorial, nome e e-mail NÃO trafegam pela URL,
 * então não podem ser forjados pelo usuário.
 */
@Controller
public class OAuthController {

    private static final String ROLE_PREFIX = "ROLE_";

    private final AccessControlService accessControl;

    public OAuthController(AccessControlService accessControl) {
        this.accessControl = accessControl;
    }

    @GetMapping("/oauth")
    public String page(@AuthenticationPrincipal OidcUser user, Model model) {
        // Papéis definidos NO LOGIN pelo GoogleRbacUserService (authorities "ROLE_*").
        // Usar a sessão como fonte garante que a checagem de email_verified foi feita;
        // consultar o banco direto pelo e-mail pularia essa validação.
        Set<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .collect(Collectors.toCollection(TreeSet::new)); // ordenado, para exibição estável

        // Permissões efetivas = união das permissões dos papéis (relação PA da atividade 01).
        // Sem papel => conjunto vazio => tudo negado (default deny).
        Set<Permission> permissions = EnumSet.noneOf(Permission.class);
        accessControl.listRoles().stream()
                .filter(role -> roles.contains(role.getName()))
                .forEach(role -> permissions.addAll(role.getPermissions()));

        // Dados da AUTENTICAÇÃO (Google)
        model.addAttribute("name", user.getFullName());            // claim "name"
        model.addAttribute("email", user.getEmail());              // claim "email": atributo usado no RBAC
        model.addAttribute("emailVerified", user.getEmailVerified()); // claim "email_verified"
        // Resultado da AUTORIZAÇÃO (RBAC)
        model.addAttribute("roles", roles);
        model.addAttribute("permissions", permissions);
        model.addAttribute("allPermissions", Permission.values());
        return "oauth/index";
    }
}