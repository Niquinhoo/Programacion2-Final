package com.restaurant.backend.dao;

import java.util.List;

import com.restaurant.backend.model.DetallePedido;
import com.restaurant.backend.model.EstadoPedido;
import com.restaurant.backend.model.Pedido;

public interface PedidoDAO {

  public List<Pedido> getPedidos();
  public List<Pedido> getPedidosPorEstado(EstadoPedido estado);
  public String Insertar(Pedido P,List<DetallePedido> detalles);
  public String ModificarEstado(int id,EstadoPedido estado);
  List<DetallePedido> getDetallesPedido(int pedidoId);
  Pedido getPedidoPorId(int id);
  List<Pedido> getPedidosPorMesa(int mesaId);
  
}
