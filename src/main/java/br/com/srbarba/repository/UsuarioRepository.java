package br.com.srbarba.repository;

import br.com.srbarba.domain.Perfil;
import br.com.srbarba.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    long countByPerfil(Perfil perfil);

    long countByPerfilAndDataCadastroGreaterThanEqual(Perfil perfil, LocalDateTime desde);

    @Query("""
            select u from Usuario u
            where u.perfil = :perfil
              and (:termo = ''
                   or lower(u.nome) like lower(concat('%', :termo, '%'))
                   or lower(u.email) like lower(concat('%', :termo, '%'))
                   or u.telefone like concat('%', :termo, '%')
                   or u.cpf like concat('%', :termo, '%'))
            order by u.nome
            """)
    List<Usuario> buscar(@Param("perfil") Perfil perfil, @Param("termo") String termo);
}
