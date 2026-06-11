# Análisis del Frontend (GUI) — Trazabilidad Commits vs Planificación

> Basado en la rama `layer/view` (fusionada en `develop`) y la planificación en `divisionTareasPlan/divisiontareas.html`

---

## Estructura de commits de Frontend (autor: EnzoLlanos)

```
66d0473  termine las vistas
8204c96  ReservasDialog
088b3d2  checkout
35428b4  Replace GUI/ with Login project views (NetBeans Java)
```

Commit adicional de infraestructura (Niquinhoo, en `develop`):
```
a534509  Configure local project libraries
```

---

## Trazabilidad commit por commit

### Commit `35428b4` — "Replace GUI/ with Login project views (NetBeans Java)"
**Autor:** EnzoLlanos | **Archivos:** +37 archivos (+10.862 líneas)

#### Archivos de vista creados

| Archivo | Líneas | Descripción |
|---|---|---|
| `vistas/Login.java` | 230 | Pantalla de inicio de sesión |
| `vistas/Registro.java` | 246 | Pantalla de registro de usuario |
| `vistas/Menu.java` | 1.197 | Shell principal con sidebar + CardLayout |
| `vistas/Mesas.java` | 702 | **(eliminado después)** Pantalla standalone de mesas |
| `vistas/paneles/CardProducto.java` | 124 | Componente tarjeta de producto reutilizable |
| `vistas/paneles/DetallesMesasPanel.java` | 288 | Panel de detalle de mesa |
| `vistas/paneles/MesasPanel.java` | 434 | Mapa de 16 mesas |
| `vistas/paneles/PedidosPanel.java` | 260 | Lista/tabla de pedidos |
| `vistas/paneles/ProductosPanel.java` | 261 | Tabla ABM de productos |
| `vistas/paneles/ReportesPanel.java` | 172 | Contenedor de reportes |
| `vistas/paneles/UsuarioPanel.java` | 176 | **(eliminado después)** Panel de usuario |

#### Assets incluidos
- `com/imagen/logoSistemaNaranja.png` — Logo principal
- `com/imagen/icon-icons (1).png` — Icono de usuario
- `com/imagen/iconoMenu.png` / `iconoMenuNaranja.png` — Iconos de menú
- `com/imagen/Captura de pantalla 2026-06-07 171854.png` — Screenshot/logo

#### Proyecto NetBeans
- `build.xml` — Build de Ant
- `manifest.mf` — Manifest para JAR
- `nbproject/` — Configuración completa del proyecto NetBeans

#### Funcionalidades implementadas en este commit

**Login:**
- Campo de texto para usuario
- Campo de contraseña (JPasswordField)
- Botón "ENTRAR" que imprime credenciales en consola y abre Menu
- Label "Registrate" que navega a Registro

**Registro:**
- Campo de email, usuario y contraseña
- Botón "ENTRAR" que imprime datos en consola y abre Menu
- Label "Iniciar Sesión" que navega a Login

**Menu (shell principal):**
- Sidebar con 6 ítems: Menú, Mesas, Pedidos, Productos, Reportes, Configuraciones
- CardLayout para navegación entre paneles
- Resaltado visual del ítem activo (naranja sobre fondo oscuro)
- Encabezado con fecha, hora y panel de usuario

**Pestaña "Menú" (la principal):**
- 7 categorías clickeables: Todas, Entradas, Pizzas, Hamburguesas, Pastas, Bebidas, Postres
- Grid de productos (GridLayout 3 columnas) con 15 tarjetas hardcodeadas
- Tabla de pedidos con columnas: Producto, Cant., Precio, [X]
- Botón "+" en tarjetas agrega producto a la tabla
- Click en columna [X] elimina fila y recalcula total
- Total dinámico que se actualiza al agregar/eliminar
- Botón "Cancelar Pedido" y "Confirmar Pedido" (aún sin acción real)

**Panel Mesas:**
- 16 botones (Mesa 1 a Mesa 16) en verde simulando disponibles
- Mostrador/barra decorativa de madera
- DetallesMesasPanel embebido y oculto inicialmente

**DetallesMesasPanel:**
- Label de información de mesa
- Tabla de reservas (5 columnas: Cliente, Fecha, Hora, Seña, Pers.)
- Botones: Ocupar (rojo), Liberar (verde, oculto), Reservar (amarillo), Cancelar Reserva
- Método `mostrarMesa()` que actualiza la vista

