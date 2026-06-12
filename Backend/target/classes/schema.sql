-- Crear base de datos
CREATE DATABASE IF NOT EXISTS restaurante_db;
USE restaurante_db;

-- Tabla Categoria
CREATE TABLE IF NOT EXISTS categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

-- Tabla Producto
CREATE TABLE IF NOT EXISTS producto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    precio DECIMAL(10,2) NOT NULL,
    categoria_id INT,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE SET NULL
);

-- Tabla Mesa
CREATE TABLE IF NOT EXISTS mesa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero INT UNIQUE NOT NULL,
    estado VARCHAR(20) DEFAULT 'LIBRE' -- LIBRE, OCUPADA
);

-- Tabla Pedido
CREATE TABLE IF NOT EXISTS pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    mesa_id INT,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) DEFAULT 0.00,
    estado VARCHAR(20) DEFAULT 'PENDIENTE', -- PENDIENTE, PAGADO, CANCELADO
    FOREIGN KEY (mesa_id) REFERENCES mesa(id) ON DELETE SET NULL
);

-- Tabla DetallePedido
CREATE TABLE IF NOT EXISTS detalle_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT,
    producto_id INT,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE SET NULL
);

-- Insertar Datos de Prueba (Semillas)

-- Categorias
INSERT INTO categoria (id, nombre, descripcion) VALUES
(1, 'Hamburguesas', 'Variedad de hamburguesas caseras con guarnicion'),
(2, 'Bebidas', 'Gaseosas, aguas, cervezas y licuados'),
(3, 'Postres', 'Dulces tradicionales para cerrar la comida'),
(4, 'Entradas', 'Platos rapidos para compartir antes del principal')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), descripcion=VALUES(descripcion);

-- Productos
INSERT INTO producto (id, nombre, descripcion, precio, categoria_id) VALUES
(1, 'Hamburguesa Completa', 'Carne 150g, queso, lechuga, tomate, huevo y bacon con papas', 1200.00, 1),
(2, 'Hamburguesa Doble Cheddar', 'Doble carne, doble queso cheddar y salsa especial', 1400.00, 1),
(3, 'Hamburguesa Vegana', 'Medallon de lentejas, palta, lechuga y tomate con papas rústicas', 1100.00, 1),
(4, 'Coca Cola 500ml', 'Gaseosa sabor original en botella de plastico', 350.00, 2),
(5, 'Agua Mineral 500ml', 'Agua sin gas o con gas', 300.00, 2),
(6, 'Cerveza Patagonia IPA', 'Lata 473ml de cerveza artesanal', 600.00, 2),
(7, 'Flan con Dulce de Leche', 'Flan casero con dulce de leche y crema', 500.00, 3),
(8, 'Volcan de Chocolate', 'Bizcochuelo tibio relleno de chocolate fundido con helado', 700.00, 3),
(9, 'Papas Fritas Comunes', 'Porcion grande de papas fritas saladas', 650.00, 4),
(10, 'Bastones de Muzzarella', '6 bastones fritos acompañados de salsa pomodoro', 750.00, 4)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), descripcion=VALUES(descripcion), precio=VALUES(precio), categoria_id=VALUES(categoria_id);

-- Mesas
INSERT INTO mesa (id, numero, estado) VALUES
(1, 1, 'LIBRE'),
(2, 2, 'LIBRE'),
(3, 3, 'LIBRE'),
(4, 4, 'LIBRE'),
(5, 5, 'LIBRE')
ON DUPLICATE KEY UPDATE numero=VALUES(numero), estado=VALUES(estado);
