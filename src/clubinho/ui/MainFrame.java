package clubinho.ui;

import clubinho.service.ReservaService;

import javax.swing.*;
import java.awt.*;

/** Janela do aplicativo. */
public class MainFrame extends JFrame {
    public MainFrame(ReservaService service) {
        super("Clubinho do Focinho — Reservas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImages(Theme.icones());
        setContentPane(new AppPanel(service));
        setMinimumSize(new Dimension(1100, 700));
        setSize(1320, 840);
        setLocationRelativeTo(null);
    }
}
