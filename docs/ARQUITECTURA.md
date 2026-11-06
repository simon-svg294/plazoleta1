# Arquitectura y diseño — Plazoleta

> Documento de apoyo para la sustentación del Sprint 3. Describe la arquitectura
> implementada, los patrones de diseño, el modelo de datos y la ruta de migración
> hacia microservicios.

## 1. Estado actual

El proyecto se entrega como **un único despliegue Spring Boot** organizado por
contextos delimitados (bounded contexts) que coinciden con los microservicios
propuestos en el enunciado:

| Contexto | Paquete principal | Responsabilidad | Historias |
|---|---|---|---|
| Usuarios | `controller`, `service`, `security` | Registro, autenticación, roles y permisos | HU-01, HU-05, HU-06, HU-08 |
| Plazoleta | `controller`, `service`, `entity` | Restaurantes, categorías, platos y pedidos | HU-02, HU-03, HU-04, HU-07, HU-09, HU-10, HU-11, HU-12 |
| Trazabilidad | `service.impl.TrazabilidadServiceImpl`, `entity.PedidoEstadoLog` | Historial de estados y consulta del cliente | HU-13, HU-16, HU-17 |
| Notificaciones | `service.SmsService`, `event.PedidoListoNotificacionListener` | Aviso al cliente y PIN de seguridad | HU-14, HU-15 |

**Decisión consciente:** se priorizó un monolito modular funcional y verificable
para la demo, con los contextos separados por paquetes y contratos de servicio.
La separación física en cuatro procesos es la siguiente fase de la ruta de
migración (sección 7), no un cambio que se pueda completar sin arriesgar la
entrega del lunes.

## 2. Diagrama de arquitectura

```mermaid
flowchart LR
    subgraph Usuarios
        C[Cliente]
        E[Empleado]
        P[Propietario]
        A[Administrador]
    end

    subgraph API["API REST Spring Boot :8080"]
        SEC[SecurityConfig + JWT]
        UC[Usuarios]
        PC[Plazoleta]
        TC[Trazabilidad]
        NC[Notificaciones]
    end

    DB[(MySQL plazoleta_db)]
    SMS[[Proveedor SMS / Twilio opcional]]

    C --> SEC
    E --> SEC
    P --> SEC
    A --> SEC
    SEC --> UC
    SEC --> PC
    SEC --> TC
    UC --> DB
    PC --> DB
    TC --> DB
    PC -->|PedidoListoEvent| NC
    NC -.-> SMS
```

Flujo resumido:

1. `Usuarios` valida credenciales y emite el JWT con el rol.
2. `Plazoleta` gestiona restaurantes, platos y el ciclo del pedido.
3. `Trazabilidad` registra cada transición en `pedido_estado_log`.
4. `Notificaciones` recibe el evento `PedidoListoEvent` después del commit y
   envía el PIN al cliente (en desarrollo se escribe en el log).

## 3. Patrones de diseño aplicados

| Patrón | Dónde | Para qué |
|---|---|---|
| **Singleton** | Beans de Spring (`@Service`, `@Repository`, `@RestController`) | Una sola instancia por contenedor; el contenedor gestiona el ciclo de vida. |
| **Builder** | Entidades y DTOs con Lombok `@Builder` | Construcción legible de objetos con muchos campos. |
| **Repository** | `UsuarioRepository`, `PedidoRepository`, etc. | Abstrae el acceso a datos y las consultas derivadas. |
| **Strategy** | `SmsService` + `SmsServiceImpl` | Permite cambiar la implementación de SMS (log, Twilio, otro proveedor) sin tocar el dominio. |
| **Observer / Event** | `PedidoListoEvent` + `PedidoListoNotificacionListener` | Desacopla la notificación del caso de uso y la ejecuta después del commit. |
| **DTO / Mapper** | Paquete `dto` y métodos `convertir` | Evita exponer entidades y controla el contrato de la API. |
| **Dependency Injection** | Constructor generado por `@RequiredArgsConstructor` | Facilita pruebas unitarias con Mockito. |

## 4. Modelo de clases

