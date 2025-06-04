package model;

import java.awt.Graphics2D;

/**
 * Interfaz base para todas las figuras dibujables.
 * Define al menos:
 *  - dibujar(Graphics2D g): para pintarla en pantalla
 *  - toSVG(): para generar la cadena SVG correspondiente
 */
public interface Figura {
    /**
     * Dibuja esta figura en el contexto Graphics2D proporcionado.
     * 
     * @param g el contexto gráfico donde pintar
     */
    void dibujar(Graphics2D g);

    /**
     * Devuelve la representación SVG de esta figura (solo la etiqueta <.../> correspondiente).
     * 
     * @return cadena con la etiqueta SVG (por ejemplo: "<line ... />")
     */
    String toSVG();
}
