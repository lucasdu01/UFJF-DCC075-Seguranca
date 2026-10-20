package br.ufjf.dcc075.oauth;

import br.ufjf.dcc075.rbac.AccessControlService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Ponto onde o RBAC entra no fluxo OAuth2/OIDC.
 *
 * O Spring chama loadUser() logo APÓS validar o ID Token do Google
 * (autenticação concluída) e ANTES de guardar o usuário na sessão.
 * Aqui traduzimos um atributo recebido do Google (o e-mail) nos papéis
 * do RBAC da atividade 01, que viram authorities "ROLE_*".
 *
 * Autenticação (Google): QUEM é o usuário.
 * Autorização (RBAC):    O QUE ele pode fazer.
 */
@Service
public class GoogleRbacUserService extends OidcUserService {

    private final AccessControlService accessControl;

    public GoogleRbacUserService(AccessControlService accessControl) {
        this.accessControl = accessControl;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        // 1. Usuário padrão montado a partir do ID Token (já validado pelo Spring).
        OidcUser googleUser = super.loadUser(request);

        // 2. Mantém as authorities padrão (OIDC_USER, SCOPE_*) e acrescenta os papéis.
        Set<GrantedAuthority> authorities = new HashSet<>(googleUser.getAuthorities());

        // 3. Só confia no e-mail se o Google atesta que ele foi verificado;
        //    senão o e-mail não prova nada (CWE-290).
        String email = googleUser.getEmail();
        if (email != null && Boolean.TRUE.equals(googleUser.getEmailVerified())) {
            // 4. Mapeamento atributo -> papel: procura o e-mail no RBAC da atividade 01.
            //    E-mail não cadastrado => nenhum papel (default deny).
            accessControl.findUser(email.toLowerCase(Locale.ROOT))
                    .ifPresent(appUser -> appUser.getRoleNames()
                            .forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role))));
        }

        // 5. Devolve um novo OidcUser com as authorities completas; é ele que vai para a sessão.
        //    "email" = atributo usado como nome do usuário (getName()).
        return new DefaultOidcUser(authorities, googleUser.getIdToken(), googleUser.getUserInfo(), "email");
    }
}