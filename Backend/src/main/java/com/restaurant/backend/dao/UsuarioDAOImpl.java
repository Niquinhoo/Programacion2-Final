package com.restaurant.backend.dao;

import com.restaurant.backend.model.Rol;
import com.restaurant.backend.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de UsuarioDAO con autenticación mediante SHA2(?, 256).
 * Utiliza la misma infraestructura de conexión que el resto de los DAOs del proyecto.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    // Query base para SELECT con JOIN a roles
    private static final String SELECT_USUARIO_BASE = """
            SELECT
                u.id_usuario,
                u.nombre,
                u.apellido,
                u.usuario,
                u.activo,
                u.created_at,
                r.id_rol,
                r.nombre AS rol_nombre
            FROM usuarios u
            JOIN roles r ON u.id_rol = r.id_rol
            """;

    /**
     * Autentica al usuario comparando la contraseña con SHA2(?, 256) directamente en SQL.
     * Solo autentica usuarios activos.
     */
    @Override
    public Usuario autenticar(String nombreUsuario, String contrasenaPlana) {
        if (nombreUsuario == null || nombreUsuario.isBlank()
                || contrasenaPlana == null || contrasenaPlana.isBlank()) {
            return null;
        }

        String sql = SELECT_USUARIO_BASE
                + "WHERE u.usuario = ? AND u.contrasena = SHA2(?, 256) AND u.activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario.trim());
            ps.setString(2, contrasenaPlana);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error al autenticar usuario: " + ex.getMessage());
        }

        return null;
    }

    /**
     * Retorna todos los usuarios activos con su rol cargado.
     */
    @Override
    public List<Usuario> getUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = SELECT_USUARIO_BASE + "WHERE u.activo = TRUE ORDER BY u.apellido, u.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        } catch (SQLException ex) {
            System.out.println("Error al obtener usuarios: " + ex.getMessage());
        }

        return usuarios;
    }

    /**
     * Obtiene un usuario por su ID (activo o inactivo).
     */
    @Override
    public Usuario getUsuarioPorId(int id) {
        if (id <= 0) {
            return null;
        }

        String sql = SELECT_USUARIO_BASE + "WHERE u.id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error al obtener usuario por id: " + ex.getMessage());
        }

        return null;
    }

    /**
     * Inserta un nuevo usuario. La contraseña en texto plano se hashea con SHA2(?, 256) en SQL.
     */
    @Override
    public String insertar(Usuario usuario) {
        String validacion = validarUsuario(usuario, false);
        if (validacion != null) {
            return validacion;
        }

        String sql = """
                INSERT INTO usuarios (nombre, apellido, usuario, contrasena, id_rol, activo)
                VALUES (?, ?, ?, SHA2(?, 256), ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setString(3, usuario.getUsuario());
            ps.setString(4, usuario.getContrasena()); // texto plano, SHA2 se aplica en la query
            ps.setInt(5, usuario.getRol().getIdRol());
            ps.setBoolean(6, usuario.isActivo());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                return "No se pudo insertar el usuario";
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setIdUsuario(keys.getInt(1));
                }
            }

            return "Usuario insertado correctamente";
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
                return "Ya existe un usuario con ese nombre de usuario";
            }
            return "Error al insertar usuario: " + ex.getMessage();
        }
    }

    /**
     * Actualiza nombre, apellido, usuario y rol (sin modificar la contraseña).
     */
    @Override
    public String actualizar(Usuario usuario) {
        String validacion = validarUsuario(usuario, true);
        if (validacion != null) {
            return validacion;
        }

        String sql = """
                UPDATE usuarios
                SET nombre = ?, apellido = ?, usuario = ?, id_rol = ?
                WHERE id_usuario = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setString(3, usuario.getUsuario());
            ps.setInt(4, usuario.getRol().getIdRol());
            ps.setInt(5, usuario.getIdUsuario());

            int filas = ps.executeUpdate();
            return filas > 0 ? "Usuario actualizado correctamente" : "No existe un usuario con ese id";
        } catch (SQLException ex) {
            return "Error al actualizar usuario: " + ex.getMessage();
        }
    }

    /**
     * Realiza un soft-delete: marca el usuario como activo = false.
     */
    @Override
    public String desactivar(int id) {
        if (id <= 0) {
            return "El id del usuario debe ser mayor a cero";
        }

        String sql = "UPDATE usuarios SET activo = FALSE WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            return filas > 0 ? "Usuario desactivado correctamente" : "No existe un usuario con ese id";
        } catch (SQLException ex) {
            return "Error al desactivar usuario: " + ex.getMessage();
        }
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("id_rol"));
        rol.setNombre(rs.getString("rol_nombre"));

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setUsuario(rs.getString("usuario"));
        usuario.setRol(rol);
        usuario.setActivo(rs.getBoolean("activo"));

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            usuario.setCreatedAt(createdAt.toLocalDateTime());
        }

        return usuario;
    }

    private String validarUsuario(Usuario usuario, boolean requiereId) {
        if (usuario == null) {
            return "El usuario no puede ser nulo";
        }
        if (requiereId && (usuario.getIdUsuario() == null || usuario.getIdUsuario() <= 0)) {
            return "El id del usuario debe ser mayor a cero";
        }
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            return "El nombre del usuario es obligatorio";
        }
        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
            return "El apellido del usuario es obligatorio";
        }
        if (usuario.getUsuario() == null || usuario.getUsuario().isBlank()) {
            return "El nombre de usuario es obligatorio";
        }
        if (!requiereId && (usuario.getContrasena() == null || usuario.getContrasena().isBlank())) {
            return "La contraseña del usuario es obligatoria";
        }
        if (usuario.getRol() == null || usuario.getRol().getIdRol() == null
                || usuario.getRol().getIdRol() <= 0) {
            return "El rol del usuario es obligatorio";
        }
        return null;
    }
}
