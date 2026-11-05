package clubinho.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Caixa de texto de várias linhas, arredondada, com dica que some ao digitar. */
public class HintArea extends JPanel {
    private final JTextArea area;

    public HintArea(String dica, int linhas) {
        super(new BorderLayout());
        setOpaque(false);
        area = new JTextArea(linhas, 20) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && dica != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    Theme.suavizar(g2);
                    g2.setFont(Theme.italic(13));
                    g2.setColor(Theme.comAlpha(Theme.MUTED, 190));
                    Insets in = getInsets();
                    g2.drawString(dica, in.left, in.top + g2.getFontMetrics().getAscent() + 1);
                    g2.dispose();
                }
            }
        };
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(Theme.regular(14));
        area.setForeground(Theme.TEAL_INK);
        area.setCaretColor(Theme.TEAL_INK);
        area.setSelectionColor(Theme.CREAM2);
        area.setSelectedTextColor(Theme.TEAL_INK);
        area.setBorder(new EmptyBorder(11, 14, 11, 14));
        area.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); }
        });
        add(area, BorderLayout.CENTER);
    }

    public String getText() { return area.getText(); }
    public void setText(String t) { area.setText(t); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.suavizar(g2);
        int w = getWidth(), h = getHeight();
        g2.setColor(Theme.FIELD_BG);
        g2.fillRoundRect(0, 0, w, h, 16, 16);
        boolean foco = area.hasFocus();
        g2.setColor(foco ? Theme.TEAL : Theme.FIELD_BORDER);
        g2.setStroke(new BasicStroke(foco ? 2f : 1.5f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }
}
