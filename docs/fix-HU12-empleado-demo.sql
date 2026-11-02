-- Fix HU-12: deja un restaurante demo y asocia al empleado de prueba.
-- No borra datos y se puede ejecutar varias veces.
-- Uso: mysql -u root -p < docs/fix-HU12-empleado-demo.sql
USE plazoleta_db;

-- 1. Propietario demo (misma clave que el admin: Admin123*)
INSERT INTO usuario (nombre, apellido, documento_de_identidad, celular, fecha_de_nacimiento, correo, clave, rol)
SELECT 'Propietario','Demo','3000000000','+573003333333','1990-01-01','propietario@plazoleta.com', clave, 'PROPIETARIO'
FROM usuario WHERE correo='admin@plazoleta.com'
AND NOT EXISTS (SELECT 1 FROM usuario WHERE correo='propietario@plazoleta.com');

-- 2. Restaurante demo de ese propietario
INSERT INTO restaurante (nombre, nit, direccion, telefono, url_logo, id_propietario)
SELECT 'Restaurante Demo','900000000-1','Calle 1 # 2-3','+573004444444','https://example.com/logo.png', id
FROM usuario WHERE correo='propietario@plazoleta.com'
AND NOT EXISTS (SELECT 1 FROM restaurante WHERE nit='900000000-1');

-- 3. El empleado semilla queda asignado al restaurante demo
UPDATE usuario
SET id_restaurante = (SELECT id FROM restaurante WHERE nit = '900000000-1')
WHERE correo = 'empleado@plazoleta.com'
  AND id_restaurante IS NULL;

SELECT u.correo, u.rol, r.nombre AS restaurante
FROM usuario u
LEFT JOIN restaurante r ON r.id = u.id_restaurante
WHERE u.correo IN ('propietario@plazoleta.com', 'empleado@plazoleta.com');
