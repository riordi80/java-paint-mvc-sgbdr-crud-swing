package controller;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.MouseInputAdapter;

import model.CirculoFigura;
import model.Figura;
import model.LienzoModel;
import model.LineaFigura;
import model.PoligonoIrregularFigura;
import model.PoligonoRegularFigura;
import model.PuntoFigura;
import view.CanvasPanel;
import view.MainFrame;
import dao.DibujoDAO;
import dao.FiguraDAO;

/**
 * MainController: Registra todos los listeners y coordina 
 * Model (LienzoModel, DAOs) y View (MainFrame, CanvasPanel).
 */
public class MainController {
    private final MainFrame view;
    private final LienzoModel model;

    // Colores seleccionados actualmente:
    private Color colorTrazo = Color.BLACK;
    private Color colorRelleno = Color.WHITE;

    // Estado temporal para dibujar cada figura:
    private int x0, y0;
    private java.util.List<Point> verticesTemp = new java.util.ArrayList<>();
    private boolean enModoDibujar = false;
    private Figura figuraTemporal; // se manda al canvas

    public MainController(MainFrame view) {
        this.view = view;
        this.model = view.getLienzoModel();

        // Registrar todos los listeners:
        registrarComponentes();
    }

    private void registrarComponentes() {
        // 1) Listener para comboFiguras
        JComboBox<String> combo = view.getComboFiguras();
        combo.addActionListener(e -> {
            String seleccionado = (String) combo.getSelectedItem();
            boolean esPolRegular = "Pol. regular".equals(seleccionado);
            view.getSliderVertices().setEnabled(esPolRegular);

            boolean esPolIrregular = "Pol. irregular".equals(seleccionado);
            view.getBtnFinalizarPoligono().setEnabled(esPolIrregular);
            if (!esPolIrregular) {
                verticesTemp.clear();
                view.getCanvas().clearFiguraTemporal();
                enModoDibujar = false;
            }
        });

        // 2) Botón Color Trazo
        JButton btnColorTrazo = view.getBtnColorTrazo();
        btnColorTrazo.addActionListener(e -> {
            Color elegido = JColorChooser.showDialog(
                view,
                "Elige color de trazo", colorTrazo
            );
            if (elegido != null) {
                colorTrazo = elegido;
            }
        });

        // 3) Botón Color Relleno
        JButton btnColorRelleno = view.getBtnColorRelleno();
        btnColorRelleno.addActionListener(e -> {
            Color elegido = JColorChooser.showDialog(
                view,
                "Elige color de relleno", colorRelleno
            );
            if (elegido != null) {
                colorRelleno = elegido;
            }
        });

        // 4) Botón “Finalizar Pol. Irr.”
        JButton btnFinalizar = view.getBtnFinalizarPoligono();
        btnFinalizar.addActionListener(e -> {
            if (verticesTemp.size() < 3) {
                JOptionPane.showMessageDialog(
                    view,
                    "Para un polígono irregular debes hacer al menos 3 clics."
                );
                return;
            }

            // Si hay intersección, pedir confirmación sobre guardado
            if (hayInterseccionEntreVertices(verticesTemp)) {
                int resp = JOptionPane.showConfirmDialog(
                    view,
                    "Los lados se cruzan: esto deja de ser un polígono irregular.\n" +
                    "¿Deseas guardarlo de todas formas?",
                    "Intersección detectada",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (resp != JOptionPane.YES_OPTION) {
                    // El usuario ha dicho NO → cancelar y reiniciar el polígono
                    verticesTemp.clear();
                    enModoDibujar = false;
                    view.getCanvas().clearFiguraTemporal();
                    return;
                }
            }

            // Si llega aquí, o no había intersección, creamos el polígono
            PoligonoIrregularFigura pIrr = new PoligonoIrregularFigura(
                new ArrayList<>(verticesTemp),
                colorTrazo, colorRelleno, true
            );
            model.agregarFigura(pIrr);
            verticesTemp.clear();
            enModoDibujar = false;
            view.getCanvas().clearFiguraTemporal();
            view.getCanvas().repaint();
            actualizarBotones();
        });

        // 5) Botón “Guardar dibujo”
        JButton btnGuardar = view.getBtnGuardar();
        btnGuardar.addActionListener(e -> {
            if (model.getFiguras().isEmpty()) {
                JOptionPane.showMessageDialog(
                    view,
                    "No hay figuras para guardar."
                );
                return;
            }
            String nombre = JOptionPane.showInputDialog(
                view,
                "Introduce un nombre para este dibujo:"
            );
            if (nombre == null) return; 
            nombre = nombre.trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(
                    view,
                    "El nombre no puede estar vacío."
                );
                return;
            }
            // Prohibir caracteres inválidos
            Pattern pat = Pattern.compile("[\\\\/:*?\"<>|]");
            if (pat.matcher(nombre).find()) {
                JOptionPane.showMessageDialog(
                    view,
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
                        int resp2 = JOptionPane.showConfirmDialog(
                            view,
                            "El nombre ya existe. ¿Deseas sobrescribir el dibujo existente?",
                            "Confirmar sobrescritura",
                            JOptionPane.YES_NO_OPTION
                        );
                        if (resp2 == JOptionPane.YES_OPTION) {
                            idDibujo = dibujoDAO.obtenerIdPorNombre(nombre);
                            figuraDAO.eliminarFigurasDeDibujo(idDibujo);
                        } else {
                            return;
                        }
                    } else {
                        throw ex;
                    }
                }
                List<Figura> figs = model.getFiguras();
                for (int i = 0; i < figs.size(); i++) {
                    figuraDAO.guardarFigura(idDibujo, figs.get(i), i);
                }
                view.setNombreActual(nombre);
                JOptionPane.showMessageDialog(
                    view,
                    "Dibujo '" + nombre + "' guardado correctamente."
                );
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    view,
                    "Error al guardar dibujo: " + ex.getMessage()
                );
                ex.printStackTrace();
            }
        });

        // 6) Botón “Cargar dibujo”
        JButton btnCargar = view.getBtnCargar();
        btnCargar.addActionListener(e -> {
            if (!model.getFiguras().isEmpty()) {
                int resp2 = JOptionPane.showConfirmDialog(
                    view,
                    "Hay un dibujo sin guardar. ¿Deseas perder los cambios y cargar otro?",
                    "Confirmar pérdida de datos",
                    JOptionPane.YES_NO_OPTION
                );
                if (resp2 != JOptionPane.YES_OPTION) return;
            }
            try {
                DibujoDAO dibujoDAO = new DibujoDAO();
                FiguraDAO figuraDAO = new FiguraDAO();
                java.util.List<String> nombres = dibujoDAO.listarNombresDibujos();
                if (nombres.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        view,
                        "No hay dibujos guardados."
                    );
                    return;
                }
                String seleccionado = (String) JOptionPane.showInputDialog(
                    view,
                    "Selecciona un dibujo:",
                    "Cargar dibujo",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    nombres.toArray(),
                    nombres.get(0)
                );
                if (seleccionado == null) return;
                int idDibujo = dibujoDAO.obtenerIdPorNombre(seleccionado);
                if (idDibujo < 0) {
                    JOptionPane.showMessageDialog(
                        view,
                        "Error: no se encontró ese dibujo en la base de datos."
                    );
                    return;
                }
                java.util.List<Figura> figs = figuraDAO.cargarFigurasPorDibujo(idDibujo);
                model.clear();
                for (Figura f : figs) {
                    model.agregarFigura(f);
                }
                view.setNombreActual(seleccionado);
                view.getCanvas().clearFiguraTemporal();
                view.getCanvas().repaint();
                actualizarBotones();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    view,
                    "Error al cargar dibujo: " + ex.getMessage()
                );
                ex.printStackTrace();
            }
        });

        // 7) Botón “Exportar a SVG”
        JButton btnExportar = view.getBtnExportarSVG();
        btnExportar.addActionListener(e -> {
            if (model.getFiguras().isEmpty()) {
                JOptionPane.showMessageDialog(
                    view,
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

            int seleccion = chooser.showSaveDialog(view);
            if (seleccion != JFileChooser.APPROVE_OPTION) return;
            File archivo = chooser.getSelectedFile();
            String ruta = archivo.getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".svg")) {
                archivo = new File(ruta + ".svg");
            }
            String svgContent = generarSVG(view.getCanvas(), model);
            try (FileWriter writer = new FileWriter(archivo)) {
                writer.write(svgContent);
                JOptionPane.showMessageDialog(
                    view,
                    "SVG guardado en: " + archivo.getAbsolutePath()
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    view,
                    "Error al guardar SVG: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });

        // 8) MouseListener & MouseMotionListener para el canvas
        CanvasPanel canvas = view.getCanvas();
        canvas.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                manejarMouseClicked(e.getX(), e.getY(), canvas);
            }
        });
        canvas.addMouseMotionListener(new MouseInputAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                manejarMouseMoved(e.getX(), e.getY(), canvas);
            }
        });
    }

    private void manejarMouseClicked(int x, int y, CanvasPanel canvas) {
        String modo = (String) view.getComboFiguras().getSelectedItem();
        Color cT = colorTrazo;
        Color cR = colorRelleno;
        boolean rell = modo.equals("Pol. regular") 
                    || modo.equals("Pol. irregular") 
                    || modo.equals("Circunferencia");

        switch (modo) {
            case "Punto":
                PuntoFigura pf = new PuntoFigura(x, y, cT);
                model.agregarFigura(pf);
                canvas.clearFiguraTemporal();
                canvas.repaint();
                actualizarBotones();
                break;

            case "Línea":
                if (!enModoDibujar) {
                    x0 = x; y0 = y;
                    enModoDibujar = true;
                    figuraTemporal = new LineaFigura(x0, y0, x0, y0, cT);
                    canvas.setFiguraTemporal(figuraTemporal);
                } else {
                    LineaFigura lf = new LineaFigura(x0, y0, x, y, cT);
                    model.agregarFigura(lf);
                    enModoDibujar = false;
                    canvas.clearFiguraTemporal();
                    canvas.repaint();
                    actualizarBotones();
                }
                break;

            case "Circunferencia":
                if (!enModoDibujar) {
                    x0 = x; y0 = y;
                    enModoDibujar = true;
                    figuraTemporal = new CirculoFigura(x0, y0, 0, cT, cR, rell);
                    canvas.setFiguraTemporal(figuraTemporal);
                } else {
                    int dx = x - x0;
                    int dy = y - y0;
                    int radio = (int) Math.round(Math.hypot(dx, dy));
                    CirculoFigura cf = new CirculoFigura(x0, y0, radio, cT, cR, rell);
                    model.agregarFigura(cf);
                    enModoDibujar = false;
                    canvas.clearFiguraTemporal();
                    canvas.repaint();
                    actualizarBotones();
                }
                break;

            case "Pol. regular":
                if (!enModoDibujar) {
                    x0 = x; y0 = y;
                    enModoDibujar = true;
                    int nL = view.getSliderVertices().getValue();
                    figuraTemporal = new PoligonoRegularFigura(
                        x0, y0, 0, nL, 0.0, cT, cR, rell
                    );
                    canvas.setFiguraTemporal(figuraTemporal);
                } else {
                    int dx = x - x0;
                    int dy = y - y0;
                    int radio = (int) Math.round(Math.hypot(dx, dy));
                    double angulo = Math.atan2(dy, dx);
                    int nL = view.getSliderVertices().getValue();
                    PoligonoRegularFigura prf = new PoligonoRegularFigura(
                        x0, y0, radio, nL, angulo, cT, cR, rell
                    );
                    model.agregarFigura(prf);
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
                        verticesTemp, cT, cR, rell
                    );
                    canvas.setFiguraTemporal(pirTemp);
                }
                break;
        }
    }

    private void manejarMouseMoved(int x, int y, CanvasPanel canvas) {
        if (!enModoDibujar) return;

        String modo = (String) view.getComboFiguras().getSelectedItem();
        Color cT = colorTrazo;
        Color cR = colorRelleno;
        boolean rell = modo.equals("Pol. regular") || modo.equals("Circunferencia");

        switch (modo) {
            case "Línea":
                figuraTemporal = new LineaFigura(x0, y0, x, y, cT);
                canvas.setFiguraTemporal(figuraTemporal);
                break;
            case "Circunferencia":
                int dx = x - x0;
                int dy = y - y0;
                int radio = (int) Math.round(Math.hypot(dx, dy));
                figuraTemporal = new CirculoFigura(x0, y0, radio, cT, cR, rell);
                canvas.setFiguraTemporal(figuraTemporal);
                break;
            case "Pol. regular":
                dx = x - x0;
                dy = y - y0;
                radio = (int) Math.round(Math.hypot(dx, dy));
                double angulo = Math.atan2(dy, dx);
                int nL = view.getSliderVertices().getValue();
                figuraTemporal = new PoligonoRegularFigura(
                    x0, y0, radio, nL, angulo, cT, cR, rell
                );
                canvas.setFiguraTemporal(figuraTemporal);
                break;
            default:
                break;
        }
    }

    /**
     * Construye el contenido SVG para todas las figuras actuales en el modelo.
     */
    private String generarSVG(CanvasPanel canvas, LienzoModel model) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(String.format(
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\">\n",
            width, height
        ));
        for (Figura f : model.getFiguras()) {
            sb.append("  ").append(f.toSVG()).append("\n");
        }
        sb.append("</svg>\n");
        return sb.toString();
    }

    /**
     * Actualiza el estado (habilitado/deshabilitado) de los botones Guardar y Exportar
     * según haya o no figuras en el lienzo.
     */
    private void actualizarBotones() {
        boolean hayFiguras = !model.getFiguras().isEmpty();
        view.getBtnGuardar().setEnabled(hayFiguras);
        view.getBtnExportarSVG().setEnabled(hayFiguras);
    }

    /**
     * Comprueba si la lista de vertices (en orden) tiene algún par
     * de segmentos que se cruza (ignora adyacentes).
     */
    private boolean hayInterseccionEntreVertices(List<Point> v) {
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

        return ( (cross1 > 0 && cross2 < 0 || cross1 < 0 && cross2 > 0)
              && (cross3 > 0 && cross4 < 0 || cross3 < 0 && cross4 > 0) );
    }
}
