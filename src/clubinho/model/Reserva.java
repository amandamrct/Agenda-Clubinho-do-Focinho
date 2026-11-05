package clubinho.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Uma reserva (hospedagem ou creche). Os campos seguem a ficha de reserva em PDF.
 * Para creche, a saída é sempre igual à entrada (o cão volta para casa no mesmo dia).
 */
public record Reserva(
        int id,
        Servico servico,
        Status status,
        String tutorNome,
        String contato,
        String endereco,
        String petNome,
        String raca,
        String idade,
        String castrado,          // "Sim", "Não" ou "" (não informado)
        String alimentacao,
        String outrosAlimentos,
        boolean medicacao,
        String medicacaoDetalhe,
        String comportamento,
        LocalDate entrada,
        LocalDate saida,
        LocalTime checkIn,
        LocalTime checkOut,
        LocalDateTime criadaEm) {

    public Reserva comId(int novoId) {
        return new Reserva(novoId, servico, status, tutorNome, contato, endereco, petNome, raca, idade, castrado,
                alimentacao, outrosAlimentos, medicacao, medicacaoDetalhe, comportamento, entrada, saida,
                checkIn, checkOut, criadaEm);
    }

    public Reserva comStatus(Status novo) {
        return new Reserva(id, servico, novo, tutorNome, contato, endereco, petNome, raca, idade, castrado,
                alimentacao, outrosAlimentos, medicacao, medicacaoDetalhe, comportamento, entrada, saida,
                checkIn, checkOut, criadaEm);
    }

    public boolean confirmada() { return status == Status.CONFIRMADA; }

    /** Data em que o pet vai embora (creche: o mesmo dia da entrada). */
    public LocalDate saidaEfetiva() { return saida == null ? entrada : saida; }

    /** Número de noites (só hospedagem). */
    public long noites() {
        return servico == Servico.HOSPEDAGEM && saida != null ? ChronoUnit.DAYS.between(entrada, saida) : 0;
    }

    /**
     * Ocupa vaga no dia informado? Hospedagem ocupa a vaga em cada NOITE (entrada inclusive, saída exclusive);
     * creche ocupa a vaga apenas no dia da entrada.
     */
    public boolean ocupaVagaEm(LocalDate dia) {
        if (!confirmada() || entrada == null) return false;
        if (servico == Servico.HOSPEDAGEM) {
            return saida != null && !dia.isBefore(entrada) && dia.isBefore(saida);
        }
        return dia.equals(entrada);
    }

    /** O pet está fisicamente no hotel nesse dia? (hospedagem, do check-in até o dia do check-out) */
    public boolean hospedadoEm(LocalDate dia) {
        return confirmada() && servico == Servico.HOSPEDAGEM && saida != null
                && !dia.isBefore(entrada) && !dia.isAfter(saida);
    }
}
