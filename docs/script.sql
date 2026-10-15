CREATE DATABASE IF NOT EXISTS plazoleta_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE plazoleta_db;

-- Los 4 roles del sistema (se cargan abajo, en los datos iniciales)
CREATE TABLE rol (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(30)  NOT NULL,
    descripcion VARCHAR(100) NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE usuario (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    nombre                 VARCHAR(50)  NOT NULL,
    apellido               VARCHAR(50)  NOT NULL,
    documento_de_identidad VARCHAR(20)  NOT NULL,
    celular                VARCHAR(13)  NOT NULL,
    fecha_de_nacimiento    DATE         NULL,
    correo                 VARCHAR(100) NOT NULL,
    clave                  VARCHAR(100) NOT NULL,
    id_rol                 BIGINT       NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_documento UNIQUE (documento_de_identidad),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id)
) ENGINE = InnoDB;
-
-- HU-02. El indice por nombre ayuda al listado alfabetico (HU-09).
CREATE TABLE restaurante (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(80)  NOT NULL,
    nit            VARCHAR(20)  NOT NULL,
    direccion      VARCHAR(150) NOT NULL,
    telefono       VARCHAR(13)  NOT NULL,
    url_logo       VARCHAR(255) NOT NULL,
id_propietario BIGINT       NOT NULL,
CONSTRAINT pk_restaurante PRIMARY KEY (id),
CONSTRAINT uq_restaurante_nit UNIQUE (nit),
CONSTRAINT fk_restaurante_propietario FOREIGN KEY (id_propietario) REFERENCES usuario (id),
                             INDEX idx_restaurante_nombre (nombre)
) ENGINE = InnoDB;

