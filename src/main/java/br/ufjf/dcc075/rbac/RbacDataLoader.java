package br.ufjf.dcc075.rbac;

import br.ufjf.dcc075.rbac.estoque.Product;
import br.ufjf.dcc075.rbac.estoque.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

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

    public RbacDataLoader(RoleRepository roles, AppUserRepository users, ProductRepository products) {
        this.roles = roles;
        this.users = users;
        this.products = products;
    }

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

        products.save(new Product("Arroz 5kg", 40, new BigDecimal("27.90")));
        products.save(new Product("Feijão 1kg", 60, new BigDecimal("8.49")));
        products.save(new Product("Café 500g", 25, new BigDecimal("18.90")));
    }
}