**Panel Pedidos:**
- Tabla con 6 columnas: Mesa, Nombre, Cant., Precio, Estado, Hora
- Celdas editables solo en Nombre y Cant.

**Panel Productos (ABM):**
- Tabla con 5 columnas: Nombre, Stock, Precio, Id, Cant. Vendidos
- Método `listarProductos()` vacío (sin implementar)

**Panel Reportes:**
- Contenedor vacío con estructura base

**CardProducto:**
- Label nombre, label precio, botón "+" naranja
- Interface `OnAgregarListener` para callback al Menu

#### Tareas planificadas cubiertas
- ✅ VentanaLogin (Login.java)
- ✅ VentanaMenu (Menu.java)
- ✅ VentanaMesas (MesasPanel.java + DetallesMesasPanel.java)
- ✅ VentanaPedido (PedidosPanel.java)
- ✅ Main.java (Login.java como entry point)
- ✅ VentanaABM (ProductosPanel.java) — solo UI
- ✅ VentanaReportes (ReportesPanel.java) — solo UI base

---

### Commit `088b3d2` — "checkout"
**Autor:** EnzoLlanos | **Archivos:** +12 archivos (+1.712 líneas)

#### Archivos creados

| Archivo | Líneas | Descripción |
|---|---|---|
| `vistas/CheckoutDialog.java` | 440 | Diálogo modal de confirmación de pedido |

#### Archivos modificados
| Archivo | Cambio |
|---|---|
| `vistas/Menu.java` | Conexión del botón "Confirmar Pedido" → abre CheckoutDialog |
| `vistas/paneles/CardProducto.java` | Ajustes de integración |
| `vistas/paneles/DetallesMesasPanel.java` | Refactor de nombres y layout |
| `vistas/paneles/MesasPanel.java` | Refactor de navegación |
| `vistas/paneles/PedidosPanel.java` | Refactor de tabla |

#### Funcionalidad implementada
**CheckoutDialog:**
- Selector de mesa (JComboBox, carga dinámica)
- Fecha y hora actual
- Lista de productos (placeholder)
- Subtotal, campo de descuento, total
- Método de pago: Efectivo, Débito, Crédito, Mercado Pago
- Campo de observaciones
- Botones Confirmar / Cancelar
- Getters públicos: getMetodoPago, getObservaciones, getDescuento, getMesaSeleccionada

#### Tareas cubiertas
- ✅ CheckoutDialog (funcionalidad no planificada originalmente, necesaria para flujo de pedido completo)

---

### Commit `8204c96` — "ReservasDialog"
**Autor:** EnzoLlanos | **Archivos:** +11 archivos (+737 líneas)

#### Archivos creados
| Archivo | Líneas | Descripción |
|---|---|---|
| `vistas/ReservaDialog.java` | 291 | Diálogo modal de reserva de mesa |

#### Archivos modificados
| Archivo | Cambio |
|---|---|
| `vistas/CheckoutDialog.java` | Ajustes en layout |
| `vistas/Login.java` | Ajustes en flujo de navegación |
| `vistas/Menu.java` | Ajustes en navegación de sidebar |
| `vistas/Registro.java` | Ajustes en formulario |
| `vistas/paneles/CardProducto.java` | Ajuste menor |
| `vistas/paneles/DetallesMesasPanel.java` | Conexión botón "Reservar" → abre ReservaDialog |

#### Funcionalidad implementada
**ReservaDialog:**
- Campo de nombre (JTextField)
- Campo de teléfono (JTextField)
- DateTimePicker (LGoodDatePicker) con límite desde fecha actual
- Botones Confirmar / Cancelar
- Getters: isConfirmado, getNombre, getTelefono, getFechaHora
- Impresión en consola de datos de reserva al confirmar

#### Tareas cubiertas
- ✅ ReservaDialog (funcionalidad no planificada originalmente, da soporte al botón "Reservar" en DetallesMesasPanel)

---

### Commit `66d0473` — "termine las vistas"
**Autor:** EnzoLlanos | **Archivos:** +14 archivos (+1.679 líneas, -2.207 líneas)

#### Archivos creados
| Archivo | Líneas | Descripción |
|---|---|---|
| `vistas/paneles/reportes/GeneralPanel.java` | 363 | KPIs + gráficos generales |
| `vistas/paneles/reportes/VentasPanel.java` | 366 | KPIs + gráficos de ventas |

