package clubinho;

import clubinho.data.Config;
import clubinho.data.DemoData;
import clubinho.data.ReservaRepository;
import clubinho.service.ReservaService;
import clubinho.ui.MainFrame;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Sistema de reservas do Clubinho do Focinho.
 *   java -jar ClubinhoReservas.jar          -> usa a pasta C:\Users\<usuário>\ClubinhoDoFocinho\dados
 *   java -jar ClubinhoReservas.jar --demo   -> abre com reservas de exemplo, sem gravar nada
 */
public class App {
    /** Pasta fixa na pasta do usuário: os dados ficam no mesmo lugar não importa de onde o programa for aberto. */
    static String pastaPadrao() {
        return Path.of(System.getProperty("user.home"), "ClubinhoDoFocinho", "dados").toString();
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        boolean demo = Arrays.asList(args).contains("--demo");
        ReservaRepository repo;
        Config config;
        try {
            if (demo) {
                repo = ReservaRepository.emMemoria();
                config = Config.emMemoria();
                DemoData.carregar(repo, LocalDate.now());
            } else {
                Path dir = Path.of(System.getProperty("clubinho.dados", pastaPadrao()));
                repo = ReservaRepository.doArquivo(dir.resolve("reservas.tsv"));
                config = Config.carregar(dir.resolve("config.properties"));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Não foi possível abrir os dados:\n" + e.getMessage(),
                    "Clubinho do Focinho", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ReservaService service = new ReservaService(repo, config);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }
            new MainFrame(service).setVisible(true);
        });
    }
}
