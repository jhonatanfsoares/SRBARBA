package br.com.srbarba.service;

import br.com.srbarba.domain.Empresa;
import br.com.srbarba.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public Empresa obter() {
        return empresaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Dados da empresa não cadastrados."));
    }

    public Set<DayOfWeek> diasDeFuncionamento(Empresa empresa) {
        return Arrays.stream(empresa.getDiasFuncionamento().split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }
}
