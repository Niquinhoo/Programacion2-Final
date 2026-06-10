package com.restaurant.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.restaurant.backend.model.Producto;

import java.sql.SQLException;

public class ProductoDAOImpl implements ProductoDAO {
  
  @Override
  public List<Producto> getProductos(){

    List<Producto> lista = new ArrayList<>();
    String query = "SELECT * FROM producto";

    try {

      Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query); // Prepara la consulta 
      ResultSet respuesta = ps.executeQuery(); // Ejecuta 

      while (respuesta.next()) {

        Producto p = new Producto();
        p.setIdProducto(respuesta.getInt("id"));
        p.setNombre(respuesta.getString("nombre"));
        p.setDescripcion(respuesta.getString("descripcion"));
        p.setPrecio(respuesta.getBigDecimal("precio"));
        p.setCategoria(null);

        lista.add(p);
      }

      ps.close();
      conn.close();

    } catch (SQLException ex) {
      System.out.println("Error" + ex);
    }
  
  return lista;
}

  @Override
  public Producto FiltrarPorCategoria(String categoria) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'FiltrarPorCategoria'");
  }

  @Override
  public String insertar() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'insertar'");
  }

  @Override
  public String eliminar() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'eliminar'");
  }

  @Override
  public String editar() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'editar'");
  }

  @Override
  public Producto getProductoPorId(int id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getProductoPorId'");
  }
}

