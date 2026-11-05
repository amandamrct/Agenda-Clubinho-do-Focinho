package clubinho.ui;

import clubinho.data.Config;
import clubinho.model.Reserva;
import clubinho.model.Servico;
import clubinho.service.ReservaService;
import clubinho.util.Fmt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Calendário mensal com a ocupação de cada dia, vagas do hotel e fechamento de agenda. */
public class AgendaPanel extends JPanel {
    private final ReservaService service;
    private final Cabecalho cabecalho = new Cabecalho("Agenda", "Ocupação por dia. Clique em um dia para ver quem está.", null);
    private final Grade grade = new Grade();
    private final ChipGroup tipo = new ChipGroup(false, "Hospedagem", "Creche");
    private final JLabel nomeMes = Ui.rotulo("", Theme.bold(20), Theme.TEAL_INK);
    private final JPanel doDia = new JPanel();
    private final JLabel tituloDia = Ui.rotulo("", Theme.bold(15), Theme.TEAL_DARK);

    private final HintField campoVagasHosp = new HintField("");
    private final HintField campoVagasCreche = new HintField("");
    private final HintField campoFechadaAte = new HintField("dd/mm/aaaa (vazio = agenda aberta)");
    private final JLabel aviso = Ui.rotulo(" ", Theme.medium(12), Theme.TEAL_DARK);

    private YearMonth mes;
    private LocalDate selecionado;

    public AgendaPanel(ReservaService service) {
        super(new BorderLayout());
        this.service = service;
        setOpaque(false);
        this.mes = YearMonth.from(service.hoje());
        this.selecionado = service.hoje();

        Masks.numero(campoVagasHosp, 3);
        Masks.numero(campoVagasCreche, 3);
        Masks.data(campoFechadaAte);
        add(cabecalho, BorderLayout.NORTH);

        // calendário
        RoundedPanel calendario = new RoundedPanel(Theme.WHITE, 22, true).espaco(18, 20, 18, 20);
        calendario.setLayout(new BorderLayout(0, 12));
        JPanel navegacao = new JPanel(new BorderLayout(10, 0));
        navegacao.setOpaque(false);
        JPanel setas = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        setas.setOpaque(false);
        setas.add(botaoSeta(false));
        setas.add(botaoSeta(true));
        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        esquerda.setOpaque(false);
        esquerda.add(setas);
        esquerda.add(nomeMes);
        navegacao.add(esquerda, BorderLayout.WEST);
        navegacao.add(tipo, BorderLayout.EAST);
        calendario.add(navegacao, BorderLayout.NORTH);
        calendario.add(grade, BorderLayout.CENTER);
        calendario.add(legenda(), BorderLayout.SOUTH);

        // coluna da direita
        Stack direita = new Stack(18);
        direita.empilhar(cartaoDia());
        direita.empilhar(cartaoConfig());
        direita.fechar();
        JScrollPane rolagemDireita = Ui.rolagem(direita);
        rolagemDireita.setPreferredSize(new Dimension(380, 100));

        JPanel corpo = new JPanel(new BorderLayout(20, 0));
        corpo.setOpaque(false);
        corpo.add(calendario, BorderLayout.CENTER);
        corpo.add(rolagemDireita, BorderLayout.EAST);
        add(corpo, BorderLayout.CENTER);

        tipo.selecionar("Hospedagem");
        tipo.aoMudar(s -> atualizar());
        grade.aoClicar = d -> { selecionado = d; atualizarDia(); grade.repaint(); };
    }

