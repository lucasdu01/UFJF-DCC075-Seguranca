package br.ufjf.dcc075.cesar;

import org.springframework.stereotype.Service;

import java.text.Normalizer;

/**
 * Cifra de César: substituição monoalfabética por deslocamento.
 * E(x) = (x + k) mod 26 — e decifrar é o mesmo que cifrar com -k.
 */
@Service
public class CifraCesar {

    static final int TAMANHO_ALFABETO = 26;

    public String cifrar(String texto, int k) {
        int deslocamento = Math.floorMod(k, TAMANHO_ALFABETO); // k=29 vira 3; k=-3 vira 23
        StringBuilder saida = new StringBuilder(texto.length());

        for (char c : normalizar(texto).toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                int x = c - 'A';                                                  // letra -> índice 0..25
                saida.append((char) ('A' + (x + deslocamento) % TAMANHO_ALFABETO)); // desloca e volta para letra
            } else {
                saida.append(c);                                                  // espaço, pontuação, dígito: inalterados
            }
        }
        return saida.toString();
    }

    /** Remove acentos (á -> a, ç -> c) e passa para maiúsculas: alfabeto fica só A–Z. */
    static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase();
    }
}