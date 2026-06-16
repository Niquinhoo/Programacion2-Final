package com.restaurant.backend.controller;

import java.util.List;

import com.restaurant.backend.dao.CategoriaDAO;
import com.restaurant.backend.dao.CategoriaDAOImpl;
import com.restaurant.backend.model.Categoria;

public class CategoriaController {

    private final CategoriaDAO categoriaDAO = new CategoriaDAOImpl();

    public List<Categoria> listar() {
        return categoriaDAO.getCategorias();
    }
}

