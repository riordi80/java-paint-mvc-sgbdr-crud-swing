package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton para gestionar la conexión con MySQL y crear tablas si no existen.
 */
public class ConexionBD {
    // URL fija
    private static final String URL =
        "jdbc:mysql://localhost:3306/paint_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";

    // Ahora leemos usuario/contraseña de System.getProperty()
    private static final String USER = System.getProperty("DB_USER");
    private static final String PASS = System.getProperty("DB_PASS");

    static {
        if (USER == null || PASS == null) {
            throw new IllegalStateException(
                "Faltan las system properties DB_USER y/o DB_PASS. "
                + "Arranca la app con: -DDB_USER=tu_usuario -DDB_PASS=tu_contraseña"
            );
        }
    }

    private static ConexionBD instancia = null;

    private ConexionBD() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // 1) Tabla 'dibujos'
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS dibujos (\n" +
                "  id_dibujo      INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "  nombre         VARCHAR(255) NOT NULL UNIQUE,\n" +
                "  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP\n" +
                ") ENGINE=InnoDB;"
            );

            // 2) Tabla 'figuras'
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS figuras (\n" +
                "  id_figura      INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "  id_dibujo      INT NOT NULL,\n" +
                "  orden          INT NOT NULL,\n" +
                "  tipo           VARCHAR(20) NOT NULL,\n" +
                "  color_trazo    INT NOT NULL,\n" +
                "  color_relleno  INT NOT NULL,\n" +
                "  relleno        TINYINT(1) NOT NULL,\n" +
                "  x              INT,\n" +
                "  y              INT,\n" +
                "  x1             INT,\n" +
                "  y1             INT,\n" +
                "  x2             INT,\n" +
                "  y2             INT,\n" +
                "  centroX        INT,\n" +
                "  centroY        INT,\n" +
                "  radio          INT,\n" +
                "  n_lados        INT,\n" +
                "  angulo_inicio  DOUBLE,\n" +
                "  FOREIGN KEY (id_dibujo) REFERENCES dibujos(id_dibujo) ON DELETE CASCADE\n" +
                ") ENGINE=InnoDB;"
            );

            // 3) Tabla 'vertices_poligonos_irregulares'
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS vertices_poligonos_irregulares (\n" +
                "  id_vertice    INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "  id_figura     INT NOT NULL,\n" +
                "  x             INT NOT NULL,\n" +
                "  y             INT NOT NULL,\n" +
                "  orden_vert    INT NOT NULL,\n" +
                "  FOREIGN KEY (id_figura) REFERENCES figuras(id_figura) ON DELETE CASCADE\n" +
                ") ENGINE=InnoDB;"
            );

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error al inicializar la base de datos MySQL", ex);
        }
    }

    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
