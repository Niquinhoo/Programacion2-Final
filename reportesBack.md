# Backend Change Log

## 2026-06-09

### Inicial

- Se creó el archivo `reportesBack.md` para registrar cambios en el backend.
- Se estableció el formato de reportes de cambios.

### Archivos modificados

- `reportesBack.md`

---

## 2026-06-10 - 14:30

### Refactoring

- Se refactorizó `DatabaseConnection.java` para implementar patrón Singleton con `ConexionDB`.
- Se eliminó la carga estática de propiedades y se centralizó la conexión en la clase `ConexionDB`.
- Se simplificó el método `getConnection()` para delegar al singleton.

### Implementación

- Se completó la implementación de `ProductoDAOImpl.java`.
- Se agregó el método `GetProductos()` que consulta todos los productos de la base de datos.
- Se mapean correctamente los resultados del ResultSet al modelo `Producto`.

### Archivos modificados

- `Backend/src/main/java/com/restaurant/backend/dao/DatabaseConnection.java`
- `Backend/src/main/java/com/restaurant/backend/dao/ProductoDAOImpl.java`

---
