-- ============================================================
--  RESTAURANTE — Schema MySQL
--  Orden de creación respeta foreign keys
-- ============================================================

CREATE DATABASE IF NOT EXISTS restaurante_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE restaurante_db;

-- ────────────────────────────────────────
-- 1. ROLES
-- ────────────────────────────────────────
CREATE TABLE roles (
    id_rol      TINYINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(30)       NOT NULL,   -- 'ADMIN', 'MOZO', 'COCINA'
    CONSTRAINT pk_roles PRIMARY KEY (id_rol),
    CONSTRAINT uq_roles_nombre UNIQUE (nombre)
);

-- ────────────────────────────────────────
-- 2. USUARIOS
-- ────────────────────────────────────────
CREATE TABLE usuarios (
    id_usuario  INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(60)       NOT NULL,
    apellido    VARCHAR(60)       NOT NULL,
    usuario     VARCHAR(40)       NOT NULL,
    contrasena  VARCHAR(255)      NOT NULL,   -- hash bcrypt o SHA-256
    id_rol      TINYINT UNSIGNED  NOT NULL,
    activo      BOOLEAN           NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuarios PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuarios_usuario UNIQUE (usuario),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol)
        REFERENCES roles (id_rol)
);

-- ────────────────────────────────────────
-- 3. CATEGORIAS
-- ────────────────────────────────────────
CREATE TABLE categorias (
    id_categoria  INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(60)   NOT NULL,
    descripcion   VARCHAR(200)  NULL,
    activa        BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_categorias PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre)
);

-- ────────────────────────────────────────
-- 4. PRODUCTOS
-- ────────────────────────────────────────
CREATE TABLE productos (
    id_producto   INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(100)      NOT NULL,
    descripcion   VARCHAR(300)      NULL,
    precio        DECIMAL(10, 2)    NOT NULL,
    stock         INT UNSIGNED      NOT NULL DEFAULT 0,
    id_categoria  INT UNSIGNED      NOT NULL,
    disponible    BOOLEAN           NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_productos PRIMARY KEY (id_producto),
    CONSTRAINT ck_productos_precio CHECK (precio >= 0),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (id_categoria)
        REFERENCES categorias (id_categoria)
);

-- ────────────────────────────────────────
-- 5. MESAS
-- ────────────────────────────────────────
CREATE TABLE mesas (
    id_mesa     INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    numero      INT UNSIGNED  NOT NULL,
    capacidad   INT UNSIGNED  NOT NULL DEFAULT 4,
    estado      ENUM('LIBRE','OCUPADA','RESERVADA','FUERA_DE_SERVICIO')
                              NOT NULL DEFAULT 'LIBRE',
    CONSTRAINT pk_mesas PRIMARY KEY (id_mesa),
    CONSTRAINT uq_mesas_numero UNIQUE (numero),
    CONSTRAINT ck_mesas_capacidad CHECK (capacidad > 0)
);

-- ────────────────────────────────────────
-- 6. PEDIDOS
-- ────────────────────────────────────────
CREATE TABLE pedidos (
    id_pedido   INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_mesa     INT UNSIGNED  NOT NULL,
    id_usuario  INT UNSIGNED  NOT NULL,   -- mozo que tomó el pedido
    estado      ENUM('ABIERTO','EN_COCINA','LISTO','CERRADO','CANCELADO')
                              NOT NULL DEFAULT 'ABIERTO',
    total       DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    observacion VARCHAR(300)  NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_pedidos PRIMARY KEY (id_pedido),
    CONSTRAINT ck_pedidos_total CHECK (total >= 0),
    CONSTRAINT fk_pedidos_mesa FOREIGN KEY (id_mesa)
        REFERENCES mesas (id_mesa),
    CONSTRAINT fk_pedidos_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario)
);

-- ────────────────────────────────────────
-- 7. DETALLE_PEDIDO
-- ────────────────────────────────────────
CREATE TABLE detalle_pedido (
    id_detalle  INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    id_pedido   INT UNSIGNED    NOT NULL,
    id_producto INT UNSIGNED    NOT NULL,
    cantidad    INT UNSIGNED    NOT NULL,
    precio_unit DECIMAL(10, 2)  NOT NULL,   -- precio al momento del pedido
    subtotal    DECIMAL(10, 2)  NOT NULL,   -- cantidad * precio_unit
    observacion VARCHAR(200)    NULL,
    CONSTRAINT pk_detalle_pedido PRIMARY KEY (id_detalle),
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_precio   CHECK (precio_unit >= 0),
    CONSTRAINT ck_detalle_subtotal CHECK (subtotal >= 0),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido)
        REFERENCES pedidos (id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
        REFERENCES productos (id_producto)
);

-- ============================================================
--  ÍNDICES adicionales para consultas frecuentes
-- ============================================================
CREATE INDEX idx_productos_categoria  ON productos (id_categoria);
CREATE INDEX idx_productos_disponible ON productos (disponible);
CREATE INDEX idx_pedidos_mesa         ON pedidos (id_mesa);
CREATE INDEX idx_pedidos_estado       ON pedidos (estado);
CREATE INDEX idx_pedidos_fecha        ON pedidos (created_at);
CREATE INDEX idx_detalle_pedido       ON detalle_pedido (id_pedido);

-- ============================================================
--  VISTA: reporte de ventas por producto
-- ============================================================
CREATE VIEW vw_ventas_por_producto AS
SELECT
    p.id_producto,
    p.nombre                          AS producto,
    c.nombre                          AS categoria,
    SUM(dp.cantidad)                  AS unidades_vendidas,
    SUM(dp.subtotal)                  AS total_recaudado
FROM detalle_pedido dp
JOIN productos  p  ON dp.id_producto  = p.id_producto
JOIN categorias c  ON p.id_categoria  = c.id_categoria
JOIN pedidos    pe ON dp.id_pedido    = pe.id_pedido
WHERE pe.estado = 'CERRADO'
GROUP BY p.id_producto, p.nombre, c.nombre
ORDER BY total_recaudado DESC;

-- ============================================================
--  VISTA: reporte de ventas por mes
-- ============================================================
CREATE VIEW vw_ventas_por_mes AS
SELECT
    YEAR(pe.created_at)              AS anio,
    MONTH(pe.created_at)             AS mes,
    COUNT(DISTINCT pe.id_pedido)     AS cantidad_pedidos,
    SUM(pe.total)                    AS total_mes
FROM pedidos pe
WHERE pe.estado = 'CERRADO'
GROUP BY YEAR(pe.created_at), MONTH(pe.created_at)
ORDER BY anio DESC, mes DESC;

-- ============================================================
--  VISTA: estado actual de mesas
-- ============================================================
CREATE VIEW vw_estado_mesas AS
SELECT
    m.id_mesa,
    m.numero,
    m.capacidad,
    m.estado,
    pe.id_pedido,
    pe.created_at   AS pedido_desde,
    pe.total        AS total_actual
FROM mesas m
LEFT JOIN pedidos pe
    ON pe.id_mesa = m.id_mesa
    AND pe.estado IN ('ABIERTO', 'EN_COCINA', 'LISTO');