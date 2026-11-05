package clubinho.data;

import clubinho.model.Reserva;
import clubinho.model.Servico;
import clubinho.model.Status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda as reservas em um arquivo de texto simples (TSV: uma reserva por linha, campos separados por TAB).
 * Dá para abrir no Excel/Planilhas. Se o arquivo for null, tudo fica só na memória (modo demonstração).
 */
public class ReservaRepository {
    private static final String CABECALHO = "#clubinho-reservas v1";

    private final Path arquivo;
    private final List<Reserva> itens = new ArrayList<>();
    private int proximoId = 1;

    private ReservaRepository(Path arquivo) { this.arquivo = arquivo; }

    public static ReservaRepository emMemoria() { return new ReservaRepository(null); }

    public static ReservaRepository doArquivo(Path arquivo) throws IOException {
        ReservaRepository r = new ReservaRepository(arquivo);
        if (Files.exists(arquivo)) r.carregar();
        return r;
    }

    public synchronized List<Reserva> listar() { return List.copyOf(itens); }

    public synchronized Optional<Reserva> buscar(int id) {
        return itens.stream().filter(r -> r.id() == id).findFirst();
    }

    /** Insere (id == 0) ou substitui (id existente). Devolve a reserva já com o id definitivo. */
    public synchronized Reserva salvar(Reserva r) throws IOException {
        if (r.id() == 0) {
            r = r.comId(proximoId++);
            itens.add(r);
        } else {
            boolean achou = false;
            for (int i = 0; i < itens.size(); i++) {
                if (itens.get(i).id() == r.id()) { itens.set(i, r); achou = true; break; }
            }
            if (!achou) { itens.add(r); proximoId = Math.max(proximoId, r.id() + 1); }
        }
        persistir();
        return r;
    }

    // ---------------------------------------------------------------- arquivo

    private void carregar() throws IOException {
        for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
            if (linha.isBlank() || linha.startsWith("#")) continue;
            try {
                Reserva r = lerLinha(linha);
                itens.add(r);
                proximoId = Math.max(proximoId, r.id() + 1);
            } catch (RuntimeException e) {
                System.err.println("Linha ignorada (formato inválido): " + e.getMessage());
            }
        }
    }

    private void persistir() throws IOException {
        if (arquivo == null) return;
        Path dir = arquivo.toAbsolutePath().getParent();
        Files.createDirectories(dir);
        List<String> linhas = new ArrayList<>();
        linhas.add(CABECALHO);
        for (Reserva r : itens) linhas.add(paraLinha(r));
        Path tmp = dir.resolve(arquivo.getFileName() + ".tmp");
        Files.write(tmp, linhas, StandardCharsets.UTF_8);
        if (Files.exists(arquivo)) {   // guarda a versão anterior como cópia de segurança
            Files.copy(arquivo, dir.resolve(arquivo.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        }
        Files.move(tmp, arquivo, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String paraLinha(Reserva r) {
        String[] c = {
                String.valueOf(r.id()), r.servico().name(), r.status().name(),
                r.tutorNome(), r.contato(), r.endereco(),
                r.petNome(), r.raca(), r.idade(), r.castrado(),
                r.alimentacao(), r.outrosAlimentos(),
                r.medicacao() ? "1" : "0", r.medicacaoDetalhe(), r.comportamento(),
                str(r.entrada()), str(r.saida()), str(r.checkIn()), str(r.checkOut()), str(r.criadaEm())
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c.length; i++) {
            if (i > 0) sb.append('\t');
            sb.append(esc(c[i]));
        }
        return sb.toString();
    }

    private static Reserva lerLinha(String linha) {
        String[] p = linha.split("\t", -1);
        if (p.length < 20) throw new IllegalArgumentException("esperava 20 campos, veio " + p.length);
        for (int i = 0; i < p.length; i++) p[i] = unesc(p[i]);
        return new Reserva(
                Integer.parseInt(p[0]), Servico.valueOf(p[1]), Status.valueOf(p[2]),
                p[3], p[4], p[5], p[6], p[7], p[8], p[9], p[10], p[11],
                "1".equals(p[12]), p[13], p[14],
                data(p[15]), data(p[16]), hora(p[17]), hora(p[18]),
                p[19].isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(p[19]));
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }
    private static LocalDate data(String s) { return s.isEmpty() ? null : LocalDate.parse(s); }
    private static LocalTime hora(String s) { return s.isEmpty() ? null : LocalTime.parse(s); }

    private static String esc(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> b.append("\\\\");
                case '\t' -> b.append("\\t");
                case '\n' -> b.append("\\n");
                case '\r' -> { }
                default -> b.append(c);
            }
        }
        return b.toString();
    }

    private static String unesc(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                b.append(n == 't' ? '\t' : n == 'n' ? '\n' : n);
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }
}
