package br.com.srbarba.service;

import br.com.srbarba.domain.*;
import br.com.srbarba.repository.AgendamentoRepository;
import br.com.srbarba.repository.BarbeiroRepository;
import br.com.srbarba.repository.ServicoRepository;
import br.com.srbarba.repository.UsuarioRepository;
import br.com.srbarba.web.form.AgendamentoForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Testes unitários das regras de agenda (sem banco, com relógio fixo). */
class AgendamentoServiceTest {

    // Quarta-feira, 10:00 em São Paulo
    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 18, 10, 0);
    private static final LocalDate QUINTA = LocalDate.of(2026, 11, 19);

    private AgendamentoRepository agendamentoRepository;
    private AgendamentoService service;
    private Empresa empresa;
    private Usuario cliente;
    private Barbeiro barbeiro;
    private Servico corte;

    @BeforeEach
    void setUp() {
        agendamentoRepository = mock(AgendamentoRepository.class);
        BarbeiroRepository barbeiroRepository = mock(BarbeiroRepository.class);
        ServicoRepository servicoRepository = mock(ServicoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        EmpresaService empresaService = mock(EmpresaService.class);

        empresa = new Empresa();
        empresa.setHorarioAbertura(LocalTime.of(9, 0));
        empresa.setHorarioFechamento(LocalTime.of(20, 0));
        empresa.setDiasFuncionamento("TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY");
        empresa.setIntervaloAgendaMinutos(15);
        empresa.setAntecedenciaCancelamentoHoras(2);
        when(empresaService.obter()).thenReturn(empresa);
        when(empresaService.diasDeFuncionamento(any())).thenCallRealMethod();

        cliente = new Usuario();
        cliente.setId(1L);
        barbeiro = new Barbeiro("Jefferson", "Degradê", null, new BigDecimal("40"));
        barbeiro.setId(10L);
        corte = new Servico("Corte", null, new BigDecimal("45.00"), 30);
        corte.setId(100L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(barbeiroRepository.findById(10L)).thenReturn(Optional.of(barbeiro));
        when(servicoRepository.findById(100L)).thenReturn(Optional.of(corte));
        when(agendamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Clock clock = Clock.fixed(AGORA.atZone(SP).toInstant(), SP);
        service = new AgendamentoService(agendamentoRepository, barbeiroRepository, servicoRepository,
                usuarioRepository, empresaService, clock);
    }

    private AgendamentoForm form(LocalDate data, LocalTime hora) {
        AgendamentoForm f = new AgendamentoForm();
        f.setServicoId(100L);
        f.setBarbeiroId(10L);
        f.setData(data);
        f.setHorario(hora);
        return f;
    }

    private Agendamento existente(LocalDateTime inicio, int minutos, StatusAgendamento status) {
        Agendamento a = new Agendamento();
        a.setCliente(cliente);
        a.setBarbeiro(barbeiro);
        a.setServico(corte);
        a.setDataHoraInicio(inicio);
        a.setDataHoraFim(inicio.plusMinutes(minutos));
        a.setStatus(status);
        return a;
    }

    @Test
    void agendaHorarioLivreComPrecoDoServico() {
        Agendamento a = service.agendar(1L, form(QUINTA, LocalTime.of(14, 0)));
        assertEquals(QUINTA.atTime(14, 30), a.getDataHoraFim());
        assertEquals(new BigDecimal("45.00"), a.getValor());
        assertEquals(StatusAgendamento.AGENDADO, a.getStatus());
    }

    @Test
    void recusaHorarioOcupadoDoBarbeiro() {
        when(agendamentoRepository.findSobrepostos(eq(10L), any(), any(), anyList()))
                .thenReturn(List.of(existente(QUINTA.atTime(13, 45), 30, StatusAgendamento.AGENDADO)));
        var ex = assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, form(QUINTA, LocalTime.of(14, 0))));
        assertTrue(ex.getMessage().contains("ocupado"));
    }

    @Test
    void recusaDomingoESegunda() {
        assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, form(LocalDate.of(2026, 11, 22), LocalTime.of(10, 0))));
        assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, form(LocalDate.of(2026, 11, 23), LocalTime.of(10, 0))));
    }

    @Test
    void recusaServicoQueTerminaDepoisDoFechamento() {
        assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, form(QUINTA, LocalTime.of(19, 45))));
    }

    @Test
    void recusaHorarioNoPassado() {
        assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, form(AGORA.toLocalDate(), LocalTime.of(9, 30))));
    }

    @Test
    void recusaHorarioForaDaGradeDe15Minutos() {
        assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, form(QUINTA, LocalTime.of(14, 10))));
    }

    @Test
    void limitaAgendamentosEmAberto() {
        when(agendamentoRepository.findDoCliente(1L)).thenReturn(List.of(
                existente(QUINTA.atTime(9, 0), 30, StatusAgendamento.AGENDADO),
                existente(QUINTA.atTime(10, 0), 30, StatusAgendamento.AGENDADO),
                existente(QUINTA.atTime(11, 0), 30, StatusAgendamento.AGENDADO)));
        var ex = assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, form(QUINTA, LocalTime.of(15, 0))));
        assertTrue(ex.getMessage().contains("em aberto"));
    }

    @Test
    void horariosDisponiveisPulamOsOcupados() {
        when(agendamentoRepository.findSobrepostos(eq(10L), any(), any(), anyList()))
                .thenReturn(List.of(existente(QUINTA.atTime(9, 0), 60, StatusAgendamento.AGENDADO)));
        List<LocalTime> livres = service.horariosDisponiveis(10L, 100L, QUINTA);
        assertFalse(livres.contains(LocalTime.of(9, 0)));
        assertFalse(livres.contains(LocalTime.of(9, 45)));
        assertTrue(livres.contains(LocalTime.of(10, 0)));
        assertEquals(LocalTime.of(19, 30), livres.get(livres.size() - 1));
    }

    @Test
    void cancelamentoRespeitaAntecedencia() {
        Agendamento emUmaHora = existente(AGORA.plusHours(1), 30, StatusAgendamento.AGENDADO);
        emUmaHora.setId(5L);
        when(agendamentoRepository.findById(5L)).thenReturn(Optional.of(emUmaHora));
        assertThrows(RegraNegocioException.class, () -> service.cancelarPeloCliente(1L, 5L));

        Agendamento amanha = existente(QUINTA.atTime(14, 0), 30, StatusAgendamento.AGENDADO);
        amanha.setId(6L);
        when(agendamentoRepository.findById(6L)).thenReturn(Optional.of(amanha));
        service.cancelarPeloCliente(1L, 6L);
        assertEquals(StatusAgendamento.CANCELADO, amanha.getStatus());
    }

    @Test
    void concluirExigeFormaDePagamento() {
        Agendamento a = existente(QUINTA.atTime(14, 0), 30, StatusAgendamento.AGENDADO);
        when(agendamentoRepository.findById(7L)).thenReturn(Optional.of(a));
        assertThrows(RegraNegocioException.class,
                () -> service.alterarStatus(7L, StatusAgendamento.CONCLUIDO, null));
        service.alterarStatus(7L, StatusAgendamento.CONCLUIDO, FormaPagamento.PIX);
        assertEquals(FormaPagamento.PIX, a.getFormaPagamento());
    }
}
