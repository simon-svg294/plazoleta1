-- HU-17: migracion para bases de datos que ya existen.
-- Crea la tabla de logs de trazabilidad de estados del pedido.
USE plazoleta_db;

CREATE TABLE IF NOT EXISTS pedido_estado_log (
    id               INT NOT NULL AUTO_INCREMENT,
    id_pedido        INT NOT NULL,
    estado_anterior  ENUM('PENDIENTE','EN_PREPARACION','LISTO','ENTREGADO','CANCELADO') NULL,
    estado_nuevo     ENUM('PENDIENTE','EN_PREPARACION','LISTO','ENTREGADO','CANCELADO') NOT NULL,
    id_empleado      INT NULL,
    fecha_cambio     DATETIME NOT NULL,
    CONSTRAINT pk_pedido_estado_log PRIMARY KEY (id),
    CONSTRAINT fk_log_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_log_empleado FOREIGN KEY (id_empleado) REFERENCES usuario (id),
    INDEX idx_log_pedido_fecha (id_pedido, fecha_cambio)
) ENGINE = InnoDB;

-- Backfill: los pedidos creados antes de esta HU no tienen historial.
-- Se registra al menos su creacion; los cambios historicos no se pueden reconstruir.
INSERT INTO pedido_estado_log (id_pedido, estado_anterior, estado_nuevo, id_empleado, fecha_cambio)
SELECT p.id, NULL, 'PENDIENTE', NULL, p.fecha_creacion
FROM pedido p
WHERE NOT EXISTS (
    SELECT 1 FROM pedido_estado_log l WHERE l.id_pedido = p.id
);
