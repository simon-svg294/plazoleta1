# Sprint 3 — Planning

> **Naturaleza de este documento.** No existía un acta de planning en el
> repositorio. Este documento se elabora el **2026-11-04** para dejar por escrito
> el alcance que realmente se trabajó. La fecha original de la reunión de
> planning no está registrada y no se inventa.

## 1. Objetivo del sprint

Completar el ciclo operativo de un pedido en la plazoleta —asignación,
preparación, notificación, entrega, cancelación y trazabilidad— con reglas de
negocio, seguridad por rol y evidencia de pruebas, dejando el sistema listo para
la demo del **lunes 9 de noviembre de 2026**.

## 2. Alcance comprometido

| HU | Historia | Prioridad | Criterio de aceptación principal |
|---|---|---|---|
| HU-13 | Asignar pedido | Must | Un empleado del restaurante se asigna un pedido `PENDIENTE` y este pasa a `EN_PREPARACION`. |
| HU-14 | Notificar pedido listo | Must | Un empleado del restaurante marca un pedido `EN_PREPARACION` como `LISTO`; se genera un PIN de 4 dígitos y se envía al cliente. |
| HU-15 | Entregar pedido | Must | El empleado entrega un pedido `LISTO` solo con el PIN correcto del cliente. |
| HU-16 | Cancelar pedido | Must | El cliente cancela su propio pedido solo si está `PENDIENTE`; en otro estado recibe el mensaje definido. |
| HU-17 | Trazabilidad | Must | El cliente consulta el historial de estados de sus propios pedidos. |

**Fuera de alcance acordado:**

- **HU-18**: excluida explícitamente por el equipo para este sprint.
- Separación física en microservicios: se documenta la ruta, no se ejecuta.
- SMS real con Twilio: se acepta la implementación simulada.
- Notificaciones push o correo.

## 3. Requisitos transversales del sprint

| Requisito | Entregable |
|---|---|
| Arquitectura | Contextos delimitados documentados y diagramas Mermaid. |
| Seguridad | JWT, autorización por rol y validación de pertenencia del recurso. |
| Errores | Respuestas consistentes `{"mensaje": "..."}` y códigos HTTP correctos. |
| Base de datos | Script consolidado y migración para bases existentes. |
| Pruebas | Pruebas unitarias, en especial del registro de usuario. |
| API | OpenAPI/Swagger y colección Postman por HU. |
| Patrones | Singleton (beans Spring) y al menos otro patrón demostrable. |
| Git Flow | Ramas `feature/HU-xx`, PR hacia `develop`. |
| Documentación | README, arquitectura, diccionario de datos y evidencias Scrum. |

## 4. Historias de usuario (resumen)

### HU-13 — Asignar pedido

- **Como** empleado, **quiero** asignarme un pedido pendiente de mi restaurante,
  **para** iniciar su preparación.
- **Dado** un pedido `PENDIENTE` del restaurante del empleado, **cuando** el
  empleado se asigna, **entonces** el pedido queda `EN_PREPARACION` con el
  empleado registrado y se crea un log de trazabilidad.
- **Error**: pedido inexistente (`404`), pedido de otro restaurante o estado
  inválido (`400`/`403`).

### HU-14 — Notificar pedido listo

- **Como** empleado, **quiero** marcar un pedido en preparación como listo,
  **para** avisar al cliente que puede reclamarlo.
- **Dado** un pedido `EN_PREPARACION` del restaurante del empleado, **cuando** lo
  marca listo, **entonces** el pedido queda `LISTO`, se genera un PIN de 4 dígitos
  y se envía al celular del cliente.
- **Seguridad**: la respuesta al empleado **no** incluye el PIN.
- **Cliente**: puede consultar su PIN con `GET /api/v1/pedidos/{id}/pin`.

### HU-15 — Entregar pedido

- **Como** empleado, **quiero** entregar el pedido digitando el PIN del cliente,
  **para** confirmar que quien lo recibe es el dueño.
- **Dado** un pedido `LISTO` con PIN, **cuando** el empleado ingresa el PIN
  correcto, **entonces** el pedido queda `ENTREGADO`.
- **Error**: PIN incorrecto, pedido no `LISTO`, pedido de otro restaurante.

### HU-16 — Cancelar pedido

- **Como** cliente, **quiero** cancelar mi pedido mientras esté pendiente,
  **para** arrepentirme antes de que empiece la preparación.
- **Dado** un pedido propio `PENDIENTE`, **cuando** lo cancelo, **entonces** queda
  `CANCELADO`.
- **Error**: si ya está en preparación, responde `400` con el mensaje:
  `Lo sentimos, tu pedido ya está en preparación y no puede cancelarse`.

### HU-17 — Trazabilidad

- **Como** cliente, **quiero** ver el historial de estados de mi pedido,
  **para** saber qué ocurrió y cuándo.
- **Dado** un pedido propio, **cuando** consulto la trazabilidad, **entonces**
  recibo la lista cronológica de cambios con estado anterior, estado nuevo,
  empleado y fecha.
- **Error**: pedido de otro cliente (`403`), pedido inexistente (`404`).

## 5. Estimación y capacidad

No existe registro de una sesión de estimación. Para no inventar puntos de
historia, la priorización se expresa con **MoSCoW** y el avance se mide con
commits y PR reales (ver `docs/scrum/PARTICIPACION.md`).

Capacidad observada: dos contribuyentes con identidad en Git. La mayor parte de
las HU-13 a HU-17 fue desarrollada por `ardilamelina8-blip` en la semana del
2 de noviembre, lo que concentró el riesgo al final del sprint.

## 6. Riesgos identificados

| Riesgo | Probabilidad | Impacto | Mitigación aplicada |
|---|---|---|---|
| Concentrar HU al final del sprint | Alta | Alto | Cierre anticipado de HU-16 y HU-17 y correcciones de cierre el 4 de noviembre. |
| Cambios de estado concurrentes sobre el mismo pedido | Media | Alto | Bloqueo optimista con `@Version` y respuesta `409`. |
| Exponer el PIN al empleado | Media | Alto | PIN fuera del DTO del empleado; endpoint exclusivo del cliente. |
| SMS falla y revierte el pedido | Media | Medio | Evento `AFTER_COMMIT`; el fallo del SMS solo se registra. |
| Base de datos existente sin cambios de esquema | Alta | Alto | `docs/migration-sprint3.sql` y `docs/script.sql` actualizados. |
| Demo sin MySQL disponible | Media | Medio | Perfil de pruebas con H2 para `.\mvnw.cmd test`. |
| Rúbrica exige microservicios | Alta | Alto | Contextos separados y ruta de migración documentada; brecha declarada. |

## 7. Entregables verificables al cierre

- [x] Endpoints HU-13 a HU-17 implementados y protegidos por rol.
- [x] PIN visible solo para el cliente dueño del pedido.
- [x] Trazabilidad persistida en `pedido_estado_log`.
- [x] Migración de base de datos.
- [x] `.\mvnw.cmd test` en verde (50 pruebas).
- [x] Colección Postman con casos felices y de error.
- [x] OpenAPI/Swagger.
- [x] Diagramas de arquitectura, clases, entidades y secuencias.
- [x] README y diccionario de datos.
- [ ] Microservicios separados físicamente.
- [ ] SMS real con Twilio.
- [ ] Historial Git conforme a la rúbrica.