-- HU-03 (categoria del plato) y HU-10 (filtro del menu)
CREATE TABLE categoria (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(100) NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

-- HU-03, HU-04, HU-07, HU-10
-- precio entero mayor a 0; activo nace en TRUE (HU-03) y la HU-07 lo cambia.
CREATE TABLE plato (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(80)  NOT NULL,
    precio         INT          NOT NULL,
    descripcion    VARCHAR(255) NOT NULL,
    url_imagen     VARCHAR(255) NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    id_categoria   BIGINT       NOT NULL,
    id_restaurante BIGINT       NOT NULL,
    CONSTRAINT pk_plato PRIMARY KEY (id),
    CONSTRAINT chk_plato_precio CHECK (precio > 0),
    CONSTRAINT fk_plato_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id),
    CONSTRAINT fk_plato_restaurante FOREIGN KEY (id_restaurante) REFERENCES restaurante (id),
    INDEX idx_plato_menu (id_restaurante, id_categoria, activo)
) ENGINE = InnoDB;
-- HU-06, HU-12, HU-13: a que restaurante pertenece cada empleado.
-- id_empleado UNIQUE: un empleado trabaja en un solo restaurante.
-- El vinculo vive aqui (modulo plazoleta) y no en usuario, para que el modulo
-- usuarios no tenga que conocer los restaurantes.
CREATE TABLE empleado_restaurante (
    id             BIGINT NOT NULL AUTO_INCREMENT,
    id_empleado    BIGINT NOT NULL,
    id_restaurante BIGINT NOT NULL,
    CONSTRAINT pk_empleado_restaurante PRIMARY KEY (id),
    CONSTRAINT uq_empleado_restaurante_empleado UNIQUE (id_empleado),
    CONSTRAINT fk_emp_rest_empleado FOREIGN KEY (id_empleado) REFERENCES usuario (id),
    CONSTRAINT fk_emp_rest_restaurante FOREIGN KEY (id_restaurante) REFERENCES restaurante (id)
) ENGINE = InnoDB;
-- HU-11 a HU-16
-- estado: PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO o CANCELADO
-- id_empleado queda vacio hasta que un empleado se asigna (HU-13)
-- pin: se envia al cliente (HU-14) y se pide para entregar (HU-15)
CREATE TABLE pedido (
                        id             BIGINT      NOT NULL AUTO_INCREMENT,
                        id_cliente     BIGINT      NOT NULL,
                        id_empleado    BIGINT      NULL,
                        id_restaurante BIGINT      NOT NULL,
                        estado         VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
                        pin            VARCHAR(6)  NULL,
                        fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                        CONSTRAINT pk_pedido PRIMARY KEY (id),
                        CONSTRAINT chk_pedido_estado CHECK (estado IN
                                                            ('PENDIENTE', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO')),
                        CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES usuario (id),
                        CONSTRAINT fk_pedido_empleado FOREIGN KEY (id_empleado) REFERENCES usuario (id),
                        CONSTRAINT fk_pedido_restaurante FOREIGN KEY (id_restaurante) REFERENCES restaurante (id),
                        INDEX idx_pedido_cliente_estado (id_cliente, estado),
                        INDEX idx_pedido_restaurante_estado (id_restaurante, estado)
) ENGINE = InnoDB;
-- HU-11: platos de cada pedido y cantidad. Un plato no se repite dentro del mismo pedido.
CREATE TABLE plato_pedido (
                              id        BIGINT NOT NULL AUTO_INCREMENT,
                              id_pedido BIGINT NOT NULL,
                              id_plato  BIGINT NOT NULL,
                              cantidad  INT    NOT NULL,
                              CONSTRAINT pk_plato_pedido PRIMARY KEY (id),
                              CONSTRAINT uq_plato_pedido UNIQUE (id_pedido, id_plato),
                              CONSTRAINT chk_plato_pedido_cantidad CHECK (cantidad > 0),
                              CONSTRAINT fk_plato_pedido_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id) ON DELETE CASCADE,
                              CONSTRAINT fk_plato_pedido_plato FOREIGN KEY (id_plato) REFERENCES plato (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- MODULO TRAZABILIDAD (se usa desde la HU-13, sprint 3)
-- ---------------------------------------------------------------------

-- HU-17: un registro por cada cambio de estado. HU-18 calcula tiempos con fecha.
-- estado_anterior queda vacio en el primer registro (cuando el pedido nace).
CREATE TABLE trazabilidad (
                              id              BIGINT      NOT NULL AUTO_INCREMENT,
                              id_pedido       BIGINT      NOT NULL,
                              estado_anterior VARCHAR(20) NULL,
                              estado_nuevo    VARCHAR(20) NOT NULL,
                              id_usuario      BIGINT      NULL,
                              fecha           DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                              CONSTRAINT pk_trazabilidad PRIMARY KEY (id),
                              CONSTRAINT fk_trazabilidad_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id),
                              CONSTRAINT fk_trazabilidad_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id),
                              INDEX idx_trazabilidad_pedido (id_pedido, fecha)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- DATOS INICIALES
-- ---------------------------------------------------------------------
INSERT INTO rol (nombre, descripcion) VALUES
                                          ('ADMINISTRADOR', 'Administra la plataforma: crea propietarios y restaurantes'),
                                          ('PROPIETARIO',   'Dueno de un restaurante: gestiona platos y empleados'),
                                          ('EMPLEADO',      'Atiende los pedidos de un restaurante'),
                                          ('CLIENTE',       'Hace pedidos en la plazoleta');

-- Categorias para poder crear platos (HU-03) sin necesitar otro endpoint
INSERT INTO categoria (nombre, descripcion) VALUES
                                                ('Entradas',       'Platos para empezar'),
                                                ('Platos fuertes', 'Platos principales'),
                                                ('Postres',        'Dulces y postres'),
                                                ('Bebidas',        'Bebidas frias y calientes');

-- Primer administrador. Sin el, nadie podria crear propietarios (HU-01 exige
-- ser administrador). Solo para desarrollo:
--   correo: admin@plazoleta.com   clave: Admin123*
-- La clave de abajo es el hash bcrypt de "Admin123*". Cambiala antes de entregar.
INSERT INTO usuario
(nombre, apellido, documento_de_identidad, celular, fecha_de_nacimiento, correo, clave, id_rol)
SELECT 'Admin', 'Plazoleta', '1000000000', '+573000000000', '1990-01-01',
       'admin@plazoleta.com',
       '$2a$10$SCDUsSUoUb93IzJYNUtQUueUh4CDOTLOyllSRfIhYEGPHidd2y.8W',
       id
FROM rol
WHERE nombre = 'ADMINISTRADOR';