```mermaid
classDiagram
    class Usuario {
        Integer id
        String nombre
        String apellido
        String documentoDeIdentidad
        String celular
        LocalDate fechaDeNacimiento
        String correo
        String clave
        Rol rol
    }

    class Restaurante {
        Integer id
        String nombre
        String nit
        String direccion
        String telefono
        String urlLogo
    }

    class Categoria {
        Integer id
        String nombre
        String descripcion
    }

    class Plato {
        Integer id
        String nombre
        Integer precio
        String descripcion
        String urlImagen
        Boolean estado
    }

    class Pedido {
        Integer id
        EstadoPedido estado
        LocalDateTime fechaCreacion
        Integer total
        String pin
        Integer version
    }

    class DetallePedido {
        Integer id
        Integer cantidad
        Integer precioUnitario
    }

    class PedidoEstadoLog {
        Integer id
        EstadoPedido estadoAnterior
        EstadoPedido estadoNuevo
        LocalDateTime fechaCambio
    }

    Usuario "1" --> "*" Restaurante : propietario
    Restaurante "1" --> "*" Usuario : empleados
    Restaurante "1" --> "*" Plato
    Categoria "1" --> "*" Plato
    Usuario "1" --> "*" Pedido : cliente
    Usuario "0..1" --> "*" Pedido : empleado
    Restaurante "1" --> "*" Pedido
    Pedido "1" --> "*" DetallePedido
    Plato "1" --> "*" DetallePedido
    Pedido "1" --> "*" PedidoEstadoLog
    Usuario "0..1" --> "*" PedidoEstadoLog : empleado
```

## 5. Modelo relacional

```mermaid
erDiagram
    USUARIO ||--o{ RESTAURANTE : "id_propietario"
    RESTAURANTE ||--o{ USUARIO : "id_restaurante"
    RESTAURANTE ||--o{ PLATO : "id_restaurante"
    CATEGORIA ||--o{ PLATO : "id_categoria"
    USUARIO ||--o{ PEDIDO : "id_cliente"
    USUARIO ||--o{ PEDIDO : "id_empleado"
    RESTAURANTE ||--o{ PEDIDO : "id_restaurante"
    PEDIDO ||--o{ DETALLE_PEDIDO : "id_pedido"
    PLATO ||--o{ DETALLE_PEDIDO : "id_plato"
    PEDIDO ||--o{ PEDIDO_ESTADO_LOG : "id_pedido"
    USUARIO ||--o{ PEDIDO_ESTADO_LOG : "id_empleado"

    USUARIO {
        int id PK
        varchar correo UK
        varchar documento_de_identidad UK
        varchar rol
        int id_restaurante FK
    }
    RESTAURANTE {
        int id PK
        varchar nit UK
        int id_propietario FK
    }
    PLATO {
        int id PK
        int id_restaurante FK
        int id_categoria FK
        boolean estado
    }
    PEDIDO {
        int id PK
        int id_cliente FK
        int id_restaurante FK
        int id_empleado FK
        varchar estado
        varchar pin
        int version
    }
    DETALLE_PEDIDO {
        int id PK
        int id_pedido FK
        int id_plato FK
        int cantidad
        int precio_unitario
    }
    PEDIDO_ESTADO_LOG {
        int id PK
        int id_pedido FK
        int id_empleado FK
        varchar estado_anterior
        varchar estado_nuevo
        datetime fecha_cambio
    }
```

## 6. Secuencias del ciclo del pedido

### HU-13 — Asignarse y pasar a EN_PREPARACION

```mermaid
sequenceDiagram
    actor E as Empleado
    participant C as PedidoController
    participant S as PedidoServiceImpl
    participant R as PedidoRepository
    participant L as PedidoEstadoLogRepository

    E->>C: PATCH /pedidos/{id}/asignar
    C->>S: asignarPedido(id, correo)
    S->>R: findById(id)
    S->>S: validar restaurante del empleado
    S->>S: validar estado PENDIENTE
    S->>R: save(pedido EN_PREPARACION)
    S->>L: save(log PENDIENTE -> EN_PREPARACION)
    S-->>C: PedidoResponseDTO
    C-->>E: 200 OK
```

### HU-14 — Pedido listo y PIN

