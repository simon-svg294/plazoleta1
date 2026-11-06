# Sprint 3 — Retrospectiva

> **Naturaleza de este documento.** Es un **borrador de retrospectiva** elaborado
> el 2026-11-04 a partir de la evidencia del repositorio. No existía un acta
> previa. El equipo debe validarlo en la reunión de cierre y ajustar las acciones.

## 1. Qué salió bien

| Hecho | Evidencia |
|---|---|
| Se completaron las cinco HU del sprint (HU-13 a HU-17). | PR #18, #20, #21 y #22 mergeados a `develop`. |
| El flujo del pedido quedó cerrado de punta a punta. | Crear → listar → asignar → listo → consultar PIN → entregar → cancelar → trazabilidad. |
| Se corrigieron brechas de seguridad detectadas en la revisión. | PIN fuera del DTO del empleado; endpoint de PIN solo para el cliente; categorías solo para administrador. |
| La concurrencia se atacó con una solución estándar. | `@Version` en `Pedido` y respuesta `409` para conflictos. |
| Las pruebas dejaron de depender de MySQL. | Perfil H2 + 50 pruebas en verde con `.\mvnw.cmd test`. |
| Se agregó documentación técnica útil para la sustentación. | Arquitectura, diccionario de datos, diagramas y colección Postman. |
| Se usaron patrones verificables. | Singleton (beans), Builder, Repository, Strategy (`SmsService`) y Observer (`PedidoListoEvent`). |

## 2. Qué no salió bien

| Problema | Impacto | Evidencia |
|---|---|---|
| Commits acumulados al final del sprint. | Riesgo alto de última hora y poca revisión real. | 6 de 21 commits de desarrollo el 2–3 de noviembre. |
| Formato de commit inconsistente. | Incumple la rúbrica de Git. | Solo 3 de 21 commits cumplen `tipo(modulo): descripcion [HU-xx]`. |
| Frecuencia menor a 3 commits por semana en varias semanas. | Incumple el seguimiento semanal exigido. | Semanas del 19 y 26 de octubre con 1–2 commits por integrante. |
| PRs sin revisión cruzada efectiva. | Menos detección temprana de errores. | Los PR fueron mergeados por el mismo equipo, sin comentarios registrados. |
| Se sigue usando un solo despliegue. | Brecha frente al requisito de microservicios. | Un único `pom.xml`, un único `PlazoletaApplication`. |
| El SMS es simulado. | La notificación no llega a un teléfono real. | `SmsServiceImpl` escribe en el log. |
| Documentación y tablero se crearon al cierre. | Poca trazabilidad del proceso. | No había `docs/scrum/` ni tablero antes del 4 de noviembre. |
| Falta de integración continua. | Las pruebas se ejecutan manualmente. | No hay workflow de CI en el repositorio. |

## 3. Lecciones aprendidas

1. **Integrar temprano**: una HU debe mergearse en cuanto cumple su DoD, no al
   final del sprint.
2. **El formato de commit es parte del entregable**: se debe validar antes del
   push, no después.
3. **La seguridad se prueba con casos negativos**: el PIN expuesto se detectó en
   revisión, no en las pruebas originales.
4. **La base de datos es parte del código**: sin migración, la app no arranca
   después de un cambio de esquema.
5. **Un monolito modular es válido como paso intermedio**, pero no reemplaza el
   requisito de microservicios.

## 4. Acciones de mejora

| # | Acción | Responsable | Fecha objetivo | Cómo se verifica |
|---|---|---|---|---|
| A1 | Usar la plantilla `tipo(modulo): descripcion [HU-xx]` en todos los commits. | Ambos integrantes | Inmediato | `git log --format="%s"` sin excepciones. |
| A2 | Hacer al menos 3 commits por integrante por semana, en días distintos. | Ambos integrantes | Cada semana | Reporte semanal de `git shortlog`. |
| A3 | Abrir PR pequeños y revisar el del compañero antes del merge. | Ambos integrantes | Próximo sprint | Comentario de revisión en cada PR. |
| A4 | Agregar un workflow de CI que ejecute `.\mvnw.cmd test`. | Equipo | Antes del próximo corte | Estado verde en GitHub Actions. |
| A5 | Extraer Notificaciones como primer microservicio. | Equipo | Próximo sprint | Servicio independiente con su propio contrato OpenAPI. |
| A6 | Integrar Twilio real detrás de `SmsService`. | Equipo | Próximo sprint | SMS recibido en un celular de prueba. |
| A7 | Medir cobertura con JaCoCo y fijar un umbral. | Equipo | Próximo sprint | Reporte `target/site/jacoco`. |
| A8 | Crear el tag `v3.0.0` al cerrar el sprint. | Líder técnico | Cierre | `git tag` muestra la versión. |

## 5. Compromisos para la demo del 9 de noviembre

- [x] Commitear las correcciones de cierre **antes** del domingo.
- [ ] Ejecutar `.\mvnw.cmd test` y guardar la salida como evidencia.
- [ ] Probar la colección Postman completa contra MySQL.
- [ ] Ensayar el guion de demo con tiempos.
- [ ] Preparar la explicación honesta de las brechas (microservicios, Twilio,
      historial Git) y de la ruta de migración.
- [ ] Verificar que `docs/migration-sprint3.sql` se aplicó en la base de la demo.

## 6. Dinámica sugerida para la reunión de cierre (15 min)

1. **5 min** — Cada integrante dice qué salió bien y qué no (sin culpar).
2. **5 min** — Se priorizan A1, A2 y A3 como acciones immediatas.
3. **5 min** — Se asignan responsables y fechas y se firma este documento.
