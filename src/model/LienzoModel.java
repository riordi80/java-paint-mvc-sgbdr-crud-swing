package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LienzoModel actúa como “modelo” en la BD: es simplemente una lista de Figuras.
 */
public class LienzoModel {
    // Lista interna con las figuras dibujadas en el orden en que se agregan
    private final List<Figura> figuras;

    public LienzoModel() {
        this.figuras = new ArrayList<>();
    }

    /**
     * Devuelve una vista inmutable de la lista de figuras; la Vista la usará para pintar.
     */
    public List<Figura> getFiguras() {
        return Collections.unmodifiableList(figuras);
    }

    /**
     * Agrega una figura al final de la lista. 
     * Se asume que la figura ya está correctamente construida (parámetros válidos).
     */
    public void agregarFigura(Figura f) {
        if (f != null) {
            figuras.add(f);
        }
    }

    /**
     * Limpia todas las figuras del lienzo (vacía la lista).
     */
    public void clear() {
        figuras.clear();
    }

    /**
     * Número de figuras actuales (útil para llevar orden, si hiciera falta).
     */
    public int getCantidadFiguras() {
        return figuras.size();
    }
}
