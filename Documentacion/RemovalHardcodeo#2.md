# RemovalHardcodeo#2 — Conexión de Pedidos, Descuentos Dinámicos e Integración de Mesas

## Objetivo

Eliminar el comportamiento estático y hardcodeado del diálogo de confirmación de pedido (`CheckoutDialog`) y el flujo de guardado de pedidos, permitiendo la asignación real a mesas (incluyendo mesas ya ocupadas), cálculo interactivo de descuentos, guardado en base de datos, desocupación manual de mesas y visualización de pedidos activos en la vista de mesas.

---

## Cambios realizados

### 1. Conexión y Reglas de Pedidos con el Backend (`PedidoService.java` y `PedidoDAOImpl.java`)

- **Carga de observaciones y método de pago:** Se habilitó en el backend la persistencia y recuperación del campo `observacion` en la tabla `pedidos`.
- **Pedidos en mesas ocupadas (`OCUPADA`):** Se modificó la regla en el backend para permitir la creación de nuevos pedidos en mesas que ya tienen el estado `OCUPADA`, facilitando que una mesa tenga varios pedidos abiertos simultáneos (agregado a `ESTADOS_MESA_VALIDOS`).
- **Desocupación de mesas estrictamente manual:** Se removió la liberación automática de mesas en `cerrarPedido()` y `cancelarPedido()`. Las mesas permanecen `OCUPADA` tras el cobro o cancelación y deben ser liberadas manualmente por el mozo desde el panel de mesas.
- **Descuento de stock en tiempo real:** Se trasladó la lógica de descuento de stock de `cerrarPedido()` a `crearPedido()`. Ahora, tan pronto como un pedido se guarda en el sistema, el stock de cada producto involucrado se reduce automáticamente, reflejándose al instante en la base de datos y en la pestaña de productos.
- **Devolución de stock por cancelación:** En caso de que se cancele un pedido, el sistema ejecuta una rutina que le reintegra las cantidades pedidas al stock del inventario para mantener la consistencia física y lógica.
- **Actualización dinámica de "Cantidad Vendidos":** Se modificó la vista de la base de datos `vw_ventas_por_producto` para filtrar por `pe.estado != 'CANCELADO'` (en lugar de `pe.estado = 'CERRADO'`). Esto permite que, en cuanto se crea un pedido (estado `ABIERTO`), la cantidad de vendidos del producto aumente automáticamente a la par del descuento de stock. De igual manera, si el pedido se cancela, la cantidad vendida vuelve a reducirse de forma automática al descartarse del filtro de la vista. Esta actualización se inyecta de forma automática en `ConexionDB.java` al arrancar el pool de conexiones.

### 2. Diálogo Modal Dinámico (`CheckoutDialog.java`)

- **Carga dinámica de productos:** El método `cargarDetalles(List<String[]> items)` renderiza en el panel central (`jPanel3`) la lista real de ítems que el cliente está pidiendo.
- **Cálculo interactivo de descuento:** Se implementó `setSubtotalYCalcular(double subtotal)` y un `DocumentListener` en el campo `DescuentoVar` que detecta y computa el descuento en tiempo real:
  - Si termina en `%` o es menor o igual a `100`, se aplica como descuento porcentual (ej. `10` o `15%`).
  - Si es mayor a `100`, se asume un descuento de monto fijo.
  - El resultado actualiza el label `TotalNum` de inmediato.

```java
private void recalcularTotalConDescuento() {
    String descText = DescuentoVar.getText().trim();
    double descuento = 0.0;
    if (!descText.isEmpty()) {
        try {
            if (descText.endsWith("%")) {
                descText = descText.substring(0, descText.length() - 1).trim();
            }
            descuento = Double.parseDouble(descText);
        } catch (NumberFormatException e) {
            descuento = 0.0;
        }
    }
    double total = subtotalOriginal;
    if (descuento > 0) {
        if (descuento <= 100) {
            total = subtotalOriginal * (1 - (descuento / 100.0));
        } else {
            total = Math.max(0.0, subtotalOriginal - descuento);
        }
    }
    TotalNum.setText(String.format(java.util.Locale.US, "$%.2f", total));
}
```

