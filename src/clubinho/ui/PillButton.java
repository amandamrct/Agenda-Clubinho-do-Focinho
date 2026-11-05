package clubinho.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Botão em formato de pílula, nas cores da marca. */
public class PillButton extends JButton {
    public enum Tipo { PRIMARIO, SECUNDARIO, CONTORNO, PERIGO }

    private final Tipo tipo;
    private final boolean pequeno;
    private boolean sobre;
    private boolean pressionado;

    public PillButton(String texto, Tipo tipo) { this(texto, tipo, false); }

    public PillButton(String texto, Tipo tipo, boolean pequeno) {
        super(texto);
        this.tipo = tipo;
        this.pequeno = pequeno;
        setFont(Theme.bold(pequeno ? 12 : 14));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { sobre = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { sobre = false; pressionado = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { pressionado = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e) { pressionado = false; repaint(); }
        });
    }

    @Override public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + (pequeno ? 30 : 46), pequeno ? 32 : 42);
    }

    @Override public Dimension getMinimumSize() { return getPreferredSize(); }
    @Override public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.suavizar(g2);
        int w = getWidth(), h = getHeight();
        Color fundo, texto, borda = null;
        switch (tipo) {
            case PRIMARIO -> { fundo = pressionado ? Theme.TEAL_INK : sobre ? Theme.TEAL_DARK : Theme.TEAL; texto = Theme.WHITE; }
            case SECUNDARIO -> { fundo = pressionado ? Theme.CREAM2 : sobre ? new Color(0xEBCB98) : Theme.TAN; texto = Theme.TEAL_INK; }
            case PERIGO -> { fundo = sobre ? new Color(0xF6E1DB) : null; texto = Theme.DANGER; borda = Theme.DANGER; }
            default -> { fundo = sobre ? Theme.CREAM2 : null; texto = Theme.TEAL_DARK; borda = Theme.TEAL; }
        }
        if (!isEnabled()) {
            fundo = fundo == null ? null : Theme.comAlpha(fundo, 120);
            texto = Theme.comAlpha(texto, 130);
            if (borda != null) borda = Theme.comAlpha(borda, 120);
        }
        if (fundo != null) {
            g2.setColor(fundo);
            g2.fillRoundRect(0, 0, w, h, h, h);
        }
        if (borda != null) {
            g2.setColor(borda);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, h, h);
        }
        g2.setFont(getFont());
        g2.setColor(texto);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
