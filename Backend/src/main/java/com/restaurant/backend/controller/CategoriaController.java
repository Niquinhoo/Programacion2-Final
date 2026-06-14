package com.restaurant.backend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.restaurant.backend.model.Categoria;
import com.restaurant.backend.model.Producto;
import com.restaurant.backend.service.ProductoService;
import com.restaurant.backend.service.ServicioFactory;

public class CategoriaController {

    private final ProductoService productoService = ServicioFactory.getProductoService();

    public List<Categoria> listar() {
        Map<Integer, Categoria> categorias = new LinkedHashMap<>();

        for (Producto producto : productoService.obtenerTodos()) {
            if (producto.getCategoria() != null && producto.getCategoria().getIdCategoria() != null) {
                categorias.putIfAbsent(producto.getCategoria().getIdCategoria(), producto.getCategoria());
            }
        }

        return new ArrayList<>(categorias.values());
    }
}
