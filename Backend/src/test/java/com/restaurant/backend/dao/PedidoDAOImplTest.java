package com.restaurant.backend.dao;

import com.restaurant.backend.model.DetallePedido;
import com.restaurant.backend.model.EstadoPedido;
import com.restaurant.backend.model.Mesa;
import com.restaurant.backend.model.Pedido;
import com.restaurant.backend.model.Producto;
import com.restaurant.backend.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para PedidoDAOImpl.
 * Requieren conexión real a la base de datos (TiDB Cloud).
 * Usa mesa_id=1 y usuario_id=1 que se asume existen en la BD de prueba.
 * Todos los pedidos insertados son limpiados con cascade delete via detalle_pedido.
 */
@Tag("integration")
@DisplayName("PedidoDAOImpl — Tests de Integración")
class PedidoDAOImplTest {

    private PedidoDAOImpl dao;
    private Integer pedidoInsertadoId; // tracking para cleanup

    // Asumimos que la mesa 1 y usuario 1 existen en la BD
    private static final int MESA_ID_PRUEBA = 1;
    private static final int USUARIO_ID_PRUEBA = 1;
    private static final int PRODUCTO_ID_PRUEBA = 1; // primer producto disponible

    @BeforeEach
    void setUp() {
        dao = new PedidoDAOImpl();
        pedidoInsertadoId = null;
    }

    @AfterEach
    void tearDown() {
        if (pedidoInsertadoId != null && pedidoInsertadoId > 0) {
            eliminarPedidoPrueba(pedidoInsertadoId);
            pedidoInsertadoId = null;
        }
    }

    // ── Tests de lectura ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getPedidos() debe retornar lista no nula")
    void getPedidos_debeRetornarListaNoNula() {
        List<Pedido> pedidos = dao.getPedidos();

        assertNotNull(pedidos, "La lista de pedidos no debe ser nula");
    }

    @Test
    @DisplayName("getPedidos() debe retornar pedidos con campos básicos no nulos")
    void getPedidos_debeRetornarPedidosConCamposBasicos() {
        List<Pedido> pedidos = dao.getPedidos();

        for (Pedido p : pedidos) {
            assertNotNull(p.getIdPedido(), "El id del pedido no debe ser nulo");
            assertNotNull(p.getEstado(), "El estado del pedido no debe ser nulo");
            assertNotNull(p.getMesa(), "La mesa del pedido no debe ser nula");
        }
    }

    @Test
    @DisplayName("getPedidosPorEstado() debe retornar solo pedidos con ese estado")
    void getPedidosPorEstado_debeRetornarSoloPedidosConEseEstado() {
        List<Pedido> pedidosCerrados = dao.getPedidosPorEstado(EstadoPedido.CERRADO);

        assertNotNull(pedidosCerrados);
        for (Pedido p : pedidosCerrados) {
            assertEquals(EstadoPedido.CERRADO, p.getEstado(),
                    "Todos los pedidos deben estar en estado CERRADO");
        }
    }

    @Test
    @DisplayName("getPedidosPorMesa() debe retornar pedidos de esa mesa")
    void getPedidosPorMesa_debeRetornarPedidosDeMesa() {
        List<Pedido> pedidosMesa = dao.getPedidosPorMesa(MESA_ID_PRUEBA);

        assertNotNull(pedidosMesa, "La lista no debe ser nula");
        for (Pedido p : pedidosMesa) {
            assertEquals(MESA_ID_PRUEBA, p.getMesa().getIdMesa(),
                    "Todos los pedidos deben ser de la mesa especificada");
        }
    }

    // ── Tests de inserción ────────────────────────────────────────────────────

    @Test
    @DisplayName("Insertar() pedido con detalles debe crear pedido y detalles (transacción)")
    void insertar_pedidoConDetalles_debeCrearPedidoYDetalles() {
        // Arrange
        Pedido pedido = crearPedidoPrueba();
        List<DetallePedido> detalles = crearDetallesPrueba(1, new BigDecimal("100.00"));
        pedido.setTotal(new BigDecimal("100.00"));

        // Act
        String resultado = dao.Insertar(pedido, detalles);

        // Assert: inserción exitosa
        assertTrue(resultado.toLowerCase().contains("correctamente"),
                "Resultado esperado: 'correctamente'. Obtenido: " + resultado);

        // Buscar el pedido recién insertado en la lista de pedidos abiertos
        pedidoInsertadoId = buscarUltimoPedidoAbiertoDeMesa(MESA_ID_PRUEBA);
        assertNotNull(pedidoInsertadoId, "Debe haberse creado el pedido en la BD");
        assertTrue(pedidoInsertadoId > 0);
    }

