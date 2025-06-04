package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * CanvasPanel es nuestro “lienzo” blanco. Por ahora, guarda una lista
 * de puntos clicados y dibuja un pequeño círculo en cada clic.
 */
public class CanvasPanel extends JPanel {
    // Lista de puntos donde el usuario hizo clic
    private final List<Point> puntos = new ArrayList<>();

    public CanvasPanel() {
        // Preferimos un tamaño razonable de lienzo, p. ej. 800×600
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        // Añadimos un MouseAdapter que en cada clic guarda la posición
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Guardamos la posición clicada
                puntos.add(e.getPoint());
                // Pedimos repintado
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Para cada punto, dibujamos un pequeño círculo de radio fijo (p.ej. 4 px)
        g2.setColor(Color.RED); // color de prueba para el marcador
        for (Point p : puntos) {
            int r = 4;
            // Dibujamos un círculo centrado en (p.x, p.y)
            g2.fillOval(p.x - r, p.y - r, r * 2, r * 2);
        }

        g2.dispose();
    }

    /**
     * Método para limpiar todos los puntos (por si hiciera falta más tarde).
     */
    public void clear() {
        puntos.clear();
        repaint();
    }
}
