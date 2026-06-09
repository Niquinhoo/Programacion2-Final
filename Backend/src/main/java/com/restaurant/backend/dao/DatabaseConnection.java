package com.restaurant.backend.dao;

import com.restaurant.backend.util.ConexionDB;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getConnection() throws SQLException {
        return ConexionDB.getInstance().getConnection();
    }
}
