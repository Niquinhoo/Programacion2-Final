package com.restaurant.backend.dao;

import java.util.List;

import com.restaurant.backend.model.DetallePedido;
import com.restaurant.backend.model.Pedido;

public interface PedidoDAO {

  public List<Pedido> getPedidos();
  public List<Pedido> getPedidosPendientes();
  public String Insertar(Pedido P,List<DetallePedido> detalles);
  public String ModificarEstado(int id);
  
}
