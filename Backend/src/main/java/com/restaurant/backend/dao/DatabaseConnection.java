package com.restaurant.backend.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnection {
  private static final String URL;
  private static final String USER;
  private static final String PASSWORD;

  // Cuando el programa ve DatabaseConnection por primera vez carga el archivo db.propertie
  static{
    try {
      Properties properties = new Properties(); 
      InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties");
      properties.load(input);
      URL = properties.getProperty("db.url");
      USER = properties.getProperty("db.user");
      PASSWORD = properties.getProperty("db.password");
    } catch (Exception e) {
       throw new RuntimeException("Error cargando db.properties", e);
    }
  }

  public static Connection getConnection() {
    try {
      return DriverManager.getConnection(URL, USER, PASSWORD);
    } catch (Exception e) {
      throw new RuntimeException("Error al conectar a la base de datos", e);
    }
  }



}



