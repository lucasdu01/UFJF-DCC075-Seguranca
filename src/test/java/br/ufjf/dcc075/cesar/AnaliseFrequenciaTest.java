package br.ufjf.dcc075.cesar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnaliseFrequenciaTest {

    private final AnaliseFrequencia analise = new AnaliseFrequencia();

    @Test
    void frequenciaDoExemploCifrado() {
        // DWDTXH DR DPDQKHFHU: 17 letras, D aparece 5 vezes, H 3 vezes
        double[] f = analise.calcularFrequencias("DWDTXH DR DPDQKHFHU");
        assertEquals(5 * 100.0 / 17, f['D' - 'A'], 0.001); // ~29,41%
        assertEquals(3 * 100.0 / 17, f['H' - 'A'], 0.001); // ~17,65%
        assertEquals(0.0, f['A' - 'A'], 0.001);
    }

    private static final String PARAGRAFO = """
            A criptografia estuda formas de proteger informações para que somente o \
            destinatário consiga entender a mensagem. Na cifra de César, cada letra do \
            texto é trocada por outra que está um número fixo de posições à frente no \
            alfabeto. Apesar de simples, ela foi usada pelo imperador romano para enviar \
            ordens aos seus generais durante as campanhas militares.""";

    @Test
    void recuperaTodasAsChavesEmTextoLongo() {
        CifraCesar cifra = new CifraCesar();
        for (int k = 1; k < 26; k++) {
            String cifrado = cifra.cifrar(PARAGRAFO, k);
            int estimado = analise.estimarChave(cifrado);

            assertEquals(k, estimado, "falhou para k=" + k);
            assertEquals(CifraCesar.normalizar(PARAGRAFO), cifra.cifrar(cifrado, -estimado));
        }
    }
}