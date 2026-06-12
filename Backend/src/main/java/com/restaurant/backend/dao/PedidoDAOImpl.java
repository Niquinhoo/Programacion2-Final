package com.restaurant.backend.dao;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

import com.restaurant.backend.model.DetallePedido;
import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.EstadoPedido;
import com.restaurant.backend.model.Mesa;
import com.restaurant.backend.model.Pedido;
import com.restaurant.backend.model.Producto;


public class PedidoDAOImpl implements PedidoDAO {


  @Override
  public List<Pedido> getPedidos() {

    List<Pedido> lista = new ArrayList<>();
    String query = 
            """
            SELECT 
                p.id AS pedido_id,
                p.mesa_id AS pedido_mesa_id,
                p.fecha,
                p.total,
                p.estado AS estado_pedido,

                m.id AS mesa_id,
                m.numero,
                m.estado AS estado_mesa
            FROM pedido p
            JOIN mesa m ON p.mesa_id = m.id;
            """;

    try(
      Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet result = ps.executeQuery();) {
      

      while (result.next()) {
        // ----Mesa----
        Mesa m = new Mesa();
        m.setIdMesa(result.getInt("mesa_id"));
        m.setNumero(result.getInt("numero"));
        
        String estadoMesaoDb = result.getString("estado_mesa");
        m.setEstado(EstadoMesa.valueOf(estadoMesaoDb));
        
        //----Pedido----
        Pedido p = new Pedido();
        p.setIdPedido(result.getInt("pedido_id"));
        p.setMesa(m);
        p.setCreatedAt(result.getTimestamp("fecha").toLocalDateTime());
        p.setTotal(result.getBigDecimal("total"));
        
        String estadoPeidoDb = result.getString("estado_pedido");
        p.setEstado(EstadoPedido.valueOf(estadoPeidoDb));

        lista.add(p);
      }
      
      return lista;

    } catch (SQLException e) {
      System.out.println("Error" + e.getMessage());
      return lista;
    }
  }
  
