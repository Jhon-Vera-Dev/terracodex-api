-- Login con Google (OpenID Connect). Transaccionales: columnas sin prefijo, salvo la FK id_usuario.
-- Debe correr en H2 (modo PostgreSQL) y en PostgreSQL.

-- Vínculo cuenta de Google ↔ usuario. sub es el id estable de Google (el correo puede cambiar).
create table cuentas_google (
    google_sub   varchar(255) primary key,
    id_usuario   bigint       not null unique references usuarios (id_usuario),
    email        varchar(320) not null,
    vinculada_en timestamp with time zone not null
);

-- Cada intento de login: id = state de OAuth; desafio = SHA-256 del verificador que guarda la app.
-- Vive 10 minutos; las filas viejas se pueden borrar sin efecto (solo sirven para el canje).
create table inicios_google (
    id           varchar(64)  primary key,
    desafio      varchar(64)  not null,
    canal        varchar(10)  not null,
    creado_en    timestamp with time zone not null,
    expira_en    timestamp with time zone not null,
    id_usuario   bigint references usuarios (id_usuario),
    error        varchar(300),
    consumido_en timestamp with time zone
);

create index ix_inicios_google_expira on inicios_google (expira_en);
