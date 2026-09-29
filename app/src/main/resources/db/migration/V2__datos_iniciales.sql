INSERT INTO rol (id, nombre, descripcion) VALUES
    (1, 'ADMIN',    'Administrador de la plataforma: gestiona usuarios y academias'),
    (2, 'ACADEMIA', 'Institucion que publica y dicta cursos'),
    (3, 'ALUMNO',   'Usuario que se inscribe, paga y cursa');

INSERT INTO usuario (id, email, password_hash, nombre, apellido, documento, telefono, activo, fecha_alta) VALUES
    (1, 'admin@tribu.com.ar',        '$2b$10$EXviITbIkkWvkJCAt2D9r.ZL62Qg/lKA7LZjOfu7IV80S0ldlci8i', 'Martin',   'Sanchez',  '34567890', '+54 351 4000001', TRUE,  '2026-01-15 09:00:00'),
    (2, 'contacto@codeacademy.com.ar','$2b$10$e6lPwrK6UxyNm.el0tHI2O1zuag1ZVPeGTbUEjh.ZJ6.i/CeohHnu', 'Laura',    'Gimenez',  '28111222', '+54 351 4000002', TRUE,  '2026-02-03 11:20:00'),
    (3, 'info@institutodatos.com.ar', '$2b$10$e6lPwrK6UxyNm.el0tHI2O1zuag1ZVPeGTbUEjh.ZJ6.i/CeohHnu', 'Diego',    'Fernandez','30222333', '+54 351 4000003', TRUE,  '2026-02-19 16:45:00'),
    (4, 'ana.lopez@gmail.com',        '$2b$10$xX4WL1NGkqBA9tak0hfvxer9wuqpfQ7NScBPUEKu/SDRgOeR6rGj.', 'Ana',      'Lopez',    '40111222', '+54 351 4000004', TRUE,  '2026-04-28 20:10:00'),
    (5, 'juan.perez@gmail.com',       '$2b$10$xX4WL1NGkqBA9tak0hfvxer9wuqpfQ7NScBPUEKu/SDRgOeR6rGj.', 'Juan',     'Perez',    '41222333', '+54 351 4000005', TRUE,  '2026-04-05 08:35:00'),
    (6, 'sofia.rios@gmail.com',       '$2b$10$xX4WL1NGkqBA9tak0hfvxer9wuqpfQ7NScBPUEKu/SDRgOeR6rGj.', 'Sofia',    'Rios',     '42333444', '+54 351 4000006', TRUE,  '2026-05-22 13:05:00'),
    (7, 'pablo.diaz@gmail.com',       '$2b$10$xX4WL1NGkqBA9tak0hfvxer9wuqpfQ7NScBPUEKu/SDRgOeR6rGj.', 'Pablo',    'Diaz',     '43444555', '+54 351 4000007', FALSE, '2026-03-11 10:50:00');

INSERT INTO usuario_rol (usuario_id, rol_id) VALUES
    (1, 1),
    (2, 2),
    (3, 2),
    (4, 3), (5, 3), (6, 3), (7, 3);

INSERT INTO academia (id, usuario_id, razon_social, cuit, descripcion, activa) VALUES
    (1, 2, 'Code Academy SRL',        '30-71234567-1', 'Formacion en desarrollo de software y buenas practicas.', TRUE),
    (2, 3, 'Instituto de Datos SAS',  '30-71888444-2', 'Especialistas en analisis de datos y bases de datos.',    TRUE);

INSERT INTO categoria (id, nombre) VALUES
    (1, 'Programacion'),
    (2, 'Bases de Datos'),
    (3, 'Redes y Comunicaciones'),
    (4, 'Gestion de Proyectos');

