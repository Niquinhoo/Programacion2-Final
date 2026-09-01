package com.restaurant.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurant.backend.dao.ProductoDAO;
import com.restaurant.backend.model.Categoria;
import com.restaurant.backend.model.Producto;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService — pruebas unitarias")
class ProductoServiceTest {

    @Mock
    private ProductoDAO dao;
    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoService(dao);
    }

    @Test
    void categoriaVaciaDevuelveListaVaciaSinConsultarDao() {
        assertTrue(service.obtenerPorCategoria("  ").isEmpty());
        verifyNoInteractions(dao);
    }

    @Test
    void filtraCategoriaSinDistinguirMayusculasYSoloDisponibles() {
        Producto disponible = producto(1, "Clásica", "Hamburguesas", 5, true);
        Producto noDisponible = producto(2, "Veggie", "Hamburguesas", 5, false);
        Producto otraCategoria = producto(3, "Agua", "Bebidas", 5, true);
        when(dao.getProductos()).thenReturn(List.of(disponible, noDisponible, otraCategoria));

        List<Producto> resultado = service.obtenerPorCategoria("  HAMBURGUESAS ");

        assertEquals(List.of(disponible), resultado);
    }

    @Test
    void crearRechazaProductoInvalidoSinPersistir() {
        Producto producto = producto(null, " ", "Hamburguesas", 1, true);

        assertEquals("El nombre del producto es obligatorio", service.crear(producto));
        verify(dao, never()).insertar(any());
    }

    @Test
    void actualizarExigeIdValido() {
        Producto producto = producto(null, "Clásica", "Hamburguesas", 1, true);

        assertEquals("El id del producto debe ser mayor a cero", service.actualizar(producto));
        verify(dao, never()).editar(any());
    }

    @Test
    void crearValidoDelegaEnDao() {
        Producto producto = producto(null, "Clásica", "Hamburguesas", 1, true);
        when(dao.insertar(producto)).thenReturn("Producto insertado correctamente");

        assertEquals("Producto insertado correctamente", service.crear(producto));
        verify(dao).insertar(producto);
    }

    @Test
    void validarStockDetectaProductoNoDisponible() {
        Producto producto = producto(1, "Clásica", "Hamburguesas", 5, false);
        when(dao.getProductoPorId(1)).thenReturn(producto);

        assertEquals("El producto Clásica no esta disponible", service.validarStockDisponible(1, 1));
    }

    @Test
    void validarStockDetectaFaltante() {
        Producto producto = producto(1, "Clásica", "Hamburguesas", 2, true);
        when(dao.getProductoPorId(1)).thenReturn(producto);

        assertEquals("Stock insuficiente para el producto Clásica", service.validarStockDisponible(1, 3));
    }

    @Test
    void descontarStockActualizaCantidadExacta() {
        Producto producto = producto(1, "Clásica", "Hamburguesas", 5, true);
        when(dao.getProductoPorId(1)).thenReturn(producto);
        when(dao.editar(producto)).thenReturn("Producto editado correctamente");

        assertEquals("Producto editado correctamente", service.descontarStock(1, 2));
        assertEquals(3, producto.getStock());
        verify(dao).editar(producto);
    }

    @Test
    void idsYCantidadesInvalidasNoConsultanDao() {
        assertNull(service.obtenerPorId(0));
        assertEquals("El id del producto debe ser mayor a cero", service.eliminar(0));
        assertEquals("La cantidad a descontar debe ser mayor a cero", service.descontarStock(1, 0));
        verifyNoInteractions(dao);
    }

    private Producto producto(Integer id, String nombre, String categoriaNombre, int stock, boolean disponible) {
        Categoria categoria = new Categoria(1, categoriaNombre, null, true);
        return new Producto(id, nombre, null, new BigDecimal("100.00"), stock, categoria, disponible);
    }
}
