package br.ufjf.dcc075.cesar;

import org.springframework.stereotype.Service;

import static br.ufjf.dcc075.cesar.CifraCesar.TAMANHO_ALFABETO;

/**
 * Criptoanálise da Cifra de César pela frequência de letras do português.
 */
@Service
public class AnaliseFrequencia {

    /**
     * Frequência (%) de cada letra A..Z no texto; índice 0 = A, 25 = Z.
     * Só letras entram na conta: espaços e pontuação são ignorados.
     */
    public double[] calcularFrequencias(String texto) {
        int[] contagem = new int[TAMANHO_ALFABETO];
        int totalLetras = 0;

        for (char c : CifraCesar.normalizar(texto).toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                contagem[c - 'A']++;
                totalLetras++;
            }
        }

        double[] frequencias = new double[TAMANHO_ALFABETO];
        if (totalLetras == 0) {
            return frequencias; // texto sem letras: tudo 0%, evita divisão por zero
        }
        for (int i = 0; i < TAMANHO_ALFABETO; i++) {
            frequencias[i] = contagem[i] * 100.0 / totalLetras;
        }
        return frequencias;
    }

    /** Frequências (%) do português, A..Z — fonte: pt.wikipedia.org/wiki/Frequência_de_letras */
    static final double[] FREQUENCIAS_PORTUGUES = {
            14.63, 1.04, 3.88, 4.99, 12.57, 1.02, 1.30, 1.28, 6.18, 0.40, // A–J
            0.02, 2.78, 4.74, 5.05, 10.73, 2.52, 1.20, 6.53, 7.81, 4.34,  // K–T
            4.63, 1.67, 0.01, 0.21, 0.01, 0.47                            // U–Z
    };

    /**
     * Estima a chave k testando os 26 deslocamentos e escolhendo o de menor χ²,
     * isto é, aquele cujo texto decifrado mais se parece com o português.
     */
    public int estimarChave(String cifrado) {
        double[] observadas = calcularFrequencias(cifrado);

        int melhorK = 0;
        double menorChi = Double.MAX_VALUE;

        for (int k = 0; k < TAMANHO_ALFABETO; k++) {
            double chi = 0;
            for (int i = 0; i < TAMANHO_ALFABETO; i++) {
                // Se a chave fosse k, a letra clara i virou a letra cifrada (i + k) mod 26
                double obs = observadas[(i + k) % TAMANHO_ALFABETO];
                double esp = FREQUENCIAS_PORTUGUES[i];
                chi += (obs - esp) * (obs - esp) / esp; // esp nunca é 0 na tabela
            }
            if (chi < menorChi) {
                menorChi = chi;
                melhorK = k;
            }
        }
        return melhorK;
    }
}