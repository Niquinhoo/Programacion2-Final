package com.restaurant.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurant.backend.dao.UsuarioDAO;
import com.restaurant.backend.model.Rol;
import com.restaurant.backend.model.Usuario;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — pruebas unitarias")
class UsuarioServiceTest {

    @Mock private UsuarioDAO dao;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(dao);
    }

    @Test
    void iniciarSesionValidaVaciosYNormalizaUsuario() {
        assertNull(service.iniciarSesion(" ", "clave"));
        assertNull(service.iniciarSesion("usuario", " "));
        verifyNoInteractions(dao);

        Usuario esperado = usuarioValido();
        when(dao.autenticar("usuario", "secreto")).thenReturn(esperado);
        assertSame(esperado, service.iniciarSesion(" usuario ", "secreto"));
    }

    @Test
    void registrarValidaTodosLosCamposAntesDePersistir() {
        assertEquals("El usuario no puede ser nulo", service.registrar(null));

        Usuario usuario = usuarioValido();
        usuario.setContrasena("123");
        assertEquals("La contraseña debe tener al menos 6 caracteres", service.registrar(usuario));

        usuario = usuarioValido();
        usuario.setRol(null);
        assertEquals("El rol del usuario es obligatorio", service.registrar(usuario));
        verify(dao, never()).insertar(any());
    }

    @Test
    void registrarValidoDelegaEnDao() {
        Usuario usuario = usuarioValido();
        when(dao.insertar(usuario)).thenReturn("Usuario insertado correctamente");

        assertEquals("Usuario insertado correctamente", service.registrar(usuario));
        verify(dao).insertar(usuario);
    }

    @Test
    void actualizarYDesactivarValidanId() {
        Usuario usuario = usuarioValido();
        usuario.setIdUsuario(null);

        assertEquals("El usuario debe tener un id válido", service.actualizar(usuario));
        assertEquals("El id del usuario debe ser mayor a cero", service.desactivar(0));
        assertNull(service.obtenerPorId(0));
        verifyNoInteractions(dao);
    }

    private Usuario usuarioValido() {
        return new Usuario(1, "Ana", "Pérez", "usuario", "secreto", new Rol(2, "MOZO"), true);
    }
}
