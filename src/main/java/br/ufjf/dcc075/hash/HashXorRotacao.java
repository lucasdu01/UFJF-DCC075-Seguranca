package br.ufjf.dcc075.hash;

import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

/**
 * Hash simples de 32 bits baseado em XOR com rotação, bloco a bloco.
 * <p>
 * 1. Padding: adiciona o byte 0x80 e completa com zeros até um múltiplo de 4 bytes
 *    (mesma ideia do padding do SHA: "a" e "a\0" não viram a mesma entrada).
 * 2. Divide a mensagem em blocos de 32 bits.
 * 3. Para cada bloco: h = rotl(h, 5) XOR bloco, começando de um valor inicial fixo.
 * <p>
 * A rotação faz a ordem dos blocos importar (só com XOR, "abcdefgh" e "efghabcd"
 * dariam o mesmo hash). Mas a função continua LINEAR: não é segura (ver README).
 */
@Service
public class HashXorRotacao {

    /** Valor inicial: os mesmos 32 bits iniciais (H0) do SHA-256. */
    static final int VALOR_INICIAL = 0x6A09E667;

    /** Quantos bits o estado gira para a esquerda antes de cada XOR. */
    static final int ROTACAO = 5;

    private static final HexFormat HEX = HexFormat.of().withUpperCase();

    /** Um passo do cálculo: o bloco processado e o estado h logo depois (para exibir na tela). */
    public record Passo(String bloco, String estado) {}

    /** Hash de um texto (UTF-8) em 8 dígitos hexadecimais. */
    public String calcularHex(String texto) {
        return HEX.toHexDigits(calcular(texto.getBytes(StandardCharsets.UTF_8)));
    }

    /** Hash de 32 bits de uma sequência de bytes. */
    public int calcular(byte[] mensagem) {
        int h = VALOR_INICIAL;
        for (int bloco : blocos(mensagem)) {
            h = Integer.rotateLeft(h, ROTACAO) ^ bloco; // bits que saem pela esquerda voltam pela direita
        }
        return h;
    }

    /** Mesmo cálculo de {@link #calcular}, guardando cada passo para mostrar na tela. */
    public List<Passo> passos(String texto) {
        List<Passo> passos = new ArrayList<>();
        int h = VALOR_INICIAL;
        for (int bloco : blocos(texto.getBytes(StandardCharsets.UTF_8))) {
            h = Integer.rotateLeft(h, ROTACAO) ^ bloco;
            passos.add(new Passo(HEX.toHexDigits(bloco), HEX.toHexDigits(h)));
        }
        return passos;
    }

    /** Padding (0x80 + zeros até múltiplo de 4 bytes) e divisão em blocos de 32 bits. */
    int[] blocos(byte[] mensagem) {
        int tamanho = (mensagem.length + 1 + 3) / 4 * 4; // +1 do byte 0x80, arredondado para múltiplo de 4
        byte[] comPadding = Arrays.copyOf(mensagem, tamanho); // completa com zeros
        comPadding[mensagem.length] = (byte) 0x80;

        ByteBuffer buffer = ByteBuffer.wrap(comPadding); // lê de 4 em 4 bytes (big-endian)
        int[] blocos = new int[tamanho / 4];
        for (int i = 0; i < blocos.length; i++) {
            blocos[i] = buffer.getInt();
        }
        return blocos;
    }
}