    @Test
    @DisplayName("getPedidoPorId() con id existente debe retornar el pedido")
    void getPedidoPorId_idExistente_debeRetornarPedido() {
        // Primero insertar un pedido de prueba
        Pedido pedido = crearPedidoPrueba();
        List<DetallePedido> detalles = crearDetallesPrueba(2, new BigDecimal("200.00"));
        pedido.setTotal(new BigDecimal("200.00"));
        dao.Insertar(pedido, detalles);
        pedidoInsertadoId = buscarUltimoPedidoAbiertoDeMesa(MESA_ID_PRUEBA);

        assertNotNull(pedidoInsertadoId, "El pedido de prueba debe existir");

        // Act: leer por id
        Pedido leido = dao.getPedidoPorId(pedidoInsertadoId);

        // Assert
        assertNotNull(leido, "El pedido debe encontrarse por ID");
        assertEquals(pedidoInsertadoId, leido.getIdPedido());
        assertNotNull(leido.getEstado());
    }

    // ── Tests de modificación de estado ──────────────────────────────────────

    @Test
    @DisplayName("ModificarEstado() debe actualizar el estado del pedido")
    void modificarEstado_pedidoExistente_debeActualizar() {
        // Arrange: insertar pedido de prueba
        Pedido pedido = crearPedidoPrueba();
        List<DetallePedido> detalles = crearDetallesPrueba(1, new BigDecimal("100.00"));
        pedido.setTotal(new BigDecimal("100.00"));
        dao.Insertar(pedido, detalles);
        pedidoInsertadoId = buscarUltimoPedidoAbiertoDeMesa(MESA_ID_PRUEBA);
        assertNotNull(pedidoInsertadoId);

        // Act: cambiar a EN_COCINA
        String resultado = dao.ModificarEstado(pedidoInsertadoId, EstadoPedido.EN_COCINA);

        // Assert
        assertTrue(resultado.toLowerCase().contains("correctamente"),
                "Resultado esperado: 'correctamente'. Obtenido: " + resultado);

        Pedido actualizado = dao.getPedidoPorId(pedidoInsertadoId);
        assertEquals(EstadoPedido.EN_COCINA, actualizado.getEstado());
    }

    @Test
    @DisplayName("ModificarEstado() con id inexistente debe retornar mensaje negativo")
    void modificarEstado_idInexistente_debeMensajeNegativo() {
        String resultado = dao.ModificarEstado(Integer.MAX_VALUE, EstadoPedido.CERRADO);

        assertFalse(resultado.toLowerCase().contains("correctamente"),
                "No debe reportar éxito con id inexistente");
    }

    // ── Tests de detalles ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getDetallesPedido() debe retornar detalles del pedido insertado")
    void getDetallesPedido_debeRetornarDetalles() {
        // Arrange: insertar pedido con 2 items
        Pedido pedido = crearPedidoPrueba();
        List<DetallePedido> detalles = crearDetallesPrueba(3, new BigDecimal("300.00"));
        pedido.setTotal(new BigDecimal("300.00"));
        dao.Insertar(pedido, detalles);
        pedidoInsertadoId = buscarUltimoPedidoAbiertoDeMesa(MESA_ID_PRUEBA);
        assertNotNull(pedidoInsertadoId);

        // Act
        List<DetallePedido> leidos = dao.getDetallesPedido(pedidoInsertadoId);

        // Assert
        assertNotNull(leidos, "Los detalles no deben ser nulos");
        assertFalse(leidos.isEmpty(), "Debe haber al menos un detalle");
        for (DetallePedido d : leidos) {
            assertNotNull(d.getProducto(), "El producto del detalle no debe ser nulo");
            assertTrue(d.getCantidad() > 0, "La cantidad debe ser positiva");
        }
    }

