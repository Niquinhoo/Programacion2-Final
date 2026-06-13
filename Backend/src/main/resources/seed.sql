USE restaurante_db;

-- ── 1. Roles ──
INSERT INTO roles (id_rol, nombre) VALUES
    (1, 'ADMIN'),
    (2, 'MOZO'),
    (3, 'COCINA')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- ── 2. Usuarios (contraseña por defecto: "1234") ──
INSERT INTO usuarios (id_usuario, nombre, apellido, usuario, contrasena, id_rol, activo) VALUES
    (1, 'Admin', 'Sistema', 'admin', SHA2('1234', 256), 1, TRUE),
    (2, 'Carlos', 'Gomez', 'cgomez', SHA2('1234', 256), 2, TRUE),
    (3, 'Lucia', 'Perez', 'lperez', SHA2('1234', 256), 2, TRUE),
    (4, 'Marcos', 'Ruiz', 'mruiz', SHA2('1234', 256), 3, TRUE),
    (5, 'Sofia', 'Valenzuela', 'svalenzuela', SHA2('1234', 256), 2, TRUE),
    (6, 'Jorge', 'Rodriguez', 'jrodriguez', SHA2('1234', 256), 3, TRUE),
    (7, 'Marta', 'Lopez', 'mlopez', SHA2('1234', 256), 1, TRUE)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    apellido = VALUES(apellido),
    contrasena = VALUES(contrasena),
    id_rol = VALUES(id_rol),
    activo = VALUES(activo);

-- ── 3. Categorías ──
INSERT INTO categorias (id_categoria, nombre, descripcion, activa) VALUES
    (1, 'Entradas', 'Platos para comenzar', TRUE),
    (2, 'Hamburguesas', 'Hamburguesas artesanales', TRUE),
    (3, 'Pastas', 'Pastas frescas del dia', TRUE),
    (4, 'Postres', 'Dulces y helados', TRUE),
    (5, 'Bebidas', 'Gaseosas, aguas y jugos', TRUE),
    (6, 'Cervezas', 'Cervezas nacionales e importadas', TRUE),
    (7, 'Minutas', 'Platos rapidos y tradicionales', TRUE),
    (8, 'Pizzas', 'Pizzas al horno de piedra', TRUE),
    (9, 'Vinos', 'Vinos tintos y blancos', TRUE)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion),
    activa = VALUES(activa);

-- ── 4. Productos ──
INSERT INTO productos (id_producto, nombre, descripcion, precio, stock, id_categoria, disponible) VALUES
    -- Entradas
    (1, 'Tabla de fiambres', 'Jamon, salame y quesos', 1800.00, 20, 1, TRUE),
    (2, 'Empanadas x6', 'Carne cortada a cuchillo', 1200.00, 30, 1, TRUE),
    -- Hamburguesas
    (3, 'Clasica', 'Carne, lechuga, tomate, cheddar', 2500.00, 25, 2, TRUE),
    (4, 'Doble BBQ', 'Doble carne, bacon, salsa BBQ', 3200.00, 20, 2, TRUE),
    (5, 'Veggie', 'Medallon de garbanzo, palta', 2600.00, 15, 2, TRUE),
    -- Pastas
    (6, 'Noquis con tuco', 'Noquis de papa con salsa tuco', 2200.00, 20, 3, TRUE),
    (7, 'Fetuccini Alfredo', 'Crema, parmesano y jamon', 2400.00, 20, 3, TRUE),
    -- Postres
    (8, 'Volcan de chocolate', 'Con helado de vainilla', 1400.00, 15, 4, TRUE),
    (9, 'Tiramisu', 'Receta tradicional italiana', 1300.00, 12, 4, TRUE),
    (10, 'Helado 3 bochas', 'Eleccion de sabores', 900.00, 50, 4, TRUE),
    -- Bebidas
    (11, 'Coca-Cola 500ml', NULL, 700.00, 60, 5, TRUE),
    (12, 'Agua mineral 500ml', NULL, 500.00, 80, 5, TRUE),
    (13, 'Jugo naranja natural', NULL, 850.00, 30, 5, TRUE),
    -- Cervezas
    (14, 'Quilmes 1L', 'Chopp personal', 1100.00, 40, 6, TRUE),
    (15, 'IPA artesanal', 'Lupulada, 500ml', 1600.00, 25, 6, TRUE),
    -- Minutas
    (16, 'Milanesa con papas fritas', 'De ternera, acompanada de papas fritas baston', 2800.00, 30, 7, TRUE),
    (17, 'Milanesa Napolitana', 'Con salsa de tomate, jamon y muzzarella gratinada', 3400.00, 25, 7, TRUE),
    (18, 'Suprema de pollo con pure', 'Suprema rebozada con pure de papas casero', 2700.00, 20, 7, TRUE),
    -- Pizzas
    (19, 'Muzzarella clasica', 'Salsa de tomate, muzzarella y aceitunas verdes', 3200.00, 40, 8, TRUE),
    (20, 'Pizza Especial', 'Muzzarella, jamon cocido y morrones asados', 3800.00, 30, 8, TRUE),
    (21, 'Fugazzeta', 'Muzzarella, abundante cebolla y oregano', 3500.00, 25, 8, TRUE),
    -- Vinos
    (22, 'Malbec Reserva 750ml', 'Vino tinto varietal de alta gama', 4500.00, 15, 9, TRUE),
    (23, 'Chardonnay 750ml', 'Vino blanco varietal frutado', 3800.00, 18, 9, TRUE)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion),
    precio = VALUES(precio),
    stock = VALUES(stock),
    id_categoria = VALUES(id_categoria),
    disponible = VALUES(disponible);

