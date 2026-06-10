package com.restaurant.gui;

import java.sql.Connection;
import com.restaurant.backend.dao.DatabaseConnection;

public class GUI {
    public static void main(String[] args) {
        try{
           Connection conn = DatabaseConnection.getConnection();
           if(conn != null && !conn.isClosed()){
                System.out.println("Conexión exitosa a la base de datos");
           }else{
                System.out.println("No se pudo conectar");
           }
        } catch (Exception e) {
            System.out.println("Error en la conexión:");
            e.printStackTrace();
        }
    }
}