```mermaid
sequenceDiagram
    actor E as Empleado
    participant S as PedidoServiceImpl
    participant R as PedidoRepository
    participant P as ApplicationEventPublisher
    participant N as PedidoListoNotificacionListener
    participant SMS as SmsService

    E->>S: PATCH /pedidos/{id}/listo
    S->>R: findById(id)
    S->>S: validar EN_PREPARACION
    S->>S: generar PIN con SecureRandom
    S->>R: save(pedido LISTO + pin)
    S->>P: publishEvent(PedidoListoEvent)
    Note over P,N: El listener se ejecuta AFTER_COMMIT
    P->>N: onPedidoListo(evento)
    N->>SMS: enviarPedidoListo(celular, pin, id)
    S-->>E: 200 OK (sin PIN)
```

### HU-15 — Entregar con PIN

```mermaid
sequenceDiagram
    actor E as Empleado
    participant S as PedidoServiceImpl
    participant R as PedidoRepository
    participant L as PedidoEstadoLogRepository

    E->>S: PATCH /pedidos/{id}/entregar {pin}
    S->>R: findById(id)
    S->>S: validar LISTO y PIN correcto
    S->>R: save(pedido ENTREGADO)
    S->>L: save(log LISTO -> ENTREGADO)
    S-->>E: 200 OK
```

### HU-16 — Cancelar pedido

```mermaid
sequenceDiagram
    actor C as Cliente
    participant S as PedidoServiceImpl
    participant R as PedidoRepository

    C->>S: PATCH /pedidos/{id}/cancelar
    S->>R: findById(id)
    S->>S: validar dueño y estado PENDIENTE
    alt Estado distinto de PENDIENTE
        S-->>C: 400 "Lo sentimos, tu pedido ya está en preparación..."
    else PENDIENTE
        S->>R: save(pedido CANCELADO)
        S-->>C: 200 OK
    end
```

### HU-17 — Trazabilidad

```mermaid
sequenceDiagram
    actor C as Cliente
    participant T as TrazabilidadServiceImpl
    participant R as PedidoRepository
    participant L as PedidoEstadoLogRepository

    C->>T: GET /pedidos/{id}/trazabilidad
    T->>R: findById(id)
    T->>T: validar que el pedido es del cliente
    T->>L: findByPedido_IdOrderByFechaCambioAscIdAsc(id)
    T-->>C: List<PedidoEstadoLogResponseDTO>
```

## 7. Ruta de migración a microservicios

La separación física recomendada, en orden de riesgo:

1. **Notificaciones** (bajo riesgo): mover `SmsService` a un proceso aparte y
   consumir `POST /api/v1/notificaciones/sms`. Es el contexto con menos
   dependencias de datos.
2. **Trazabilidad** (riesgo medio): separar `pedido_estado_log` y publicar los
   cambios de estado mediante eventos (RabbitMQ/Kafka) o REST.
3. **Usuarios** (riesgo medio-alto): separar autenticación y emisión de JWT;
   Plazoleta valida el token con la clave pública o consulta a Usuarios.
4. **Plazoleta** (riesgo alto): conservar el núcleo de restaurantes, platos y
   pedidos; extraer las consultas de solo lectura si se requiere escalar.

Componentes transversales necesarios: API Gateway, configuración centralizada,
descubrimiento de servicios, trazabilidad distribuida y contratos OpenAPI por
servicio.

## 8. Decisiones no funcionales

| Tema | Decisión |
|---|---|
| Seguridad | JWT stateless, BCrypt, autorización por rol y por recurso (dueño del pedido/restaurante). |
| Concurrencia | Bloqueo optimista (`@Version`) en `Pedido`; bloqueo pesimista del cliente al crear pedido. |
| Rendimiento | Paginación con tope de 50, `@EntityGraph` para relaciones to-one e índices en `pedido` y `plato`. |
| Errores | `GlobalExceptionHandler` con respuestas `{"mensaje": ...}` y códigos 400/401/403/404/409/500. |
| Notificaciones | Evento después del commit; un fallo del SMS no revierte el pedido. |
| Pruebas | 50 pruebas (unitarias + contexto) con H2, sin depender de MySQL. |
| Documentación | OpenAPI/Swagger, Postman por HU y diagramas Mermaid versionados. |
