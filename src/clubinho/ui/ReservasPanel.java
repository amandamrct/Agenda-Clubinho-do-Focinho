package clubinho.ui;

import clubinho.model.Reserva;
import clubinho.model.Servico;
import clubinho.model.Status;
import clubinho.service.ReservaService;
import clubinho.util.Fmt;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/** Lista de reservas com busca, filtros e ações (detalhes, editar, cancelar). */
public class ReservasPanel extends JPanel {
    private final ReservaService service;
    private final Consumer<Reserva> aoEditar;
    private final Cabecalho cabecalho = new Cabecalho("Reservas", "", null);
    private final Stack lista = new Stack(12);
    private final HintField busca;
    private final ChipGroup filtroStatus = new ChipGroup(false, "Todas", "Confirmadas", "Canceladas");
    private final ChipGroup filtroServico = new ChipGroup(false, "Todos", "Hospedagem", "Creche");

    public ReservasPanel(ReservaService service, Consumer<Reserva> aoEditar) {
        super(new BorderLayout());
        this.service = service;
        this.aoEditar = aoEditar;
        setOpaque(false);

        busca = new HintField("Buscar por tutor, pet ou telefone") {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Icons.lupa(g, 15, 13, 18, Theme.MUTED);
            }
        };
        busca.setBorder(BorderFactory.createEmptyBorder(10, 42, 10, 14));
        busca.setPreferredSize(new Dimension(330, 44));

