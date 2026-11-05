package clubinho.ui;

import clubinho.model.Reserva;
import clubinho.model.Servico;
import clubinho.service.ReservaService;
import clubinho.util.Fmt;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Tela inicial: números de hoje e próximas chegadas/saídas. */
public class DashboardPanel extends JPanel {
    private final ReservaService service;
    private final Cabecalho cabecalho;
    private final Stack conteudo = new Stack(18);

    public DashboardPanel(ReservaService service, Runnable novaReserva) {
        super(new BorderLayout());
        this.service = service;
        setOpaque(false);
        PillButton nova = new PillButton("+  Nova reserva", PillButton.Tipo.PRIMARIO);
        nova.addActionListener(e -> novaReserva.run());
        cabecalho = new Cabecalho("Painel", "", nova);
        add(cabecalho, BorderLayout.NORTH);
        add(Ui.rolagem(conteudo), BorderLayout.CENTER);
    }

    public void atualizar() {
        LocalDate hoje = service.hoje();
        cabecalho.setSubtitulo(Fmt.dataExtensa(hoje));
        conteudo.limpar();

        JPanel numeros = new JPanel(new GridLayout(1, 4, 18, 0));
        numeros.setOpaque(false);
        numeros.add(new CartaoNumero("No hotel hoje", service.hospedadosEm(hoje), true, Icones.LUA));
        numeros.add(new CartaoNumero("Chegadas hoje", service.chegadasEm(hoje), false, Icones.SETA));
        numeros.add(new CartaoNumero("Saídas hoje", service.saidasEm(hoje), false, Icones.SETA_VOLTA));
        numeros.add(new CartaoNumero("Creche hoje", service.crecheEm(hoje), false, Icones.SOL));
        conteudo.empilhar(numeros);

        JPanel listas = new JPanel(new GridLayout(1, 2, 18, 0));
        listas.setOpaque(false);
        listas.add(lista("Próximas chegadas", "nos próximos 7 dias", service.proximasChegadas(7), true));
        listas.add(lista("Próximas saídas", "nos próximos 7 dias", service.proximasSaidas(7), false));
        conteudo.empilhar(listas);
        conteudo.fechar();
        conteudo.revalidate();
        conteudo.repaint();
    }

    private enum Icones { LUA, SOL, SETA, SETA_VOLTA }

    private static class CartaoNumero extends RoundedPanel {
        private final Icones icone;
        private final boolean destaque;

        CartaoNumero(String rotulo, long valor, boolean destaque, Icones icone) {
            super(destaque ? Theme.TEAL : Theme.WHITE, 22, true);
            this.icone = icone;
            this.destaque = destaque;
            espaco(18, 22, 18, 22);
            setLayout(new BorderLayout(0, 2));
            JLabel n = Ui.rotulo(String.valueOf(valor), Theme.bold(42), destaque ? Theme.WHITE : Theme.TEAL_INK);
            JLabel r = Ui.rotulo(rotulo, Theme.medium(14), destaque ? Theme.CREAM : Theme.MUTED);
            add(n, BorderLayout.CENTER);
            add(r, BorderLayout.SOUTH);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Color c = destaque ? Theme.comAlpha(Theme.CREAM, 235) : Theme.TAN;
            int s = 26, x = getWidth() - s - 20, y = 18;
            switch (icone) {
                case LUA -> Icons.lua(g, x, y, s, c);
                case SOL -> Icons.sol(g, x, y, s, c);
                case SETA -> Icons.seta(g, x, y, s, c, true);
                default -> Icons.seta(g, x, y, s, c, false);
            }
        }
    }

    private JComponent lista(String titulo, String sub, List<Reserva> itens, boolean chegadas) {
        RoundedPanel card = new RoundedPanel(Theme.WHITE, 22, true).espaco(20, 22, 20, 22);
        card.setLayout(new BorderLayout(0, 12));
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(Ui.rotulo(titulo, Theme.bold(17), Theme.TEAL_DARK), BorderLayout.WEST);
        topo.add(Ui.rotulo(sub, Theme.regular(12), Theme.MUTED), BorderLayout.EAST);
        card.add(topo, BorderLayout.NORTH);

        JPanel linhas = new JPanel();
        linhas.setOpaque(false);
        linhas.setLayout(new BoxLayout(linhas, BoxLayout.Y_AXIS));
        if (itens.isEmpty()) {
            linhas.add(vazio());
        } else {
            int max = 6;
            for (int i = 0; i < Math.min(max, itens.size()); i++) {
                linhas.add(linha(itens.get(i), chegadas));
                linhas.add(Box.createVerticalStrut(8));
            }
            if (itens.size() > max) {
                JLabel mais = Ui.rotulo("+ " + (itens.size() - max) + " na lista de reservas", Theme.medium(12), Theme.MUTED);
                mais.setAlignmentX(Component.LEFT_ALIGNMENT);
                linhas.add(mais);
            }
        }
        card.add(linhas, BorderLayout.CENTER);
        return card;
    }

    private JComponent vazio() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Icons.patinha(g, 2, 8, 22, Theme.comAlpha(Theme.TAN, 200));
            }
        };
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createEmptyBorder(8, 30, 8, 0));
        p.add(Ui.rotulo("Nada previsto por aqui.", Theme.medium(14), Theme.MUTED));
        return p;
    }

    private JComponent linha(Reserva r, boolean chegada) {
        LocalDate dia = chegada ? r.entrada() : r.saidaEfetiva();
        LocalDate hoje = service.hoje();
        String quando = dia.equals(hoje) ? "Hoje" : dia.equals(hoje.plusDays(1)) ? "Amanhã" : Fmt.data(dia).substring(0, 5);
        String hora = Fmt.hora(chegada ? r.checkIn() : r.checkOut());

        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JPanel esq = new JPanel(new GridBagLayout());
        esq.setOpaque(false);
        esq.setPreferredSize(new Dimension(84, 40));
        esq.add(new Etiqueta(quando, dia.equals(hoje) ? Theme.TEAL : Theme.CREAM2, dia.equals(hoje) ? Theme.WHITE : Theme.TEAL_INK));
        p.add(esq, BorderLayout.WEST);

        JPanel meio = new JPanel();
        meio.setOpaque(false);
        meio.setLayout(new BoxLayout(meio, BoxLayout.Y_AXIS));
        meio.add(Ui.rotulo(r.petNome() + (Fmt.vazio(r.raca()) ? "" : "  ·  " + r.raca()), Theme.bold(14), Theme.TEAL_INK));
        meio.add(Ui.rotulo(r.tutorNome() + (hora.isEmpty() ? "" : "  ·  " + hora), Theme.regular(12), Theme.MUTED));
        p.add(meio, BorderLayout.CENTER);

        JPanel dir = new JPanel(new GridBagLayout());
        dir.setOpaque(false);
        dir.add(r.servico() == Servico.HOSPEDAGEM
                ? new Etiqueta("Hospedagem", Theme.CREAM2, Theme.TEAL_INK)
                : new Etiqueta("Creche", Theme.TAN, Theme.TEAL_INK));
        p.add(dir, BorderLayout.EAST);
        return p;
    }
}
