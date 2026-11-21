package br.com.srbarba.repository;

import br.com.srbarba.domain.Agendamento;
import br.com.srbarba.domain.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /** Agendamentos de um barbeiro que tocam o intervalo informado (usado para checar conflito). */
    @Query("""
            select a from Agendamento a
            where a.barbeiro.id = :barbeiroId
              and a.status in :status
              and a.dataHoraInicio < :fim
              and a.dataHoraFim > :inicio
            """)
    List<Agendamento> findSobrepostos(@Param("barbeiroId") Long barbeiroId,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fim") LocalDateTime fim,
                                      @Param("status") List<StatusAgendamento> status);

    @Query("""
            select a from Agendamento a
            join fetch a.cliente join fetch a.barbeiro join fetch a.servico
            where a.cliente.id = :clienteId
            order by a.dataHoraInicio desc
            """)
    List<Agendamento> findDoCliente(@Param("clienteId") Long clienteId);

    @Query("""
            select a from Agendamento a
            join fetch a.cliente join fetch a.barbeiro join fetch a.servico
            where a.dataHoraInicio >= :inicio and a.dataHoraInicio < :fim
            order by a.dataHoraInicio, a.barbeiro.nome
            """)
    List<Agendamento> findNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    long countByDataHoraInicioBetweenAndStatus(LocalDateTime inicio, LocalDateTime fim, StatusAgendamento status);

    long countByClienteIdAndStatus(Long clienteId, StatusAgendamento status);

    @Query("""
            select coalesce(sum(a.valor), 0) from Agendamento a
            where a.status = br.com.srbarba.domain.StatusAgendamento.CONCLUIDO
              and a.dataHoraInicio >= :inicio and a.dataHoraInicio < :fim
            """)
    BigDecimal somarFaturamento(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    /** Ranking de serviços concluídos no período: [nome, quantidade, total]. */
    @Query("""
            select a.servico.nome, count(a), sum(a.valor) from Agendamento a
            where a.status = br.com.srbarba.domain.StatusAgendamento.CONCLUIDO
              and a.dataHoraInicio >= :inicio and a.dataHoraInicio < :fim
            group by a.servico.nome
            order by count(a) desc
            """)
    List<Object[]> rankingServicos(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    /** Produção por barbeiro no período: [nome, atendimentos, total, percentualComissao]. */
    @Query("""
            select a.barbeiro.nome, count(a), sum(a.valor), a.barbeiro.percentualComissao from Agendamento a
            where a.status = br.com.srbarba.domain.StatusAgendamento.CONCLUIDO
              and a.dataHoraInicio >= :inicio and a.dataHoraInicio < :fim
            group by a.barbeiro.nome, a.barbeiro.percentualComissao
            order by sum(a.valor) desc
            """)
    List<Object[]> producaoBarbeiros(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
