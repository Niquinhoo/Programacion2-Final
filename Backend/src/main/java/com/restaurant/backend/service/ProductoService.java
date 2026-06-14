package com.restaurant.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.restaurant.backend.dao.ProductoDAO;
import com.restaurant.backend.dao.ProductoDAOImpl;
import com.restaurant.backend.model.Producto;

public class ProductoService {

    private final ProductoDAO productoDAO;

    public ProductoService() {
        this(new ProductoDAOImpl());
    }

    public ProductoService(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    public List<Producto> obtenerTodos() {
        return productoDAO.getProductos();
    }

    public List<Producto> obtenerPorCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return new ArrayList<>();
        }

        String categoriaNormalizada = categoria.trim().toLowerCase(Locale.ROOT);
        List<Producto> filtrados = new ArrayList<>();

        for (Producto producto : productoDAO.getProductos()) {
            if (producto.getCategoria() != null
                    && producto.getCategoria().getNombre() != null
                    && producto.getCategoria().getNombre().trim().toLowerCase(Locale.ROOT)
                            .equals(categoriaNormalizada)
                    && producto.isDisponible()) {
                filtrados.add(producto);
            }
        }

        return filtrados;
    }

    public Producto obtenerPorId(int id) {
        if (id <= 0) {
            return null;
        }
        return productoDAO.getProductoPorId(id);
    }

    public String crear(Producto producto) {
        String validacion = validarProducto(producto, false);
        if (validacion != null) {
            return validacion;
        }
        return productoDAO.insertar(producto);
    }

    public String actualizar(Producto producto) {
        String validacion = validarProducto(producto, true);
        if (validacion != null) {
            return validacion;
        }
        return productoDAO.editar(producto);
    }

    public String eliminar(int id) {
        if (id <= 0) {
            return "El id del producto debe ser mayor a cero";
        }
        return productoDAO.eliminar(id);
    }

    public String descontarStock(int productoId, int cantidad) {
        if (productoId <= 0) {
            return "El id del producto debe ser mayor a cero";
        }
        if (cantidad <= 0) {
            return "La cantidad a descontar debe ser mayor a cero";
        }

        Producto producto = productoDAO.getProductoPorId(productoId);
        if (producto == null) {
            return "No existe un producto con ese id";
        }
        if (producto.getStock() < cantidad) {
            return "Stock insuficiente para el producto " + producto.getNombre();
        }

        producto.setStock(producto.getStock() - cantidad);
        return productoDAO.editar(producto);
    }

    public String validarStockDisponible(int productoId, int cantidad) {
        if (productoId <= 0) {
            return "El id del producto debe ser mayor a cero";
        }
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor a cero";
        }

        Producto producto = productoDAO.getProductoPorId(productoId);
        if (producto == null) {
            return "No existe un producto con ese id";
        }
        if (!producto.isDisponible()) {
            return "El producto " + producto.getNombre() + " no esta disponible";
        }
        if (producto.getStock() < cantidad) {
            return "Stock insuficiente para el producto " + producto.getNombre();
        }

        return null;
    }

    private String validarProducto(Producto producto, boolean requiereId) {
        if (producto == null) {
            return "El producto no puede ser nulo";
        }
        if (requiereId && (producto.getIdProducto() == null || producto.getIdProducto() <= 0)) {
            return "El id del producto debe ser mayor a cero";
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            return "El nombre del producto es obligatorio";
        }
        if (producto.getPrecio() == null || producto.getPrecio().signum() < 0) {
            return "El precio del producto debe ser mayor o igual a cero";
        }
        if (producto.getStock() < 0) {
            return "El stock del producto debe ser mayor o igual a cero";
        }
        if (producto.getCategoria() == null || producto.getCategoria().getIdCategoria() == null
                || producto.getCategoria().getIdCategoria() <= 0) {
            return "La categoria del producto es obligatoria";
        }

        return null;
    }
}
