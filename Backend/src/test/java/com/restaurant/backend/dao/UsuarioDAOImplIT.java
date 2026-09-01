package com.restaurant.backend.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.restaurant.backend.model.Rol;
import com.restaurant.backend.model.Usuario;
import com.restaurant.backend.support.DedicatedDatabaseExtension;

@Tag("integration")
@DisplayName("UsuarioDAOImpl — Tests de Integración")
@ExtendWith(DedicatedDatabaseExtension.class)
class UsuarioDAOImplIT {

    private Integer usuarioCreadoId;

    @AfterEach
    void limpiar() throws Exception {
        if (usuarioCreadoId == null) {
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM usuarios WHERE id_usuario = ?")) {
            statement.setInt(1, usuarioCreadoId);
            statement.executeUpdate();
        }
    }

    @Test
    void flujoCrudYAutenticacionUsaHashSha256() {
        UsuarioDAOImpl dao = new UsuarioDAOImpl();
        String nombreUsuario = "junit_" + System.nanoTime();
        Usuario usuario = new Usuario(null, "JUnit", "Prueba", nombreUsuario,
                "secreto123", new Rol(2, "MOZO"), true);

        assertEquals("Usuario insertado correctamente", dao.insertar(usuario));
        usuarioCreadoId = usuario.getIdUsuario();
        assertNotNull(usuarioCreadoId);

        Usuario autenticado = dao.autenticar(nombreUsuario, "secreto123");
        assertNotNull(autenticado);
        assertEquals("MOZO", autenticado.getRol().getNombre());
        assertNull(dao.autenticar(nombreUsuario, "incorrecta"));

        usuario.setNombre("JUnit Editado");
        assertEquals("Usuario actualizado correctamente", dao.actualizar(usuario));
        assertEquals("JUnit Editado", dao.getUsuarioPorId(usuarioCreadoId).getNombre());

        assertEquals("Usuario desactivado correctamente", dao.desactivar(usuarioCreadoId));
        assertNull(dao.autenticar(nombreUsuario, "secreto123"));
    }
}
