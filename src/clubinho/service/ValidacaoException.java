package clubinho.service;

import java.util.List;

/** Lançada quando a reserva não passa nas regras do hotel; carrega as mensagens para mostrar na tela. */
public class ValidacaoException extends Exception {
    private final List<String> erros;

    public ValidacaoException(List<String> erros) {
        super(String.join("; ", erros));
        this.erros = List.copyOf(erros);
    }

    public List<String> erros() { return erros; }
}
