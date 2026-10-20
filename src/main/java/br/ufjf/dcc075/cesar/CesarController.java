package br.ufjf.dcc075.cesar;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Página da atividade 03: encriptação com a Cifra de César e criptoanálise por frequência.
 */
@Controller
@RequestMapping("/cesar")
public class CesarController {

    private final CifraCesar cifra;
    private final AnaliseFrequencia analise;

    public CesarController(CifraCesar cifra, AnaliseFrequencia analise) {
        this.cifra = cifra;
        this.analise = analise;
    }

    @GetMapping
    public String pagina() {
        return "cesar/index";
    }

    @PostMapping("/cifrar")
    public String cifrar(@RequestParam String texto, @RequestParam int k, Model model) {
        // Mantém o que o usuário digitou no formulário após o envio
        model.addAttribute("texto", texto);
        model.addAttribute("k", k);

        // Validação no servidor: o min="1" do HTML pode ser contornado
        if (k < 1) {
            model.addAttribute("erroCifrar", "A chave k deve ser maior ou igual a 1.");
            return "cesar/index";
        }

        model.addAttribute("textoCifrado", cifra.cifrar(texto, k));
        return "cesar/index";
    }

    /** Uma linha da tabela: letra, % no texto cifrado e % esperada no português. */
    public record LinhaFrequencia(char letra, double observada, double esperada) {}

    @PostMapping("/analisar")
    public String analisar(@RequestParam String cifrado, Model model) {
        model.addAttribute("cifrado", cifrado);

        // Histograma do texto cifrado comparado com o do português
        double[] observadas = analise.calcularFrequencias(cifrado);
        List<LinhaFrequencia> linhas = IntStream.range(0, CifraCesar.TAMANHO_ALFABETO)
                .mapToObj(i -> new LinhaFrequencia((char) ('A' + i),
                        observadas[i], AnaliseFrequencia.FREQUENCIAS_PORTUGUES[i]))
                .toList();

        // Ataque: estima k só pelo cifrado e decifra cifrando com -k
        int kEstimado = analise.estimarChave(cifrado);

        model.addAttribute("linhas", linhas);
        model.addAttribute("kEstimado", kEstimado);
        model.addAttribute("textoDecifrado", cifra.cifrar(cifrado, -kEstimado));
        return "cesar/index";
    }
}