package clubinho.service;

import clubinho.data.Config;
import clubinho.data.ReservaRepository;
import clubinho.model.Reserva;
import clubinho.model.Servico;
import clubinho.model.Status;
import clubinho.util.Fmt;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Regras de negócio do Clubinho do Focinho: validação, vagas por dia e consultas para as telas. */
public class ReservaService {
    private final ReservaRepository repo;
    private final Config config;
    private final Clock relogio;

    public ReservaService(ReservaRepository repo, Config config) { this(repo, config, Clock.systemDefaultZone()); }

    public ReservaService(ReservaRepository repo, Config config, Clock relogio) {
        this.repo = repo;
        this.config = config;
        this.relogio = relogio;
    }

    public Config config() { return config; }

    public LocalDate hoje() { return LocalDate.now(relogio); }

    public List<Reserva> todas() { return repo.listar(); }

    // ------------------------------------------------------------------ vagas

    public int vagas(Servico s) {
        return s == Servico.HOSPEDAGEM ? config.vagasHospedagem() : config.vagasCreche();
    }

    /** Quantas vagas desse serviço estão ocupadas no dia (ignorando uma reserva, útil ao editar). */
    public int ocupacao(LocalDate dia, Servico s, int ignorarId) {
        int n = 0;
        for (Reserva r : repo.listar()) {
            if (r.id() != ignorarId && r.servico() == s && r.ocupaVagaEm(dia)) n++;
        }
        return n;
    }

    public boolean agendaFechada(LocalDate dia) {
        return config.agendaFechadaAte() != null && !dia.isAfter(config.agendaFechadaAte());
    }

    /**
     * Problemas de disponibilidade para o período. Lista vazia = pode reservar.
     * A regra "agenda fechada" só vale para reservas novas (ignorarId == 0).
     */
    public List<String> problemasDeVagas(Servico servico, LocalDate entrada, LocalDate saida, int ignorarId) {
        List<String> problemas = new ArrayList<>();
        if (servico == null || entrada == null) return problemas;

        if (ignorarId == 0 && agendaFechada(entrada)) {
            problemas.add("A agenda está fechada até " + Fmt.data(config.agendaFechadaAte())
                    + ". Para aceitar reservas nesse período, altere em Agenda > Configurações.");
            return problemas;
        }

        List<LocalDate> dias = new ArrayList<>();
        if (servico == Servico.HOSPEDAGEM) {
            if (saida == null || !saida.isAfter(entrada)) return problemas;
            for (LocalDate d = entrada; d.isBefore(saida); d = d.plusDays(1)) dias.add(d);
        } else {
            dias.add(entrada);
        }

        int limite = vagas(servico);
        List<LocalDate> cheios = new ArrayList<>();
        for (LocalDate d : dias) {
            if (ocupacao(d, servico, ignorarId) >= limite) cheios.add(d);
        }
        if (!cheios.isEmpty()) {
            String onde = servico == Servico.HOSPEDAGEM ? "de hospedagem na noite de " : "na creche em ";
            problemas.add("Sem vagas " + onde + Fmt.data(cheios.get(0)) + " (" + limite + " de " + limite
                    + " ocupadas)" + (cheios.size() > 1 ? " e em mais " + (cheios.size() - 1) + " data(s)." : "."));
        }
        return problemas;
    }

    // -------------------------------------------------------------- validação

    public List<String> validar(Reserva r) {
        List<String> erros = new ArrayList<>();
        if (Fmt.vazio(r.tutorNome())) erros.add("Informe o nome completo do tutor.");
        if (Fmt.vazio(r.contato())) {
            erros.add("Informe um contato (WhatsApp e ligação).");
        } else if (r.contato().replaceAll("\\D", "").length() < 10) {
            erros.add("O contato precisa ter DDD + número (ex.: (21) 99999-9999).");
        }
        if (Fmt.vazio(r.petNome())) erros.add("Informe o nome do pet.");
        if (r.medicacao() && Fmt.vazio(r.medicacaoDetalhe())) {
            erros.add("Você marcou que o pet toma medicação: informe qual(is) e os horários.");
        }
        if (r.servico() == null) erros.add("Escolha o serviço (hospedagem ou creche).");

        if (r.entrada() == null) {
            erros.add("Informe a data de entrada no formato dd/mm/aaaa.");
        } else if (r.id() == 0 && r.entrada().isBefore(hoje())) {
            erros.add("A data de entrada não pode ser anterior a hoje (" + Fmt.data(hoje()) + ").");
        }

        if (r.servico() == Servico.HOSPEDAGEM) {
            if (r.saida() == null) {
                erros.add("Informe a data de saída no formato dd/mm/aaaa.");
            } else if (r.entrada() != null && !r.saida().isAfter(r.entrada())) {
                erros.add("A data de saída precisa ser depois da data de entrada.");
            }
        }

        if (r.confirmada() && erros.isEmpty()) {
            erros.addAll(problemasDeVagas(r.servico(), r.entrada(), r.saidaEfetiva(), r.id()));
        }
        return erros;
    }

