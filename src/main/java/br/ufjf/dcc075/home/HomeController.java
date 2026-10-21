package br.ufjf.dcc075.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller da página inicial: exibe o menu com as atividades da disciplina.
 */
@Controller
public class HomeController {

    // Lista fixa de atividades. Para adicionar uma nova, basta incluir uma linha aqui.
    private static final List<Activity> ACTIVITIES = List.of(
            new Activity("01 – RBAC", "Controle de acesso baseado em papéis", "/rbac"),
            new Activity("02 – OAuth2 + RBAC", "Login com Google e autorização por papéis", "/oauth"),
            new Activity("03 – Cifra de César", "Encriptação e criptoanálise por frequência de letras", "/cesar"),
            new Activity("04 – Cifra de Feistel", "16 rodadas com encriptação e decriptação", "/feistel"),
            new Activity("07 – Blum Blum Shub", "Gerador pseudoaleatório testado na suíte NIST", "/bbs"),
            new Activity("08 – Diffie-Hellman", "Troca de chaves com primo de 2048 bits", "/dh"),
            new Activity("09 – Hash simples", "XOR com rotação, bloco a bloco", "/hash")
    );

    // Responde a GET "/" e devolve o template "index" (templates/index.html)
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("activities", ACTIVITIES);
        return "index";
    }
}