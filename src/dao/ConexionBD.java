package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 * Gestionar la conexión con MySQL, crear la base de datos si no existe
 * y crear tablas si no existen.
 */
public class ConexionBD {
    private static final String DB_NAME = "paint_db";
    // URL base sin esquema para operaciones de nivel servidor
    private static final String BASE_URL =
        "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    // URL con el esquema específico
    private static final String DB_URL =
        "jdbc:mysql://localhost:3306/" + DB_NAME +
        "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String USER = "desarrollo";
    private static final String PASS = "desarrollo";

    private static ConexionBD instancia = null;

    private ConexionBD() {
        // 1) Verificar que la base de datos existe; si no, intentar crearla
        ensureDatabaseExists();
        // 2) Crear tablas necesarias dentro de la base de datos
        createTablesIfNotExists();
    }

    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /**
     * Obtiene una conexión directa a la base de datos 'DB_NAME'.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * Se asegura de que la base de datos exista, solicitando al servidor
     * MySQL crearla si no está. Maneja errores de permisos.
     */
    private void ensureDatabaseExists() {
        // Intentamos conectar al esquema; si falla con código 1049, no existe
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // La base de datos ya existe, no hay nada más que hacer
            return;
        } catch (SQLException ex) {
            // 1049 = Unknown database
            if (ex.getErrorCode() == 1049) {
                int resp = JOptionPane.showConfirmDialog(
                    null,
                    "La base de datos '" + DB_NAME + "' no existe. ¿Deseas crearla ahora?",
                    "Base de datos no encontrada",
                    JOptionPane.YES_NO_OPTION
                );
                if (resp == JOptionPane.YES_OPTION) {
                    try (Connection srvConn = DriverManager.getConnection(BASE_URL, USER, PASS);
                         Statement stmt = srvConn.createStatement()) {
                        stmt.executeUpdate(
                            "CREATE DATABASE IF NOT EXISTS " + DB_NAME +
                            " DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci"
                        );
                    } catch (SQLException e2) {
                        JOptionPane.showMessageDialog(
                            null,
                            "Error al crear la base de datos: " + e2.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                        throw new RuntimeException("No se pudo crear la base de datos", e2);
                    }
                } else {
                    throw new RuntimeException(
                        "La aplicación requiere la base de datos '" + DB_NAME + "' para funcionar."
                    );
                }
            } else {
                // Otro error al conectar: permisos insuficientes u otros problemas
                JOptionPane.showMessageDialog(
                    null,
                    "No se pudo conectar a MySQL: " + ex.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE
                );
                throw new RuntimeException("Error al conectar a MySQL", ex);
            }
        }
    }

    /**
     * Ejecuta los CREATE TABLE IF NOT EXISTS dentro de la base de datos 'DB_NAME'.
     */
    private void createTablesIfNotExists() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // 1) Tabla 'dibujos'
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS dibujos (" +
                " id_dibujo INT AUTO_INCREMENT PRIMARY KEY," +
                " nombre VARCHAR(255) NOT NULL UNIQUE," +
                " fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB;"
            );

            // 2) Tabla 'figuras'
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS figuras (" +
                " id_figura INT AUTO_INCREMENT PRIMARY KEY," +
                " id_dibujo INT NOT NULL," +
                " orden INT NOT NULL," +
                " tipo VARCHAR(20) NOT NULL," +
                " color_trazo INT NOT NULL," +
                " color_relleno INT NOT NULL," +
                " relleno TINYINT(1) NOT NULL," +
                " x INT, y INT," +
                " x1 INT, y1 INT," +
                " x2 INT, y2 INT," +
                " centroX INT, centroY INT," +
                " radio INT," +
                " n_lados INT," +
                " angulo_inicio DOUBLE," +
                " FOREIGN KEY (id_dibujo) REFERENCES dibujos(id_dibujo) ON DELETE CASCADE" +
                ") ENGINE=InnoDB;"
            );

            // 3) Tabla 'vertices_poligonos_irregulares'
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS vertices_poligonos_irregulares (" +
                " id_vertice INT AUTO_INCREMENT PRIMARY KEY," +
                " id_figura INT NOT NULL," +
                " x INT NOT NULL, y INT NOT NULL," +
                " orden_vert INT NOT NULL," +
                " FOREIGN KEY (id_figura) REFERENCES figuras(id_figura) ON DELETE CASCADE" +
                ") ENGINE=InnoDB;"
            );
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                null,
                "Error al inicializar las tablas: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException("Error al crear tablas", ex);
        }
    }
}