    /** Valida e grava. Para creche, a saída é ajustada para o mesmo dia da entrada. */
    public Reserva salvar(Reserva r) throws ValidacaoException, IOException {
        if (r.servico() == Servico.CRECHE && r.entrada() != null) {
            r = new Reserva(r.id(), r.servico(), r.status(), r.tutorNome(), r.contato(), r.endereco(), r.petNome(),
                    r.raca(), r.idade(), r.castrado(), r.alimentacao(), r.outrosAlimentos(), r.medicacao(),
                    r.medicacaoDetalhe(), r.comportamento(), r.entrada(), r.entrada(), r.checkIn(), r.checkOut(),
                    r.criadaEm());
        }
        if (r.criadaEm() == null) {
            r = new Reserva(r.id(), r.servico(), r.status(), r.tutorNome(), r.contato(), r.endereco(), r.petNome(),
                    r.raca(), r.idade(), r.castrado(), r.alimentacao(), r.outrosAlimentos(), r.medicacao(),
                    r.medicacaoDetalhe(), r.comportamento(), r.entrada(), r.saida(), r.checkIn(), r.checkOut(),
                    LocalDateTime.now(relogio));
        }
        List<String> erros = validar(r);
        if (!erros.isEmpty()) throw new ValidacaoException(erros);
        return repo.salvar(r);
    }

    public void cancelar(int id) throws IOException {
        var r = repo.buscar(id);
        if (r.isPresent()) repo.salvar(r.get().comStatus(Status.CANCELADA));
    }

    // -------------------------------------------------------------- consultas

    /** Texto mostrado na tela: Cancelada, Concluída, Em andamento ou Confirmada. */
    public String situacao(Reserva r) {
        if (!r.confirmada()) return Status.CANCELADA.rotulo();
        LocalDate h = hoje();
        if (r.saidaEfetiva().isBefore(h)) return "Concluída";
        if (!r.entrada().isAfter(h)) return "Em andamento";
        return Status.CONFIRMADA.rotulo();
    }

    /** Busca por nome do tutor, nome do pet ou contato. Filtros nulos = sem filtro. */
    public List<Reserva> buscar(String texto, Status status, Servico servico) {
        String q = normalizar(texto == null ? "" : texto);
        String qDigitos = q.replaceAll("\\D", "");
        LocalDate h = hoje();
        List<Reserva> res = new ArrayList<>();
        for (Reserva r : repo.listar()) {
            if (status != null && r.status() != status) continue;
            if (servico != null && r.servico() != servico) continue;
            if (!q.isEmpty()) {
                boolean achou = normalizar(r.tutorNome()).contains(q) || normalizar(r.petNome()).contains(q)
                        || (!qDigitos.isEmpty() && r.contato().replaceAll("\\D", "").contains(qDigitos));
                if (!achou) continue;
            }
            res.add(r);
        }
        // próximas/ativas primeiro (da mais cedo para a mais tarde); depois o histórico (da mais recente para a mais antiga)
        Comparator<Reserva> porData = Comparator.comparing(Reserva::entrada);
        res.sort(Comparator
                .comparingInt((Reserva r) -> r.confirmada() && !r.saidaEfetiva().isBefore(h) ? 0 : 1)
                .thenComparing((a, b) -> {
                    boolean ativa = a.confirmada() && !a.saidaEfetiva().isBefore(h);
                    return ativa ? porData.compare(a, b) : porData.reversed().compare(a, b);
                }));
        return res;
    }

    public long hospedadosEm(LocalDate dia) {
        return repo.listar().stream().filter(r -> r.hospedadoEm(dia)).count();
    }

    public long chegadasEm(LocalDate dia) {
        return repo.listar().stream().filter(r -> r.confirmada() && dia.equals(r.entrada())).count();
    }

    public long saidasEm(LocalDate dia) {
        return repo.listar().stream().filter(r -> r.confirmada() && dia.equals(r.saidaEfetiva())).count();
    }

    public long crecheEm(LocalDate dia) {
        return repo.listar().stream()
                .filter(r -> r.confirmada() && r.servico() == Servico.CRECHE && dia.equals(r.entrada())).count();
    }

    /** Chegadas de hoje até hoje + dias, em ordem. */
    public List<Reserva> proximasChegadas(int dias) {
        LocalDate h = hoje();
        return repo.listar().stream()
                .filter(r -> r.confirmada() && !r.entrada().isBefore(h) && !r.entrada().isAfter(h.plusDays(dias)))
                .sorted(Comparator.comparing(Reserva::entrada)
                        .thenComparing(Reserva::checkIn, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /** Saídas de hoje até hoje + dias, em ordem (creche sai no mesmo dia da entrada). */
    public List<Reserva> proximasSaidas(int dias) {
        LocalDate h = hoje();
        return repo.listar().stream()
                .filter(r -> r.confirmada() && !r.saidaEfetiva().isBefore(h) && !r.saidaEfetiva().isAfter(h.plusDays(dias)))
                .sorted(Comparator.comparing(Reserva::saidaEfetiva)
                        .thenComparing(Reserva::checkOut, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /** Reservas que ocupam vaga do serviço no dia (para a agenda). */
    public List<Reserva> doDia(LocalDate dia, Servico s) {
        return repo.listar().stream()
                .filter(r -> r.servico() == s && r.ocupaVagaEm(dia))
                .sorted(Comparator.comparing(Reserva::petNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
