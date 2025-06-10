package model;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Representa un punto simple. Se dibuja como un pequeño círculo relleno.
 */
public class PuntoFigura implements Figura {
    private int x, y;           // coordenadas del punto
    private Color colorTrazo;   // color para pintar el punto (relleno)

    /**
     * Constructor básico.
     * @param x coordenada x
     * @param y coordenada y
     * @param colorTrazo color con el que pintaremos el punto
     */
    public PuntoFigura(int x, int y, Color colorTrazo) {
        this.x = x;
        this.y = y;
        this.colorTrazo = colorTrazo;
    }

    @Override
    public void dibujar(Graphics2D g) {
        // Dibujamos un círculo pequeñito de radio 3 px (por ejemplo)
        int r = 3;
        g.setColor(colorTrazo);
        // fillOval(x - r, y - r, ancho, alto) → dibuja un círculo centrado en (x,y)
        g.fillOval(x - r, y - r, r * 2, r * 2);
    }

    @Override
    public String toSVG() {
        // Convertimos el Color a "rgb(r,g,b)"
        String rgb = String.format("rgb(%d,%d,%d)", 
                colorTrazo.getRed(), 
                colorTrazo.getGreen(), 
                colorTrazo.getBlue());
        // Rayon pequeño (3 px). En SVG, <circle cx="..." cy="..." r="..." fill="..."/>
        return String.format(
            "<circle cx=\"%d\" cy=\"%d\" r=\"%d\" fill=\"%s\" />",
            x, y, 3, rgb
        );
    }

    // Getters y setters (para persistencia)
    public int getX() { return x; }
    public int getY() { return y; }
    public Color getColorTrazo() { return colorTrazo; }
}
