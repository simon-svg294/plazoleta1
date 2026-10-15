-- Script corregido: coincide con las entidades Java (Usuario, Restaurante, Categoria, Plato).
-- OJO: borra y vuelve a crear plazoleta_db (solo para desarrollo).
DROP DATABASE IF EXISTS plazoleta_db;
CREATE DATABASE plazoleta_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE plazoleta_db;

CREATE TABLE usuario (
                         id                     INT          NOT NULL AUTO_INCREMENT,
                         nombre                 VARCHAR(50)  NOT NULL,
                         apellido               VARCHAR(50)  NULL,
                         documento_de_identidad VARCHAR(20)  NULL,
                         celular                VARCHAR(13)  NULL,
                         fecha_de_nacimiento    DATE         NULL,
                         correo                 VARCHAR(100) NOT NULL,
                         clave                  VARCHAR(100) NULL,
                         rol                    ENUM('ADMINISTRADOR','CLIENTE','EMPLEADO','PROPIETARIO') NULL,
                         CONSTRAINT pk_usuario PRIMARY KEY (id),
                         CONSTRAINT uq_usuario_documento UNIQUE (documento_de_identidad),
                         CONSTRAINT uq_usuario_correo UNIQUE (correo)
) ENGINE = InnoDB;

CREATE TABLE restaurante (
                             id             INT          NOT NULL AUTO_INCREMENT,
                             nombre         VARCHAR(255) NOT NULL,
                             nit            VARCHAR(255) NOT NULL,
                             direccion      VARCHAR(255) NOT NULL,
                             telefono       VARCHAR(255) NULL,
                             url_logo       VARCHAR(255) NULL,
                             id_propietario INT          NOT NULL,
                             CONSTRAINT pk_restaurante PRIMARY KEY (id),
                             CONSTRAINT uq_restaurante_nit UNIQUE (nit),
                             CONSTRAINT fk_restaurante_propietario FOREIGN KEY (id_propietario) REFERENCES usuario (id),
                             INDEX idx_restaurante_nombre (nombre)
) ENGINE = InnoDB;

CREATE TABLE categoria (
                           id          INT          NOT NULL AUTO_INCREMENT,
                           nombre      VARCHAR(255) NOT NULL,
                           descripcion VARCHAR(255) NULL,
                           CONSTRAINT pk_categoria PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE plato (
                       id             INT          NOT NULL AUTO_INCREMENT,
                       nombre         VARCHAR(255) NOT NULL,
                       precio         INT          NOT NULL,
                       descripcion    VARCHAR(255) NULL,
                       url_imagen     VARCHAR(255) NULL,
                       estado         BOOLEAN      NULL,
                       id_categoria   INT          NOT NULL,
                       id_restaurante INT          NOT NULL,
                       CONSTRAINT pk_plato PRIMARY KEY (id),
                       CONSTRAINT chk_plato_precio CHECK (precio > 0),
                       CONSTRAINT fk_plato_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id),
                       CONSTRAINT fk_plato_restaurante FOREIGN KEY (id_restaurante) REFERENCES restaurante (id)
) ENGINE = InnoDB;

-- Datos iniciales
INSERT INTO categoria (nombre, descripcion) VALUES
                                                ('Entradas','Platos para empezar'),
                                                ('Platos fuertes','Platos principales'),
                                                ('Postres','Dulces y postres'),
                                                ('Bebidas','Bebidas frias y calientes');

-- Administrador: admin@plazoleta.com  /  Admin123*  (hash bcrypt)
INSERT INTO usuario (nombre, apellido, documento_de_identidad, celular, fecha_de_nacimiento, correo, clave, rol)
VALUES ('Admin','Plazoleta','1000000000','+573000000000','1990-01-01','admin@plazoleta.com',
        '$2a$10$SCDUsSUoUb93IzJYNUtQUueUh4CDOTLOyllSRfIhYEGPHidd2y.8W','ADMINISTRADOR');

-- Cliente y empleado de prueba (misma clave: Admin123*)
INSERT INTO usuario (nombre, apellido, documento_de_identidad, celular, fecha_de_nacimiento, correo, clave, rol)
SELECT 'Cliente','Prueba','2000000001','+573001111111','1995-03-10','cliente@plazoleta.com', clave, 'CLIENTE'
FROM usuario WHERE correo='admin@plazoleta.com';
INSERT INTO usuario (nombre, apellido, documento_de_identidad, celular, fecha_de_nacimiento, correo, clave, rol)
SELECT 'Empleado','Prueba','2000000002','+573002222222','1995-03-10','empleado@plazoleta.com', clave, 'EMPLEADO'
FROM usuario WHERE correo='admin@plazoleta.com';

SELECT id, correo, rol FROM usuario;

