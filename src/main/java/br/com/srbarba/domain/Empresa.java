package br.com.srbarba.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dados cadastrais e operacionais da barbearia.
 * Existe um único registro (a própria Sr. Barba).
 */
@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String inscricaoMunicipal;
    private String alvaraFuncionamento;
    private String licencaSanitaria;
    private String regimeTributario;
    private String cnae;
    private String responsavelLegal;
    private LocalDate dataFundacao;
    private BigDecimal capitalSocial;

    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;

    private String telefone;
    private String whatsapp;
    private String email;
    private String instagram;

    private LocalTime horarioAbertura;
    private LocalTime horarioFechamento;
    /** Dias da semana de funcionamento, ex.: "TUESDAY,WEDNESDAY,..." */
    private String diasFuncionamento;
    private Integer intervaloAgendaMinutos;
    private Integer antecedenciaCancelamentoHoras;
    private Integer quantidadeCadeiras;

    public String getEnderecoCompleto() {
        return logradouro + ", " + numero + " - " + bairro + ", " + cidade + "/" + uf + " - CEP " + cep;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getInscricaoMunicipal() { return inscricaoMunicipal; }
    public void setInscricaoMunicipal(String inscricaoMunicipal) { this.inscricaoMunicipal = inscricaoMunicipal; }
    public String getAlvaraFuncionamento() { return alvaraFuncionamento; }
    public void setAlvaraFuncionamento(String alvaraFuncionamento) { this.alvaraFuncionamento = alvaraFuncionamento; }
    public String getLicencaSanitaria() { return licencaSanitaria; }
    public void setLicencaSanitaria(String licencaSanitaria) { this.licencaSanitaria = licencaSanitaria; }
    public String getRegimeTributario() { return regimeTributario; }
    public void setRegimeTributario(String regimeTributario) { this.regimeTributario = regimeTributario; }
    public String getCnae() { return cnae; }
    public void setCnae(String cnae) { this.cnae = cnae; }
    public String getResponsavelLegal() { return responsavelLegal; }
    public void setResponsavelLegal(String responsavelLegal) { this.responsavelLegal = responsavelLegal; }
    public LocalDate getDataFundacao() { return dataFundacao; }
    public void setDataFundacao(LocalDate dataFundacao) { this.dataFundacao = dataFundacao; }
    public BigDecimal getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(BigDecimal capitalSocial) { this.capitalSocial = capitalSocial; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }
    public LocalTime getHorarioAbertura() { return horarioAbertura; }
    public void setHorarioAbertura(LocalTime horarioAbertura) { this.horarioAbertura = horarioAbertura; }
    public LocalTime getHorarioFechamento() { return horarioFechamento; }
    public void setHorarioFechamento(LocalTime horarioFechamento) { this.horarioFechamento = horarioFechamento; }
    public String getDiasFuncionamento() { return diasFuncionamento; }
    public void setDiasFuncionamento(String diasFuncionamento) { this.diasFuncionamento = diasFuncionamento; }
    public Integer getIntervaloAgendaMinutos() { return intervaloAgendaMinutos; }
    public void setIntervaloAgendaMinutos(Integer intervaloAgendaMinutos) { this.intervaloAgendaMinutos = intervaloAgendaMinutos; }
    public Integer getAntecedenciaCancelamentoHoras() { return antecedenciaCancelamentoHoras; }
    public void setAntecedenciaCancelamentoHoras(Integer antecedenciaCancelamentoHoras) { this.antecedenciaCancelamentoHoras = antecedenciaCancelamentoHoras; }
    public Integer getQuantidadeCadeiras() { return quantidadeCadeiras; }
    public void setQuantidadeCadeiras(Integer quantidadeCadeiras) { this.quantidadeCadeiras = quantidadeCadeiras; }
}
