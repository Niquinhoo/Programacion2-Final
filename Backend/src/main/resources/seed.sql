USE restaurante_db;

INSERT INTO roles (id_rol, nombre) VALUES
    (1, 'ADMIN'),
    (2, 'MOZO'),
    (3, 'COCINA')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO usuarios (id_usuario, nombre, apellido, usuario, contrasena, id_rol, activo) VALUES
    (1, 'Admin', 'Sistema', 'admin', SHA2('1234', 256), 1, TRUE),
    (2, 'Carlos', 'Gomez', 'cgomez', SHA2('1234', 256), 2, TRUE),
    (3, 'Lucia', 'Perez', 'lperez', SHA2('1234', 256), 2, TRUE),
    (4, 'Marcos', 'Ruiz', 'mruiz', SHA2('1234', 256), 3, TRUE)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    apellido = VALUES(apellido),
    contrasena = VALUES(contrasena),
    id_rol = VALUES(id_rol),
    activo = VALUES(activo);

INSERT INTO categorias (id_categoria, nombre, descripcion, activa) VALUES
    (1, 'Entradas', 'Platos para comenzar', TRUE),
    (2, 'Hamburguesas', 'Hamburguesas artesanales', TRUE),
    (3, 'Pastas', 'Pastas frescas del dia', TRUE),
    (4, 'Postres', 'Dulces y helados', TRUE),
    (5, 'Bebidas', 'Gaseosas, aguas y jugos', TRUE),
    (6, 'Cervezas', 'Cervezas nacionales e importadas', TRUE)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion),
    activa = VALUES(activa);

INSERT INTO productos (id_producto, nombre, descripcion, precio, stock, id_categoria, disponible) VALUES
    (1, 'Tabla de fiambres', 'Jamon, salame y quesos', 1800.00, 20, 1, TRUE),
    (2, 'Empanadas x6', 'Carne cortada a cuchillo', 1200.00, 30, 1, TRUE),
    (3, 'Clasica', 'Carne, lechuga, tomate, cheddar', 2500.00, 25, 2, TRUE),
    (4, 'Doble BBQ', 'Doble carne, bacon, salsa BBQ', 3200.00, 20, 2, TRUE),
    (5, 'Veggie', 'Medallon de garbanzo, palta', 2600.00, 15, 2, TRUE),
    (6, 'Noquis con tuco', 'Noquis de papa con salsa tuco', 2200.00, 20, 3, TRUE),
    (7, 'Fetuccini Alfredo', 'Crema, parmesano y jamon', 2400.00, 20, 3, TRUE),
    (8, 'Volcan de chocolate', 'Con helado de vainilla', 1400.00, 15, 4, TRUE),
    (9, 'Tiramisu', 'Receta tradicional italiana', 1300.00, 12, 4, TRUE),
    (10, 'Helado 3 bochas', 'Eleccion de sabores', 900.00, 50, 4, TRUE),
    (11, 'Coca-Cola 500ml', NULL, 700.00, 60, 5, TRUE),
    (12, 'Agua mineral 500ml', NULL, 500.00, 80, 5, TRUE),
    (13, 'Jugo naranja natural', NULL, 850.00, 30, 5, TRUE),
    (14, 'Quilmes 1L', 'Chopp personal', 1100.00, 40, 6, TRUE),
    (15, 'IPA artesanal', 'Lupulada, 500ml', 1600.00, 25, 6, TRUE)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion),
    precio = VALUES(precio),
    stock = VALUES(stock),
    id_categoria = VALUES(id_categoria),
    disponible = VALUES(disponible);

INSERT INTO mesas (id_mesa, numero, capacidad, estado) VALUES
    (1, 1, 2, 'LIBRE'),
    (2, 2, 4, 'LIBRE'),
    (3, 3, 4, 'LIBRE'),
    (4, 4, 6, 'LIBRE'),
    (5, 5, 6, 'LIBRE'),
    (6, 6, 8, 'LIBRE'),
    (7, 7, 2, 'FUERA_DE_SERVICIO'),
    (8, 8, 4, 'LIBRE')
ON DUPLICATE KEY UPDATE
    numero = VALUES(numero),
    capacidad = VALUES(capacidad),
    estado = VALUES(estado);

INSERT INTO pedidos (id_pedido, id_mesa, id_usuario, estado, total, observacion) VALUES
    (1, 2, 2, 'CERRADO', 10300.00, 'Sin cebolla en las hamburguesas')
ON DUPLICATE KEY UPDATE
    id_mesa = VALUES(id_mesa),
    id_usuario = VALUES(id_usuario),
    estado = VALUES(estado),
    total = VALUES(total),
    observacion = VALUES(observacion);

INSERT INTO detalle_pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES
    (1, 1, 3, 2, 2500.00, 5000.00),
    (2, 1, 1, 1, 1800.00, 1800.00),
    (3, 1, 11, 3, 700.00, 2100.00),
    (4, 1, 8, 1, 1400.00, 1400.00)
ON DUPLICATE KEY UPDATE
    id_pedido = VALUES(id_pedido),
    id_producto = VALUES(id_producto),
    cantidad = VALUES(cantidad),
    precio_unitario = VALUES(precio_unitario),
    subtotal = VALUES(subtotal);
