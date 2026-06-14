package com.restaurant.backend.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.restaurant.backend.dao.MesaDAO;
import com.restaurant.backend.dao.MesaDAOImpl;
import com.restaurant.backend.dao.PedidoDAO;
import com.restaurant.backend.dao.PedidoDAOImpl;
import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.EstadoPedido;
import com.restaurant.backend.model.Mesa;
import com.restaurant.backend.model.Pedido;

public class MesaService {

    private static final Set<EstadoMesa> ESTADOS_OCUPABLES = EnumSet.of(EstadoMesa.LIBRE, EstadoMesa.RESERVADA);
    private static final Set<EstadoPedido> ESTADOS_PEDIDO_ACTIVO = EnumSet.of(
            EstadoPedido.ABIERTO, EstadoPedido.EN_COCINA, EstadoPedido.LISTO);

    private final MesaDAO mesaDAO;
    private final PedidoDAO pedidoDAO;

    public MesaService() {
        this(new MesaDAOImpl(), new PedidoDAOImpl());
    }

    public MesaService(MesaDAO mesaDAO, PedidoDAO pedidoDAO) {
        this.mesaDAO = mesaDAO;
        this.pedidoDAO = pedidoDAO;
    }

    public List<Mesa> listar() {
        return mesaDAO.getMesas();
    }

    public Mesa obtenerPorNumero(int numero) {
        if (numero <= 0) {
            return null;
        }

        for (Mesa mesa : mesaDAO.getMesas()) {
            if (mesa.getNumero() == numero) {
                return mesa;
            }
        }

        return null;
    }

    public Mesa obtenerPorId(int mesaId) {
        if (mesaId <= 0) {
            return null;
        }
        return mesaDAO.getMesaPorId(mesaId);
    }

    public String ocupar(int mesaId) {
        Mesa mesa = mesaDAO.getMesaPorId(mesaId);
        if (mesa == null) {
            return "No se encontro la mesa";
        }
        if (!ESTADOS_OCUPABLES.contains(mesa.getEstado())) {
            return "La mesa no puede ocuparse en estado " + mesa.getEstado();
        }

        return mesaDAO.cambiarEstado(mesaId, EstadoMesa.OCUPADA);
    }

    public String liberar(int mesaId) {
        Mesa mesa = mesaDAO.getMesaPorId(mesaId);
        if (mesa == null) {
            return "No se encontro la mesa";
        }

        if (tienePedidoActivo(mesaId)) {
            return "No se puede liberar la mesa porque tiene un pedido activo";
        }

        return mesaDAO.cambiarEstado(mesaId, EstadoMesa.LIBRE);
    }

    public String reservar(int mesaId) {
        Mesa mesa = mesaDAO.getMesaPorId(mesaId);
        if (mesa == null) {
            return "No se encontro la mesa";
        }
        if (mesa.getEstado() != EstadoMesa.LIBRE) {
            return "Solo se pueden reservar mesas libres";
        }

        return mesaDAO.cambiarEstado(mesaId, EstadoMesa.RESERVADA);
    }

    public String cancelarReserva(int mesaId) {
        Mesa mesa = mesaDAO.getMesaPorId(mesaId);
        if (mesa == null) {
            return "No se encontro la mesa";
        }
        if (mesa.getEstado() != EstadoMesa.RESERVADA) {
            return "La mesa no tiene una reserva activa";
        }

        return mesaDAO.cambiarEstado(mesaId, EstadoMesa.LIBRE);
    }

    public String cambiarEstado(int mesaId, EstadoMesa nuevoEstado) {
        if (mesaId <= 0) {
            return "El id de la mesa debe ser mayor a cero";
        }
        if (nuevoEstado == null) {
            return "El estado de la mesa es obligatorio";
        }

        Mesa mesa = mesaDAO.getMesaPorId(mesaId);
        if (mesa == null) {
            return "No se encontro la mesa";
        }
        if (!esTransicionPermitida(mesa.getEstado(), nuevoEstado)) {
            return "Transicion de estado no permitida: " + mesa.getEstado() + " -> " + nuevoEstado;
        }

        return mesaDAO.cambiarEstado(mesaId, nuevoEstado);
    }

    private boolean tienePedidoActivo(int mesaId) {
        for (Pedido pedido : pedidoDAO.getPedidosPorMesa(mesaId)) {
            if (ESTADOS_PEDIDO_ACTIVO.contains(pedido.getEstado())) {
                return true;
            }
        }
        return false;
    }

    private boolean esTransicionPermitida(EstadoMesa actual, EstadoMesa nuevo) {
        if (actual == nuevo) {
            return true;
        }

        return switch (actual) {
            case LIBRE -> nuevo == EstadoMesa.OCUPADA
                    || nuevo == EstadoMesa.RESERVADA
                    || nuevo == EstadoMesa.FUERA_DE_SERVICIO;
            case RESERVADA -> nuevo == EstadoMesa.OCUPADA || nuevo == EstadoMesa.LIBRE;
            case OCUPADA -> nuevo == EstadoMesa.LIBRE;
            case FUERA_DE_SERVICIO -> nuevo == EstadoMesa.LIBRE;
        };
    }
}
