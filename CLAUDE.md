# Terracodex API

Java 21, Spring Boot 4.1, Spring Modulith 2.1, JPA + Flyway, JWT (HS256). Arquitectura copiada de `D:\desarrollos\app-zonal-api`: `shared` y el módulo `seguridad` (features usuario, rol, menu, autenticacion). Roles base: ADMIN, SUPERVISOR, OPERADOR.

Módulo `campo`: `recomendacion` (proxy a `servicio-ml/`, modelo de aptitud de cultivos en Python/FastAPI, red interna `ML_URL`) y `registro` (registros de parcela desde el móvil, tabla `registros_campo`, V3). Cada usuario ve los suyos; ADMIN/SUPERVISOR ven todos con `todos=true`.

## Reglas de arquitectura (las verifican `ModularityTests` y `HexagonalArchitectureTests`)

- Cada paquete de primer nivel bajo `com.terracodex` es un módulo de negocio (screaming architecture). `shared` es un módulo abierto con piezas transversales y no conoce a ningún módulo.
- Esquema por feature: `modulo/feature/{domain,application,infrastructure}`.
  - `domain/model`, `domain/events`: Java puro. Nada de Spring, JPA, Jackson.
  - `application/port/input`: una interfaz por caso de uso. `application/port/output`: repositorios y servicios externos.
  - `application/usecase`: `XService` package-private con `@UseCase` + `@Transactional` que implementa un puerto de entrada.
  - `application/dto/{command,query,result}`: records. Los casos de uso reciben commands/queries y devuelven results.
  - `infrastructure/adapter/input/rest`: controladores que solo dependen de puertos de entrada; `request/` con `toCommand()`, `response/` con `static from(Result)`. Otros: `input/event`, `input/startup`.
  - `infrastructure/adapter/output/persistence`: `entity/`, `repository/`, `mapper/` y un `XPersistenceAdapter` package-private.
  - Una feature solo usa el `domain` y los `application.port` de otra. Para reaccionar sin acoplarse, publica un evento.
- Los módulos se comunican solo por eventos; declara la dependencia en `package-info.java` (`allowedDependencies`).
- Casos de uso: cargar el agregado, llamar métodos de dominio, `save` y `events.publish(agregado.pullEvents())` en la misma transacción.
- La hora viene del `Clock` inyectado (America/Lima); nunca `now()` en el dominio.
- Errores: `DomainException` → 422, `NotFoundException` → 404, `NoAutenticadoException` → 401, `AccesoDenegadoException` → 403, `IllegalArgumentException` → 400.
- Cada cambio de tablas va en una migración Flyway nueva `V<n>__<descripcion>.sql` que corra en H2 (modo PostgreSQL) y PostgreSQL. `ddl-auto` es `validate`.
- Lenguaje de dominio en español; términos de framework en inglés.
- Maestros: id bigint identity, `descripcion`, `estado`, y las 4 columnas de auditoría (la entidad extiende `EntidadAuditada`). En BD cada columna del maestro lleva el nombre de la entidad (`id_rol`, `descripcion_usuario`); transaccionales sin prefijo, salvo sus FK.
- No se borran filas: los maestros se desactivan (`estado = false`).

## Convenciones del API

- Toda respuesta es `ApiResponse<T>` (`{ ok, status, message, data, errors, timestamp }`), incluidos los errores y los 401/403 de Spring Security.
- Listados: `PageRequestParams` → `PageQuery` → `PageResult<T>` → `PageResponse.from(...)`.
- Todo `/api/**` exige JWT salvo `POST /api/auth/{login,refresh,logout}`. Roles con `@PreAuthorize`; el usuario actual sale de `@AuthenticationPrincipal Jwt` (`sub`, `login`, `nombre`, `roles`, `canal`, `sid`).

## Comandos

- `./mvnw test` corre todo (sin Docker).
- `./mvnw spring-boot:run` levanta el perfil `local` (H2). Admin inicial: admin / Admin123*.
- Perfil `dev`: PostgreSQL con `compose.yaml`.
- Producción: VPS de Hostinger (76.13.225.20) con Docker Compose (`deploy/`: PostgreSQL + API + Caddy), guía en `docs/despliegue-docker.md`, variables en `deploy/.env.ejemplo`. La cuenta de Hostinger es compartida: nunca actuar sobre ella (VPS, DNS, firewall) sin confirmación explícita, y nunca en el VPS de app-zonal (`srv1608280`).
- `python3 scripts/seed/cargar_usuarios.py --api <url>` carga por el API los usuarios de `scripts/seed/usuarios.json` (idempotente). Pide la contraseña del admin y la inicial de los usuarios (o `TERRACODEX_ADMIN_CLAVE` / `TERRACODEX_CLAVE_INICIAL`); las contraseñas nunca van en el JSON.
