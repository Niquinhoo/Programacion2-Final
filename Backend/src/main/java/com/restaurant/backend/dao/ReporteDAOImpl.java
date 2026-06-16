package com.restaurant.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.restaurant.backend.service.dto.ResumenGeneralDTO;
import com.restaurant.backend.service.dto.VentaPorMesDTO;
import com.restaurant.backend.service.dto.VentaPorProductoDTO;

public class ReporteDAOImpl implements ReporteDAO {

    @Override
    public List<VentaPorProductoDTO> ventasPorProducto() {
        List<VentaPorProductoDTO> ventas = new ArrayList<>();
        String sql = """
                SELECT id_producto, producto, categoria, unidades_vendidas, total_recaudado
                FROM vw_ventas_por_producto
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ventas.add(new VentaPorProductoDTO(
                        rs.getInt("id_producto"),
                        rs.getString("producto"),
                        rs.getString("categoria"),
                        rs.getLong("unidades_vendidas"),
                        rs.getBigDecimal("total_recaudado")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al consultar ventas por producto: " + ex.getMessage());
        }

        return ventas;
    }

    @Override
    public List<VentaPorMesDTO> ventasPorMes() {
        List<VentaPorMesDTO> ventas = new ArrayList<>();
        String sql = """
                SELECT anio, mes, cantidad_pedidos, total_mes
                FROM vw_ventas_por_mes
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ventas.add(new VentaPorMesDTO(
                        rs.getInt("anio"),
                        rs.getInt("mes"),
                        rs.getLong("cantidad_pedidos"),
                        rs.getBigDecimal("total_mes")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al consultar ventas por mes: " + ex.getMessage());
        }

        return ventas;
    }

    @Override
    public ResumenGeneralDTO resumenGeneral() {
        String sql = """
                SELECT COUNT(*) AS pedidos_cerrados, COALESCE(SUM(total), 0) AS total_recaudado
                FROM pedidos
                WHERE estado = 'CERRADO'
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new ResumenGeneralDTO(
                        rs.getLong("pedidos_cerrados"),
                        rs.getBigDecimal("total_recaudado"));
            }
        } catch (SQLException ex) {
            System.out.println("Error al consultar resumen general: " + ex.getMessage());
        }

        return new ResumenGeneralDTO();
    }
}
