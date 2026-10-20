package br.ufjf.dcc075.feistel;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.regex.Pattern;

/**
 * Página da atividade 04: encriptação e decriptação com uma Cifra de Feistel de 16 rodadas.
 */
@Controller
@RequestMapping("/feistel")
public class FeistelController {

    /** Chave de 32 bits = exatamente 8 dígitos hexadecimais. */
    private static final Pattern CHAVE_HEX = Pattern.compile("[0-9A-Fa-f]{8}");
    private static final String ERRO_CHAVE = "A chave deve ter exatamente 8 dígitos hexadecimais (ex.: 1A2B3C4D).";

    private final CifraFeistel cifra;

    public FeistelController(CifraFeistel cifra) {
        this.cifra = cifra;
    }

    @GetMapping
    public String pagina() {
        return "feistel/index";
    }

    @PostMapping("/cifrar")
    public String cifrar(@RequestParam String texto, @RequestParam String chave, Model model) {
        // Mantém o que o usuário digitou no formulário após o envio
        model.addAttribute("texto", texto);
        model.addAttribute("chave", chave);

        // Validação no servidor: o pattern do HTML pode ser contornado
        if (!CHAVE_HEX.matcher(chave).matches()) {
            model.addAttribute("erroCifrar", ERRO_CHAVE);
            return "feistel/index";
        }

        String hex = cifra.cifrar(texto, Integer.parseUnsignedInt(chave, 16));
        model.addAttribute("textoCifrado", hex);
        model.addAttribute("cifrado", hex); // pré-preenche a seção de decriptação
        return "feistel/index";
    }

    @PostMapping("/decifrar")
    public String decifrar(@RequestParam String cifrado, @RequestParam String chave, Model model) {
        model.addAttribute("cifrado", cifrado);
        model.addAttribute("chave", chave);

        if (!CHAVE_HEX.matcher(chave).matches()) {
            model.addAttribute("erroDecifrar", ERRO_CHAVE);
            return "feistel/index";
        }

        try {
            model.addAttribute("textoDecifrado", cifra.decifrar(cifrado, Integer.parseUnsignedInt(chave, 16)));
        } catch (IllegalArgumentException e) {
            // Hex inválido ou padding que não confere (em geral, chave errada).
            // A mensagem é controlada pelo código, sem stack trace na tela.
            model.addAttribute("erroDecifrar", e.getMessage());
        }
        return "feistel/index";
    }
}
