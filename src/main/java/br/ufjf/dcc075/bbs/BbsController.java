package br.ufjf.dcc075.bbs;

import br.ufjf.dcc075.bbs.BlumBlumShub.Premissa;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.regex.Pattern;

/**
 * Página da atividade 07: gerador Blum Blum Shub, com verificação das premissas,
 * prévia dos bits e download da sequência completa para a suíte de testes NIST.
 */
@Controller
@RequestMapping("/bbs")
public class BbsController {

    /** Valores escolhidos para a atividade (pré-preenchem o formulário). */
    static final String P_PADRAO = "102407";
    static final String Q_PADRAO = "98999";
    static final String S_PADRAO = "5333145159";

    /** Exigência do enunciado: p e q maiores que 10.000. */
    static final BigInteger MINIMO_ENUNCIADO = BigInteger.valueOf(10_000);

    /** 10^6 bits: tamanho que permite rodar todos os testes da suíte NIST. */
    static final int MAX_BITS = 1_000_000;
    static final int BITS_PREVIA = 256;

    /**
     * Só dígitos, no máximo 30. Limita o tamanho dos números para que ninguém
     * envie valores gigantes e trave o servidor (CWE-400, consumo de recursos).
     */
    private static final Pattern NUMERO = Pattern.compile("\\d{1,30}");

    private final BlumBlumShub bbs;

    public BbsController(BlumBlumShub bbs) {
        this.bbs = bbs;
    }

    @GetMapping
    public String pagina(Model model) {
        manterFormulario(model, P_PADRAO, Q_PADRAO, S_PADRAO, MAX_BITS);
        return "bbs/index";
    }

    @PostMapping("/gerar")
    public String gerar(@RequestParam String p, @RequestParam String q, @RequestParam String s,
                        @RequestParam int quantidade, Model model) {
        manterFormulario(model, p, q, s, quantidade);

        // Validação no servidor: os atributos pattern/min/max do HTML podem ser contornados
        String erro = validarEntrada(p, q, s, quantidade);
        if (erro != null) {
            model.addAttribute("erro", erro);
            return "bbs/index";
        }

        BigInteger bp = new BigInteger(p);
        BigInteger bq = new BigInteger(q);
        BigInteger bs = new BigInteger(s);

        // Premissas do algoritmo + a exigência do enunciado, exibidas com ✓/✗
        List<Premissa> premissas = new ArrayList<>(bbs.verificar(bp, bq, bs));
        premissas.add(new Premissa("p > 10.000 e q > 10.000 (exigência do enunciado)",
                bp.compareTo(MINIMO_ENUNCIADO) > 0 && bq.compareTo(MINIMO_ENUNCIADO) > 0));
        model.addAttribute("premissas", premissas);

        if (premissas.stream().anyMatch(premissa -> !premissa.atendida())) {
            model.addAttribute("erro", "Há premissas não atendidas (marcadas com ✗). Os bits não foram gerados.");
            return "bbs/index";
        }

        String bits = bbs.gerarBits(bp, bq, bs, quantidade);
        long uns = bits.chars().filter(c -> c == '1').count();

        // O período precisa ser maior que a quantidade de bits; senão a sequência se repete
        OptionalLong periodo = bbs.periodo(bp, bq, bs, quantidade);

        model.addAttribute("n", bp.multiply(bq));
        model.addAttribute("uns", uns);
        model.addAttribute("zeros", quantidade - uns);
        model.addAttribute("semRepeticao", periodo.isEmpty());
        model.addAttribute("periodo", periodo.isPresent() ? periodo.getAsLong() : null);
        model.addAttribute("previa", bits.substring(0, Math.min(BITS_PREVIA, bits.length())));
        return "bbs/index";
    }

    /**
     * Download da sequência completa como texto ("0101..."), para colar no testador NIST.
     * GET sem efeito colateral: só calcula e devolve.
     */
    @GetMapping("/bits.txt")
    public ResponseEntity<String> baixar(@RequestParam String p, @RequestParam String q, @RequestParam String s,
                                         @RequestParam int quantidade) {
        String erro = validarEntrada(p, q, s, quantidade);
        if (erro != null) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(erro);
        }
        try {
            String bits = bbs.gerarBits(new BigInteger(p), new BigInteger(q), new BigInteger(s), quantidade);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bbs-bits.txt\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(bits);
        } catch (IllegalArgumentException e) {
            // Premissa não atendida: mensagem controlada pelo código, sem stack trace
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
        }
    }

    /** Devolve a mensagem de erro, ou null se a entrada for válida. */
    private String validarEntrada(String p, String q, String s, int quantidade) {
        if (!NUMERO.matcher(p).matches() || !NUMERO.matcher(q).matches() || !NUMERO.matcher(s).matches()) {
            return "p, q e s devem ser inteiros positivos com até 30 dígitos.";
        }
        if (quantidade < 1 || quantidade > MAX_BITS) {
            return "A quantidade de bits deve estar entre 1 e " + MAX_BITS + ".";
        }
        return null;
    }

    /** Mantém o que o usuário digitou no formulário após o envio. */
    private void manterFormulario(Model model, String p, String q, String s, int quantidade) {
        model.addAttribute("p", p);
        model.addAttribute("q", q);
        model.addAttribute("s", s);
        model.addAttribute("quantidade", quantidade);
    }
}
