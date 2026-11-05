package clubinho.ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Identidade visual do Clubinho do Focinho: cores, fontes (Poppins) e logo. */
public final class Theme {
    public static final Color TEAL = new Color(0x019B99);
    public static final Color TEAL_DARK = new Color(0x026F6D);
    public static final Color TEAL_INK = new Color(0x0A3E3B);
    public static final Color CREAM = new Color(0xFBF0C6);
    public static final Color CREAM2 = new Color(0xF7E8A5);
    public static final Color TAN = new Color(0xE2BE84);
    public static final Color WHITE = new Color(0xFFFDF7);
    public static final Color MUTED = new Color(0x5F807D);
    public static final Color DANGER = new Color(0xB5503F);
    public static final Color FIELD_BG = new Color(0xFFF8DA);
    public static final Color FIELD_BORDER = new Color(0xE6D79F);
    public static final Color LINE = new Color(0xEADFB0);

    private static final Font REGULAR = carregar("Poppins-Regular.ttf", Font.PLAIN);
    private static final Font MEDIUM = carregar("Poppins-Medium.ttf", Font.PLAIN);
    private static final Font BOLD = carregar("Poppins-Bold.ttf", Font.BOLD);
    private static final Font ITALIC = carregar("Poppins-Italic.ttf", Font.ITALIC);
    private static BufferedImage logo;

    private Theme() {}

    /**
     * Abre um arquivo da pasta "resources". Primeiro tenta pelo classpath (funciona no .jar); se a IDE não tiver
     * copiado os recursos, procura a pasta "resources" ao lado do projeto. Devolve null se não achar.
     */
    private static InputStream abrir(String caminho) {
        InputStream in = Theme.class.getResourceAsStream("/" + caminho);
        if (in != null) return in;
        for (Path base : pastasPossiveis()) {
            Path arquivo = base.resolve("resources").resolve(caminho);
            if (Files.isRegularFile(arquivo)) {
                try { return Files.newInputStream(arquivo); } catch (IOException ignored) { }
            }
        }
        return null;
    }

    private static List<Path> pastasPossiveis() {
        List<Path> pastas = new ArrayList<>();
        pastas.add(Path.of("").toAbsolutePath());                       // pasta de onde o programa foi aberto
        try {
            Path codigo = Path.of(Theme.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path p = Files.isDirectory(codigo) ? codigo : codigo.getParent();
            for (int i = 0; i < 3 && p != null; i++, p = p.getParent()) pastas.add(p);   // bin/, projeto/...
        } catch (Exception ignored) { }
        return pastas;
    }

    private static Font carregar(String arquivo, int estiloPlanoB) {
        try (InputStream in = abrir("fonts/" + arquivo)) {
            if (in != null) return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception ignored) { }
        return new Font(Font.SANS_SERIF, estiloPlanoB, 12);   // plano B se a fonte não carregar
    }

    public static Font regular(float tamanho) { return REGULAR.deriveFont(tamanho); }
    public static Font medium(float tamanho) { return MEDIUM.deriveFont(tamanho); }
    public static Font bold(float tamanho) { return BOLD.deriveFont(tamanho); }
    public static Font italic(float tamanho) { return ITALIC.deriveFont(tamanho); }

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Logo do Clubinho (selo redondo) redimensionado com boa qualidade. */
    public static Image logo(int tamanho) {
        try {
            if (logo == null) {
                try (InputStream in = abrir("logo.png")) {
                    if (in != null) logo = ImageIO.read(in);
                }
                if (logo == null) {
                    System.err.println("Aviso: resources/logo.png não foi encontrado; usando o logo embutido no programa.");
                    logo = LogoEmbutido.logo();
                }
            }
            if (logo == null) return logoPlanoB(tamanho);
            return reduzir(logo, tamanho);
        } catch (Exception e) {
            return logoPlanoB(tamanho);
        }
    }

    /** Ícone da janela em vários tamanhos (versão simplificada: só a casinha e o filhote, para ficar legível pequeno). */
    public static List<Image> icones() {
        BufferedImage base = LogoEmbutido.icone();
        List<Image> lista = new ArrayList<>();
        if (base == null) {
            lista.add(logo(64));
            return lista;
        }
        for (int t : new int[]{16, 20, 24, 32, 40, 48, 64, 128, 256}) lista.add(reduzir(base, t));
        return lista;
    }

    /** Redimensiona em etapas (metade por vez) para não ficar serrilhado. */
    private static BufferedImage reduzir(BufferedImage origem, int tamanho) {
        BufferedImage atual = origem;
        int w = origem.getWidth();
        while (w / 2 > tamanho) {
            w /= 2;
            atual = escalar(atual, w);
        }
        return escalar(atual, tamanho);
    }

    private static BufferedImage escalar(BufferedImage origem, int lado) {
        BufferedImage out = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(origem, 0, 0, lado, lado, null);
        g.dispose();
        return out;
    }

    /** Se o arquivo do logo não for encontrado: círculo verde com uma patinha (nunca fica o espaço vazio). */
    private static Image logoPlanoB(int tamanho) {
        BufferedImage out = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        suavizar(g);
        g.setColor(TEAL);
        g.fillOval(0, 0, tamanho, tamanho);
        Icons.patinha(g, tamanho * 0.24, tamanho * 0.24, tamanho * 0.52, CREAM);
        g.dispose();
        return out;
    }

    public static Color comAlpha(Color c, int alpha) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha); }
}
