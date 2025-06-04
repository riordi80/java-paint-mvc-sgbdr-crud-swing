# Java Swing Paint Project

## Project Overview

This is a lightweight “Paint”-style application developed in Java using Swing for the GUI. You can draw various geometric shapes on a canvas, save/load your drawings to/from a MySQL database, and export them as SVG files. The architecture follows a pure MVC pattern with separate packages for Model, View, Controller, and DAO (data access).

---

## Key Features

1. **Shape Drawing**

   * **Point**: Draw a small filled circle at a clicked coordinate.
   * **Line**: Click once to set the first endpoint, move the mouse to preview, click again to set the second endpoint.
   * **Circle**: Click to set center, move the mouse to preview radius, click again to finalize.
   * **Regular Polygon**: Select number of sides (3–12) via slider. Click to set center, move the mouse to preview size and orientation, click again to finalize.
   * **Irregular Polygon**: Click repeatedly to add vertices; click “Finish Polygon” to validate (minimum 3 points, no self-intersections) and finalize.

2. **Color Selection**

   * **Stroke Color**: Choose any color for the outline of shapes via a color picker dialog.
   * **Fill Color**: Choose any color for filling shapes that support filling (circle, polygons). If “fill” is disabled for a given shape, only the outline is drawn.

3. **Saving & Loading Drawings (MySQL)**

   * **Save**:

     1. Click “Save Drawing.”
     2. Enter a unique name (no invalid filename characters).
     3. If the name already exists, choose whether to overwrite or cancel.
     4. All shapes currently on the canvas are persisted in two tables:

        * **dibujos**: stores drawing ID, name, and timestamp.
        * **figuras** and **vertices\_poligonos\_irregulares**: each shape’s data (type, coordinates, color values, etc.).
   * **Load**:

     1. Click “Load Drawing.”
     2. If any shapes on the canvas are unsaved, you’ll be prompted to confirm discarding them.
     3. A dialog lists all saved drawing names (ordered by most recent).
     4. Selecting one loads every shape back onto the canvas exactly as they were drawn previously.

4. **Export to SVG**

   * Click “Export to SVG” to open a file‐save dialog.
   * If no shapes exist, you’ll see a warning. Otherwise, choose a filename (extension “.svg” is appended automatically if missing).
   * The application generates a valid SVG XML file containing each shape’s SVG element (`<circle>`, `<line>`, `<polygon>`, etc.) with correct coordinates, stroke color, fill color (or none), and size.

5. **Validation & User Feedback**

   * Minimum 3 vertices required for irregular polygons; attempts to finish with fewer produce an alert.
   * Polygons cannot self‐intersect: attempted invalid vertex placements show an alert to correct or restart.
   * Cannot save or export if canvas is empty (buttons are disabled and/or show a message).
   * Prevents empty or invalid drawing names (disallows characters: `\ / : * ? " < > |`).

6. **MVC Architecture**

   * **Model** (`model` package):

     * `Figura` interface (defines `dibujar(Graphics2D)` and `toSVG()`).
     * Concrete shape classes: `PuntoFigura`, `LineaFigura`, `CirculoFigura`, `PoligonoRegularFigura`, `PoligonoIrregularFigura`.
     * `LienzoModel`: holds a list of `Figura` instances in memory and provides methods to add, clear, or retrieve shapes.
   * **View** (`view` package):

     * `MainFrame`: constructs the GUI layout (control panel on the left, drawing canvas in the center) and exposes getters for buttons, sliders, combo boxes, and the canvas.
     * `CanvasPanel`: extends `JPanel` and overrides `paintComponent(Graphics)` to render all shapes from `LienzoModel` and any temporary shape being dragged.
   * **Controller** (`controller` package):

     * `MainController`: registers all event listeners (button clicks, slider changes, mouse events) and mediates between View and Model. It handles shape creation logic, color picking, save/load/export workflow, and enables/disables buttons appropriately.
   * **DAO** (`dao` package):

     * `ConexionBD`: singleton that manages the JDBC connection, creates necessary tables (`dibujos`, `figuras`, `vertices_poligonos_irregulares`) if they don’t exist.
     * `DibujoDAO`: methods to create a new drawing record, list drawing names, retrieve IDs by name, and delete drawings.
     * `FiguraDAO`: methods to insert each shape into the database (including an additional table for irregular‐polygon vertices) and to load shapes back into `Figura` instances when retrieving a saved drawing.

