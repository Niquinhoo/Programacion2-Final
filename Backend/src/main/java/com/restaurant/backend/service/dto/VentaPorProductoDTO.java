package com.restaurant.backend.service.dto;

import java.math.BigDecimal;

public class VentaPorProductoDTO {
    private int idProducto;
    private String producto;
    private String categoria;
    private long unidadesVendidas;
    private BigDecimal totalRecaudado;

    public VentaPorProductoDTO() {
        this.totalRecaudado = BigDecimal.ZERO;
    }

    public VentaPorProductoDTO(int idProducto, String producto, String categoria, long unidadesVendidas,
            BigDecimal totalRecaudado) {
        this.idProducto = idProducto;
        this.producto = producto;
        this.categoria = categoria;
        this.unidadesVendidas = unidadesVendidas;
        this.totalRecaudado = totalRecaudado;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public long getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public void setUnidadesVendidas(long unidadesVendidas) {
        this.unidadesVendidas = unidadesVendidas;
    }

    public BigDecimal getTotalRecaudado() {
        return totalRecaudado;
    }

    public void setTotalRecaudado(BigDecimal totalRecaudado) {
        this.totalRecaudado = totalRecaudado;
    }
}
