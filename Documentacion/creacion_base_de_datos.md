# Registro de Creación de la Base de Datos

Este documento detalla la ejecución de los scripts de creación del esquema de la base de datos `restaurante_db` en la instancia de **TiDB Cloud Serverless**.

---

## 1. Información General de la Ejecución

* **Fecha/Hora de Ejecución:** 2026-06-09 19:05:33 - 19:05:38
* **Base de Datos:** `restaurante_db`
* **Codificación (Charset):** `utf8mb4`
* **Colación (Collate):** `utf8mb4_unicode_ci`
* **Entorno:** TiDB Cloud Serverless (puerto 4000)

---

## 2. Bitácora detallada de Ejecución (Logs)

A continuación se presenta el registro de cada sentencia ejecutada, su duración y estado:

| Paso | Hora | Sentencia SQL | Filas Afectadas | Duración | Estado / Advertencias |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **3** | 19:05:33 | `CREATE DATABASE IF NOT EXISTS restaurante_db...` | 0 | 0.235 s | ✅ Éxito |
| **4** | 19:05:33 | `USE restaurante_db` | 0 | 0.156 s | ✅ Éxito |
| **5** | 19:05:33 | `CREATE TABLE IF NOT EXISTS roles (...)` | 0 | 0.219 s | ✅ Éxito |
| **6** | 19:05:33 | `CREATE TABLE IF NOT EXISTS usuarios (...)` | 0 | 0.266 s | ✅ Éxito |
| **7** | 19:05:34 | `CREATE TABLE IF NOT EXISTS categorias (...)` | 0 | 0.218 s | ✅ Éxito |
| **8** | 19:05:34 | `CREATE TABLE IF NOT EXISTS productos (...)` | 0 | 0.250 s | ⚠️ 1 Advertencia (Ver Sección 3) |
| **9** | 19:05:34 | `CREATE TABLE IF NOT EXISTS mesas (...)` | 0 | 0.218 s | ⚠️ 1 Advertencia (Ver Sección 3) |
| **10** | 19:05:35 | `CREATE TABLE IF NOT EXISTS pedidos (...)` | 0 | 0.250 s | ⚠️ 1 Advertencia (Ver Sección 3) |
| **11** | 19:05:35 | `CREATE TABLE IF NOT EXISTS detalle_pedido (...)` | 0 | 0.265 s | ⚠️ 3 Advertencias (Ver Sección 3) |
| **12** | 19:05:35 | `CREATE INDEX idx_productos_categoria ON productos...` | 0 | 0.359 s | ✅ Éxito |
| **13** | 19:05:36 | `CREATE INDEX idx_productos_disponible ON productos...` | 0 | 0.328 s | ✅ Éxito |
| **14** | 19:05:36 | `CREATE INDEX idx_pedidos_mesa ON pedidos...` | 0 | 0.391 s | ✅ Éxito |
| **15** | 19:05:36 | `CREATE INDEX idx_pedidos_estado ON pedidos...` | 0 | 0.344 s | ✅ Éxito |
| **16** | 19:05:37 | `CREATE INDEX idx_pedidos_fecha ON pedidos...` | 0 | 0.328 s | ✅ Éxito |
| **17** | 19:05:37 | `CREATE INDEX idx_detalle_pedido ON detalle_pedido...` | 0 | 0.360 s | ✅ Éxito |
| **18** | 19:05:38 | `CREATE OR REPLACE VIEW vw_ventas_por_producto AS...` | 0 | 0.203 s | ✅ Éxito |
| **19** | 19:05:38 | `CREATE OR REPLACE VIEW vw_ventas_por_mes AS...` | 0 | 0.219 s | ✅ Éxito |
| **20** | 19:05:38 | `CREATE OR REPLACE VIEW vw_estado_mesas AS...` | 0 | 0.218 s | ✅ Éxito |

---

## 3. Análisis de Advertencias detectadas (TiDB Cloud)

Durante la creación de las tablas `productos`, `mesas`, `pedidos` y `detalle_pedido`, el motor de base de datos arrojó la siguiente advertencia:

> **Warning 1105:** `tidb_enable_check_constraint is off`

### ¿Qué significa?
En MySQL y TiDB, las restricciones `CHECK` (por ejemplo, `CHECK (precio >= 0)` o `CHECK (capacidad > 0)`) se utilizan para validar reglas de negocio directamente en el motor antes de insertar o modificar datos. 

Sin embargo, en TiDB Serverless, la validación activa de estas restricciones está **desactivada por defecto** a nivel global. Esto significa que las tablas se crean correctamente sin errores (el analizador sintáctico de SQL acepta las instrucciones `CHECK`), pero **no se aplicará ninguna validación automática en inserciones que violen la regla** (por ejemplo, se podría insertar un producto con precio negativo).

### ¿Cómo solucionarlo?
Si se desea que TiDB valide de forma activa estas restricciones, se debe ejecutar la siguiente variable del sistema en la base de datos con privilegios de administrador:

```sql
SET GLOBAL tidb_enable_check_constraint = ON;
```
*Nota: Para conexiones de sesión individuales también se puede configurar ejecutando `SET session tidb_enable_check_constraint = ON;` al inicializar la conexión en Java.*

---

## 4. Elementos Creados en el Esquema

Una vez finalizada la ejecución, el esquema contiene la siguiente estructura:

### Tablas (7)
1. `roles`: Define los roles de acceso al sistema (Administrador, Mozo, Cocina, etc.).
2. `usuarios`: Almacena las credenciales y el estado activo del personal del restaurante.
3. `categorias`: Agrupaciones del menú (bebidas, platos principales, postres, etc.).
4. `productos`: Catálogo de ítems con precio, stock y disponibilidad.
5. `mesas`: Control físico del salón (número, capacidad y estado actual).
6. `pedidos`: Cabecera de órdenes por mesa y mozo.
7. `detalle_pedido`: Relación de productos y cantidades de cada pedido.

### Índices (6)
* `idx_productos_categoria`: Optimiza las búsquedas de productos por categoría.
* `idx_productos_disponible`: Optimiza el filtrado de productos activos para el menú en Swing.
* `idx_pedidos_mesa`: Mejora el rendimiento al buscar pedidos por mesa asignada.
* `idx_pedidos_estado`: Agiliza las consultas del estado de las comandas (Ej: en cocina).
* `idx_pedidos_fecha`: Optimiza los reportes basados en fechas o rangos temporales.
* `idx_detalle_pedido`: Acelera la obtención del detalle de cada pedido cabecera.

### Vistas (3)
* `vw_ventas_por_producto`: Sumatoria de ventas físicas y recaudación por producto.
* `vw_ventas_por_mes`: Reporte consolidado mensual de ingresos y cantidad de órdenes.
* `vw_estado_mesas`: Vista de monitoreo en tiempo real que mapea las mesas con sus pedidos activos.
