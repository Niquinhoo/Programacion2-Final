# Informe de Primera Etapa y Plan de Continuación
## Proyecto: Sistema de Gestión de Restaurante (Java SE + Swing + Maven + MySQL)

Este documento resume el progreso realizado durante la **Primera Etapa (Modelado de Datos y Base de Datos)** y define la hoja de ruta clara para las siguientes fases del desarrollo.

---

## 1. Resumen de Cambios Realizados (Etapa 1)

Se ha completado la base estructural y de persistencia del sistema, cumpliendo con los entregables asignados al modelado de datos y base de datos:

### A. Arquitectura del Proyecto (Maven Multi-módulo)
*   **POM Padre (`pom.xml` en raíz):** Coordina los submódulos e inicializa propiedades globales (Java 17, codificación UTF-8).
*   **Módulo Backend (`Backend/pom.xml`):** Empaqueta la lógica e infraestructura de datos. Incluye dependencias para **MySQL Connector/J (9.1.0)** y **JUnit Jupiter (5.10.1)** para pruebas unitarias.
*   **Módulo GUI (`GUI/pom.xml`):** Módulo de presentación Swing. Tiene declarada la dependencia local hacia el módulo Backend para consumir los modelos y servicios.

### B. Diseño e Implementación de la Base de Datos (MySQL / TiDB)
*   **Esquema de Base de Datos (`Backend/src/main/resources/schema.sql`):**
    *   Creación de la base de datos `restaurante_db`.
    *   Definición de 7 tablas con claves primarias, foráneas, restricciones de control (`CHECK`) e índices de optimización:
        *   `roles` (Administrador, Mozo, Cocina).
        *   `usuarios` (Con claves cifradas mediante SHA-2).
        *   `categorias` (Categorías de productos alimenticios).
        *   `productos` (Stock, precio, disponibilidad).
        *   `mesas` (Número de mesa, capacidad y estado).
        *   `pedidos` (Cabecera de órdenes por mesa, usuario mozo y estado).
        *   `detalle_pedido` (Líneas del pedido con subtotales calculados).
    *   Diseño de 3 vistas (`VIEW`) para reportes en tiempo real:
        *   `vw_ventas_por_producto`: Suma de cantidades e ingresos por producto facturado.
        *   `vw_ventas_por_mes`: Consolidado mensual de facturación y volumen de pedidos.
        *   `vw_estado_mesas`: Asocia mesas con pedidos activos (`ABIERTO`, `EN_COCINA`, `LISTO`).
*   **Carga Inicial (`Backend/src/main/resources/seed.sql`):** Script para poblar registros semilla para pruebas rápidas (roles, mozos/administrador por defecto, menús completos, mesas y un pedido cerrado de prueba para validar vistas).
*   **Configuración del Servidor (`db.properties`):** Apunta a una instancia en la nube de **TiDB Cloud Serverless** (`gateway01.us-east-1.prod.aws.tidbcloud.com`), garantizando base de datos en línea accesible por todo el equipo.

### C. Persistencia e Infraestructura de Conexión (Backend)
*   **Singleton de Conexión (`ConexionDB.java`):**
    *   Implementa patrón Singleton con carga dinámica del archivo `db.properties`.
    *   Provee un método `testConnection()` para verificar salud de la BD de forma no bloqueante.
    *   Acceso thread-safe sincronizado.
*   **Fachada de Conexión (`DatabaseConnection.java`):** Simplifica la obtención del objeto `Connection` de JDBC mediante llamadas estáticas.

### D. Modelos de Dominio (POJOs en Java)
Se programaron las clases de datos en `com.restaurant.backend.model` mapeando las tablas relacionales. Cuentan con constructores, encapsulamiento (`private`), getters/setters, métodos de formateo (ej. `getNombreCompleto` en `Usuario`) y sobrescritura de `equals()`, `hashCode()` basados en el identificador único (ID) y `toString()` para su despliegue en componentes Swing (`JList` / `JComboBox`):
*   `Categoria.java`
*   `Producto.java`
*   `Mesa.java`
*   `EstadoMesa.java` (Enum: `LIBRE`, `OCUPADA`, `RESERVADA`, `FUERA_DE_SERVICIO`)
*   `Usuario.java`
*   `Rol.java`
*   `Pedido.java`
*   `EstadoPedido.java` (Enum: `ABIERTO`, `EN_COCINA`, `LISTO`, `CERRADO`, `CANCELADO`)
*   `DetallePedido.java`

