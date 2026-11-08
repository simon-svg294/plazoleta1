# Plazoleta

API REST para la administración de una plazoleta de comida. Cubre el ciclo
completo del negocio: usuarios y roles, restaurantes, menús, pedidos,
notificaciones con PIN, cancelación y trazabilidad de estados.

**Estado:** HU-01 a HU-17 implementadas. HU-18 fuera de alcance. 50 pruebas
automatizadas en verde y verificación funcional contra MySQL.

## Documentación

Toda la información del proyecto está consolidada en un solo documento:

- **[`docs/DOCUMENTACION.md`](docs/DOCUMENTACION.md)**: alcance, roles,
  arquitectura, patrones, modelo de datos, reglas de negocio, seguridad, API,
  errores, pruebas, Postman, puesta en marcha, Git Flow y evidencias Scrum.
- **[`docs/script.sql`](docs/script.sql)**: base de datos completa (esquema,
  índices y datos semilla).

## Inicio rápido

```powershell
# 1. Crear la base de datos
mysql -u root -p < docs/script.sql

# 2. Configurar credenciales (opcional en desarrollo)
$env:DB_URL="jdbc:mysql://localhost:3306/plazoleta_db?serverTimezone=America/Bogota&allowPublicKeyRetrieval=true&useSSL=false"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="tu_clave"
$env:JWT_SECRET="una-clave-propia-de-al-menos-32-caracteres"

# 3. Ejecutar
.\mvnw.cmd spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

## Usuarios semilla

| Rol | Correo | Clave |
|---|---|---|
| ADMINISTRADOR | `admin@plazoleta.com` | `Admin123*` |
| CLIENTE | `cliente@plazoleta.com` | `Admin123*` |
| EMPLEADO | `empleado@plazoleta.com` | `Admin123*` |
| PROPIETARIO | `propietario@plazoleta.com` | `Admin123*` |

## Pruebas

```powershell
.\mvnw.cmd test
```

## Estructura

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

src/test/            Pruebas JUnit 5 + Mockito + H2
docs/                DOCUMENTACION.md + script.sql
postman/             Colección por HU
```

## Equipo

- Evaluación: lunes 9 de noviembre de 2026, 6:10 a. m.; sustentaciones desde
  las 7:00 a. m., máximo 20 minutos.
- Integrantes: `ardilamelina8-blip` y Simon (`simon-svg294`).
- Tags: `v3.0.0`, `v3.0.1`.
