package clubinho.ui;

import javax.swing.*;
import java.awt.*;

/** Rótulo em negrito em cima + campo embaixo. */
public class FormField extends JPanel {
    private final JLabel rotulo = new JLabel();

    public FormField(String texto, JComponent campo) {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        rotulo.setText(texto);
        rotulo.setFont(Theme.bold(12));
        rotulo.setForeground(Theme.TEAL_INK);
        add(rotulo, BorderLayout.NORTH);
        add(campo, BorderLayout.CENTER);
    }

    public void setRotuloAtivo(boolean ativo) {
        rotulo.setForeground(ativo ? Theme.TEAL_INK : Theme.comAlpha(Theme.TEAL_INK, 110));
    }
}
