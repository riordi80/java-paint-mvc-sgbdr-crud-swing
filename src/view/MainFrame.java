package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import model.CirculoFigura;
import model.Figura;
import model.LienzoModel;
import model.LineaFigura;
import model.PoligonoIrregularFigura;
import model.PoligonoRegularFigura;
import model.PuntoFigura;
import dao.DibujoDAO;
import dao.FiguraDAO;

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

    // Modelo en memoria
    private LienzoModel lienzoModel;

    // Nombre del dibujo actual (para título)
    private String nombreActual = "(sin nombre)";

    // Estado temporal para dibujar cada figura:
    private int x0, y0;
    private java.util.List<Point> verticesTemp = new ArrayList<>();
    private boolean enModoDibujar = false;
    private Figura figuraTemporal; // para pasarla a canvas

    public MainFrame() {
        super("Mi Paint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Mi Paint - Dibujo: " + nombreActual);
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

        // 1.4) Botones de Guardar/Cargar/Exportar
        gbc.gridy++;
        btnGuardar = new JButton("Guardar dibujo");
        btnGuardar.setEnabled(false); // Deshabilitado si no hay figuras
        panelControles.add(btnGuardar, gbc);
        gbc.gridy++;
        btnCargar = new JButton("Cargar dibujo");
        btnCargar.setEnabled(true);
        panelControles.add(btnCargar, gbc);
        gbc.gridy++;
        btnExportarSVG = new JButton("Exportar a SVG");
        btnExportarSVG.setEnabled(false); // Deshabilitado si no hay figuras
        panelControles.add(btnExportarSVG, gbc);

        // 1.5) Botón para “Finalizar pol. irr.”
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

        // 3) Asociar listeners
        configurarListeners();

        // 4) MouseAdapter para el canvas
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
        setLocationRelativeTo(null);
    }

    private void configurarListeners() {
        // 3.1) ComboFiguras
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

        // 3.2) Color Trazo
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

        // 3.3) Color Relleno
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

        // 3.4) SliderVertices (se quita println)
        sliderVertices.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Ya no imprimimos nada en consola
            }
        });

        // 3.5) Finalizar Polígono Irregular
        btnFinalizarPoligono.addActionListener(e -> {
            if (verticesTemp.size() < 3) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "Para un polígono irregular debes hacer al menos 3 clics."
                );
                return;
            }
            if (hayInterseccionEntreVertices(verticesTemp)) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "Los lados se cruzan. Corrige los puntos o vuelve a empezar."
                );
                return;
            }
            PoligonoIrregularFigura pIrr = new PoligonoIrregularFigura(
                new ArrayList<>(verticesTemp),
                colorTrazo, colorRelleno, true
            );
            lienzoModel.agregarFigura(pIrr);
            verticesTemp.clear();
            enModoDibujar = false;
            canvas.clearFiguraTemporal();
            canvas.repaint();
            actualizarBotones(); // Habilitar guardar/exportar
        });

        // 3.6) Guardar dibujo
        btnGuardar.addActionListener(e -> {
            if (lienzoModel.getFiguras().isEmpty()) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "No hay figuras para guardar."
                );
                return;
            }
            String nombre = JOptionPane.showInputDialog(
                MainFrame.this,
                "Introduce un nombre para este dibujo:"
            );
            if (nombre == null) {
                return;
            }
            nombre = nombre.trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "El nombre no puede estar vacío."
                );
                return;
            }
            // Prohibir caracteres inválidos en el nombre
            Pattern pat = Pattern.compile("[\\/:*?\"<>|]");
            if (pat.matcher(nombre).find()) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "El nombre contiene caracteres inválidos."
                );
                return;
            }
            try {
                DibujoDAO dibujoDAO = new DibujoDAO();
                FiguraDAO figuraDAO = new FiguraDAO();
                int idDibujo;
                try {
                    idDibujo = dibujoDAO.crearDibujo(nombre);
                } catch (SQLException ex) {
                    if (ex.getMessage().contains("Duplicate") || ex.getErrorCode() == 1062) {
                        int resp = JOptionPane.showConfirmDialog(
                            MainFrame.this,
                            "El nombre ya existe. ¿Deseas sobrescribir el dibujo existente?",
                            "Confirmar sobrescritura",
                            JOptionPane.YES_NO_OPTION
                        );
                        if (resp == JOptionPane.YES_OPTION) {
                            idDibujo = dibujoDAO.obtenerIdPorNombre(nombre);
                            figuraDAO.eliminarFigurasDeDibujo(idDibujo);
                        } else {
                            return;
                        }
                    } else {
                        throw ex;
                    }
                }
                List<Figura> figs = lienzoModel.getFiguras();
                for (int i = 0; i < figs.size(); i++) {
                    figuraDAO.guardarFigura(idDibujo, figs.get(i), i);
                }
                nombreActual = nombre;
                setTitle("Mi Paint - Dibujo: " + nombreActual); // Actualizar título
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "Dibujo '" + nombre + "' guardado correctamente."
                );
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "Error al guardar dibujo: " + ex.getMessage()
                );
                ex.printStackTrace();
            }
        });

        // 3.7) Cargar dibujo
        btnCargar.addActionListener(e -> {
            if (!lienzoModel.getFiguras().isEmpty()) {
                int resp2 = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Hay un dibujo sin guardar. ¿Deseas perder los cambios y cargar otro?",
                    "Confirmar pérdida de datos",
                    JOptionPane.YES_NO_OPTION
                );
                if (resp2 != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            try {
                DibujoDAO dibujoDAO = new DibujoDAO();
                FiguraDAO figuraDAO = new FiguraDAO();
                java.util.List<String> nombres = dibujoDAO.listarNombresDibujos();
                if (nombres.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        MainFrame.this,
                        "No hay dibujos guardados."
                    );
                    return;
                }
                String seleccionado = (String) JOptionPane.showInputDialog(
                    MainFrame.this,
                    "Selecciona un dibujo:",
                    "Cargar dibujo",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    nombres.toArray(),
                    nombres.get(0)
                );
                if (seleccionado == null) {
                    return;
                }
                int idDibujo = dibujoDAO.obtenerIdPorNombre(seleccionado);
                if (idDibujo < 0) {
                    JOptionPane.showMessageDialog(
                        MainFrame.this,
                        "Error: no se encontró ese dibujo en la base de datos."
                    );
                    return;
                }
                java.util.List<Figura> figs = figuraDAO.cargarFigurasPorDibujo(idDibujo);
                lienzoModel.clear();
                for (Figura f : figs) {
                    lienzoModel.agregarFigura(f);
                }
                nombreActual = seleccionado;
                setTitle("Mi Paint - Dibujo: " + nombreActual); // Actualizar título
                canvas.clearFiguraTemporal();
                canvas.repaint();
                actualizarBotones(); // Habilitar guardar/exportar tras cargar
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "Error al cargar dibujo: " + ex.getMessage()
                );
                ex.printStackTrace();
            }
        });

        // 3.8) Exportar a SVG
        btnExportarSVG.addActionListener(e -> {
            if (lienzoModel.getFiguras().isEmpty()) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "No hay figuras para exportar."
                );
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar como SVG");
            FileNameExtensionFilter filtro = new FileNameExtensionFilter(
                "Archivos SVG", "svg"
            );
            chooser.setFileFilter(filtro);

            int seleccion = chooser.showSaveDialog(MainFrame.this);
            if (seleccion != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File archivo = chooser.getSelectedFile();
            String ruta = archivo.getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".svg")) {
                archivo = new File(ruta + ".svg");
            }
            String svgContent = generarSVG();
            try (FileWriter writer = new FileWriter(archivo)) {
                writer.write(svgContent);
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "SVG guardado en: " + archivo.getAbsolutePath()
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "Error al guardar SVG: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });
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
                actualizarBotones(); // Habilitar guardar/exportar al agregar
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
                    actualizarBotones(); // Habilitar guardar/exportar al agregar
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
                    actualizarBotones();
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
                    actualizarBotones();
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
     * Construye el contenido SVG para todas las figuras actuales en el modelo.
     * @return String con el XML completo del SVG.
     */
    private String generarSVG() {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(String.format(
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\">\n",
            width, height
        ));

        for (Figura f : lienzoModel.getFiguras()) {
            sb.append("  ");
            sb.append(f.toSVG());
            sb.append("\n");
        }

        sb.append("</svg>\n");

        return sb.toString();
    }

    /**
     * Actualiza el estado (habilitado/deshabilitado) de los botones Guardar y Exportar
     * según haya o no figuras en el lienzo.
     */
    private void actualizarBotones() {                            // Nuevo método
        boolean hayFiguras = !lienzoModel.getFiguras().isEmpty();  // 409-a
        btnGuardar.setEnabled(hayFiguras);                        // 409-b
        btnExportarSVG.setEnabled(hayFiguras);                    // 409-c
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();
            mf.setVisible(true);
        });
    }
}