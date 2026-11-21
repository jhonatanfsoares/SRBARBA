package br.com.srbarba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do sistema da barbearia Sr. Barba.
 */
@SpringBootApplication
public class SrBarbaApplication {

    public static void main(String[] args) {
        // Toda a agenda trabalha no horário de Brasília
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(SrBarbaApplication.class, args);
    }
}
