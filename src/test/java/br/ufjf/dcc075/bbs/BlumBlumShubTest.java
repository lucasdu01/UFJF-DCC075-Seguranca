package br.ufjf.dcc075.bbs;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlumBlumShubTest {

    private final BlumBlumShub bbs = new BlumBlumShub();

    private static BigInteger big(long valor) {
        return BigInteger.valueOf(valor);
    }

    // Valores escolhidos para a atividade
    private static final BigInteger P = big(102_407);
    private static final BigInteger Q = big(98_999);
    private static final BigInteger S = big(5_333_145_159L);

    @Test
    void exemploFeitoAMao() {
        // p=7, q=11, n=77, s=3: x0=9 -> 4, 16, 25, 9 (volta ao x0, período 4)
        // bits = paridade de 4, 16, 25, 9 = 0, 0, 1, 1, e depois repete
        assertEquals("00110011", bbs.gerarBits(big(7), big(11), big(3), 8));
    }

    @Test
    void primeirosBitsDosValoresDaAtividade() {
        // Conferido com uma implementação independente (Python)
        assertEquals("1100110011010110", bbs.gerarBits(P, Q, S, 16));
    }

    @Test
    void valoresDaAtividadeAtendemTodasAsPremissas() {
        assertTrue(bbs.verificar(P, Q, S).stream().allMatch(BlumBlumShub.Premissa::atendida));
    }

    @Test
    void geraUmMilhaoDeBits() {
        assertEquals(1_000_000, bbs.gerarBits(P, Q, S, 1_000_000).length());
    }

    @Test
    void recusaPrimoQueNaoECongruenteA3Mod4() {
        // 13 é primo, mas 13 mod 4 = 1
        assertThrows(IllegalArgumentException.class, () -> bbs.gerarBits(big(13), big(11), big(3), 8));
    }

    @Test
    void recusaSementeComFatorComumComN() {
        // s = 7 é múltiplo de p: mdc(7, 77) = 7
        assertThrows(IllegalArgumentException.class, () -> bbs.gerarBits(big(7), big(11), big(7), 8));
    }

    @Test
    void recusaNumeroQueNaoEPrimo() {
        // 15 = 3 * 5, embora 15 mod 4 = 3
        assertThrows(IllegalArgumentException.class, () -> bbs.gerarBits(big(15), big(11), big(2), 8));
    }

    @Test
    void periodoDoExemploFeitoAMao() {
        // x0=9 -> 4 -> 16 -> 25 -> 9: volta ao x0 em 4 passos
        assertEquals(OptionalLong.of(4), bbs.periodo(big(7), big(11), big(3), 100));
    }

    @Test
    void valoresDaAtividadeNaoSeRepetemEmUmMilhaoDePassos() {
        // Período maior que 10^6: os bits enviados à suíte NIST não se repetem
        assertTrue(bbs.periodo(P, Q, S, 1_000_000).isEmpty());
    }
}
