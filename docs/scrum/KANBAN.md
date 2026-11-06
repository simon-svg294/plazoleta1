# Tablero Kanban — Sprint 3

> **Naturaleza de este documento.** El repositorio no tenía tablero Kanban ni
> actas de ceremonias. Este tablero se reconstruye el **2026-11-04** a partir de
> la evidencia real de Git (commits, ramas y PR registrados en GitHub). No se inventan fechas de
> reuniones ni responsables distintos a los que firman los commits. Actualizado
> al cierre del **2026-11-06**.

## 1. Columnas y política de flujo

| Columna | Significado | Límite WIP |
|---|---|---|
| **Backlog** | Historias priorizadas para el sprint. | — |
| **To Do** | Lista para iniciar; criterios de aceptación claros. | 5 |
| **In Progress** | En desarrollo en una rama `feature/HU-xx-...`. | 2 |
| **In Review** | Pull Request abierto hacia `develop`. | 2 |
| **Done** | Mergeado a `develop`, probado y documentado. | — |

Regla de flujo: una tarjeta solo pasa a **Done** cuando cumple la Definición de
Terminado (sección 4). Los PR se integran con *merge commit*, conservando la
rama y el historial.

## 2. Tarjetas del Sprint 3

| ID | Tarjeta | Responsable (según Git) | Rama | Evidencia | Estado |
|---|---|---|---|---|---|
| HU-13 | Asignar pedido y pasar a `EN_PREPARACION` | `ardilamelina8-blip` | `feature/HU-13-14-gestion-pedidos` | `b655af5` (PR #18) | Done |
| HU-14 | Notificar pedido listo y generar PIN | `ardilamelina8-blip` | `feature/HU-13-14-gestion-pedidos` | `b655af5` (PR #18) | Done |
| HU-15 | Entregar pedido validando PIN | `ardilamelina8-blip` | `feature/HU-15-entregar-pedido` | `5884b27` (PR #20) | Done |
| HU-16 | Cancelar pedido solo si está `PENDIENTE` | `ardilamelina8-blip` | `feature/HU-16-cancelar-pedido` | `7ebfc14` (PR #21) | Done |
| HU-17 | Consultar trazabilidad del pedido | `ardilamelina8-blip` | `feature/HU-17-trazabilidad` | `336821b` (PR #22) | Done |
| T-01 | Endpoint de PIN solo para el cliente | Simon | `develop` (commit de cierre) | `d51f6c2` | Done |
| T-02 | Bloqueo optimista en `Pedido` | Simon | `develop` (commit de cierre) | `d51f6c2` | Done |
| T-03 | Manejo centralizado de errores | Simon | `develop` (commit de cierre) | `c3ddd7d` | Done |
| T-04 | Pruebas unitarias de registro de usuario | Simon | `develop` (commit de cierre) | `c3ddd7d` | Done |
| T-05 | Optimización de consultas (N+1, índices, paginación) | Simon | `develop` (commit de cierre) | `692e422` | Done |
| T-06 | OpenAPI, Postman y diagramas | Simon | `develop` (commit de cierre) | commit `docs(sprint)` del 2026-11-06 | Done |
| T-07 | Separación física en microservicios | Equipo | — | solo ruta documentada | Backlog |
| T-08 | SMS real con Twilio | Equipo | — | `SmsServiceImpl` simulado | Backlog |

## 3. Flujo real observado

```text
Backlog ──> To Do ──> In Progress ──> In Review (PR) ──> Done (merge a develop)
   │                                       │
   │                                       └── cambios solicitados ──> In Progress
   │
   └── HU-18 (fuera de alcance) y microservicios quedan en Backlog
```

## 4. Definición de Terminado (DoD)

Una tarjeta se considera terminada solo si:

- [x] El endpoint responde con el contrato documentado en Swagger.
- [x] Las reglas de negocio y los roles están validados en el servicio y en
      `SecurityConfig`.
- [x] Existe al menos una prueba automatizada o un caso Postman que cubre el
      camino feliz y un error.
- [x] El código compila con `.\mvnw.cmd test`.
- [x] La documentación de la HU está actualizada.
- [x] El commit sigue `tipo(modulo): descripcion corta [HU-xx]` en los commits
      de cierre del 2026-11-06.
- [ ] El PR fue revisado por alguien distinto de quien lo escribió (deuda
      histórica; los commits de cierre se integraron directamente a `develop`).

> Los dos últimos puntos **no se cumplieron** en la mayoría de los commits del
> repositorio. Se documentan como deuda del proceso en
> `docs/scrum/PARTICIPACION.md`; no se reescribe el historial.

## 5. Bloqueos y deuda visible

| Bloqueo | Impacto | Mitigación |
|---|---|---|
| El proyecto sigue siendo un solo despliegue Spring Boot. | No cumple literalmente “microservicios”. | Contextos separados por paquete + ruta de migración en `docs/ARQUITECTURA.md`. |
| SMS simulado (log), sin Twilio. | La HU-14 no envía un SMS real. | La interfaz `SmsService` permite sustituir la implementación sin tocar el dominio. |
| Historial Git con formato y frecuencia irregulares. | No cumple la rúbrica de Git Flow. | Reporte honesto y acciones correctivas; prohibido reescribir o backdatear. |
| Sin tablero ni actas previas. | Evidencia Scrum incompleta. | Este tablero y los documentos de sprint generados al cierre. |

## 6. Cómo usar este tablero en la sustentación

1. Mostrar la tabla de tarjetas y su evidencia (hash + PR).
2. Abrir `git log --graph --oneline --all` y contrastar.
3. Explicar la deuda (microservicios, Twilio, historial) sin ocultarla.
4. Presentar las acciones de mejora de la retrospectiva.
