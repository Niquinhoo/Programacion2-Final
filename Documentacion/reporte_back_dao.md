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

## 2026-06-10 - 15:45

### Implementación de PedidoDAO

- Se completó la interfaz `PedidoDAO` con los métodos base para gestión de pedidos.
- Se implementó completamente `PedidoDAOImpl.java` con lógica de acceso a datos.

### Métodos implementados

#### `getPedidos()`
- Consulta SQL con JOIN entre tabla `pedido` y `mesa`
- Mapea resultados a objetos `Pedido` incluyendo datos de la mesa asociada
- Mapea estados enumerados: `EstadoPedido` y `EstadoMesa`
- Manejo de excepciones SQL con devolución de lista vacía en caso de error

#### `Insertar(Pedido p, List<DetallePedido> detalles)`
- Implementación de transacción para insertar pedido y detalles atómicamente
- Desactiva `AutoCommit` para control manual de transacción
- Obtiene ID generado del pedido usando `Statement.RETURN_GENERATED_KEYS`
- Utiliza `executeBatch()` para insertar múltiples detalles de manera eficiente
- Rollback automático si falla la inserción
- Manejo completo de excepciones con restauración de configuración

### Métodos pendientes (TODO)

- `getPedidosPendientes()`: Debe retornar pedidos con estado ABIERTO
- `ModificarEstado(int id)`: Debe actualizar el estado de un pedido

### Archivos modificados

- `Backend/src/main/java/com/restaurant/backend/dao/PedidoDAO.java`
- `Backend/src/main/java/com/restaurant/backend/dao/PedidoDAOImpl.java`

---
