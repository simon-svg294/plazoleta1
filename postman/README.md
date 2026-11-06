# Postman — Plazoleta

## Colección canónica

```text
postman/collections/Plazoleta - HU Autenticacion.postman_collection.json
```

Es la colección vigente al cierre del Sprint 3. Contiene 16 carpetas y cubre
autenticación, registro, restaurantes, platos, pedidos, PIN, cancelación y
trazabilidad.

### Orden de ejecución

```text
0 → A → B → D → C → E → F → G → H → I → J → K → L → M → N → O
```

| Carpeta | Contenido |
|---|---|
| `0` | Preparación: espera que la app esté arriba y limpia variables. |
| `A` | Login y validación de JWT. |
| `B` | Registro de propietario (solo ADMINISTRADOR). |
| `D` | Creación de restaurante (solo ADMINISTRADOR). |
| `C` | Creación de empleado (solo PROPIETARIO). |
| `E` | Crear y modificar platos. |
| `F` | Habilitar/deshabilitar plato (HU-07). |
| `G` | Listar restaurantes (HU-09). |
| `H` | Listar platos (HU-10). |
| `I` | Crear pedido (HU-11). |
| `J` | Listar pedidos por estado (HU-12). |
| `K` | Asignar pedido (HU-13). |
| `L` | Marcar pedido listo (HU-14). |
| `M` | Consultar PIN (`M0`) y entregar pedido (HU-15). |
| `N` | Cancelar pedido (HU-16). |
| `O` | Consultar trazabilidad (HU-17). |

> **Importante:** la carpeta `M` empieza con `M0 GET PIN del pedido (CLIENTE)`.
> Ese paso usa `tokenCliente`, guarda el PIN en `pinPedido` y es indispensable
> para los casos de entrega. El empleado ya **no** recibe el PIN en la respuesta
> de `PATCH /listo`; la aserción `L4` verifica justamente que no se exponga.

### Variables principales

| Variable | Uso |
|---|---|
| `baseUrl` | URL base, por defecto `http://localhost:8080`. |
| `tokenAdmin`, `tokenProp1`, `tokenCliente`, `tokenEmpleado`, `tokenOtroCliente` | Tokens por rol. |
| `idPropietario`, `idRestaurante`, `idPlato`, `idPedido` | Identificadores creados por la colección. |
| `pinPedido` | PIN obtenido en `M0` y usado en la entrega. |
| `pinIncorrecto` | Caso negativo de entrega. |
| `idPedidoCancelar`, `idPedidoEnPreparacion` | Casos de HU-16. |

## Ejecución con Newman

```powershell
npx newman run "postman/collections/Plazoleta - HU Autenticacion.postman_collection.json" `
  --env-var baseUrl=http://localhost:8080
```

Requiere la aplicación en ejecución y la base de datos creada con
`docs/script.sql` (o migrada con `docs/migration-sprint3.sql`).

## Colección obsoleta

```text
postman/collections/Plazoleta - HU 12-13-14.postman_collection.json
```

Se conserva solo como referencia histórica. **No usar para la demo ni para la
evaluación**: no incluye HU-15, HU-16 ni HU-17, y no tiene el flujo de consulta
del PIN por parte del cliente. La colección canónica la reemplaza por completo.