### 3. Asignación de Pedido y Corrección de Bug de Totales (`Menu.java`)

- **Corrección de Bug de Totales:** Se corrigió `actualizarTotal()` para que calcule `precio * cantidad` en vez de simplemente sumar los precios unitarios de las filas de productos.
- **Asignación sin restricciones:** El menú permite asignar el pedido a cualquier mesa en estado `LIBRE`, `RESERVADA` u `OCUPADA`.
- **Impresión de la Comanda (.txt):** Una vez creado con éxito en el backend, el menú invoca a `guardarComanda(...)` que abre un diálogo de Swing (`JFileChooser`) para guardar la comanda estructurada como ticket en un archivo `.txt` local.

### 4. Estado de Mesas en Vivo y Pedidos Activos (`MesasPanel.java` y `DetallesMesasPanel.java`)

- **Colores en tiempo real (`MesasPanel.java`):** El panel de mesas ejecuta `actualizarMesas()` al iniciar y tras cualquier cambio para pintar dinámicamente el color de fondo de las mesas:
  - 🟢 **Verde:** `LIBRE`
  - 🔴 **Rojo:** `OCUPADA`
  - 🟠 **Naranja:** `RESERVADA`
  - ⚫ **Gris:** `FUERA_DE_SERVICIO`
- **Pedidos Activos por Mesa (`DetallesMesasPanel.java`):** La tabla de detalles de la mesa ahora lista todos los pedidos activos de la mesa (`ABIERTO`, `EN_COCINA`, `LISTO`), mostrando ID de Pedido, Mozo asignado, Fecha/Hora, Estado y Total del pedido actual.
- **Acciones y Liberación manual:** Se enlazaron los botones `btnOcupar`, `btnLiberar` y `btnCancelarReserva` a llamadas del backend de la clase `MesaService`, permitiendo cambiar manualmente el estado de las mesas en vivo.

### 5. Renderizado Dinámico de Pedidos (`PedidosPanel.java`)

- **Historial y Detalle en Vivo:** Se implementó `listarPedidos()` para rellenar la tabla principal del panel de pedidos a partir de los datos reales del backend (`ServicioFactory.getPedidoService().listarTodos()`).
- Por cada pedido se recuperan sus detalles individuales (`DetallePedido`) y se muestran en la tabla: Mesa, Nombre del producto, Cantidad, Precio Unitario, Estado y Hora de creación.
- **Navegación Interactiva:** Se añadió una rutina en `Menu.java` que refresca dinámicamente la tabla de pedidos cada vez que el usuario hace clic en la pestaña "Pedidos" en la barra lateral.

---

## Verificación

Se verificó el correcto funcionamiento de las siguientes transiciones de flujo:
1. Agregar productos en el menú (el total calcula `precio * cant`).
2. Presionar "Confirmar Pedido", seleccionar mesa (inclusive ocupadas) y digitar un descuento.
3. Al dar clic en "Confirmar", el pedido se crea en la base de datos, el stock del producto se descuenta al instante, y se abre el cuadro de diálogo para guardar el archivo `.txt` de la comanda.
4. El botón de la mesa en la vista de mesas pasa automáticamente a rojo (`OCUPADA`).
5. Al hacer clic sobre el botón de la mesa, la tabla de detalles lista la información real del pedido.
6. Al dar clic en el botón verde de "Liberar", la mesa cambia su estado de vuelta a `LIBRE` (Verde) y se limpia la lista de pedidos activos.
7. Al navegar a la pestaña "Pedidos", se muestra el listado con los productos e información en tiempo real.
8. Al cancelar un pedido en el backend, la cantidad de stock se reintegra al inventario de forma automática.
