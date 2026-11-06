-- Migracion para bases de datos creadas antes del cierre del Sprint 3.
-- Aplica solo si ya tienes plazoleta_db y no quieres recrearla con docs/script.sql.

USE plazoleta_db;

-- Bloqueo optimista para los cambios de estado del pedido.
ALTER TABLE pedido
    ADD COLUMN version INT NOT NULL DEFAULT 0;

-- Indices para las consultas del menu (HU-10).
CREATE INDEX idx_plato_restaurante_estado
    ON plato (id_restaurante, estado);

CREATE INDEX idx_plato_restaurante_categoria_estado
    ON plato (id_restaurante, id_categoria, estado);

-- Verificacion rapida.
SHOW COLUMNS FROM pedido LIKE 'version';
SHOW INDEX FROM plato;
