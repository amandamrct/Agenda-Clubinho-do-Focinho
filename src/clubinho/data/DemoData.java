package clubinho.data;

import clubinho.model.Reserva;
import clubinho.model.Servico;
import clubinho.model.Status;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Reservas fictícias para conhecer o sistema (java -jar ClubinhoReservas.jar --demo). Nada é gravado em arquivo. */
public final class DemoData {
    private DemoData() {}

    public static void carregar(ReservaRepository repo, LocalDate hoje) throws IOException {
        add(repo, Servico.HOSPEDAGEM, "Marina Costa", "(21) 98877-1122", "Rua das Flores, 120 - Icaraí", "Thor", "Golden retriever", "4 anos", "Sim",
                "7h e 18h, 300 g de ração por refeição", "maçã e cenoura", false, "", "Sociável com cães e pessoas, adora bola.",
                hoje.minusDays(1), hoje.plusDays(2), "10:00", "17:00");
        add(repo, Servico.HOSPEDAGEM, "Rafael Nunes", "(21) 99654-3300", "Av. Roberto Silveira, 45 - Icaraí", "Mel", "Shih tzu", "7 anos", "Sim",
                "8h e 19h, 120 g", "", true, "Colírio às 9h e às 21h", "Calma, mas tem medo de barulho alto.",
                hoje, hoje.plusDays(4), "09:00", "16:00");
        add(repo, Servico.CRECHE, "Camila Duarte", "(21) 97788-5566", "Rua Mem de Sá, 300 - Ingá", "Bolinha", "Spitz alemão", "2 anos", "Não",
                "Almoça às 12h, 80 g", "ovo cozido", false, "", "Brinca bastante, às vezes late para cães maiores.",
                hoje, hoje, "08:00", "18:00");
        add(repo, Servico.CRECHE, "Paulo Henrique", "(21) 98123-4455", "Rua Lopes Trovão, 88 - Icaraí", "Nina", "Vira-lata", "1 ano", "Não",
                "Uma refeição às 13h", "", false, "", "Muito agitada, ótima com outros pets.",
                hoje, hoje, "07:30", "17:30");
        add(repo, Servico.HOSPEDAGEM, "Juliana Prado", "(21) 99001-7788", "Rua Cel. Moreira César, 200 - Icaraí", "Billy", "Beagle", "5 anos", "Sim",
                "7h30 e 18h30, 250 g", "iogurte", false, "", "Come muito rápido; prefere comer separado.",
                hoje.plusDays(1), hoje.plusDays(6), "11:00", "15:00");
        add(repo, Servico.HOSPEDAGEM, "Fernanda Lima", "(21) 98456-9001", "Rua Presidente Backer, 15 - Icaraí", "Luna", "Pinscher", "9 anos", "Sim",
                "8h e 18h, 90 g", "", true, "Remédio de coração às 8h", "Idosa e tranquila, não gosta de cães agitados.",
                hoje.plusDays(3), hoje.plusDays(5), "10:00", "12:00");
        add(repo, Servico.CRECHE, "Bruno Teixeira", "(21) 97345-8899", "Rua Noronha Torrezão, 70 - Santa Rosa", "Pipoca", "Poodle", "3 anos", "Não",
                "Almoço às 12h, 100 g", "", false, "", "Sociável.",
                hoje.plusDays(1), hoje.plusDays(1), "08:30", "17:00");
        add(repo, Servico.HOSPEDAGEM, "Luciana Martins", "(21) 98700-2211", "Av. Quintino Bocaiúva, 500 - São Francisco", "Max", "Labrador", "6 anos", "Sim",
                "7h e 17h, 350 g", "ovo", false, "", "Calmo, convive bem com todos.",
                hoje.plusDays(8), hoje.plusDays(12), "09:30", "14:00");
        add(repo, Servico.HOSPEDAGEM, "Patrícia Rocha", "(21) 99888-4040", "Rua Dr. Paulo César, 33 - Santa Rosa", "Dudu", "Yorkshire", "2 anos", "Não",
                "8h e 18h, 70 g", "", false, "", "Medroso no início, depois solta.",
                hoje.plusDays(10), hoje.plusDays(13), "10:00", "16:00");
        Reserva cancelada = new Reserva(0, Servico.CRECHE, Status.CANCELADA, "André Souza", "(21) 99111-2020", "",
                "Toby", "Pug", "5 anos", "Sim", "", "", false, "", "", hoje.plusDays(2), hoje.plusDays(2),
                LocalTime.of(8, 0), LocalTime.of(17, 0), LocalDateTime.now());
        repo.salvar(cancelada);
    }

    private static void add(ReservaRepository repo, Servico s, String tutor, String contato, String end, String pet,
                            String raca, String idade, String castrado, String alim, String outros, boolean med,
                            String medDet, String comp, LocalDate ent, LocalDate sai, String in, String out) throws IOException {
        repo.salvar(new Reserva(0, s, Status.CONFIRMADA, tutor, contato, end, pet, raca, idade, castrado, alim, outros,
                med, medDet, comp, ent, sai, LocalTime.parse(in), LocalTime.parse(out), LocalDateTime.now()));
    }
}
