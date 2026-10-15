package br.ufjf.dcc075.rbac;

import br.ufjf.dcc075.rbac.estoque.InventoryService;
import br.ufjf.dcc075.rbac.estoque.Product;
import br.ufjf.dcc075.rbac.estoque.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;

import static br.ufjf.dcc075.rbac.Permission.ALTERAR_PRECO;
import static br.ufjf.dcc075.rbac.Permission.REGISTRAR_ENTRADA;
import static br.ufjf.dcc075.rbac.Permission.REGISTRAR_VENDA;
import static br.ufjf.dcc075.rbac.Permission.VISUALIZAR_ESTOQUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa a política RBAC usando os dados criados pelo RbacDataLoader.
 * @Transactional desfaz as alterações no banco ao fim de cada teste.
 */
@SpringBootTest
@Transactional
class RbacTest {

    @Autowired
    AccessControlService accessControl;

    @Autowired
    InventoryService inventory;

    @Autowired
    ProductRepository products;

    @Test
    void vendedorPodeVenderMasNaoAlterarPreco() {
        assertEquals(EnumSet.of(VISUALIZAR_ESTOQUE, REGISTRAR_VENDA), accessControl.permissionsOf("ana"));
        assertFalse(accessControl.hasPermission("ana", ALTERAR_PRECO));
    }

    @Test
    void usuarioComDoisPapeisRecebeUniaoDasPermissoes() {
        assertEquals(EnumSet.of(VISUALIZAR_ESTOQUE, REGISTRAR_VENDA, REGISTRAR_ENTRADA),
                accessControl.permissionsOf("diego"));
    }

    @Test
    void defaultDenyParaSemPapelInexistenteEAnonimo() {
        assertTrue(accessControl.permissionsOf("eva").isEmpty());
        assertTrue(accessControl.permissionsOf("mallory").isEmpty());
        assertTrue(accessControl.permissionsOf(null).isEmpty());
    }

    @Test
    void estoquistaNaoConsegueVenderEEstoqueNaoMuda() {
        Product arroz = products.findByName("Arroz 5kg").orElseThrow();
        int antes = arroz.getQuantity();

        assertThrows(PermissionDeniedException.class, () -> inventory.sell("bruno", arroz.getId(), 1));
        assertEquals(antes, products.findById(arroz.getId()).orElseThrow().getQuantity());
    }

    @Test
    void gerenteAlteraPreco() {
        Product cafe = products.findByName("Café 500g").orElseThrow();
        inventory.changePrice("carla", cafe.getId(), new BigDecimal("19.90"));
        assertEquals(new BigDecimal("19.90"), products.findById(cafe.getId()).orElseThrow().getPrice());
    }

    @Test
    void autorizacaoNaoDispensaValidacao() {
        Product feijao = products.findByName("Feijão 1kg").orElseThrow();
        // Gerente tem permissão, mas não pode vender mais do que existe.
        assertThrows(IllegalArgumentException.class,
                () -> inventory.sell("carla", feijao.getId(), feijao.getQuantity() + 1));
    }
}