### E. Prototipo Visual e Interfaces de Capa (GUI Mockup)
*   **Maqueta Principal (`GUI.java` en GUI):** Estructura base en Swing con paleta de colores limpia (estética moderna en tonos claros y verdes). Contiene paneles estáticos para Login, Grilla de mesas funcionales ficticias, Tabla de menú de productos, Detalle de pedido y área de Reportes.
*   **Clases Esqueleto:** Creación inicial de las interfaces y clases vacías DAO (`CategoriaDAO`, `MesaDAO`, `PedidoDAO`, `ProductoDAO`) y controladores correspondientes, listos para programarse.

---

## 2. Plan de Continuación (Cómo Proseguir)

Con la primera etapa completamente funcional y desplegada en la rama `layer/model`, el desarrollo debe continuar en paralelo o secuencial según el plan de trabajo establecido originalmente:

### Fase A: Implementación de la Capa DAO (Integrante 2)
El siguiente paso prioritario consiste en inyectar el código SQL JDBC dentro de las implementaciones DAO en `com.restaurant.backend.dao`.
1.  **Operaciones CRUD Básicas:**
    *   **`ProductoDAO`**: Métodos para guardar, actualizar estado (`disponible`), editar stock, borrar y listar productos (con opción de filtrar por categoría).
    *   **`MesaDAO`**: Obtener lista de mesas y cambiar su estado (`LIBRE`, `OCUPADA`, etc.) al abrir/cerrar pedidos.
    *   **`CategoriaDAO`**: Obtener y guardar categorías.
2.  **Operaciones Transaccionales Complejas:**
    *   **`PedidoDAO` & `DetallePedidoDAO`**: Crear pedido cabecera y guardar el listado de líneas asociadas dentro de una transacción JDBC (`connection.setAutoCommit(false)`) para asegurar consistencia si falla la inserción de algún ítem.
3.  **DAO de Reportes:**
    *   **`ReporteDAO`**: Consultar las vistas de la BD (`vw_ventas_por_producto` y `vw_ventas_por_mes`) y retornar colecciones para alimentar gráficos o tablas estadísticas.
4.  **Autenticación (`UsuarioDAO`):**
    *   Crear la interfaz e implementación para buscar usuarios por credenciales (`usuario` y password) aplicando `SHA2(?, 256)` en la consulta para validar contraseñas cifradas en base de datos.

### Fase B: Lógica de Negocio y Servicios (Integrante 3)
Crear las clases de servicio (`service/`) para desacoplar el flujo de datos (DAO) de la interfaz gráfica (UI).
*   **Validaciones Críticas:**
    *   No permitir abrir un pedido en una mesa que ya está `OCUPADA` o `FUERA_DE_SERVICIO`.
    *   Validar que haya suficiente `stock` en el producto antes de agregarlo al detalle del pedido.
    *   Disminuir el stock en la base de datos una vez se confirme el pedido.
    *   Calcular de forma segura los importes totales (precios unitarios * cantidad) y actualizar el total del pedido cabecera.

### Fase C: Modularización de Vistas Swing (Integrante 4)
El archivo único `GUI.java` debe dividirse en componentes y ventanas reales reutilizables dentro del paquete `com.restaurant.gui.view`:
1.  **`VentanaLogin.java`:** Dialogo modal de acceso inicial.
2.  **`VentanaMesas.java`:** Panel principal para mozos que lee el estado de la base de datos y dibuja dinámicamente los botones de las mesas con colores según su estado actual.
3.  **`VentanaPedido.java`:** Ventana para gestionar el pedido de la mesa seleccionada, permitiendo agregar productos al carrito.
4.  **`MainFrame.java`:** Panel contenedor principal que gestiona la navegación lateral.

### Fase D: Reportes y Cierre (Integrante 5)
1.  **Exportación a PDF:** Agregar la librería **iText** o **Apache PDFBox** al `pom.xml` del Backend y escribir la lógica para generar tickets de pedidos cerrados y resúmenes de ventas en formato PDF descargable.
2.  **Plugin de Construcción:** Agregar el `maven-shade-plugin` o `maven-assembly-plugin` en el POM del agregador principal para empaquetar un solo archivo `.jar` autoejecutable que contenga las dependencias de MySQL, PDF y los recursos gráficos.
