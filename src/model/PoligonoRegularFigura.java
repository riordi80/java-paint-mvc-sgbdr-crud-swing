package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * Representa un polígono regular de n lados. 
 * El ánguloInicio (en radianes) define la rotación del primer vértice.
 */
public class PoligonoRegularFigura implements Figura {
    private int centroX, centroY;
    private int radio;
    private int nLados;
    private double anguloInicio; // en radianes
    private Color colorTrazo;
    private Color colorRelleno;
    private boolean relleno;

    /**
     * @param centroX coordenada x del centro
     * @param centroY coordenada y del centro
     * @param radio distancia desde el centro a cada vértice
     * @param nLados número de lados (>= 3)
     * @param anguloInicio ángulo (en radianes) donde colocar el primer vértice
     * @param colorTrazo color del contorno
     * @param colorRelleno color de relleno
     * @param relleno si true, se rellena; si false, solo contorno
     */
    public PoligonoRegularFigura(int centroX, int centroY, int radio,
                                 int nLados, double anguloInicio,
                                 Color colorTrazo, Color colorRelleno, boolean relleno) {
        this.centroX = centroX;
        this.centroY = centroY;
        this.radio = radio;
        this.nLados = nLados;
        this.anguloInicio = anguloInicio;
        this.colorTrazo = colorTrazo;
        this.colorRelleno = colorRelleno;
        this.relleno = relleno;
    }

    @Override
    public void dibujar(Graphics2D g) {
        // Calculamos los vértices en arrays de int para usar Polygon
        int[] xs = new int[nLados];
        int[] ys = new int[nLados];

        double angInc = 2 * Math.PI / nLados; // incremento de ángulo entre vértices

        for (int i = 0; i < nLados; i++) {
            double ang = anguloInicio + i * angInc;
            xs[i] = centroX + (int) Math.round(radio * Math.cos(ang));
            ys[i] = centroY + (int) Math.round(radio * Math.sin(ang));
        }

        Polygon poly = new Polygon(xs, ys, nLados);
        if (relleno) {
            g.setColor(colorRelleno);
            g.fillPolygon(poly);
        }
        g.setColor(colorTrazo);
        g.drawPolygon(poly);
    }

    @Override
    public String toSVG() {
        String strokeRGB = String.format("rgb(%d,%d,%d)",
                colorTrazo.getRed(), colorTrazo.getGreen(), colorTrazo.getBlue());
        String fillRGB = relleno
                ? String.format("rgb(%d,%d,%d)",
                        colorRelleno.getRed(), colorRelleno.getGreen(), colorRelleno.getBlue())
                : "none";

        // Construir la lista "x1,y1 x2,y2 ... xn,yn"
        StringBuilder puntosSB = new StringBuilder();
        double angInc = 2 * Math.PI / nLados;
        for (int i = 0; i < nLados; i++) {
            double ang = anguloInicio + i * angInc;
            int vx = centroX + (int) Math.round(radio * Math.cos(ang));
            int vy = centroY + (int) Math.round(radio * Math.sin(ang));
            puntosSB.append(vx).append(",").append(vy);
            if (i < nLados - 1) puntosSB.append(" ");
        }

        // <polygon points="x1,y1 x2,y2 ... xn,yn" stroke="..." fill="..."/>
        return String.format(
            "<polygon points=\"%s\" stroke=\"%s\" fill=\"%s\" />",
            puntosSB.toString(), strokeRGB, fillRGB
        );
    }

    // Getters (para persistencia)
    public int getCentroX() { return centroX; }
    public int getCentroY() { return centroY; }
    public int getRadio() { return radio; }
    public int getnLados() { return nLados; }
    public double getAnguloInicio() { return anguloInicio; }
    public Color getColorTrazo() { return colorTrazo; }
    public Color getColorRelleno() { return colorRelleno; }
    public boolean isRelleno() { return relleno; }
}
