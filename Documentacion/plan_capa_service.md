# Plan e inventario de cambios — Capa Service y Controllers

> Implementación del bloque ③ de `divisionTareasPlan/` — Integrante 3  
> Fecha: 2026-06-14

---

## 1. Resumen ejecutivo

Se implementó la capa de lógica de negocio (`service/`), el punto de acceso `ServicioFactory`, los controllers que delegan a los services, y el soporte mínimo de `ReporteDAO` para reportes. La GUI aún no está conectada; los controllers exponen métodos listos para ser invocados desde Swing.

**Regla de capas:** Vista → Controller → Service → DAO → MySQL

---

## 2. Arquitectura implementada

```
GUI (fase posterior)
    ↓
controller/   PedidoController, ProductoController, MesaController, CategoriaController, ReporteController
    ↓
ServicioFactory
    ↓
service/      PedidoService, ProductoService, MesaService, ReporteService
    ↓
dao/          PedidoDAOImpl, ProductoDAOImpl, MesaDAOImpl, ReporteDAOImpl
    ↓
MySQL
```

---

## 3. Archivos nuevos

| Archivo | Descripción |
|---|---|
| `Backend/src/main/java/com/restaurant/backend/service/ServicioFactory.java` | Singleton lazy de acceso a los 4 services |
| `Backend/src/main/java/com/restaurant/backend/service/PedidoService.java` | Lógica de pedidos: crear, cerrar, cancelar, totales |
| `Backend/src/main/java/com/restaurant/backend/service/ProductoService.java` | ABM y validaciones de productos |
| `Backend/src/main/java/com/restaurant/backend/service/MesaService.java` | Ocupar, liberar, reservar mesas |
| `Backend/src/main/java/com/restaurant/backend/service/ReporteService.java` | Datos para paneles de reportes |
| `Backend/src/main/java/com/restaurant/backend/service/dto/VentaPorProductoDTO.java` | DTO vista `vw_ventas_por_producto` |
| `Backend/src/main/java/com/restaurant/backend/service/dto/VentaPorMesDTO.java` | DTO vista `vw_ventas_por_mes` |
| `Backend/src/main/java/com/restaurant/backend/service/dto/ResumenGeneralDTO.java` | Resumen agregado de ventas |
| `Backend/src/main/java/com/restaurant/backend/dao/ReporteDAO.java` | Interfaz de consultas de reportes |
| `Backend/src/main/java/com/restaurant/backend/dao/ReporteDAOImpl.java` | JDBC sobre vistas y tabla `pedidos` |
| `Backend/src/main/java/com/restaurant/backend/controller/ReporteController.java` | Delegación a `ReporteService` |

---

## 4. Archivos modificados

| Archivo | Cambio |
|---|---|
| `Backend/src/main/java/com/restaurant/backend/controller/PedidoController.java` | Stub → delegación a `PedidoService` |
| `Backend/src/main/java/com/restaurant/backend/controller/ProductoController.java` | Stub → delegación a `ProductoService` |
| `Backend/src/main/java/com/restaurant/backend/controller/MesaController.java` | Stub → delegación a `MesaService` |
| `Backend/src/main/java/com/restaurant/backend/controller/CategoriaController.java` | Stub → listado de categorías desde productos |
| `Backend/src/main/java/com/restaurant/backend/dao/MesaDAOImpl.java` | Implementados `getMesas()` y `getMesaPorId()` contra tabla `mesas` |

---

## 5. Contrato de services

### ServicioFactory

```java
ServicioFactory.getProductoService()
ServicioFactory.getMesaService()
ServicioFactory.getPedidoService()
ServicioFactory.getReporteService()
```

### ProductoService

| Método | Regla de negocio |
|---|---|
| `obtenerTodos()` | Lista todos los productos disponibles en BD |
| `obtenerPorCategoria(String)` | Filtra en memoria por nombre de categoría (case-insensitive) |
| `obtenerPorId(int)` | Retorna null si id <= 0 |
| `crear(Producto)` | Valida nombre, precio >= 0, stock >= 0, categoría obligatoria |
| `actualizar(Producto)` | Igual que crear + id obligatorio |
| `eliminar(int id)` | Valida id > 0 |
| `descontarStock(int, int)` | Uso interno al cerrar pedido; verifica stock suficiente |
| `validarStockDisponible(int, int)` | Verifica producto disponible y stock; retorna null si OK |

### MesaService

| Método | Regla de negocio |
|---|---|
| `listar()` | Todas las mesas |
| `obtenerPorNumero(int)` | Busca por número de mesa |
| `obtenerPorId(int)` | Busca por id |
| `ocupar(int)` | Solo desde `LIBRE` o `RESERVADA` → `OCUPADA` |
| `liberar(int)` | Solo si no hay pedido activo (`ABIERTO`, `EN_COCINA`, `LISTO`) |
| `reservar(int)` | Solo desde `LIBRE` → `RESERVADA` |
| `cancelarReserva(int)` | Solo desde `RESERVADA` → `LIBRE` |
| `cambiarEstado(int, EstadoMesa)` | Valida transiciones permitidas |

