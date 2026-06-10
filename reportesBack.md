# Backend Change Log

## 2026-06-09

### Inicial

- Se creó el archivo `reportesBack.md` para registrar cambios en el backend.
- Se estableció el formato de reportes de cambios.

### Archivos modificados

- `reportesBack.md`

---

## 2026-06-10

### Configuración

- Se configuró Maven con la versión 9.1.0 de `mysql-connector-j` para conexión a BD.
- Se agregó JUnit Jupiter 5.10.1 para pruebas unitarias.
- Se configuró el compilador de Maven para Java 17.

###  Acceso a datos (DAO)

- Se creó `DatabaseConnection.java` con carga automática de propiedades desde `db.properties`.
- Se implementó método `getConnection()` para obtener conexiones a la base de datos.
- Se agregó manejo de excepciones para errores de conexión.

### Testing

- Se implementó una prueba manual de conexión desde la capa GUI
- Se implementó verificación de estado de conexión exitosa.

### Archivos modificados

- `Backend/pom.xml`
- `Backend/src/main/java/com/restaurant/backend/dao/DatabaseConnection.java`
- `GUI/src/main/java/com/restaurant/gui/GUI.java`

---
