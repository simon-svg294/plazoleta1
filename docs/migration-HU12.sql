-- Migracion HU-12: ejecutar UNA sola vez sobre una base plazoleta_db existente.
-- No borra datos.
USE plazoleta_db;

-- 1. El empleado debe quedar asociado al restaurante en el que trabaja.
ALTER TABLE usuario
    ADD COLUMN id_restaurante INT NULL;

ALTER TABLE usuario
    ADD CONSTRAINT fk_usuario_restaurante
    FOREIGN KEY (id_restaurante) REFERENCES restaurante (id);

-- 2. Indice para el listado de pedidos por restaurante y estado.
ALTER TABLE pedido
    ADD INDEX idx_pedido_restaurante_estado (id_restaurante, estado);
