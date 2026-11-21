package br.com.srbarba.repository;

import br.com.srbarba.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findFirstByOrderByIdAsc();
}
