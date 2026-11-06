# Plazoleta

API REST para la gestión de una plazoleta de comida. Cubre el ciclo completo de
un pedido: creación por el cliente, listado y asignación por el empleado,
notificación con PIN, entrega, cancelación y trazabilidad.

**Sprint 3:** HU-13, HU-14, HU-15, HU-16 y HU-17 completas. HU-18 fuera de
alcance.

## Stack

| Componente | Versión / tecnología |
|---|---|
| Lenguaje | Java 24 |
| Framework | Spring Boot 4.1.1 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | MySQL 8 |
| Seguridad | Spring Security + JWT (JJWT) + BCrypt |
| Documentación | springdoc OpenAPI / Swagger UI |
| Pruebas | JUnit 5 + Mockito + H2 |
| Build | Maven Wrapper (`mvnw`) |

## Arquitectura

El proyecto se entrega como **un único despliegue Spring Boot** organizado en
cuatro contextos delimitados: Usuarios, Plazoleta, Trazabilidad y Notificaciones.
La separación física en microservicios está documentada como ruta de migración,
no implementada todavía.

- Diagramas de arquitectura, clases, entidades y secuencias:
  [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md)
- Diccionario de datos: [`docs/DiccionarioDatos.md`](docs/DiccionarioDatos.md)
- Evidencias Scrum: [`docs/scrum/`](docs/scrum/)
- Git Flow y cierre del sprint: [`docs/GITFLOW.md`](docs/GITFLOW.md)

## Requisitos

- Java 24 (JDK 24 o superior).
- MySQL 8 en ejecución.
- No necesitas Maven instalado: el proyecto incluye `mvnw.cmd` (Windows) y
  `mvnw` (Linux/macOS).

## Puesta en marcha

### 1. Crear la base de datos

Para una base nueva:

```powershell
mysql -u root -p < docs/script.sql
```

El script crea `plazoleta_db`, las siete tablas, los índices y los datos semilla.
No elimina la base completa.

Si ya tienes una base del Sprint 2, aplica solo la migración:

```powershell
mysql -u root -p plazoleta_db < docs/migration-sprint3.sql
```

> Es obligatorio aplicar la migración antes de arrancar: `ddl-auto=validate`
> espera la columna `pedido.version`.

### 2. Configurar variables de entorno

Copia `.env.example` y define tus valores, o expórtalos en la terminal:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/plazoleta_db?serverTimezone=America/Bogota&allowPublicKeyRetrieval=true&useSSL=false"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="tu_clave"
$env:JWT_SECRET="una-clave-propia-de-al-menos-32-caracteres"
```

| Variable | Obligatoria en producción | Valor por defecto (desarrollo) |
|---|---|---|
| `DB_URL` | Sí | `jdbc:mysql://localhost:3306/plazoleta_db?...` |
| `DB_USERNAME` | Sí | `root` |
| `DB_PASSWORD` | Sí | `root` |
| `JWT_SECRET` | Sí | Clave de desarrollo (no usar en producción) |
| `JWT_EXPIRATION` | No | `3600000` ms (1 hora) |
| `JPA_SHOW_SQL` | No | `false` |
| `SECURITY_LOG_LEVEL` | No | `INFO` |

### 3. Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación queda en `http://localhost:8080`.

### 4. Abrir Swagger

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Usa **Authorize** con el token obtenido en `POST /api/v1/auth/login`.

## Usuarios semilla

| Rol | Correo | Clave |
|---|---|---|
| ADMINISTRADOR | `admin@plazoleta.com` | `Admin123*` |
| CLIENTE | `cliente@plazoleta.com` | `Admin123*` |
| EMPLEADO | `empleado@plazoleta.com` | `Admin123*` |
| PROPIETARIO | `propietario@plazoleta.com` | `Admin123*` |

El empleado y el propietario están asociados al restaurante demo
`900000000-1`. Cambia estas claves antes de cualquier despliegue real.

## Endpoints

### Autenticación