INSERT INTO curso (id, academia_id, categoria_id, titulo, descripcion, precio, moneda, estado, fecha_publicacion) VALUES
    (1, 1, 1, 'Java desde cero',
        'Fundamentos del lenguaje Java: tipos, control de flujo, POO, colecciones y manejo de excepciones.',
        45000.00, 'ARS', 'PUBLICADO', '2026-03-01 10:00:00'),
    (2, 1, 1, 'Spring Boot aplicado',
        'Construccion de aplicaciones web con Spring Boot, Spring Data JPA y Spring Security.',
        72000.00, 'ARS', 'PUBLICADO', '2026-03-15 10:00:00'),
    (3, 2, 2, 'MySQL y modelado relacional',
        'Diseno de esquemas normalizados, DER, indices, transacciones y optimizacion de consultas.',
        58000.00, 'ARS', 'PUBLICADO', '2026-04-01 10:00:00'),
    (4, 2, 3, 'Redes para desarrolladores',
        'Modelo TCP/IP, HTTP/HTTPS, TLS, DNS y diagnostico de problemas de conectividad.',
        39000.00, 'ARS', 'PUBLICADO', '2026-04-20 10:00:00'),
    (5, 1, 4, 'Proceso Unificado de Desarrollo',
        'Fases, disciplinas y artefactos del PUD aplicados a un proyecto real.',
        0.00, 'ARS', 'BORRADOR', NULL),
    (6, 1, 1, 'Git y control de versiones',
        'Flujo de trabajo con ramas, resolucion de conflictos, revision de codigo y buenas practicas de historial.',
        32000.00, 'ARS', 'PUBLICADO', '2026-06-10 09:00:00');

INSERT INTO modulo (id, curso_id, titulo, orden) VALUES
    (1, 1, 'Introduccion al lenguaje', 1),
    (2, 1, 'Programacion orientada a objetos', 2),
    (3, 2, 'Primeros pasos con Spring Boot', 1),
    (4, 3, 'Modelo relacional', 1),
    (5, 4, 'Fundamentos de red', 1),
    (6, 6, 'Trabajo con ramas', 1);

INSERT INTO leccion (id, modulo_id, titulo, orden, tipo_contenido, url_contenido, contenido, duracion_min) VALUES
    (1, 1, 'Que es la JVM',                  1, 'VIDEO', 'https://cdn.tribu.com.ar/java/jvm.mp4', NULL, 18),
    (2, 1, 'Tipos de datos y variables',     2, 'VIDEO', 'https://cdn.tribu.com.ar/java/tipos.mp4', NULL, 25),
    (3, 1, 'Estructuras de control',         3, 'TEXTO', NULL, 'Sentencias if, switch, for, while y do-while con ejemplos resueltos.', 20),
    (4, 2, 'Clases y objetos',               1, 'VIDEO', 'https://cdn.tribu.com.ar/java/poo.mp4', NULL, 30),
    (5, 2, 'Herencia y polimorfismo',        2, 'VIDEO', 'https://cdn.tribu.com.ar/java/herencia.mp4', NULL, 28),
    (6, 2, 'Autoevaluacion del modulo',      3, 'QUIZ',  NULL, 'Cuestionario de 10 preguntas de opcion multiple.', 15),
    (7, 3, 'Estructura de un proyecto Boot', 1, 'VIDEO', 'https://cdn.tribu.com.ar/spring/estructura.mp4', NULL, 22),
    (8, 3, 'Inyeccion de dependencias',      2, 'VIDEO', 'https://cdn.tribu.com.ar/spring/di.mp4', NULL, 26),
    (9, 4, 'Normalizacion hasta 3FN',        1, 'TEXTO', NULL, 'Primera, segunda y tercera forma normal con casos practicos.', 35),
    (10, 4, 'Claves foraneas e integridad',  2, 'VIDEO', 'https://cdn.tribu.com.ar/mysql/fk.mp4', NULL, 24),
    (11, 5, 'Modelo TCP/IP',                 1, 'VIDEO', 'https://cdn.tribu.com.ar/redes/tcpip.mp4', NULL, 30),
    (12, 5, 'HTTP, HTTPS y TLS',             2, 'VIDEO', 'https://cdn.tribu.com.ar/redes/tls.mp4', NULL, 27),
    (13, 5, 'Diagnostico de conectividad',   3, 'TEXTO', NULL, 'Uso de ping, traceroute, dig y curl para aislar fallas de red.', 22),
    (14, 6, 'Ramas y fusiones',              1, 'VIDEO', 'https://cdn.tribu.com.ar/git/ramas.mp4', NULL, 24),
    (15, 6, 'Resolucion de conflictos',      2, 'TEXTO', NULL, 'Estrategias para resolver conflictos de fusion sin perder trabajo.', 19);

