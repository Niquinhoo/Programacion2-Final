package com.restaurant.backend.model;

import java.util.Objects;

public class Mesa {
    private Integer idMesa;
    private int numero;
    private int capacidad;
    private EstadoMesa estado;

    public Mesa() {
        this.capacidad = 4;
        this.estado = EstadoMesa.LIBRE;
    }

    public Mesa(Integer idMesa, int numero, int capacidad, EstadoMesa estado) {
        this.idMesa = idMesa;
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public Integer getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(Integer idMesa) {
        this.idMesa = idMesa;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public void setEstado(EstadoMesa estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Mesa " + numero;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Mesa mesa)) {
            return false;
        }
        return Objects.equals(idMesa, mesa.idMesa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMesa);
    }
}