package com.restaurant.backend.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.restaurant.backend.model.Categoria;
import com.restaurant.backend.support.DedicatedDatabaseExtension;

@Tag("integration")
@DisplayName("CategoriaDAOImpl — Tests de Integración")
@ExtendWith(DedicatedDatabaseExtension.class)
class CategoriaDAOImplIT {

    @Test
    void getCategoriasDevuelveCategoriasActivasCompletas() {
        List<Categoria> categorias = new CategoriaDAOImpl().getCategorias();

        assertFalse(categorias.isEmpty());
        assertTrue(categorias.stream().allMatch(Categoria::isActiva));
        assertTrue(categorias.stream().allMatch(c -> c.getIdCategoria() != null && c.getNombre() != null));
    }
}
