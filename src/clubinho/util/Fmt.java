package clubinho.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.util.Locale;

/** Formatação e leitura de datas/horas no padrão brasileiro. */
public final class Fmt {
    public static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private Fmt() {}

    public static String data(LocalDate d) { return d == null ? "" : DATA.format(d); }

    public static String hora(LocalTime t) { return t == null ? "" : HORA.format(t); }

    /** Lê dd/mm/aaaa. Devolve null se estiver vazio ou inválido. */
    public static LocalDate parseData(String s) {
        try { return LocalDate.parse(s.trim(), DATA); } catch (Exception e) { return null; }
    }

    /** Lê hh:mm. Devolve null se estiver vazio ou inválido. */
    public static LocalTime parseHora(String s) {
        try { return LocalTime.parse(s.trim(), HORA); } catch (Exception e) { return null; }
    }

    public static String mes(YearMonth m) {
        return capitalizar(m.getMonth().getDisplayName(TextStyle.FULL, PT_BR)) + " " + m.getYear();
    }

    public static String dataExtensa(LocalDate d) {
        return capitalizar(d.getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR)) + ", "
                + d.getDayOfMonth() + " de " + d.getMonth().getDisplayName(TextStyle.FULL, PT_BR)
                + " de " + d.getYear();
    }

    public static String capitalizar(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static boolean vazio(String s) { return s == null || s.isBlank(); }
}
