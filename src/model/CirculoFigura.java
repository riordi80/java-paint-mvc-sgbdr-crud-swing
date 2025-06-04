package model;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Representa un círculo (o circunferencia) dado centro (x,y), radio y colores.
 * Permite opcionalmente rellenarlo o solo trazarlo.
 */
public class CirculoFigura implements Figura {
    private int centroX, centroY;
    private int radio;
    private Color colorTrazo;
    private Color colorRelleno;
    private boolean relleno;  // true = rellenar, false = solo contorno

    /**
     * @param centroX coordenada x del centro
     * @param centroY coordenada y del centro
     * @param radio longitud del radio
     * @param colorTrazo color del contorno
     * @param colorRelleno color de relleno (si relleno == true)
     * @param relleno si es true, hará fill+draw; si false, solo draw
     */
    public CirculoFigura(int centroX, int centroY, int radio,
                         Color colorTrazo, Color colorRelleno, boolean relleno) {
        this.centroX = centroX;
        this.centroY = centroY;
        this.radio = radio;
        this.colorTrazo = colorTrazo;
        this.colorRelleno = colorRelleno;
        this.relleno = relleno;
    }

    @Override
    public void dibujar(Graphics2D g) {
        int x = centroX - radio;
        int y = centroY - radio;
        int diam = radio * 2;

        if (relleno) {
            g.setColor(colorRelleno);
            g.fillOval(x, y, diam, diam);
        }
        g.setColor(colorTrazo);
        g.drawOval(x, y, diam, diam);
    }

    @Override
    public String toSVG() {
        String strokeRGB = String.format("rgb(%d,%d,%d)",
                colorTrazo.getRed(), colorTrazo.getGreen(), colorTrazo.getBlue());
        String fillRGB = relleno
                ? String.format("rgb(%d,%d,%d)", 
                        colorRelleno.getRed(), colorRelleno.getGreen(), colorRelleno.getBlue())
                : "none";

        // <circle cx="..." cy="..." r="..." stroke="..." fill="..."/>
        return String.format(
            "<circle cx=\"%d\" cy=\"%d\" r=\"%d\" stroke=\"%s\" fill=\"%s\" />",
            centroX, centroY, radio, strokeRGB, fillRGB
        );
    }

    // Getters para persistencia:
    public int getCentroX() { return centroX; }
    public int getCentroY() { return centroY; }
    public int getRadio() { return radio; }
    public Color getColorTrazo() { return colorTrazo; }
    public Color getColorRelleno() { return colorRelleno; }
    public boolean isRelleno() { return relleno; }
}
