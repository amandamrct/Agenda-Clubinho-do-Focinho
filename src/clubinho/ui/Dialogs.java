package clubinho.ui;

import clubinho.model.Reserva;
import clubinho.model.Servico;
import clubinho.service.ReservaService;
import clubinho.util.Fmt;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Janelas de confirmação, aviso e detalhes, no visual do Clubinho. */
public final class Dialogs {
    private Dialogs() {}

    private static JDialog base(Component pai, String titulo) {
        Window janela = pai == null ? null : SwingUtilities.getWindowAncestor(pai);
        JDialog d = new JDialog(janela, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        return d;
    }

    private static JTextArea texto(String t, Font f, Color c) {
        JTextArea a = new JTextArea(t);
        a.setEditable(false);
        a.setFocusable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setOpaque(false);
        a.setFont(f);
        a.setForeground(c);
        a.setBorder(BorderFactory.createEmptyBorder());
        return a;
    }

    /** Pergunta sim/não. Devolve true se confirmou. */
    public static boolean confirmar(Component pai, String titulo, String mensagem, String textoSim, boolean perigo) {
        JDialog d = base(pai, titulo);
        boolean[] resposta = {false};

        JPanel raiz = new JPanel(new BorderLayout(0, 18));
        raiz.setBackground(Theme.CREAM);
        raiz.setBorder(BorderFactory.createEmptyBorder(26, 30, 24, 30));
        raiz.add(Ui.rotulo(titulo, Theme.bold(20), Theme.TEAL_INK), BorderLayout.NORTH);
        JTextArea msg = texto(mensagem, Theme.regular(14), Theme.TEAL_DARK);
        msg.setColumns(30);
        raiz.add(msg, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoes.setOpaque(false);
        PillButton nao = new PillButton("Voltar", PillButton.Tipo.CONTORNO);
        PillButton sim = new PillButton(textoSim, perigo ? PillButton.Tipo.PERIGO : PillButton.Tipo.PRIMARIO);
        nao.addActionListener(e -> d.dispose());
        sim.addActionListener(e -> { resposta[0] = true; d.dispose(); });
        botoes.add(nao);
        botoes.add(sim);
        raiz.add(botoes, BorderLayout.SOUTH);

        d.setContentPane(raiz);
        d.pack();
        d.setLocationRelativeTo(pai);
        d.setVisible(true);
        return resposta[0];
    }

    public static void aviso(Component pai, String titulo, String mensagem) {
        JDialog d = base(pai, titulo);
        JPanel raiz = new JPanel(new BorderLayout(0, 18));
        raiz.setBackground(Theme.CREAM);
        raiz.setBorder(BorderFactory.createEmptyBorder(26, 30, 24, 30));
        raiz.add(Ui.rotulo(titulo, Theme.bold(20), Theme.TEAL_INK), BorderLayout.NORTH);
        JTextArea msg = texto(mensagem, Theme.regular(14), Theme.TEAL_DARK);
        msg.setColumns(30);
        raiz.add(msg, BorderLayout.CENTER);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoes.setOpaque(false);
        PillButton ok = new PillButton("Ok", PillButton.Tipo.PRIMARIO);
        ok.addActionListener(e -> d.dispose());
        botoes.add(ok);
        raiz.add(botoes, BorderLayout.SOUTH);
        d.setContentPane(raiz);
        d.pack();
        d.setLocationRelativeTo(pai);
        d.setVisible(true);
    }

    /** Todos os dados da reserva (a ficha completa). */
    public static void detalhes(Component pai, Reserva r, ReservaService svc) {
        JDialog d = base(pai, "Reserva #" + r.id());
        d.setContentPane(painelDetalhes(r, svc, d::dispose));
        d.pack();
        d.setLocationRelativeTo(pai);
        d.setVisible(true);
    }

    /** Conteúdo da janela de detalhes (separado para facilitar testes e capturas de tela). */
    static JPanel painelDetalhes(Reserva r, ReservaService svc, Runnable fechar) {
        JPanel raiz = new JPanel(new BorderLayout(0, 14));
        raiz.setBackground(Theme.CREAM);
        raiz.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));

        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setOpaque(false);
        topo.add(Ui.rotulo(r.petNome() + "  ·  " + r.servico().rotulo(), Theme.bold(20), Theme.TEAL_INK), BorderLayout.CENTER);
        topo.add(Etiqueta.paraSituacao(svc.situacao(r)), BorderLayout.EAST);
        raiz.add(topo, BorderLayout.NORTH);

        Stack lista = new Stack(10);
        lista.empilhar(secao("Tutor"));
        lista.empilhar(par("Nome", r.tutorNome()));
        lista.empilhar(par("Contato", r.contato()));
        lista.empilhar(par("Endereço", r.endereco()));
        lista.empilhar(secao("Pet"));
        lista.empilhar(par("Raça", r.raca()));
        lista.empilhar(par("Idade", r.idade()));
        lista.empilhar(par("Castrado(a)", r.castrado()));
        lista.empilhar(par("Alimentação", r.alimentacao()));
        lista.empilhar(par("Outros alimentos", r.outrosAlimentos()));
        lista.empilhar(par("Medicação", r.medicacao() ? "Sim — " + r.medicacaoDetalhe() : "Não"));
        lista.empilhar(par("Comportamento", r.comportamento()));
        lista.empilhar(secao("Reserva"));
        if (r.servico() == Servico.HOSPEDAGEM) {
            lista.empilhar(par("Período", Fmt.data(r.entrada()) + " até " + Fmt.data(r.saida())
                    + "  (" + r.noites() + (r.noites() == 1 ? " noite)" : " noites)")));
        } else {
            lista.empilhar(par("Dia", Fmt.data(r.entrada())));
        }
        lista.empilhar(par("Check-in", Fmt.hora(r.checkIn())));
        lista.empilhar(par("Check-out", Fmt.hora(r.checkOut())));
        lista.empilhar(par("Registrada em", r.criadaEm() == null ? "" : Fmt.data(r.criadaEm().toLocalDate())
                + " às " + Fmt.hora(r.criadaEm().toLocalTime())));
        lista.fechar();

        JScrollPane rolagem = Ui.rolagem(lista);
        rolagem.setPreferredSize(new Dimension(520, 440));
        raiz.add(rolagem, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoes.setOpaque(false);
        PillButton botaoFechar = new PillButton("Fechar", PillButton.Tipo.PRIMARIO);
        botaoFechar.addActionListener(e -> fechar.run());
        botoes.add(botaoFechar);
        raiz.add(botoes, BorderLayout.SOUTH);
        return raiz;
    }

    private static JComponent secao(String nome) {
        JLabel l = Ui.rotulo(nome.toUpperCase(), Theme.bold(12), Theme.TEAL);
        l.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        return l;
    }

    private static JComponent par(String rotulo, String valor) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.add(Ui.rotulo(rotulo, Theme.bold(12), Theme.MUTED), BorderLayout.NORTH);
        p.add(texto(Fmt.vazio(valor) ? "—" : valor, Theme.regular(14), Theme.TEAL_INK), BorderLayout.CENTER);
        return p;
    }

    /** Mostra uma lista de erros de validação como aviso. */
    public static void erros(Component pai, List<String> erros) {
        aviso(pai, "Confira os dados", String.join("\n", erros));
    }
}
