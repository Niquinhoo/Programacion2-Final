# Backend Change Log

## 2026-06-09

### Inicial

- Se creó el archivo `reportesBack.md` para registrar cambios en el backend.
- Se estableció el formato de reportes de cambios.

### Archivos modificados

- `reportesBack.md`

---

## 2026-06-10

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

#### `getPedidosPorEstado(EstadoPedido estado)`
- Filtra pedidos por su estado (ABIERTO, CERRADO, COMPLETADO, etc.)
- Consulta con INNER JOIN entre tabla `pedido` y `mesa`
- Mapea resultados completos incluyendo datos de la mesa asociada
- Manejo de excepciones con devolución de lista vacía

#### `ModificarEstado(int id, EstadoPedido estado)`
- Actualiza el estado de un pedido por su ID
- Valida que la actualización afectó al menos una fila
- Retorna mensaje descriptivo del resultado de la operación
- Manejo completo de excepciones SQL

### Métodos pendientes (TODO)

- `borrarMesa(int id)` en MesaDAOImpl: Debe implementar eliminación de mesas

### Archivos modificados

- `Backend/src/main/java/com/restaurant/backend/dao/PedidoDAO.java`
- `Backend/src/main/java/com/restaurant/backend/dao/PedidoDAOImpl.java`

---

## 2026-06-11

### Implementación de MesaDAO

- Se completó la interfaz `MesaDAO` con métodos para gestión de mesas.
- Se implementó `MesaDAOImpl.java` con los siguientes métodos:

#### `nuevaMesa(Mesa mesa)`
- Inserta una nueva mesa en la base de datos
- Establece el número de mesa y su estado inicial
- Valida que la inserción afectó al menos una fila
- Retorna mensaje descriptivo del resultado

#### `cambiarEstado(int id, EstadoMesa estado)`
- Actualiza el estado de una mesa por su ID
- Mapea correctamente los estados del enum `EstadoMesa`
- Valida que la mesa existe antes de actualizar
- Manejo completo de excepciones SQL

### Métodos pendientes en MesaDAO

- `borrarMesa(int id)`: Lanza `UnsupportedOperationException` (aún sin implementar)

### Verificación de PedidoDAO

- Se confirmó que todos los métodos de `PedidoDAOImpl.java` están completamente implementados
- Se removieron métodos del listado de pendientes: `getPedidosPorEstado()` y `ModificarEstado()`

### Archivos modificados

- `Backend/src/main/java/com/restaurant/backend/dao/MesaDAO.java`
- `Backend/src/main/java/com/restaurant/backend/dao/MesaDAOImpl.java`
- `Backend/src/main/java/com/restaurant/backend/dao/PedidoDAOImpl.java`

---

## 2026-06-12 - 09:45

### Completitud de DAO Layer

- Se confirmó que la capa DAO está completamente implementada para operaciones base.
- Se verificó la calidad y consistencia del código en todas las implementaciones.

### Estado de Implementación

#### ProductoDAOImpl ✓ COMPLETO
- Método `GetProductos()` implementado y funcional

#### PedidoDAOImpl ✓ COMPLETO (7/7 métodos)
- `getPedidos()` - Obtiene todos los pedidos con datos de mesa
- `Insertar()` - Inserta pedido y detalles con transacción
- `getPedidosPorEstado()` - Filtra por estado del pedido
- `ModificarEstado()` - Actualiza estado del pedido
- `getDetallesPedido()` - Obtiene detalles de un pedido
- `getPedidoPorId()` - Obtiene un pedido específico
- `getPedidosPorMesa()` - Obtiene pedidos de una mesa

#### MesaDAOImpl ⚠️ PARCIALMENTE COMPLETO (3/6 métodos)
- `nuevaMesa()` ✓ - Inserta nueva mesa
- `cambiarEstado()` ✓ - Actualiza estado de mesa
- `getMesasPorEstado()` ✓ - Obtiene mesas filtradas por estado
- `borrarMesa()` ❌ - Pendiente de implementación
- `getMesas()` ❌ - Pendiente de implementación
- `getMesaPorId()` ❌ - Pendiente de implementación

### Patrones de código aplicados

- **Try-with-resources**: Gestión automática de conexiones y statements
- **Transacciones**: En `PedidoDAOImpl.Insertar()` para atomicidad
- **Batch Operations**: En inserciones múltiples de detalles de pedido
- **Manejo de Enums**: Conversión bidireccional con estados de BD
- **Generated Keys**: Recuperación de IDs auto-generados

### Próximos pasos

- Implementar métodos pendientes en `MesaDAOImpl`
- Crear tests unitarios para validación
- Implementar `CategoriaDAOImpl` si aún no existe

### Archivos modificados

- `Backend/src/main/java/com/restaurant/backend/dao/MesaDAOImpl.java`
- `Backend/src/main/java/com/restaurant/backend/dao/PedidoDAOImpl.java`

---

## 2026-06-12 - Actualización de reportes

### Correcciones en MesaDAOImpl

Se identificó que el método `getMesasPorEstado()` ya estaba implementado en `MesaDAOImpl` pero no había sido reportado correctamente.

#### `getMesasPorEstado(EstadoMesa estado)` ✓ COMPLETO
- Consulta SQL que filtra mesas por estado
- Mapea correctamente los valores del enum `EstadoMesa`
- Retorna lista de mesas (`ArrayList<Mesa>`)
- Mapea todos los campos: `id`, `numero`, `estado`
- Manejo de excepciones SQL con devolución de lista vacía en caso de error
- Implementa try-with-resources para gestión automática de conexión

### Actualización de estado

#### MesaDAOImpl actualizado: ⚠️ PARCIALMENTE COMPLETO (3/6 métodos)
- `nuevaMesa()` ✓
- `cambiarEstado()` ✓
- `getMesasPorEstado()` ✓ (recién reportado)
- `borrarMesa()` ❌
- `getMesas()` ❌
- `getMesaPorId()` ❌

### Archivos verificados

- `Backend/src/main/java/com/restaurant/backend/dao/MesaDAOImpl.java`
