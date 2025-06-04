package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

/**
 * MainFrame es la ventana principal. Contiene:
 *  - un panel de controles a la izquierda
 *  - un CanvasPanel (lienzo) en el centro
 */
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

    // Colores seleccionados actualmente
    private Color colorTrazo = Color.BLACK;
    private Color colorRelleno = Color.WHITE;

    public MainFrame() {
        super("Mi Paint en Swing (parte 1)");
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
        panelControles.add(new JLabel("Vértices (solo pol. reg.):"), gbc);
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

        // Añadimos el panelControles al MainFrame
        add(panelControles, BorderLayout.WEST);

        // 2) Creamos el CanvasPanel y lo añadimos al centro
        canvas = new CanvasPanel();
        add(canvas, BorderLayout.CENTER);

        // 3) Asociar listeners a comboFiguras y botones de color
        configurarListeners();

        pack();
        setLocationRelativeTo(null); // centrar en pantalla
    }

    private void configurarListeners() {
        // 3.1) Cuando cambia la selección en comboFiguras
        comboFiguras.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String seleccionado = (String) comboFiguras.getSelectedItem();
                // Solo si es "Pol. regular" habilitamos el slider
                boolean esPolRegular = "Pol. regular".equals(seleccionado);
                sliderVertices.setEnabled(esPolRegular);
            }
        });

        // 3.2) Botón Color Trazo: abre JColorChooser y guarda color
        btnColorTrazo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color elegido = JColorChooser.showDialog(MainFrame.this,
                        "Elige color de trazo", colorTrazo);
                if (elegido != null) {
                    colorTrazo = elegido;
                    // Para probar, podríamos pasar este color a CanvasPanel,
                    // pero en esta fase solo almacenamos la selección.
                }
            }
        });

        // 3.3) Botón Color Relleno: similar
        btnColorRelleno.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color elegido = JColorChooser.showDialog(MainFrame.this,
                        "Elige color de relleno", colorRelleno);
                if (elegido != null) {
                    colorRelleno = elegido;
                }
            }
        });

        // 3.4) (Opcional) Si quisiéramos saber el valor actual del slider:
        sliderVertices.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int nLados = sliderVertices.getValue();
                // Por ahora, solo lo imprimimos; en la Parte 2 esto definirá el pol. regular.
                System.out.println("Slider vertices = " + nLados);
            }
        });

        // 3.5) Los botones Guardar/Cargar/Exportar no hacen nada aún.
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

    /**
     * Método main para arrancar la aplicación
     */
    public static void main(String[] args) {
        // Para que la GUI se inicialice en el hilo de eventos de Swing:
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();
            mf.setVisible(true);
        });
    }
}
