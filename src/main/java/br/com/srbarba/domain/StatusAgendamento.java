package br.com.srbarba.domain;

/** Ciclo de vida de um agendamento. */
public enum StatusAgendamento {
    AGENDADO("Agendado"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado"),
    NAO_COMPARECEU("Não compareceu");

    private final String descricao;

    StatusAgendamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Status que ocupam a agenda do barbeiro. */
    public boolean ocupaHorario() {
        return this == AGENDADO || this == CONCLUIDO;
    }
}
