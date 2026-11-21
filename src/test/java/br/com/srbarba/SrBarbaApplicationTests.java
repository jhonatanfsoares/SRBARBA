package br.com.srbarba;

import br.com.srbarba.domain.Perfil;
import br.com.srbarba.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Sobe a aplicação inteira com banco em memória e verifica telas e segurança. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
@AutoConfigureMockMvc
class SrBarbaApplicationTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Test
    void dadosIniciaisCarregados() {
        assertTrue(usuarioRepository.countByPerfil(Perfil.CLIENTE) > 20);
    }

    @Test
    void paginasPublicas() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/cadastro")).andExpect(status().isOk());
    }

    @Test
    void areaAdminExigeLogin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "admin@srbarba.com.br", roles = "ADMIN")
    void telasDoAdmin() throws Exception {
        for (String url : new String[]{"/admin", "/admin/agenda", "/admin/clientes", "/admin/servicos",
                "/admin/barbeiros", "/admin/empresa"}) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(username = "cliente@email.com", roles = "CLIENTE")
    void telasDoCliente() throws Exception {
        mvc.perform(get("/cliente")).andExpect(status().isOk());
        mvc.perform(get("/cliente/agendar").param("servicoId", "1").param("barbeiroId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "cliente@email.com", roles = "CLIENTE")
    void clienteNaoAcessaAdmin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
    }
}
