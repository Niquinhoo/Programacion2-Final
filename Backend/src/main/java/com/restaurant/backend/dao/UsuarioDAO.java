package com.restaurant.backend.dao;

import com.restaurant.backend.model.Usuario;
import java.util.List;

/**
 * Interfaz DAO para la gestión de usuarios del sistema.
 * Proporciona operaciones de autenticación y CRUD básico.
 */
public interface UsuarioDAO {

    /**
     * Autentica un usuario verificando sus credenciales.
     * La contraseña se compara usando SHA2(?, 256) directamente en la query SQL.
     *
     * @param nombreUsuario el nombre de usuario (campo 'usuario' en la tabla)
     * @param contrasenaPlana la contraseña en texto plano (se hashea en la query)
     * @return el Usuario autenticado con su Rol cargado, o null si las credenciales son inválidas
     */
    Usuario autenticar(String nombreUsuario, String contrasenaPlana);

    /**
     * Obtiene todos los usuarios activos del sistema.
     *
     * @return lista de usuarios activos con su rol cargado
     */
    List<Usuario> getUsuarios();

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id el id del usuario
     * @return el Usuario encontrado o null si no existe
     */
    Usuario getUsuarioPorId(int id);

    /**
     * Inserta un nuevo usuario en el sistema.
     * La contraseña ya debe venir hasheada (SHA-256) o en texto plano según la implementación.
     *
     * @param usuario el usuario a insertar (contrasena en texto plano, se hashea con SHA2 en la query)
     * @return mensaje de resultado de la operación
     */
    String insertar(Usuario usuario);

    /**
     * Actualiza los datos de un usuario existente (sin modificar la contraseña).
     *
     * @param usuario el usuario con los datos actualizados (debe tener idUsuario)
     * @return mensaje de resultado de la operación
     */
    String actualizar(Usuario usuario);

    /**
     * Desactiva un usuario (soft-delete: activo = false).
     *
     * @param id el id del usuario a desactivar
     * @return mensaje de resultado de la operación
     */
    String desactivar(int id);
}
