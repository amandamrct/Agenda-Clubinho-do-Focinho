package clubinho.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Campo de texto arredondado com dica (placeholder) que some assim que a pessoa digita. */
public class HintField extends JTextField {
    private final String dica;

    public HintField(String dica) {
        this.dica = dica;
        setOpaque(false);
        setFont(Theme.regular(14));
        setForeground(Theme.TEAL_INK);
        setCaretColor(Theme.TEAL_INK);
        setSelectionColor(Theme.CREAM2);
        setSelectedTextColor(Theme.TEAL_INK);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); }
        });
    }

    @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, 44);
    }

    @Override public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 44);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.suavizar(g2);
        int w = getWidth(), h = getHeight();
        boolean ativo = isEnabled();
        g2.setColor(ativo ? Theme.FIELD_BG : Theme.comAlpha(Theme.FIELD_BG, 110));
        g2.fillRoundRect(0, 0, w, h, 16, 16);
        boolean foco = hasFocus();
        g2.setColor(foco ? Theme.TEAL : ativo ? Theme.FIELD_BORDER : Theme.comAlpha(Theme.FIELD_BORDER, 120));
        g2.setStroke(new BasicStroke(foco ? 2f : 1.5f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, 16, 16);
        g2.dispose();
        setForeground(ativo ? Theme.TEAL_INK : Theme.MUTED);
        super.paintComponent(g);
        if (getText().isEmpty() && dica != null && !dica.isEmpty()) {
            Graphics2D g3 = (Graphics2D) g.create();
            Theme.suavizar(g3);
            g3.setFont(Theme.italic(13));
            g3.setColor(Theme.comAlpha(Theme.MUTED, ativo ? 190 : 110));
            Insets in = getInsets();
            FontMetrics fm = g3.getFontMetrics();
            g3.drawString(dica, in.left, (h - fm.getHeight()) / 2 + fm.getAscent());
            g3.dispose();
        }
    }
}
