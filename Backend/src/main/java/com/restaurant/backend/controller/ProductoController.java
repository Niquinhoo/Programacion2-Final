package com.restaurant.backend.controller;

import java.util.List;

import com.restaurant.backend.model.Producto;
import com.restaurant.backend.service.ProductoService;
import com.restaurant.backend.service.ServicioFactory;

public class ProductoController {

    private final ProductoService productoService;

    public ProductoController() {
        this(ServicioFactory.getProductoService());
    }

    ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    public List<Producto> listar() {
        return productoService.obtenerTodos();
    }

    public List<Producto> listarPorCategoria(String categoria) {
        return productoService.obtenerPorCategoria(categoria);
    }

    public Producto obtenerPorId(int id) {
        return productoService.obtenerPorId(id);
    }

    public String crear(Producto producto) {
        return productoService.crear(producto);
    }

    public String editar(Producto producto) {
        return productoService.actualizar(producto);
    }

    public String eliminar(int id) {
        return productoService.eliminar(id);
    }
}
