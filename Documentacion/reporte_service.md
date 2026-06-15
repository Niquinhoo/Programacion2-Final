# Reporte de Estado — Capa Service y Controladores (Backend)

Este reporte contrasta la planificación inicial de la capa de servicios y controladores del Backend con la implementación real en el código, evaluando el estado de cada tarea solicitada.

---

## 1. Contraste: Planificación vs. Código Real

Al comparar el plan de diseño ([capa_service_backend_3643b021.plan.md](file:///c:/Users/nicot/Desktop/Programacion2-Final/Documentacion/capa_service_backend_3643b021.plan.md)) y el inventario de cambios ([plan_capa_service.md](file:///c:/Users/nicot/Desktop/Programacion2-Final/Documentacion/plan_capa_service.md)) con el código fuente del backend, se observa lo siguiente:

1. **Estructura y Singleton**: La clase [ServicioFactory.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/ServicioFactory.java) está completamente implementada usando el patrón Singleton Lazy para proveer instancias de `ProductoService`, `MesaService`, `PedidoService` y `ReporteService`.
2. **Servicios Core**:
   - [ProductoService.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/ProductoService.java): Implementa la lógica ABM, validaciones de stock/precio mayor a cero y filtrado por categoría en memoria.
   - [MesaService.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/MesaService.java): Implementa la transición de estados de mesas (`LIBRE`, `OCUPADA`, `RESERVADA`, `FUERA_DE_SERVICIO`) aplicando las reglas del diagrama de estados.
   - [PedidoService.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/PedidoService.java): Implementa la creación, cancelación y cierre de pedidos con descuento de stock y actualización de estado de las mesas.
   - [ReporteService.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/ReporteService.java): Delega la generación de datos para reportes a la base de datos a través de `ReporteDAO`.
3. **Avance sobre lo planificado (DAO)**:
   - El plan indicaba que la persistencia en `agregarItem` de `PedidoService` estaba *"pendiente de DAO"*. Sin embargo, al revisar [PedidoDAOImpl.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/dao/PedidoDAOImpl.java) se observa que **se implementaron con éxito** los métodos `insertarDetalle` y `actualizarTotal` bajo JDBC, permitiendo la completa persistencia y recálculo de montos para ítems individuales.
4. **Controladores**:
   - Todos los controladores (`MesaController`, `PedidoController`, `ProductoController`, `CategoriaController` y `ReporteController`) están completamente implementados y delegando su lógica de negocio a la capa `service` mediante `ServicioFactory`.
5. **División Frontend (GUI)**:
   - Al contrastar con el Frontend en `/GUI/src`, la conexión hacia los controladores **no está realizada**. Los ActionListeners e invocaciones a `ServicioFactory` y `Controllers` en las vistas (`Login.java`, `Registro.java`, `Menu.java`, `ReservaDialog.java`, etc.) se encuentran comentadas, por lo que la interfaz gráfica opera de forma autónoma con datos hardcodeados y simulados.

---

## 2. Estado de Tareas (17 ítems solicitados)

A continuación se detalla el estado actual de cada una de las tareas del listado provisto:

### 🟢 Tareas REALIZADAS (Completadas en el Backend)

| Tarea | Componente | Prioridad | Detalle de Implementación / Evidencia |
|---|---|---|---|
| **Diseñar ServicioFactory** | Backend | Media | Implementado en [ServicioFactory.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/ServicioFactory.java) como un Singleton lazy que expone instancias de los 4 servicios. |
| **Implementar ProductoServicio** | Backend | Media | Implementado en [ProductoService.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/ProductoService.java). Incluye métodos ABM, validaciones de stock suficiente y precios no negativos. |
| **Implementar MesaServicio** | Backend | Media | Implementado en [MesaService.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/MesaService.java). Gestiona la ocupación, liberación, reservas y validación de transiciones de estados de mesas. |
| **Implementar PedidoServicio** | Backend | Media | Implementado en [PedidoService.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/PedidoService.java). Resuelve el cálculo de subtotales/totales, validación de stock disponible antes de confirmar y descuento de stock al cerrar el pedido. |
| **Implementar ReporteServicio** | Backend | Baja | Implementado en [ReporteService.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/ReporteService.java) y [ReporteDAOImpl.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/dao/ReporteDAOImpl.java). Ejecuta queries de agregación sobre `pedidos` y las vistas `vw_ventas_por_producto` y `vw_ventas_por_mes`. |
| **Completar Controllers (Puentes)** | Backend | Baja | Implementados todos los controladores en `com.restaurant.backend.controller` (`MesaController`, `PedidoController`, `ProductoController`, `CategoriaController` y `ReporteController`) actuando como puentes directos hacia los servicios. |

---

### 🔴 Tareas PENDIENTES (Por Hacer)

| Tarea | Componente | Prioridad | Motivo del Estado / Diagnóstico |
|---|---|---|---|
| **Implementar Exportación PDF** | Backend | Baja | Sin desarrollar. No hay librerías de generación de PDF (iText / PDFBox) configuradas en el pom ni código que exporte facturas o reportes. |
| **Configurar JAR Único y Maven Multi-módulo** | Backend | Baja | Incompleto. El [pom.xml](file:///c:/Users/nicot/Desktop/Programacion2-Final/pom.xml) raíz solo declara como módulo al `Backend`. La `GUI` sigue siendo un proyecto basado en Ant (con `build.xml`), por lo que no están integrados en un empaquetado Maven multi-módulo final. |
| **Desarrollar CRUD Funcional de Productos** | Frontend | Baja | Pendiente de integración. El panel [ProductosPanel.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/GUI/src/vistas/paneles/ProductosPanel.java) tiene su método `listarProductos()` vacío y no cuenta con interfaz gráfica/lógica de botones para Altas, Bajas y Modificaciones en BD. |
| **Conectar Reportes a JFreeChart Real** | Frontend | Baja | Pendiente. Los paneles `GeneralPanel.java` y `VentasPanel.java` inicializan componentes JFreeChart utilizando datos estáticos/hardcodeados en lugar de invocar a `ReporteController`. |
| **Conectar ReservaDialog a Base de Datos** | Frontend | Baja | Pendiente. [ReservaDialog.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/GUI/src/vistas/ReservaDialog.java) es un diálogo Swing aislado que captura datos pero no persiste en base de datos al no haber capa de reserva en el backend. |
| **Confirmar Pedido y Checkout en GUI** | Frontend | Media | Pendiente. El diálogo de Checkout no invoca al `PedidoController` para persistir la orden ni realizar la rebaja de stock. |
| **Integrar Mapa de Mesas en GUI** | Frontend | Media | Pendiente. El panel [MesasPanel.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/GUI/src/vistas/paneles/MesasPanel.java) muestra las mesas de forma estática (todas pintadas en verde) sin conectarse a `MesaController` para evaluar el estado real (`OCUPADA`, `RESERVADA`, etc.) dinámicamente. |
| **Cargar Menú y Tarjetas Dinámicamente** | Frontend | Media | Pendiente. El menú de productos en `Menu.java` tiene productos hardcodeados y sus consultas mediante `ServicioFactory` están desactivadas (comentadas). |
| **Implementar ReservaServicio** | Backend | Media | Pendiente. Excluido del alcance del backend actual. No existen las tablas de reservas en la base de datos ni clases modelo/servicio correspondientes. |
| **Integrar Login y Registro en la GUI** | Frontend | Media | Pendiente. Los archivos `Login.java` y `Registro.java` tienen comentados los accesos al backend y simulan el login localmente. |
| **Implementar AutenticacionServicio** | Backend | Media | Pendiente. No existe clase para la gestión/validación de credenciales a nivel de servicio ni capa de datos (`UsuarioDAO` o `AutenticacionServicio`) en el Backend. |

---

## 3. Conclusión y Siguientes Pasos

La capa de negocio (`service/`) y los controladores del **Backend están al 100% de lo planificado para esta etapa**, incluso habiendo adelantado métodos que originalmente estaban marcados como "pendientes del DAO" (como `insertarDetalle` y `actualizarTotal` en pedidos). 

El gran volumen de tareas pendientes radica en la **capa Frontend (GUI)** y en la **integración (conexión)** entre ambas capas, ya que la GUI actualmente funciona con datos hardcodeados y tiene las invocaciones al backend comentadas.
