package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Figura;
import model.LienzoModel;

/**
 * MainFrame (View) únicamente crea la interfaz y expone sus componentes
 * para que el Controller los registre.
 */
public class MainFrame extends JFrame {
    // Componentes Swing (View)
    private final CanvasPanel canvas;
    private final JComboBox<String> comboFiguras;
    private final JSlider sliderVertices;
    private final JButton btnColorTrazo;
    private final JButton btnColorRelleno;
    private final JButton btnGuardar;
    private final JButton btnCargar;
    private final JButton btnExportarSVG;
    private final JButton btnFinalizarPoligono;

    // Modelo en memoria (Model)
    private final LienzoModel lienzoModel;

    // Nombre del dibujo actual (para título)
    private String nombreActual = "(sin nombre)";

    public MainFrame() {
        super("Mi Paint");  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // PANEL DE CONTROLES (WEST)
        JPanel panelControles = new JPanel(new GridBagLayout());
        panelControles.setBorder(BorderFactory.createTitledBorder("Controles"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 1. ComboBox de figuras
        panelControles.add(new JLabel("Figura:"), gbc);
        gbc.gridy++;
        comboFiguras = new JComboBox<>(
            new String[] { "Punto", "Línea", "Circunferencia", "Pol. regular", "Pol. irregular" }
        );
        panelControles.add(comboFiguras, gbc);

        // 2. Slider de vértices (inicialmente deshabilitado)
        gbc.gridy++;
        panelControles.add(new JLabel("Vértices (solo pol. reg):"), gbc);
        gbc.gridy++;
        sliderVertices = new JSlider(SwingConstants.HORIZONTAL, 3, 12, 5);
        sliderVertices.setEnabled(false);
        sliderVertices.setMajorTickSpacing(1);
        sliderVertices.setPaintTicks(true);
        sliderVertices.setPaintLabels(true);
        panelControles.add(sliderVertices, gbc);

        // 3. Botones de color
        gbc.gridy++;
        btnColorTrazo = new JButton("Color de trazo");
        panelControles.add(btnColorTrazo, gbc);
        gbc.gridy++;
        btnColorRelleno = new JButton("Color de relleno");
        panelControles.add(btnColorRelleno, gbc);

        // 4. Botones Guardar/Cargar/Exportar
        gbc.gridy++;
        btnGuardar = new JButton("Guardar dibujo");
        btnGuardar.setEnabled(false);
        panelControles.add(btnGuardar, gbc);
        gbc.gridy++;
        btnCargar = new JButton("Cargar dibujo");
        panelControles.add(btnCargar, gbc);
        gbc.gridy++;
        btnExportarSVG = new JButton("Exportar a SVG");
        btnExportarSVG.setEnabled(false);
        panelControles.add(btnExportarSVG, gbc);

        // 5. Botón para “Finalizar pol. irr.”
        gbc.gridy++;
        btnFinalizarPoligono = new JButton("Finalizar pol. irr.");
        btnFinalizarPoligono.setEnabled(false);
        panelControles.add(btnFinalizarPoligono, gbc);

        add(panelControles, BorderLayout.WEST);

        // CANVAS (centro)
        canvas = new CanvasPanel();
        lienzoModel = new LienzoModel();
        canvas.setModel(lienzoModel);
        add(canvas, BorderLayout.CENTER);

        // Título inicial con el nombreActual
        setTitle("Mi Paint - Dibujo: " + nombreActual);

        pack();
        setLocationRelativeTo(null);
    }

    // -----------------------
    // GETTERS para Controller
    // -----------------------

    public LienzoModel getLienzoModel() {
        return lienzoModel;
    }

    public CanvasPanel getCanvas() {
        return canvas;
    }

    public JComboBox<String> getComboFiguras() {
        return comboFiguras;
    }

    public JSlider getSliderVertices() {
        return sliderVertices;
    }

    public JButton getBtnColorTrazo() {
        return btnColorTrazo;
    }

    public JButton getBtnColorRelleno() {
        return btnColorRelleno;
    }

    public JButton getBtnGuardar() {
        return btnGuardar;
    }

    public JButton getBtnCargar() {
        return btnCargar;
    }

    public JButton getBtnExportarSVG() {
        return btnExportarSVG;
    }

    public JButton getBtnFinalizarPoligono() {
        return btnFinalizarPoligono;
    }

    public String getNombreActual() {
        return nombreActual;
    }

    public void setNombreActual(String nombreActual) {
        this.nombreActual = nombreActual;
        setTitle("Mi Paint - Dibujo: " + nombreActual);
    }
    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();
            // Aquí enlazamos la Vista con el Controller:
            new controller.MainController(mf);
            mf.setVisible(true);
        });
    }
    
}
