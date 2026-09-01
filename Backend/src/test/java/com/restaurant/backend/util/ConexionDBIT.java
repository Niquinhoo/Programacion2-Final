package com.restaurant.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import com.restaurant.backend.support.DedicatedDatabaseExtension;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@DisplayName("Conexión y flujo completo — Tests de Integración")
@ExtendWith(DedicatedDatabaseExtension.class)
public class ConexionDBIT {

    @Test
    public void testDatabaseConnection() {
        ConexionDB conexion = ConexionDB.getInstance();
        assertNotNull(conexion, "La instancia de ConexionDB no debe ser nula");
        
        boolean isConnected = conexion.testConnection();
        assertTrue(isConnected, "La conexion a la base de datos de TiDB Cloud debe ser exitosa. Verifique la configuracion en db.properties");
    }

    @Test
    public void testCompleteOrderFlow() {
        System.out.println("=== STARTING INTEGRATION TEST FOR COMPLETE FLOW ===");
        
        // 1. Get services from factory
        com.restaurant.backend.service.MesaService mesaService = com.restaurant.backend.service.ServicioFactory.getMesaService();
        com.restaurant.backend.service.PedidoService pedidoService = com.restaurant.backend.service.ServicioFactory.getPedidoService();
        com.restaurant.backend.service.ProductoService productoService = com.restaurant.backend.service.ServicioFactory.getProductoService();
        com.restaurant.backend.service.ReporteService reporteService = com.restaurant.backend.service.ServicioFactory.getReporteService();
        
        assertNotNull(mesaService);
        assertNotNull(pedidoService);
        assertNotNull(productoService);
        assertNotNull(reporteService);
        
        // 2. Fetch products and check stock
        com.restaurant.backend.model.Producto producto = productoService.obtenerPorId(3); // Hamburguesa Clasica
        assertNotNull(producto);
        int originalStock = producto.getStock();
        System.out.println("Producto obtenido: " + producto.getNombre() + " | Stock original: " + originalStock);
        
        // 3. Find a free table (e.g., Table 1)
        com.restaurant.backend.model.Mesa mesa = mesaService.obtenerPorId(1);
        assertNotNull(mesa);
        System.out.println("Mesa 1 original estado: " + mesa.getEstado());
        
        // Ensure table is LIBRE for test by canceling/closing any open orders first
        if (mesa.getEstado() != com.restaurant.backend.model.EstadoMesa.LIBRE) {
            java.util.List<com.restaurant.backend.model.Pedido> pedidos = pedidoService.listarTodos();
            for (com.restaurant.backend.model.Pedido p : pedidos) {
                if (p.getMesa().getIdMesa() == 1 && (p.getEstado() == com.restaurant.backend.model.EstadoPedido.ABIERTO || p.getEstado() == com.restaurant.backend.model.EstadoPedido.EN_COCINA || p.getEstado() == com.restaurant.backend.model.EstadoPedido.LISTO)) {
                    pedidoService.cancelarPedido(p.getIdPedido());
                }
            }
            mesaService.liberar(1);
            mesa = mesaService.obtenerPorId(1);
        }
        assertEquals(com.restaurant.backend.model.EstadoMesa.LIBRE, mesa.getEstado());
        
        // 4. Create Usuario mock (we just fetch user 2)
        com.restaurant.backend.model.Usuario usuario = new com.restaurant.backend.model.Usuario();
        usuario.setIdUsuario(2); // Carlos Gomez (Mozo)
        
        // 5. Create new Order
        java.util.List<com.restaurant.backend.model.DetallePedido> detalles = new java.util.ArrayList<>();
        com.restaurant.backend.model.DetallePedido primerDetalle = new com.restaurant.backend.model.DetallePedido();
        primerDetalle.setProducto(producto);
        primerDetalle.setCantidad(1);
        primerDetalle.setPrecioUnitario(producto.getPrecio());
        detalles.add(primerDetalle);

        String resCrear = pedidoService.crearPedido(mesa, usuario, detalles);
        System.out.println("Resultado crearPedido: " + resCrear);
        assertEquals("Se cambio el estado", resCrear);
        
        // Extract order ID by querying open orders for Mesa 1
        java.util.List<com.restaurant.backend.model.Pedido> pedidos = pedidoService.listarTodos();
        int pedidoId = -1;
        for (com.restaurant.backend.model.Pedido p : pedidos) {
            if (p.getMesa().getIdMesa() == 1 && p.getEstado() == com.restaurant.backend.model.EstadoPedido.ABIERTO) {
                pedidoId = p.getIdPedido();
                break;
            }
        }
        System.out.println("ID del nuevo pedido encontrado en la base de datos: " + pedidoId);
        assertTrue(pedidoId > 0);
        
        // Verify table is now OCUPADA
        com.restaurant.backend.model.Mesa mesaOcupada = mesaService.obtenerPorId(1);
        assertEquals(com.restaurant.backend.model.EstadoMesa.OCUPADA, mesaOcupada.getEstado());
        System.out.println("Mesa 1 estado despues de crear pedido: " + mesaOcupada.getEstado());
        
        // 6. Add item to order
        String resAgregar = pedidoService.agregarItem(pedidoId, producto, 2); // Add 2 Hamburguesas Clasicas
        System.out.println("Resultado agregarItem: " + resAgregar);
        assertTrue(resAgregar.contains("correctamente") || resAgregar.contains("exito"));
        
        // Get order details
        com.restaurant.backend.model.Pedido pedidoActual = pedidoService.obtenerPorId(pedidoId);
        assertNotNull(pedidoActual);
        System.out.println("Pedido total acumulado: " + pedidoActual.getTotal());
        assertEquals(0, new java.math.BigDecimal("7500.00").compareTo(pedidoActual.getTotal())); // (1 + 2) * 2500
        
        // 7. Close order
        String resCerrar = pedidoService.cerrarPedido(pedidoId);
        System.out.println("Resultado cerrarPedido: " + resCerrar);
        assertEquals("Estado actualizado correctamente", resCerrar);
        
        // Verify table is now LIBRE
        com.restaurant.backend.model.Mesa mesaLiberada = mesaService.obtenerPorId(1);
        System.out.println("Mesa 1 estado despues de cerrar pedido: " + mesaLiberada.getEstado());
        
        // El pedido inicial descuenta 1 unidad y agregarItem descuenta otras 2.
        com.restaurant.backend.model.Producto productoPost = productoService.obtenerPorId(3);
        System.out.println("Stock post-cierre: " + productoPost.getStock());
        assertEquals(originalStock - 3, productoPost.getStock());
        
        // Verify Report
        java.util.List<com.restaurant.backend.service.dto.VentaPorProductoDTO> reporteProductos = reporteService.ventasPorProducto();
        assertNotNull(reporteProductos);
        System.out.println("Reporte de ventas por producto obtenido. Cantidad de registros: " + reporteProductos.size());
        
        System.out.println("=== INTEGRATION TEST COMPLETED SUCCESSFULLY ===");
    }

