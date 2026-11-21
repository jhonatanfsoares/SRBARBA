package br.com.srbarba.web.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/** Dados do formulário de novo agendamento. */
public class AgendamentoForm {

    @NotNull(message = "Escolha o serviço")
    private Long servicoId;

    @NotNull(message = "Escolha o barbeiro")
    private Long barbeiroId;

    @NotNull(message = "Escolha a data")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    @NotNull(message = "Escolha o horário")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horario;

    @Size(max = 255, message = "Observação muito longa")
    private String observacao;

    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public Long getBarbeiroId() { return barbeiroId; }
    public void setBarbeiroId(Long barbeiroId) { this.barbeiroId = barbeiroId; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
