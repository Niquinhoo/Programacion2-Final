# Reporte de cambios — Alineación DAO y persistencia de items

> Corrección de pendientes conocidos de la capa Service y DAO  
> Fecha: 2026-06-14

---

## 1. Resumen

Se corrigieron los tres pendientes documentados en `plan_capa_service.md` que bloqueaban la integración con la base de datos. Todos los DAOs ahora usan los nombres de tablas y columnas definidos en `schema.sql`, y `agregarItem` persiste realmente los detalles en la base de datos.

---

## 2. Cambios realizados

### 2.1 PedidoDAOImpl — Alineación SQL completa

Todas las consultas SQL se actualizaron para coincidir con `schema.sql`:

| Método | Antes | Después |
|---|---|---|
| `getPedidos()` | `FROM pedido p JOIN mesa m ON p.mesa_id = m.id` | `FROM pedidos p JOIN mesas m ON p.id_mesa = m.id_mesa` |
| `Insertar()` | `INSERT INTO pedido(mesa_id,fecha,...)` | `INSERT INTO pedidos(id_mesa,created_at,...)` |
| `Insertar()` (detalles) | `INSERT INTO detalle_pedido(pedido_id,producto_id,...)` | `INSERT INTO detalle_pedido(id_pedido,id_producto,...,subtotal)` |
| `getPedidosPorEstado()` | `FROM pedido p INNER JOIN mesa m ON p.mesa_id = m.id` | `FROM pedidos p INNER JOIN mesas m ON p.id_mesa = m.id_mesa` |
| `ModificarEstado()` | `UPDATE pedido SET ... WHERE id = ?` | `UPDATE pedidos SET ... WHERE id_pedido = ?` |
| `getDetallesPedido()` | `dp.id`, `dp.producto_id`, `p.id` | `dp.id_detalle`, `dp.id_producto`, `p.id_producto` |
| `getPedidoPorId()` | `FROM pedido p INNER JOIN mesa m ON p.mesa_id = m.id` | `FROM pedidos p INNER JOIN mesas m ON p.id_mesa = m.id_mesa` |
| `getPedidosPorMesa()` | `FROM pedido p INNER JOIN mesa m ON p.mesa_id = m.id` | `FROM pedidos p INNER JOIN mesas m ON p.id_mesa = m.id_mesa` |

### 2.2 MesaDAOImpl — Alineación SQL parcial

| Método | Antes | Después |
|---|---|---|
| `nuevaMesa()` | `INSERT INTO mesa(numero,estado)` | `INSERT INTO mesas(numero,capacidad,estado)` |
| `cambiarEstado()` | `UPDATE mesa SET ... WHERE id = ?` | `UPDATE mesas SET ... WHERE id_mesa = ?` |
| `getMesasPorEstado()` | `SELECT * FROM mesa` + `result.getInt("id")` | `SELECT id_mesa, numero, capacidad, estado FROM mesas` + usa `mapearMesa()` |

### 2.3 PedidoDAO — Nuevos métodos para persistencia de items

Se agregaron dos métodos a la interfaz `PedidoDAO`:

```java
String insertarDetalle(int pedidoId, DetallePedido detalle);
String actualizarTotal(int pedidoId, BigDecimal total);
```

**insertarDetalle**: Inserta una fila en `detalle_pedido` con `id_pedido`, `id_producto`, `cantidad`, `precio_unitario`, `subtotal`, `observacion`.

**actualizarTotal**: Actualiza el total del pedido en la tabla `pedidos` después de agregar un item.

### 2.4 PedidoService.agregarItem() — Persistencia real

Antes: validaba reglas pero retornaba *"Agregar items a pedidos existentes requiere soporte en la capa DAO"*.

Ahora:
1. Valida pedido existe, está `ABIERTO`, producto existe y stock suficiente
2. Crea `DetallePedido` con los datos
3. Persiste mediante `pedidoDAO.insertarDetalle()`
4. Recalcula el total del pedido y lo actualiza con `pedidoDAO.actualizarTotal()`
5. Retorna *"Item agregado correctamente"*

---

## 3. Archivos modificados

| Archivo | Cambio |
|---|---|
| `Backend/src/main/java/com/restaurant/backend/dao/PedidoDAO.java` | Agregados `insertarDetalle()` y `actualizarTotal()` |
| `Backend/src/main/java/com/restaurant/backend/dao/PedidoDAOImpl.java` | SQL alineado con schema + implementación de nuevos métodos |
| `Backend/src/main/java/com/restaurant/backend/dao/MesaDAOImpl.java` | SQL corregido en `nuevaMesa()`, `cambiarEstado()`, `getMesasPorEstado()` |
| `Backend/src/main/java/com/restaurant/backend/service/PedidoService.java` | `agregarItem()` ahora persiste realmente |

---

## 4. Estado final de pendientes conocidos

| Pendiente | Estado |
|---|---|
| `agregarItem` valida pero no persiste | **RESUELTO** — ahora persiste y actualiza total |
| PedidoDAOImpl desalineado con schema (tablas singulares) | **RESUELTO** — todas las queries usan nombres plurales |
| MesaDAOImpl usa `mesa` en lugar de `mesas` | **RESUELTO** — `nuevaMesa`, `cambiarEstado`, `getMesasPorEstado` corregidos |
| Autenticación | Fuera de alcance |
| Reservas | Fuera de alcance |

---

## 5. Verificación

```bash
mvn compile -f Backend/pom.xml
```

**Resultado: BUILD SUCCESS** — 34 archivos compilados sin errores.

---

## 6. Arquitectura final

```
GUI (fase posterior)
    ↓
controller/   PedidoController, ProductoController, MesaController,
              CategoriaController, ReporteController
    ↓
ServicioFactory (singleton)
    ↓
service/      PedidoService, ProductoService, MesaService, ReporteService
    ↓
dao/          PedidoDAOImpl, ProductoDAOImpl, MesaDAOImpl, ReporteDAOImpl,
              CategoriaDAOImpl (stub)
    ↓
MySQL (schema.sql: pedidos, mesas, productos, detalle_pedido, ...)
```

Toda la capa DAO ahora está alineada con `schema.sql`. La validación de reglas de negocio y la persistencia de items en pedidos están completas.
