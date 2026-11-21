package br.com.srbarba.web.form;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Dados digitados na tela de cadastro de cliente. */
public class CadastroClienteForm {

    @NotBlank(message = "Informe seu nome completo")
    @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
    private String nome;

    @NotBlank(message = "Informe seu e-mail")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Informe seu CPF")
    @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF no formato 000.000.000-00")
    private String cpf;

    @NotBlank(message = "Informe seu telefone/WhatsApp")
    @Pattern(regexp = "\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}", message = "Telefone no formato (11) 91234-5678")
    private String telefone;

    @NotNull(message = "Informe sua data de nascimento")
    @Past(message = "Data de nascimento inválida")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @NotBlank(message = "Crie uma senha")
    @Size(min = 6, max = 60, message = "A senha deve ter no mínimo 6 caracteres")
    private String senha;

    @NotBlank(message = "Confirme a senha")
    private String confirmacaoSenha;

    @AssertTrue(message = "É preciso aceitar os termos de uso e a política de privacidade")
    private boolean aceitaTermos;

    private boolean aceitaComunicacoes;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getConfirmacaoSenha() { return confirmacaoSenha; }
    public void setConfirmacaoSenha(String confirmacaoSenha) { this.confirmacaoSenha = confirmacaoSenha; }
    public boolean isAceitaTermos() { return aceitaTermos; }
    public void setAceitaTermos(boolean aceitaTermos) { this.aceitaTermos = aceitaTermos; }
    public boolean isAceitaComunicacoes() { return aceitaComunicacoes; }
    public void setAceitaComunicacoes(boolean aceitaComunicacoes) { this.aceitaComunicacoes = aceitaComunicacoes; }
}
