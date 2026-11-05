package clubinho.ui;

import java.awt.*;
import java.awt.geom.*;

/** Ícones simples desenhados em código (sem depender de imagens nem de emojis). */
public final class Icons {
    private Icons() {}

    private static Graphics2D prep(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.suavizar(g2);
        return g2;
    }

    public static void patinha(Graphics g, double x, double y, double s, Color c) {
        Graphics2D g2 = prep(g);
        g2.setColor(c);
        g2.fill(new Ellipse2D.Double(x + 0.04 * s, y + 0.38 * s, 0.20 * s, 0.27 * s));
        g2.fill(new Ellipse2D.Double(x + 0.26 * s, y + 0.10 * s, 0.22 * s, 0.31 * s));
        g2.fill(new Ellipse2D.Double(x + 0.52 * s, y + 0.10 * s, 0.22 * s, 0.31 * s));
        g2.fill(new Ellipse2D.Double(x + 0.76 * s, y + 0.38 * s, 0.20 * s, 0.27 * s));
        Path2D pad = new Path2D.Double();
        pad.moveTo(x + 0.50 * s, y + 0.46 * s);
        pad.curveTo(x + 0.78 * s, y + 0.46 * s, x + 0.90 * s, y + 0.74 * s, x + 0.74 * s, y + 0.90 * s);
        pad.curveTo(x + 0.62 * s, y + 0.98 * s, x + 0.56 * s, y + 0.88 * s, x + 0.50 * s, y + 0.88 * s);
        pad.curveTo(x + 0.44 * s, y + 0.88 * s, x + 0.38 * s, y + 0.98 * s, x + 0.26 * s, y + 0.90 * s);
        pad.curveTo(x + 0.10 * s, y + 0.74 * s, x + 0.22 * s, y + 0.46 * s, x + 0.50 * s, y + 0.46 * s);
        g2.fill(pad);
        g2.dispose();
    }

    public static void painel(Graphics g, double x, double y, double s, Color c) {
        Graphics2D g2 = prep(g);
        g2.setColor(c);
        double q = s * 0.44, gap = s * 0.12;
        g2.fill(new RoundRectangle2D.Double(x, y, q, q, s * 0.12, s * 0.12));
        g2.fill(new RoundRectangle2D.Double(x + q + gap, y, q, q, s * 0.12, s * 0.12));
        g2.fill(new RoundRectangle2D.Double(x, y + q + gap, q, q, s * 0.12, s * 0.12));
        g2.fill(new RoundRectangle2D.Double(x + q + gap, y + q + gap, q, q, s * 0.12, s * 0.12));
        g2.dispose();
    }

