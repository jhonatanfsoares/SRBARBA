package br.com.srbarba.service;

/** Violação de uma regra de negócio; a mensagem é exibida ao usuário. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
