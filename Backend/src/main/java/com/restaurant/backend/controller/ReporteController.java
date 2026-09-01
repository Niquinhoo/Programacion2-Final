package com.restaurant.backend.controller;

import java.util.List;

import com.restaurant.backend.service.ReporteService;
import com.restaurant.backend.service.ServicioFactory;
import com.restaurant.backend.service.dto.ResumenGeneralDTO;
import com.restaurant.backend.service.dto.VentaPorMesDTO;
import com.restaurant.backend.service.dto.VentaPorProductoDTO;

public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController() {
        this(ServicioFactory.getReporteService());
    }

    ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    public List<VentaPorProductoDTO> ventasPorProducto() {
        return reporteService.ventasPorProducto();
    }

    public List<VentaPorMesDTO> ventasPorMes() {
        return reporteService.ventasPorMes();
    }

    public ResumenGeneralDTO resumenGeneral() {
        return reporteService.resumenGeneral();
    }
}
