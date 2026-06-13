package com.restaurant.backend.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConexionDBTest {

    @Test
    public void testDatabaseConnection() {
        ConexionDB conexion = ConexionDB.getInstance();
        assertNotNull(conexion, "La instancia de ConexionDB no debe ser nula");
        
        boolean isConnected = conexion.testConnection();
        assertTrue(isConnected, "La conexion a la base de datos de TiDB Cloud debe ser exitosa. Verifique la configuracion en db.properties");
    }
}
