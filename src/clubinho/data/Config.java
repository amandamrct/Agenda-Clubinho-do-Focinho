package clubinho.data;

import clubinho.util.Fmt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Properties;

/** Configurações do hotel: vagas por dia e (opcionalmente) até quando a agenda está fechada. */
public class Config {
    public static final int VAGAS_HOSPEDAGEM_PADRAO = 10;
    public static final int VAGAS_CRECHE_PADRAO = 15;

    private final Path arquivo;          // null = só em memória
    private int vagasHospedagem = VAGAS_HOSPEDAGEM_PADRAO;
    private int vagasCreche = VAGAS_CRECHE_PADRAO;
    private LocalDate agendaFechadaAte;

    private Config(Path arquivo) { this.arquivo = arquivo; }

    public static Config emMemoria() { return new Config(null); }

    public static Config carregar(Path arquivo) throws IOException {
        Config c = new Config(arquivo);
        if (Files.exists(arquivo)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(arquivo)) { p.load(in); }
            c.vagasHospedagem = lerInt(p, "vagasHospedagem", VAGAS_HOSPEDAGEM_PADRAO);
            c.vagasCreche = lerInt(p, "vagasCreche", VAGAS_CRECHE_PADRAO);
            String f = p.getProperty("agendaFechadaAte", "").trim();
            if (!f.isEmpty()) {
                try { c.agendaFechadaAte = LocalDate.parse(f); } catch (Exception ignored) { }
            }
        } else {
            c.salvar();   // cria o arquivo com os valores padrão para a pessoa poder ver/editar
        }
        return c;
    }

    private static int lerInt(Properties p, String chave, int padrao) {
        try { return Math.max(1, Integer.parseInt(p.getProperty(chave, "").trim())); }
        catch (NumberFormatException e) { return padrao; }
    }

    public void salvar() throws IOException {
        if (arquivo == null) return;
        Files.createDirectories(arquivo.toAbsolutePath().getParent());
        Properties p = new Properties();
        p.setProperty("vagasHospedagem", String.valueOf(vagasHospedagem));
        p.setProperty("vagasCreche", String.valueOf(vagasCreche));
        p.setProperty("agendaFechadaAte", agendaFechadaAte == null ? "" : agendaFechadaAte.toString());
        try (OutputStream out = Files.newOutputStream(arquivo)) {
            p.store(out, "Clubinho do Focinho - configuracoes (agendaFechadaAte no formato aaaa-mm-dd)");
        }
    }

    /** Pasta onde os dados ficam gravados (null no modo demonstração). */
    public Path pastaDados() { return arquivo == null ? null : arquivo.toAbsolutePath().getParent(); }

    public int vagasHospedagem() { return vagasHospedagem; }
    public int vagasCreche() { return vagasCreche; }
    public LocalDate agendaFechadaAte() { return agendaFechadaAte; }

    public void setVagasHospedagem(int v) { this.vagasHospedagem = Math.max(1, v); }
    public void setVagasCreche(int v) { this.vagasCreche = Math.max(1, v); }
    public void setAgendaFechadaAte(LocalDate d) { this.agendaFechadaAte = d; }

    @Override public String toString() {
        return "Config[hospedagem=" + vagasHospedagem + ", creche=" + vagasCreche
                + ", fechadaAte=" + Fmt.data(agendaFechadaAte) + "]";
    }
}
