# Git Flow y cierre del Sprint 3

> Estado verificado el **2026-11-04**. Este documento describe el flujo usado, la
> brecha real frente a la rúbrica y los comandos que **el equipo debe ejecutar**
> para cerrar el sprint. El agente de revisión no ejecutó commits, push ni merge.

## 1. Modelo de ramas

```text
main ─────────────●──────────────────────────────●──────────────> (producción)
                   \                            /
develop ────────────●────●────●────●────●──────●───────────────> (integración)
                      \  /    \  /    \  /
feature/HU-xx ─────────●──────●──────●──────●
```

Reglas del equipo:

- `main`: solo versiones estables y etiquetadas.
- `develop`: integración continua del sprint.
- `feature/HU-xx-descripcion`: una rama por historia.
- `fix/descripcion`: correcciones puntuales.
- Integración mediante **Pull Request** con merge commit hacia `develop`.
- Al cerrar el sprint: PR de `develop` a `main`, tag `v3.0.0` y volver a
  `develop`.

## 2. Estado actual real

| Elemento | Estado |
|---|---|
| Rama actual | `develop` |
| Último commit de `develop` | Commit de documentación del 2026-11-06 |
| Último commit de `main` | Merge de cierre `v3.0.0` |
| `develop` contiene HU-13 a HU-17 | Sí |
| `main` contiene HU-13 a HU-17 | Sí, tras el merge de cierre |
| Ramas `release/*` | Ninguna (cierre directo `develop` → `main`) |
| Tags | `v3.0.0` |
| PR mergeados | 17 (numeración de GitHub hasta #22) |
| Commits totales en `develop` | 48 (21 merges + 27 de desarrollo) |
| Cambios sin commitear | Ninguno |

## 3. Convención de commits

```text
tipo(modulo): descripcion corta [HU-xx]
```

Tipos permitidos: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `style`,
`perf`.

Ejemplos correctos:

```text
feat(trazabilidad): consultar historial de estados del pedido [HU-17]
fix(pedidos): ocultar el PIN al empleado [HU-14]
test(usuarios): cubrir registro de propietario y cliente [HU-08]
docs(sprint): agregar actas y tablero kanban [HU-17]
```

Ejemplos incorrectos (presentes en el historial):

```text
first commit
feat HU 11 Realizar pedido
 Habilitar/Deshabilitar Plato
```

Cumplimiento real: **9 de 27 commits de desarrollo (33,3 %)**, incluidos los 6
commits de cierre del 2026-11-06.

## 4. Commits de cierre aplicados

| Commit | Mensaje |
|---|---|
| `d51f6c2` | `fix(pedidos): ocultar PIN al empleado y proteger el ciclo del pedido [HU-14]` |
| `c3ddd7d` | `fix(errores): unificar excepciones y validar registro de usuarios [HU-08]` |
| `692e422` | `perf(consultas): reducir N+1 y limitar el tamano de pagina [HU-10]` |
| `f187608` | `fix(categorias): devolver id y validar NIT con digito de verificacion [HU-03]` |
| `9ac69db` | `chore(config): externalizar credenciales y probar con H2 [HU-05]` |
| commit de documentación | `docs(sprint): documentar arquitectura, datos y evidencias scrum [HU-17]` |

Los seis commits cumplen el formato exigido y el árbol de trabajo queda limpio.

## 5. Cierre del sprint (ejecutado el 2026-11-06)

No se usó `gh` porque no está instalado en el equipo; el cierre se hace con
merge local `--no-ff` y push a `origin`.

```powershell
# 1. Verificar que las pruebas pasan
.\mvnw.cmd test

# 2. Publicar develop
git push origin develop

# 3. Integrar en main y etiquetar (merge local, sin PR)
git checkout main
git pull origin main
git merge --no-ff develop -m "release(sprint-3): integrar HU-13 a HU-17"
git tag -a v3.0.0 -m "Sprint 3: ciclo de pedidos y trazabilidad"
git push origin main
git push origin v3.0.0

# 4. Volver a develop
git checkout develop
git pull origin develop
```

## 6. Verificación posterior

```powershell
git log --graph --oneline --all --decorate
git tag
git branch -a
git status
```

Debe verse:

- `main` contiene todo el historial de `develop` más el merge de cierre.
- El tag `v3.0.0` apuntando al merge de release.
- El árbol de trabajo limpio.

## 7. Qué no se debe hacer

- **No** reescribir el historial (`rebase -i`, `filter-branch`, `push --force`)
  para simular el formato o la frecuencia de commits.
- **No** cambiar las fechas de commits (`GIT_COMMITTER_DATE`) para aparentar
  trabajo semanal.
- **No** crear commits vacíos para llegar a la métrica de 3 por semana.
- **No** mergear sin haber ejecutado las pruebas.

La rúbrica evalúa el proceso; falsearlo es peor que declarar la brecha.