#### Archivos eliminados
| Archivo | Razón |
|---|---|
| `vistas/Mesas.java` + `Mesas.form` | Reemplazado por MesasPanel (mejor integración con CardLayout) |
| `vistas/paneles/UsuarioPanel.java` + `UsuarioPanel.form` | No se utilizó, funcionalidad cubierta en Login/Registro |

#### Archivos modificados
| Archivo | Cambio |
|---|---|
| `vistas/Menu.java` | Conexión de todos los paneles al CardLayout, ajustes finales |
| `vistas/paneles/MesasPanel.java` | Refactor final, integración con CardLayout |
| `vistas/paneles/ReportesPanel.java` | CardLayout interno con GeneralPanel + VentasPanel |

#### Funcionalidad implementada

**GeneralPanel:**
- 3 KPIs con valores hardcodeados: Ventas Totales ($00000), Pedidos Totales (100), Ticket Promedio ($132.12)
- Gráfico de barras (JFreeChart): ventas por día de la semana
- Gráfico de anillo (Ring Chart): productos más vendidos

**VentasPanel:**
- 3 KPIs hardcodeados: Ventas Hoy ($00000), Ventas Semanales ($00000), Ventas Mensuales ($00000)
- Gráfico de líneas (JFreeChart): ventas acumuladas por día del mes
- Gráfico de anillo (Ring Chart): recaudación por producto

**Limpieza de código muerto:**
- Eliminación de `Mesas.java` (redundante con `MesasPanel.java`)
- Eliminación de `UsuarioPanel.java` (no utilizado)

#### Tareas planificadas cubiertas
- ✅ VentanaReportes completa (GeneralPanel + VentasPanel con gráficos JFreeChart)

---

### Commit `a534509` — "Configure local project libraries" (en develop)
**Autor:** Niquinhoo | **Archivos:** +4 archivos

#### Archivos agregados
| Archivo | Propósito |
|---|---|
| `GUI/lib/AbsoluteLayout.jar` | Layout manager de NetBeans |
| `GUI/lib/LGoodDatePicker.jar` | DatePicker para reservas |
| `GUI/lib/jfreechart-1.5.4.jar` | Librería de gráficos para reportes |

#### Configuración
- `GUI/nbproject/project.properties`: classpath actualizado para incluir los 3 JARs, source/target cambiado de 26 a 17

#### Tareas cubiertas
- ✅ Las 3 dependencias externas del frontend están presentes y configuradas

---

## Mapa completo: Tareas planificadas vs realizadas

### Integrante 4 — Vistas Swing (pantallas del cliente/mozo)

| Tarea Planificada | Estado | Archivo(s) | Commit | Detalle |
|---|---|---|---|---|
| VentanaLogin.java | ✅ Completo | `Login.java` | `35428b4` | JTextField + JPasswordField + botón Entrar + navegación a Registro |
| VentanaMesas.java | ✅ Completo | `MesasPanel.java` + `DetallesMesasPanel.java` | `35428b4` + `66d0473` | 16 botones de mesa, panel de detalle, botones Ocupar/Liberar/Reservar |
| VentanaMenu.java | ✅ Completo | `Menu.java` | `35428b4` + `088b3d2` + `8204c96` | Categorías filtrables, grid de productos, tabla de pedidos, total dinámico |
| VentanaPedido.java | ✅ Completo | `PedidosPanel.java` | `35428b4` | JTable con 6 columnas (Mesa, Nombre, Cant., Precio, Estado, Hora) |
| Main.java | ✅ Completo | `Login.java` (main) | `35428b4` | Entry point desde Login |

### Integrante 5 — ABM, reportes, informe PDF y despliegue

| Tarea Planificada | Estado | Archivo(s) | Commit | Detalle |
|---|---|---|---|---|
| VentanaABM.java | 🟡 Parcial | `ProductosPanel.java` | `35428b4` | Tabla con columnas (Nombre, Stock, Precio, Id, Cant. Vendidos). **Sin lógica CRUD ni botones de crear/editar/borrar** |
| VentanaReportes.java | 🟡 Parcial | `ReportesPanel.java` + `GeneralPanel.java` + `VentasPanel.java` | `35428b4` + `66d0473` | KPIs + gráficos JFreeChart. **Datos hardcodeados, sin filtros reales** |
| Exportar PDF | ❌ No implementado | — | — | No hay código de iText/PDFBox en ninguna parte |
| pom.xml dependencias | 🟡 Parcial | — | `a534509` | Las librerías JAR están en `GUI/lib/`, pero el proyecto usa Ant, no Maven. No hay shade plugin |
| JAR ejecutable | 🟡 Parcial | `GUI/dist/Login.jar` | — | Existe un pre-build de prueba. No es el JAR multi-módulo definitivo |
| Informe proyecto | ❌ No implementado | — | — | No se encontró informe entregable |

