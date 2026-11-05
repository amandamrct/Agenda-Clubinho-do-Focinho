package clubinho.ui;

import clubinho.model.Reserva;
import clubinho.model.Servico;
import clubinho.model.Status;
import clubinho.service.ReservaService;
import clubinho.service.ValidacaoException;
import clubinho.util.Fmt;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Ficha de reserva (a mesma do PDF), para criar ou editar uma reserva. */
public class NovaReservaPanel extends JPanel {
    private final ReservaService service;
    private final Consumer<Reserva> aoSalvar;
    private final Runnable aoCancelarEdicao;

    private int editandoId = 0;
    private Status statusOriginal = Status.CONFIRMADA;
    private LocalDateTime criadaEmOriginal;

    private final Cabecalho cabecalho;
    private final Stack form = new Stack(18);

    private final HintField tutorNome = new HintField("Nome e sobrenome");
    private final HintField contato = new HintField("(21) 99999-9999");
    private final HintField endereco = new HintField("Rua, número, bairro");

    private final HintField petNome = new HintField("");
    private final HintField raca = new HintField("ex: shih tzu");
    private final HintField idade = new HintField("ex: 3 anos");
    private final ChipGroup castrado = new ChipGroup(true, "Sim", "Não");
    private final HintArea alimentacao = new HintArea("horários que o cãozinho costuma comer e quantidade", 3);
    private final HintField outrosAlimentos = new HintField("ex: maçã, ovo, iogurte,...");
    private final ChipGroup medicacao = new ChipGroup(true, "Sim", "Não");
    private final HintField medicacaoDetalhe = new HintField("qual(is) e horários");
    private final HintArea comportamento = new HintArea("como é a convivência com outros pets e com pessoas", 3);

    private final ChipGroup servico = new ChipGroup(false, "Hospedagem", "Creche");
    private final HintField entrada = new HintField("dd/mm/aaaa");
    private final HintField saida = new HintField("dd/mm/aaaa");
    private final HintField checkIn = new HintField("hh:mm");
    private final HintField checkOut = new HintField("hh:mm");
    private final FormField campoSaida;
    private final FormField campoMedicacaoDetalhe;
    private final JTextArea infoVagas = new JTextArea();

    private final RoundedPanel caixaErros = new RoundedPanel(new Color(0xFBE9E4), 18, false);
    private final JPanel listaErros = new JPanel();
    private final PillButton botaoSalvar = new PillButton("Salvar reserva", PillButton.Tipo.PRIMARIO);
    private final PillButton botaoLimpar = new PillButton("Limpar", PillButton.Tipo.CONTORNO);
    private final PillButton botaoCancelarEdicao = new PillButton("Cancelar edição", PillButton.Tipo.CONTORNO);

