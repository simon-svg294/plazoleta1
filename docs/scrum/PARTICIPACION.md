# Participación del equipo — evidencia real de Git

> Datos obtenidos con `git log develop` el **2026-11-04**. No se inventan
> integrantes, fechas ni commits. Este documento existe porque la rúbrica exige
> evidencia de participación y el repositorio no tenía un reporte.

## 1. Integrantes identificados en Git

| Identidad | Correos registrados | Commits de desarrollo | Rol observado |
|---|---|---|---|
| `ardilamelina8-blip` | `ardilamelina8@gmail.com` | 12 | Autenticación, platos, restaurantes y HU-13 a HU-17. |
| `Simon` / `simon-svg294` | `restrepoosoriosmion@gmail.com`, `restrepoosoriosimon@gmail.com` | 15 | HU-01 a HU-04, HU-06, HU-08, HU-11, HU-12 y commits de cierre del Sprint 3. |

**Nota:** `Simon` y `simon-svg294` son la misma persona con dos correos
configurados. Se cuentan juntos para no inflar la participación. Se recomienda
unificar la configuración de Git:

```powershell
git config --global user.name "Simon Restrepo"
git config --global user.email "restrepoosoriosimon@gmail.com"
```

## 2. Commits por semana (solo commits de desarrollo, sin merges)

| Semana | `ardilamelina8-blip` | Simon | Total | ¿Cada uno ≥ 3? |
|---|---:|---:|---:|---|
| 12–18 oct 2026 | 2 | 6 | 8 | Simon sí; `ardilamelina8-blip` no |
| 19–25 oct 2026 | 1 | 1 | 2 | **No** |
| 26 oct–1 nov 2026 | 2 | 2 | 4 | **No** |
| 2–8 nov 2026 | 7 | 6 | 13 | Ambos ≥ 3, pero Simon concentró sus 6 en un solo día (6 nov) |
| **Total** | **12** | **15** | **27** | — |

Conclusión honesta: **la regla de 3 commits por semana por integrante no se
cumple en todas las semanas.** La última semana alcanza el número, pero Simon
concentró sus commits en un solo día, así que tampoco cumple el criterio de
“días diferentes”. No se deben crear commits retroactivos para “arreglar” esta
métrica; eso sería falsificar el historial.

## 3. Distribución por historia de usuario

| HU | Responsable según commits | Commit | PR |
|---|---|---|---|
| HU-01 | Simon | `d94062d` | #1 |
| HU-02 | Simon | `fb4ee3a` | #2 |
| HU-03 | Simon | `ca749c2` | #3 |
| HU-04 | Simon | `516dc74` | #4 |
| HU-05 | `ardilamelina8-blip` | `8483eb6` | #5 |
| HU-06 | Simon | `bc62c04` | #7 |
| HU-07 | `ardilamelina8-blip` | `ecbd9bc` | #8 |
| HU-08 | Simon | `92e8eb4` | #9 |
| HU-09 | `ardilamelina8-blip` | `6bc611c` | #11 |
| HU-10 | `ardilamelina8-blip` | `44731b1` | #12 |
| HU-11 | Simon | `d8e9b87` | #13 |
| HU-12 | Simon | `0d75142` | #13 |
| HU-13 | `ardilamelina8-blip` | `b655af5` | #18 |
| HU-14 | `ardilamelina8-blip` | `b655af5` | #18 |
| HU-15 | `ardilamelina8-blip` | `5884b27` | #20 |
| HU-16 | `ardilamelina8-blip` | `7ebfc14` | #21 |
| HU-17 | `ardilamelina8-blip` | `336821b` | #22 |

## 4. Formato de commits

Formato exigido:

```text
tipo(modulo): descripcion corta [HU-xx]
```

Resultado real sobre los 27 commits de desarrollo (incluidos los 6 de cierre):

| Criterio | Cantidad | Porcentaje |
|---|---:|---:|
| Cumplen el formato completo | 9 | 33,3 % |
| Contienen etiqueta `[HU-xx]` | 9 | 33,3 % |
| No cumplen | 18 | 66,7 % |

Commits que sí cumplen:

```text
d51f6c2 fix(pedidos): ocultar PIN al empleado y proteger el ciclo del pedido [HU-14]
c3ddd7d fix(errores): unificar excepciones y validar registro de usuarios [HU-08]
692e422 perf(consultas): reducir N+1 y limitar el tamano de pagina [HU-10]
f187608 fix(categorias): devolver id y validar NIT con digito de verificacion [HU-03]
9ac69db chore(config): externalizar credenciales y probar con H2 [HU-05]
docs(sprint): documentar arquitectura, datos y evidencias scrum [HU-17]
336821b feat(trazabilidad): consultar historial de estados del pedido [HU-17]
7ebfc14 feat(pedidos): cancelar pedido solo en estado pendiente [HU-16]
0d75142 feat(pedido): listar pedidos por estado para el empleado [HU-12]
```

Ejemplos que no cumplen (para explicar la brecha, no para corregirlos):

```text
76fbd9e first commit
d8e9b87 feat HU 11 Realizar pedido
ecbd9bc  Habilitar/Deshabilitar Plato
5884b27 feat: HU-15 entrega de pedidos y documentacion OpenAPI
```

## 5. Ramas y Pull Requests

- Ramas remotas: `main`, `develop`, 16 ramas `feature/HU-*` y 2 ramas `fix/*`.
- Pull Requests mergeados: **17** (numeración de GitHub hasta #22; los números
  #6, #10, #14, #15 y #16 no tienen merge commit en `develop`).
- Ramas `release/*`: **ninguna** (cierre directo `develop` → `main`).
- Tags: **`v3.0.0`**.
- `main` se actualizó el 2026-11-06 con el merge `--no-ff` de cierre; antes
  estaba en `7d0c3f8` (PR #8, HU-07).

Implicación para la entrega: `main` ya contiene HU-08 a HU-17 y el tag `v3.0.0`,
así que el docente verá el Sprint 3 al revisar la rama principal.

## 6. Cómo generar este reporte de nuevo

```powershell
# Commits de desarrollo por autor
git shortlog -sn --no-merges develop

# Commits por semana y autor (PowerShell)
$rows = git log develop --no-merges --format="%an|%ad" --date=short |
  ForEach-Object {
    $p = $_ -split '\|'
    $d = [datetime]$p[1]
    $monday = $d.AddDays(-(([int]$d.DayOfWeek + 6) % 7))
    "$($monday.ToString('yyyy-MM-dd'))|$($p[0])"
  }
$rows | Group-Object | Sort-Object Name

# Cumplimiento del formato
git log develop --no-merges --format="%h|%s" |
  Where-Object { ($_ -split '\|',2)[1] -match '^(feat|fix|docs|test|refactor|chore|style|perf)\([a-z0-9-]+\): .+ \[HU-[0-9]{2}\]$' }
```

## 7. Recomendaciones para el siguiente sprint

1. Unificar nombre y correo de Git en ambos equipos.
2. Configurar una plantilla de commit o un hook `commit-msg`.
3. Hacer commits pequeños y diarios; no acumular al final.
4. Revisar el PR del compañero antes de mergear.
5. Etiquetar cada cierre de sprint (`v3.0.0`, `v4.0.0`).
6. Integrar `develop` en `main` al cerrar, para que la entrega sea visible.
