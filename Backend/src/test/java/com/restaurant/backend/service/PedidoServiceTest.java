package com.restaurant.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurant.backend.dao.PedidoDAO;
import com.restaurant.backend.model.Categoria;
import com.restaurant.backend.model.DetallePedido;
import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.EstadoPedido;
import com.restaurant.backend.model.Mesa;
import com.restaurant.backend.model.Pedido;
import com.restaurant.backend.model.Producto;
import com.restaurant.backend.model.Usuario;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService — pruebas unitarias")
class PedidoServiceTest {

    @Mock private PedidoDAO pedidoDAO;
    @Mock private MesaService mesaService;
    @Mock private ProductoService productoService;
    private PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService(pedidoDAO, mesaService, productoService);
    }

    @Test
    void crearPedidoValidaDatosObligatorios() {
        Mesa mesa = mesa(1, EstadoMesa.LIBRE);
        Usuario usuario = usuario(1);

        assertEquals("La mesa es obligatoria", service.crearPedido(null, usuario, List.of()));
        assertEquals("El usuario es obligatorio", service.crearPedido(mesa, null, List.of()));
        assertEquals("El pedido debe tener al menos un item", service.crearPedido(mesa, usuario, List.of()));
        verifyNoInteractions(pedidoDAO, mesaService, productoService);
    }

    @Test
    void crearPedidoRechazaMesaFueraDeServicio() {
        Mesa mesa = mesa(1, EstadoMesa.FUERA_DE_SERVICIO);
        when(mesaService.obtenerPorId(1)).thenReturn(mesa);

        assertEquals("No se puede abrir un pedido en una mesa FUERA_DE_SERVICIO",
                service.crearPedido(mesa, usuario(1), List.of(detalle(producto(1, 5), 1))));
        verifyNoInteractions(pedidoDAO, productoService);
    }

    @Test
    void crearPedidoRechazaDetalleSinProducto() {
        DetallePedido detalle = new DetallePedido();
        when(mesaService.obtenerPorId(1)).thenReturn(mesa(1, EstadoMesa.LIBRE));

        assertEquals("Cada detalle debe tener un producto valido",
                service.crearPedido(mesa(1, EstadoMesa.LIBRE), usuario(1), List.of(detalle)));
        verifyNoInteractions(pedidoDAO, productoService);
    }

    @Test
    void crearPedidoPropagaErrorDeStockSinPersistir() {
        Producto producto = producto(1, 1);
        when(mesaService.obtenerPorId(1)).thenReturn(mesa(1, EstadoMesa.LIBRE));
        when(productoService.validarStockDisponible(1, 2)).thenReturn("Stock insuficiente");

        assertEquals("Stock insuficiente",
                service.crearPedido(mesa(1, EstadoMesa.LIBRE), usuario(1), List.of(detalle(producto, 2))));
        verify(pedidoDAO, never()).Insertar(any(), anyList());
    }

    @Test
    void crearPedidoCalculaTotalDescuentaStockYOcupaMesa() {
        Mesa mesa = mesa(1, EstadoMesa.LIBRE);
        Producto productoCompleto = producto(2, 10);
        productoCompleto.setPrecio(new BigDecimal("250.00"));
        DetallePedido detalle = detalle(producto(2, 0), 3);
        when(mesaService.obtenerPorId(1)).thenReturn(mesa);
        when(productoService.validarStockDisponible(2, 3)).thenReturn(null);
        when(productoService.obtenerPorId(2)).thenReturn(productoCompleto);
        when(pedidoDAO.Insertar(any(Pedido.class), eq(List.of(detalle))))
                .thenReturn("Pedido y detalles insertados correctamente");
        when(productoService.descontarStock(2, 3)).thenReturn("Producto editado correctamente");
        when(mesaService.ocupar(1)).thenReturn("Se cambio el estado");

        String resultado = service.crearPedido(mesa, usuario(4), List.of(detalle), "sin sal");

        assertEquals("Se cambio el estado", resultado);
        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoDAO).Insertar(pedidoCaptor.capture(), eq(List.of(detalle)));
        Pedido guardado = pedidoCaptor.getValue();
        assertEquals(0, new BigDecimal("750.00").compareTo(guardado.getTotal()));
        assertEquals("sin sal", guardado.getObservacion());
        assertEquals(EstadoPedido.ABIERTO, guardado.getEstado());
        verify(productoService).descontarStock(2, 3);
        verify(mesaService).ocupar(1);
    }

    @Test
    void crearPedidoEnMesaOcupadaNoIntentaOcuparlaOtraVez() {
        Mesa mesa = mesa(1, EstadoMesa.OCUPADA);
        Producto producto = producto(2, 10);
        when(mesaService.obtenerPorId(1)).thenReturn(mesa);
        when(productoService.validarStockDisponible(2, 1)).thenReturn(null);
        when(productoService.obtenerPorId(2)).thenReturn(producto);
        when(pedidoDAO.Insertar(any(), anyList())).thenReturn("Pedido insertado correctamente");
        when(productoService.descontarStock(2, 1)).thenReturn("Producto editado correctamente");

        assertEquals("Pedido y detalles insertados correctamente",
                service.crearPedido(mesa, usuario(1), List.of(detalle(producto, 1))));
        verify(mesaService, never()).ocupar(anyInt());
    }

    @Test
    void agregarItemPersisteActualizaTotalYDescuentaStock() {
        Pedido pedido = pedido(10, EstadoPedido.ABIERTO);
        Producto producto = producto(2, 8);
        producto.setPrecio(new BigDecimal("100.00"));
        DetallePedido existente = detalle(producto, 1);
        when(pedidoDAO.getPedidoPorId(10)).thenReturn(pedido);
        when(productoService.obtenerPorId(2)).thenReturn(producto);
        when(productoService.validarStockDisponible(2, 2)).thenReturn(null);
        when(pedidoDAO.insertarDetalle(eq(10), any())).thenReturn("Detalle insertado correctamente");
        when(pedidoDAO.getDetallesPedido(10)).thenReturn(List.of(existente, detalle(producto, 2)));
        when(pedidoDAO.actualizarTotal(10, new BigDecimal("300.00"))).thenReturn("Total actualizado correctamente");
        when(productoService.descontarStock(2, 2)).thenReturn("Producto editado correctamente");

        assertEquals("Item agregado correctamente", service.agregarItem(10, producto, 2));
        verify(productoService).descontarStock(2, 2);
        verify(pedidoDAO).actualizarTotal(10, new BigDecimal("300.00"));
    }

    @Test
    void agregarItemNoActualizaNiDescuentaSiFallaInsercion() {
        Pedido pedido = pedido(10, EstadoPedido.ABIERTO);
        Producto producto = producto(2, 8);
        when(pedidoDAO.getPedidoPorId(10)).thenReturn(pedido);
        when(productoService.obtenerPorId(2)).thenReturn(producto);
        when(productoService.validarStockDisponible(2, 1)).thenReturn(null);
        when(pedidoDAO.insertarDetalle(eq(10), any())).thenReturn("Error al insertar detalle");

        assertEquals("Error al insertar detalle", service.agregarItem(10, producto, 1));
        verify(pedidoDAO, never()).actualizarTotal(anyInt(), any());
        verify(productoService, never()).descontarStock(anyInt(), anyInt());
    }

    @Test
    void agregarItemSoloAceptaPedidosAbiertos() {
        when(pedidoDAO.getPedidoPorId(10)).thenReturn(pedido(10, EstadoPedido.CERRADO));

        assertEquals("Solo se pueden agregar items a pedidos abiertos",
                service.agregarItem(10, producto(2, 8), 1));
        verifyNoInteractions(productoService);
    }

    @Test
    void cerrarPedidoCambiaEstadoSinLiberarMesa() {
        when(pedidoDAO.getPedidoPorId(10)).thenReturn(pedido(10, EstadoPedido.ABIERTO));
        when(pedidoDAO.ModificarEstado(10, EstadoPedido.CERRADO)).thenReturn("Estado actualizado correctamente");

        assertEquals("Estado actualizado correctamente", service.cerrarPedido(10));
        verifyNoInteractions(mesaService);
    }

    @Test
    void cancelarPedidoRestauraStockSinLiberarMesa() {
        Producto producto = producto(2, 5);
        DetallePedido detalle = detalle(producto, 3);
        when(pedidoDAO.getPedidoPorId(10)).thenReturn(pedido(10, EstadoPedido.ABIERTO));
        when(pedidoDAO.ModificarEstado(10, EstadoPedido.CANCELADO)).thenReturn("Estado actualizado correctamente");
        when(pedidoDAO.getDetallesPedido(10)).thenReturn(List.of(detalle));
        when(productoService.obtenerPorId(2)).thenReturn(producto);
        when(productoService.actualizar(producto)).thenReturn("Producto editado correctamente");

        assertEquals("Estado actualizado correctamente", service.cancelarPedido(10));
        assertEquals(8, producto.getStock());
        verify(productoService).actualizar(producto);
        verifyNoInteractions(mesaService);
    }

    @Test
    void consultasInvalidasNoLleganAlDao() {
        assertEquals(BigDecimal.ZERO, service.calcularTotal(null));
        assertTrue(service.obtenerDetalles(0).isEmpty());
        assertNull(service.obtenerPorId(0));
        assertEquals("El id del pedido debe ser mayor a cero", service.cerrarPedido(0));
        assertEquals("El id del pedido debe ser mayor a cero", service.cancelarPedido(0));
        verifyNoInteractions(pedidoDAO, mesaService, productoService);
    }

    private Mesa mesa(int id, EstadoMesa estado) {
        return new Mesa(id, id, 4, estado);
    }

    private Usuario usuario(int id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        return usuario;
    }

    private Producto producto(int id, int stock) {
        return new Producto(id, "Producto " + id, null, new BigDecimal("100.00"), stock,
                new Categoria(1, "Categoria", null, true), true);
    }

    private DetallePedido detalle(Producto producto, int cantidad) {
        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setPrecioUnitario(producto.getPrecio());
        detalle.setCantidad(cantidad);
        return detalle;
    }

    private Pedido pedido(int id, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(id);
        pedido.setEstado(estado);
        return pedido;
    }
}