    public NovaReservaPanel(ReservaService service, Consumer<Reserva> aoSalvar, Runnable aoCancelarEdicao) {
        super(new BorderLayout());
        this.service = service;
        this.aoSalvar = aoSalvar;
        this.aoCancelarEdicao = aoCancelarEdicao;
        setOpaque(false);

        Masks.telefone(contato);
        Masks.data(entrada);
        Masks.data(saida);
        Masks.hora(checkIn);
        Masks.hora(checkOut);

        cabecalho = new Cabecalho("Nova reserva", "Preencha a ficha do tutor e do pet.", null);
        add(cabecalho, BorderLayout.NORTH);

        // --- avisos de erro (começa escondido)
        caixaErros.borda(Theme.DANGER, false).espaco(14, 18, 14, 18);
        caixaErros.setLayout(new BorderLayout(0, 6));
        caixaErros.add(Ui.rotulo("Confira os dados antes de salvar", Theme.bold(14), Theme.DANGER), BorderLayout.NORTH);
        listaErros.setOpaque(false);
        listaErros.setLayout(new BoxLayout(listaErros, BoxLayout.Y_AXIS));
        caixaErros.add(listaErros, BorderLayout.CENTER);
        caixaErros.setVisible(false);
        form.empilhar(caixaErros);

        // --- tutor
        form.empilhar(secao("Dados do tutor",
                new FormField("Nome completo do tutor", tutorNome),
                linha(new FormField("Contato (WhatsApp e ligação)", contato), new FormField("Endereço", endereco))));

        // --- pet
        castrado.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        medicacao.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        campoMedicacaoDetalhe = new FormField("Se sim, qual(is) e horários", medicacaoDetalhe);
        form.empilhar(secao("Dados do pet",
                linha(new FormField("Nome do pet", petNome), new FormField("Raça", raca), new FormField("Idade", idade)),
                new FormField("Castrado(a)?", castrado),
                new FormField("Alimentação", alimentacao),
                new FormField("Seu cãozinho costuma comer outros alimentos? Se sim, quais?", outrosAlimentos),
                linha(new FormField("Toma medicação?", medicacao), campoMedicacaoDetalhe),
                new FormField("Comportamento", comportamento)));

        // --- reserva
        infoVagas.setEditable(false);
        infoVagas.setFocusable(false);
        infoVagas.setOpaque(false);
        infoVagas.setLineWrap(true);
        infoVagas.setWrapStyleWord(true);
        infoVagas.setFont(Theme.medium(13));
        campoSaida = new FormField("Data de saída", saida);
        servico.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        form.empilhar(secao("Reserva",
                new FormField("Serviço", servico),
                linha(new FormField("Data de entrada", entrada), campoSaida,
                        new FormField("Horário previsto — check-in", checkIn),
                        new FormField("Horário previsto — check-out", checkOut)),
                infoVagas));

        // --- botões
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        botoes.setOpaque(false);
        botoes.add(botaoSalvar);
        botoes.add(botaoLimpar);
        botoes.add(botaoCancelarEdicao);
        botaoCancelarEdicao.setVisible(false);
        form.empilhar(botoes);
        form.fechar();
        add(Ui.rolagem(form), BorderLayout.CENTER);

        // --- comportamento
        botaoSalvar.addActionListener(e -> salvar());
        botaoLimpar.addActionListener(e -> limpar());
        botaoCancelarEdicao.addActionListener(e -> { limpar(); aoCancelarEdicao.run(); });
        servico.aoMudar(s -> aoMudarServico());
        medicacao.aoMudar(s -> aoMudarMedicacao());
        DocumentListener recalcular = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { atualizarVagas(); }
            @Override public void removeUpdate(DocumentEvent e) { atualizarVagas(); }
            @Override public void changedUpdate(DocumentEvent e) { atualizarVagas(); }
        };
        entrada.getDocument().addDocumentListener(recalcular);
        saida.getDocument().addDocumentListener(recalcular);
        limpar();
    }

    // ------------------------------------------------------------------ layout

    private RoundedPanel secao(String titulo, JComponent... linhas) {
        RoundedPanel card = new RoundedPanel(Theme.WHITE, 22, true).espaco(20, 24, 22, 24);
        card.setLayout(new BorderLayout(0, 6));
        card.add(Ui.tituloSecao(titulo), BorderLayout.NORTH);
        Stack s = new Stack(14);
        for (JComponent l : linhas) s.empilhar(l);
        card.add(s, BorderLayout.CENTER);
        return card;
    }

    private JPanel linha(JComponent... colunas) {
        JPanel p = new JPanel(new GridLayout(1, colunas.length, 16, 0));
        p.setOpaque(false);
        for (JComponent c : colunas) p.add(c);
        return p;
    }

    // ------------------------------------------------------------------ eventos

    private Servico servicoEscolhido() {
        return "Creche".equals(servico.selecionado()) ? Servico.CRECHE : Servico.HOSPEDAGEM;
    }

    private void aoMudarServico() {
        boolean hospedagem = servicoEscolhido() == Servico.HOSPEDAGEM;
        saida.setEnabled(hospedagem);
        campoSaida.setRotuloAtivo(hospedagem);
        if (!hospedagem) saida.setText("");
        atualizarVagas();
    }

    private void aoMudarMedicacao() {
        boolean sim = "Sim".equals(medicacao.selecionado());
        medicacaoDetalhe.setEnabled(sim);
        campoMedicacaoDetalhe.setRotuloAtivo(sim);
        if (!sim) medicacaoDetalhe.setText("");
    }

    private void atualizarVagas() {
        Servico s = servicoEscolhido();
        LocalDate e = Fmt.parseData(entrada.getText());
        LocalDate sd = Fmt.parseData(saida.getText());
        if (e == null) {
            info("Informe a data de entrada para ver a disponibilidade.", Theme.MUTED);
            return;
        }
        if (s == Servico.HOSPEDAGEM) {
            if (sd == null) { info("Informe a data de saída para ver a disponibilidade.", Theme.MUTED); return; }
            if (!sd.isAfter(e)) { info("A data de saída precisa ser depois da data de entrada.", Theme.DANGER); return; }
        }
        List<String> problemas = service.problemasDeVagas(s, e, sd, editandoId);
        if (!problemas.isEmpty()) {
            info(problemas.get(0), Theme.DANGER);
        } else if (s == Servico.HOSPEDAGEM) {
            long noites = java.time.temporal.ChronoUnit.DAYS.between(e, sd);
            info("Há vagas para esse período (" + noites + (noites == 1 ? " noite)." : " noites)."), Theme.TEAL_DARK);
        } else {
            info("Há vagas na creche nesse dia.", Theme.TEAL_DARK);
        }
    }

    private void info(String texto, Color cor) {
        infoVagas.setText(texto);
        infoVagas.setForeground(cor);
    }

    // ------------------------------------------------------------------ ações

    private Reserva montar(List<String> erros) {
        Servico s = servicoEscolhido();
        LocalTime in = Fmt.parseHora(checkIn.getText());
        LocalTime out = Fmt.parseHora(checkOut.getText());
        if (!checkIn.getText().isBlank() && in == null) erros.add("Horário de check-in inválido (use hh:mm).");
        if (!checkOut.getText().isBlank() && out == null) erros.add("Horário de check-out inválido (use hh:mm).");
        LocalDate e = Fmt.parseData(entrada.getText());
        LocalDate sd = s == Servico.CRECHE ? e : Fmt.parseData(saida.getText());
        String cast = castrado.selecionado() == null ? "" : castrado.selecionado();
        boolean med = "Sim".equals(medicacao.selecionado());
        return new Reserva(editandoId, s, statusOriginal,
                tutorNome.getText().trim(), contato.getText().trim(), endereco.getText().trim(),
                petNome.getText().trim(), raca.getText().trim(), idade.getText().trim(), cast,
                alimentacao.getText().trim(), outrosAlimentos.getText().trim(),
                med, medicacaoDetalhe.getText().trim(), comportamento.getText().trim(),
                e, sd, in, out, criadaEmOriginal);
    }

    private void salvar() {
        List<String> erros = new ArrayList<>();
        Reserva r = montar(erros);
        erros.addAll(service.validar(r));
        if (!erros.isEmpty()) {
            mostrarErros(erros);
            return;
        }
        try {
            Reserva salva = service.salvar(r);
            limpar();
            aoSalvar.accept(salva);
        } catch (ValidacaoException ex) {
            mostrarErros(ex.erros());
        } catch (IOException ex) {
            mostrarErros(List.of("Não foi possível gravar os dados: " + ex.getMessage()));
        }
    }

    private void mostrarErros(List<String> erros) {
        listaErros.removeAll();
        for (String e : erros) {
            JTextArea t = new JTextArea("•  " + e);
            t.setEditable(false);
            t.setFocusable(false);
            t.setOpaque(false);
            t.setLineWrap(true);
            t.setWrapStyleWord(true);
            t.setFont(Theme.regular(13));
            t.setForeground(Theme.TEAL_INK);
            listaErros.add(t);
        }
        caixaErros.setVisible(true);
        listaErros.revalidate();
        form.revalidate();
        form.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        form.repaint();
    }

    public boolean estaEditando() { return editandoId != 0; }

    /** Limpa a ficha e volta para o modo "nova reserva". */
    public void limpar() {
        editandoId = 0;
        statusOriginal = Status.CONFIRMADA;
        criadaEmOriginal = null;
        for (JTextField f : List.of(tutorNome, contato, endereco, petNome, raca, idade, outrosAlimentos,
                medicacaoDetalhe, entrada, saida, checkIn, checkOut)) f.setText("");
        alimentacao.setText("");
        comportamento.setText("");
        castrado.selecionar(null);
        medicacao.selecionar(null);
        servico.selecionar("Hospedagem");
        caixaErros.setVisible(false);
        cabecalho.setTitulo("Nova reserva");
        cabecalho.setSubtitulo("Preencha a ficha do tutor e do pet.");
        botaoSalvar.setText("Salvar reserva");
        botaoCancelarEdicao.setVisible(false);
        botaoLimpar.setVisible(true);
        aoMudarMedicacao();
        aoMudarServico();
        form.revalidate();
        form.repaint();
    }

    /** Preenche a ficha com uma reserva existente para editar. */
    public void carregar(Reserva r) {
        limpar();
        editandoId = r.id();
        statusOriginal = r.status();
        criadaEmOriginal = r.criadaEm();
        tutorNome.setText(r.tutorNome());
        contato.setText(r.contato());
        endereco.setText(r.endereco());
        petNome.setText(r.petNome());
        raca.setText(r.raca());
        idade.setText(r.idade());
        castrado.selecionar(Fmt.vazio(r.castrado()) ? null : r.castrado());
        alimentacao.setText(r.alimentacao());
        outrosAlimentos.setText(r.outrosAlimentos());
        medicacao.selecionar(r.medicacao() ? "Sim" : "Não");
        medicacaoDetalhe.setText(r.medicacaoDetalhe());
        comportamento.setText(r.comportamento());
        servico.selecionar(r.servico().rotulo());
        entrada.setText(Fmt.data(r.entrada()));
        if (r.servico() == Servico.HOSPEDAGEM) saida.setText(Fmt.data(r.saida()));
        checkIn.setText(Fmt.hora(r.checkIn()));
        checkOut.setText(Fmt.hora(r.checkOut()));
        cabecalho.setTitulo("Editar reserva #" + r.id());
        cabecalho.setSubtitulo("Altere o que precisar e salve. As vagas são conferidas de novo.");
        botaoSalvar.setText("Salvar alterações");
        botaoCancelarEdicao.setVisible(true);
        botaoLimpar.setVisible(false);
        atualizarVagas();
    }
}
