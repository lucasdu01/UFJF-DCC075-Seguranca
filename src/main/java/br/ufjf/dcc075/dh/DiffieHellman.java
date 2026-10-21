package br.ufjf.dcc075.dh;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

/**
 * Troca de chaves Diffie-Hellman.
 * <p>
 * Públicos: p (primo) e g (raiz primitiva módulo p)
 * Alice: sorteia a (secreto) e envia A = g^a mod p
 * Bob:   sorteia b (secreto) e envia B = g^b mod p
 * Segredo: s = B^a mod p = A^b mod p = g^(ab) mod p
 * <p>
 * Quem escuta a conversa vê p, g, A e B, mas para calcular s precisaria
 * descobrir a ou b a partir de A ou B: o problema do logaritmo discreto,
 * inviável com p de 2048 bits.
 */
@Service
public class DiffieHellman {

    /**
     * Primo de 2048 bits do RFC 3526 (grupo 14), usado em TLS, SSH e IPsec.
     * É um primo seguro: p = 2q + 1, com q também primo.
     */
    public static final BigInteger P_RFC3526 = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74"
            + "020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F1437"
            + "4FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"
            + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF05"
            + "98DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB"
            + "9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B"
            + "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF695581718"
            + "3995497CEA956AE515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);

    /**
     * Menor raiz primitiva módulo P_RFC3526. O gerador g = 2 do RFC NÃO é raiz
     * primitiva: ele gera só o subgrupo de ordem q (2^q ≡ 1 mod p).
     */
    public static final BigInteger G_RAIZ_PRIMITIVA = BigInteger.valueOf(11);

    /** Certeza do teste de Miller-Rabin: chance de erro < 2^-50. */
    private static final int CERTEZA_PRIMO = 50;

    private final SecureRandom random = new SecureRandom();

    /** Uma premissa e se ela foi atendida (exibida na tela com ✓/✗). */
    public record Premissa(String descricao, boolean atendida) {}

    /** Confere as premissas dos parâmetros escolhidos. */
    public List<Premissa> verificar(BigInteger p, BigInteger g, BigInteger a, BigInteger b) {
        boolean primoSeguro = ehPrimoSeguro(p);
        return List.of(
                new Premissa("p é primo seguro (p e q = (p−1)/2 primos)", primoSeguro),
                new Premissa("g é raiz primitiva módulo p", primoSeguro && ehRaizPrimitiva(g, p)),
                new Premissa("1 < a < p−1", noIntervalo(a, p)),
                new Premissa("1 < b < p−1", noIntervalo(b, p))
        );
    }

    /** p = 2q + 1 com p e q primos. Com isso, os únicos fatores primos de p−1 são 2 e q. */
    public boolean ehPrimoSeguro(BigInteger p) {
        BigInteger q = p.subtract(ONE).shiftRight(1);
        return p.isProbablePrime(CERTEZA_PRIMO) && q.isProbablePrime(CERTEZA_PRIMO);
    }

    /**
     * g é raiz primitiva quando suas potências geram todos os valores de 1 a p−1,
     * ou seja, quando a ordem de g é p−1. Isso vale se g^((p−1)/f) ≠ 1 para todo
     * fator primo f de p−1. Para um primo seguro (fatores 2 e q), sobram dois testes:
     * g^2 ≠ 1 e g^q ≠ 1 (mod p).
     * <p>
     * Pré-condição: p é primo seguro. Para um p qualquer de 2048 bits, fatorar p−1
     * seria inviável, e por isso não daria para verificar a raiz primitiva.
     */
    public boolean ehRaizPrimitiva(BigInteger g, BigInteger p) {
        if (g.compareTo(ONE) <= 0 || g.compareTo(p.subtract(ONE)) >= 0) {
            return false;
        }
        BigInteger q = p.subtract(ONE).shiftRight(1);
        return !g.modPow(TWO, p).equals(ONE) && !g.modPow(q, p).equals(ONE);
    }

    /** Sorteia uma chave privada uniforme em [2, p−2] com um gerador criptográfico. */
    public BigInteger gerarChavePrivada(BigInteger p) {
        BigInteger x;
        do {
            x = new BigInteger(p.bitLength(), random); // sorteia e descarta o que cair fora do intervalo
        } while (!noIntervalo(x, p));
        return x;
    }

    /** Chave pública: g^privada mod p (A para Alice, B para Bob). */
    public BigInteger chavePublica(BigInteger g, BigInteger privada, BigInteger p) {
        return g.modPow(privada, p);
    }

    /**
     * Segredo compartilhado: (chave pública do outro)^privada mod p.
     * <p>
     * A chave pública recebida é validada antes: com A = 1 ou A = p−1, o segredo
     * seria 1 ou ±1 qualquer que fosse a chave privada (ataque de subgrupo pequeno).
     *
     * @throws IllegalArgumentException se a chave pública não estiver em 1 < valor < p−1
     */
    public BigInteger segredo(BigInteger publicaDoOutro, BigInteger privada, BigInteger p) {
        if (!noIntervalo(publicaDoOutro, p)) {
            throw new IllegalArgumentException("Chave pública inválida: deve estar entre 1 e p−1 (exclusive).");
        }
        return publicaDoOutro.modPow(privada, p);
    }

    /** 1 < x < p−1 */
    private boolean noIntervalo(BigInteger x, BigInteger p) {
        return x.compareTo(ONE) > 0 && x.compareTo(p.subtract(ONE)) < 0;
    }
}
