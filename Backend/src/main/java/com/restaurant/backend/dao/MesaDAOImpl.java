package com.restaurant.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.Mesa;

public class MesaDAOImpl implements MesaDAO {

  
    @Override
    public String nuevaMesa(Mesa mesa) {
      String query = "INSERT INTO mesa(numero,estado) VALUES(?,?)";
      try(
      Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);) {

      
        ps.setInt(1, mesa.getNumero());
        ps.setString(2,mesa.getEstado().toString());

        int filasAfectadas = ps.executeUpdate();
        if(filasAfectadas > 0) return "Se agrego la mesa ";
        return "No se agrego la mesa";
        
        
      } catch (SQLException e) {
        System.out.println("Error: "+ e.getMessage());
        return "Error al agregar mesa";
      }

    }
    

  @Override
  public String borrarMesa(int id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'borrarMesa'");
  }


  @Override
  public String cambiarEstado(int id,EstadoMesa estado) {
    String query = "UPDATE mesa SET estado = ? WHERE id = ? ";
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
  public List<Mesa> getMesas() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getMesas'");
  }


  @Override
  public Mesa getMesaPorId(int id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getMesaPorId'");
  }
}
