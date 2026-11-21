package br.com.srbarba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.util.TimeZone;

/**
 * Ponto de entrada do sistema da barbearia Sr. Barba.
 */
@SpringBootApplication
public class SrBarbaApplication {

    public static void main(String[] args) {
        // Toda a agenda trabalha no horário de Brasília
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        configurarBancoDaNuvem();
        SpringApplication.run(SrBarbaApplication.class, args);
    }

    /**
     * Em produção (Render, Heroku etc.) o banco PostgreSQL chega na variável DATABASE_URL,
     * no formato postgres://usuario:senha@host:porta/banco. Convertemos para as propriedades
     * do Spring. Sem a variável, o sistema usa o H2 local (desenvolvimento).
     */
    static void configurarBancoDaNuvem() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }
        URI uri = URI.create(databaseUrl.trim());
        String[] credenciais = uri.getUserInfo().split(":", 2);
        int porta = uri.getPort() == -1 ? 5432 : uri.getPort();
        System.setProperty("spring.datasource.url",
                "jdbc:postgresql://" + uri.getHost() + ":" + porta + uri.getPath());
        System.setProperty("spring.datasource.username", credenciais[0]);
        System.setProperty("spring.datasource.password", credenciais.length > 1 ? credenciais[1] : "");
        System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
        System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
    }
}
