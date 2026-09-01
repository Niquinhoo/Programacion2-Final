package com.restaurant.backend.controller;

import java.util.List;

import com.restaurant.backend.dao.CategoriaDAO;
import com.restaurant.backend.dao.CategoriaDAOImpl;
import com.restaurant.backend.model.Categoria;

public class CategoriaController {

    private final CategoriaDAO categoriaDAO;

    public CategoriaController() {
        this(new CategoriaDAOImpl());
    }

    CategoriaController(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public List<Categoria> listar() {
        return categoriaDAO.getCategorias();
    }
}

