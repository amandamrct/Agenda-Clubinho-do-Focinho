package clubinho.model;

/** Tipo de serviço oferecido pelo Clubinho do Focinho. */
public enum Servico {
    HOSPEDAGEM("Hospedagem"),
    CRECHE("Creche");

    private final String rotulo;

    Servico(String rotulo) { this.rotulo = rotulo; }

    public String rotulo() { return rotulo; }
}