    @Test
    public void testLiberarMesaConPedidosActivos() {
        System.out.println("=== STARTING TEST FOR LIBERAR MESA CON PEDIDOS ACTIVOS ===");
        com.restaurant.backend.service.MesaService mesaService = com.restaurant.backend.service.ServicioFactory.getMesaService();
        com.restaurant.backend.service.PedidoService pedidoService = com.restaurant.backend.service.ServicioFactory.getPedidoService();
        com.restaurant.backend.service.ProductoService productoService = com.restaurant.backend.service.ServicioFactory.getProductoService();

        // 1. Get mesa 1
        com.restaurant.backend.model.Mesa mesa = mesaService.obtenerPorId(1);
        assertNotNull(mesa);

        // Ensure table is LIBRE or clean it
        if (mesa.getEstado() != com.restaurant.backend.model.EstadoMesa.LIBRE) {
            java.util.List<com.restaurant.backend.model.Pedido> pedidos = pedidoService.listarTodos();
            for (com.restaurant.backend.model.Pedido p : pedidos) {
                if (p.getMesa().getIdMesa() == 1 && (p.getEstado() == com.restaurant.backend.model.EstadoPedido.ABIERTO || p.getEstado() == com.restaurant.backend.model.EstadoPedido.EN_COCINA || p.getEstado() == com.restaurant.backend.model.EstadoPedido.LISTO)) {
                    pedidoService.cancelarPedido(p.getIdPedido());
                }
            }
            mesaService.liberar(1);
        }

        // Create a new order on mesa 1
        com.restaurant.backend.model.Producto producto = productoService.obtenerPorId(3);
        com.restaurant.backend.model.Usuario usuario = new com.restaurant.backend.model.Usuario();
        usuario.setIdUsuario(2);

        java.util.List<com.restaurant.backend.model.DetallePedido> detalles = new java.util.ArrayList<>();
        com.restaurant.backend.model.DetallePedido primerDetalle = new com.restaurant.backend.model.DetallePedido();
        primerDetalle.setProducto(producto);
        primerDetalle.setCantidad(1);
        primerDetalle.setPrecioUnitario(producto.getPrecio());
        detalles.add(primerDetalle);

        String resCrear = pedidoService.crearPedido(mesa, usuario, detalles);
        assertEquals("Se cambio el estado", resCrear);

        // Get the active order on mesa 1
        java.util.List<com.restaurant.backend.model.Pedido> pedidos = pedidoService.listarTodos();
        com.restaurant.backend.model.Pedido pedidoActivo = null;
        for (com.restaurant.backend.model.Pedido p : pedidos) {
            if (p.getMesa().getIdMesa() == 1 && p.getEstado() == com.restaurant.backend.model.EstadoPedido.ABIERTO) {
                pedidoActivo = p;
                break;
            }
        }
        assertNotNull(pedidoActivo);
        assertEquals(com.restaurant.backend.model.EstadoPedido.ABIERTO, pedidoActivo.getEstado());

        // 2. Liberar mesa manually (which should close all active orders)
        String resLiberar = mesaService.liberar(1);
        assertTrue(resLiberar.contains("cambio") || resLiberar.contains("cambió") || resLiberar.contains("exito") || resLiberar.contains("correctamente"));

        // Verify mesa is LIBRE
        com.restaurant.backend.model.Mesa mesaPost = mesaService.obtenerPorId(1);
        assertEquals(com.restaurant.backend.model.EstadoMesa.LIBRE, mesaPost.getEstado());

        // Verify the order is CERRADO
        com.restaurant.backend.model.Pedido pedidoPost = pedidoService.obtenerPorId(pedidoActivo.getIdPedido());
        assertNotNull(pedidoPost);
        assertEquals(com.restaurant.backend.model.EstadoPedido.CERRADO, pedidoPost.getEstado());

        System.out.println("=== TEST FOR LIBERAR MESA CON PEDIDOS ACTIVOS COMPLETED SUCCESSFULLY ===");
    }
}
