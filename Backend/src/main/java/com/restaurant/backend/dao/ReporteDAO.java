package com.restaurant.backend.dao;

import java.util.List;

import com.restaurant.backend.service.dto.ResumenGeneralDTO;
import com.restaurant.backend.service.dto.VentaPorMesDTO;
import com.restaurant.backend.service.dto.VentaPorProductoDTO;

public interface ReporteDAO {
    List<VentaPorProductoDTO> ventasPorProducto();

    List<VentaPorMesDTO> ventasPorMes();

    ResumenGeneralDTO resumenGeneral();
}
