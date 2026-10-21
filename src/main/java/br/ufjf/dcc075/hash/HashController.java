package br.ufjf.dcc075.hash;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Página da atividade 09 (parte 3): hash simples com XOR e rotação, mostrando cada bloco processado.
 */
@Controller
@RequestMapping("/hash")
public class HashController {

    /** Limite de entrada para a tabela de passos não ficar gigante (CWE-400). */
    static final int MAX_CARACTERES = 1000;

    private final HashXorRotacao hash;

    public HashController(HashXorRotacao hash) {
        this.hash = hash;
    }

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("texto", "Ola, mundo!");
        return "hash/index";
    }

    @PostMapping("/calcular")
    public String calcular(@RequestParam String texto, Model model) {
        model.addAttribute("texto", texto);

        // Validação no servidor: o maxlength do HTML pode ser contornado
        if (texto.length() > MAX_CARACTERES) {
            model.addAttribute("erro", "O texto deve ter no máximo " + MAX_CARACTERES + " caracteres.");
            return "hash/index";
        }

        model.addAttribute("resultado", hash.calcularHex(texto));
        model.addAttribute("passos", hash.passos(texto));
        return "hash/index";
    }
}
