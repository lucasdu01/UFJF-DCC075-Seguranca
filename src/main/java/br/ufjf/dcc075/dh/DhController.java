package br.ufjf.dcc075.dh;

import br.ufjf.dcc075.dh.DiffieHellman.Premissa;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigInteger;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Página da atividade 08: troca de chaves Diffie-Hellman entre Alice e Bob,
 * mostrando o que trafega no canal público e o segredo calculado por cada lado.
 */
@Controller
@RequestMapping("/dh")
public class DhController {

    /** Chaves privadas escolhidas para a atividade (sorteadas com gerador criptográfico). */
    static final String A_PADRAO = "46690A49B3CFFA43E76D0ABD5291B70E09B579518D38B0D44583426DE345D2E2D69FEF4D7F843145F063CE25A860735FDA2ABF60F1E5719C90369C2176990831D599E69EAD0C4F894A21AE8CAC80F7ED773C20F20B0F6F54A32C1F5E43F1B2D1D7126D679F1102FFB6EC708722BA51A875D1D5EB2179F22855068A71E6AEC44FC6756FDAF2197684709D5EAA2458B40963ECD7B12A6DC6AE86689E2AA784EF9923FE0709B4049509B7487E70087FD1642A02DE493CB938A5532DBB93FB44C71ABEAFBC4DF221F0A2AF30E2E22C61D06A7894A586A1ECE9FBFB4F0E2A0C8103ADA47EC8962B1A487EC1F52DA4FA25D13942CBD115D8B6D75F488E0139C429FC40";
    static final String B_PADRAO = "66D32BB1097F781F5DABBF5AD2396F2FDCC07BC029715AD3039CB27E6142F97B1E7083378D8B7317AFBDC0C9F6E28EB59DBE0F77B54BA026EDF596C3C2BCD1AA5F6F74C68FE735428F745A945E9FDBBCA5AECC6F0119E6967A70AE085BD3B7013D22FFD8DE1E501BCC33B8543C7585B2CFEF3C6319A4A1B704563674CE15D26B3108D4E7C258852607A6E8BCBFF456511440954D153F4DADD3D5CC4156B0F29B12CFFAA48E359992E43A5A2C12969392164990954302B3D973829EE487982872DDC0B4F05A77A1096863AEB357BFEAFF17AECACB7A0475921A73E6C5C96959474062D905D03294C06BF7CDD2ACC3529C0652231FB1C837D91569F728DA5F427D";

    /**
     * Hexadecimal com até 512 dígitos (2048 bits). Limita o tamanho dos números para
     * que ninguém envie valores gigantes e trave o servidor (CWE-400).
     */
    private static final Pattern HEX = Pattern.compile("[0-9A-Fa-f]{1,512}");
    private static final Pattern DECIMAL = Pattern.compile("\\d{1,30}");

    private final DiffieHellman dh;

    public DhController(DiffieHellman dh) {
        this.dh = dh;
    }

    @GetMapping
    public String pagina(Model model) {
        manterFormulario(model, hex(DiffieHellman.P_RFC3526), DiffieHellman.G_RAIZ_PRIMITIVA.toString(),
                A_PADRAO, B_PADRAO);
        return "dh/index";
    }

    @PostMapping("/calcular")
    public String calcular(@RequestParam String p, @RequestParam String g,
                           @RequestParam String a, @RequestParam String b, Model model) {
        return processar(p, g, a, b, model);
    }

    /** Mantém p e g e sorteia novas chaves privadas para Alice e Bob. */
    @PostMapping("/sortear")
    public String sortear(@RequestParam String p, @RequestParam String g, Model model) {
        String pLimpo = limpar(p);
        if (!HEX.matcher(pLimpo).matches()) {
            manterFormulario(model, p, g, "", "");
            model.addAttribute("erro", "p deve estar em hexadecimal, com até 512 dígitos (2048 bits).");
            return "dh/index";
        }
        BigInteger bp = new BigInteger(pLimpo, 16);
        if (bp.compareTo(BigInteger.valueOf(5)) < 0) {
            manterFormulario(model, p, g, "", "");
            model.addAttribute("erro", "p deve ser maior que 4.");
            return "dh/index";
        }
        return processar(p, g, hex(dh.gerarChavePrivada(bp)), hex(dh.gerarChavePrivada(bp)), model);
    }

    private String processar(String p, String g, String a, String b, Model model) {
        manterFormulario(model, p, g, a, b);

        // Validação no servidor: o pattern do HTML pode ser contornado
        String pLimpo = limpar(p), aLimpo = limpar(a), bLimpo = limpar(b), gLimpo = limpar(g);
        if (!HEX.matcher(pLimpo).matches() || !HEX.matcher(aLimpo).matches() || !HEX.matcher(bLimpo).matches()) {
            model.addAttribute("erro", "p, a e b devem estar em hexadecimal, com até 512 dígitos (2048 bits).");
            return "dh/index";
        }
        if (!DECIMAL.matcher(gLimpo).matches()) {
            model.addAttribute("erro", "g deve ser um inteiro decimal com até 30 dígitos.");
            return "dh/index";
        }

        BigInteger bp = new BigInteger(pLimpo, 16);
        BigInteger bg = new BigInteger(gLimpo);
        BigInteger ba = new BigInteger(aLimpo, 16);
        BigInteger bb = new BigInteger(bLimpo, 16);

        List<Premissa> premissas = dh.verificar(bp, bg, ba, bb);
        model.addAttribute("premissas", premissas);
        if (premissas.stream().anyMatch(premissa -> !premissa.atendida())) {
            model.addAttribute("erro", "Há premissas não atendidas (marcadas com ✗). A troca não foi feita.");
            return "dh/index";
        }

        // Cada lado calcula a sua chave pública e envia pelo canal público
        BigInteger publicaAlice = dh.chavePublica(bg, ba, bp); // A = g^a mod p
        BigInteger publicaBob = dh.chavePublica(bg, bb, bp);   // B = g^b mod p

        // Cada lado combina a pública recebida com a sua privada
        BigInteger segredoAlice = dh.segredo(publicaBob, ba, bp);   // s = B^a mod p
        BigInteger segredoBob = dh.segredo(publicaAlice, bb, bp);   // s = A^b mod p

        model.addAttribute("bits", bp.bitLength());
        model.addAttribute("publicaAlice", formatar(publicaAlice));
        model.addAttribute("publicaBob", formatar(publicaBob));
        model.addAttribute("segredoAlice", formatar(segredoAlice));
        model.addAttribute("segredoBob", formatar(segredoBob));
        model.addAttribute("iguais", segredoAlice.equals(segredoBob));
        return "dh/index";
    }

    /** Remove espaços e quebras de linha, para aceitar hexadecimal colado em blocos. */
    private static String limpar(String valor) {
        return valor.replaceAll("\\s", "");
    }

    private static String hex(BigInteger valor) {
        return valor.toString(16).toUpperCase();
    }

    /** Hexadecimal em blocos de 8 dígitos, para facilitar a leitura e a conferência. */
    static String formatar(BigInteger valor) {
        return formatar(hex(valor));
    }

    static String formatar(String hex) {
        return limpar(hex).toUpperCase().replaceAll("(.{8})(?!$)", "$1 ");
    }

    /** Mantém o que o usuário digitou no formulário após o envio. */
    private static void manterFormulario(Model model, String p, String g, String a, String b) {
        model.addAttribute("p", formatar(p));
        model.addAttribute("g", g);
        model.addAttribute("a", formatar(a));
        model.addAttribute("b", formatar(b));
    }
}
