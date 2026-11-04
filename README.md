# Plazoleta

API REST para la gestion de una plazoleta de comida.

## Requisitos

- Java 24
- MySQL 8
- Base de datos `plazoleta_db` creada con `docs/script.sql`

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicacion queda en `http://localhost:8080`.

## Swagger / OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Autenticacion: usar el boton **Authorize** con el token JWT obtenido en `POST /api/v1/auth/login`.

## Pruebas

- Unitarias:

```powershell
.\mvnw.cmd test
```

- Postman: importar `postman/collections/Plazoleta - HU Autenticacion.postman_collection.json` y ejecutar en orden:

```text
0 -> A -> B -> D -> C -> E -> F -> G -> H -> I -> J -> K -> L -> M -> N -> O
```

- Coleccion solo HU 12-13-14: `postman/collections/Plazoleta - HU 12-13-14.postman_collection.json`.
- Newman:

```powershell
npx newman run "postman/collections/Plazoleta - HU Autenticacion.postman_collection.json" --env-var baseUrl=http://localhost:8080
```

## Endpoints HU-16 y HU-17

- `PATCH /api/v1/pedidos/{idPedido}/cancelar` (solo CLIENTE): cancela el pedido solo si esta en estado `PENDIENTE`. Si el estado es diferente, responde `400` con el mensaje exacto de la HU-16: `Lo sentimos, tu pedido ya está en preparación y no puede cancelarse`.
- `GET /api/v1/pedidos/{idPedido}/trazabilidad` (solo CLIENTE): devuelve el historial de cambios de estado del pedido. El cliente solo puede consultar sus propios pedidos.

La tabla `pedido_estado_log` se crea con `docs/script.sql`. Si la base de datos ya existe, aplicar `docs/migration-HU17.sql`.

## Patrones y estructura

- Builder (Lombok) en entidades y DTOs.
- Repository (Spring Data JPA).
- Service layer con interfaces e implementaciones.
