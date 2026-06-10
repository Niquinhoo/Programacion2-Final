package com.restaurant.backend.dao;

import com.restaurant.backend.model.Producto;
import java.util.List;

public interface ProductoDAO {

  public List<Producto> getProductos();
  public Producto FiltrarPorCategoria(String categoria);
  
  // Crud
  public String insertar();
  public String eliminar();
  public String editar();
  public Producto getProductoPorId(int id);
}
