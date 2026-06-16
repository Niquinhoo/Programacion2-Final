package com.restaurant.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.restaurant.backend.dao.PedidoDAO;
import com.restaurant.backend.dao.PedidoDAOImpl;
import com.restaurant.backend.model.DetallePedido;
import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.EstadoPedido;
import com.restaurant.backend.model.Mesa;
import com.restaurant.backend.model.Pedido;
import com.restaurant.backend.model.Producto;
import com.restaurant.backend.model.Usuario;

public class PedidoService {

    private static final Set<EstadoMesa> ESTADOS_MESA_VALIDOS = EnumSet.of(EstadoMesa.LIBRE, EstadoMesa.RESERVADA, EstadoMesa.OCUPADA);

    private final PedidoDAO pedidoDAO;
    private final MesaService mesaService;
    private final ProductoService productoService;

    public PedidoService() {
        this(new PedidoDAOImpl(), new MesaService(), new ProductoService());
    }

    public PedidoService(PedidoDAO pedidoDAO, MesaService mesaService, ProductoService productoService) {
        this.pedidoDAO = pedidoDAO;
        this.mesaService = mesaService;
        this.productoService = productoService;
    }

    public String crearPedido(Mesa mesa, Usuario usuario, List<DetallePedido> detalles) {
        return crearPedido(mesa, usuario, detalles, null);
    }

    public String crearPedido(Mesa mesa, Usuario usuario, List<DetallePedido> detalles, String observacion) {
        if (mesa == null || mesa.getIdMesa() == null) {
            return "La mesa es obligatoria";
        }
        if (usuario == null || usuario.getIdUsuario() == null) {
            return "El usuario es obligatorio";
        }
        if (detalles == null || detalles.isEmpty()) {
            return "El pedido debe tener al menos un item";
        }

        Mesa mesaActual = mesaService.obtenerPorId(mesa.getIdMesa());
        if (mesaActual == null) {
            return "No se encontro la mesa";
        }
        if (!ESTADOS_MESA_VALIDOS.contains(mesaActual.getEstado())) {
            return "No se puede abrir un pedido en una mesa " + mesaActual.getEstado();
        }

        for (DetallePedido detalle : detalles) {
            if (detalle.getProducto() == null || detalle.getProducto().getIdProducto() == null) {
                return "Cada detalle debe tener un producto valido";
            }
            if (detalle.getCantidad() <= 0) {
                return "La cantidad de cada item debe ser mayor a cero";
            }

            String validacionStock = productoService.validarStockDisponible(
                    detalle.getProducto().getIdProducto(),
                    detalle.getCantidad());
            if (validacionStock != null) {
                return validacionStock;
            }

            Producto producto = productoService.obtenerPorId(detalle.getProducto().getIdProducto());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.recalcularSubtotal();
        }

        Pedido pedido = new Pedido();
        pedido.setMesa(mesaActual);
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.ABIERTO);
        pedido.setObservacion(observacion);
        pedido.setCreatedAt(LocalDateTime.now());

        for (DetallePedido detalle : detalles) {
            pedido.agregarDetalle(detalle);
        }
        calcularTotal(pedido);

        String resultadoInsercion = pedidoDAO.Insertar(pedido, detalles);
        if (!resultadoInsercion.toLowerCase().contains("correctamente")) {
            return resultadoInsercion;
        }

        // Descontar stock inmediatamente al crear el pedido
        for (DetallePedido detalle : detalles) {
            productoService.descontarStock(detalle.getProducto().getIdProducto(), detalle.getCantidad());
        }

        if (mesaActual.getEstado() == EstadoMesa.OCUPADA) {
            return "Pedido y detalles insertados correctamente";
        }

