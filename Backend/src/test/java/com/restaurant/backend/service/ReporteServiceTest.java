package com.restaurant.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurant.backend.dao.ReporteDAO;
import com.restaurant.backend.service.dto.ResumenGeneralDTO;
import com.restaurant.backend.service.dto.VentaPorMesDTO;
import com.restaurant.backend.service.dto.VentaPorProductoDTO;

@DisplayName("Reportes y fábrica — pruebas unitarias")
class ReporteServiceTest {

    @Test
    void reporteServiceDelegaSinAlterarResultados() {
        ReporteDAO dao = mock(ReporteDAO.class);
        ReporteService service = new ReporteService(dao);
        List<VentaPorProductoDTO> productos = List.of(new VentaPorProductoDTO());
        List<VentaPorMesDTO> meses = List.of(new VentaPorMesDTO());
        ResumenGeneralDTO resumen = new ResumenGeneralDTO(2, new BigDecimal("500.00"));
        when(dao.ventasPorProducto()).thenReturn(productos);
        when(dao.ventasPorMes()).thenReturn(meses);
        when(dao.resumenGeneral()).thenReturn(resumen);

        assertSame(productos, service.ventasPorProducto());
        assertSame(meses, service.ventasPorMes());
        assertSame(resumen, service.resumenGeneral());
    }

    @Test
    void servicioFactoryDevuelveSiempreLaMismaInstancia() {
        assertSame(ServicioFactory.getProductoService(), ServicioFactory.getProductoService());
        assertSame(ServicioFactory.getMesaService(), ServicioFactory.getMesaService());
        assertSame(ServicioFactory.getPedidoService(), ServicioFactory.getPedidoService());
        assertSame(ServicioFactory.getReporteService(), ServicioFactory.getReporteService());
        assertSame(ServicioFactory.getUsuarioService(), ServicioFactory.getUsuarioService());
    }
}
