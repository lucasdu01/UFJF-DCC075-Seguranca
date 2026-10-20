package br.ufjf.dcc075.feistel;

import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Cifra de Feistel com 16 rodadas, bloco de 64 bits (duas metades de 32 bits)
 * e chave de 32 bits.
 * <p>
 * Uma rodada:  L(i) = R(i-1)
 *              R(i) = L(i-1) XOR f(R(i-1), K(i))
 * A função f nunca precisa ser invertida: para decriptar, basta repetir as
 * mesmas rodadas com as subchaves na ordem inversa (XOR é autorreversível).
 */
@Service
public class CifraFeistel {

    static final int RODADAS = 16;
    static final int TAMANHO_BLOCO = 8; // bytes (64 bits)

    private static final HexFormat HEX = HexFormat.of().withUpperCase();

    /**
     * Escalonamento de chave: gera uma subchave diferente para cada rodada,
     * K(i) = chave rotacionada i bits para a esquerda (i = 1..16).
     * <p>
     * Simples de propósito (didático). Uma chave com todos os bits iguais
     * (0x00000000 ou 0xFFFFFFFF) gera 16 subchaves idênticas.
     */
    int[] gerarSubchaves(int chave) {
        int[] subchaves = new int[RODADAS];
        for (int i = 0; i < RODADAS; i++) {
            subchaves[i] = Integer.rotateLeft(chave, i + 1); // posição 0 guarda K1
        }
        return subchaves;
    }

    /**
     * Função de rodada f: mistura a metade direita com a subchave.
     * XOR com a subchave + rotação de 3 bits (os bits que saem pela esquerda
     * voltam pela direita).
     * <p>
     * É uma f LINEAR, permitida pelo enunciado para fins didáticos. Uma cifra
     * forte usaria aqui uma parte não linear (como as S-boxes do DES).
     */
    int f(int r, int k) {
        return Integer.rotateLeft(r ^ k, 3);
    }

    /** Encripta um bloco de 64 bits: 16 rodadas com as subchaves K1..K16. */
    long cifrarBloco(long bloco, int chave) {
        return aplicarRodadas(bloco, gerarSubchaves(chave));
    }

    /**
     * Decripta um bloco de 64 bits: o MESMO algoritmo da encriptação,
     * só que com as subchaves na ordem inversa (K16..K1).
     */
    long decifrarBloco(long bloco, int chave) {
        int[] subchaves = gerarSubchaves(chave);
        int[] invertidas = new int[RODADAS];
        for (int i = 0; i < RODADAS; i++) {
            invertidas[i] = subchaves[RODADAS - 1 - i];
        }
        return aplicarRodadas(bloco, invertidas);
    }

    /**
     * Rede de Feistel: divide o bloco em L e R (32 bits cada) e aplica uma
     * rodada para cada subchave. Serve tanto para encriptar quanto para decriptar.
     */
    private long aplicarRodadas(long bloco, int[] subchaves) {
        int l = (int) (bloco >>> 32); // 32 bits mais altos
        int r = (int) bloco;          // 32 bits mais baixos

        for (int k : subchaves) {
            int novoR = l ^ f(r, k); // R(i) = L(i-1) XOR f(R(i-1), K(i))
            l = r;                   // L(i) = R(i-1)
            r = novoR;
        }

        // Troca final das metades (saída = R16 || L16). É ela que permite
        // decriptar com o mesmo algoritmo, apenas invertendo as subchaves.
        // A máscara evita que o sinal do int "suje" os 32 bits altos do long.
        return ((long) r << 32) | (l & 0xFFFFFFFFL);
    }

    // ---------------------------------------------------------------------
    // Texto <-> blocos: é o que permite cifrar mensagens de qualquer tamanho.
    // Modo ECB: cada bloco de 8 bytes é cifrado de forma independente.
    // ---------------------------------------------------------------------

    /** Cifra um texto: UTF-8 -> padding -> 16 rodadas por bloco -> hexadecimal. */
    public String cifrar(String texto, int chave) {
        byte[] dados = adicionarPadding(texto.getBytes(StandardCharsets.UTF_8));
        ByteBuffer buffer = ByteBuffer.wrap(dados); // big-endian: o 1º byte vira o mais alto do long

        for (int i = 0; i < dados.length; i += TAMANHO_BLOCO) {
            buffer.putLong(i, cifrarBloco(buffer.getLong(i), chave));
        }
        // Bytes cifrados não são texto legível: hexadecimal permite exibir e copiar
        return HEX.formatHex(dados);
    }

    /**
     * Decifra um hexadecimal gerado por {@link #cifrar}: hex -> blocos -> remove padding -> UTF-8.
     *
     * @throws IllegalArgumentException se o hex for inválido ou o padding não conferir
     *                                  (em geral, chave errada)
     */
    public String decifrar(String hex, int chave) {
        byte[] dados = HEX.parseHex(hex.strip()); // lança IllegalArgumentException se não for hex
        if (dados.length == 0 || dados.length % TAMANHO_BLOCO != 0) {
            throw new IllegalArgumentException("O cifrado deve ter um múltiplo de 8 bytes (16 dígitos hex).");
        }

        ByteBuffer buffer = ByteBuffer.wrap(dados);
        for (int i = 0; i < dados.length; i += TAMANHO_BLOCO) {
            buffer.putLong(i, decifrarBloco(buffer.getLong(i), chave));
        }
        return new String(removerPadding(dados), StandardCharsets.UTF_8);
    }

    /**
     * Padding PKCS#7: completa o último bloco com N bytes de valor N (N = 1..8).
     * Se o texto já tiver múltiplo de 8 bytes, entra um bloco inteiro de padding,
     * para que a remoção nunca seja ambígua.
     */
    private static byte[] adicionarPadding(byte[] dados) {
        int n = TAMANHO_BLOCO - dados.length % TAMANHO_BLOCO;
        byte[] saida = Arrays.copyOf(dados, dados.length + n);
        Arrays.fill(saida, dados.length, saida.length, (byte) n);
        return saida;
    }

    /**
     * Remove o padding PKCS#7, verificando se ele é válido. Com a chave errada, o último
     * bloco sai "aleatório" e quase sempre falha aqui.
     * <p>
     * Atenção: isso NÃO é verificação de integridade. Por acaso (cerca de 1 em 256),
     * uma chave errada pode gerar um padding válido e devolver lixo.
     */
    private static byte[] removerPadding(byte[] dados) {
        int n = dados[dados.length - 1];
        if (n < 1 || n > TAMANHO_BLOCO) {
            throw new IllegalArgumentException("Padding inválido: chave incorreta ou cifrado corrompido.");
        }
        for (int i = dados.length - n; i < dados.length; i++) {
            if (dados[i] != n) {
                throw new IllegalArgumentException("Padding inválido: chave incorreta ou cifrado corrompido.");
            }
        }
        return Arrays.copyOf(dados, dados.length - n);
    }
}
