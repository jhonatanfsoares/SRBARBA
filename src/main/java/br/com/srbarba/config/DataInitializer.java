package br.com.srbarba.config;

import br.com.srbarba.domain.*;
import br.com.srbarba.repository.*;
import br.com.srbarba.service.CpfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Random;

/**
 * Popula o banco com os dados (fictícios) da Sr. Barba na primeira execução:
 * empresa, administrador, barbeiros, serviços, clientes e histórico de atendimentos.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String[] NOMES = {
            "Carlos Eduardo Lima", "Rafael Souza", "Bruno Henrique Alves", "Lucas Martins", "Thiago Ribeiro",
            "Felipe Andrade", "Gustavo Pereira", "Diego Fernandes", "Marcelo Costa", "André Luiz Rocha",
            "Pedro Henrique Dias", "João Victor Nunes", "Matheus Oliveira", "Gabriel Santos", "Rodrigo Barbosa",
            "Vinícius Cardoso", "Leonardo Teixeira", "Eduardo Moreira", "Fábio Gomes", "Ricardo Almeida",
            "Daniel Carvalho", "Henrique Pires", "Igor Mendes", "Paulo Sérgio Freitas", "Renato Castro",
            "Samuel Araújo", "Otávio Monteiro", "Caio Batista"};

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final BarbeiroRepository barbeiroRepository;
    private final ServicoRepository servicoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public DataInitializer(EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository,
                           BarbeiroRepository barbeiroRepository, ServicoRepository servicoRepository,
                           AgendamentoRepository agendamentoRepository, PasswordEncoder passwordEncoder,
                           Clock clock) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.barbeiroRepository = barbeiroRepository;
        this.servicoRepository = servicoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (empresaRepository.count() > 0) {
            return;
        }
        Random random = new Random(42);
        empresaRepository.save(criarEmpresa());

        Usuario admin = new Usuario();
        admin.setNome("Antônio Barbosa (Gerente)");
        admin.setEmail("admin@srbarba.com.br");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setPerfil(Perfil.ADMIN);
        admin.setTelefone("(11) 3456-7890");
        usuarioRepository.save(admin);

        List<Barbeiro> barbeiros = barbeiroRepository.saveAll(List.of(
                new Barbeiro("Antônio Barbosa", "Barba clássica e navalha", "(11) 98765-1001", new BigDecimal("50")),
                new Barbeiro("Jefferson Silva", "Degradê e cortes modernos", "(11) 98765-1002", new BigDecimal("40")),
                new Barbeiro("Marcos Vinícius", "Desenhos e freestyle", "(11) 98765-1003", new BigDecimal("40"))));

        List<Servico> servicos = servicoRepository.saveAll(List.of(
                new Servico("Corte masculino", "Corte na tesoura ou máquina, lavagem e finalização", new BigDecimal("45.00"), 30),
                new Servico("Barba completa", "Toalha quente, navalha e balm hidratante", new BigDecimal("35.00"), 30),
                new Servico("Corte + Barba", "Combo completo com desconto", new BigDecimal("70.00"), 60),
                new Servico("Degradê (fade)", "Fade navalhado com acabamento", new BigDecimal("50.00"), 45),
                new Servico("Sobrancelha", "Design na navalha ou pinça", new BigDecimal("15.00"), 15),
                new Servico("Pigmentação de barba", "Correção de falhas com pigmento", new BigDecimal("40.00"), 30),
                new Servico("Corte infantil", "Até 12 anos", new BigDecimal("35.00"), 30),
                new Servico("Hidratação capilar", "Tratamento com máscara e vapor", new BigDecimal("30.00"), 30)));

        // Cliente de demonstração
        Usuario demo = novoCliente("Cliente Demonstração", "cliente@email.com", "123456", random, 0);
        demo.setDataCadastro(LocalDateTime.now(clock).minusMonths(3));
        usuarioRepository.save(demo);

        List<Usuario> clientes = new java.util.ArrayList<>();
        clientes.add(demo);
        for (int i = 0; i < NOMES.length; i++) {
            String email = NOMES[i].toLowerCase()
                    .replaceAll("[áàâã]", "a").replaceAll("[éê]", "e").replaceAll("í", "i")
                    .replaceAll("[óôõ]", "o").replaceAll("ú", "u").replaceAll("ç", "c")
                    .replace(' ', '.') + "@email.com";
            Usuario c = novoCliente(NOMES[i], email, "123456", random, i + 1);
            // Parte dos clientes cadastrados neste mês, o restante nos meses anteriores
            c.setDataCadastro(LocalDateTime.now(clock).minusDays(i < 6 ? random.nextInt(10) : 30 + random.nextInt(300)));
            clientes.add(usuarioRepository.save(c));
        }

        gerarHistorico(clientes, barbeiros, servicos, random);
        log.info("Dados iniciais da Sr. Barba criados: {} clientes, {} barbeiros, {} serviços",
                clientes.size(), barbeiros.size(), servicos.size());
    }

    private Empresa criarEmpresa() {
        Empresa e = new Empresa();
        e.setRazaoSocial("Sr. Barba Barbearia e Estética Masculina Ltda.");
        e.setNomeFantasia("Sr. Barba");
        e.setCnpj("45.723.174/0001-10");
        e.setInscricaoMunicipal("1.234.567-8");
        e.setAlvaraFuncionamento("ALV-2021/004587 (válido até 12/2027)");
        e.setLicencaSanitaria("CMVS 355030801-960-000123-1-0 (Vigilância Sanitária)");
        e.setRegimeTributario("Simples Nacional");
        e.setCnae("9602-5/01 - Cabeleireiros, manicure e pedicure");
        e.setResponsavelLegal("Antônio Barbosa");
        e.setDataFundacao(LocalDate.of(2021, 3, 15));
        e.setCapitalSocial(new BigDecimal("80000.00"));
        e.setLogradouro("Rua Augusta");
        e.setNumero("1520");
        e.setBairro("Consolação");
        e.setCidade("São Paulo");
        e.setUf("SP");
        e.setCep("01304-001");
        e.setTelefone("(11) 3456-7890");
        e.setWhatsapp("(11) 98765-4321");
        e.setEmail("contato@srbarba.com.br");
        e.setInstagram("@srbarba.oficial");
        e.setHorarioAbertura(LocalTime.of(9, 0));
        e.setHorarioFechamento(LocalTime.of(20, 0));
        e.setDiasFuncionamento("TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY");
        e.setIntervaloAgendaMinutos(15);
        e.setAntecedenciaCancelamentoHoras(2);
        e.setQuantidadeCadeiras(3);
        return e;
    }

    private Usuario novoCliente(String nome, String email, String senha, Random random, int seq) {
        Usuario c = new Usuario();
        c.setNome(nome);
        c.setEmail(email);
        c.setSenha(passwordEncoder.encode(senha));
        c.setCpf(gerarCpf(random));
        c.setTelefone(String.format("(11) 9%04d-%04d", 1000 + random.nextInt(9000), random.nextInt(10000)));
        c.setDataNascimento(LocalDate.of(1970 + random.nextInt(35), 1 + random.nextInt(12), 1 + random.nextInt(28)));
        c.setAceitaComunicacoes(seq % 3 != 0);
        c.setPerfil(Perfil.CLIENTE);
        return c;
    }

    /** Atendimentos dos últimos 45 dias (concluídos) e alguns futuros (agendados). */
    private void gerarHistorico(List<Usuario> clientes, List<Barbeiro> barbeiros, List<Servico> servicos, Random random) {
        Empresa empresa = empresaRepository.findAll().get(0);
        LocalDate hoje = LocalDate.now(clock);
        for (int dia = -45; dia <= 10; dia++) {
            LocalDate data = hoje.plusDays(dia);
            if (data.getDayOfWeek() == DayOfWeek.SUNDAY || data.getDayOfWeek() == DayOfWeek.MONDAY) continue;
            for (Barbeiro b : barbeiros) {
                LocalTime hora = empresa.getHorarioAbertura();
                int atendimentos = dia <= 0 ? 4 + random.nextInt(4) : random.nextInt(3);
                for (int n = 0; n < atendimentos; n++) {
                    hora = hora.plusMinutes(15L * random.nextInt(4));
                    Servico s = servicos.get(random.nextInt(servicos.size()));
                    LocalDateTime inicio = data.atTime(hora);
                    LocalDateTime fim = inicio.plusMinutes(s.getDuracaoMinutos());
                    if (fim.toLocalTime().isAfter(empresa.getHorarioFechamento()) || fim.toLocalDate().isAfter(data)) break;

                    Agendamento a = new Agendamento();
                    a.setCliente(clientes.get(1 + random.nextInt(clientes.size() - 1)));
                    a.setBarbeiro(b);
                    a.setServico(s);
                    a.setDataHoraInicio(inicio);
                    a.setDataHoraFim(fim);
                    a.setValor(s.getPreco());
                    a.setCriadoEm(inicio.minusDays(2));
                    if (inicio.isBefore(LocalDateTime.now(clock))) {
                        int sorte = random.nextInt(20);
                        if (sorte == 0) a.setStatus(StatusAgendamento.CANCELADO);
                        else if (sorte == 1) a.setStatus(StatusAgendamento.NAO_COMPARECEU);
                        else {
                            a.setStatus(StatusAgendamento.CONCLUIDO);
                            a.setFormaPagamento(FormaPagamento.values()[random.nextInt(FormaPagamento.values().length)]);
                        }
                    }
                    agendamentoRepository.save(a);
                    hora = fim.toLocalTime();
                }
            }
        }
    }

    private static String gerarCpf(Random random) {
        String cpf;
        do {
            StringBuilder base = new StringBuilder();
            for (int i = 0; i < 9; i++) base.append(random.nextInt(10));
            cpf = base.toString();
            for (int len = 9; len <= 10; len++) {
                int soma = 0;
                for (int i = 0; i < len; i++) soma += (cpf.charAt(i) - '0') * (len + 1 - i);
                int dv = (soma * 10) % 11;
                cpf += (dv == 10 ? 0 : dv);
            }
        } while (!CpfUtil.isValido(cpf));
        return CpfUtil.formatar(cpf);
    }
}
