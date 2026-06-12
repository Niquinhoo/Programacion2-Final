package com.restaurant.backend.dao;

import java.util.List;

import com.restaurant.backend.model.EstadoMesa;
import com.restaurant.backend.model.Mesa;

public interface MesaDAO {
  public String nuevaMesa(Mesa mesa);
  public String borrarMesa(int id);
  public String cambiarEstado(int id,EstadoMesa estado);
  List<Mesa> getMesas();
  List<Mesa> getMesasPorEstado(EstadoMesa estado);
  Mesa getMesaPorId(int id);
}
