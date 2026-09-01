package com.restaurant.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurant.backend.dao.CategoriaDAO;
import com.restaurant.backend.model.Categoria;
import com.restaurant.backend.model.DetallePedido;
import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.EstadoPedido;
import com.restaurant.backend.model.Mesa;
import com.restaurant.backend.model.Pedido;
import com.restaurant.backend.model.Producto;
import com.restaurant.backend.model.Usuario;
import com.restaurant.backend.service.MesaService;
import com.restaurant.backend.service.PedidoService;
import com.restaurant.backend.service.ProductoService;
import com.restaurant.backend.service.ReporteService;
import com.restaurant.backend.service.dto.ResumenGeneralDTO;
import com.restaurant.backend.service.dto.VentaPorMesDTO;
import com.restaurant.backend.service.dto.VentaPorProductoDTO;

@DisplayName("Controladores — delegación al backend")
class ControllersTest {

    @Test
    void productoControllerDelegaTodasLasOperaciones() {
        ProductoService service = mock(ProductoService.class);
        ProductoController controller = new ProductoController(service);
        Producto producto = new Producto();
        List<Producto> productos = List.of(producto);
        when(service.obtenerTodos()).thenReturn(productos);
        when(service.obtenerPorCategoria("Bebidas")).thenReturn(productos);
        when(service.obtenerPorId(1)).thenReturn(producto);
        when(service.crear(producto)).thenReturn("creado");
        when(service.actualizar(producto)).thenReturn("editado");
        when(service.eliminar(1)).thenReturn("eliminado");

        assertSame(productos, controller.listar());
        assertSame(productos, controller.listarPorCategoria("Bebidas"));
        assertSame(producto, controller.obtenerPorId(1));
        assertEquals("creado", controller.crear(producto));
        assertEquals("editado", controller.editar(producto));
        assertEquals("eliminado", controller.eliminar(1));
    }

    @Test
    void mesaControllerDelegaTodasLasOperaciones() {
        MesaService service = mock(MesaService.class);
        MesaController controller = new MesaController(service);
        Mesa mesa = new Mesa();
        List<Mesa> mesas = List.of(mesa);
        when(service.listar()).thenReturn(mesas);
        when(service.obtenerPorNumero(1)).thenReturn(mesa);
        when(service.obtenerPorId(2)).thenReturn(mesa);
        when(service.ocupar(2)).thenReturn("ocupada");
        when(service.liberar(2)).thenReturn("libre");
        when(service.reservar(2)).thenReturn("reservada");
        when(service.cancelarReserva(2)).thenReturn("cancelada");
        when(service.cambiarEstado(2, EstadoMesa.FUERA_DE_SERVICIO)).thenReturn("cambiada");

        assertSame(mesas, controller.listarMesas());
        assertSame(mesa, controller.obtenerPorNumero(1));
        assertSame(mesa, controller.obtenerPorId(2));
        assertEquals("ocupada", controller.ocupar(2));
        assertEquals("libre", controller.liberar(2));
        assertEquals("reservada", controller.reservar(2));
        assertEquals("cancelada", controller.cancelarReserva(2));
        assertEquals("cambiada", controller.cambiarEstado(2, EstadoMesa.FUERA_DE_SERVICIO));
    }

    @Test
    void pedidoControllerDelegaTodasLasOperaciones() {
        PedidoService service = mock(PedidoService.class);
        PedidoController controller = new PedidoController(service);
        Mesa mesa = new Mesa();
        Usuario usuario = new Usuario();
        List<DetallePedido> detalles = List.of(new DetallePedido());
        Pedido pedido = new Pedido();
        List<Pedido> pedidos = List.of(pedido);
        when(service.crearPedido(mesa, usuario, detalles)).thenReturn("creado");
        when(service.cerrarPedido(1)).thenReturn("cerrado");
        when(service.cancelarPedido(1)).thenReturn("cancelado");
        when(service.listarTodos()).thenReturn(pedidos);
        when(service.listarPorEstado(EstadoPedido.ABIERTO)).thenReturn(pedidos);
        when(service.obtenerDetalles(1)).thenReturn(detalles);
        when(service.obtenerPorId(1)).thenReturn(pedido);

        assertEquals("creado", controller.crear(mesa, usuario, detalles));
        assertEquals("cerrado", controller.cerrar(1));
        assertEquals("cancelado", controller.cancelar(1));
        assertSame(pedidos, controller.listar());
        assertSame(pedidos, controller.listarPorEstado(EstadoPedido.ABIERTO));
        assertSame(detalles, controller.obtenerDetalles(1));
        assertSame(pedido, controller.obtenerPorId(1));
    }

    @Test
    void reporteYCategoriaControllersDelegan() {
        ReporteService reporteService = mock(ReporteService.class);
        ReporteController reporteController = new ReporteController(reporteService);
        List<VentaPorProductoDTO> productos = List.of(new VentaPorProductoDTO());
        List<VentaPorMesDTO> meses = List.of(new VentaPorMesDTO());
        ResumenGeneralDTO resumen = new ResumenGeneralDTO();
        when(reporteService.ventasPorProducto()).thenReturn(productos);
        when(reporteService.ventasPorMes()).thenReturn(meses);
        when(reporteService.resumenGeneral()).thenReturn(resumen);

        assertSame(productos, reporteController.ventasPorProducto());
        assertSame(meses, reporteController.ventasPorMes());
        assertSame(resumen, reporteController.resumenGeneral());

        CategoriaDAO categoriaDAO = mock(CategoriaDAO.class);
        List<Categoria> categorias = List.of(new Categoria());
        when(categoriaDAO.getCategorias()).thenReturn(categorias);
        assertSame(categorias, new CategoriaController(categoriaDAO).listar());
    }
}
