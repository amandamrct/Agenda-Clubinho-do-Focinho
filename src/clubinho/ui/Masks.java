package clubinho.ui;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.function.Function;

/** Máscaras de digitação: data (dd/mm/aaaa), hora (hh:mm), telefone e só-números. */
public final class Masks {
    private Masks() {}

    public static void data(JTextField f) {
        aplicar(f, 8, d -> d.length() > 4 ? d.substring(0, 2) + "/" + d.substring(2, 4) + "/" + d.substring(4)
                : d.length() > 2 ? d.substring(0, 2) + "/" + d.substring(2) : d);
    }

    public static void hora(JTextField f) {
        aplicar(f, 4, d -> d.length() > 2 ? d.substring(0, 2) + ":" + d.substring(2) : d);
    }

    public static void telefone(JTextField f) {
        aplicar(f, 11, d -> {
            if (d.isEmpty()) return "";
            if (d.length() <= 2) return "(" + d;
            if (d.length() <= 6) return "(" + d.substring(0, 2) + ") " + d.substring(2);
            if (d.length() <= 10) return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
            return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        });
    }

    public static void numero(JTextField f, int maxDigitos) { aplicar(f, maxDigitos, d -> d); }

    private static void aplicar(JTextField f, int max, Function<String, String> formato) {
        ((AbstractDocument) f.getDocument()).setDocumentFilter(new Filtro(max, formato));
    }

    private static class Filtro extends DocumentFilter {
        private final int max;
        private final Function<String, String> formato;

        Filtro(int max, Function<String, String> formato) { this.max = max; this.formato = formato; }

        @Override public void insertString(FilterBypass fb, int offset, String s, javax.swing.text.AttributeSet a)
                throws BadLocationException { replace(fb, offset, 0, s, a); }

        @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            String trecho = atual.substring(offset, offset + length);
            // apagar só um símbolo (/ : - parênteses) apaga o número que vem antes dele
            if (trecho.replaceAll("\\D", "").isEmpty()) {
                int o = offset - 1;
                while (o >= 0 && !Character.isDigit(atual.charAt(o))) o--;
                if (o >= 0) {
                    escrever(fb, atual.substring(0, o) + atual.substring(offset + length));
                    return;
                }
            }
            escrever(fb, atual.substring(0, offset) + atual.substring(offset + length));
        }

        @Override public void replace(FilterBypass fb, int offset, int length, String texto,
                                      javax.swing.text.AttributeSet a) throws BadLocationException {
            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            escrever(fb, atual.substring(0, offset) + (texto == null ? "" : texto) + atual.substring(offset + length));
        }

        private void escrever(FilterBypass fb, String novo) throws BadLocationException {
            String digitos = novo.replaceAll("\\D", "");
            if (digitos.length() > max) digitos = digitos.substring(0, max);
            fb.replace(0, fb.getDocument().getLength(), formato.apply(digitos), null);
        }
    }
}
