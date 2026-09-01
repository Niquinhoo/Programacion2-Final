package com.restaurant.backend.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.restaurant.backend.service.dto.ResumenGeneralDTO;
import com.restaurant.backend.support.DedicatedDatabaseExtension;

@Tag("integration")
@DisplayName("ReporteDAOImpl — Tests de Integración")
@ExtendWith(DedicatedDatabaseExtension.class)
class ReporteDAOImplIT {

    @Test
    void consultasDeReporteDevuelvenResultadosValidos() {
        ReporteDAOImpl dao = new ReporteDAOImpl();

        assertNotNull(dao.ventasPorProducto());
        assertFalse(dao.ventasPorMes().isEmpty());
        ResumenGeneralDTO resumen = dao.resumenGeneral();
        assertNotNull(resumen);
        assertTrue(resumen.getPedidosCerrados() >= 0);
        assertNotNull(resumen.getTotalRecaudado());
    }
}
