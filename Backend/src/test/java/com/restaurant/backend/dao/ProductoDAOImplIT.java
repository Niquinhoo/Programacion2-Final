package com.restaurant.backend.dao;

import com.restaurant.backend.model.Categoria;
import com.restaurant.backend.model.Producto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.restaurant.backend.support.DedicatedDatabaseExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para ProductoDAOImpl.
 * Requieren conexión real a la base de datos (TiDB Cloud).
 * Cada test que inserta datos realiza cleanup garantizado en @AfterEach.
 */
@Tag("integration")
@DisplayName("ProductoDAOImpl — Tests de Integración")
@ExtendWith(DedicatedDatabaseExtension.class)
class ProductoDAOImplIT {

    private ProductoDAOImpl dao;
    private Integer productoInsertadoId; // tracking para cleanup

    @BeforeEach
    void setUp() {
        dao = new ProductoDAOImpl();
        productoInsertadoId = null;
    }

    @AfterEach
    void tearDown() {
        // Cleanup: eliminar producto de prueba si fue insertado
        if (productoInsertadoId != null && productoInsertadoId > 0) {
            dao.eliminar(productoInsertadoId);
            productoInsertadoId = null;
        }
    }

    // ── Tests de lectura ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getProductos() debe retornar lista no nula")
    void getProductos_debeRetornarListaNoNula() {
        List<Producto> productos = dao.getProductos();

        assertNotNull(productos, "La lista de productos no debe ser nula");
    }

    @Test
    @DisplayName("getProductos() debe incluir el JOIN con categorías")
    void getProductos_debeIncluirCategoria() {
        List<Producto> productos = dao.getProductos();

        if (!productos.isEmpty()) {
            Producto primero = productos.get(0);
            assertNotNull(primero.getCategoria(),
                    "Cada producto debe tener su categoría cargada (JOIN)");
            assertNotNull(primero.getCategoria().getNombre(),
                    "El nombre de la categoría no debe ser nulo");
        }
    }

    @Test
    @DisplayName("getProductoPorId() con id inválido debe retornar null")
    void getProductoPorId_idInvalido_debeRetornarNull() {
        Producto resultado = dao.getProductoPorId(-1);
        assertNull(resultado, "id inválido (-1) debe retornar null");

        resultado = dao.getProductoPorId(0);
        assertNull(resultado, "id cero debe retornar null");
    }

    @Test
    @DisplayName("getProductoPorId() con id inexistente debe retornar null")
    void getProductoPorId_idInexistente_debeRetornarNull() {
        Producto resultado = dao.getProductoPorId(Integer.MAX_VALUE);
        assertNull(resultado, "id inexistente debe retornar null");
    }

    // ── Tests de inserción ────────────────────────────────────────────────────

    @Test
    @DisplayName("insertar() producto válido debe retornar mensaje de éxito y asignar ID")
    void insertar_productoValido_debeRetornarExitoYAsignarId() {
        Producto producto = crearProductoPrueba("TestProducto_Insertar");

        String resultado = dao.insertar(producto);

        assertTrue(resultado.toLowerCase().contains("correctamente"),
                "Resultado esperado: 'correctamente'. Obtenido: " + resultado);
        assertNotNull(producto.getIdProducto(),
                "El ID debe ser asignado luego del INSERT");
        assertTrue(producto.getIdProducto() > 0,
                "El ID asignado debe ser positivo");

        productoInsertadoId = producto.getIdProducto(); // para cleanup
    }

    @Test
    @DisplayName("insertar() debe poder leerse con getProductoPorId()")
    void insertar_productoValido_debePoderseLeerPorId() {
        Producto producto = crearProductoPrueba("TestProducto_LeerPorId");

        dao.insertar(producto);
        productoInsertadoId = producto.getIdProducto();

        assertNotNull(productoInsertadoId, "El producto debe haberse insertado");

        Producto leido = dao.getProductoPorId(productoInsertadoId);

        assertNotNull(leido, "El producto insertado debe encontrarse por ID");
        assertEquals("TestProducto_LeerPorId", leido.getNombre(),
                "El nombre del producto debe coincidir");
        assertEquals(0, new BigDecimal("150.00").compareTo(leido.getPrecio()),
                "El precio del producto debe coincidir");
    }

