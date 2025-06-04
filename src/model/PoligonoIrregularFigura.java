package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.List;

/**
 * Polígono irregular definido por una lista de puntos (vértices) en orden.
 * El constructor recibe la lista ya validada (sin auto‐intersecciones).
 */
public class PoligonoIrregularFigura implements Figura {
    private List<Point> vertices;   // lista de puntos (x,y) ordenados
    private Color colorTrazo;
    private Color colorRelleno;
    private boolean relleno;

    /**
     * @param vertices lista de puntos; primera posición = primer vértice, etc.
     * @param colorTrazo color del contorno
     * @param colorRelleno color de relleno
     * @param relleno si true, se rellena; si false, solo contorno
     */
    public PoligonoIrregularFigura(List<Point> vertices,
                                   Color colorTrazo, Color colorRelleno, boolean relleno) {
        // Supone que 'vertices' ya no tiene vértices repetidos y no hay auto‐intersecciones
        this.vertices = vertices;
        this.colorTrazo = colorTrazo;
        this.colorRelleno = colorRelleno;
        this.relleno = relleno;
    }

    @Override
    public void dibujar(Graphics2D g) {
        int n = vertices.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            Point p = vertices.get(i);
            xs[i] = p.x;
            ys[i] = p.y;
        }
        Polygon poly = new Polygon(xs, ys, n);
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

        StringBuilder puntosSB = new StringBuilder();
        for (int i = 0; i < vertices.size(); i++) {
            Point p = vertices.get(i);
            puntosSB.append(p.x).append(",").append(p.y);
            if (i < vertices.size() - 1) puntosSB.append(" ");
        }
        return String.format(
            "<polygon points=\"%s\" stroke=\"%s\" fill=\"%s\" />",
            puntosSB.toString(), strokeRGB, fillRGB
        );
    }

    // Getters (para persistencia)
    public List<Point> getVertices() { return vertices; }
    public Color getColorTrazo() { return colorTrazo; }
    public Color getColorRelleno() { return colorRelleno; }
    public boolean isRelleno() { return relleno; }
}
