package com.restaurant.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.Mesa;

public class MesaDAOImpl implements MesaDAO {

  
    @Override
    public String nuevaMesa(Mesa mesa) {
      String query = "INSERT INTO mesas(numero, capacidad, estado) VALUES(?, ?, ?)";
      try(
      Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);) {

      
        ps.setInt(1, mesa.getNumero());
        ps.setInt(2, mesa.getCapacidad());
        ps.setString(3, mesa.getEstado().toString());

        int filasAfectadas = ps.executeUpdate();
        if(filasAfectadas > 0) return "Se agrego la mesa ";
        return "No se agrego la mesa";
        
        
      } catch (SQLException e) {
        System.out.println("Error: "+ e.getMessage());
        return "Error al agregar mesa";
      }

    }
    

    
    
    @Override
    public String cambiarEstado(int id,EstadoMesa estado) {
      String query = "UPDATE mesas SET estado = ? WHERE id_mesa = ? ";
      try(Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);) {
        
        
        ps.setString(1, estado.toString());
        ps.setInt(2, id);
        
        int filasAfectadas = ps.executeUpdate();
        if(filasAfectadas > 0) return "Se cambio el estado";
        return "No se encontro la mesa";
        
      } catch (SQLException e) {
        System.out.println("Error: "+ e);
        return "Error al editar estado: " + e;
      }
    
    }
    
    @Override
    public String borrarMesa(int id) {
      String query = "DELETE FROM mesas WHERE id_mesa = ?";

      try(Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);) {
        
        ps.setInt(1, id);

        int filasAfectadas = ps.executeUpdate();
        if(filasAfectadas > 0) return "Se borro la mesa";
        return "No se encontro la mesa";

      } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
        return "Error al borrar mesa";
      }
    }
  
  @Override
  public List<Mesa> getMesas() {
    List<Mesa> lista = new ArrayList<>();
    String query = "SELECT id_mesa, numero, capacidad, estado FROM mesas ORDER BY numero";

    try(Connection conn = DatabaseConnection.getConnection();
    PreparedStatement ps = conn.prepareStatement(query);
    ResultSet result = ps.executeQuery();) {

      while(result.next()){
        lista.add(mapearMesa(result));
      }

    } catch (SQLException e) {
      System.out.println("Error: " + e.getMessage());
    }

    return lista;
  }
  
  
  @Override
  public Mesa getMesaPorId(int id) {
    String query = "SELECT id_mesa, numero, capacidad, estado FROM mesas WHERE id_mesa = ?";

    try(Connection conn = DatabaseConnection.getConnection();
    PreparedStatement ps = conn.prepareStatement(query);) {
      
      ps.setInt(1, id);

      try(ResultSet result = ps.executeQuery();) {
        if(result.next()){
          return mapearMesa(result);
        }
      }

    } catch (SQLException e) {
      System.out.println("Error: " + e.getMessage());
    }

    return null;
  }

  private Mesa mapearMesa(ResultSet result) throws SQLException {
    Mesa mesa = new Mesa();
    mesa.setIdMesa(result.getInt("id_mesa"));
    mesa.setNumero(result.getInt("numero"));
    mesa.setCapacidad(result.getInt("capacidad"));
    mesa.setEstado(EstadoMesa.valueOf(result.getString("estado")));
    return mesa;
  }
}
