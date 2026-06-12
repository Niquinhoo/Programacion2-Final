package com.restaurant.backend.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionDB {
    private static final String CONFIG_FILE = "db.properties";
    private static ConexionDB instance;
    private final Properties properties;

    private ConexionDB() {
        properties = new Properties();
        cargarPropiedades();
    }

    public static synchronized ConexionDB getInstance() {
        if (instance == null) {
            instance = new ConexionDB();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password"));
    }

    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(3);
        } catch (SQLException exception) {
            return false;
        }
    }

    private void cargarPropiedades() {
        try {
            InputStream inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(CONFIG_FILE);
            
            if (inputStream == null) {
                inputStream = new java.io.FileInputStream("Backend/src/main/resources/" + CONFIG_FILE);
            }
            
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo cargar la configuracion de base de datos", exception);
        }
    }
}