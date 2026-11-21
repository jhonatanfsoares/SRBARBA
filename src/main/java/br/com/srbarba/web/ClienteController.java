package br.com.srbarba.web;

import br.com.srbarba.domain.Usuario;
import br.com.srbarba.repository.BarbeiroRepository;
import br.com.srbarba.repository.ServicoRepository;
import br.com.srbarba.service.AgendamentoService;
import br.com.srbarba.service.RegraNegocioException;
import br.com.srbarba.service.UsuarioService;
import br.com.srbarba.web.form.AgendamentoForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Área do cliente: meus agendamentos, novo agendamento e cancelamento. */
@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final UsuarioService usuarioService;
    private final AgendamentoService agendamentoService;
    private final ServicoRepository servicoRepository;
    private final BarbeiroRepository barbeiroRepository;

    public ClienteController(UsuarioService usuarioService, AgendamentoService agendamentoService,
                             ServicoRepository servicoRepository, BarbeiroRepository barbeiroRepository) {
        this.usuarioService = usuarioService;
        this.agendamentoService = agendamentoService;
        this.servicoRepository = servicoRepository;
        this.barbeiroRepository = barbeiroRepository;
    }

    @GetMapping
    public String inicio(@AuthenticationPrincipal UserDetails user, Model model) {
        Usuario cliente = usuarioService.buscarPorEmail(user.getUsername());
        model.addAttribute("cliente", cliente);
        model.addAttribute("agendamentos", agendamentoService.doCliente(cliente.getId()));
        model.addAttribute("agora", LocalDateTime.now());
        return "cliente/inicio";
    }

    /**
     * Tela de agendamento em etapas: o cliente escolhe serviço, barbeiro e data;
     * a mesma tela é recarregada mostrando os horários livres.
     */
    @GetMapping("/agendar")
    public String agendar(@ModelAttribute("form") AgendamentoForm form, Model model) {
        if (form.getData() == null) {
            form.setData(LocalDate.now());
        }
        carregarOpcoes(model);
        if (form.getServicoId() != null && form.getBarbeiroId() != null) {
            try {
                model.addAttribute("horarios",
                        agendamentoService.horariosDisponiveis(form.getBarbeiroId(), form.getServicoId(), form.getData()));
            } catch (RegraNegocioException e) {
                model.addAttribute("erro", e.getMessage());
            }
        }
        return "cliente/agendar";
    }

    @PostMapping("/agendar")
    public String confirmar(@AuthenticationPrincipal UserDetails user,
                            @Valid @ModelAttribute("form") AgendamentoForm form, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            carregarOpcoes(model);
            model.addAttribute("erro", "Preencha todos os campos e escolha um horário.");
            return "cliente/agendar";
        }
        try {
            Usuario cliente = usuarioService.buscarPorEmail(user.getUsername());
            agendamentoService.agendar(cliente.getId(), form);
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            redirect.addAttribute("servicoId", form.getServicoId());
            redirect.addAttribute("barbeiroId", form.getBarbeiroId());
            redirect.addAttribute("data", form.getData());
            return "redirect:/cliente/agendar";
        }
        redirect.addFlashAttribute("sucesso", "Horário marcado! Te esperamos na Sr. Barba.");
        return "redirect:/cliente";
    }

    @PostMapping("/agendamentos/{id}/cancelar")
    public String cancelar(@AuthenticationPrincipal UserDetails user, @PathVariable Long id,
                           RedirectAttributes redirect) {
        try {
            Usuario cliente = usuarioService.buscarPorEmail(user.getUsername());
            agendamentoService.cancelarPeloCliente(cliente.getId(), id);
            redirect.addFlashAttribute("sucesso", "Agendamento cancelado.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/cliente";
    }

    private void carregarOpcoes(Model model) {
        model.addAttribute("servicos", servicoRepository.findByAtivoTrueOrderByNome());
        model.addAttribute("barbeiros", barbeiroRepository.findByAtivoTrueOrderByNome());
    }
}
