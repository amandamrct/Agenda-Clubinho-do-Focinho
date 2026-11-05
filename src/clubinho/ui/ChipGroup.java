package clubinho.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Grupo de chips de escolha única. Com permiteNenhum, clicar de novo no escolhido desmarca. */
public class ChipGroup extends JPanel {
    private final List<Chip> chips = new ArrayList<>();
    private final boolean permiteNenhum;
    private Consumer<String> aoMudar = s -> { };

    public ChipGroup(boolean permiteNenhum, String... rotulos) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.permiteNenhum = permiteNenhum;
        setOpaque(false);
        for (String r : rotulos) {
            Chip c = new Chip(r);
            c.setAlignmentY(Component.TOP_ALIGNMENT);
            c.addActionListener(e -> clicou(c));
            if (!chips.isEmpty()) add(Box.createHorizontalStrut(8));
            chips.add(c);
            add(c);
        }
        add(Box.createHorizontalGlue());
    }

    private void clicou(Chip c) {
        if (c.isSelected()) {
            for (Chip o : chips) if (o != c) o.setSelected(false);
        } else if (!permiteNenhum) {
            c.setSelected(true);
        }
        aoMudar.accept(selecionado());
    }

    /** Rótulo escolhido ou null. */
    public String selecionado() {
        for (Chip c : chips) if (c.isSelected()) return c.rotulo();
        return null;
    }

    public void selecionar(String rotulo) {
        for (Chip c : chips) c.setSelected(c.rotulo().equals(rotulo));
        aoMudar.accept(selecionado());
    }

    public void aoMudar(Consumer<String> acao) { this.aoMudar = acao; }
}
