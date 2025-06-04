package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import model.CirculoFigura;
import model.Figura;
import model.LienzoModel;
import model.LineaFigura;
import model.PoligonoIrregularFigura;
import model.PoligonoRegularFigura;
import model.PuntoFigura;

public class MainFrame extends JFrame {
    // Componentes Swing
    private final CanvasPanel canvas;
    private final JComboBox<String> comboFiguras;
    private final JSlider sliderVertices;
    private final JButton btnColorTrazo;
    private final JButton btnColorRelleno;
    private final JButton btnGuardar;
    private final JButton btnCargar;
    private final JButton btnExportarSVG;
    private final JButton btnFinalizarPoligono;

    // Colores seleccionados actualmente
    private Color colorTrazo = Color.BLACK;
    private Color colorRelleno = Color.WHITE;

    // Modelo en memoria (Parte 2)
    private LienzoModel lienzoModel;

    // Estado temporal para dibujar cada figura:
    private int x0, y0; 
    private java.util.List<Point> verticesTemp = new ArrayList<>();
    private boolean enModoDibujar = false;
    private Figura figuraTemporal; // para pasarla a canvas

    public MainFrame() {
        super("Mi Paint en Swing (parte 2)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1) Creamos el panel de controles (a la izquierda)
        JPanel panelControles = new JPanel(new GridBagLayout());
        panelControles.setBorder(BorderFactory.createTitledBorder("Controles"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 1.1) ComboBox de figuras
        panelControles.add(new JLabel("Figura:"), gbc);
        gbc.gridy++;
        comboFiguras = new JComboBox<>(
                new String[] { "Punto", "Línea", "Circunferencia", "Pol. regular", "Pol. irregular" }
        );
        panelControles.add(comboFiguras, gbc);

        // 1.2) Slider de vértices (inicialmente deshabilitado)
        gbc.gridy++;
        panelControles.add(new JLabel("Vértices (solo pol. reg):"), gbc);
        gbc.gridy++;
        sliderVertices = new JSlider(SwingConstants.HORIZONTAL, 3, 12, 5);
        sliderVertices.setEnabled(false);
        sliderVertices.setMajorTickSpacing(1);
        sliderVertices.setPaintTicks(true);
        sliderVertices.setPaintLabels(true);
        panelControles.add(sliderVertices, gbc);

        // 1.3) Botones de color
        gbc.gridy++;
        btnColorTrazo = new JButton("Color de trazo");
        panelControles.add(btnColorTrazo, gbc);
        gbc.gridy++;
        btnColorRelleno = new JButton("Color de relleno");
        panelControles.add(btnColorRelleno, gbc);

        // 1.4) Botones de Guardar/Cargar/Exportar (deshabilitados por ahora)
        gbc.gridy++;
        btnGuardar = new JButton("Guardar dibujo");
        btnGuardar.setEnabled(false);
        panelControles.add(btnGuardar, gbc);
        gbc.gridy++;
        btnCargar = new JButton("Cargar dibujo");
        btnCargar.setEnabled(false);
        panelControles.add(btnCargar, gbc);
        gbc.gridy++;
        btnExportarSVG = new JButton("Exportar a SVG");
        btnExportarSVG.setEnabled(false);
        panelControles.add(btnExportarSVG, gbc);

        // 1.5) Botón para “Finalizar pol. irr.” (inicialmente deshabilitado)
        gbc.gridy++;
        btnFinalizarPoligono = new JButton("Finalizar pol. irr.");
        btnFinalizarPoligono.setEnabled(false);
        panelControles.add(btnFinalizarPoligono, gbc);

        // Añadimos el panelControles al MainFrame
        add(panelControles, BorderLayout.WEST);

        // 2) Creamos el CanvasPanel y lo añadimos al centro
        canvas = new CanvasPanel();
        lienzoModel = new LienzoModel();
        canvas.setModel(lienzoModel);
        add(canvas, BorderLayout.CENTER);

        // 3) Asociar listeners a comboFiguras, slider, botones de color y finalizar pol. irr.
        configurarListeners();

        // 4) Asociar MouseAdapter a canvas para captura de clics y movimiento
        canvas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                manejarMouseClicked(e.getX(), e.getY());
            }
        });
        canvas.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                manejarMouseMoved(e.getX(), e.getY());
            }
        });

        pack();
        setLocationRelativeTo(null); // centrar en pantalla
    }

    private void configurarListeners() {
        // 3.1) Cuando cambia la selección en comboFiguras
        comboFiguras.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String seleccionado = (String) comboFiguras.getSelectedItem();
                boolean esPolRegular = "Pol. regular".equals(seleccionado);
                sliderVertices.setEnabled(esPolRegular);

                boolean esPolIrregular = "Pol. irregular".equals(seleccionado);
                btnFinalizarPoligono.setEnabled(esPolIrregular);
                if (!esPolIrregular) {
                    verticesTemp.clear();
                    canvas.clearFiguraTemporal();
                    enModoDibujar = false;
                }
            }
        });

        // 3.2) Botón Color Trazo
        btnColorTrazo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color elegido = JColorChooser.showDialog(
                        MainFrame.this,
                        "Elige color de trazo", colorTrazo
                );
                if (elegido != null) {
                    colorTrazo = elegido;
                }
            }
        });

        // 3.3) Botón Color Relleno
        btnColorRelleno.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color elegido = JColorChooser.showDialog(
                        MainFrame.this,
                        "Elige color de relleno", colorRelleno
                );
                if (elegido != null) {
                    colorRelleno = elegido;
                }
            }
        });

        // 3.4) Slider (opcional): solo imprime el valor
        sliderVertices.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int nLados = sliderVertices.getValue();
                System.out.println("Slider vertices = " + nLados);
            }
        });

        // 3.5) Botón “Finalizar pol. irr.”
        btnFinalizarPoligono.addActionListener(e -> {
            if (verticesTemp.size() < 3) {
                javax.swing.JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "Para un polígono irregular debes hacer al menos 3 clics."
                );
                return;
            }
            if (hayInterseccionEntreVertices(verticesTemp)) {
                javax.swing.JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "Los lados se cruzan. Corrige los puntos o vuelve a empezar."
                );
                return;
            }
            // Polígono válido: lo agregamos al modelo
            PoligonoIrregularFigura pIrr = new PoligonoIrregularFigura(
                new ArrayList<>(verticesTemp),
                colorTrazo, colorRelleno, true
            );
            lienzoModel.agregarFigura(pIrr);
            verticesTemp.clear();
            enModoDibujar = false;
            canvas.clearFiguraTemporal();
            canvas.repaint();
        });

        // 3.6) Botones Guardar/Cargar/Exportar no implementados
        btnGuardar.addActionListener(e ->
            javax.swing.JOptionPane.showMessageDialog(
                MainFrame.this,
                "Funcionalidad de \"Guardar\" aún no implementada."
            )
        );
        btnCargar.addActionListener(e ->
            javax.swing.JOptionPane.showMessageDialog(
                MainFrame.this,
                "Funcionalidad de \"Cargar\" aún no implementada."
            )
        );
        btnExportarSVG.addActionListener(e ->
            javax.swing.JOptionPane.showMessageDialog(
                MainFrame.this,
                "Funcionalidad de \"Exportar a SVG\" aún no implementada."
            )
        );
    }

    private void manejarMouseClicked(int x, int y) {
        String modo = (String) comboFiguras.getSelectedItem();
        Color cTrazo = colorTrazo;
        Color cRelleno = colorRelleno;
        boolean rell = (modo.equals("Pol. regular") || modo.equals("Pol. irregular") || modo.equals("Circunferencia"))
                       ? true : false;

        switch (modo) {
            case "Punto":
                PuntoFigura pf = new PuntoFigura(x, y, cTrazo);
                lienzoModel.agregarFigura(pf);
                canvas.clearFiguraTemporal();
                canvas.repaint();
                break;

            case "Línea":
                if (!enModoDibujar) {
                    x0 = x; 
                    y0 = y;
                    enModoDibujar = true;
                    figuraTemporal = new LineaFigura(x0, y0, x0, y0, cTrazo);
                    canvas.setFiguraTemporal(figuraTemporal);
                } else {
                    LineaFigura lf = new LineaFigura(x0, y0, x, y, cTrazo);
                    lienzoModel.agregarFigura(lf);
                    enModoDibujar = false;
                    canvas.clearFiguraTemporal();
                    canvas.repaint();
                }
                break;

            case "Circunferencia":
                if (!enModoDibujar) {
                    x0 = x; 
                    y0 = y;
                    enModoDibujar = true;
                    figuraTemporal = new CirculoFigura(x0, y0, 0, cTrazo, cRelleno, rell);
                    canvas.setFiguraTemporal(figuraTemporal);
                } else {
                    int dx = x - x0;
                    int dy = y - y0;
                    int radio = (int) Math.round(Math.hypot(dx, dy));
                    CirculoFigura cf = new CirculoFigura(x0, y0, radio, cTrazo, cRelleno, rell);
                    lienzoModel.agregarFigura(cf);
                    enModoDibujar = false;
                    canvas.clearFiguraTemporal();
                    canvas.repaint();
                }
                break;

            case "Pol. regular":
                if (!enModoDibujar) {
                    x0 = x;
                    y0 = y;
                    enModoDibujar = true;
                    int nL = sliderVertices.getValue();
                    figuraTemporal = new PoligonoRegularFigura(x0, y0, 0, nL, 0.0, cTrazo, cRelleno, rell);
                    canvas.setFiguraTemporal(figuraTemporal);
                } else {
                    int dx = x - x0;
                    int dy = y - y0;
                    int radio = (int) Math.round(Math.hypot(dx, dy));
                    double angulo = Math.atan2(dy, dx);
                    int nL = sliderVertices.getValue();
                    PoligonoRegularFigura prf = new PoligonoRegularFigura(
                        x0, y0, radio, nL, angulo, cTrazo, cRelleno, rell
                    );
                    lienzoModel.agregarFigura(prf);
                    enModoDibujar = false;
                    canvas.clearFiguraTemporal();
                    canvas.repaint();
                }
                break;

            case "Pol. irregular":
                verticesTemp.add(new Point(x, y));
                if (verticesTemp.size() >= 2) {
                    PoligonoIrregularFigura pirTemp = new PoligonoIrregularFigura(
                        verticesTemp, cTrazo, cRelleno, rell
                    );
                    canvas.setFiguraTemporal(pirTemp);
                }
                break;
        }
    }

    private void manejarMouseMoved(int x, int y) {
        if (!enModoDibujar) return;

        String modo = (String) comboFiguras.getSelectedItem();
        Color cTrazo = colorTrazo;
        Color cRelleno = colorRelleno;
        boolean rell = (modo.equals("Pol. regular") || modo.equals("Circunferencia"))
                       ? true : false;

        switch (modo) {
            case "Línea":
                figuraTemporal = new LineaFigura(x0, y0, x, y, cTrazo);
                canvas.setFiguraTemporal(figuraTemporal);
                break;
            case "Circunferencia":
                int dx = x - x0;
                int dy = y - y0;
                int radio = (int) Math.round(Math.hypot(dx, dy));
                figuraTemporal = new CirculoFigura(x0, y0, radio, cTrazo, cRelleno, rell);
                canvas.setFiguraTemporal(figuraTemporal);
                break;
            case "Pol. regular":
                dx = x - x0;
                dy = y - y0;
                radio = (int) Math.round(Math.hypot(dx, dy));
                double angulo = Math.atan2(dy, dx);
                int nL = sliderVertices.getValue();
                figuraTemporal = new PoligonoRegularFigura(
                    x0, y0, radio, nL, angulo, cTrazo, cRelleno, rell
                );
                canvas.setFiguraTemporal(figuraTemporal);
                break;
            default:
                break;
        }
    }

    /**
     * Comprueba si la lista de vertices (en orden) tiene algún par
     * de segmentos que se cruza (ignora adyacentes).
     */
    private boolean hayInterseccionEntreVertices(java.util.List<Point> v) {
        int n = v.size();
        if (n < 4) return false;

        for (int i = 0; i < n - 1; i++) {
            Point a1 = v.get(i);
            Point a2 = v.get(i + 1);
            for (int j = i + 2; j < n - 1; j++) {
                if (i == 0 && j == n - 2) continue;
                Point b1 = v.get(j);
                Point b2 = v.get(j + 1);
                if (segmentosSeIntersectan(
                        a1.x, a1.y, a2.x, a2.y,
                        b1.x, b1.y, b2.x, b2.y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determina si dos segmentos (x1,y1)-(x2,y2) y (x3,y3)-(x4,y4) se cruzan.
     */
    private boolean segmentosSeIntersectan(
            int x1, int y1, int x2, int y2,
            int x3, int y3, int x4, int y4) {
        java.util.function.BiFunction<Point, Point, Point> sub = 
            (p, q) -> new Point(p.x - q.x, p.y - q.y);
        java.util.function.BiFunction<Point, Point, Long> cross = 
            (p, q) -> (long) p.x * q.y - (long) p.y * q.x;

        Point A = new Point(x1, y1);
        Point B = new Point(x2, y2);
        Point C = new Point(x3, y3);
        Point D = new Point(x4, y4);

        Point AB = sub.apply(B, A);
        Point AC = sub.apply(C, A);
        Point AD = sub.apply(D, A);
        Point CD = sub.apply(D, C);
        Point CA = sub.apply(A, C);
        Point CB = sub.apply(B, C);

        long cross1 = cross.apply(AB, AC);
        long cross2 = cross.apply(AB, AD);
        long cross3 = cross.apply(CD, CA);
        long cross4 = cross.apply(CD, CB);

        if ((cross1 > 0 && cross2 < 0 || cross1 < 0 && cross2 > 0) &&
            (cross3 > 0 && cross4 < 0 || cross3 < 0 && cross4 > 0)) {
            return true;
        }
        return false;
    }

    /**
     * Método main para arrancar la aplicación.
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();
            mf.setVisible(true);
        });
    }
}
