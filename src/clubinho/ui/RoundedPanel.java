package clubinho.ui;

import javax.swing.*;
import java.awt.*;

/** Painel com cantos arredondados, sombra suave e borda (contínua ou tracejada) opcionais. */
public class RoundedPanel extends JPanel {
    public static final int SOMBRA = 5;

    private Color fundo;
    private final int arco;
    private final boolean sombra;
    private Color borda;
    private boolean tracejada;

    public RoundedPanel(Color fundo, int arco, boolean sombra) {
        this.fundo = fundo;
        this.arco = arco;
        this.sombra = sombra;
        setOpaque(false);
    }

    public RoundedPanel borda(Color cor, boolean tracejada) {
        this.borda = cor;
        this.tracejada = tracejada;
        repaint();
        return this;
    }

    /** Define o espaço interno (a sombra é somada embaixo automaticamente). */
    public RoundedPanel espaco(int cima, int esquerda, int baixo, int direita) {
        setBorder(BorderFactory.createEmptyBorder(cima, esquerda, baixo + (sombra ? SOMBRA : 0), direita));
        return this;
    }

    public void setFundo(Color c) { this.fundo = c; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.suavizar(g2);
        int w = getWidth();
        int h = getHeight() - (sombra ? SOMBRA : 0);
        if (sombra) {
            for (int i = 0; i < SOMBRA; i++) {
                g2.setColor(new Color(10, 62, 59, 13));
                g2.fillRoundRect(i / 2, i + 1, w - i, h, arco, arco);
            }
        }
        g2.setColor(fundo);
        g2.fillRoundRect(0, 0, w, h, arco, arco);
        if (borda != null) {
            g2.setColor(borda);
            g2.setStroke(tracejada
                    ? new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{7f, 6f}, 0f)
                    : new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arco, arco);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
