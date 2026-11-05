package clubinho.ui;

import javax.swing.*;
import java.awt.*;

/** Cabeçalho das telas: título grande, subtítulo e (opcional) um componente à direita. */
public class Cabecalho extends JPanel {
    private final JLabel titulo = Ui.rotulo("", Theme.bold(28), Theme.TEAL_INK);
    private final JLabel subtitulo = Ui.rotulo("", Theme.regular(14), Theme.MUTED);

    public Cabecalho(String t, String s, JComponent direita) {
        super(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        titulo.setText(t);
        subtitulo.setText(s);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);
        add(textos, BorderLayout.CENTER);
        if (direita != null) {
            JPanel lado = new JPanel(new GridBagLayout());
            lado.setOpaque(false);
            lado.add(direita);
            add(lado, BorderLayout.EAST);
        }
    }

    public void setTitulo(String t) { titulo.setText(t); }
    public void setSubtitulo(String s) { subtitulo.setText(s); }
}