    @Override
    public String Insertar(Pedido p,List<DetallePedido> detalles) {
      String queryPedido = "INSERT INTO pedido(mesa_id,fecha,total,estado) VALUES(?,?,?,?)";
      String queryDetalles = "INSERT INTO detalle_pedido(pedido_id,producto_id,cantidad,precio_unitario) VALUES(?,?,?,?)";
      Connection conn = null;

      try {

        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false); // desactiva el insert automatico

        // ---INSERT PEDIDO---
        PreparedStatement ps = conn.prepareStatement(queryPedido,Statement.RETURN_GENERATED_KEYS); // Retorna el id de pedido

        ps.setInt(1, p.getMesa().getIdMesa());
        ps.setTimestamp(2, java.sql.Timestamp.valueOf(p.getCreatedAt()));
        ps.setBigDecimal(3, p.getTotal());
        ps.setString(4, EstadoPedido.ABIERTO.toString());

        int filas = ps.executeUpdate(); // Obtiene el numero de filas que cambiaron 
        
        if(filas == 0){
          conn.rollback();
          return "No se pudo insertar el pedido";
        }
        
        // ---OBETENER ID PEDIDO---
        ResultSet rs = ps.getGeneratedKeys(); 
        int pedidoId = -1;
        if(rs.next()){
          pedidoId = rs.getInt(1);
        }

        // ---INSERT DETALLES---
        PreparedStatement psDetalles = conn.prepareStatement(queryDetalles);
        for(DetallePedido d : detalles){
          psDetalles.setInt(1,pedidoId);
          psDetalles.setInt(2,d.getProducto().getIdProducto());
          psDetalles.setInt(3,d.getCantidad());
          psDetalles.setBigDecimal(4, d.getPrecioUnitario());

          psDetalles.addBatch(); // Guarda cada insert en una cola 

        }

        psDetalles.executeBatch(); // ejecuta todos los insert juntos 

        // si sale bien se guarda 
        conn.commit();
        return "Pedido y detalles insertados correctamente";

        
      } catch (SQLException ex) {
        // si sale mal, se deshace todo
        try {

          if (conn != null) conn.rollback();

        } catch (SQLException e) {

          System.out.println("Error en rollback: " + e);

        }

        System.out.println("Error: " + ex);
        return "Error al insertar pedido";

      }finally{
        // restaura y cierra conexion
        try {

          if (conn != null) conn.setAutoCommit(true); // vuelte a poner los inset automaticos 
          if (conn != null) conn.close(); 

        } catch (SQLException e) {

          System.out.println("Error cerrando conexión: " +  e.getMessage());

        }
      }
    }



  @Override
  public List<Pedido> getPedidosPorEstado (EstadoPedido estado) {
    List<Pedido> list = new ArrayList<Pedido>();
    String query = """
            SELECT p.id,
                  p.fecha,
                  p.estado AS estado_pedido,
                  m.id AS mesa_id,
                  m.numero,
                  m.estado AS estado_mesa
            FROM pedido p
            INNER JOIN mesa m ON p.mesa_id = m.id
            WHERE p.estado = ?;
                  """;
    try( 
      Connection conn  = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);
      ) {
     
      ps.setString(1, estado.toString());
      
      try (ResultSet resp = ps.executeQuery()) {
        while(resp.next()){
          Mesa m = new Mesa();
          m.setIdMesa(resp.getInt("mesa_id"));
          m.setNumero(resp.getInt("numero"));
          m.setEstado(EstadoMesa.valueOf(resp.getString("estado_mesa")));

          Pedido p = new Pedido();
          p.setIdPedido(resp.getInt("id"));
          p.setCreatedAt(resp.getTimestamp("fecha").toLocalDateTime());
          p.setEstado(EstadoPedido.valueOf(resp.getString("estado_pedido")));
          p.setMesa(m);

          list.add(p);

        }
      }
      
    }catch (SQLException e) {
      System.out.println("Error cerrando conexión: " + e.getMessage());
    }

    return list;
  }



  
  @Override
  public String ModificarEstado(int id,EstadoPedido estado) {
    String query = "UPDATE pedido SET estado = ? WHERE id = ?";

    try(
      Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);) {
      
      
      ps.setString(1, estado.toString());
      ps.setInt(2,id);

      int filasAfectadas = ps.executeUpdate();
      if(filasAfectadas > 0) return "Estado actualizado correctamente";
      
      return "No se encontró el pedido";

    } catch (SQLException e) {
      return "Error: " + e.getMessage();
    }
    
  }

  @Override
  public List<DetallePedido> getDetallesPedido(int pedidoId) {
    String query = """
                  SELECT
                    dp.id,
                    dp.pedido_id,
                    dp.producto_id,
                    dp.cantidad,
                    dp.precio_unitario,
                    p.nombre
                FROM detalle_pedido dp
                INNER JOIN producto p ON dp.producto_id = p.id
                WHERE dp.pedido_id = ?;
                  """; 
    List<DetallePedido> listDetalles = new ArrayList<DetallePedido>();
    try ( Connection conn = DatabaseConnection.getConnection();
          PreparedStatement ps = conn.prepareStatement(query);
        ) {
          
          ps.setInt(1, pedidoId);
          
          try (ResultSet result = ps.executeQuery();) {
            
            while(result.next()){
              Pedido pedido = new Pedido();
              pedido.setIdPedido(result.getInt("pedido_id"));

              Producto producto = new Producto();
              producto.setIdProducto(result.getInt("producto_id"));
              producto.setNombre(result.getString("nombre"));

              DetallePedido dp = new DetallePedido();
              dp.setIdDetalle(result.getInt("id"));
              dp.setPedido(pedido);
              dp.setProducto(producto);
              dp.setCantidad(result.getInt("cantidad"));
              dp.setPrecioUnitario(result.getBigDecimal("precio_unitario"));

              listDetalles.add(dp);
              
            }
          } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
          }



    } catch (SQLException e) {
      System.err.println("Error: " + e.getMessage());
    }
    return listDetalles;
  }




  
  @Override
  public Pedido getPedidoPorId(int id) {
    Pedido p = new Pedido();
    String query =  """
                      SELECT
                          p.id,
                          p.fecha,
                          p.total,
                          p.estado,

                          m.id AS mesa_id,
                          m.numero
                      FROM pedido p
                      INNER JOIN mesa m ON p.mesa_id = m.id
                      WHERE p.id = ?;
                    """;;

    try (Connection
          conn = DatabaseConnection.getConnection();
          PreparedStatement ps = conn.prepareStatement(query)
        ) {

       ps.setInt(1, id);

       try (ResultSet result = ps.executeQuery()) {
        if(result.next()){
          
          Mesa mesa = new Mesa();
          mesa.setIdMesa(result.getInt("mesa_id"));
          mesa.setNumero(result.getInt("numero"));
  
          p.setIdPedido(result.getInt("id"));
          p.setCreatedAt(result.getTimestamp("fecha").toLocalDateTime());
          p.setEstado(EstadoPedido.valueOf(result.getString("estado")));
          p.setTotal(result.getBigDecimal("total"));
          p.setMesa(mesa);
        }

        

       } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
       }


    } catch (SQLException e) {
      System.err.println("Error: " + e.getMessage());
    }
    return p;
  }



  @Override
  public List<Pedido> getPedidosPorMesa(int mesaId) {
    String query = """
              SELECT
                p.id,
                p.fecha,
                p.total,
                p.estado,
                m.id AS mesa_id,
                m.numero
            FROM pedido p
            INNER JOIN mesa m ON p.mesa_id = m.id
            WHERE p.mesa_id = ?;
                  """;; 

    List<Pedido> listPedidos = new ArrayList<Pedido>();
    try (
      Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query)
    ) {
        ps.setInt(1, mesaId);
        try (ResultSet result = ps.executeQuery()) {

          while (result.next()) {
            Mesa mesa = new Mesa();
            mesa.setIdMesa(result.getInt("mesa_id"));
            mesa.setNumero(result.getInt("numero"));


            Pedido p = new Pedido();
            p.setIdPedido(result.getInt("id"));
            p.setMesa(mesa);
            p.setCreatedAt(result.getTimestamp("fecha").toLocalDateTime());
            p.setTotal(result.getBigDecimal("total"));
            p.setEstado(EstadoPedido.valueOf(result.getString("estado")));

            listPedidos.add(p);
          }

      } catch (SQLException e) {
         System.err.println("Error: " + e.getMessage());
      }  
    } catch (SQLException e) {
       System.err.println("Error: " + e.getMessage());
    }

    return listPedidos;
  } 




}