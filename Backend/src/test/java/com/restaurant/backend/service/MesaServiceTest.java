package com.restaurant.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurant.backend.dao.MesaDAO;
import com.restaurant.backend.dao.PedidoDAO;
import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.EstadoPedido;
import com.restaurant.backend.model.Mesa;
import com.restaurant.backend.model.Pedido;

@ExtendWith(MockitoExtension.class)
@DisplayName("MesaService — pruebas unitarias")
class MesaServiceTest {

    @Mock private MesaDAO mesaDAO;
    @Mock private PedidoDAO pedidoDAO;
    private MesaService service;

    @BeforeEach
    void setUp() {
        service = new MesaService(mesaDAO, pedidoDAO);
    }

    @Test
    void obtenerPorNumeroBuscaEnListado() {
        Mesa uno = mesa(1, 1, EstadoMesa.LIBRE);
        Mesa dos = mesa(2, 2, EstadoMesa.RESERVADA);
        when(mesaDAO.getMesas()).thenReturn(List.of(uno, dos));

        assertSame(dos, service.obtenerPorNumero(2));
        assertNull(service.obtenerPorNumero(3));
    }

    @Test
    void ocuparAceptaMesaLibreOReservada() {
        Mesa mesa = mesa(1, 1, EstadoMesa.RESERVADA);
        when(mesaDAO.getMesaPorId(1)).thenReturn(mesa);
        when(mesaDAO.cambiarEstado(1, EstadoMesa.OCUPADA)).thenReturn("Se cambio el estado");

        assertEquals("Se cambio el estado", service.ocupar(1));
        verify(mesaDAO).cambiarEstado(1, EstadoMesa.OCUPADA);
    }

    @Test
    void ocuparRechazaEstadoNoOcupable() {
        when(mesaDAO.getMesaPorId(1)).thenReturn(mesa(1, 1, EstadoMesa.FUERA_DE_SERVICIO));

        assertEquals("La mesa no puede ocuparse en estado FUERA_DE_SERVICIO", service.ocupar(1));
        verify(mesaDAO, never()).cambiarEstado(anyInt(), any());
    }

    @Test
    void reservarYCancelarReservaRespetanEstado() {
        when(mesaDAO.getMesaPorId(1))
                .thenReturn(mesa(1, 1, EstadoMesa.LIBRE))
                .thenReturn(mesa(1, 1, EstadoMesa.RESERVADA));
        when(mesaDAO.cambiarEstado(eq(1), any())).thenReturn("ok");

        assertEquals("ok", service.reservar(1));
        assertEquals("ok", service.cancelarReserva(1));
        verify(mesaDAO).cambiarEstado(1, EstadoMesa.RESERVADA);
        verify(mesaDAO).cambiarEstado(1, EstadoMesa.LIBRE);
    }

    @Test
    void cambiarEstadoRechazaTransicionInvalida() {
        when(mesaDAO.getMesaPorId(1)).thenReturn(mesa(1, 1, EstadoMesa.OCUPADA));

        assertEquals("Transicion de estado no permitida: OCUPADA -> RESERVADA",
                service.cambiarEstado(1, EstadoMesa.RESERVADA));
        verify(mesaDAO, never()).cambiarEstado(anyInt(), any());
    }

    @Test
    void cambiarEstadoAceptaTransicionesDefinidas() {
        when(mesaDAO.getMesaPorId(1)).thenReturn(mesa(1, 1, EstadoMesa.FUERA_DE_SERVICIO));
        when(mesaDAO.cambiarEstado(1, EstadoMesa.LIBRE)).thenReturn("ok");

        assertEquals("ok", service.cambiarEstado(1, EstadoMesa.LIBRE));
    }

    @Test
    void liberarCierraSoloPedidosActivosYLuegoLiberaMesa() {
        Pedido abierto = pedido(10, EstadoPedido.ABIERTO);
        Pedido cocina = pedido(11, EstadoPedido.EN_COCINA);
        Pedido cerrado = pedido(12, EstadoPedido.CERRADO);
        when(mesaDAO.getMesaPorId(1)).thenReturn(mesa(1, 1, EstadoMesa.OCUPADA));
        when(pedidoDAO.getPedidosPorMesa(1)).thenReturn(List.of(abierto, cocina, cerrado));
        when(mesaDAO.cambiarEstado(1, EstadoMesa.LIBRE)).thenReturn("Se cambio el estado");

        assertEquals("Se cambio el estado", service.liberar(1));

        verify(pedidoDAO).ModificarEstado(10, EstadoPedido.CERRADO);
        verify(pedidoDAO).ModificarEstado(11, EstadoPedido.CERRADO);
        verify(pedidoDAO, never()).ModificarEstado(12, EstadoPedido.CERRADO);
        verify(mesaDAO).cambiarEstado(1, EstadoMesa.LIBRE);
    }

    @Test
    void validacionesBasicasEvitanAccesoInnecesario() {
        assertNull(service.obtenerPorId(0));
        assertEquals("El id de la mesa debe ser mayor a cero", service.cambiarEstado(0, EstadoMesa.LIBRE));
        assertEquals("El estado de la mesa es obligatorio", service.cambiarEstado(1, null));
        verifyNoInteractions(mesaDAO, pedidoDAO);
    }

    private Mesa mesa(int id, int numero, EstadoMesa estado) {
        return new Mesa(id, numero, 4, estado);
    }

    private Pedido pedido(int id, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(id);
        pedido.setEstado(estado);
        return pedido;
    }
}
