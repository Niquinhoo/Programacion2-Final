# Análisis de Avance — GUI del Sistema Restaurante

> Comparación entre la planificación en `divisionTareasPlan/` y el estado real del código en `develop` (rama `layer/view` fusionada).

---

### ③ Integrante 3 — Service y Controller
| Planificado | Real | Estado |
|---|---|---|
| PedidoService.java | **No existe** (no hay carpeta `service/`) | ❌ No implementado |
| ProductoService.java | **No existe** | ❌ No implementado |
| MesaService.java | **No existe** | ❌ No implementado |
| ReporteService.java | **No existe** | ❌ No implementado |
| PedidoController.java | `PedidoController.java` (stub vacío, 4 líneas) | ⚠️ Stub |
| ProductoController.java | `ProductoController.java` (stub vacío, 4 líneas) | ⚠️ Stub |
| MesaController.java | `MesaController.java` (stub vacío, 4 líneas) | ⚠️ Stub |
| CategoriaController.java | (no planificado pero existe, stub vacío) | ⚠️ Stub |

**Conclusión: Capa Service inexistente. Controllers vacíos.**
- La rama `layer/service-controller` no tiene commits de feature (solo setup inicial).
- Los TODO en las vistas referencian `ServicioFactory.get*()` pero esa clase no existe.

---

### ④ Integrante 4 — Vistas Swing (cliente/mozo)
| Planificado | Real | Estado |
|---|---|---|
| VentanaLogin.java | `GUI/src/vistas/Login.java` | ✅ UI completa con dark theme |
| VentanaMesas.java | `GUI/src/vistas/paneles/MesasPanel.java` + `DetallesMesasPanel.java` | ✅ 16 botones de mesa + panel de detalle |
| VentanaMenu.java | `GUI/src/vistas/Menu.java` (JFrame principal) | ✅ Cat. filtrables, grilla productos, tabla pedido |
| VentanaPedido.java | `GUI/src/vistas/paneles/PedidosPanel.java` | ✅ Tabla con columnas Mesa, Cant, Precio, Estado, Hora |
| Main.java | `Login.java` como entry point | ✅ Arranque desde Login |
| **Adicionales** | `Registro.java` | ✅ Pantalla de registro de usuario |
| **Adicionales** | `ReservaDialog.java` | ✅ Diálogo modal con DatePicker |
| **Adicionales** | `CheckoutDialog.java` | ✅ Diálogo con método pago, descuento, combo mesa |
| **Adicionales** | `CardProducto.java` | ✅ Componente reutilizable de tarjeta de producto |
| **Adicionales** | `ProductosPanel.java` | ✅ Tabla ABM de productos |
| **Adicionales** | `ReportesPanel.java` + `GeneralPanel.java` + `VentasPanel.java` | ✅ Paneles de reportes con JFreeChart |
| **Adicionales** | Imágenes en `com/imagen/` | ✅ Assets visuales (logo, iconos) |

**Conclusión: GUI casi completa en términos de UI/UX.**
- Tema oscuro coherente en todas las pantallas.
- CardLayout para navegación sidebar.
- Todos los formularios y paneles están armados.
- **Pero**: 100% datos hardcodeados. Ninguna conexión al backend.

---

### ⑤ Integrante 5 — ABM, reportes, PDF, despliegue
| Planificado | Real | Estado |
|---|---|---|
| VentanaABM.java | `ProductosPanel.java` | ✅ UI de tabla (sin CRUD real) |
| VentanaReportes.java | `ReportesPanel.java` + `GeneralPanel` + `VentasPanel` | ✅ KPIs + gráficos JFreeChart (datos hardcodeados) |
| Exportar PDF | **No implementado** | ❌ |
| pom.xml dependencias | `Backend/pom.xml` con MySQL connector | ✅ |
| JAR ejecutable | `GUI/dist/Login.jar` existe (pre-build) | 🟡 JAR de prueba, no el definitivo |
| Informe del proyecto | `Documentacion/reporte_primera_etapa.md` | 🟡 Parcial |

**Conclusión: UI de ABM y reportes armada, pero sin lógica real. Sin exportación PDF.**

---

## 2. Estado de las ramas

| Rama | Responsable | Commits de feature | Estado |
|---|---|---|---|
| `layer/model` | Integrante 1 | 4 commits (modelos, DB, seed, docs) | ✅ Terminado |
| `layer/dao` | Integrante 2 | 0 (solo setup) | ❌ No arrancado |
| `layer/service-controller` | Integrante 3 | 0 (solo setup) | ❌ No arrancado |
| `layer/view` | Integrante 4 | 7 commits (vistas completas) | ✅ Terminado y fusionado a `develop` |
| `layer/admin-reports` | Integrante 5 | 0 (solo setup) | ❌ No arrancado |
| `feature/tests-dao` | — | Pendiente de revisar | ❓ |

