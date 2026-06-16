package com.restaurant.backend.dao;

import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.Mesa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para MesaDAOImpl.
 * Requieren conexión real a la base de datos (TiDB Cloud).
 * Cada test que inserta datos realiza cleanup garantizado en @AfterEach.
 * Nota: borrarMesa() lanza UnsupportedOperationException en la implementación actual,
 * por lo que el cleanup usa cambio de estado a FUERA_DE_SERVICIO donde no hay DELETE disponible.
 */
@Tag("integration")
@DisplayName("MesaDAOImpl — Tests de Integración")
class MesaDAOImplTest {

    private MesaDAOImpl dao;
    private Integer mesaInsertadaId; // tracking para cleanup

    // Número alto para evitar conflictos con mesas existentes
    private static final int NUMERO_MESA_PRUEBA = 9990;

    @BeforeEach
    void setUp() {
        dao = new MesaDAOImpl();
        mesaInsertadaId = null;
        // Asegurar que no quede mesa de prueba de una ejecución anterior
        limpiarMesaPrueba();
    }

    @AfterEach
    void tearDown() {
        // borrarMesa() no está implementado aún; cambiamos estado a FUERA_DE_SERVICIO
        // como señal de "mesa de prueba" y luego intentamos borrar si hay soporte futuro
        if (mesaInsertadaId != null && mesaInsertadaId > 0) {
            // Intentar eliminar directamente por SQL via DatabaseConnection
            try (java.sql.Connection conn = DatabaseConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM mesas WHERE id_mesa = ?")) {
                ps.setInt(1, mesaInsertadaId);
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                System.err.println("Advertencia: no se pudo limpiar mesa de prueba id=" + mesaInsertadaId);
            }
            mesaInsertadaId = null;
        }
    }

    // ── Tests de lectura ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getMesas() debe retornar lista no nula")
    void getMesas_debeRetornarListaNoNula() {
        List<Mesa> mesas = dao.getMesas();

        assertNotNull(mesas, "La lista de mesas no debe ser nula");
    }

    @Test
    @DisplayName("getMesaPorId() con id inválido debe retornar null")
    void getMesaPorId_idInvalido_debeRetornarNull() {
        Mesa resultado = dao.getMesaPorId(-1);
        assertNull(resultado, "id inválido (-1) debe retornar null");
    }

    @Test
    @DisplayName("getMesaPorId() con id inexistente debe retornar null")
    void getMesaPorId_idInexistente_debeRetornarNull() {
        Mesa resultado = dao.getMesaPorId(Integer.MAX_VALUE);
        assertNull(resultado, "id inexistente debe retornar null");
    }

    @Test
    @DisplayName("getMesasPorEstado() debe retornar solo mesas con ese estado")
    void getMesasPorEstado_debeRetornarMesasFiltradas() {
        List<Mesa> mesasLibres = dao.getMesasPorEstado(EstadoMesa.LIBRE);

        assertNotNull(mesasLibres, "La lista filtrada no debe ser nula");
        for (Mesa mesa : mesasLibres) {
            assertEquals(EstadoMesa.LIBRE, mesa.getEstado(),
                    "Todas las mesas retornadas deben estar en estado LIBRE");
        }
    }

    // ── Tests de inserción ────────────────────────────────────────────────────

    @Test
    @DisplayName("nuevaMesa() con mesa válida debe insertar y poder leerse")
    void nuevaMesa_mesaValida_debeInsertarYLeer() {
        Mesa mesa = crearMesaPrueba(NUMERO_MESA_PRUEBA, 4);

        String resultado = dao.nuevaMesa(mesa);

        assertTrue(resultado.toLowerCase().contains("agrego") || resultado.toLowerCase().contains("agregó"),
                "Resultado esperado: confirmación de inserción. Obtenido: " + resultado);

        // Buscar la mesa recién creada por número
        Mesa insertada = buscarMesaPorNumero(NUMERO_MESA_PRUEBA);
        assertNotNull(insertada, "La mesa insertada debe poder encontrarse en la BD");
        assertEquals(NUMERO_MESA_PRUEBA, insertada.getNumero());
        assertEquals(EstadoMesa.LIBRE, insertada.getEstado(),
                "El estado inicial debe ser LIBRE");

        mesaInsertadaId = insertada.getIdMesa(); // para cleanup
    }

    // ── Tests de actualización de estado ─────────────────────────────────────

    @Test
    @DisplayName("cambiarEstado() debe actualizar el estado de la mesa")
    void cambiarEstado_mesaExistente_debeActualizar() {
        // Arrange: insertar mesa de prueba
        Mesa mesa = crearMesaPrueba(NUMERO_MESA_PRUEBA, 4);
        dao.nuevaMesa(mesa);
        Mesa insertada = buscarMesaPorNumero(NUMERO_MESA_PRUEBA);
        assertNotNull(insertada, "La mesa de prueba debe haberse insertado");
        mesaInsertadaId = insertada.getIdMesa();

        // Act: cambiar estado a OCUPADA
        String resultado = dao.cambiarEstado(mesaInsertadaId, EstadoMesa.OCUPADA);

        // Assert
        assertTrue(resultado.toLowerCase().contains("cambio") || resultado.toLowerCase().contains("cambió"),
                "Resultado esperado: confirmación de cambio. Obtenido: " + resultado);

        Mesa actualizada = dao.getMesaPorId(mesaInsertadaId);
        assertNotNull(actualizada);
        assertEquals(EstadoMesa.OCUPADA, actualizada.getEstado(),
                "El estado debe haberse actualizado a OCUPADA");
    }

    @Test
    @DisplayName("cambiarEstado() con id inexistente debe retornar mensaje negativo")
    void cambiarEstado_idInexistente_debeMensajeNegativo() {
        String resultado = dao.cambiarEstado(Integer.MAX_VALUE, EstadoMesa.LIBRE);

        assertFalse(resultado.toLowerCase().contains("cambio") && resultado.toLowerCase().contains("estado"),
                "No debe reportar éxito con id inexistente");
    }

    @Test
    @DisplayName("getMesas() debe retornar mesas con estado válido")
    void getMesas_debeRetornarMesasConEstadoValido() {
        List<Mesa> mesas = dao.getMesas();

        for (Mesa mesa : mesas) {
            assertNotNull(mesa.getEstado(), "El estado de la mesa no debe ser nulo");
            assertNotNull(mesa.getIdMesa(), "El id de la mesa no debe ser nulo");
            assertTrue(mesa.getNumero() > 0, "El número de mesa debe ser positivo");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Mesa crearMesaPrueba(int numero, int capacidad) {
        Mesa mesa = new Mesa();
        mesa.setNumero(numero);
        mesa.setCapacidad(capacidad);
        mesa.setEstado(EstadoMesa.LIBRE);
        return mesa;
    }

    private Mesa buscarMesaPorNumero(int numero) {
        List<Mesa> mesas = dao.getMesas();
        for (Mesa m : mesas) {
            if (m.getNumero() == numero) {
                return m;
            }
        }
        return null;
    }

    /**
     * Limpia la mesa de prueba si quedó de una ejecución anterior.
     */
    private void limpiarMesaPrueba() {
        Mesa existente = buscarMesaPorNumero(NUMERO_MESA_PRUEBA);
        if (existente != null) {
            try (java.sql.Connection conn = DatabaseConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM mesas WHERE id_mesa = ?")) {
                ps.setInt(1, existente.getIdMesa());
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                System.err.println("No se pudo limpiar mesa preexistente: " + e.getMessage());
            }
        }
    }
}