        return mesaService.ocupar(mesaActual.getIdMesa());
    }

    public String agregarItem(int pedidoId, Producto producto, int cantidad) {
        if (pedidoId <= 0) {
            return "El id del pedido debe ser mayor a cero";
        }
        if (producto == null || producto.getIdProducto() == null) {
            return "El producto es obligatorio";
        }
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor a cero";
        }

        Pedido pedido = pedidoDAO.getPedidoPorId(pedidoId);
        if (pedido.getIdPedido() == null) {
            return "No se encontro el pedido";
        }
        if (pedido.getEstado() != EstadoPedido.ABIERTO) {
            return "Solo se pueden agregar items a pedidos abiertos";
        }

        Producto productoCompleto = productoService.obtenerPorId(producto.getIdProducto());
        if (productoCompleto == null) {
            return "No se encontro el producto";
        }

        String validacionStock = productoService.validarStockDisponible(producto.getIdProducto(), cantidad);
        if (validacionStock != null) {
            return validacionStock;
        }

        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(productoCompleto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(productoCompleto.getPrecio());

        String resultado = pedidoDAO.insertarDetalle(pedidoId, detalle);
        if (!resultado.toLowerCase().contains("correctamente")) {
            return resultado;
        }

        List<DetallePedido> detalles = pedidoDAO.getDetallesPedido(pedidoId);
        BigDecimal nuevoTotal = detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedidoDAO.actualizarTotal(pedidoId, nuevoTotal);

        return "Item agregado correctamente";
    }

    public BigDecimal calcularTotal(Pedido pedido) {
        if (pedido == null) {
            return BigDecimal.ZERO;
        }
        pedido.recalcularTotal();
        return pedido.getTotal();
    }

    public String cerrarPedido(int pedidoId) {
        if (pedidoId <= 0) {
            return "El id del pedido debe ser mayor a cero";
        }

        Pedido pedido = pedidoDAO.getPedidoPorId(pedidoId);
        if (pedido.getIdPedido() == null) {
            return "No se encontro el pedido";
        }
        if (pedido.getEstado() == EstadoPedido.CERRADO) {
            return "El pedido ya esta cerrado";
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            return "No se puede cerrar un pedido cancelado";
        }



        String resultadoEstado = pedidoDAO.ModificarEstado(pedidoId, EstadoPedido.CERRADO);
        if (!resultadoEstado.toLowerCase().contains("correctamente")) {
            return resultadoEstado;
        }

        return resultadoEstado;
    }

    public String cancelarPedido(int pedidoId) {
        if (pedidoId <= 0) {
            return "El id del pedido debe ser mayor a cero";
        }

        Pedido pedido = pedidoDAO.getPedidoPorId(pedidoId);
        if (pedido.getIdPedido() == null) {
            return "No se encontro el pedido";
        }
        if (pedido.getEstado() == EstadoPedido.CERRADO) {
            return "No se puede cancelar un pedido cerrado";
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            return "El pedido ya esta cancelado";
        }

        String resultadoEstado = pedidoDAO.ModificarEstado(pedidoId, EstadoPedido.CANCELADO);
        if (!resultadoEstado.toLowerCase().contains("correctamente")) {
            return resultadoEstado;
        }

        // Devolver stock al cancelar el pedido
        List<DetallePedido> detalles = pedidoDAO.getDetallesPedido(pedidoId);
        for (DetallePedido detalle : detalles) {
            Producto prod = productoService.obtenerPorId(detalle.getProducto().getIdProducto());
            if (prod != null) {
                prod.setStock(prod.getStock() + detalle.getCantidad());
                productoService.actualizar(prod);
            }
        }

        return resultadoEstado;
    }

    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        if (estado == null) {
            return pedidoDAO.getPedidos();
        }
        return pedidoDAO.getPedidosPorEstado(estado);
    }

    public List<Pedido> listarTodos() {
        return pedidoDAO.getPedidos();
    }

    public List<DetallePedido> obtenerDetalles(int pedidoId) {
        if (pedidoId <= 0) {
            return List.of();
        }
        return pedidoDAO.getDetallesPedido(pedidoId);
    }

    public Pedido obtenerPorId(int pedidoId) {
        if (pedidoId <= 0) {
            return null;
        }
        Pedido pedido = pedidoDAO.getPedidoPorId(pedidoId);
        if (pedido.getIdPedido() == null) {
            return null;
        }
        return pedido;
    }
}
