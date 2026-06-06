-- ============================================================
--  RESTAURANTE — Datos de prueba (seed)
-- ============================================================

USE restaurante_db;

-- ── Roles ──
INSERT INTO roles (nombre) VALUES
    ('ADMIN'),
    ('MOZO'),
    ('COCINA');

-- ── Usuarios (contraseña: "1234" — en prod usar hash) ──
INSERT INTO usuarios (nombre, apellido, usuario, contrasena, id_rol) VALUES
    ('Admin',   'Sistema',   'admin',   SHA2('1234', 256), 1),
    ('Carlos',  'Gomez',     'cgomez',  SHA2('1234', 256), 2),
    ('Lucia',   'Perez',     'lperez',  SHA2('1234', 256), 2),
    ('Marcos',  'Ruiz',      'mruiz',   SHA2('1234', 256), 3);

-- ── Categorías ──
INSERT INTO categorias (nombre, descripcion) VALUES
    ('Entradas',    'Platos para comenzar'),
    ('Hamburguesas','Hamburguesas artesanales'),
    ('Pastas',      'Pastas frescas del día'),
    ('Postres',     'Dulces y helados'),
    ('Bebidas',     'Gaseosas, aguas y jugos'),
    ('Cervezas',    'Cervezas nacionales e importadas');

-- ── Productos ──
INSERT INTO productos (nombre, descripcion, precio, stock, id_categoria) VALUES
    -- Entradas
    ('Tabla de fiambres',   'Jamón, salame y quesos',           1800.00, 20, 1),
    ('Empanadas x6',        'Carne cortada a cuchillo',         1200.00, 30, 1),
    -- Hamburguesas
    ('Clásica',             'Carne, lechuga, tomate, cheddar',  2500.00, 25, 2),
    ('Doble BBQ',           'Doble carne, bacon, salsa BBQ',    3200.00, 20, 2),
    ('Veggie',              'Medallón de garbanzo, palta',      2600.00, 15, 2),
    -- Pastas
    ('Ñoquis c/tuco',       'Ñoquis de papa con salsa tuco',    2200.00, 20, 3),
    ('Fetuccini Alfredo',   'Crema, parmesano y jamón',         2400.00, 20, 3),
    -- Postres
    ('Volcán de chocolate', 'Con helado de vainilla',           1400.00, 15, 4),
    ('Tiramisú',            'Receta tradicional italiana',      1300.00, 12, 4),
    ('Helado 3 bochas',     'Elección de sabores',               900.00, 50, 4),
    -- Bebidas
    ('Coca-Cola 500ml',     NULL,                                700.00, 60, 5),
    ('Agua mineral 500ml',  NULL,                                500.00, 80, 5),
    ('Jugo naranja natural',NULL,                                850.00, 30, 5),
    -- Cervezas
    ('Quilmes 1L',          'Chopp personal',                   1100.00, 40, 6),
    ('IPA artesanal',       'Lupulada, 500ml',                  1600.00, 25, 6);

-- ── Mesas ──
INSERT INTO mesas (numero, capacidad, estado) VALUES
    (1, 2, 'LIBRE'),
    (2, 4, 'LIBRE'),
    (3, 4, 'LIBRE'),
    (4, 6, 'LIBRE'),
    (5, 6, 'LIBRE'),
    (6, 8, 'LIBRE'),
    (7, 2, 'FUERA_DE_SERVICIO'),
    (8, 4, 'LIBRE');

-- ── Pedido de ejemplo (cerrado — para que los reportes muestren datos) ──
INSERT INTO pedidos (id_mesa, id_usuario, estado, total, observacion) VALUES
    (2, 2, 'CERRADO', 9500.00, 'Sin cebolla en las hamburguesas');

INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unit, subtotal) VALUES
    (1, 3, 2, 2500.00, 5000.00),   -- 2 Clásicas
    (1, 1, 1, 1800.00, 1800.00),   -- 1 Tabla de fiambres
    (1, 11, 3,  700.00, 2100.00),  -- 3 Coca-Colas
    (1, 8,  1, 1400.00, 1400.00);  -- 1 Volcán de chocolate
-- (total real = 10300, dejado en 9500 para simular descuento manual)