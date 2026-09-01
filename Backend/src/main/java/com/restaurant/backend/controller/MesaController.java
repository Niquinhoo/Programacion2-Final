package com.restaurant.backend.controller;

import java.util.List;

import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.Mesa;
import com.restaurant.backend.service.MesaService;
import com.restaurant.backend.service.ServicioFactory;

public class MesaController {

    private final MesaService mesaService;

    public MesaController() {
        this(ServicioFactory.getMesaService());
    }

    MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    public List<Mesa> listarMesas() {
        return mesaService.listar();
    }

    public Mesa obtenerPorNumero(int numero) {
        return mesaService.obtenerPorNumero(numero);
    }

    public Mesa obtenerPorId(int mesaId) {
        return mesaService.obtenerPorId(mesaId);
    }

    public String ocupar(int mesaId) {
        return mesaService.ocupar(mesaId);
    }

    public String liberar(int mesaId) {
        return mesaService.liberar(mesaId);
    }

    public String reservar(int mesaId) {
        return mesaService.reservar(mesaId);
    }

    public String cancelarReserva(int mesaId) {
        return mesaService.cancelarReserva(mesaId);
    }

    public String cambiarEstado(int mesaId, EstadoMesa estado) {
        return mesaService.cambiarEstado(mesaId, estado);
    }
}