INSERT INTO inscripcion (id, alumno_id, curso_id, estado, fecha_alta, fecha_fin) VALUES
    (1, 4, 1, 'ACTIVA',         '2026-05-02 09:15:00', NULL),
    (2, 4, 3, 'ACTIVA',         '2026-05-10 18:40:00', NULL),
    (3, 5, 1, 'COMPLETADA',     '2026-04-11 11:00:00', '2026-06-01 16:20:00'),
    (4, 5, 2, 'PENDIENTE_PAGO', '2026-06-05 20:05:00', NULL),
    (5, 6, 4, 'ACTIVA',         '2026-05-28 08:30:00', NULL),
    (6, 6, 1, 'CANCELADA',      '2026-03-20 14:00:00', '2026-03-22 09:00:00');

INSERT INTO pago (id, inscripcion_id, monto, moneda, medio_pago, estado, clave_idempotencia, referencia_externa, detalle_rechazo, fecha_solicitud, fecha_confirmacion) VALUES
    (1, 1, 45000.00, 'ARS', 'TARJETA_CREDITO', 'APROBADO',  'idem-0001-a7f3', 'PSP-99180023', NULL,                       '2026-05-02 09:15:10', '2026-05-02 09:15:14'),
    (2, 2, 58000.00, 'ARS', 'TRANSFERENCIA',   'APROBADO',  'idem-0002-b1c9', 'PSP-99180451', NULL,                       '2026-05-10 18:40:05', '2026-05-10 18:41:02'),
    (3, 3, 45000.00, 'ARS', 'TARJETA_DEBITO',  'APROBADO',  'idem-0003-c4e2', 'PSP-99177310', NULL,                       '2026-04-11 11:00:08', '2026-04-11 11:00:11'),
    (4, 4, 72000.00, 'ARS', 'TARJETA_CREDITO', 'RECHAZADO', 'idem-0004-d8a1', 'PSP-99191002', 'Fondos insuficientes',     '2026-06-05 20:05:12', '2026-06-05 20:05:15'),
    (5, 5, 39000.00, 'ARS', 'TARJETA_CREDITO', 'APROBADO',  'idem-0005-e5b7', 'PSP-99188877', NULL,                       '2026-05-28 08:30:09', '2026-05-28 08:30:13'),
    (6, 6, 45000.00, 'ARS', 'TARJETA_CREDITO', 'APROBADO',  'idem-0006-f2d4', 'PSP-99170455', NULL,                       '2026-03-20 14:00:07', '2026-03-20 14:00:10');

INSERT INTO progreso_leccion (inscripcion_id, leccion_id, fecha_completada) VALUES
    (1, 1, '2026-05-02 10:00:00'),
    (1, 2, '2026-05-03 19:30:00'),
    (1, 3, '2026-05-05 21:10:00');

INSERT INTO progreso_leccion (inscripcion_id, leccion_id, fecha_completada) VALUES
    (3, 1, '2026-04-12 08:00:00'),
    (3, 2, '2026-04-14 08:00:00'),
    (3, 3, '2026-04-18 08:00:00'),
    (3, 4, '2026-05-02 08:00:00'),
    (3, 5, '2026-05-20 08:00:00'),
    (3, 6, '2026-06-01 16:20:00');

INSERT INTO progreso_leccion (inscripcion_id, leccion_id, fecha_completada) VALUES
    (5, 11, '2026-05-29 22:15:00');

INSERT INTO certificado (inscripcion_id, codigo, fecha_emision) VALUES
    (3, 'TRIBU-2026-000031', '2026-06-01 16:25:00');
