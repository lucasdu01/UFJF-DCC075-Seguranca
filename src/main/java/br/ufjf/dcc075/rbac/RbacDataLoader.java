package br.ufjf.dcc075.rbac;

import br.ufjf.dcc075.rbac.estoque.Product;
import br.ufjf.dcc075.rbac.estoque.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import java.util.Locale;

import static br.ufjf.dcc075.rbac.Permission.ALTERAR_PRECO;
import static br.ufjf.dcc075.rbac.Permission.REGISTRAR_ENTRADA;
import static br.ufjf.dcc075.rbac.Permission.REGISTRAR_VENDA;
import static br.ufjf.dcc075.rbac.Permission.VISUALIZAR_ESTOQUE;

/**
 * Popula o banco H2 (em memória) com o cenário de estoque ao iniciar a aplicação.
 *
 * Matriz papel x permissão (relação PA), seguindo o MENOR PRIVILÉGIO:
 *
 *                 VISUALIZAR  VENDA  ENTRADA  PREÇO
 *   VENDEDOR          X         X
 *   ESTOQUISTA        X                 X
 *   GERENTE           X         X       X       X
 *
 * Usuários (relação UA):
 *   ana    -> VENDEDOR
 *   bruno  -> ESTOQUISTA
 *   carla  -> GERENTE
 *   diego  -> VENDEDOR + ESTOQUISTA  (mostra a união de permissões)
 *   eva    -> nenhum papel           (mostra o default deny)
 */
@Component
public class RbacDataLoader implements CommandLineRunner {

    private final RoleRepository roles;
    private final AppUserRepository users;
    private final ProductRepository products;
    private final String oauthTestEmail;

    public RbacDataLoader(RoleRepository roles, AppUserRepository users, ProductRepository products,
                          @Value("${dcc075.oauth.test-email}") String oauthTestEmail) {
        this.roles = roles;
        this.users = users;
        this.products = products;
        this.oauthTestEmail = oauthTestEmail;
    }

    // no run(), depois de users.save(new AppUser("eva", ...)):
// Atividade 02: usuário autenticado pelo Google. O username é o e-mail,
// que é o atributo recebido do Google usado para achar os papéis.
// Vem do .env para não expor o e-mail no repositório público.


    @Override
    @Transactional
    public void run(String... args) {
        Role vendedor = roles.save(new Role("VENDEDOR",
                "Atende clientes e registra vendas",
                EnumSet.of(VISUALIZAR_ESTOQUE, REGISTRAR_VENDA)));

        Role estoquista = roles.save(new Role("ESTOQUISTA",
                "Recebe mercadorias e mantém o estoque",
                EnumSet.of(VISUALIZAR_ESTOQUE, REGISTRAR_ENTRADA)));

        Role gerente = roles.save(new Role("GERENTE",
                "Responsável pela loja; define preços",
                EnumSet.allOf(Permission.class)));

        users.save(new AppUser("ana", "Ana", Set.of(vendedor)));
        users.save(new AppUser("bruno", "Bruno", Set.of(estoquista)));
        users.save(new AppUser("carla", "Carla", Set.of(gerente)));
        users.save(new AppUser("diego", "Diego", Set.of(vendedor, estoquista)));
        users.save(new AppUser("eva", "Eva", Set.of()));

        if (!oauthTestEmail.isBlank()) {
            users.save(new AppUser(oauthTestEmail.trim().toLowerCase(Locale.ROOT), "Usuário Google", Set.of(gerente)));
        }
        products.save(new Product("Arroz 5kg", 40, new BigDecimal("27.90")));
        products.save(new Product("Feijão 1kg", 60, new BigDecimal("8.49")));
        products.save(new Product("Café 500g", 25, new BigDecimal("18.90")));
    }
}
