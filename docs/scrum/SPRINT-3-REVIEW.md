# Sprint 3 — Review

> **Naturaleza de este documento.** No existía acta de la reunión de review. Este
> documento resume el **incremento realmente disponible en el repositorio al
> 2026-11-04**. La demo oficial es el **lunes 9 de noviembre de 2026 a las
> 7:00 a. m.**; la asistencia y la evaluación individual son a las 6:10 a. m.

## 1. Incremento entregado

| HU | Resultado | Evidencia en código | Evidencia Git |
|---|---|---|---|
| HU-13 | El empleado se asigna un pedido `PENDIENTE` de su restaurante y pasa a `EN_PREPARACION`. | `PedidoServiceImpl.asignarPedido`, `PATCH /api/v1/pedidos/{id}/asignar` | `b655af5`, PR #18 |
| HU-14 | El pedido pasa a `LISTO`, se genera PIN y se notifica al cliente. | `marcarPedidoListo`, `PedidoListoEvent`, `PedidoListoNotificacionListener`, `SmsServiceImpl` | `b655af5`, PR #18 |
| HU-15 | El empleado entrega solo con PIN correcto. | `entregarPedido`, `PATCH /api/v1/pedidos/{id}/entregar` | `5884b27`, PR #20 |
| HU-16 | El cliente cancela solo si está `PENDIENTE`. | `cancelarPedido`, `PATCH /api/v1/pedidos/{id}/cancelar` | `7ebfc14`, PR #21 |
| HU-17 | El cliente consulta el historial de estados de sus pedidos. | `TrazabilidadServiceImpl`, `GET /api/v1/pedidos/{id}/trazabilidad` | `336821b`, PR #22 |

## 2. Correcciones de cierre (integradas el 2026-11-06)

Estos cambios quedaron integrados en `develop` con los commits de cierre
`d51f6c2`, `c3ddd7d`, `692e422`, `f187608`, `9ac69db` y el commit de
documentación:

| Corrección | Motivo | Archivos principales |
|---|---|---|
| PIN fuera de la respuesta al empleado | El empleado podía ver el PIN del cliente. | `PedidoListoResponseDTO`, `PedidoServiceImpl` |
| `GET /api/v1/pedidos/{id}/pin` solo para `CLIENTE` | Permitir al cliente obtener su PIN sin exponerlo al empleado. | `PedidoController`, `SecurityConfig` |
| Evento `AFTER_COMMIT` para el SMS | Evitar notificar un pedido que termina en rollback. | `event/` |
| `@Version` en `Pedido` | Evitar transiciones concurrentes inconsistentes. | `Pedido`, `docs/script.sql` |
| `GlobalExceptionHandler` | Unificar errores y cubrir 400/401/403/404/409/500. | `exception/` |
| Validación de documento duplicado | Evitar violaciones de unicidad. | `UsuarioServiceImpl` |
| `id` en la respuesta de categoría | El cliente necesitaba el id para crear platos. | `CategoriaResponseDTO`, `CategoriaServiceImpl` |
| Categorías solo `ADMINISTRADOR` | Cerrar un hueco de autorización. | `SecurityConfig` |
| NIT con dígito de verificación | Permitir `900000000-1`, usado por los datos semilla. | `RestauranteRequestDTO` |
| Paginación con tope de 50 | Evitar consultas desmedidas. | Servicios de pedidos, platos y restaurantes |
| `@EntityGraph` e índices | Reducir N+1 y acelerar el menú. | Repositorios, `docs/script.sql` |
| H2 para pruebas | Ejecutar `.\mvnw.cmd test` sin MySQL. | `pom.xml`, `application-test.properties` |

## 3. Verificación de criterios de aceptación

