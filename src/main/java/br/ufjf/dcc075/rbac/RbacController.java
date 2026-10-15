package br.ufjf.dcc075.rbac;

import br.ufjf.dcc075.rbac.estoque.InventoryService;
import br.ufjf.dcc075.rbac.estoque.Product;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Interface web da atividade 01.
 *
 * AUTENTICAÇÃO SIMULADA: o usuário é escolhido numa lista, sem senha.
 * O foco desta atividade é a AUTORIZAÇÃO (RBAC). A autenticação de verdade
 * entra na atividade 02 (OAuth2 com Google), reaproveitando este RBAC.
 *
 * O usuário escolhido fica guardado na HttpSession — equivale ao conceito
 * de "sessão" do RBAC do NIST: o vínculo entre o usuário e seus papéis ativos.
 *
 * Os botões de ação aparecem para TODOS de propósito: esconder um botão é só
 * usabilidade, não segurança (qualquer um pode montar o POST na mão, com curl
 * ou pelo DevTools). A verificação real acontece no InventoryService.
 */
@Controller
@RequestMapping("/rbac")
public class RbacController {

    static final String SESSION_USER = "rbac.username";

    private final AccessControlService accessControl;
    private final InventoryService inventory;

    public RbacController(AccessControlService accessControl, InventoryService inventory) {
        this.accessControl = accessControl;
        this.inventory = inventory;
    }

    @GetMapping
    public String page(HttpSession session, Model model) {
        String username = currentUsername(session);
        Set<Permission> myPermissions = accessControl.permissionsOf(username);

        model.addAttribute("currentUser", accessControl.findUser(username).orElse(null));
        model.addAttribute("myPermissions", myPermissions);
        model.addAttribute("allPermissions", Permission.values());
        // Só para a tela esmaecer os botões (usabilidade, não segurança).
        model.addAttribute("canSell", myPermissions.contains(Permission.REGISTRAR_VENDA));
        model.addAttribute("canRestock", myPermissions.contains(Permission.REGISTRAR_ENTRADA));
        model.addAttribute("canChangePrice", myPermissions.contains(Permission.ALTERAR_PRECO));
        model.addAttribute("users", accessControl.listUsers());   // relação UA
        model.addAttribute("roles", accessControl.listRoles());   // relação PA

        // A lista de produtos também passa pelo PEP: sem VISUALIZAR_ESTOQUE, não vem.
        List<Product> products = myPermissions.contains(Permission.VISUALIZAR_ESTOQUE)
                ? inventory.listProducts(username)
                : List.of();
        model.addAttribute("products", products);
        return "rbac/index";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, HttpServletRequest request, RedirectAttributes redirect) {
        // Só aceita usuários que existem no banco (nunca confia no valor enviado).
        if (accessControl.findUser(username).isEmpty()) {
            redirect.addFlashAttribute("error", "Usuário inexistente.");
            return "redirect:/rbac";
        }
        // Troca o ID da sessão ao "logar": impede SESSION FIXATION, ataque em que
        // o atacante planta um ID de sessão conhecido na vítima antes do login.
        request.changeSessionId();
        request.getSession().setAttribute(SESSION_USER, username);
        redirect.addFlashAttribute("success", "Sessão iniciada como " + username + ".");
        return "redirect:/rbac";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirect) {
        session.invalidate(); // descarta a sessão inteira, não só o atributo
        redirect.addFlashAttribute("success", "Sessão encerrada.");
        return "redirect:/rbac";
    }

    @PostMapping("/produtos/{id}/vender")
    public String sell(@PathVariable long id, @RequestParam int quantidade,
                       HttpSession session, RedirectAttributes redirect) {
        return execute(session, redirect, user -> {
            Product p = inventory.sell(user, id, quantidade);
            return "Venda registrada: %d x %s. Estoque atual: %d.".formatted(quantidade, p.getName(), p.getQuantity());
        });
    }

    @PostMapping("/produtos/{id}/repor")
    public String restock(@PathVariable long id, @RequestParam int quantidade,
                          HttpSession session, RedirectAttributes redirect) {
        return execute(session, redirect, user -> {
            Product p = inventory.restock(user, id, quantidade);
            return "Entrada registrada: +%d %s. Estoque atual: %d.".formatted(quantidade, p.getName(), p.getQuantity());
        });
    }

    @PostMapping("/produtos/{id}/preco")
    public String changePrice(@PathVariable long id, @RequestParam BigDecimal preco,
                              HttpSession session, RedirectAttributes redirect) {
        return execute(session, redirect, user -> {
            Product p = inventory.changePrice(user, id, preco);
            return "Preço de %s alterado para R$ %s.".formatted(p.getName(), p.getPrice());
        });
    }

    /**
     * Executa uma ação protegida e traduz o resultado em mensagem para a tela.
     * Padrão PRG (Post/Redirect/Get): após o POST redireciona para o GET,
     * evitando que um F5 reenvie a venda.
     */
    private String execute(HttpSession session, RedirectAttributes redirect, Function<String, String> action) {
        try {
            redirect.addFlashAttribute("success", action.apply(currentUsername(session)));
        } catch (PermissionDeniedException e) {
            // Mensagem própria para negação: a tela diferencia "negado" de "inválido".
            redirect.addFlashAttribute("denied", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", "Operação inválida: " + e.getMessage() + ".");
        }
        return "redirect:/rbac";
    }

    private static String currentUsername(HttpSession session) {
        return (String) session.getAttribute(SESSION_USER);
    }
}
