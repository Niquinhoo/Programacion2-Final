package com.restaurant.backend.dao;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                p.id_pedido,
                p.id_mesa AS pedido_mesa_id,
                p.created_at AS fecha,
                p.total,
                p.estado AS estado_pedido,

                m.id_mesa,
                m.numero,
                m.estado AS estado_mesa
            FROM pedidos p
            JOIN mesas m ON p.id_mesa = m.id_mesa;
            """;

    try(
      Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet result = ps.executeQuery();) {
      

      while (result.next()) {
        Mesa m = new Mesa();
        m.setIdMesa(result.getInt("id_mesa"));
        m.setNumero(result.getInt("numero"));
        
        String estadoMesaoDb = result.getString("estado_mesa");
        m.setEstado(EstadoMesa.valueOf(estadoMesaoDb));
        
        Pedido p = new Pedido();
        p.setIdPedido(result.getInt("id_pedido"));
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
      String queryPedido = "INSERT INTO pedidos(id_mesa,id_usuario,created_at,total,estado) VALUES(?,?,?,?,?)";
      String queryDetalles = "INSERT INTO detalle_pedido(id_pedido,id_producto,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)";
      Connection conn = null;

      try {

        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);

        PreparedStatement ps = conn.prepareStatement(queryPedido,Statement.RETURN_GENERATED_KEYS);

        ps.setInt(1, p.getMesa().getIdMesa());
        ps.setInt(2, p.getUsuario().getIdUsuario());
        ps.setTimestamp(3, java.sql.Timestamp.valueOf(p.getCreatedAt()));
        ps.setBigDecimal(4, p.getTotal());
        ps.setString(5, EstadoPedido.ABIERTO.toString());

        int filas = ps.executeUpdate();
        
        if(filas == 0){
          conn.rollback();
          return "No se pudo insertar el pedido";
        }
        
        ResultSet rs = ps.getGeneratedKeys(); 
        int pedidoId = -1;
        if(rs.next()){
          pedidoId = rs.getInt(1);
        }

        PreparedStatement psDetalles = conn.prepareStatement(queryDetalles);
        for(DetallePedido d : detalles){
          psDetalles.setInt(1,pedidoId);
          psDetalles.setInt(2,d.getProducto().getIdProducto());
          psDetalles.setInt(3,d.getCantidad());
          psDetalles.setBigDecimal(4, d.getPrecioUnitario());
          psDetalles.setBigDecimal(5, d.getSubtotal());

          psDetalles.addBatch();

        }

        psDetalles.executeBatch();

        conn.commit();
        return "Pedido y detalles insertados correctamente";

        
      } catch (SQLException ex) {
        try {

          if (conn != null) conn.rollback();

        } catch (SQLException e) {

          System.out.println("Error en rollback: " + e);

        }

        System.out.println("Error: " + ex);
        return "Error al insertar pedido";

      }finally{
        try {

          if (conn != null) conn.setAutoCommit(true);
          if (conn != null) conn.close(); 

        } catch (SQLException e) {

          System.out.println("Error cerrando conexi\u00f3n: " +  e.getMessage());

        }
      }
    }



  @Override
  public List<Pedido> getPedidosPorEstado (EstadoPedido estado) {
    List<Pedido> list = new ArrayList<Pedido>();
    String query = """
            SELECT p.id_pedido,
                  p.created_at AS fecha,
                  p.total,
                  p.estado AS estado_pedido,
                  m.id_mesa,
                  m.numero,
                  m.estado AS estado_mesa
            FROM pedidos p
            INNER JOIN mesas m ON p.id_mesa = m.id_mesa
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
          m.setIdMesa(resp.getInt("id_mesa"));
          m.setNumero(resp.getInt("numero"));
          m.setEstado(EstadoMesa.valueOf(resp.getString("estado_mesa")));

          Pedido p = new Pedido();
          p.setIdPedido(resp.getInt("id_pedido"));
          p.setCreatedAt(resp.getTimestamp("fecha").toLocalDateTime());
          p.setTotal(resp.getBigDecimal("total"));
          p.setEstado(EstadoPedido.valueOf(resp.getString("estado_pedido")));
          p.setMesa(m);

          list.add(p);

        }
      }
      
    }catch (SQLException e) {
      System.out.println("Error cerrando conexi\u00f3n: " + e.getMessage());
    }

    return list;
  }



  
  @Override
  public String ModificarEstado(int id,EstadoPedido estado) {
    String query = "UPDATE pedidos SET estado = ? WHERE id_pedido = ?";

    try(
      Connection conn = DatabaseConnection.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);) {
      
      
      ps.setString(1, estado.toString());
      ps.setInt(2,id);

      int filasAfectadas = ps.executeUpdate();
      if(filasAfectadas > 0) return "Estado actualizado correctamente";
      
      return "No se encontr\u00f3 el pedido";

    } catch (SQLException e) {
      return "Error: " + e.getMessage();
    }
    
  }

  @Override
  public List<DetallePedido> getDetallesPedido(int pedidoId) {
    String query = """
                  SELECT
                    dp.id_detalle,
                    dp.id_pedido,
                    dp.id_producto,
                    dp.cantidad,
                    dp.precio_unitario,
                    p.nombre
                FROM detalle_pedido dp
                INNER JOIN productos p ON dp.id_producto = p.id_producto
                WHERE dp.id_pedido = ?;
                  """; 
    List<DetallePedido> listDetalles = new ArrayList<DetallePedido>();
    try ( Connection conn = DatabaseConnection.getConnection();
          PreparedStatement ps = conn.prepareStatement(query);
        ) {
          
          ps.setInt(1, pedidoId);
          
          try (ResultSet result = ps.executeQuery();) {
            
            while(result.next()){
              Pedido pedido = new Pedido();
              pedido.setIdPedido(result.getInt("id_pedido"));

              Producto producto = new Producto();
              producto.setIdProducto(result.getInt("id_producto"));
              producto.setNombre(result.getString("nombre"));

              DetallePedido dp = new DetallePedido();
              dp.setIdDetalle(result.getInt("id_detalle"));
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
                          p.id_pedido,
                          p.created_at AS fecha,
                          p.total,
                          p.estado,
                          p.id_mesa,
                          m.numero
                      FROM pedidos p
                      INNER JOIN mesas m ON p.id_mesa = m.id_mesa
                      WHERE p.id_pedido = ?;
                    """;;

    try (Connection
          conn = DatabaseConnection.getConnection();
          PreparedStatement ps = conn.prepareStatement(query)
        ) {

       ps.setInt(1, id);

       try (ResultSet result = ps.executeQuery()) {
        if(result.next()){
          
          Mesa mesa = new Mesa();
          mesa.setIdMesa(result.getInt("id_mesa"));
          mesa.setNumero(result.getInt("numero"));
  
          p.setIdPedido(result.getInt("id_pedido"));
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
                p.id_pedido,
                p.created_at AS fecha,
                p.total,
                p.estado,
                m.id_mesa,
                m.numero
            FROM pedidos p
            INNER JOIN mesas m ON p.id_mesa = m.id_mesa
            WHERE p.id_mesa = ?;
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
            mesa.setIdMesa(result.getInt("id_mesa"));
            mesa.setNumero(result.getInt("numero"));


            Pedido p = new Pedido();
            p.setIdPedido(result.getInt("id_pedido"));
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

  @Override
  public String insertarDetalle(int pedidoId, DetallePedido detalle) {
    String query = "INSERT INTO detalle_pedido(id_pedido,id_producto,cantidad,precio_unitario,subtotal,observacion) VALUES(?,?,?,?,?,?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {
      ps.setInt(1, pedidoId);
      ps.setInt(2, detalle.getProducto().getIdProducto());
      ps.setInt(3, detalle.getCantidad());
      ps.setBigDecimal(4, detalle.getPrecioUnitario());
      ps.setBigDecimal(5, detalle.getSubtotal());
      ps.setString(6, detalle.getObservacion());
      int filas = ps.executeUpdate();
      if (filas > 0) return "Detalle insertado correctamente";
      return "No se pudo insertar el detalle";
    } catch (SQLException e) {
      return "Error al insertar detalle: " + e.getMessage();
    }
  }

  @Override
  public String actualizarTotal(int pedidoId, java.math.BigDecimal total) {
    String query = "UPDATE pedidos SET total = ? WHERE id_pedido = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {
      ps.setBigDecimal(1, total);
      ps.setInt(2, pedidoId);
      int filas = ps.executeUpdate();
      if (filas > 0) return "Total actualizado correctamente";
      return "No se encontr\u00f3 el pedido";
    } catch (SQLException e) {
      return "Error al actualizar total: " + e.getMessage();
    }
  }


}