| Criterio | Cómo se verificó | Resultado |
|---|---|---|
| Empleado de otro restaurante no puede asignar ni entregar | Pruebas unitarias de `PedidoServiceImplTest` | Pasa |
| Pedido en estado inválido no se puede asignar/listo/entregar/cancelar | Pruebas unitarias + Postman | Pasa |
| PIN de 4 dígitos generado con `SecureRandom` | Prueba unitaria de `marcarPedidoListo` | Pasa |
| PIN no se devuelve al empleado | Prueba unitaria + aserción Postman L4 | Pasa |
| Cliente solo consulta su propio PIN | Pruebas unitarias de `consultarPinPedido` | Pasa |
| Cliente solo consulta su propia trazabilidad | `TrazabilidadServiceImplTest` | Pasa |
| Cancelación solo en `PENDIENTE` | Pruebas unitarias + Postman | Pasa |
| Registro de usuario: correo y documento duplicados, menor de edad | `UsuarioServiceImplTest` (10 pruebas) | Pasa |
| Todos los endpoints documentados | Swagger UI y `docs/ARQUITECTURA.md` | Pasa |
| Compilación y pruebas | `.\mvnw.cmd test` | **50 pruebas, 0 fallos, 0 errores** |

## 4. Demostración en vivo (guion de 15–20 minutos)

Orden recomendado para no romper el estado de la base:

1. **Preparación**: `docs/script.sql`, arrancar la app, abrir Swagger.
2. **Autenticación**: login de administrador, propietario, empleado y cliente.
3. **HU-02 / HU-03**: crear restaurante y platos (si la base está vacía).
4. **HU-09 / HU-10**: cliente lista restaurantes y menú.
5. **HU-11**: cliente crea un pedido.
6. **HU-12**: empleado lista pedidos `PENDIENTE`.
7. **HU-13**: empleado se asigna; el pedido pasa a `EN_PREPARACION`.
8. **HU-14**: empleado marca `LISTO`; mostrar en el log el SMS simulado con el PIN.
9. **PIN**: el cliente consulta `GET /api/v1/pedidos/{id}/pin`; el empleado **no**
   ve el PIN en la respuesta de `listo`.
10. **HU-15**: empleado entrega con el PIN; el pedido queda `ENTREGADO`.
11. **HU-16**: crear otro pedido y cancelarlo en `PENDIENTE`; intentar cancelar
    uno en preparación y mostrar el `400` con el mensaje exacto.
12. **HU-17**: cliente consulta la trazabilidad y se ve la secuencia completa.
13. **Errores**: mostrar un `401` sin token, un `403` con rol incorrecto y un
    `409` de concurrencia si se puede reproducir.
14. **Calidad**: `.\mvnw.cmd test`, Swagger y diagramas.

## 5. Métricas del incremento

| Métrica | Valor real |
|---|---|
| Commits totales en `develop` | 48 (21 merges + 27 de desarrollo) |
| Commits de cierre del 2026-11-06 | 6 |
| PR mergeados | 17 (numeración hasta #22) |
| Pruebas automatizadas | 50 |
| Endpoints documentados | 20 |
| HU del sprint completadas | 5 de 5 (HU-13 a HU-17) |
| HU fuera de alcance | HU-18 |
| Cobertura de pruebas | No medida con JaCoCo; no se reporta un porcentaje inventado. |

## 6. Pendientes y brechas declaradas

1. **Microservicios**: sigue siendo un monolito modular. La ruta de separación
   está en `docs/ARQUITECTURA.md`, sección 7.
2. **Twilio**: el SMS es simulado y se escribe en el log. La interfaz permite
   cambiarlo.
3. **Historial Git**: 9 de 27 commits de desarrollo cumplen el formato
   `tipo(modulo): descripcion [HU-xx]` (incluidos los 6 commits de cierre); la
   frecuencia de 3 commits por semana por integrante no se cumple en todas las
   semanas. Ver `docs/scrum/PARTICIPACION.md`.
4. **Cobertura**: no hay reporte de cobertura; no se afirma un porcentaje.
5. **Commit de cierre**: las correcciones se integraron el 2026-11-06 y el
   procedimiento de push, merge a `main` y tag está en `docs/GITFLOW.md`.

## 7. Conclusión del review

El incremento cubre las cinco HU del Sprint 3 y las brechas técnicas más graves
(PIN expuesto, concurrencia, errores inconsistentes, N+1 y pruebas sin MySQL).
La brecha principal frente a la rúbrica es **arquitectónica y de proceso**
(microservicios e historial Git), no funcional. Se presenta con transparencia y
con un plan de migración concreto.