-- ── 5. Mesas ──
INSERT INTO mesas (id_mesa, numero, capacidad, estado) VALUES
    (1, 1, 2, 'LIBRE'),
    (2, 2, 4, 'LIBRE'),
    (3, 3, 4, 'LIBRE'),
    (4, 4, 6, 'LIBRE'),
    (5, 5, 6, 'LIBRE'),
    (6, 6, 8, 'LIBRE'),
    (7, 7, 2, 'FUERA_DE_SERVICIO'),
    (8, 8, 4, 'LIBRE'),
    (9, 9, 2, 'LIBRE'),
    (10, 10, 4, 'OCUPADA'),
    (11, 11, 4, 'OCUPADA'),
    (12, 12, 6, 'RESERVADA'),
    (13, 13, 2, 'LIBRE'),
    (14, 14, 4, 'OCUPADA'),
    (15, 15, 8, 'LIBRE')
ON DUPLICATE KEY UPDATE
    numero = VALUES(numero),
    capacidad = VALUES(capacidad),
    estado = VALUES(estado);

-- ── 6. Pedidos Históricos y Activos ──
-- Fechas variadas para enriquecer las vistas estadísticas (vw_ventas_por_mes y vw_ventas_por_producto)
INSERT INTO pedidos (id_pedido, id_mesa, id_usuario, estado, total, observacion, created_at) VALUES
    (1, 2, 2, 'CERRADO', 10300.00, 'Sin cebolla en las hamburguesas', '2026-06-01 13:00:00'),
    (2, 3, 3, 'CERRADO', 5800.00, NULL, '2026-04-15 21:00:00'),
    (3, 1, 2, 'CERRADO', 9800.00, 'Cocido jugoso', '2026-05-10 20:30:00'),
    (4, 5, 3, 'CERRADO', 6200.00, NULL, '2026-05-25 22:15:00'),
    (5, 8, 5, 'CERRADO', 14900.00, 'Malbec a temperatura ambiente', '2026-06-05 21:30:00'),
    (6, 10, 2, 'ABIERTO', 5000.00, NULL, CURRENT_TIMESTAMP),
    (7, 11, 5, 'EN_COCINA', 6100.00, 'Pure bien caliente', CURRENT_TIMESTAMP),
    (8, 14, 3, 'LISTO', 3900.00, NULL, CURRENT_TIMESTAMP),
    (9, 4, 2, 'CANCELADO', 0.00, 'Cliente se tuvo que retirar', '2026-06-08 15:00:00')
ON DUPLICATE KEY UPDATE
    id_mesa = VALUES(id_mesa),
    id_usuario = VALUES(id_usuario),
    estado = VALUES(estado),
    total = VALUES(total),
    observacion = VALUES(observacion),
    created_at = VALUES(created_at);

-- ── 7. Detalle de los Pedidos ──
INSERT INTO detalle_pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES
    -- Pedido 1 (Total = 10300.00)
    (1, 1, 3, 2, 2500.00, 5000.00),   -- 2 Hamburguesas Clásicas
    (2, 1, 1, 1, 1800.00, 1800.00),   -- 1 Tabla de fiambres
    (3, 1, 11, 3, 700.00, 2100.00),   -- 3 Coca-Colas
    (4, 1, 8, 1, 1400.00, 1400.00),   -- 1 Volcán de chocolate

    -- Pedido 2 (Total = 5800.00)
    (5, 2, 6, 2, 2200.00, 4400.00),   -- 2 Ñoquis con tuco
    (6, 2, 11, 2, 700.00, 1400.00),   -- 2 Coca-Colas

    -- Pedido 3 (Total = 9800.00)
    (7, 3, 17, 1, 3400.00, 3400.00),  -- 1 Milanesa Napolitana
    (8, 3, 4, 1, 3200.00, 3200.00),   -- 1 Doble BBQ
    (9, 3, 15, 2, 1600.00, 3200.00),  -- 2 IPA artesanal

    -- Pedido 4 (Total = 6200.00)
    (10, 4, 20, 1, 3800.00, 3800.00), -- 1 Pizza Especial
    (11, 4, 14, 1, 1100.00, 1100.00), -- 1 Quilmes 1L
    (12, 4, 9, 1, 1300.00, 1300.00),  -- 1 Tiramisú

    -- Pedido 5 (Total = 14900.00)
    (13, 5, 1, 1, 1800.00, 1800.00),  -- 1 Tabla de fiambres
    (14, 5, 22, 1, 4500.00, 4500.00), -- 1 Malbec Reserva
    (15, 5, 7, 2, 2400.00, 4800.00),  -- 2 Fetuccini Alfredo
    (16, 5, 8, 2, 1400.00, 2800.00),  -- 2 Volcán de chocolate
    (17, 5, 12, 2, 500.00, 1000.00),  -- 2 Aguas minerales

    -- Pedido 6 (Total = 5000.00)
    (18, 6, 3, 2, 2500.00, 5000.00),  -- 2 Hamburguesas Clásicas

    -- Pedido 7 (Total = 6100.00)
    (19, 7, 17, 1, 3400.00, 3400.00), -- 1 Milanesa Napolitana
    (20, 7, 18, 1, 2700.00, 2700.00), -- 1 Suprema de pollo con puré

    -- Pedido 8 (Total = 3900.00)
    (21, 8, 19, 1, 3200.00, 3200.00), -- 1 Pizza Muzzarella clásica
    (22, 8, 11, 1, 700.00, 700.00)    -- 1 Coca-Cola
ON DUPLICATE KEY UPDATE
    id_pedido = VALUES(id_pedido),
    id_producto = VALUES(id_producto),
    cantidad = VALUES(cantidad),
    precio_unitario = VALUES(precio_unitario),
    subtotal = VALUES(subtotal);
