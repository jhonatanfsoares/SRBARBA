package br.com.srbarba.service;

import br.com.srbarba.domain.*;
import br.com.srbarba.repository.AgendamentoRepository;
import br.com.srbarba.repository.BarbeiroRepository;
import br.com.srbarba.repository.ServicoRepository;
import br.com.srbarba.repository.UsuarioRepository;
import br.com.srbarba.web.form.AgendamentoForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio da agenda:
 * <ul>
 *   <li>só em dias e horários de funcionamento, em blocos do intervalo configurado;</li>
 *   <li>o serviço precisa terminar até o horário de fechamento;</li>
 *   <li>o barbeiro não pode ter dois atendimentos sobrepostos;</li>
 *   <li>o cliente não pode estar em dois lugares ao mesmo tempo;</li>
 *   <li>no máximo {@value #MAX_AGENDAMENTOS_FUTUROS} agendamentos futuros por cliente;</li>
 *   <li>agenda aberta até {@value #DIAS_MAXIMOS_ANTECEDENCIA} dias à frente;</li>
 *   <li>cancelamento pelo cliente com antecedência mínima configurada na empresa.</li>
 * </ul>
 */
@Service
public class AgendamentoService {

    static final int MAX_AGENDAMENTOS_FUTUROS = 3;
    static final int DIAS_MAXIMOS_ANTECEDENCIA = 60;
    private static final List<StatusAgendamento> STATUS_QUE_OCUPAM =
            List.of(StatusAgendamento.AGENDADO, StatusAgendamento.CONCLUIDO);

    private final AgendamentoRepository agendamentoRepository;
    private final BarbeiroRepository barbeiroRepository;
    private final ServicoRepository servicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaService empresaService;
    private final Clock clock;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              BarbeiroRepository barbeiroRepository,
                              ServicoRepository servicoRepository,
                              UsuarioRepository usuarioRepository,
                              EmpresaService empresaService,
                              Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.barbeiroRepository = barbeiroRepository;
        this.servicoRepository = servicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.empresaService = empresaService;
        this.clock = clock;
    }

    /** Horários livres do barbeiro na data para o serviço escolhido. */
    @Transactional(readOnly = true)
    public List<LocalTime> horariosDisponiveis(Long barbeiroId, Long servicoId, LocalDate data) {
        Empresa empresa = empresaService.obter();
        Servico servico = buscarServico(servicoId);
        LocalDateTime agora = LocalDateTime.now(clock);

        if (!empresaService.diasDeFuncionamento(empresa).contains(data.getDayOfWeek())
                || data.isBefore(agora.toLocalDate())
                || data.isAfter(agora.toLocalDate().plusDays(DIAS_MAXIMOS_ANTECEDENCIA))) {
            return List.of();
        }

        List<Agendamento> ocupados = agendamentoRepository.findSobrepostos(barbeiroId,
                data.atTime(empresa.getHorarioAbertura()), data.atTime(empresa.getHorarioFechamento()),
                STATUS_QUE_OCUPAM);

        List<LocalTime> livres = new ArrayList<>();
        LocalTime t = empresa.getHorarioAbertura();
        LocalTime ultimoInicio = empresa.getHorarioFechamento().minusMinutes(servico.getDuracaoMinutos());
        while (!t.isAfter(ultimoInicio)) {
            LocalDateTime inicio = data.atTime(t);
            LocalDateTime fim = inicio.plusMinutes(servico.getDuracaoMinutos());
            boolean livre = inicio.isAfter(agora) && ocupados.stream().noneMatch(a -> a.conflitaCom(inicio, fim));
            if (livre) {
                livres.add(t);
            }
            t = t.plusMinutes(empresa.getIntervaloAgendaMinutos());
        }
        return livres;
    }

    @Transactional
    public Agendamento agendar(Long clienteId, AgendamentoForm form) {
        Empresa empresa = empresaService.obter();
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new RegraNegocioException("Cliente não encontrado."));
        Barbeiro barbeiro = barbeiroRepository.findById(form.getBarbeiroId())
                .filter(Barbeiro::isAtivo)
                .orElseThrow(() -> new RegraNegocioException("Barbeiro indisponível."));
        Servico servico = buscarServico(form.getServicoId());

        LocalDateTime inicio = form.getData().atTime(form.getHorario());
        LocalDateTime fim = inicio.plusMinutes(servico.getDuracaoMinutos());
        LocalDateTime agora = LocalDateTime.now(clock);

        if (!cliente.isAtivo()) {
            throw new RegraNegocioException("Sua conta está bloqueada. Fale com a barbearia.");
        }
        if (!inicio.isAfter(agora)) {
            throw new RegraNegocioException("Escolha um horário futuro.");
        }
        if (inicio.toLocalDate().isAfter(agora.toLocalDate().plusDays(DIAS_MAXIMOS_ANTECEDENCIA))) {
            throw new RegraNegocioException("A agenda está aberta somente para os próximos "
                    + DIAS_MAXIMOS_ANTECEDENCIA + " dias.");
        }
        if (!empresaService.diasDeFuncionamento(empresa).contains(inicio.getDayOfWeek())) {
            throw new RegraNegocioException("A barbearia não abre neste dia.");
        }
        if (form.getHorario().isBefore(empresa.getHorarioAbertura())
                || fim.toLocalTime().isAfter(empresa.getHorarioFechamento())
                || !fim.toLocalDate().equals(inicio.toLocalDate())) {
            throw new RegraNegocioException("Horário fora do expediente ("
                    + empresa.getHorarioAbertura() + " às " + empresa.getHorarioFechamento() + ").");
        }
        long minutosDesdeAbertura = Duration.between(empresa.getHorarioAbertura(), form.getHorario()).toMinutes();
        if (minutosDesdeAbertura % empresa.getIntervaloAgendaMinutos() != 0) {
            throw new RegraNegocioException("Horário inválido para a agenda.");
        }
        if (!agendamentoRepository.findSobrepostos(barbeiro.getId(), inicio, fim, STATUS_QUE_OCUPAM).isEmpty()) {
            throw new RegraNegocioException("Esse horário acabou de ser ocupado. Escolha outro.");
        }
        List<Agendamento> doCliente = agendamentoRepository.findDoCliente(clienteId);
        boolean clienteOcupado = doCliente.stream().anyMatch(a -> a.conflitaCom(inicio, fim));
        if (clienteOcupado) {
            throw new RegraNegocioException("Você já tem um agendamento nesse horário.");
        }
        long futuros = doCliente.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.AGENDADO && a.getDataHoraInicio().isAfter(agora))
                .count();
        if (futuros >= MAX_AGENDAMENTOS_FUTUROS) {
            throw new RegraNegocioException("Você já possui " + MAX_AGENDAMENTOS_FUTUROS
                    + " agendamentos em aberto. Conclua ou cancele um para marcar outro.");
        }

        Agendamento a = new Agendamento();
        a.setCliente(cliente);
        a.setBarbeiro(barbeiro);
        a.setServico(servico);
        a.setDataHoraInicio(inicio);
        a.setDataHoraFim(fim);
        a.setValor(servico.getPreco());
        a.setObservacao(form.getObservacao());
        a.setCriadoEm(agora);
        return agendamentoRepository.save(a);
    }

    @Transactional
    public void cancelarPeloCliente(Long clienteId, Long agendamentoId) {
        Agendamento a = buscar(agendamentoId);
        if (!a.getCliente().getId().equals(clienteId)) {
            throw new RegraNegocioException("Agendamento não encontrado.");
        }
        if (a.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException("Este agendamento não pode mais ser cancelado.");
        }
        int horas = empresaService.obter().getAntecedenciaCancelamentoHoras();
        if (LocalDateTime.now(clock).plusHours(horas).isAfter(a.getDataHoraInicio())) {
            throw new RegraNegocioException("Cancelamentos só com " + horas
                    + "h de antecedência. Ligue para a barbearia.");
        }
        a.setStatus(StatusAgendamento.CANCELADO);
    }

    /** Usado pela recepção/admin para dar baixa no atendimento. */
    @Transactional
    public void alterarStatus(Long agendamentoId, StatusAgendamento novoStatus, FormaPagamento formaPagamento) {
        Agendamento a = buscar(agendamentoId);
        if (a.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException("Só é possível alterar agendamentos em aberto.");
        }
        if (novoStatus == StatusAgendamento.CONCLUIDO && formaPagamento == null) {
            throw new RegraNegocioException("Informe a forma de pagamento para concluir.");
        }
        a.setStatus(novoStatus);
        a.setFormaPagamento(novoStatus == StatusAgendamento.CONCLUIDO ? formaPagamento : null);
    }

    @Transactional(readOnly = true)
    public List<Agendamento> doCliente(Long clienteId) {
        return agendamentoRepository.findDoCliente(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Agendamento> agendaDoDia(LocalDate data) {
        return agendamentoRepository.findNoPeriodo(data.atStartOfDay(), data.plusDays(1).atStartOfDay());
    }

    private Agendamento buscar(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Agendamento não encontrado."));
    }

    private Servico buscarServico(Long id) {
        return servicoRepository.findById(id)
                .filter(Servico::isAtivo)
                .orElseThrow(() -> new RegraNegocioException("Serviço indisponível."));
    }
}
