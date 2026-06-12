package com.restaurant.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pedido {
    private Integer idPedido;
    private Mesa mesa;
    private Usuario usuario;
    private EstadoPedido estado;
    private BigDecimal total;
    private String observacion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<DetallePedido> detalles;

    public Pedido() {
        this.estado = EstadoPedido.ABIERTO;
        this.total = BigDecimal.ZERO;
        this.detalles = new ArrayList<>();
    }

    public Pedido(Integer idPedido, Mesa mesa, Usuario usuario, EstadoPedido estado, BigDecimal total,
            String observacion) {
        this.idPedido = idPedido;
        this.mesa = mesa;
        this.usuario = usuario;
        this.estado = estado;
        this.total = total;
        this.observacion = observacion;
        this.detalles = new ArrayList<>();
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        recalcularTotal();
    }

    public void quitarDetalle(DetallePedido detalle) {
        detalles.remove(detalle);
        recalcularTotal();
    }

    public void recalcularTotal() {
        total = detalles.stream()
                .map(DetallePedido::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return "Pedido #" + idPedido;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Pedido pedido)) {
            return false;
        }
        return Objects.equals(idPedido, pedido.idPedido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPedido);
    }
}