    @Test
    @DisplayName("insertarDetalle() debe agregar un detalle al pedido existente")
    void insertarDetalle_debeAgregarDetalle() {
        // Arrange
        Pedido pedido = crearPedidoPrueba();
        List<DetallePedido> detalles = crearDetallesPrueba(1, new BigDecimal("100.00"));
        pedido.setTotal(new BigDecimal("100.00"));
        dao.Insertar(pedido, detalles);
        pedidoInsertadoId = buscarUltimoPedidoAbiertoDeMesa(MESA_ID_PRUEBA);
        assertNotNull(pedidoInsertadoId);

        int cantidadInicial = dao.getDetallesPedido(pedidoInsertadoId).size();

        // Act: insertar detalle adicional
        DetallePedido nuevoDetalle = new DetallePedido();
        Producto producto = new Producto();
        producto.setIdProducto(PRODUCTO_ID_PRUEBA);
        nuevoDetalle.setProducto(producto);
        nuevoDetalle.setCantidad(2);
        nuevoDetalle.setPrecioUnitario(new BigDecimal("150.00"));

        String resultado = dao.insertarDetalle(pedidoInsertadoId, nuevoDetalle);

        // Assert
        assertTrue(resultado.toLowerCase().contains("correctamente"),
                "Resultado esperado: 'correctamente'. Obtenido: " + resultado);

        int cantidadFinal = dao.getDetallesPedido(pedidoInsertadoId).size();
        assertEquals(cantidadInicial + 1, cantidadFinal,
                "Debe haber un detalle más que al inicio");
    }

    @Test
    @DisplayName("actualizarTotal() debe actualizar el total del pedido")
    void actualizarTotal_debeActualizarTotal() {
        // Arrange
        Pedido pedido = crearPedidoPrueba();
        List<DetallePedido> detalles = crearDetallesPrueba(1, new BigDecimal("100.00"));
        pedido.setTotal(new BigDecimal("100.00"));
        dao.Insertar(pedido, detalles);
        pedidoInsertadoId = buscarUltimoPedidoAbiertoDeMesa(MESA_ID_PRUEBA);
        assertNotNull(pedidoInsertadoId);

        // Act
        BigDecimal nuevoTotal = new BigDecimal("999.99");
        String resultado = dao.actualizarTotal(pedidoInsertadoId, nuevoTotal);

        // Assert
        assertTrue(resultado.toLowerCase().contains("correctamente"),
                "Resultado esperado: 'correctamente'. Obtenido: " + resultado);

        Pedido actualizado = dao.getPedidoPorId(pedidoInsertadoId);
        assertEquals(0, nuevoTotal.compareTo(actualizado.getTotal()),
                "El total debe haberse actualizado");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Pedido crearPedidoPrueba() {
        Mesa mesa = new Mesa();
        mesa.setIdMesa(MESA_ID_PRUEBA);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(USUARIO_ID_PRUEBA);

        Pedido pedido = new Pedido();
        pedido.setMesa(mesa);
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.ABIERTO);
        pedido.setCreatedAt(LocalDateTime.now());

        return pedido;
    }

    private List<DetallePedido> crearDetallesPrueba(int cantidad, BigDecimal precioUnitario) {
        Producto producto = new Producto();
        producto.setIdProducto(PRODUCTO_ID_PRUEBA);

        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precioUnitario);
        detalle.recalcularSubtotal();

        List<DetallePedido> detalles = new ArrayList<>();
        detalles.add(detalle);
        return detalles;
    }

    /**
     * Busca el ID del último pedido en estado ABIERTO para una mesa dada.
     */
    private Integer buscarUltimoPedidoAbiertoDeMesa(int mesaId) {
        List<Pedido> pedidosMesa = dao.getPedidosPorMesa(mesaId);
        for (int i = pedidosMesa.size() - 1; i >= 0; i--) {
            if (pedidosMesa.get(i).getEstado() == EstadoPedido.ABIERTO) {
                return pedidosMesa.get(i).getIdPedido();
            }
        }
        return null;
    }

    /**
     * Elimina un pedido de prueba (y sus detalles por cascade) directamente vía SQL.
     */
    private void eliminarPedidoPrueba(int pedidoId) {
        try (java.sql.Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (java.sql.PreparedStatement psDetalle = conn.prepareStatement(
                         "DELETE FROM detalle_pedido WHERE id_pedido = ?");
                 java.sql.PreparedStatement psPedido = conn.prepareStatement(
                         "DELETE FROM pedidos WHERE id_pedido = ?")) {

                psDetalle.setInt(1, pedidoId);
                psDetalle.executeUpdate();

                psPedido.setInt(1, pedidoId);
                psPedido.executeUpdate();

                conn.commit();
            } catch (java.sql.SQLException ex) {
                conn.rollback();
                System.err.println("Error en cleanup de pedido " + pedidoId + ": " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al limpiar pedido de prueba: " + e.getMessage());
        }
    }
}
