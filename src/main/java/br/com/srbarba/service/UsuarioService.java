package br.com.srbarba.service;

import br.com.srbarba.domain.Perfil;
import br.com.srbarba.domain.Usuario;
import br.com.srbarba.repository.UsuarioRepository;
import br.com.srbarba.web.form.CadastroClienteForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class UsuarioService {

    /** Idade mínima para cadastro sem responsável. */
    static final int IDADE_MINIMA = 12;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrarCliente(CadastroClienteForm form) {
        String email = form.getEmail().trim().toLowerCase();
        String cpf = CpfUtil.formatar(form.getCpf());

        if (!form.getSenha().equals(form.getConfirmacaoSenha())) {
            throw new RegraNegocioException("As senhas não conferem.");
        }
        if (!CpfUtil.isValido(cpf)) {
            throw new RegraNegocioException("CPF inválido.");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Já existe uma conta com este e-mail.");
        }
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new RegraNegocioException("Já existe uma conta com este CPF.");
        }
        if (form.getDataNascimento().isAfter(LocalDate.now().minusYears(IDADE_MINIMA))) {
            throw new RegraNegocioException("Cadastro permitido a partir de " + IDADE_MINIMA + " anos.");
        }

        Usuario u = new Usuario();
        u.setNome(form.getNome().trim());
        u.setEmail(email);
        u.setCpf(cpf);
        u.setTelefone(form.getTelefone().trim());
        u.setDataNascimento(form.getDataNascimento());
        u.setSenha(passwordEncoder.encode(form.getSenha()));
        u.setPerfil(Perfil.CLIENTE);
        u.setAceitaComunicacoes(form.isAceitaComunicacoes());
        return usuarioRepository.save(u);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarClientes(String termo) {
        return usuarioRepository.buscar(Perfil.CLIENTE, termo == null ? "" : termo.trim());
    }

    @Transactional
    public void alternarAtivo(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Cliente não encontrado."));
        if (u.getPerfil() == Perfil.ADMIN) {
            throw new RegraNegocioException("Não é possível bloquear um administrador.");
        }
        u.setAtivo(!u.isAtivo());
    }
}