### Funcionalidades NO planificadas (adicionales)

| Funcionalidad | Archivo(s) | Commit | Justificación |
|---|---|---|---|
| Ventana Registro | `Registro.java` | `35428b4` | Necesaria para el flujo de registro de usuarios |
| Checkout Dialog | `CheckoutDialog.java` | `088b3d2` | Necesaria para confirmar pedidos con método de pago, descuento y selección de mesa |
| Reserva Dialog | `ReservaDialog.java` | `8204c96` | Necesaria para el botón "Reservar" en DetallesMesasPanel |
| CardProducto | `paneles/CardProducto.java` | `35428b4` | Componente reutilizable para la grilla de productos |
| Estilo visual unificado | Todos los archivos | Todos | Tema oscuro consistente (fondo `rgb(36,30,26)`, acento naranja `rgb(249,155,32)`) |

---

## Resumen cuantitativo del frontend

| Métrica | Valor |
|---|---|
| Commits de frontend (EnzoLlanos) | 4 |
| Commits de infraestructura frontend (Niquinhoo) | 1 |
| Total archivos .java en GUI | 13 clases |
| Total líneas de código Java en GUI | ~4.200 líneas |
| Total archivos .form (NetBeans GUI Builder) | 13 formularios |
| Librerías externas incluidas | 3 (AbsoluteLayout, LGoodDatePicker, JFreeChart) |
| Assets gráficos | 5 imágenes |
| Pantallas/diálogos implementados | 8 (Login, Registro, Menu, CheckoutDialog, ReservaDialog, MesasPanel, PedidosPanel, ProductosPanel, ReportesPanel) |
| Sub-paneles de reportes | 2 (GeneralPanel, VentasPanel) |
| Componentes reutilizables | 1 (CardProducto) |

---

## Estado de integración con backend

Ninguna vista está conectada al backend. Todos los datos son hardcodeados.

| Vista | Datos actuales | Lo que debería recibir del backend |
|---|---|---|
| Login | Usuario/contraseña se imprimen en consola | `ServicioFactory.getAutenticacion().iniciarSesion()` |
| Registro | Email/usuario/contraseña se imprimen en consola | `ServicioFactory.getAutenticacion().registrar()` |
| Menu (productos) | 15 productos hardcodeados | `ServicioFactory.getProductoServicio().obtenerTodos()` / `obtenerPorCategoria()` |
| Menu (pedido) | Tabla manipulable pero no persiste | `ServicioFactory.getPedidoService().crear()` |
| CheckoutDialog | Solo captura datos, no los envía | Persistencia del pedido + calcularTotal |
| MesasPanel | 16 botones verdes fijos | `ServicioFactory.getMesaServicio().listar()` |
| DetallesMesasPanel | Tabla de reservas hardcodeada | `ServicioFactory.getMesaServicio().obtenerPorNumero()` / `ocupar()` / `liberar()` |
| ReservaDialog | Imprime datos en consola | `ServicioFactory.getReservaServicio().crearReserva()` |
| PedidosPanel | Sin datos reales | `ServicioFactory.getProductoServicio().obtenerTodos()` |
| ProductosPanel | Sin datos reales | CRUD completo de ProductoService |
| Reportes | KPIs y gráficos hardcodeados | ReporteService con consultas reales a BD |

**ServicioFactory no existe en el código** — es solo una clase referenciada en TODOs como interfaz planeada.

---

## Conclusión: ¿Qué tan avanzado está el frontend?

```
Plan original (Integrante 4): ████████████████░  95%  (todas las pantallas existen)
Plan original (Integrante 5): ██████░░░░░░░░░░░  30%  (solo UI de ABM y reportes)
Integración con backend:     ░░░░░░░░░░░░░░░░░   0%  (nada conectado)
Funcionalidades adicionales:  +3 (Registro, Checkout, Reserva)

El frontend tiene TODAS las pantallas diseñadas y funcionales
en términos de UI. Lo que falta es exclusivamente la conexión
con el backend (aprox. 1:1 mapeo de TODOs → servicios a implementar).
```
