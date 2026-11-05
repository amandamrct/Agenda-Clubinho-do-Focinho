package clubinho.ui;

import javax.swing.*;
import java.awt.*;

/** Pilha vertical de componentes (cada um ocupa a largura toda). Rola bem dentro de um JScrollPane. */
public class Stack extends JPanel implements Scrollable {
    private final int espaco;
    private int linha = 0;

    public Stack(int espaco) {
        super(new GridBagLayout());
        this.espaco = espaco;
        setOpaque(false);
    }

    public Stack empilhar(Component c) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = linha++;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, espaco, 0);
        add(c, gc);
        return this;
    }

    /** Empurra tudo para cima (chame depois do último empilhar). */
    public Stack fechar() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = linha++;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.VERTICAL;
        add(Box.createGlue(), gc);
        return this;
    }

    public void limpar() { removeAll(); linha = 0; revalidate(); repaint(); }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 28; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(80, r.height - 60); }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
