package com.restaurant.backend.dao;

import com.restaurant.backend.model.Categoria;
import com.restaurant.backend.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl implements ProductoDAO {

    private static final String SELECT_PRODUCTOS = """
            SELECT
                p.id_producto,
                p.nombre,
                p.descripcion,
                p.precio,
                p.stock,
                p.disponible,
                p.created_at,
                p.updated_at,
                c.id_categoria,
                c.nombre AS categoria_nombre,
                c.descripcion AS categoria_descripcion,
                c.activa AS categoria_activa
            FROM productos p
            JOIN categorias c ON p.id_categoria = c.id_categoria
            """;

    @Override
    public List<Producto> getProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = SELECT_PRODUCTOS + "ORDER BY p.id_producto";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException ex) {
            System.out.println("Error al obtener productos: " + ex.getMessage());
        }

        return productos;
    }


    
    @Override
    public Producto FiltrarPorCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return null;
        }

        String sql = SELECT_PRODUCTOS
                + "WHERE LOWER(c.nombre) = LOWER(?) AND p.disponible = TRUE "
                + "ORDER BY p.id_producto LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoria.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error al filtrar producto por categoria: " + ex.getMessage());
        }

        return null;
    }

    @Override
    public String insertar() {
        return "Debe indicar un producto para insertar";
    }

    @Override
    public String insertar(Producto producto) {
        String validacion = validarProducto(producto, false);
        if (validacion != null) {
            return validacion;
        }

        String sql = """
                INSERT INTO productos (nombre, descripcion, precio, stock, id_categoria, disponible)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setInt(5, producto.getCategoria().getIdCategoria());
            ps.setBoolean(6, producto.isDisponible());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                return "No se pudo insertar el producto";
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    producto.setIdProducto(keys.getInt(1));
                }
            }

            return "Producto insertado correctamente";
        } catch (SQLException ex) {
            return "Error al insertar producto: " + ex.getMessage();
        }
    }

    @Override
    public String eliminar() {
        return "Debe indicar el id del producto para eliminar";
    }

    @Override
    public String eliminar(int id) {
        if (id <= 0) {
            return "El id del producto debe ser mayor a cero";
        }

        String sql = "DELETE FROM productos WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            return filas > 0 ? "Producto eliminado correctamente" : "No existe un producto con ese id";
        } catch (SQLException ex) {
            return "Error al eliminar producto: " + ex.getMessage();
        }
    }

    @Override
    public String editar() {
        return "Debe indicar un producto para editar";
    }

    @Override
    public String editar(Producto producto) {
        String validacion = validarProducto(producto, true);
        if (validacion != null) {
            return validacion;
        }

        String sql = """
                UPDATE productos
                SET nombre = ?, descripcion = ?, precio = ?, stock = ?, id_categoria = ?, disponible = ?
                WHERE id_producto = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setInt(5, producto.getCategoria().getIdCategoria());
            ps.setBoolean(6, producto.isDisponible());
            ps.setInt(7, producto.getIdProducto());

            int filas = ps.executeUpdate();
            return filas > 0 ? "Producto editado correctamente" : "No existe un producto con ese id";
        } catch (SQLException ex) {
            return "Error al editar producto: " + ex.getMessage();
        }
    }

    @Override
    public Producto getProductoPorId(int id) {
        if (id <= 0) {
            return null;
        }

        String sql = SELECT_PRODUCTOS + "WHERE p.id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error al obtener producto por id: " + ex.getMessage());
        }

        return null;
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombre(rs.getString("categoria_nombre"));
        categoria.setDescripcion(rs.getString("categoria_descripcion"));
        categoria.setActiva(rs.getBoolean("categoria_activa"));

        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setStock(rs.getInt("stock"));
        producto.setCategoria(categoria);
        producto.setDisponible(rs.getBoolean("disponible"));
        producto.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        producto.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));

        return producto;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
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