| Método | Ruta | Rol | HU |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Público | HU-05 |

### Usuarios

| Método | Ruta | Rol | HU |
|---|---|---|---|
| POST | `/api/v1/usuarios/registro-propietario` | ADMINISTRADOR | HU-01 |
| POST | `/api/v1/usuarios/empleado` | PROPIETARIO | HU-06 |
| POST | `/api/v1/usuarios/cliente` | Público | HU-08 |

### Restaurantes y categorías

| Método | Ruta | Rol | HU |
|---|---|---|---|
| POST | `/api/v1/restaurantes` | ADMINISTRADOR | HU-02 |
| GET | `/api/v1/restaurantes?pagina=0&tamano=10` | CLIENTE | HU-09 |
| POST | `/api/v1/categorias/registro` | ADMINISTRADOR | — |
| GET | `/api/v1/categorias` | Autenticado | — |

### Platos

| Método | Ruta | Rol | HU |
|---|---|---|---|
| POST | `/api/v1/platos/registro` | PROPIETARIO | HU-03 |
| PATCH | `/api/v1/platos/{idPlato}` | PROPIETARIO | HU-04 |
| PATCH | `/api/v1/platos/{idPlato}/estado` | PROPIETARIO | HU-07 |
| GET | `/api/v1/platos?idRestaurante=1&idCategoria=1&pagina=0&tamano=10` | CLIENTE | HU-10 |

### Pedidos

| Método | Ruta | Rol | HU |
|---|---|---|---|
| POST | `/api/v1/pedidos` | CLIENTE | HU-11 |
| GET | `/api/v1/pedidos?estado=PENDIENTE&pagina=0&tamano=10` | EMPLEADO | HU-12 |
| PATCH | `/api/v1/pedidos/{id}/asignar` | EMPLEADO | HU-13 |
| PATCH | `/api/v1/pedidos/{id}/listo` | EMPLEADO | HU-14 |
| GET | `/api/v1/pedidos/{id}/pin` | CLIENTE dueño | HU-14 / HU-15 |
| PATCH | `/api/v1/pedidos/{id}/entregar` | EMPLEADO | HU-15 |
| PATCH | `/api/v1/pedidos/{id}/cancelar` | CLIENTE dueño | HU-16 |
| GET | `/api/v1/pedidos/{id}/trazabilidad` | CLIENTE dueño | HU-17 |

Notas de seguridad:

- El PIN **nunca** se devuelve al empleado en `PATCH /listo`.
- El cliente obtiene su PIN en `GET /{id}/pin` y solo si el pedido está `LISTO`.
- El empleado solo opera pedidos de su restaurante.
- El cliente solo consulta sus propios pedidos.

## Flujo de estados

```text
PENDIENTE ──asignar (EMPLEADO)──> EN_PREPARACION ──listo (EMPLEADO)──> LISTO
    │                                                                   │
    │ cancelar (CLIENTE)                                    entregar (PIN)
    ▼                                                                   ▼
CANCELADO                                                           ENTREGADO
```

Cada transición se registra en `pedido_estado_log` para la trazabilidad (HU-17).

## Patrones de diseño

| Patrón | Implementación |
|---|---|
| **Singleton** | Beans de Spring (`@Service`, `@Repository`, `@RestController`). |
| **Builder** | Lombok `@Builder` en entidades y DTOs. |
| **Repository** | Interfaces Spring Data JPA. |
| **Strategy** | `SmsService` + `SmsServiceImpl` (log o Twilio). |
| **Observer / Event** | `PedidoListoEvent` + `PedidoListoNotificacionListener` con `AFTER_COMMIT`. |
| **DTO / Mapper** | Paquete `dto` y métodos de conversión. |

Detalle y diagramas en [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md).

## Pruebas

```powershell
.\mvnw.cmd test
```

- 50 pruebas automatizadas (unitarias + carga de contexto).
- Usan un perfil H2 (`src/test/resources/application-test.properties`), por lo
  que **no requieren MySQL**.
