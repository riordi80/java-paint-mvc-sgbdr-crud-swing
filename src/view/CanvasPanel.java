package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import model.Figura;
import model.LienzoModel;

/**
 * CanvasPanel es nuestro “lienzo” (vista).
 * Recibe un LienzoModel y, en paintComponent, invoca a cada figura allí guardada.
 *
 * Además muestra cualquier figura temporal que el controlador establezca.
 */
public class CanvasPanel extends JPanel {
    private LienzoModel modelo;       // referencia al modelo
    private Figura figuraTemporal;    // figura que se está dibujando “en proceso”
    private Color colorFondo = Color.WHITE;

    public CanvasPanel() {
        // Tamaño preferido del lienzo
        setPreferredSize(new Dimension(800, 600));
        setBackground(colorFondo);
        // La detección de movimiento y clics se realiza en el controlador
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
     * que queremos pintar entre clics o arrastres.
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
            // Dibujamos todas las figuras del modelo
            for (Figura f : modelo.getFiguras()) {
                f.dibujar(g2);
            }
            // Si hay figura temporal, dibujarla encima
            if (figuraTemporal != null) {
                figuraTemporal.dibujar(g2);
            }
            g2.dispose();
        }
    }

    /**
     * Reinicia la figura temporal. Llamado por el controlador una vez
     * que la figura definitiva está almacenada en el modelo.
     */
    public void clearFiguraTemporal() {
        this.figuraTemporal = null;
        repaint();
    }
}
