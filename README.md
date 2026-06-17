# RestoManager — Sistema de Gestión de Restaurante

**RestoManager** es una aplicación de escritorio diseñada para la administración integral de restaurantes y salones. Permite gestionar mozos, mesas en tiempo real, catálogo de productos, comandas de pedidos con control de stock e informes estadísticos de ventas.

Este repositorio en su rama principal (`master`) contiene el programa ejecutable compilado listo para su uso. El código fuente completo y los scripts de desarrollo se encuentran disponibles en la rama de desarrollo (`develop`).

---

## 🚀 Cómo Ejecutar la Aplicación

La aplicación se distribuye como un archivo ejecutable portable de Java (`.jar`).

### Requisitos Previos:
1. **Java JRE o JDK (Versión 17 o superior):** Asegúrate de tener instalado Java en tu máquina ejecutando `java -version` en la terminal.
2. **Conexión a Internet:** La aplicación se conecta automáticamente a una base de datos remota en la nube (**TiDB Cloud**), por lo que requiere una conexión activa para funcionar.

### Instrucciones de Ejecución:
1. Descarga el archivo `RestoManager.jar` de este repositorio.
2. Haz **doble clic** sobre el archivo `RestoManager.jar` para iniciarlo.
3. Si no arranca por doble clic, abre la terminal o PowerShell en la carpeta donde descargaste el archivo y ejecuta:
   ```bash
   java -jar RestoManager.jar
   ```

*Para ingresar en la pantalla de acceso, puedes utilizar las credenciales de prueba por defecto:*
* **Usuario:** `nicolas`
* **Contraseña:** `nicolas`

---

## 📋 Funcionalidades Principales

1. **Autenticación de Usuarios:** Acceso seguro mediante inicio de sesión para Mozos y Administradores con contraseñas encriptadas por hash SHA2-256.
2. **Panel de Mesas Interactivo:** Visualización del salón en tiempo real. Los colores indican el estado de cada mesa:
   * 🟢 **Verde:** Libre
   * 🔴 **Rojo:** Ocupada
   * 🟠 **Naranja:** Reservada
   * ⚫ **Gris:** Fuera de Servicio
3. **Gestión de Pedidos y Comandas:** Creación de pedidos asociándolos a mesas, mozos y productos seleccionados.
4. **Checkout Dinámico:** Diálogo de cobro donde se puede ingresar el método de pago, añadir observaciones e ingresar códigos de descuentos interactivos (porcentuales o fijos).
5. **Impresión de Ticket:** Generación automática del ticket de comanda imprimible en formato local `.txt`.
6. **Administración del Catálogo (ABM):** Panel completo para agregar, editar y eliminar productos con validación estricta de stock y precios mínimos.
7. **Estadísticas y Reportes:** Gráficos estadísticos dinámicos (JFreeChart) con KPIs de ingresos, pedidos y recaudación por producto e histórico de ventas.

---

## 🔄 Flujo de Trabajo en la Aplicación

El flujo operativo habitual del personal del restaurante es el siguiente:

```
[Inicio de Sesión] 
       │
       ▼
[Tablero Principal: Vista de Mesas]
       │
 ┌─────┴──────────────────────────────────┐
 │                                        │
 ▼                                        ▼
[Mesa Libre]                          [Mesa Ocupada]
 │                                        │
 ├─► Reservar (Naranja)                   ├─► Ver detalles de consumos activos
 └─► Ocupar (Rojo)                        └─► Liberar Mesa (Cierra la cuenta)
                                                  │
                                                  ▼
                                            [Mensaje de Confirmación]
                                            (Pregunta si desea cerrar todos 
                                             los pedidos activos de la mesa)
                                                  │
                                       ┌──────────┴──────────┐
                                       ▼                     ▼
                                     [ Sí ]                [ No ]
                                       │                     │
                                       ▼                     ▼
                             (Mesa se limpia a      (Cancela la acción,
                              estado LIBRE/Verde)    mantiene pedidos)

=========================================================================

[Flujo de Creación de Pedido]
       │
       ▼
[Pestaña Menú] ────► Seleccionar categoría y agregar productos (se suma al subtotal)
       │
       ▼
[Confirmar Pedido] ────► Abre diálogo de Checkout
       │                     ├─► Seleccionar Mesa (Libre, Reservada u Ocupada)
       │                     ├─► Ingresar Descuento (ej: 10% o monto fijo)
       │                     ├─► Seleccionar Método de Pago e ingresar notas
       │
       ▼
[Confirmar Checkout]
       │
       ├─► Backend: Guarda pedido asíncronamente en la BD (estado ABIERTO)
       ├─► Inventario: Descuenta stock de ingredientes/productos al instante
       ├─► Interfaz: La mesa seleccionada se actualiza automáticamente a color ROJO
       └─► Ticket: Abre ventana para guardar el archivo comanda_mesa_X.txt
```
