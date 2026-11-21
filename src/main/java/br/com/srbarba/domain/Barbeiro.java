package br.com.srbarba.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Profissional que executa os serviços. */
@Entity
@Table(name = "barbeiro")
public class Barbeiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    private String especialidade;
    private String telefone;

    /** Percentual de comissão sobre os serviços realizados (ex.: 40 = 40%). */
    private BigDecimal percentualComissao;

    private boolean ativo = true;

    public Barbeiro() {}

    public Barbeiro(String nome, String especialidade, String telefone, BigDecimal percentualComissao) {
        this.nome = nome;
        this.especialidade = especialidade;
        this.telefone = telefone;
        this.percentualComissao = percentualComissao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public BigDecimal getPercentualComissao() { return percentualComissao; }
    public void setPercentualComissao(BigDecimal percentualComissao) { this.percentualComissao = percentualComissao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