**Transiciones permitidas:**

- `LIBRE` → `OCUPADA`, `RESERVADA`, `FUERA_DE_SERVICIO`
- `RESERVADA` → `OCUPADA`, `LIBRE`
- `OCUPADA` → `LIBRE`
- `FUERA_DE_SERVICIO` → `LIBRE`

### PedidoService

| Método | Regla de negocio |
|---|---|
| `crearPedido(Mesa, Usuario, List<DetallePedido>)` | Mesa `LIBRE`/`RESERVADA`; valida stock; calcula subtotales; persiste; ocupa mesa |
| `agregarItem(int, Producto, int)` | Valida pedido abierto y stock; **pendiente de DAO** para persistir el ítem |
| `calcularTotal(Pedido)` | Delega a `Pedido.recalcularTotal()` |
| `cerrarPedido(int)` | Descuenta stock, estado `CERRADO`, libera mesa |
| `cancelarPedido(int)` | Estado `CANCELADO`, libera mesa (sin descontar stock) |
| `listarPorEstado(EstadoPedido)` | Delega a DAO |
| `listarTodos()` | Delega a DAO |
| `obtenerDetalles(int)` | Delega a DAO |
| `obtenerPorId(int)` | Delega a DAO |

### ReporteService

| Método | Fuente |
|---|---|
| `ventasPorProducto()` | `vw_ventas_por_producto` |
| `ventasPorMes()` | `vw_ventas_por_mes` |
| `resumenGeneral()` | `COUNT` y `SUM` sobre pedidos `CERRADO` |

---

## 6. Contrato de controllers

| Controller | Métodos expuestos |
|---|---|
| `PedidoController` | `crear`, `cerrar`, `cancelar`, `listar`, `listarPorEstado`, `obtenerDetalles`, `obtenerPorId` |
| `ProductoController` | `listar`, `listarPorCategoria`, `obtenerPorId`, `crear`, `editar`, `eliminar` |
| `MesaController` | `listarMesas`, `obtenerPorNumero`, `obtenerPorId`, `ocupar`, `liberar`, `reservar`, `cancelarReserva`, `cambiarEstado` |
| `CategoriaController` | `listar` (derivado de productos hasta que exista `CategoriaDAO`) |
| `ReporteController` | `ventasPorProducto`, `ventasPorMes`, `resumenGeneral` |

---

## 7. Dependencias pendientes del DAO (bloqueantes de integración)

| Componente | Estado | Impacto |
|---|---|---|
| `MesaDAOImpl.nuevaMesa`, `cambiarEstado`, `getMesasPorEstado` | Usan tabla `mesa` (nombre antiguo) | Puede fallar contra `schema.sql` actual (`mesas`) |
| `PedidoDAOImpl` | Usa tablas `pedido`, `mesa`, `detalle_pedido`, `producto` | Desalineado con `pedidos`, `mesas`, `detalle_pedido`, `productos` |
| `ProductoDAO.FiltrarPorCategoria` | Devuelve un solo producto | Service filtra en memoria como workaround |
| `CategoriaDAO` | Interfaz vacía | `CategoriaController` deriva categorías de productos |
| `UsuarioDAO` | No existe | Login/Registro pendientes |
| `PedidoDAO` agregar ítem | No existe | `agregarItem` valida pero no persiste |
| Tabla `reservas` | No existe en schema | `ReservaService` fuera de alcance |

---

## 8. Criterios de aceptación

- [x] Carpeta `service/` con 5 clases compilables
- [x] Controllers delegan exclusivamente a services (sin SQL)
- [x] Reglas de negocio de pedidos, mesas y stock en services
- [x] `ServicioFactory` expone los 4 services del plan
- [x] `Documentacion/plan_capa_service.md` generado
- [x] `mvn compile -f Backend/pom.xml` exitoso

---

## 9. Fuera de alcance (próxima iteración)

- `AutenticacionService` + `UsuarioDAO`
- `ReservaService` + modelo `Reserva`
- Conexión GUI ↔ controllers (reemplazar datos hardcodeados y TODOs de `ServicioFactory`)
- Tests unitarios de services
- Alinear SQL de todos los DAOs con `schema.sql`

---

## 10. Changelog

### 2026-06-14 — Implementación capa Service

**Nuevo**

- Paquete `com.restaurant.backend.service` con `PedidoService`, `ProductoService`, `MesaService`, `ReporteService`, `ServicioFactory`
- Paquete `com.restaurant.backend.service.dto` con DTOs de reportes
- `ReporteDAO` + `ReporteDAOImpl`
- `ReporteController`

**Modificado**

- Controllers de Pedido, Producto, Mesa y Categoria completados
- `MesaDAOImpl`: implementados `getMesas()` y `getMesaPorId()`

**Verificación**

```bash
mvn compile -f Backend/pom.xml
```

Resultado: compilación exitosa.
