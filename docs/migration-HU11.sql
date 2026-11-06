-- Migracion HU-11: ejecutar sobre una base plazoleta_db existente.
-- No borra datos. Si las tablas ya existen, no hace nada.
USE plazoleta_db;

CREATE TABLE IF NOT EXISTS pedido (
                                       id              INT NOT NULL AUTO_INCREMENT,
                                       id_cliente      INT NOT NULL,
                                       id_restaurante  INT NOT NULL,
                                       estado          ENUM('PENDIENTE','EN_PREPARACION','LISTO','ENTREGADO','CANCELADO') NOT NULL,
                                       fecha_creacion  DATETIME NOT NULL,
                                       total           INT NOT NULL,
                                       CONSTRAINT pk_pedido PRIMARY KEY (id),
                                       CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES usuario (id),
                                       CONSTRAINT fk_pedido_restaurante FOREIGN KEY (id_restaurante) REFERENCES restaurante (id),
                                       INDEX idx_pedido_cliente_estado (id_cliente, estado)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS detalle_pedido (
                                               id               INT NOT NULL AUTO_INCREMENT,
                                               id_pedido        INT NOT NULL,
                                               id_plato         INT NOT NULL,
                                               cantidad         INT NOT NULL,
                                               precio_unitario  INT NOT NULL,
                                               CONSTRAINT pk_detalle_pedido PRIMARY KEY (id),
                                               CONSTRAINT uq_detalle_pedido_plato UNIQUE (id_pedido, id_plato),
                                               CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
                                               CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id) ON DELETE CASCADE,
                                               CONSTRAINT fk_detalle_plato FOREIGN KEY (id_plato) REFERENCES plato (id)
) ENGINE = InnoDB;
