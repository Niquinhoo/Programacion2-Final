package com.restaurant.backend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Modelos — cálculos y valores iniciales")
class ModelosTest {

    @Test
    void detalleRecalculaSubtotalAlCambiarCantidadOPrecio() {
        DetallePedido detalle = new DetallePedido();

        detalle.setPrecioUnitario(new BigDecimal("125.50"));
        detalle.setCantidad(3);

        assertEquals(0, new BigDecimal("376.50").compareTo(detalle.getSubtotal()));
    }

    @Test
    void detalleConPrecioNuloTieneSubtotalCero() {
        DetallePedido detalle = new DetallePedido();

        detalle.setPrecioUnitario(null);

        assertEquals(BigDecimal.ZERO, detalle.getSubtotal());
    }

    @Test
    void pedidoAgregaYQuitaDetallesRecalculandoTotal() {
        Pedido pedido = new Pedido();
        DetallePedido primero = detalle("100.00", 2);
        DetallePedido segundo = detalle("50.00", 1);

        pedido.agregarDetalle(primero);
        pedido.agregarDetalle(segundo);
        assertEquals(0, new BigDecimal("250.00").compareTo(pedido.getTotal()));

        pedido.quitarDetalle(primero);
        assertEquals(0, new BigDecimal("50.00").compareTo(pedido.getTotal()));
    }

    @Test
    void pedidoIgnoraSubtotalesNulosAlRecalcular() {
        Pedido pedido = new Pedido();
        DetallePedido detalle = new DetallePedido();
        detalle.setSubtotal(null);

        pedido.agregarDetalle(detalle);

        assertEquals(BigDecimal.ZERO, pedido.getTotal());
    }

    @Test
    void constructoresDefinenEstadosInicialesSeguros() {
        assertEquals(EstadoPedido.ABIERTO, new Pedido().getEstado());
        assertEquals(BigDecimal.ZERO, new Pedido().getTotal());
        assertEquals(EstadoMesa.LIBRE, new Mesa().getEstado());
        assertEquals(4, new Mesa().getCapacidad());
        assertTrue(new Producto().isDisponible());
        assertTrue(new Categoria().isActiva());
        assertTrue(new Usuario().isActivo());
    }

    private DetallePedido detalle(String precio, int cantidad) {
        DetallePedido detalle = new DetallePedido();
        detalle.setPrecioUnitario(new BigDecimal(precio));
        detalle.setCantidad(cantidad);
        return detalle;
    }
}
