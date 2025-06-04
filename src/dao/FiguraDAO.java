package dao;

import model.*;
import java.awt.Point;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para las tablas 'figuras' y 'vertices_poligonos_irregulares' en MySQL.
 */
public class FiguraDAO {
    private final ConexionBD conexionBD;

    public FiguraDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Guarda una figura en la BD, vinculada a idDibujo, con el orden indicado.
     * - Para tipo POLIG_IRREG, también inserta sus vértices.
     */
    public void guardarFigura(int idDibujo, Figura f, int orden) throws SQLException {
        String tipo;
        int colorTrazoInt, colorRellenoInt, rellInt;

        if (f instanceof PuntoFigura) {
            tipo = "PUNTO";
            PuntoFigura pf = (PuntoFigura) f;
            colorTrazoInt = pf.getColorTrazo().getRGB() & 0xFFFFFF;
            colorRellenoInt = 0;
            rellInt = 0;

            String sql = "INSERT INTO figuras(id_dibujo, orden, tipo, color_trazo, color_relleno, relleno, x, y) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = conexionBD.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idDibujo);
                ps.setInt(2, orden);
                ps.setString(3, tipo);
                ps.setInt(4, colorTrazoInt);
                ps.setInt(5, colorRellenoInt);
                ps.setInt(6, rellInt);
                ps.setInt(7, pf.getX());
                ps.setInt(8, pf.getY());
                ps.executeUpdate();
            }
        }
        else if (f instanceof LineaFigura) {
            tipo = "LINEA";
            LineaFigura lf = (LineaFigura) f;
            colorTrazoInt = lf.getColorTrazo().getRGB() & 0xFFFFFF;
            colorRellenoInt = 0;
            rellInt = 0;

            String sql = "INSERT INTO figuras(id_dibujo, orden, tipo, color_trazo, color_relleno, relleno, x1, y1, x2, y2) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = conexionBD.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idDibujo);
                ps.setInt(2, orden);
                ps.setString(3, tipo);
                ps.setInt(4, colorTrazoInt);
                ps.setInt(5, colorRellenoInt);
                ps.setInt(6, rellInt);
                ps.setInt(7, lf.getX1());
                ps.setInt(8, lf.getY1());
                ps.setInt(9, lf.getX2());
                ps.setInt(10, lf.getY2());
                ps.executeUpdate();
            }
        }
        else if (f instanceof CirculoFigura) {
            tipo = "CIRCULO";
            CirculoFigura cf = (CirculoFigura) f;
            colorTrazoInt = cf.getColorTrazo().getRGB() & 0xFFFFFF;
            colorRellenoInt = cf.getColorRelleno().getRGB() & 0xFFFFFF;
            rellInt = cf.isRelleno() ? 1 : 0;

            String sql = "INSERT INTO figuras(id_dibujo, orden, tipo, color_trazo, color_relleno, relleno, centroX, centroY, radio) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = conexionBD.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idDibujo);
                ps.setInt(2, orden);
                ps.setString(3, tipo);
                ps.setInt(4, colorTrazoInt);
                ps.setInt(5, colorRellenoInt);
                ps.setInt(6, rellInt);
                ps.setInt(7, cf.getCentroX());
                ps.setInt(8, cf.getCentroY());
                ps.setInt(9, cf.getRadio());
                ps.executeUpdate();
            }
        }
        else if (f instanceof PoligonoRegularFigura) {
            tipo = "POLIG_REG";
            PoligonoRegularFigura prf = (PoligonoRegularFigura) f;
            colorTrazoInt = prf.getColorTrazo().getRGB() & 0xFFFFFF;
            colorRellenoInt = prf.getColorRelleno().getRGB() & 0xFFFFFF;
            rellInt = prf.isRelleno() ? 1 : 0;

            String sql = "INSERT INTO figuras(id_dibujo, orden, tipo, color_trazo, color_relleno, relleno, "
                       + "centroX, centroY, radio, n_lados, angulo_inicio) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = conexionBD.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idDibujo);
                ps.setInt(2, orden);
                ps.setString(3, tipo);
                ps.setInt(4, colorTrazoInt);
                ps.setInt(5, colorRellenoInt);
                ps.setInt(6, rellInt);
                ps.setInt(7, prf.getCentroX());
                ps.setInt(8, prf.getCentroY());
                ps.setInt(9, prf.getRadio());
                ps.setInt(10, prf.getnLados());
                ps.setDouble(11, prf.getAnguloInicio());
                ps.executeUpdate();
            }
        }
        else if (f instanceof PoligonoIrregularFigura) {
            tipo = "POLIG_IRREG";
            PoligonoIrregularFigura pirf = (PoligonoIrregularFigura) f;
            colorTrazoInt = pirf.getColorTrazo().getRGB() & 0xFFFFFF;
            colorRellenoInt = pirf.getColorRelleno().getRGB() & 0xFFFFFF;
            rellInt = pirf.isRelleno() ? 1 : 0;

            // 1) Insertar fila en 'figuras' sin datos de vértices
            String sqlFig = "INSERT INTO figuras(id_dibujo, orden, tipo, color_trazo, color_relleno, relleno) "
                          + "VALUES (?, ?, ?, ?, ?, ?)";
            int idFiguraGenerado;
            try (Connection conn = conexionBD.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlFig, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idDibujo);
                ps.setInt(2, orden);
                ps.setString(3, tipo);
                ps.setInt(4, colorTrazoInt);
                ps.setInt(5, colorRellenoInt);
                ps.setInt(6, rellInt);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idFiguraGenerado = rs.getInt(1);
                    } else {
                        throw new SQLException("No se obtuvo id para polígono irregular.");
                    }
                }
            }

            // 2) Insertar cada vértice en 'vertices_poligonos_irregulares'
            String sqlVert = "INSERT INTO vertices_poligonos_irregulares(id_figura, x, y, orden_vert) "
                           + "VALUES (?, ?, ?, ?)";
            List<Point> verts = pirf.getVertices();
            try (Connection conn = conexionBD.getConnection();
                 PreparedStatement ps2 = conn.prepareStatement(sqlVert)) {
                int idx = 0;
                for (Point p : verts) {
                    ps2.setInt(1, idFiguraGenerado);
                    ps2.setInt(2, p.x);
                    ps2.setInt(3, p.y);
                    ps2.setInt(4, idx++);
                    ps2.executeUpdate();
                }
            }
        }
        else {
            throw new IllegalArgumentException("Tipo de figura no soportado en DAO: " + f.getClass());
        }
    }

    /**
     * Carga todas las figuras asociadas a idDibujo, en orden, y devuelve la lista de objetos Figura.
     */
    public List<Figura> cargarFigurasPorDibujo(int idDibujo) throws SQLException {
        List<Figura> lista = new ArrayList<>();
        String sql = "SELECT * FROM figuras WHERE id_dibujo = ? ORDER BY orden ASC";
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDibujo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    int colorTrazoInt = rs.getInt("color_trazo");
                    int colorRellenoInt = rs.getInt("color_relleno");
                    boolean rell = (rs.getInt("relleno") == 1);

                    switch (tipo) {
                        case "PUNTO":
                            int x = rs.getInt("x");
                            int y = rs.getInt("y");
                            PuntoFigura pf = new PuntoFigura(
                                x, y, new java.awt.Color(colorTrazoInt)
                            );
                            lista.add(pf);
                            break;

                        case "LINEA":
                            int x1 = rs.getInt("x1");
                            int y1 = rs.getInt("y1");
                            int x2 = rs.getInt("x2");
                            int y2 = rs.getInt("y2");
                            LineaFigura lf = new LineaFigura(
                                x1, y1, x2, y2, new java.awt.Color(colorTrazoInt)
                            );
                            lista.add(lf);
                            break;

                        case "CIRCULO":
                            int cX = rs.getInt("centroX");
                            int cY = rs.getInt("centroY");
                            int radio = rs.getInt("radio");
                            CirculoFigura cf = new CirculoFigura(
                                cX, cY, radio,
                                new java.awt.Color(colorTrazoInt),
                                new java.awt.Color(colorRellenoInt),
                                rell
                            );
                            lista.add(cf);
                            break;

                        case "POLIG_REG":
                            int pX = rs.getInt("centroX");
                            int pY = rs.getInt("centroY");
                            int pradio = rs.getInt("radio");
                            int nL = rs.getInt("n_lados");
                            double angIni = rs.getDouble("angulo_inicio");
                            PoligonoRegularFigura prf = new PoligonoRegularFigura(
                                pX, pY, pradio, nL, angIni,
                                new java.awt.Color(colorTrazoInt),
                                new java.awt.Color(colorRellenoInt),
                                rell
                            );
                            lista.add(prf);
                            break;

                        case "POLIG_IRREG":
                            int idFig = rs.getInt("id_figura");
                            List<Point> verts = new ArrayList<>();
                            String sqlv = "SELECT x, y FROM vertices_poligonos_irregulares "
                                        + "WHERE id_figura = ? ORDER BY orden_vert ASC";
                            try (PreparedStatement ps2 = conn.prepareStatement(sqlv)) {
                                ps2.setInt(1, idFig);
                                try (ResultSet rs2 = ps2.executeQuery()) {
                                    while (rs2.next()) {
                                        verts.add(new Point(
                                            rs2.getInt("x"),
                                            rs2.getInt("y")
                                        ));
                                    }
                                }
                            }
                            PoligonoIrregularFigura pirf = new PoligonoIrregularFigura(
                                verts,
                                new java.awt.Color(colorTrazoInt),
                                new java.awt.Color(colorRellenoInt),
                                rell
                            );
                            lista.add(pirf);
                            break;

                        default:
                            throw new SQLException("Tipo de figura desconocido al cargar: " + tipo);
                    }
                }
            }
        }
        return lista;
    }

    /**
     * Elimina todas las figuras asociadas a un dibujo específico.
     * Útil si queremos sobrescribir un dibujo existente.
     */
    public void eliminarFigurasDeDibujo(int idDibujo) throws SQLException {
        String sql = "DELETE FROM figuras WHERE id_dibujo = ?";
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDibujo);
            ps.executeUpdate();
        }
    }
}
