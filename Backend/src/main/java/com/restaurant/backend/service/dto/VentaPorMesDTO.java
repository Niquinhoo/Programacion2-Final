package com.restaurant.backend.service.dto;

import java.math.BigDecimal;

public class VentaPorMesDTO {
    private int anio;
    private int mes;
    private long cantidadPedidos;
    private BigDecimal totalMes;

    public VentaPorMesDTO() {
        this.totalMes = BigDecimal.ZERO;
    }

    public VentaPorMesDTO(int anio, int mes, long cantidadPedidos, BigDecimal totalMes) {
        this.anio = anio;
        this.mes = mes;
        this.cantidadPedidos = cantidadPedidos;
        this.totalMes = totalMes;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public long getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(long cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }

    public BigDecimal getTotalMes() {
        return totalMes;
    }

    public void setTotalMes(BigDecimal totalMes) {
        this.totalMes = totalMes;
    }
}
