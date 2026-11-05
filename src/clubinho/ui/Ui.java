package clubinho.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/** Pequenos ajudantes de interface. */
public final class Ui {
    private Ui() {}

    /** Área de rolagem sem bordas, com barra fina nas cores da marca. */
    public static JScrollPane rolagem(Component conteudo) {
        JScrollPane sp = new JScrollPane(conteudo, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getVerticalScrollBar().setBackground(Theme.CREAM);
        sp.getVerticalScrollBar().setUI(new BarraFina());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        sp.getVerticalScrollBar().setUnitIncrement(24);
        sp.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, new JPanel() {{ setOpaque(false); }});
        return sp;
    }

    public static JLabel rotulo(String texto, Font fonte, Color cor) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte);
        l.setForeground(cor);
        return l;
    }

    /** Título de seção com um traço bege embaixo. */
    public static JComponent tituloSecao(String texto) {
        JPanel p = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.suavizar(g2);
                g2.setColor(Theme.TAN);
                g2.fillRoundRect(0, getHeight() - 4, 44, 4, 4, 4);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        p.add(rotulo(texto, Theme.bold(17), Theme.TEAL_DARK), BorderLayout.CENTER);
        return p;
    }

    static class BarraFina extends BasicScrollBarUI {
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private static JButton zero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.suavizar(g2);
            g2.setColor(isThumbRollover() ? Theme.TEAL : Theme.TAN);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }
}
