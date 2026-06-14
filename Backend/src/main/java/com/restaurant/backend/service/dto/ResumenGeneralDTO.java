package com.restaurant.backend.service.dto;

import java.math.BigDecimal;

public class ResumenGeneralDTO {
    private long pedidosCerrados;
    private BigDecimal totalRecaudado;

    public ResumenGeneralDTO() {
        this.totalRecaudado = BigDecimal.ZERO;
    }

    public ResumenGeneralDTO(long pedidosCerrados, BigDecimal totalRecaudado) {
        this.pedidosCerrados = pedidosCerrados;
        this.totalRecaudado = totalRecaudado;
    }

    public long getPedidosCerrados() {
        return pedidosCerrados;
    }

    public void setPedidosCerrados(long pedidosCerrados) {
        this.pedidosCerrados = pedidosCerrados;
    }

    public BigDecimal getTotalRecaudado() {
        return totalRecaudado;
    }

    public void setTotalRecaudado(BigDecimal totalRecaudado) {
        this.totalRecaudado = totalRecaudado;
    }
}
