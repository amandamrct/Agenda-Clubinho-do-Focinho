package clubinho;

import clubinho.data.Config;
import clubinho.data.ReservaRepository;
import clubinho.model.Reserva;
import clubinho.model.Servico;
import clubinho.model.Status;
import clubinho.service.ReservaService;
import clubinho.service.ValidacaoException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;

/** Testes simples das regras (sem JUnit): java -cp out clubinho.TesteRegras */
public class TesteRegras {
    static int falhas = 0;

    static void ok(boolean cond, String nome) {
        System.out.println((cond ? "  ok  " : " FALHOU ") + nome);
        if (!cond) falhas++;
    }

    static Reserva nova(Servico s, LocalDate ent, LocalDate sai) {
        return new Reserva(0, s, Status.CONFIRMADA, "Ana Souza", "(21) 99999-1111", "Rua A, 1", "Rex", "SRD", "3 anos",
                "Sim", "7h e 18h", "", false, "", "Calmo", ent, sai, LocalTime.of(10, 0), LocalTime.of(16, 0), null);
    }

    public static void main(String[] args) throws Exception {
        LocalDate hoje = LocalDate.of(2027, 3, 10);
        Clock relogio = Clock.fixed(hoje.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        Config cfg = Config.emMemoria();
        cfg.setVagasHospedagem(2);
        cfg.setVagasCreche(1);
        ReservaService svc = new ReservaService(ReservaRepository.emMemoria(), cfg, relogio);

        System.out.println("Validação básica");
        ok(svc.validar(nova(Servico.HOSPEDAGEM, hoje.plusDays(1), hoje.plusDays(3))).isEmpty(), "reserva válida passa");
        ok(!svc.validar(nova(Servico.HOSPEDAGEM, hoje.minusDays(1), hoje.plusDays(1))).isEmpty(), "entrada no passado é recusada");
        ok(!svc.validar(nova(Servico.HOSPEDAGEM, hoje.plusDays(3), hoje.plusDays(3))).isEmpty(), "saída igual à entrada é recusada");
        ok(!svc.validar(nova(Servico.HOSPEDAGEM, hoje.plusDays(3), hoje.plusDays(2))).isEmpty(), "saída antes da entrada é recusada");
        Reserva semNome = nova(Servico.HOSPEDAGEM, hoje.plusDays(1), hoje.plusDays(2));
        semNome = new Reserva(0, semNome.servico(), semNome.status(), " ", semNome.contato(), "", "Rex", "", "", "", "", "",
                false, "", "", semNome.entrada(), semNome.saida(), null, null, null);
        ok(!svc.validar(semNome).isEmpty(), "tutor sem nome é recusado");
        Reserva medSemDetalhe = nova(Servico.CRECHE, hoje.plusDays(1), hoje.plusDays(1));
        medSemDetalhe = new Reserva(0, medSemDetalhe.servico(), medSemDetalhe.status(), "Ana", "(21) 99999-1111", "", "Rex",
                "", "", "", "", "", true, "", "", medSemDetalhe.entrada(), medSemDetalhe.entrada(), null, null, null);
        ok(!svc.validar(medSemDetalhe).isEmpty(), "medicação 'sim' exige detalhe");

        System.out.println("Vagas");
        Reserva a = svc.salvar(nova(Servico.HOSPEDAGEM, hoje.plusDays(1), hoje.plusDays(4)));
        Reserva b = svc.salvar(nova(Servico.HOSPEDAGEM, hoje.plusDays(2), hoje.plusDays(5)));
        ok(a.id() == 1 && b.id() == 2, "ids sequenciais");
        ok(svc.ocupacao(hoje.plusDays(2), Servico.HOSPEDAGEM, 0) == 2, "duas noites ocupadas em D+2");
        ok(svc.ocupacao(hoje.plusDays(4), Servico.HOSPEDAGEM, 0) == 1, "saída não ocupa a noite do dia de saída");
        try {
            svc.salvar(nova(Servico.HOSPEDAGEM, hoje.plusDays(3), hoje.plusDays(5)));
            ok(false, "terceira hospedagem deveria ser recusada (lotado)");
        } catch (ValidacaoException e) {
            ok(e.erros().get(0).contains("Sem vagas"), "terceira hospedagem recusada por falta de vaga");
        }
        ok(svc.problemasDeVagas(Servico.HOSPEDAGEM, hoje.plusDays(5), hoje.plusDays(7), 0).isEmpty(),
                "dá para reservar a partir da noite em que alguém sai");
        svc.salvar(nova(Servico.CRECHE, hoje.plusDays(2), hoje.plusDays(2)));
        ok(!svc.problemasDeVagas(Servico.CRECHE, hoje.plusDays(2), hoje.plusDays(2), 0).isEmpty(), "creche lotada no dia");
        ok(svc.problemasDeVagas(Servico.HOSPEDAGEM, hoje.plusDays(1), hoje.plusDays(4), a.id()).isEmpty(),
                "editar a mesma reserva não conta contra si mesma");

        System.out.println("Cancelamento libera vaga");
        svc.cancelar(b.id());
        ok(svc.ocupacao(hoje.plusDays(2), Servico.HOSPEDAGEM, 0) == 1, "vaga liberada após cancelar");
        ok("Cancelada".equals(svc.situacao(svc.todas().get(1))), "situação = Cancelada");

        System.out.println("Agenda fechada");
        cfg.setAgendaFechadaAte(hoje.plusDays(20));
        ok(!svc.problemasDeVagas(Servico.CRECHE, hoje.plusDays(10), hoje.plusDays(10), 0).isEmpty(), "reserva nova em período fechado é recusada");
        ok(svc.problemasDeVagas(Servico.CRECHE, hoje.plusDays(21), hoje.plusDays(21), 0).isEmpty(), "dia seguinte ao fechamento está aberto");

        System.out.println("Busca");
        ok(svc.buscar("ana", null, null).size() == 3, "busca por tutor (sem acento/maiúscula)");
        ok(svc.buscar("rex", Status.CANCELADA, null).size() == 1, "filtro por status");
        ok(svc.buscar("99999", null, Servico.CRECHE).size() == 1, "busca por telefone + serviço");

        System.out.println("Arquivo");
        Path tmp = Files.createTempDirectory("clubinho-teste");
        Path arq = tmp.resolve("reservas.tsv");
        ReservaRepository r1 = ReservaRepository.doArquivo(arq);
        Reserva texto = new Reserva(0, Servico.HOSPEDAGEM, Status.CONFIRMADA, "João\tda Silva", "(21) 98888-7777", "Rua \\ X",
                "Bob", "SRD", "2", "Não", "linha 1\nlinha 2", "ovo", true, "remédio às 8h", "Brincalhão",
                hoje.plusDays(1), hoje.plusDays(2), LocalTime.of(9, 30), null, LocalDateTime.of(2027, 3, 1, 12, 0));
        Reserva gravada = r1.salvar(texto);
        ReservaRepository r2 = ReservaRepository.doArquivo(arq);
        ok(r2.listar().size() == 1 && r2.listar().get(0).equals(gravada), "grava e relê igual (inclusive TAB, quebra de linha e barra)");
        Reserva outra = r2.salvar(texto);
        ok(outra.id() == gravada.id() + 1, "ids continuam depois de reabrir o arquivo");

        ok(Files.exists(tmp.resolve("reservas.tsv.bak")), "cópia de segurança (.bak) criada ao gravar de novo");

        System.out.println(falhas == 0 ? "\nTODOS OS TESTES PASSARAM" : "\nFALHAS: " + falhas);
        System.exit(falhas == 0 ? 0 : 1);
    }
}