    public static void mais(Graphics g, double x, double y, double s, Color c) {
        Graphics2D g2 = prep(g);
        g2.setColor(c);
        g2.setStroke(new BasicStroke((float) (s * 0.14), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Double(x + s * 0.5, y + s * 0.12, x + s * 0.5, y + s * 0.88));
        g2.draw(new Line2D.Double(x + s * 0.12, y + s * 0.5, x + s * 0.88, y + s * 0.5));
        g2.dispose();
    }

    public static void lista(Graphics g, double x, double y, double s, Color c) {
        Graphics2D g2 = prep(g);
        g2.setColor(c);
        g2.setStroke(new BasicStroke((float) (s * 0.12), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            double yy = y + s * (0.20 + 0.30 * i);
            g2.fill(new Ellipse2D.Double(x + s * 0.04, yy - s * 0.07, s * 0.14, s * 0.14));
            g2.draw(new Line2D.Double(x + s * 0.34, yy, x + s * 0.94, yy));
        }
        g2.dispose();
    }

    public static void calendario(Graphics g, double x, double y, double s, Color c) {
        Graphics2D g2 = prep(g);
        g2.setColor(c);
        g2.setStroke(new BasicStroke((float) (s * 0.10), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new RoundRectangle2D.Double(x + s * 0.08, y + s * 0.16, s * 0.84, s * 0.76, s * 0.18, s * 0.18));
        g2.draw(new Line2D.Double(x + s * 0.08, y + s * 0.40, x + s * 0.92, y + s * 0.40));
        g2.draw(new Line2D.Double(x + s * 0.30, y + s * 0.04, x + s * 0.30, y + s * 0.26));
        g2.draw(new Line2D.Double(x + s * 0.70, y + s * 0.04, x + s * 0.70, y + s * 0.26));
        g2.fill(new Ellipse2D.Double(x + s * 0.30, y + s * 0.55, s * 0.12, s * 0.12));
        g2.fill(new Ellipse2D.Double(x + s * 0.58, y + s * 0.55, s * 0.12, s * 0.12));
        g2.dispose();
    }

    public static void lupa(Graphics g, double x, double y, double s, Color c) {
        Graphics2D g2 = prep(g);
        g2.setColor(c);
        g2.setStroke(new BasicStroke((float) (s * 0.12), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Ellipse2D.Double(x + s * 0.10, y + s * 0.10, s * 0.55, s * 0.55));
        g2.draw(new Line2D.Double(x + s * 0.58, y + s * 0.58, x + s * 0.90, y + s * 0.90));
        g2.dispose();
    }

    public static void lua(Graphics g, double x, double y, double s, Color c) {
        Graphics2D g2 = prep(g);
        Area lua = new Area(new Ellipse2D.Double(x + s * 0.12, y + s * 0.12, s * 0.76, s * 0.76));
        lua.subtract(new Area(new Ellipse2D.Double(x + s * 0.36, y + s * 0.02, s * 0.70, s * 0.70)));
        g2.setColor(c);
        g2.fill(lua);
        g2.dispose();
    }

    public static void sol(Graphics g, double x, double y, double s, Color c) {
        Graphics2D g2 = prep(g);
        g2.setColor(c);
        g2.fill(new Ellipse2D.Double(x + s * 0.28, y + s * 0.28, s * 0.44, s * 0.44));
        g2.setStroke(new BasicStroke((float) (s * 0.09), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            double a = Math.PI / 4 * i;
            double cx = x + s / 2, cy = y + s / 2;
            g2.draw(new Line2D.Double(cx + Math.cos(a) * s * 0.34, cy + Math.sin(a) * s * 0.34,
                    cx + Math.cos(a) * s * 0.46, cy + Math.sin(a) * s * 0.46));
        }
        g2.dispose();
    }

    public static void seta(Graphics g, double x, double y, double s, Color c, boolean direita) {
        Graphics2D g2 = prep(g);
        g2.setColor(c);
        g2.setStroke(new BasicStroke((float) (s * 0.14), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D p = new Path2D.Double();
        if (direita) {
            p.moveTo(x + s * 0.34, y + s * 0.16); p.lineTo(x + s * 0.68, y + s * 0.5); p.lineTo(x + s * 0.34, y + s * 0.84);
        } else {
            p.moveTo(x + s * 0.66, y + s * 0.16); p.lineTo(x + s * 0.32, y + s * 0.5); p.lineTo(x + s * 0.66, y + s * 0.84);
        }
        g2.draw(p);
        g2.dispose();
    }

    public static void coracao(Graphics g, double x, double y, double s, Color c) {
        Graphics2D g2 = prep(g);
        Path2D p = new Path2D.Double();
        p.moveTo(x + s * 0.5, y + s * 0.88);
        p.curveTo(x + s * 0.05, y + s * 0.55, x + s * 0.02, y + s * 0.15, x + s * 0.28, y + s * 0.15);
        p.curveTo(x + s * 0.42, y + s * 0.15, x + s * 0.5, y + s * 0.28, x + s * 0.5, y + s * 0.30);
        p.curveTo(x + s * 0.5, y + s * 0.28, x + s * 0.58, y + s * 0.15, x + s * 0.72, y + s * 0.15);
        p.curveTo(x + s * 0.98, y + s * 0.15, x + s * 0.95, y + s * 0.55, x + s * 0.5, y + s * 0.88);
        g2.setColor(c);
        g2.fill(p);
        g2.dispose();
    }
}
