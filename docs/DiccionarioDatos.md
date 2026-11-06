# Diccionario de datos — Plazoleta

> Versión vigente al cierre del Sprint 3. Reemplaza el contenido del archivo
> `DiccionarioDatos.xlsx`, que quedó desactualizado.

## Convenciones

- Motor: MySQL 8, `utf8mb4`.
- Llaves primarias: `INT AUTO_INCREMENT`.
- Fechas: `DATE` para nacimiento y `DATETIME` para eventos.
- Enumeraciones: se almacenan como `ENUM` en la base y como `@Enumerated(STRING)`
  en JPA.

## 1. `usuario`

| Columna | Tipo | Nulo | Clave / restricción | Descripción |
|---|---|---|---|---|
| `id` | INT | No | PK, autoincremental | Identificador del usuario. |
| `nombre` | VARCHAR(50) | No | | Nombre. |
| `apellido` | VARCHAR(50) | Sí | | Apellido. |
| `documento_de_identidad` | VARCHAR(20) | Sí | UNIQUE | Documento; no se repite. |
| `celular` | VARCHAR(13) | Sí | | Celular para SMS; admite `+`. |
| `fecha_de_nacimiento` | DATE | Sí | | Usada para validar mayoría de edad. |
| `correo` | VARCHAR(100) | No | UNIQUE | Credencial de login. |
| `clave` | VARCHAR(100) | Sí | | Hash BCrypt. |
| `rol` | ENUM | Sí | `ADMINISTRADOR`, `CLIENTE`, `EMPLEADO`, `PROPIETARIO` | Rol para autorización. |
| `id_restaurante` | INT | Sí | FK → `restaurante.id` | Restaurante del empleado (HU-12). |

## 2. `restaurante`

| Columna | Tipo | Nulo | Clave / restricción | Descripción |
|---|---|---|---|---|
| `id` | INT | No | PK | Identificador. |
| `nombre` | VARCHAR(255) | No | INDEX `idx_restaurante_nombre` | Nombre. |
| `nit` | VARCHAR(255) | No | UNIQUE | NIT; admite formato `900000000-1`. |
| `direccion` | VARCHAR(255) | No | | Dirección. |
| `telefono` | VARCHAR(255) | Sí | | Teléfono. |
| `url_logo` | VARCHAR(255) | Sí | | Logo. |
| `id_propietario` | INT | No | FK → `usuario.id` | Propietario (rol PROPIETARIO). |

## 3. `categoria`

| Columna | Tipo | Nulo | Clave | Descripción |
|---|---|---|---|---|
| `id` | INT | No | PK | Identificador. |
| `nombre` | VARCHAR(255) | No | | Nombre. |
| `descripcion` | VARCHAR(255) | Sí | | Descripción. |

## 4. `plato`

| Columna | Tipo | Nulo | Clave / restricción | Descripción |
|---|---|---|---|---|
| `id` | INT | No | PK | Identificador. |
| `nombre` | VARCHAR(255) | No | | Nombre. |
| `precio` | INT | No | CHECK `precio > 0` | Precio unitario. |
| `descripcion` | VARCHAR(255) | Sí | | Descripción. |
| `url_imagen` | VARCHAR(255) | Sí | | Imagen. |
| `estado` | BOOLEAN | Sí | | `true` activo / `false` deshabilitado (HU-07). |
| `id_categoria` | INT | No | FK → `categoria.id` | Categoría. |
| `id_restaurante` | INT | No | FK → `restaurante.id` | Restaurante. |

Índices: `idx_plato_restaurante_estado (id_restaurante, estado)` y
`idx_plato_restaurante_categoria_estado (id_restaurante, id_categoria, estado)`.

## 5. `pedido`

| Columna | Tipo | Nulo | Clave / restricción | Descripción |
|---|---|---|---|---|
| `id` | INT | No | PK | Identificador. |
| `id_cliente` | INT | No | FK → `usuario.id` | Cliente que ordena. |
| `id_restaurante` | INT | No | FK → `restaurante.id` | Restaurante. |
| `id_empleado` | INT | Sí | FK → `usuario.id` | Empleado asignado (HU-13). |
| `estado` | ENUM | No | `PENDIENTE`, `EN_PREPARACION`, `LISTO`, `ENTREGADO`, `CANCELADO` | Estado actual. |
| `fecha_creacion` | DATETIME | No | | Fecha de creación. |
| `total` | INT | No | | Suma de los detalles. |
| `pin` | VARCHAR(6) | Sí | | PIN de 4 dígitos enviado al cliente (HU-14). |
| `version` | INT | No | DEFAULT 0 | Bloqueo optimista para cambios concurrentes. |

Índices: `idx_pedido_cliente_estado`, `idx_pedido_restaurante_estado`,
`idx_pedido_empleado`.

## 6. `detalle_pedido`

| Columna | Tipo | Nulo | Clave / restricción | Descripción |
|---|---|---|---|---|
| `id` | INT | No | PK | Identificador. |
| `id_pedido` | INT | No | FK → `pedido.id` ON DELETE CASCADE | Pedido. |
| `id_plato` | INT | No | FK → `plato.id` | Plato. |
| `cantidad` | INT | No | CHECK `cantidad > 0` | Unidades. |
| `precio_unitario` | INT | No | | Precio congelado al crear el pedido. |

Restricción: `uq_detalle_pedido_plato (id_pedido, id_plato)` evita repetir el
mismo plato en un pedido.

## 7. `pedido_estado_log`

| Columna | Tipo | Nulo | Clave / restricción | Descripción |
|---|---|---|---|---|
| `id` | INT | No | PK | Identificador. |
| `id_pedido` | INT | No | FK → `pedido.id` ON DELETE CASCADE | Pedido auditado. |
| `estado_anterior` | ENUM | Sí | Mismos valores de `pedido.estado` | Estado previo; `NULL` en la creación. |
| `estado_nuevo` | ENUM | No | Mismos valores de `pedido.estado` | Estado resultante. |
| `id_empleado` | INT | Sí | FK → `usuario.id` | Empleado que hizo el cambio; `NULL` si lo hizo el cliente. |
| `fecha_cambio` | DATETIME | No | | Momento del cambio. |

Índice: `idx_log_pedido_fecha (id_pedido, fecha_cambio)`.

## 8. Datos semilla

| Usuario | Correo | Clave | Rol |
|---|---|---|---|
| Admin | `admin@plazoleta.com` | `Admin123*` | ADMINISTRADOR |
| Cliente | `cliente@plazoleta.com` | `Admin123*` | CLIENTE |
| Empleado | `empleado@plazoleta.com` | `Admin123*` | EMPLEADO |
| Propietario | `propietario@plazoleta.com` | `Admin123*` | PROPIETARIO |

Restaurante demo: NIT `900000000-1`, asociado al propietario y al empleado de
prueba. Categorías: Entradas, Platos fuertes, Postres, Bebidas.

> Las claves semilla son solo para desarrollo. En producción deben cambiarse y
> el `JWT_SECRET` debe definirse por variable de entorno.
