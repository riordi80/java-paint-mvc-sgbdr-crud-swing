package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import model.Figura;
import model.LienzoModel;

/**
 * CanvasPanel es nuestro “lienzo” (vista).
 * Recibe un LienzoModel y, en paintComponent, invoca a cada figura allí guardada.
 * 
 * Además, tendrá espacio para mostrar una figura “temporal” mientras el usuario
 * arrastra el ratón (por ejemplo, la línea o el círculo en proceso).
 */
public class CanvasPanel extends JPanel {
    private LienzoModel modelo;       // referencia al modelo
    private Figura figuraTemporal;    // figura que se está dibujando “en proceso”
    private Color colorFondo = Color.WHITE;

    public CanvasPanel() {
        // Tamaño preferido del lienzo
        setPreferredSize(new Dimension(800, 600));
        setBackground(colorFondo);

        // MouseMotionAdapter se usará más adelante para dibujar “dinámicamente”
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Por ahora, no hacemos nada; en Parte 2 usaremos también mouseMoved
                // para pintar la figura temporal mientras el usuario mueve el ratón.
            }
        });
    }

    /** 
     * Asigna el modelo (debe invocarse desde MainFrame justo después de crearlo).
     */
    public void setModel(LienzoModel modelo) {
        this.modelo = modelo;
        repaint();
    }

    /**
     * Permite que el controlador actualice la figura temporal
     * que queremos pintar entre el primer y segundo clic, etc.
     */
    public void setFiguraTemporal(Figura fTemp) {
        this.figuraTemporal = fTemp;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (modelo != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Primero, dibujamos todas las figuras definitivas almacenadas en el modelo
            for (Figura f : modelo.getFiguras()) {
                f.dibujar(g2);
            }
            // Luego, si existe una figura temporal (en proceso), la dibujamos encima
            if (figuraTemporal != null) {
                figuraTemporal.dibujar(g2);
            }
            g2.dispose();
        } else {
            // Si no hay modelo asignado, solo dejamos el fondo blanco
        }
    }

    /**
     * Reinicia (elimina) la figura temporal. El controlador llamará a esto
     * cuando la figura ya esté confirmada (segundo clic, fin de vértices, etc.).
     */
    public void clearFiguraTemporal() {
        this.figuraTemporal = null;
        repaint();
    }
}
