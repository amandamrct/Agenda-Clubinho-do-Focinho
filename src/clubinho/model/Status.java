package clubinho.model;

/** Situação gravada da reserva. (Concluída / Em andamento são calculadas pela data.) */
public enum Status {
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada");

    private final String rotulo;

    Status(String rotulo) { this.rotulo = rotulo; }

    public String rotulo() { return rotulo; }
}
