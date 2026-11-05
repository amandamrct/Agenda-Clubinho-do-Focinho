package clubinho.ui;

import javax.swing.*;
import java.awt.*;

/** Botão de escolha (tipo "etiqueta") usado no lugar de caixas de seleção. */
public class Chip extends JToggleButton {
    private final String rotulo;

    public Chip(String rotulo) {
        super(rotulo);
        this.rotulo = rotulo;
        setFont(Theme.medium(13));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public String rotulo() { return rotulo; }

    @Override public Dimension getPreferredSize() {
        return new Dimension(getFontMetrics(Theme.bold(13)).stringWidth(rotulo) + 36, 36);
    }

    @Override public Dimension getMinimumSize() { return getPreferredSize(); }
    @Override public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.suavizar(g2);
        int w = getWidth(), h = getHeight();
        boolean sel = isSelected();
        boolean sobre = getModel().isRollover();
        if (sel) {
            g2.setColor(Theme.TEAL);
            g2.fillRoundRect(0, 0, w, h, h, h);
        } else {
            g2.setColor(sobre ? Theme.CREAM2 : Theme.WHITE);
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(Theme.FIELD_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, h, h);
        }
        g2.setFont(sel ? Theme.bold(13) : Theme.medium(13));
        g2.setColor(sel ? Theme.WHITE : Theme.TEAL_DARK);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(rotulo, (w - fm.stringWidth(rotulo)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
