package com.restaurant.backend.service;

import com.restaurant.backend.dao.UsuarioDAO;
import com.restaurant.backend.dao.UsuarioDAOImpl;
import com.restaurant.backend.model.Usuario;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión y autenticación de usuarios.
 * Actúa como capa intermedia entre los controladores (vista) y el DAO.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this(new UsuarioDAOImpl());
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Autentica un usuario por nombre de usuario y contraseña en texto plano.
     * La verificación del hash SHA2 se realiza directamente en la base de datos.
     *
     * @param nombreUsuario nombre de usuario (campo 'usuario' en la tabla)
     * @param contrasenaPlana contraseña en texto plano
     * @return el Usuario autenticado con su Rol cargado, o null si las credenciales son inválidas
     */
    public Usuario iniciarSesion(String nombreUsuario, String contrasenaPlana) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            return null;
        }
        if (contrasenaPlana == null || contrasenaPlana.isBlank()) {
            return null;
        }
        return usuarioDAO.autenticar(nombreUsuario.trim(), contrasenaPlana);
    }

    /**
     * Obtiene todos los usuarios activos del sistema.
     *
     * @return lista de usuarios activos
     */
    public List<Usuario> listar() {
        return usuarioDAO.getUsuarios();
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id el id del usuario
     * @return el Usuario encontrado, o null si no existe o el id es inválido
     */
    public Usuario obtenerPorId(int id) {
        if (id <= 0) {
            return null;
        }
        return usuarioDAO.getUsuarioPorId(id);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * La contraseña en texto plano se hashea con SHA2-256 al insertarla.
     *
     * @param usuario el usuario a registrar (la contraseña en texto plano en usuario.getContrasena())
     * @return mensaje de resultado de la operación
     */
    public String registrar(Usuario usuario) {
        if (usuario == null) {
            return "El usuario no puede ser nulo";
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
        if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
            return "La contraseña es obligatoria";
        }
        if (usuario.getContrasena().length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }
        if (usuario.getRol() == null || usuario.getRol().getIdRol() == null
                || usuario.getRol().getIdRol() <= 0) {
            return "El rol del usuario es obligatorio";
        }
        return usuarioDAO.insertar(usuario);
    }

    /**
     * Actualiza los datos de un usuario existente (sin modificar la contraseña).
     *
     * @param usuario el usuario con los datos actualizados (debe tener idUsuario)
     * @return mensaje de resultado de la operación
     */
    public String actualizar(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() == null || usuario.getIdUsuario() <= 0) {
            return "El usuario debe tener un id válido";
        }
        return usuarioDAO.actualizar(usuario);
    }

    /**
     * Desactiva un usuario del sistema (soft-delete).
     *
     * @param id el id del usuario a desactivar
     * @return mensaje de resultado de la operación
     */
    public String desactivar(int id) {
        if (id <= 0) {
            return "El id del usuario debe ser mayor a cero";
        }
        return usuarioDAO.desactivar(id);
    }
}
