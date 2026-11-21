package br.com.srbarba.service;

import br.com.srbarba.domain.Perfil;
import br.com.srbarba.domain.StatusAgendamento;
import br.com.srbarba.repository.AgendamentoRepository;
import br.com.srbarba.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Indicadores do painel administrativo. */
@Service
public class DashboardService {

    public record ItemRanking(String nome, long quantidade, BigDecimal total) {}

    public record ProducaoBarbeiro(String nome, long atendimentos, BigDecimal total, BigDecimal comissao) {}

    public record Resumo(long totalClientes,
                         long novosClientesNoMes,
                         long agendamentosHoje,
                         long agendamentosSemana,
                         BigDecimal faturamentoMes,
                         long atendimentosMes,
                         BigDecimal ticketMedio,
                         long cancelamentosMes,
                         List<ItemRanking> servicosMaisVendidos,
                         List<ProducaoBarbeiro> producaoBarbeiros) {}

    private final UsuarioRepository usuarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final Clock clock;

    public DashboardService(UsuarioRepository usuarioRepository, AgendamentoRepository agendamentoRepository,
                            Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Resumo resumo() {
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = inicioMes.plusMonths(1);

        long totalClientes = usuarioRepository.countByPerfil(Perfil.CLIENTE);
        long novos = usuarioRepository.countByPerfilAndDataCadastroGreaterThanEqual(Perfil.CLIENTE, inicioMes);
        long hojeQtd = agendamentoRepository.countByDataHoraInicioBetweenAndStatus(
                hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay().minusNanos(1), StatusAgendamento.AGENDADO);
        long semanaQtd = agendamentoRepository.countByDataHoraInicioBetweenAndStatus(
                hoje.atStartOfDay(), hoje.plusDays(7).atStartOfDay(), StatusAgendamento.AGENDADO);
        long cancelados = agendamentoRepository.countByDataHoraInicioBetweenAndStatus(
                inicioMes, fimMes.minusNanos(1), StatusAgendamento.CANCELADO);
        BigDecimal faturamento = agendamentoRepository.somarFaturamento(inicioMes, fimMes);

        List<ItemRanking> ranking = agendamentoRepository.rankingServicos(inicioMes, fimMes).stream()
                .map(r -> new ItemRanking((String) r[0], ((Number) r[1]).longValue(), (BigDecimal) r[2]))
                .toList();
        long atendimentos = ranking.stream().mapToLong(ItemRanking::quantidade).sum();
        BigDecimal ticket = atendimentos == 0 ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(atendimentos), 2, RoundingMode.HALF_UP);

        List<ProducaoBarbeiro> producao = agendamentoRepository.producaoBarbeiros(inicioMes, fimMes).stream()
                .map(r -> {
                    BigDecimal total = (BigDecimal) r[2];
                    BigDecimal pct = r[3] == null ? BigDecimal.ZERO : (BigDecimal) r[3];
                    BigDecimal comissao = total.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    return new ProducaoBarbeiro((String) r[0], ((Number) r[1]).longValue(), total, comissao);
                })
                .toList();

        return new Resumo(totalClientes, novos, hojeQtd, semanaQtd, faturamento, atendimentos, ticket,
                cancelados, ranking, producao);
    }
}
