CREATE TABLE rol (
    id           INT          NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(30)  NOT NULL,
    descripcion  VARCHAR(120) NOT NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE usuario (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    email          VARCHAR(150) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    nombre         VARCHAR(80)  NOT NULL,
    apellido       VARCHAR(80)  NOT NULL,
    documento      VARCHAR(20)      NULL,
    telefono       VARCHAR(30)      NULL,
    activo         BIT(1)       NOT NULL DEFAULT 1,
    fecha_alta     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_usuario_apellido ON usuario (apellido, nombre);

CREATE TABLE usuario_rol (
    usuario_id  BIGINT NOT NULL,
    rol_id      INT    NOT NULL,
    CONSTRAINT pk_usuario_rol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (rol_id)
        REFERENCES rol (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE academia (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id    BIGINT       NOT NULL,
    razon_social  VARCHAR(150) NOT NULL,
    cuit          VARCHAR(13)  NOT NULL,
    descripcion   VARCHAR(500)     NULL,
    activa        BIT(1)       NOT NULL DEFAULT 1,
    fecha_alta    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_academia PRIMARY KEY (id),
    CONSTRAINT uq_academia_usuario UNIQUE (usuario_id),
    CONSTRAINT uq_academia_cuit UNIQUE (cuit),
    CONSTRAINT fk_academia_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE categoria (
    id      INT         NOT NULL AUTO_INCREMENT,
    nombre  VARCHAR(60) NOT NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE curso (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    academia_id        BIGINT         NOT NULL,
    categoria_id       INT            NOT NULL,
    titulo             VARCHAR(150)   NOT NULL,
    descripcion        TEXT           NOT NULL,
    precio             DECIMAL(10, 2) NOT NULL,
    moneda             VARCHAR(3)     NOT NULL DEFAULT 'ARS',
    estado             VARCHAR(20)    NOT NULL DEFAULT 'BORRADOR',
    fecha_creacion     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_publicacion  DATETIME           NULL,
    CONSTRAINT pk_curso PRIMARY KEY (id),
    CONSTRAINT fk_curso_academia FOREIGN KEY (academia_id)
        REFERENCES academia (id) ON DELETE RESTRICT,
    CONSTRAINT fk_curso_categoria FOREIGN KEY (categoria_id)
        REFERENCES categoria (id) ON DELETE RESTRICT,
    CONSTRAINT ck_curso_precio CHECK (precio >= 0),
    CONSTRAINT ck_curso_estado CHECK (estado IN ('BORRADOR', 'PUBLICADO', 'ARCHIVADO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_curso_estado_categoria ON curso (estado, categoria_id);
CREATE INDEX ix_curso_academia ON curso (academia_id);

CREATE TABLE modulo (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    curso_id  BIGINT       NOT NULL,
    titulo    VARCHAR(150) NOT NULL,
    orden     INT          NOT NULL,
    CONSTRAINT pk_modulo PRIMARY KEY (id),
    CONSTRAINT uq_modulo_curso_orden UNIQUE (curso_id, orden),
    CONSTRAINT fk_modulo_curso FOREIGN KEY (curso_id)
        REFERENCES curso (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE leccion (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    modulo_id       BIGINT       NOT NULL,
    titulo          VARCHAR(150) NOT NULL,
    orden           INT          NOT NULL,
    tipo_contenido  VARCHAR(10)  NOT NULL DEFAULT 'VIDEO',
    url_contenido   VARCHAR(500)     NULL,
    contenido       TEXT             NULL,
    duracion_min    INT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_leccion PRIMARY KEY (id),
    CONSTRAINT uq_leccion_modulo_orden UNIQUE (modulo_id, orden),
    CONSTRAINT fk_leccion_modulo FOREIGN KEY (modulo_id)
        REFERENCES modulo (id) ON DELETE CASCADE,
    CONSTRAINT ck_leccion_tipo CHECK (tipo_contenido IN ('VIDEO', 'TEXTO', 'QUIZ'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE inscripcion (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    alumno_id   BIGINT      NOT NULL,
    curso_id    BIGINT      NOT NULL,
    estado      VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE_PAGO',
    fecha_alta  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin   DATETIME        NULL,
    CONSTRAINT pk_inscripcion PRIMARY KEY (id),
    CONSTRAINT uq_inscripcion_alumno_curso UNIQUE (alumno_id, curso_id),
    CONSTRAINT fk_inscripcion_alumno FOREIGN KEY (alumno_id)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT fk_inscripcion_curso FOREIGN KEY (curso_id)
        REFERENCES curso (id) ON DELETE RESTRICT,
    CONSTRAINT ck_inscripcion_estado CHECK (
        estado IN ('PENDIENTE_PAGO', 'ACTIVA', 'COMPLETADA', 'CANCELADA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_inscripcion_curso_estado ON inscripcion (curso_id, estado);

CREATE TABLE pago (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    inscripcion_id      BIGINT         NOT NULL,
    monto               DECIMAL(10, 2) NOT NULL,
    moneda              VARCHAR(3)     NOT NULL DEFAULT 'ARS',
    medio_pago          VARCHAR(20)    NOT NULL,
    estado              VARCHAR(10)    NOT NULL DEFAULT 'INICIADO',
    clave_idempotencia  VARCHAR(64)    NOT NULL,
    referencia_externa  VARCHAR(64)        NULL,
    detalle_rechazo     VARCHAR(200)       NULL,
    fecha_solicitud     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_confirmacion  DATETIME           NULL,
    CONSTRAINT pk_pago PRIMARY KEY (id),
    CONSTRAINT uq_pago_idempotencia UNIQUE (clave_idempotencia),
    CONSTRAINT fk_pago_inscripcion FOREIGN KEY (inscripcion_id)
        REFERENCES inscripcion (id) ON DELETE RESTRICT,
    CONSTRAINT ck_pago_monto CHECK (monto >= 0),
    CONSTRAINT ck_pago_medio CHECK (
        medio_pago IN ('TARJETA_CREDITO', 'TARJETA_DEBITO', 'TRANSFERENCIA')),
    CONSTRAINT ck_pago_estado CHECK (estado IN ('INICIADO', 'APROBADO', 'RECHAZADO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_pago_inscripcion_estado ON pago (inscripcion_id, estado);

CREATE TABLE progreso_leccion (
    id                BIGINT   NOT NULL AUTO_INCREMENT,
    inscripcion_id    BIGINT   NOT NULL,
    leccion_id        BIGINT   NOT NULL,
    fecha_completada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_progreso_leccion PRIMARY KEY (id),
    CONSTRAINT uq_progreso_inscripcion_leccion UNIQUE (inscripcion_id, leccion_id),
    CONSTRAINT fk_progreso_inscripcion FOREIGN KEY (inscripcion_id)
        REFERENCES inscripcion (id) ON DELETE CASCADE,
    CONSTRAINT fk_progreso_leccion FOREIGN KEY (leccion_id)
        REFERENCES leccion (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE certificado (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    inscripcion_id  BIGINT      NOT NULL,
    codigo          VARCHAR(40) NOT NULL,
    fecha_emision   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_certificado PRIMARY KEY (id),
    CONSTRAINT uq_certificado_inscripcion UNIQUE (inscripcion_id),
    CONSTRAINT uq_certificado_codigo UNIQUE (codigo),
    CONSTRAINT fk_certificado_inscripcion FOREIGN KEY (inscripcion_id)
        REFERENCES inscripcion (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
