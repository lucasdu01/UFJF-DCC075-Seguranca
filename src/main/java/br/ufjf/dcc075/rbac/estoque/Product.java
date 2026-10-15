package br.ufjf.dcc075.rbac.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Recurso protegido pelo RBAC: um produto do estoque.
 *
 * As regras de negócio (não vender mais do que há em estoque, preço
 * positivo...) ficam aqui dentro. Isso é separado da AUTORIZAÇÃO: mesmo
 * um Gerente, que tem todas as permissões, não consegue deixar o estoque
 * negativo. Autorização responde "quem pode"; validação responde "o que é válido".
 */
@Entity
@Table(name = "produtos")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false)
    private int quantity;

    // BigDecimal para dinheiro: double tem erro de arredondamento (0.1 + 0.2 != 0.3).
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Construtor exigido pelo JPA; não usar no código da aplicação. */
    protected Product() {
    }

    public Product(String name, int quantity, BigDecimal price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public void removeStock(int amount) {
        requirePositive(amount);
        if (amount > quantity) {
            throw new IllegalArgumentException(
                    "estoque insuficiente de '%s' (disponível: %d)".formatted(name, quantity));
        }
        quantity -= amount;
    }

    public void addStock(int amount) {
        requirePositive(amount);
        quantity += amount;
    }

    public void changePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() <= 0) {
            throw new IllegalArgumentException("o preço deve ser maior que zero");
        }
        price = newPrice;
    }

    private static void requirePositive(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("a quantidade deve ser maior que zero");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
