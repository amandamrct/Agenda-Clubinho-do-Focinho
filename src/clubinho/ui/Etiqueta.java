package clubinho.ui;

import javax.swing.*;
import java.awt.*;

/** Selinho de status (Confirmada, Cancelada...), em formato de pílula. */
public class Etiqueta extends JComponent {
    private final String texto;
    private final Color fundo;
    private final Color letra;

    public Etiqueta(String texto, Color fundo, Color letra) {
        this.texto = texto;
        this.fundo = fundo;
        this.letra = letra;
        setOpaque(false);
    }

    @Override public Dimension getPreferredSize() {
        return new Dimension(getFontMetrics(Theme.bold(12)).stringWidth(texto) + 24, 26);
    }

    @Override public Dimension getMinimumSize() { return getPreferredSize(); }
    @Override public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.suavizar(g2);
        g2.setColor(fundo);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.setFont(Theme.bold(12));
        g2.setColor(letra);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, (getWidth() - fm.stringWidth(texto)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }

    /** Cria a etiqueta certa para a situação mostrada na tela. */
    public static Etiqueta paraSituacao(String situacao) {
        return switch (situacao) {
            case "Cancelada" -> new Etiqueta(situacao, new Color(0xF3DDD6), Theme.DANGER);
            case "Concluída" -> new Etiqueta(situacao, new Color(0xE3EFEA), Theme.TEAL_DARK);
            case "Em andamento" -> new Etiqueta(situacao, Theme.TEAL, Theme.WHITE);
            default -> new Etiqueta(situacao, Theme.CREAM2, Theme.TEAL_INK);
        };
    }
}