    @Test
    @DisplayName("insertar() con producto nulo debe retornar mensaje de error")
    void insertar_productoNulo_debeRetornarError() {
        String resultado = dao.insertar((Producto) null);
        assertNotNull(resultado);
        assertFalse(resultado.toLowerCase().contains("correctamente"),
                "No debe reportar éxito con producto nulo");
    }

    @Test
    @DisplayName("insertar() sin parámetro debe retornar mensaje indicativo")
    void insertar_sinParametro_debeRetornarMensajeIndicativo() {
        String resultado = dao.insertar();
        assertNotNull(resultado);
        assertFalse(resultado.isBlank());
    }

    // ── Tests de actualización ────────────────────────────────────────────────

    @Test
    @DisplayName("editar() producto existente debe actualizar nombre y precio")
    void editar_productoExistente_debeActualizar() {
        // Arrange: insertar producto de prueba
        Producto producto = crearProductoPrueba("TestProducto_Editar_Original");
        dao.insertar(producto);
        productoInsertadoId = producto.getIdProducto();
        assertNotNull(productoInsertadoId);

        // Act: modificar nombre y precio
        producto.setNombre("TestProducto_Editar_Modificado");
        producto.setPrecio(new BigDecimal("999.99"));
        String resultado = dao.editar(producto);

        // Assert
        assertTrue(resultado.toLowerCase().contains("correctamente"),
                "Resultado esperado: 'correctamente'. Obtenido: " + resultado);

        Producto leido = dao.getProductoPorId(productoInsertadoId);
        assertNotNull(leido);
        assertEquals("TestProducto_Editar_Modificado", leido.getNombre());
        assertEquals(0, new BigDecimal("999.99").compareTo(leido.getPrecio()));
    }

    @Test
    @DisplayName("editar() sin parámetro debe retornar mensaje indicativo")
    void editar_sinParametro_debeRetornarMensajeIndicativo() {
        String resultado = dao.editar();
        assertNotNull(resultado);
        assertFalse(resultado.isBlank());
    }

    @Test
    @DisplayName("editar() con id inexistente debe retornar mensaje negativo")
    void editar_idInexistente_debeRetornarMensajeNegativo() {
        Producto fantasma = crearProductoPrueba("Fantasma");
        fantasma.setIdProducto(Integer.MAX_VALUE);

        String resultado = dao.editar(fantasma);

        assertFalse(resultado.toLowerCase().contains("correctamente"),
                "No debe reportar éxito con id inexistente");
    }

    // ── Tests de eliminación ──────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar() id existente debe borrar el producto")
    void eliminar_idExistente_debeBorrar() {
        // Arrange: insertar producto de prueba
        Producto producto = crearProductoPrueba("TestProducto_Eliminar");
        dao.insertar(producto);
        int idParaBorrar = producto.getIdProducto();
        assertNotNull(idParaBorrar);

        // Act: eliminar
        String resultado = dao.eliminar(idParaBorrar);

        // Assert: resultado exitoso y ya no se puede leer
        assertTrue(resultado.toLowerCase().contains("correctamente"),
                "Resultado esperado: 'correctamente'. Obtenido: " + resultado);
        assertNull(dao.getProductoPorId(idParaBorrar),
                "El producto eliminado no debe encontrarse");

        productoInsertadoId = null; // ya fue eliminado, no necesita cleanup
    }

    @Test
    @DisplayName("eliminar() id inexistente debe retornar mensaje negativo")
    void eliminar_idInexistente_debeMensajeNegativo() {
        String resultado = dao.eliminar(Integer.MAX_VALUE);

        assertFalse(resultado.toLowerCase().contains("correctamente"),
                "No debe reportar éxito al eliminar id inexistente");
    }

    @Test
    @DisplayName("eliminar() sin parámetro debe retornar mensaje indicativo")
    void eliminar_sinParametro_debeRetornarMensajeIndicativo() {
        String resultado = dao.eliminar();
        assertNotNull(resultado);
        assertFalse(resultado.isBlank());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Crea un Producto de prueba con datos válidos.
     * Usa la categoría con id=1 (debe existir en la base de datos).
     */
    private Producto crearProductoPrueba(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1); // Asume que la categoría 1 existe en la BD de prueba

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion("Producto de prueba creado por test automatizado");
        producto.setPrecio(new BigDecimal("150.00"));
        producto.setStock(10);
        producto.setCategoria(categoria);
        producto.setDisponible(true);

        return producto;
    }
}
