package clubinho.ui;

import clubinho.model.Reserva;
import clubinho.service.ReservaService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** Janela principal: menu lateral + telas (Painel, Nova reserva, Reservas, Agenda). */
public class AppPanel extends JPanel {
    private final CardLayout cartas = new CardLayout();
    private final JPanel conteudo = new JPanel(cartas);
    private final List<ItemMenu> menu = new ArrayList<>();
    private final DashboardPanel painel;
    private final NovaReservaPanel nova;
    private final ReservasPanel reservas;
    private final AgendaPanel agenda;
    private String telaAtual = "painel";

    public AppPanel(ReservaService service) {
        super(new BorderLayout());
        setBackground(Theme.CREAM);

        painel = new DashboardPanel(service, () -> abrirNova());
        nova = new NovaReservaPanel(service, this::reservaSalva, () -> mostrar("reservas"));
        reservas = new ReservasPanel(service, r -> { nova.carregar(r); mostrar("nova"); });
        agenda = new AgendaPanel(service);

        conteudo.setOpaque(false);
        conteudo.setBorder(BorderFactory.createEmptyBorder(28, 34, 20, 30));
        conteudo.add(painel, "painel");
        conteudo.add(nova, "nova");
        conteudo.add(reservas, "reservas");
        conteudo.add(agenda, "agenda");

        add(menuLateral(), BorderLayout.WEST);
        add(conteudo, BorderLayout.CENTER);
        mostrar("painel");
    }

    private void abrirNova() {
        if (!telaAtual.equals("nova") && nova.estaEditando()) nova.limpar();
        mostrar("nova");
    }

    private void reservaSalva(Reserva r) {
        Dialogs.aviso(this, "Reserva salva",
                "A reserva de " + r.petNome() + " (" + r.servico().rotulo().toLowerCase() + ") foi registrada.");
        mostrar("reservas");
    }

    public void mostrar(String tela) {
        telaAtual = tela;
        switch (tela) {
            case "painel" -> painel.atualizar();
            case "reservas" -> reservas.atualizar();
            case "agenda" -> agenda.atualizar();
            default -> { }
        }
        cartas.show(conteudo, tela);
        for (ItemMenu i : menu) i.setAtivo(i.tela.equals(tela));
    }

    // ------------------------------------------------------------------ menu lateral

    private JComponent menuLateral() {
        JPanel lateral = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.suavizar(g2);
                g2.setColor(Theme.TEAL_INK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.comAlpha(Theme.TEAL, 70));
                g2.fillOval(-120, getHeight() - 260, 340, 340);
                Icons.patinha(g2, getWidth() - 72, getHeight() - 150, 34, Theme.comAlpha(Theme.CREAM, 28));
                Icons.patinha(g2, 24, getHeight() - 118, 26, Theme.comAlpha(Theme.CREAM, 22));
                g2.dispose();
            }
        };
        lateral.setLayout(new BorderLayout());
        lateral.setPreferredSize(new Dimension(252, 100));
        lateral.setBorder(BorderFactory.createEmptyBorder(30, 20, 24, 20));

        JPanel topo = new JPanel();
        topo.setOpaque(false);
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        LogoSelo logo = new LogoSelo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        topo.add(logo);
        topo.add(Box.createVerticalStrut(14));
        topo.add(centralizado(Ui.rotulo("Clubinho do Focinho", Theme.bold(17), Theme.CREAM)));
        topo.add(Box.createVerticalStrut(2));
        topo.add(centralizado(Ui.rotulo("creche e hospedagem", Theme.italic(13), Theme.TAN)));
        topo.add(Box.createVerticalStrut(28));

        String[][] itens = {{"painel", "Painel"}, {"nova", "Nova reserva"}, {"reservas", "Reservas"}, {"agenda", "Agenda"}};
        for (int i = 0; i < itens.length; i++) {
            ItemMenu m = new ItemMenu(itens[i][0], itens[i][1], i);
            m.addActionListener(e -> {
                if (m.tela.equals("nova")) abrirNova(); else mostrar(m.tela);
            });
            menu.add(m);
            m.setAlignmentX(Component.CENTER_ALIGNMENT);
            topo.add(m);
            topo.add(Box.createVerticalStrut(6));
        }
        lateral.add(topo, BorderLayout.NORTH);

        JPanel base = new JPanel();
        base.setOpaque(false);
        base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
        base.add(centralizado(Ui.rotulo("Niterói - RJ", Theme.medium(12), Theme.CREAM)));
        base.add(centralizado(Ui.rotulo("@clubinhodofocinho", Theme.regular(12), Theme.TAN)));
        lateral.add(base, BorderLayout.SOUTH);
        return lateral;
    }

    private static JComponent centralizado(JLabel l) {
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    /** Logo redondo com anel creme. */
    private static class LogoSelo extends JComponent {
        private final Image img = Theme.logo(128);

        LogoSelo() {
            setPreferredSize(new Dimension(132, 132));
            setMaximumSize(new Dimension(132, 132));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.suavizar(g2);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setColor(Theme.CREAM);
            g2.fillOval(0, 0, 131, 131);
            Shape antes = g2.getClip();
            g2.setClip(new java.awt.geom.Ellipse2D.Double(5, 5, 121, 121));
            g2.drawImage(img, 3, 3, 126, 126, null);
            g2.setClip(antes);
            g2.dispose();
        }
    }

    private static class ItemMenu extends JButton {
        final String tela;
        private final String texto;
        private final int icone;
        private boolean ativo, sobre;

        ItemMenu(String tela, String texto, int icone) {
            this.tela = tela;
            this.texto = texto;
            this.icone = icone;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(212, 46));
            setMaximumSize(new Dimension(212, 46));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { sobre = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { sobre = false; repaint(); }
            });
        }

        void setAtivo(boolean a) { this.ativo = a; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.suavizar(g2);
            int w = getWidth(), h = getHeight();
            if (ativo) {
                g2.setColor(Theme.TAN);
                g2.fillRoundRect(0, 0, w, h, h, h);
            } else if (sobre) {
                g2.setColor(Theme.comAlpha(Theme.TEAL, 90));
                g2.fillRoundRect(0, 0, w, h, h, h);
            }
            Color cor = ativo ? Theme.TEAL_INK : Theme.CREAM;
            double ix = 20, iy = (h - 22) / 2.0, s = 22;
            switch (icone) {
                case 0 -> Icons.painel(g2, ix, iy, s, cor);
                case 1 -> Icons.mais(g2, ix, iy, s, cor);
                case 2 -> Icons.lista(g2, ix, iy, s, cor);
                default -> Icons.calendario(g2, ix, iy, s, cor);
            }
            g2.setFont(ativo ? Theme.bold(15) : Theme.medium(15));
            g2.setColor(cor);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, 56, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
