package br.ufjf.dcc075.dh;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static br.ufjf.dcc075.dh.DiffieHellman.G_RAIZ_PRIMITIVA;
import static br.ufjf.dcc075.dh.DiffieHellman.P_RFC3526;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffieHellmanTest {

    private final DiffieHellman dh = new DiffieHellman();

    private static BigInteger big(long valor) {
        return BigInteger.valueOf(valor);
    }

    // Valores da atividade (hexadecimal). a e b foram sorteados com um gerador criptográfico;
    // A, B e s foram conferidos com uma implementação independente (Python).
    private static final BigInteger A_PRIVADA = new BigInteger(
            "46690A49B3CFFA43E76D0ABD5291B70E09B579518D38B0D44583426DE345D2E2"
            + "D69FEF4D7F843145F063CE25A860735FDA2ABF60F1E5719C90369C2176990831"
            + "D599E69EAD0C4F894A21AE8CAC80F7ED773C20F20B0F6F54A32C1F5E43F1B2D1"
            + "D7126D679F1102FFB6EC708722BA51A875D1D5EB2179F22855068A71E6AEC44F"
            + "C6756FDAF2197684709D5EAA2458B40963ECD7B12A6DC6AE86689E2AA784EF99"
            + "23FE0709B4049509B7487E70087FD1642A02DE493CB938A5532DBB93FB44C71A"
            + "BEAFBC4DF221F0A2AF30E2E22C61D06A7894A586A1ECE9FBFB4F0E2A0C8103AD"
            + "A47EC8962B1A487EC1F52DA4FA25D13942CBD115D8B6D75F488E0139C429FC40", 16);
    private static final BigInteger B_PRIVADA = new BigInteger(
            "66D32BB1097F781F5DABBF5AD2396F2FDCC07BC029715AD3039CB27E6142F97B"
            + "1E7083378D8B7317AFBDC0C9F6E28EB59DBE0F77B54BA026EDF596C3C2BCD1AA"
            + "5F6F74C68FE735428F745A945E9FDBBCA5AECC6F0119E6967A70AE085BD3B701"
            + "3D22FFD8DE1E501BCC33B8543C7585B2CFEF3C6319A4A1B704563674CE15D26B"
            + "3108D4E7C258852607A6E8BCBFF456511440954D153F4DADD3D5CC4156B0F29B"
            + "12CFFAA48E359992E43A5A2C12969392164990954302B3D973829EE487982872"
            + "DDC0B4F05A77A1096863AEB357BFEAFF17AECACB7A0475921A73E6C5C9695947"
            + "4062D905D03294C06BF7CDD2ACC3529C0652231FB1C837D91569F728DA5F427D", 16);
    private static final BigInteger A_PUBLICA = new BigInteger(
            "C1C275C1405E49569529950D465DB6C815BF7770BDF816F9EC39711A21E8881B"
            + "42BFB19656F6B67113C0F341C2E5D737D08973072DEB114E44C1EE8E069D2629"
            + "2FF90D1C60F8057FA8180683318D2A2370312AEAF9A1839D5BF8CE20741D12AA"
            + "E3FAD435591C3A7F07538DDA58EBC759A154D2647FE4A68003CE0B9B9B3115C1"
            + "A21F5AED2F6B4C640BC784EC4E4765C4D3CCF0DC6013A1548E9C03C376FDBACA"
            + "A8647E2A21A13EB39F6861868458876DBB54AD8B66BF623009CAD0E8DA2C4999"
            + "48972F869EDF35C603D63DA384B560A31B6D112738FC9619DFA469CFA017C0A6"
            + "464F1E0002291CA4913CD4BC25C375243BD1EB728658B714C31547ACD2F0F16D", 16);
    private static final BigInteger B_PUBLICA = new BigInteger(
            "DFA6491954CF32BF05B1481E68E2AD98435A86C82E0E26CDE31C254CD5DBBA6C"
            + "FF9F859663CB9A6B5982254534FE7859A57B943559FA037C894FC18D5BD1A198"
            + "3ADDD71A1C0E64FB6239BB5EDA69DC2279B70D560A0C0E5736A2F9EAAAF05B92"
            + "AA15C7480DEABC1A03DB03DA95ACACFA52708A475EFEC1BFFC2C0FEEBE3CD3AD"
            + "01CF2E18768C73BE12197AF83E29E0AA4AEF70CC7C71EF605BB74B55B9214E03"
            + "65FC7265D03BB9A1BCA8C0536D4660D3958F6E9B498D03B812C0D04DA98962E4"
            + "809A5EDCB1DC2479BE2A52C910C015261766EAE4BAC5DE4610DF7941C0A986AE"
            + "A9CDA818A146EE25B310A8C866158F8A4BE35892D17D7F812472FD3AE5D8664D", 16);
    private static final BigInteger SEGREDO = new BigInteger(
            "E453D3C4F7FBA95E83822C80F519E8A89345D30D54B38A24F4183C1BDFE283C5"
            + "7D4BDD28AD2490A82F353A2AB33B1F984245808F3D5CEC93EE5EAC8D59D5CDB0"
            + "952D40798194A29BF729438FF70C3E976194C2BDD2DD7627AFE537402CCB80D4"
            + "90183EA6CBE4EC32CC0B75C6D01152F28DB85D543371D05308C1DDCDFB7B0003"
            + "C33F86C7BE5F9F32797ADD0EB282814DFDF4BDC4259AFC3C3F7C37ECECED19DB"
            + "EEA8AD034756423A24B540AC23D698E194E2E6B36A26BBFC3A4024E643B2056E"
            + "FFED47E696C3A0193E8C06F42378B59BB53A99733F54E64FCC914F2D3A2A20EF"
            + "7509BEC1FEA17AB5732BC13EF64BFB14D1A45671B745E356E665A4E3C9479028", 16);

    @Test
    void exemploFeitoAMao() {
        // p = 23, g = 5, a = 6, b = 15
        BigInteger p = big(23), g = big(5);
        BigInteger publicaAlice = dh.chavePublica(g, big(6), p);  // 5^6 mod 23
        BigInteger publicaBob = dh.chavePublica(g, big(15), p);   // 5^15 mod 23

        assertEquals(big(8), publicaAlice);
        assertEquals(big(19), publicaBob);
        assertEquals(big(2), dh.segredo(publicaBob, big(6), p));    // 19^6 mod 23
        assertEquals(big(2), dh.segredo(publicaAlice, big(15), p)); // 8^15 mod 23
    }

    @Test
    void raizPrimitivaNoExemploPequeno() {
        // 23 = 2·11 + 1 é primo seguro; 5 é raiz primitiva, 2 não (2^11 ≡ 1 mod 23)
        assertTrue(dh.ehPrimoSeguro(big(23)));
        assertTrue(dh.ehRaizPrimitiva(big(5), big(23)));
        assertFalse(dh.ehRaizPrimitiva(big(2), big(23)));
    }

    @Test
    void primoDoRfcTem2048BitsEESeguro() {
        assertEquals(2048, P_RFC3526.bitLength());
        assertTrue(dh.ehPrimoSeguro(P_RFC3526));
    }

    @Test
    void onzeERaizPrimitivaEDoisNao() {
        assertTrue(dh.ehRaizPrimitiva(G_RAIZ_PRIMITIVA, P_RFC3526));
        // O g = 2 do RFC gera só o subgrupo de ordem q
        assertFalse(dh.ehRaizPrimitiva(big(2), P_RFC3526));
    }

    @Test
    void valoresDaAtividade() {
        assertTrue(dh.verificar(P_RFC3526, G_RAIZ_PRIMITIVA, A_PRIVADA, B_PRIVADA).stream()
                .allMatch(DiffieHellman.Premissa::atendida));
        assertEquals(A_PUBLICA, dh.chavePublica(G_RAIZ_PRIMITIVA, A_PRIVADA, P_RFC3526));
        assertEquals(B_PUBLICA, dh.chavePublica(G_RAIZ_PRIMITIVA, B_PRIVADA, P_RFC3526));
        assertEquals(SEGREDO, dh.segredo(B_PUBLICA, A_PRIVADA, P_RFC3526)); // lado da Alice
        assertEquals(SEGREDO, dh.segredo(A_PUBLICA, B_PRIVADA, P_RFC3526)); // lado do Bob
    }

    @Test
    void chavesSorteadasChegamAoMesmoSegredo() {
        BigInteger a = dh.gerarChavePrivada(P_RFC3526);
        BigInteger b = dh.gerarChavePrivada(P_RFC3526);
        BigInteger publicaAlice = dh.chavePublica(G_RAIZ_PRIMITIVA, a, P_RFC3526);
        BigInteger publicaBob = dh.chavePublica(G_RAIZ_PRIMITIVA, b, P_RFC3526);

        assertEquals(dh.segredo(publicaBob, a, P_RFC3526), dh.segredo(publicaAlice, b, P_RFC3526));
    }

    @Test
    void chavePrivadaSorteadaFicaNoIntervalo() {
        for (int i = 0; i < 1000; i++) {
            BigInteger x = dh.gerarChavePrivada(big(23));
            assertTrue(x.compareTo(big(1)) > 0 && x.compareTo(big(22)) < 0);
        }
    }

    @Test
    void recusaChavePublicaTrivial() {
        // A = 1 ou A = p−1 tornariam o segredo previsível
        assertThrows(IllegalArgumentException.class, () -> dh.segredo(big(1), big(6), big(23)));
        assertThrows(IllegalArgumentException.class, () -> dh.segredo(big(22), big(6), big(23)));
    }
}
