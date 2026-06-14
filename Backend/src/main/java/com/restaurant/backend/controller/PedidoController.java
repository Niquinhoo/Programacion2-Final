package com.restaurant.backend.controller;

import java.util.List;

import com.restaurant.backend.model.DetallePedido;
import com.restaurant.backend.model.EstadoPedido;
import com.restaurant.backend.model.Mesa;
import com.restaurant.backend.model.Pedido;
import com.restaurant.backend.model.Usuario;
import com.restaurant.backend.service.PedidoService;
import com.restaurant.backend.service.ServicioFactory;

public class PedidoController {

    private final PedidoService pedidoService = ServicioFactory.getPedidoService();

    public String crear(Mesa mesa, Usuario usuario, List<DetallePedido> detalles) {
        return pedidoService.crearPedido(mesa, usuario, detalles);
    }

    public String cerrar(int pedidoId) {
        return pedidoService.cerrarPedido(pedidoId);
    }

    public String cancelar(int pedidoId) {
        return pedidoService.cancelarPedido(pedidoId);
    }

    public List<Pedido> listar() {
        return pedidoService.listarTodos();
    }

    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return pedidoService.listarPorEstado(estado);
    }

    public List<DetallePedido> obtenerDetalles(int pedidoId) {
        return pedidoService.obtenerDetalles(pedidoId);
    }

    public Pedido obtenerPorId(int pedidoId) {
        return pedidoService.obtenerPorId(pedidoId);
    }
}
