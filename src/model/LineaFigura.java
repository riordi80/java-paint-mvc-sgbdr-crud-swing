package model;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Representa una línea entre (x1,y1) y (x2,y2) con un color de trazo.
 */
public class LineaFigura implements Figura {
    private int x1, y1, x2, y2;
    private Color colorTrazo;

    /**
     * @param x1 coordenada x del primer extremo
     * @param y1 coordenada y del primer extremo
     * @param x2 coordenada x del segundo extremo
     * @param y2 coordenada y del segundo extremo
     * @param colorTrazo color para dibujar la línea
     */
    public LineaFigura(int x1, int y1, int x2, int y2, Color colorTrazo) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.colorTrazo = colorTrazo;
    }

    @Override
    public void dibujar(Graphics2D g) {
        g.setColor(colorTrazo);
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public String toSVG() {
        String rgb = String.format("rgb(%d,%d,%d)", 
                colorTrazo.getRed(), 
                colorTrazo.getGreen(), 
                colorTrazo.getBlue());
        // SVG: <line x1="..." y1="..." x2="..." y2="..." stroke="rgb(...)" stroke-width="1"/>
        return String.format(
            "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"1\" />",
            x1, y1, x2, y2, rgb
        );
    }

    // Getters para persistencia más adelante:
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public Color getColorTrazo() { return colorTrazo; }
}
