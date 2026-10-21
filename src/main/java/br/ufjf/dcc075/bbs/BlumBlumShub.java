package br.ufjf.dcc075.bbs;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.OptionalLong;

import static java.math.BigInteger.ONE;

/**
 * Gerador pseudoaleatório Blum Blum Shub (BBS).
 * <p>
 * n = p * q, com p e q primos e p ≡ q ≡ 3 (mod 4)  ("primos de Blum")
 * x0 = s² mod n, com 1 < s < n e mdc(s, n) = 1
 * x(i+1) = x(i)² mod n
 * bit(i) = x(i) mod 2  (bit menos significativo)
 * <p>
 * A segurança vem da dificuldade de fatorar n: prever o próximo bit é tão
 * difícil quanto fatorar n. Com p e q de 5 dígitos (como nesta atividade),
 * n é fatorado em milissegundos, então este gerador é didático, não seguro.
 */
@Service
public class BlumBlumShub {

    private static final BigInteger TRES = BigInteger.valueOf(3);
    private static final BigInteger QUATRO = BigInteger.valueOf(4);

    /** Certeza do teste de Miller-Rabin: chance de erro < 2^-50. */
    private static final int CERTEZA_PRIMO = 50;

    /** Uma premissa do algoritmo e se ela foi atendida (exibida na tela com ✓/✗). */
    public record Premissa(String descricao, boolean atendida) {}

    /** Confere todas as premissas matemáticas do BBS para os valores informados. */
    public List<Premissa> verificar(BigInteger p, BigInteger q, BigInteger s) {
        BigInteger n = p.multiply(q);
        return List.of(
                new Premissa("p é primo", p.isProbablePrime(CERTEZA_PRIMO)),
                new Premissa("q é primo", q.isProbablePrime(CERTEZA_PRIMO)),
                new Premissa("p ≠ q (se p = q, n = p² e basta tirar a raiz para fatorar)", !p.equals(q)),
                new Premissa("p ≡ 3 (mod 4)", p.mod(QUATRO).equals(TRES)),
                new Premissa("q ≡ 3 (mod 4)", q.mod(QUATRO).equals(TRES)),
                new Premissa("1 < s < n", s.compareTo(ONE) > 0 && s.compareTo(n) < 0),
                new Premissa("mdc(s, n) = 1", s.gcd(n).equals(ONE))
        );
    }

    /**
     * Gera {@code quantidade} bits como uma String de '0' e '1'.
     *
     * @throws IllegalArgumentException se alguma premissa não for atendida
     */
    public String gerarBits(BigInteger p, BigInteger q, BigInteger s, int quantidade) {
        exigirPremissas(p, q, s);
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade de bits deve ser positiva.");
        }

        BigInteger n = p.multiply(q);
        BigInteger x = s.multiply(s).mod(n);          // x0 = s² mod n

        StringBuilder bits = new StringBuilder(quantidade);
        for (int i = 0; i < quantidade; i++) {
            x = x.multiply(x).mod(n);                 // x(i+1) = x(i)² mod n
            bits.append(x.testBit(0) ? '1' : '0');    // bit menos significativo
        }
        return bits.toString();
    }

    /**
     * Conta quantos passos a sequência leva para voltar ao x0 (o período).
     * <p>
     * Com primos de Blum, x -> x² é uma permutação dos resíduos quadráticos, então
     * a sequência é puramente periódica: sempre volta exatamente ao x0, sem "cauda"
     * antes do ciclo. Basta contar os passos até x == x0.
     * <p>
     * O período real pode ter bilhões de passos; para não iterar tudo, a contagem
     * para em {@code limite}. Isso basta para garantir que N bits gerados não se repetem.
     *
     * @return o período, ou vazio se ele for maior que {@code limite}
     */
    public OptionalLong periodo(BigInteger p, BigInteger q, BigInteger s, long limite) {
        exigirPremissas(p, q, s);

        BigInteger n = p.multiply(q);
        BigInteger x0 = s.multiply(s).mod(n);
        BigInteger x = x0;
        for (long i = 1; i <= limite; i++) {
            x = x.multiply(x).mod(n);
            if (x.equals(x0)) {
                return OptionalLong.of(i);
            }
        }
        return OptionalLong.empty();
    }

    /** Lança exceção com a primeira premissa que falhar, em vez de gerar uma sequência inválida. */
    private void exigirPremissas(BigInteger p, BigInteger q, BigInteger s) {
        verificar(p, q, s).stream()
                .filter(premissa -> !premissa.atendida())
                .findFirst()
                .ifPresent(premissa -> {
                    throw new IllegalArgumentException("Premissa não atendida: " + premissa.descricao());
                });
    }
}