---

## Project Structure

```
src/
├── controller/
│   └── MainController.java
├── dao/
│   ├── ConexionBD.java
│   ├── DibujoDAO.java
│   └── FiguraDAO.java
├── model/
│   ├── CirculoFigura.java
│   ├── Figura.java
│   ├── LienzoModel.java
│   ├── LineaFigura.java
│   ├── PoligonoIrregularFigura.java
│   ├── PoligonoRegularFigura.java
│   └── PuntoFigura.java
└── view/
    ├── CanvasPanel.java
    └── MainFrame.java
```

---

## Getting Started

1. **Prerequisites**

   * Java Development Kit (JDK) 11 or higher.
   * MySQL server running locally (or update the JDBC URL to point to your MySQL instance).
   * Ensure `mysql-connector-java.jar` is available on your classpath.
   * **Database credentials must be passed as system properties** (VM options) and read via `System.getProperty(...)` in `ConexionBD.java`. For example:

     ```
     -DDB_USER=your_user  
     -DDB_PASS=your_password  
     ```

2. **Database Setup**

   * The application reads the following system properties for database connection:

     * `db.url`
     * `db.user`
     * `db.password`
   * On first run, `ConexionBD` automatically creates these tables if they don’t exist:

     ```sql
     CREATE TABLE IF NOT EXISTS dibujos (
       id_dibujo      INT AUTO_INCREMENT PRIMARY KEY,
       nombre         VARCHAR(255) NOT NULL UNIQUE,
       fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
     );
     CREATE TABLE IF NOT EXISTS figuras (
       id_figura      INT AUTO_INCREMENT PRIMARY KEY,
       id_dibujo      INT NOT NULL,
       orden          INT NOT NULL,
       tipo           VARCHAR(20) NOT NULL,
       color_trazo    INT NOT NULL,
       color_relleno  INT NOT NULL,
       relleno        TINYINT(1) NOT NULL,
       x              INT, y INT,
       x1             INT, y1 INT,
       x2             INT, y2 INT,
       centroX        INT, centroY INT,
       radio          INT,
       n_lados        INT,
       angulo_inicio  DOUBLE,
       FOREIGN KEY (id_dibujo) REFERENCES dibujos(id_dibujo) ON DELETE CASCADE
     );
     CREATE TABLE IF NOT EXISTS vertices_poligonos_irregulares (
       id_vertice    INT AUTO_INCREMENT PRIMARY KEY,
       id_figura     INT NOT NULL,
       x             INT NOT NULL,
       y             INT NOT NULL,
       orden_vert    INT NOT NULL,
       FOREIGN KEY (id_figura) REFERENCES figuras(id_figura) ON DELETE CASCADE
     );
     ```

3. **Build & Run**

   * **Using an IDE (NetBeans/Eclipse/IntelliJ)**:

     1. Import the project as a Java application.
     2. Add the MySQL JDBC driver (`mysql-connector-java.jar`) to the classpath.
     3. Set VM options with the appropriate `-Ddb.url`, `-Ddb.user`, and `-Ddb.password`.
     4. Run the `MainFrame.main()` method; the `MainController` is instantiated automatically.
   * **Using Command Line**:

     ```
     javac -cp path/to/mysql-connector-java.jar src/**/*.java
     java -Ddb.url=jdbc:mysql://localhost:3306/paint_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8 \
          -Ddb.user=root \
          -Ddb.password=your_password \
          -cp .;path/to/mysql-connector-java.jar view.MainFrame
     ```

---

## Usage Guide

1. **Launching the Application**

   * When the window opens, you’ll see a blank canvas on the right and controls on the left.

2. **Selecting a Shape**

   * Use the “Figure:” drop‐down to choose one of:

     * **Point**
     * **Line**
     * **Circle**
     * **Regular Polygon** (slider appears to pick number of sides)
     * **Irregular Polygon** (“Finish Polygon” button becomes enabled)

