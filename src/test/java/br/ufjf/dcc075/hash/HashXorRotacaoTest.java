package br.ufjf.dcc075.hash;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashXorRotacaoTest {

    private final HashXorRotacao hash = new HashXorRotacao();

    @Test
    void valoresConhecidos() {
        // Conferidos com uma implementação independente (Python)
        assertEquals("C13CCCED", hash.calcularHex(""));    // só o bloco de padding 80000000
        assertEquals("205EAF6D", hash.calcularHex("abc")); // bloco 61626380
        assertEquals("2B76887D", hash.calcularHex("Ola, mundo!"));
    }

    @Test
    void passoAPasso() {
        // "Ola, mundo!" = 11 bytes + 0x80 = 3 blocos
        List<HashXorRotacao.Passo> passos = hash.passos("Ola, mundo!");
        assertEquals(List.of(
                new HashXorRotacao.Passo("4F6C612C", "0E50ADC1"),  // "Ola,"
                new HashXorRotacao.Passo("206D756E", "EA78CD4F"),  // " mun"
                new HashXorRotacao.Passo("646F2180", "2B76887D")), // "do!" + 0x80
                passos);
    }

    @Test
    void ordemDosBlocosImporta() {
        // Sem a rotação, trocar a ordem dos bytes/blocos daria o mesmo XOR
        assertNotEquals(hash.calcularHex("ab"), hash.calcularHex("ba"));
        assertNotEquals(hash.calcularHex("abcdefgh"), hash.calcularHex("efghabcd"));
    }

    @Test
    void saidaTemTamanhoFixo() {
        assertEquals(8, hash.calcularHex("a".repeat(10_000)).length());
    }

    @Test
    void colisaoEncontradaFacilmente() {
        // Fraqueza: a função é linear, então dá para montar outra mensagem com o mesmo hash
        assertEquals(hash.calcularHex("DCC075!!"), hash.calcularHex("wxAsPUiG"));
        assertEquals("989B5033", hash.calcularHex("DCC075!!"));
    }
}
