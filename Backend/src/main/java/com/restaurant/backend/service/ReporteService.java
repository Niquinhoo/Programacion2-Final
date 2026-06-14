package com.restaurant.backend.service;

import java.util.List;

import com.restaurant.backend.dao.ReporteDAO;
import com.restaurant.backend.dao.ReporteDAOImpl;
import com.restaurant.backend.service.dto.ResumenGeneralDTO;
import com.restaurant.backend.service.dto.VentaPorMesDTO;
import com.restaurant.backend.service.dto.VentaPorProductoDTO;

public class ReporteService {

    private final ReporteDAO reporteDAO;

    public ReporteService() {
        this(new ReporteDAOImpl());
    }

    public ReporteService(ReporteDAO reporteDAO) {
        this.reporteDAO = reporteDAO;
    }

    public List<VentaPorProductoDTO> ventasPorProducto() {
        return reporteDAO.ventasPorProducto();
    }

    public List<VentaPorMesDTO> ventasPorMes() {
        return reporteDAO.ventasPorMes();
    }

    public ResumenGeneralDTO resumenGeneral() {
        return reporteDAO.resumenGeneral();
    }
}
