package br.ufjf.dcc075.cesar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CifraCesarTest {

    private final CifraCesar cifra = new CifraCesar();

    @Test
    void cifraExemploDoPlano() {
        assertEquals("DWDTXH DR DPDQKHFHU", cifra.cifrar("Ataque ao amanhecer", 3));
    }

    @Test
    void chaveMaiorQue26EhReduzida() {
        assertEquals(cifra.cifrar("Ataque", 3), cifra.cifrar("Ataque", 29));
    }

    @Test
    void acentosSaoNormalizados() {
        assertEquals("FDFD", cifra.cifrar("caça", 3)); // CACA -> FDFD
    }
}