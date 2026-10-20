package br.ufjf.dcc075.feistel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CifraFeistelTest {

    private final CifraFeistel cifra = new CifraFeistel();

    @Test
    void geraDezesseisSubchavesPorRotacao() {
        int[] k = cifra.gerarSubchaves(0x1A2B3C4D);

        assertEquals(16, k.length);
        assertEquals(0x3456789A, k[0]);  // K1  = chave rotacionada 1 bit
        assertEquals(0x3C4D1A2B, k[15]); // K16 = rotacionada 16 bits (metades trocadas)
    }

    @Test
    void funcaoFFazXorERotacao() {
        assertEquals(0x000007F8, cifra.f(0x0000000F, 0x000000F0)); // 0x0F ^ 0xF0 = 0xFF; << 3 = 0x7F8
        assertEquals(0x00000004, cifra.f(0x80000000, 0));          // bit mais alto "dá a volta" para a direita
    }

    private static final int CHAVE = 0x1A2B3C4D;

    @Test
    void decifrarDesfazCifrar() {
        long[] blocos = {0L, -1L, 0x0123456789ABCDEFL, 0x8000000000000001L, 0x4F6C61204D756E64L}; // último = "Ola Mund"
        for (long bloco : blocos) {
            long cifrado = cifra.cifrarBloco(bloco, CHAVE);
            assertNotEquals(bloco, cifrado);
            assertEquals(bloco, cifra.decifrarBloco(cifrado, CHAVE));
        }
    }

    @Test
    void mudarUmBitDaChaveMudaOCifrado() {
        long bloco = 0x0123456789ABCDEFL;
        assertNotEquals(cifra.cifrarBloco(bloco, CHAVE), cifra.cifrarBloco(bloco, CHAVE ^ 1));
    }

    @Test
    void chaveErradaNaoDecifra() {
        long bloco = 0x0123456789ABCDEFL;
        long cifrado = cifra.cifrarBloco(bloco, CHAVE);
        assertNotEquals(bloco, cifra.decifrarBloco(cifrado, CHAVE ^ 1));
    }

    @Test
    void textoComAcentosVaiEVolta() {
        String texto = "Ação de segurança!"; // 21 bytes em UTF-8 -> 3 blocos com padding
        String hex = cifra.cifrar(texto, CHAVE);

        assertEquals(48, hex.length()); // 3 blocos x 8 bytes x 2 dígitos hex
        assertEquals(texto, cifra.decifrar(hex, CHAVE));
    }

    @Test
    void textoMultiploDe8GanhaBlocoInteiroDePadding() {
        String hex = cifra.cifrar("12345678", CHAVE); // 8 bytes + 8 de padding
        assertEquals(32, hex.length());
        assertEquals("12345678", cifra.decifrar(hex, CHAVE));
    }

    @Test
    void ecbBlocosIguaisGeramCifradosIguais() {
        String hex = cifra.cifrar("AAAAAAAAAAAAAAAA", CHAVE); // dois blocos "AAAAAAAA"
        assertEquals(hex.substring(0, 16), hex.substring(16, 32)); // o padrão vaza (fraqueza do ECB)
    }

    @Test
    void chaveErradaFalhaNoPadding() {
        String hex = cifra.cifrar("Ação de segurança!", CHAVE);
        assertThrows(IllegalArgumentException.class, () -> cifra.decifrar(hex, 0xDEADBEEF));
    }

    @Test
    void hexInvalidoEhRejeitado() {
        assertThrows(IllegalArgumentException.class, () -> cifra.decifrar("XYZ", CHAVE));
        assertThrows(IllegalArgumentException.class, () -> cifra.decifrar("ABCD", CHAVE)); // menos de 1 bloco
    }
}
