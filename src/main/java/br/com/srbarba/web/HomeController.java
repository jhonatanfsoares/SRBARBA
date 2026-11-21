package br.com.srbarba.web;

import br.com.srbarba.repository.BarbeiroRepository;
import br.com.srbarba.repository.ServicoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ServicoRepository servicoRepository;
    private final BarbeiroRepository barbeiroRepository;

    public HomeController(ServicoRepository servicoRepository, BarbeiroRepository barbeiroRepository) {
        this.servicoRepository = servicoRepository;
        this.barbeiroRepository = barbeiroRepository;
    }

    /** Página pública com serviços, equipe e contato. */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("servicos", servicoRepository.findByAtivoTrueOrderByNome());
        model.addAttribute("barbeiros", barbeiroRepository.findByAtivoTrueOrderByNome());
        return "index";
    }

    /** Após o login, envia cada perfil para sua área. */
    @GetMapping("/painel")
    public String painel(Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return admin ? "redirect:/admin" : "redirect:/cliente";
    }
}