**Nota:** `develop` contiene todo el trabajo de `layer/model` y `layer/view`. Las otras ramas nunca recibieron commits de feature.

---

## 3. Análisis de Integración Frontend ↔ Backend

### Lo que la GUI espera del backend (según TODOs en el código):

```
ServicioFactory.getAutenticacion().iniciarSesion()   → Login.java
ServicioFactory.getAutenticacion().registrar()         → Registro.java
ServicioFactory.getProductoServicio().obtenerTodos()   → Menu.java / ProductosPanel.java
ServicioFactory.getProductoServicio().obtenerPorCategoria() → Menu.java
ServicioFactory.getMesaServicio().obtenerPorNumero()   → DetallesMesasPanel.java
ServicioFactory.getMesaServicio().ocupar()             → DetallesMesasPanel.java
ServicioFactory.getMesaServicio().liberar()            → DetallesMesasPanel.java
ServicioFactory.getReservaServicio().crearReserva()    → ReservaDialog.java
```

### Realidad:
- `ServicioFactory` **no existe** en ninguna parte del código.
- Los Controllers están vacíos (4 líneas cada uno).
- Los DAOs están vacíos (interfaces sin métodos).
- No hay Service layer.

### Dependencias de librerías (GUI usa NetBeans Ant, no Maven):
| Librería | Ubicación | Uso |
|---|---|---|
| AbsoluteLayout.jar | `GUI/lib/` | Layout de NetBeans |
| JFreeChart 1.5.4 | `GUI/lib/` | Gráficos en reportes |
| LGoodDatePicker | `GUI/lib/` | DatePicker en reservas |
| MySQL Connector | `Backend/pom.xml` (Maven) | Conexión BD |

---

## 4. Resumen de Avance General del Proyecto

| Capa | Avance | Lo que falta |
|---|---|---|
| **Modelos** | 100% | — |
| **Base de datos** (schema + seed) | 100% | — |
| **DAO / SQL** | ~15% | Implementar todos los métodos CRUD en interfaces e impls |
| **Service / lógica negocio** | 0% | Crear capa service completa + ServicioFactory |
| **Controllers** | ~10% | Stubs vacíos, implementar lógica de conexión service↔view |
| **Vistas Swing** (cliente) | ~90% | Conectar con backend, reemplazar datos hardcodeados |
| **Vistas ABM** | ~60% | Lógica CRUD real, conexión a service |
| **Vistas Reportes** | ~60% | Consultas reales a BD, reemplazar datos hardcodeados |
| **Exportación PDF** | 0% | No implementado |
| **JAR ejecutable** | ~30% | Build pre-build de prueba, falta Maven multi-módulo |

### Semáforo general:

```
🟢 Modelo + BD         → COMPLETO
🟡 Vistas Swing (GUI)  → ALTO AVANCE (solo falta integración)
🔴 DAO                 → STUBS VACÍOS
🔴 Service             → NO EXISTE
🔴 Controllers         → STUBS VACÍOS
🔴 PDF / Despliegue    → NO ARRANCADO
```

### Dependencias bloqueantes:

```
Modelos ✅ → DAOs ❌ → Service ❌ → Controllers ⚠️ → Vistas 🟡
                                                         ↓
                                              ABM/Reportes 🟡 → PDF ❌
```

Las vistas están adelantadas respecto a las capas inferiores. El orden correcto de finalización sería:
1. **DAO** (Integrante 2) — desbloquea todo lo demás
2. **Service** (Integrante 3) — lógica de negocio
3. **Controllers** (Integrante 3) — puente con vistas
4. **Integración GUI** (Integrante 4) — conectar datos reales
5. **Reportes + PDF + JAR** (Integrante 5) — cerrar el entregable

---

## 5. Archivos de la GUI (inventario completo)

```
GUI/src/vistas/
├── Login.java                    → Autenticación
├── Registro.java                 → Registro de usuario
├── Menu.java                     → Shell principal con sidebar + CardLayout
├── ReservaDialog.java            → Diálogo de reserva
├── CheckoutDialog.java           → Diálogo de confirmación de pedido
├── paneles/
│   ├── MesasPanel.java           → Mapa de 16 mesas
│   ├── DetallesMesasPanel.java   → Detalle de mesa + acciones
│   ├── ProductosPanel.java       → ABM productos (tabla)
│   ├── CardProducto.java         → Tarjeta de producto individual
│   ├── PedidosPanel.java         → Lista de pedidos
│   └── ReportesPanel.java        → Contenedor de reportes
│   └── reportes/
│       ├── GeneralPanel.java     → KPIs + gráfico barras + dona
│       └── VentasPanel.java      → KPIs + gráfico líneas + dona
GUI/src/com/imagen/               → Recursos visuales
GUI/lib/                          → Librerías third-party
```

---

*Documentación generada el 11/06/2026 basada en el código en la rama `develop` y planificación en `divisionTareasPlan/`.*
