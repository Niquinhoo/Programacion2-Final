package com.restaurant.backend.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionDB {
    private static final String CONFIG_FILE = "db.properties";
    private static volatile ConexionDB instance;
    private final HikariDataSource dataSource;

    private ConexionDB() {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException("No se encontro " + CONFIG_FILE + " en resources");
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo cargar la configuracion de base de datos", exception);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("db.url"));
        config.setUsername(properties.getProperty("db.user"));
        config.setPassword(properties.getProperty("db.password"));

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setConnectionTimeout(10000);
        config.setPoolName("RestoManager-Pool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        this.dataSource = new HikariDataSource(config);

        // Actualizar la vista vw_ventas_por_producto para que cuente pedidos no cancelados (ABIERTO, EN_COCINA, LISTO, CERRADO)
        try (Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE OR REPLACE VIEW vw_ventas_por_producto AS " +
                         "SELECT p.id_producto, p.nombre AS producto, c.nombre AS categoria, " +
                         "SUM(dp.cantidad) AS unidades_vendidas, SUM(dp.subtotal) AS total_recaudado " +
                         "FROM detalle_pedido dp " +
                         "JOIN productos p ON dp.id_producto = p.id_producto " +
                         "JOIN categorias c ON p.id_categoria = c.id_categoria " +
                         "JOIN pedidos pe ON dp.id_pedido = pe.id_pedido " +
                         "WHERE pe.estado != 'CANCELADO' " +
                         "GROUP BY p.id_producto, p.nombre, c.nombre " +
                         "ORDER BY total_recaudado DESC");
        } catch (SQLException exception) {
            System.err.println("Advertencia: No se pudo actualizar la vista vw_ventas_por_producto en la base de datos: " + exception.getMessage());
        }
    }

    public static ConexionDB getInstance() {
        ConexionDB result = instance;
        if (result == null) {
            synchronized (ConexionDB.class) {
                result = instance;
                if (result == null) {
                    instance = result = new ConexionDB();
                }
            }
        }
        return result;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(3);
        } catch (SQLException exception) {
            return false;
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
