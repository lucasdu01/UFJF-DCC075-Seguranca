package br.ufjf.dcc075.rbac.estoque;

import br.ufjf.dcc075.rbac.AccessControlService;
import br.ufjf.dcc075.rbac.Permission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * PEP (Policy Enforcement Point): as operações do estoque.
 *
 * Toda operação segue o mesmo roteiro:
 *   1. pede ao PDP (AccessControlService) para checar a permissão;
 *   2. só então toca no recurso.
 *
 * A checagem fica no SERVIÇO, e não só no controller ou na tela, porque é
 * o serviço que de fato altera os dados. Se amanhã surgir uma API REST
 * ou outro controller chamando estes métodos, a proteção continua valendo.
 *
 * A permissão é checada ANTES de buscar o produto: assim um usuário sem
 * acesso não consegue nem descobrir se um ID de produto existe
 * (a resposta seria "não encontrado" x "negado" — um vazamento de informação).
 */
@Service
public class InventoryService {

    private final ProductRepository products;
    private final AccessControlService accessControl;

    public InventoryService(ProductRepository products, AccessControlService accessControl) {
        this.products = products;
        this.accessControl = accessControl;
    }

    @Transactional(readOnly = true)
    public List<Product> listProducts(String username) {
        accessControl.checkPermission(username, Permission.VISUALIZAR_ESTOQUE);
        return products.findAllByOrderByNameAsc();
    }

    @Transactional
    public Product sell(String username, long productId, int quantity) {
        accessControl.checkPermission(username, Permission.REGISTRAR_VENDA);
        Product product = findProduct(productId);
        product.removeStock(quantity);
        // Sem save(): dentro da transação o Hibernate detecta a mudança
        // (dirty checking) e faz o UPDATE no commit.
        return product;
    }

    @Transactional
    public Product restock(String username, long productId, int quantity) {
        accessControl.checkPermission(username, Permission.REGISTRAR_ENTRADA);
        Product product = findProduct(productId);
        product.addStock(quantity);
        return product;
    }

    @Transactional
    public Product changePrice(String username, long productId, BigDecimal newPrice) {
        accessControl.checkPermission(username, Permission.ALTERAR_PRECO);
        Product product = findProduct(productId);
        product.changePrice(newPrice);
        return product;
    }

    private Product findProduct(long productId) {
        return products.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("produto não encontrado"));
    }
}
