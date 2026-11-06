-- Migracion HU-13 y HU-14
-- HU-13: empleado asignado al pedido.
-- HU-14: PIN de seguridad para que el cliente reclame el pedido.
-- Uso: mysql -u root -p < docs/migration-HU13-14.sql
USE plazoleta_db;

ALTER TABLE pedido
    ADD COLUMN id_empleado INT NULL AFTER id_restaurante,
    ADD COLUMN pin VARCHAR(6) NULL AFTER total;

ALTER TABLE pedido
    ADD CONSTRAINT fk_pedido_empleado FOREIGN KEY (id_empleado) REFERENCES usuario (id);

CREATE INDEX idx_pedido_empleado ON pedido (id_empleado);
