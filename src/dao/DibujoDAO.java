package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'dibujos' en MySQL.
 */
public class DibujoDAO {
    private final ConexionBD conexionBD;

    public DibujoDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Crea un nuevo dibujo con el nombre dado.
     * Devuelve el id generado. Si el nombre ya existe, lanza SQLException.
     */
    public int crearDibujo(String nombre) throws SQLException {
        String sql = "INSERT INTO dibujos(nombre) VALUES (?)";
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener id de dibujo recién creado.");
                }
            }
        }
    }

    /**
     * Devuelve la lista de nombres de dibujos existentes, ordenados por fecha de creación descendente.
     */
    public List<String> listarNombresDibujos() throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre FROM dibujos ORDER BY fecha_creacion DESC";
        try (Connection conn = conexionBD.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rs.getString("nombre"));
            }
        }
        return lista;
    }

    /**
     * Devuelve el id_dibujo correspondiente a un nombre. Si no existe, retorna -1.
     */
    public int obtenerIdPorNombre(String nombre) throws SQLException {
        String sql = "SELECT id_dibujo FROM dibujos WHERE nombre = ?";
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_dibujo");
                } else {
                    return -1;
                }
            }
        }
    }

    /**
     * Elimina un dibujo dado su id. Gracias a ON DELETE CASCADE, 
     * MySQL borrará automáticamente sus 'figuras' y 'vertices'.
     */
    public void eliminarDibujo(int idDibujo) throws SQLException {
        String sql = "DELETE FROM dibujos WHERE id_dibujo = ?";
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDibujo);
            ps.executeUpdate();
        }
    }
}