3. **Choosing Colors**

   * Click **“Stroke Color”** to pick an outline color.
   * Click **“Fill Color”** to pick a fill color (used by circle and polygons when “fill” is enabled).

4. **Drawing on Canvas**

   * **Point**: Click anywhere on the canvas → a small filled circle appears.
   * **Line**:

     1. First click sets the start point.
     2. Move the mouse to see a preview.
     3. Second click sets the end point and finalizes the line.
   * **Circle**:

     1. First click sets the center.
     2. Move the mouse to see a preview (radius).
     3. Second click sets the radius and finalizes the circle (filled if “Fill Color” is chosen).
   * **Regular Polygon**:

     1. Adjust the slider (3–12) to select the number of sides.
     2. First click sets the center.
     3. Move the mouse to see a preview of a rotated polygon.
     4. Second click finalizes the polygon.
   * **Irregular Polygon**:

     1. Each click adds one vertex. Once you’ve clicked at least 3 times, a preview polygon updates continuously.
     2. Click **“Finish Polygon”** to finalize. If fewer than 3 points or if edges cross, a warning appears.

5. **Saving a Drawing**

   * Click **“Save Drawing”** (enabled only when at least one shape exists).
   * Enter a name for your drawing (no invalid characters).
   * If the name already exists, choose whether to overwrite.
   * All shapes are saved in MySQL under that name; window title updates to include the drawing name.

6. **Loading a Drawing**

   * Click **“Load Drawing”**. If the current canvas has unsaved shapes, you’ll be asked to confirm loss of those changes.
   * A dialog shows all saved drawing names. Select one and click OK.
   * The canvas is cleared and then repopulated with the selected drawing’s shapes. Window title updates accordingly.

7. **Exporting to SVG**

   * Click **“Export to SVG”** (enabled only when shapes exist).
   * A file‐save dialog appears. Choose or type a filename (“.svg” is appended automatically).
   * The resulting SVG file includes each shape’s SVG element with correct coordinates, stroke, and fill.

8. **Clearing the Canvas**

   * Loading a drawing or saving a new one clears any temporary previews and ensures the canvas accurately reflects the Model’s state.

---

## Code Highlights

* **`Figura` interface** (`model/Figura.java`):

  ```java
  public interface Figura {
      void dibujar(Graphics2D g);
      String toSVG();
  }
  ```

  Every shape implements these two methods to handle on‐screen rendering and SVG export.

* **`LienzoModel`** (`model/LienzoModel.java`):

  * Holds a `List<Figura>`
  * Methods: `agregarFigura(Figura)`, `getFiguras()`, `clear()`, etc.
  * The canvas simply iterates over this list to invoke `dibujar()` for each shape.

* **`MainController`** (`controller/MainController.java`):

  * Registers all Swing event listeners:

    * `ActionListener` for buttons and combo box.
    * `ChangeListener` for the polygon‐sides slider.
    * `MouseListener` & `MouseMotionListener` for shape creation on the canvas.
  * Coordinates between **View** (`MainFrame`), **Model** (`LienzoModel`), and **DAO** classes for save/load operations.
  * Handles all validations (e.g., polygon vertex count, name conflicts) and updates button states (enable/disable).

* **Database DAOs** (`dao/`):

  * `ConexionBD`: Singleton that opens a JDBC connection to MySQL, creates tables if necessary.
  * `DibujoDAO`: Methods to insert a new drawing name, retrieve IDs by name, list all drawing names, delete drawings.
  * `FiguraDAO`: Persists each shape to the `figuras` table, including an additional table for irregular‐polygon vertices.

---

## Dependencies

* **Java SE 11+** (Swing, AWT, JDBC are part of the JDK).
* **MySQL Connector/J** (JDBC driver). Make sure `mysql-connector-java.jar` is on your classpath.

---

## License

This project is released under the MIT License. Feel free to fork, modify, and use it in your own applications.

*Enjoy creating and saving your own vector‐style drawings in Java!*
