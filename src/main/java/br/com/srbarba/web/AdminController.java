package br.com.srbarba.web;

import br.com.srbarba.domain.Barbeiro;
import br.com.srbarba.domain.FormaPagamento;
import br.com.srbarba.domain.Servico;
import br.com.srbarba.domain.StatusAgendamento;
import br.com.srbarba.repository.BarbeiroRepository;
import br.com.srbarba.repository.ServicoRepository;
import br.com.srbarba.service.AgendamentoService;
import br.com.srbarba.service.DashboardService;
import br.com.srbarba.service.RegraNegocioException;
import br.com.srbarba.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Área administrativa: painel, agenda, clientes, serviços, equipe e dados da empresa. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final AgendamentoService agendamentoService;
    private final UsuarioService usuarioService;
    private final ServicoRepository servicoRepository;
    private final BarbeiroRepository barbeiroRepository;

    public AdminController(DashboardService dashboardService, AgendamentoService agendamentoService,
                           UsuarioService usuarioService, ServicoRepository servicoRepository,
                           BarbeiroRepository barbeiroRepository) {
        this.dashboardService = dashboardService;
        this.agendamentoService = agendamentoService;
        this.usuarioService = usuarioService;
        this.servicoRepository = servicoRepository;
        this.barbeiroRepository = barbeiroRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("resumo", dashboardService.resumo());
        model.addAttribute("agendaHoje", agendamentoService.agendaDoDia(LocalDate.now()));
        return "admin/dashboard";
    }

    // ---------- Agenda ----------

    @GetMapping("/agenda")
    public String agenda(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                         Model model) {
        LocalDate dia = data == null ? LocalDate.now() : data;
        model.addAttribute("data", dia);
        model.addAttribute("agendamentos", agendamentoService.agendaDoDia(dia));
        model.addAttribute("formasPagamento", FormaPagamento.values());
        return "admin/agenda";
    }

    @PostMapping("/agendamentos/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusAgendamento status,
                                @RequestParam(required = false) FormaPagamento formaPagamento,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                RedirectAttributes redirect) {
        try {
            agendamentoService.alterarStatus(id, status, formaPagamento);
            redirect.addFlashAttribute("sucesso", "Agendamento atualizado para: " + status.getDescricao());
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        redirect.addAttribute("data", data.toString()); // ISO yyyy-MM-dd
        return "redirect:/admin/agenda";
    }

    // ---------- Clientes ----------

    @GetMapping("/clientes")
    public String clientes(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("q", q);
        model.addAttribute("clientes", usuarioService.buscarClientes(q));
        return "admin/clientes";
    }

    @PostMapping("/clientes/{id}/ativo")
    public String alternarCliente(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            usuarioService.alternarAtivo(id);
            redirect.addFlashAttribute("sucesso", "Situação do cliente atualizada.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/clientes";
    }

    // ---------- Serviços ----------

    @GetMapping("/servicos")
    public String servicos(Model model) {
        model.addAttribute("servicos", servicoRepository.findAllByOrderByNome());
        if (!model.containsAttribute("servico")) {
            model.addAttribute("servico", new Servico());
        }
        return "admin/servicos";
    }

    @GetMapping("/servicos/{id}")
    public String editarServico(@PathVariable Long id, Model model) {
        model.addAttribute("servico", servicoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Serviço não encontrado.")));
        return servicos(model);
    }

    @PostMapping("/servicos")
    public String salvarServico(@Valid @ModelAttribute("servico") Servico servico, BindingResult result,
                                Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("servicos", servicoRepository.findAllByOrderByNome());
            return "admin/servicos";
        }
        servicoRepository.save(servico);
        redirect.addFlashAttribute("sucesso", "Serviço salvo.");
        return "redirect:/admin/servicos";
    }

    // ---------- Barbeiros ----------

    @GetMapping("/barbeiros")
    public String barbeiros(Model model) {
        model.addAttribute("barbeiros", barbeiroRepository.findAll());
        return "admin/barbeiros";
    }

    @PostMapping("/barbeiros")
    public String novoBarbeiro(@RequestParam String nome, @RequestParam(required = false) String especialidade,
                               @RequestParam(required = false) String telefone,
                               @RequestParam BigDecimal percentualComissao, RedirectAttributes redirect) {
        if (nome.isBlank()) {
            redirect.addFlashAttribute("erro", "Informe o nome do barbeiro.");
        } else {
            barbeiroRepository.save(new Barbeiro(nome.trim(), especialidade, telefone, percentualComissao));
            redirect.addFlashAttribute("sucesso", "Barbeiro cadastrado.");
        }
        return "redirect:/admin/barbeiros";
    }

    @PostMapping("/barbeiros/{id}/ativo")
    public String alternarBarbeiro(@PathVariable Long id, RedirectAttributes redirect) {
        barbeiroRepository.findById(id).ifPresent(b -> {
            b.setAtivo(!b.isAtivo());
            barbeiroRepository.save(b);
        });
        redirect.addFlashAttribute("sucesso", "Situação do barbeiro atualizada.");
        return "redirect:/admin/barbeiros";
    }

    // ---------- Empresa ----------

    @GetMapping("/empresa")
    public String empresa() {
        return "admin/empresa";
    }
}