        JPanel barra = new JPanel(new BorderLayout(18, 0));
        barra.setOpaque(false);
        barra.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        barra.add(busca, BorderLayout.WEST);
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 4));
        chips.setOpaque(false);
        chips.add(filtroStatus);
        chips.add(filtroServico);
        barra.add(chips, BorderLayout.CENTER);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(cabecalho, BorderLayout.NORTH);
        topo.add(barra, BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);
        add(Ui.rolagem(lista), BorderLayout.CENTER);

        filtroStatus.selecionar("Todas");
        filtroServico.selecionar("Todos");
        filtroStatus.aoMudar(s -> atualizar());
        filtroServico.aoMudar(s -> atualizar());
        busca.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { atualizar(); }
            @Override public void removeUpdate(DocumentEvent e) { atualizar(); }
            @Override public void changedUpdate(DocumentEvent e) { atualizar(); }
        });
    }

    public void atualizar() {
        Status st = switch (String.valueOf(filtroStatus.selecionado())) {
            case "Confirmadas" -> Status.CONFIRMADA;
            case "Canceladas" -> Status.CANCELADA;
            default -> null;
        };
        Servico sv = switch (String.valueOf(filtroServico.selecionado())) {
            case "Hospedagem" -> Servico.HOSPEDAGEM;
            case "Creche" -> Servico.CRECHE;
            default -> null;
        };
        List<Reserva> itens = service.buscar(busca.getText(), st, sv);
        cabecalho.setSubtitulo(itens.size() + (itens.size() == 1 ? " reserva" : " reservas"));

        lista.limpar();
        if (itens.isEmpty()) {
            lista.empilhar(vazio());
        } else {
            for (Reserva r : itens) lista.empilhar(new LinhaReserva(r));
        }
        lista.fechar();
        lista.revalidate();
        lista.repaint();
    }

    private JComponent vazio() {
        RoundedPanel p = new RoundedPanel(Theme.WHITE, 22, true).espaco(34, 24, 34, 24);
        p.setLayout(new BorderLayout());
        JLabel l = Ui.rotulo("Nenhuma reserva encontrada.", Theme.medium(15), Theme.MUTED);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private void cancelar(Reserva r) {
        boolean sim = Dialogs.confirmar(this, "Cancelar reserva",
                "Cancelar a reserva de " + r.petNome() + " (" + r.tutorNome() + ")? As vagas desse período serão liberadas.",
                "Cancelar reserva", true);
        if (!sim) return;
        try {
            service.cancelar(r.id());
        } catch (IOException e) {
            Dialogs.aviso(this, "Não foi possível cancelar", "Erro ao gravar os dados: " + e.getMessage());
        }
        atualizar();
    }

    /** Uma reserva na lista. */
    private class LinhaReserva extends RoundedPanel {
        LinhaReserva(Reserva r) {
            super(r.confirmada() ? Theme.WHITE : new Color(0xF4EEDC), 20, true);
            espaco(16, 18, 16, 18);
            setLayout(new BorderLayout(16, 0));
            boolean ativa = r.confirmada();

            add(new Distintivo(r.servico(), ativa), BorderLayout.WEST);

            JPanel meio = new JPanel();
            meio.setOpaque(false);
            meio.setLayout(new BoxLayout(meio, BoxLayout.Y_AXIS));
            JPanel nome = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            nome.setOpaque(false);
            nome.setAlignmentX(Component.LEFT_ALIGNMENT);
            nome.add(Ui.rotulo(r.petNome(), Theme.bold(17), ativa ? Theme.TEAL_INK : Theme.MUTED));
            if (!Fmt.vazio(r.raca())) nome.add(Ui.rotulo("   " + r.raca(), Theme.regular(13), Theme.MUTED));
            meio.add(nome);
            meio.add(linhaTexto(r.tutorNome() + "   ·   " + r.contato(), Theme.regular(13), Theme.MUTED));
            meio.add(Box.createVerticalStrut(4));
            meio.add(linhaTexto(periodo(r), Theme.medium(13), ativa ? Theme.TEAL_DARK : Theme.MUTED));
            add(meio, BorderLayout.CENTER);

            JPanel lado = new JPanel();
            lado.setOpaque(false);
            lado.setLayout(new BoxLayout(lado, BoxLayout.Y_AXIS));
            Etiqueta et = Etiqueta.paraSituacao(service.situacao(r));
            et.setAlignmentX(Component.RIGHT_ALIGNMENT);
            lado.add(et);
            lado.add(Box.createVerticalStrut(10));
            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            botoes.setOpaque(false);
            botoes.setAlignmentX(Component.RIGHT_ALIGNMENT);
            PillButton detalhes = new PillButton("Detalhes", PillButton.Tipo.CONTORNO, true);
            detalhes.addActionListener(e -> Dialogs.detalhes(ReservasPanel.this, r, service));
            botoes.add(detalhes);
            if (ativa) {
                PillButton editar = new PillButton("Editar", PillButton.Tipo.SECUNDARIO, true);
                editar.addActionListener(e -> aoEditar.accept(r));
                PillButton cancelar = new PillButton("Cancelar", PillButton.Tipo.PERIGO, true);
                cancelar.addActionListener(e -> cancelar(r));
                botoes.add(editar);
                botoes.add(cancelar);
            }
            lado.add(botoes);
            add(lado, BorderLayout.EAST);
        }

        private JLabel linhaTexto(String t, Font f, Color c) {
            JLabel l = Ui.rotulo(t, f, c);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            return l;
        }
    }

    private static String periodo(Reserva r) {
        String in = Fmt.hora(r.checkIn()), out = Fmt.hora(r.checkOut());
        if (r.servico() == Servico.HOSPEDAGEM) {
            String s = Fmt.data(r.entrada()) + " até " + Fmt.data(r.saida()) + "   ·   " + r.noites()
                    + (r.noites() == 1 ? " noite" : " noites");
            if (!in.isEmpty()) s += "   ·   check-in " + in;
            if (!out.isEmpty()) s += "   ·   check-out " + out;
            return s;
        }
        String s = "Creche em " + Fmt.data(r.entrada());
        if (!in.isEmpty() && !out.isEmpty()) s += "   ·   " + in + " às " + out;
        else if (!in.isEmpty()) s += "   ·   chegada " + in;
        return s;
    }

    /** Quadradinho colorido com ícone do serviço (lua = hospedagem, sol = creche). */
    private static class Distintivo extends JComponent {
        private final Servico servico;
        private final boolean ativa;

        Distintivo(Servico servico, boolean ativa) {
            this.servico = servico;
            this.ativa = ativa;
            setPreferredSize(new Dimension(54, 54));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.suavizar(g2);
            Color fundo = servico == Servico.HOSPEDAGEM ? Theme.TEAL : Theme.TAN;
            if (!ativa) fundo = Theme.comAlpha(fundo, 110);
            g2.setColor(fundo);
            g2.fillRoundRect(0, 0, 54, 54, 18, 18);
            Color icone = servico == Servico.HOSPEDAGEM ? Theme.CREAM : Theme.TEAL_INK;
            if (servico == Servico.HOSPEDAGEM) Icons.lua(g2, 12, 12, 30, icone);
            else Icons.sol(g2, 11, 11, 32, icone);
            g2.dispose();
        }
    }
}
