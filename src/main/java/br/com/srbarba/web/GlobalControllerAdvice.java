package br.com.srbarba.web;

import br.com.srbarba.domain.Empresa;
import br.com.srbarba.service.EmpresaService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Disponibiliza os dados da empresa em todas as telas (cabeçalho e rodapé). */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final EmpresaService empresaService;

    public GlobalControllerAdvice(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @ModelAttribute("empresa")
    public Empresa empresa() {
        return empresaService.obter();
    }
}
