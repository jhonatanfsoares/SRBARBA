package br.com.srbarba.security;

import br.com.srbarba.domain.Usuario;
import br.com.srbarba.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Carrega o usuário pelo e-mail para o Spring Security autenticar. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("E-mail não cadastrado"));
        return User.withUsername(u.getEmail())
                .password(u.getSenha())
                .roles(u.getPerfil().name())
                .disabled(!u.isAtivo())
                .build();
    }
}