    private JButton botaoSeta(boolean direita) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.suavizar(g2);
                g2.setColor(getModel().isRollover() ? Theme.CREAM2 : Theme.FIELD_BG);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                Icons.seta(g2, 8, 8, 20, Theme.TEAL_DARK, direita);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(36, 36));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> { mes = direita ? mes.plusMonths(1) : mes.minusMonths(1); atualizar(); });
        return b;
    }

    private JComponent legenda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        p.setOpaque(false);
        p.add(item(Grade.LIVRE, "livre"));
        p.add(item(Grade.POUCO, "ocupando"));
        p.add(item(Grade.QUASE, "quase lotado"));
        p.add(item(Grade.CHEIO, "lotado"));
        p.add(item(Grade.FECHADO, "agenda fechada"));
        return p;
    }

    private JComponent item(Color cor, String texto) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.suavizar(g2);
                g2.setColor(cor);
                g2.fillRoundRect(0, 4, 16, 16, 6, 6);
                g2.setColor(Theme.FIELD_BORDER);
                g2.drawRoundRect(0, 4, 15, 15, 6, 6);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        p.add(Ui.rotulo(texto, Theme.regular(12), Theme.MUTED));
        return p;
    }

    private JComponent cartaoDia() {
        RoundedPanel c = new RoundedPanel(Theme.WHITE, 22, true).espaco(18, 20, 18, 20);
        c.setLayout(new BorderLayout(0, 10));
        c.add(tituloDia, BorderLayout.NORTH);
        doDia.setOpaque(false);
        doDia.setLayout(new BoxLayout(doDia, BoxLayout.Y_AXIS));
        c.add(doDia, BorderLayout.CENTER);
        return c;
    }

    private JComponent cartaoConfig() {
        RoundedPanel c = new RoundedPanel(Theme.WHITE, 22, true).espaco(18, 20, 20, 20);
        c.setLayout(new BorderLayout(0, 8));
        c.add(Ui.tituloSecao("Configurações"), BorderLayout.NORTH);
        Stack s = new Stack(12);
        JPanel vagas = new JPanel(new GridLayout(1, 2, 12, 0));
        vagas.setOpaque(false);
        vagas.add(new FormField("Vagas hospedagem", campoVagasHosp));
        vagas.add(new FormField("Vagas creche", campoVagasCreche));
        s.empilhar(vagas);
        s.empilhar(new FormField("Agenda fechada até", campoFechadaAte));
        PillButton salvar = new PillButton("Salvar configurações", PillButton.Tipo.PRIMARIO);
        salvar.addActionListener(e -> salvarConfig());
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.setOpaque(false);
        linha.add(salvar);
        s.empilhar(linha);
        s.empilhar(aviso);
        java.nio.file.Path pasta = service.config().pastaDados();
        JTextArea nota = new JTextArea(pasta == null
                ? "Modo demonstração: nada é gravado."
                : "Seus dados ficam em: " + pasta + "\nFaça uma cópia dessa pasta de vez em quando (backup).");
        nota.setEditable(false);
        nota.setFocusable(false);
        nota.setOpaque(false);
        nota.setLineWrap(true);
        nota.setWrapStyleWord(false);
        nota.setFont(Theme.regular(12));
        nota.setForeground(Theme.MUTED);
        s.empilhar(nota);
        c.add(s, BorderLayout.CENTER);
        return c;
    }

    private void salvarConfig() {
        Config cfg = service.config();
        int h, cr;
        try {
            h = Integer.parseInt(campoVagasHosp.getText().trim());
            cr = Integer.parseInt(campoVagasCreche.getText().trim());
        } catch (NumberFormatException e) {
            avisar("Informe o número de vagas (use números inteiros).", Theme.DANGER);
            return;
        }
        if (h < 1 || cr < 1) { avisar("O número de vagas precisa ser pelo menos 1.", Theme.DANGER); return; }
        LocalDate fechada = null;
        if (!campoFechadaAte.getText().isBlank()) {
            fechada = Fmt.parseData(campoFechadaAte.getText());
            if (fechada == null) { avisar("Data de fechamento inválida (use dd/mm/aaaa).", Theme.DANGER); return; }
        }
        cfg.setVagasHospedagem(h);
        cfg.setVagasCreche(cr);
        cfg.setAgendaFechadaAte(fechada);
        try {
            cfg.salvar();
            avisar("Configurações salvas.", Theme.TEAL_DARK);
        } catch (IOException e) {
            avisar("Não foi possível gravar: " + e.getMessage(), Theme.DANGER);
        }
        atualizar();
    }

    private static JComponent esquerda(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private void avisar(String texto, Color cor) { aviso.setText(texto); aviso.setForeground(cor); }

    private Servico servico() { return "Creche".equals(tipo.selecionado()) ? Servico.CRECHE : Servico.HOSPEDAGEM; }

    /** Chamado quando a tela é aberta: recarrega os números e os campos de configuração. */
    public void atualizar() {
        Config cfg = service.config();
        campoVagasHosp.setText(String.valueOf(cfg.vagasHospedagem()));
        campoVagasCreche.setText(String.valueOf(cfg.vagasCreche()));
        campoFechadaAte.setText(Fmt.data(cfg.agendaFechadaAte()));
        nomeMes.setText(Fmt.mes(mes));
        grade.configurar(mes, selecionado, service, servico());
        atualizarDia();
        grade.repaint();
    }

    private void atualizarDia() {
        Servico s = servico();
        int vagas = service.vagas(s);
        int ocupadas = service.ocupacao(selecionado, s, 0);
        tituloDia.setText(Fmt.dataExtensa(selecionado));
        doDia.removeAll();
        String resumo = ocupadas + " de " + vagas + " vagas ocupadas  ·  " + s.rotulo().toLowerCase();
        doDia.add(esquerda(Ui.rotulo(resumo, Theme.medium(13), ocupadas >= vagas ? Theme.DANGER : Theme.TEAL_DARK)));
        if (service.agendaFechada(selecionado)) {
            doDia.add(Box.createVerticalStrut(4));
            doDia.add(esquerda(Ui.rotulo("Agenda fechada neste dia.", Theme.medium(12), Theme.MUTED)));
        }
        doDia.add(Box.createVerticalStrut(10));
        List<Reserva> itens = service.doDia(selecionado, s);
        if (itens.isEmpty()) {
            doDia.add(esquerda(Ui.rotulo("Nenhum pet nesse dia.", Theme.regular(13), Theme.MUTED)));
        }
        for (Reserva r : itens) {
            JPanel linha = new JPanel(new BorderLayout());
            linha.setOpaque(false);
            linha.setAlignmentX(Component.LEFT_ALIGNMENT);
            linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            linha.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            textos.add(Ui.rotulo(r.petNome() + (Fmt.vazio(r.raca()) ? "" : "  ·  " + r.raca()), Theme.bold(14), Theme.TEAL_INK));
            textos.add(Ui.rotulo(r.tutorNome(), Theme.regular(12), Theme.MUTED));
            linha.add(textos, BorderLayout.CENTER);
            doDia.add(linha);
        }
        doDia.revalidate();
        doDia.repaint();
    }

    // ------------------------------------------------------------------------ grade do mês

    /** Desenha o mês: cada dia mostra quantas vagas estão ocupadas. */
    static class Grade extends JComponent {
        static final Color LIVRE = new Color(0xFFFBEA);
        static final Color POUCO = new Color(0xF7E8A5);
        static final Color QUASE = new Color(0xE2BE84);
        static final Color CHEIO = new Color(0x026F6D);
        static final Color FECHADO = new Color(0xE7E2CF);
        private static final String[] SEMANA = {"dom", "seg", "ter", "qua", "qui", "sex", "sáb"};

        private YearMonth mes = YearMonth.now();
        private LocalDate selecionado = LocalDate.now();
        private ReservaService service;
        private Servico servico = Servico.HOSPEDAGEM;
        java.util.function.Consumer<LocalDate> aoClicar = d -> { };
        private static final int CAB = 28;

        Grade() {
            setPreferredSize(new Dimension(560, 420));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    LocalDate d = diaEm(e.getX(), e.getY());
                    if (d != null) aoClicar.accept(d);
                }
            });
        }

        void configurar(YearMonth mes, LocalDate selecionado, ReservaService service, Servico servico) {
            this.mes = mes;
            this.selecionado = selecionado;
            this.service = service;
            this.servico = servico;
        }

        private int linhas() {
            int offset = mes.atDay(1).getDayOfWeek().getValue() % 7;
            return (int) Math.ceil((offset + mes.lengthOfMonth()) / 7.0);
        }

        private LocalDate diaEm(int x, int y) {
            if (y < CAB) return null;
            int col = x * 7 / Math.max(1, getWidth());
            int lin = (y - CAB) * linhas() / Math.max(1, getHeight() - CAB);
            int offset = mes.atDay(1).getDayOfWeek().getValue() % 7;
            int dia = lin * 7 + col - offset + 1;
            return dia >= 1 && dia <= mes.lengthOfMonth() ? mes.atDay(dia) : null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.suavizar(g2);
            int w = getWidth(), h = getHeight();
            // dias da semana
            g2.setFont(Theme.bold(12));
            g2.setColor(Theme.MUTED);
            FontMetrics fm = g2.getFontMetrics();
            for (int c = 0; c < 7; c++) {
                int cx = c * w / 7 + (w / 7 - fm.stringWidth(SEMANA[c])) / 2;
                g2.drawString(SEMANA[c], cx, 18);
            }
            if (service == null) { g2.dispose(); return; }

            int linhas = linhas();
            int offset = mes.atDay(1).getDayOfWeek().getValue() % 7;
            int gap = 6;
            int cw = w / 7, ch = (h - CAB) / linhas;
            LocalDate hoje = service.hoje();
            int vagas = service.vagas(servico);
            for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
                LocalDate data = mes.atDay(dia);
                int idx = offset + dia - 1;
                int x = (idx % 7) * cw + gap / 2, y = CAB + (idx / 7) * ch + gap / 2;
                int rw = cw - gap, rh = ch - gap;
                int ocup = service.ocupacao(data, servico, 0);
                boolean fechado = service.agendaFechada(data);
                Color fundo = fechado ? FECHADO : ocup == 0 ? LIVRE : ocup >= vagas ? CHEIO
                        : ocup * 2 >= vagas ? QUASE : POUCO;
                g2.setColor(fundo);
                g2.fillRoundRect(x, y, rw, rh, 14, 14);
                if (fechado) {
                    g2.setColor(Theme.comAlpha(Theme.MUTED, 50));
                    Shape clip = g2.getClip();
                    g2.clipRect(x, y, rw, rh);
                    for (int k = -rh; k < rw; k += 10) g2.drawLine(x + k, y + rh, x + k + rh, y);
                    g2.setClip(clip);
                }
                boolean eHoje = data.equals(hoje), sel = data.equals(selecionado);
                g2.setStroke(new BasicStroke(sel ? 3f : eHoje ? 2f : 1.2f));
                g2.setColor(sel ? Theme.TEAL : eHoje ? Theme.TEAL_DARK : Theme.FIELD_BORDER);
                g2.drawRoundRect(x + 1, y + 1, rw - 2, rh - 2, 14, 14);

                Color texto = ocup >= vagas && !fechado ? Theme.CREAM : Theme.TEAL_INK;
                g2.setColor(texto);
                g2.setFont(Theme.bold(14));
                g2.drawString(String.valueOf(dia), x + 10, y + 22);
                if (!fechado && ocup > 0) {
                    String t = ocup + "/" + vagas;
                    g2.setFont(Theme.medium(11));
                    FontMetrics f2 = g2.getFontMetrics();
                    g2.setColor(ocup == 0 ? Theme.comAlpha(Theme.MUTED, 150) : texto);
                    g2.drawString(t, x + rw - f2.stringWidth(t) - 9, y + rh - 9);
                }
            }
            g2.dispose();
        }
    }
}
