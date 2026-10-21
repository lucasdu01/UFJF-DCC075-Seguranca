package br.ufjf.dcc075.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração do Spring Security para a aplicação inteira.
 * <p>
 * Regra geral: DEFAULT DENY — só as rotas listadas são públicas; qualquer
 * outra (incluindo /oauth) exige autenticação. Uma rota nova nasce protegida.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, GoogleRbacUserService googleRbacUserService) throws Exception {
        http.authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/", "/rbac", "/rbac/**", "/cesar/**", "/feistel/**", "/bbs/**", "/dh/**", "/hash/**", "/error")
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .oauth2Login(
                        oauth -> oauth
                                .userInfoEndpoint(
                                        userInfo -> userInfo
                                                .oidcUserService(googleRbacUserService)))
                .logout(logout -> logout
                        .logoutSuccessUrl("/"));
        return http.build();
    }
}