- Incluyen `UsuarioServiceImplTest` con 10 casos de registro de usuario
  (propietario, empleado y cliente) y `PedidoServiceImplTest` con el flujo de
  PIN, entrega, cancelación y transiciones de estado.
- Mockito imprime una advertencia de *self-attaching* en JDK 25; no afecta el
  resultado de las pruebas.

### Postman

La colección canónica es:

```text
postman/collections/Plazoleta - HU Autenticacion.postman_collection.json
```

Ejecutar las carpetas en este orden:

```text
0 → A → B → D → C → E → F → G → H → I → J → K → L → M → N → O
```

La carpeta **M** incluye `M0 GET PIN del pedido (CLIENTE)` antes de los casos de
entrega. Ver [`postman/README.md`](postman/README.md).

Con Newman:

```powershell
npx newman run "postman/collections/Plazoleta - HU Autenticacion.postman_collection.json" --env-var baseUrl=http://localhost:8080
```

## Estructura del proyecto

```text
src/main/java/com/plazoletaucc/Plazoleta/
├── config/          OpenAPI
├── controller/      Endpoints REST
├── dto/             Request y response
├── entity/          Entidades JPA
├── event/           Evento y listener de notificación
├── exception/       Excepciones y GlobalExceptionHandler
├── repository/      Repositorios Spring Data
├── security/        JWT, filtro y reglas por rol
└── service/         Interfaces e implementaciones

docs/
├── ARQUITECTURA.md
├── DiccionarioDatos.md
├── GITFLOW.md
├── script.sql
├── migration-sprint3.sql
└── scrum/           Kanban, planning, review, retrospectiva y participación

postman/             Colección por HU
```

## Documentación

| Documento | Contenido |
|---|---|
| [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) | Contextos, patrones, diagramas Mermaid y ruta a microservicios. |
| [`docs/DiccionarioDatos.md`](docs/DiccionarioDatos.md) | Tablas, columnas, restricciones e índices. |
| [`docs/GITFLOW.md`](docs/GITFLOW.md) | Ramas, convención de commits y comandos de cierre. |
| [`docs/scrum/KANBAN.md`](docs/scrum/KANBAN.md) | Tablero y Definición de Terminado. |
| [`docs/scrum/SPRINT-3-PLANNING.md`](docs/scrum/SPRINT-3-PLANNING.md) | Alcance, criterios y riesgos. |
| [`docs/scrum/SPRINT-3-REVIEW.md`](docs/scrum/SPRINT-3-REVIEW.md) | Incremento y guion de demo. |
| [`docs/scrum/RETROSPECTIVA.md`](docs/scrum/RETROSPECTIVA.md) | Lecciones y acciones de mejora. |
| [`docs/scrum/PARTICIPACION.md`](docs/scrum/PARTICIPACION.md) | Métricas reales de Git. |

## Limitaciones conocidas

1. **No hay microservicios separados**: es un monolito modular. La ruta de
   migración está en `docs/ARQUITECTURA.md`, sección 7.
2. **SMS simulado**: `SmsServiceImpl` escribe el mensaje en el log. La interfaz
   permite conectar Twilio u otro proveedor.
3. **Historial Git irregular**: 9 de 27 commits de desarrollo cumplen el formato
   exigido (incluidos los 6 commits de cierre) y la frecuencia semanal no se
   cumple en todas las semanas. No se reescribió el historial.
4. **Sin integración continua**: las pruebas se ejecutan manualmente.
5. **Cobertura no medida**: no se reporta un porcentaje que no se haya calculado.

## Equipo y entrega

- Evaluación: **lunes 9 de noviembre de 2026**, 6:10 a. m. (asistencia y
  evaluación individual); sustentaciones desde las 7:00 a. m., máximo 20 minutos.
- Integrantes identificados en Git: `ardilamelina8-blip` y Simon
  (`simon-svg294`).
- Repositorio: `develop` y `main` integrados el 2026-11-06; tag de cierre
  `v3.0.0`.
