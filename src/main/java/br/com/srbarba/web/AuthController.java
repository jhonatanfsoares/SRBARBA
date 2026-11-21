package br.com.srbarba.web;

import br.com.srbarba.service.RegraNegocioException;
import br.com.srbarba.service.UsuarioService;
import br.com.srbarba.web.form.CadastroClienteForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("form", new CadastroClienteForm());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroClienteForm form, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/cadastro";
        }
        try {
            usuarioService.cadastrarCliente(form);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "auth/cadastro";
        }
        redirect.addFlashAttribute("sucesso", "Cadastro realizado! Entre com seu e-mail e senha.");
        return "redirect:/login";
    }
}
