-- =====================================================================
--  Tribu - Consultas SQL
--  Universidad Siglo 21 - Seminario de Practica Informatica - TP2
--
--  Entregables que cubre este archivo:
--    * Insercion, consulta y borrado de registros
--    * Presentacion de las consultas SQL
--
--  Como ejecutarlo con el sistema levantado (docker compose up):
--    docker exec -i tribu-mysql mysql -utribu -ptribu tribu < docs/db/consultas.sql
--
--  El DDL completo esta en:
--    app/src/main/resources/db/migration/V1__esquema_inicial.sql
--  La carga inicial de datos en:
--    app/src/main/resources/db/migration/V2__datos_iniciales.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- A. CONSULTAS DE LECTURA
-- ---------------------------------------------------------------------

-- A.1 Catalogo publico: cursos publicados con su academia y categoria.
--     Es la consulta que alimenta la pantalla principal del sistema.
SELECT c.id,
       c.titulo,
       cat.nombre                                   AS categoria,
       a.razon_social                               AS academia,
       CONCAT(c.moneda, ' ', FORMAT(c.precio, 2))   AS precio,
       DATE_FORMAT(c.fecha_publicacion, '%d/%m/%Y') AS publicado
FROM curso c
         JOIN categoria cat ON cat.id = c.categoria_id
         JOIN academia  a   ON a.id   = c.academia_id
WHERE c.estado = 'PUBLICADO'
ORDER BY c.fecha_publicacion DESC;

-- A.2 Listado de usuarios con sus roles (panel del administrador, CU-02).
--     GROUP_CONCAT resuelve en una fila los N roles de cada usuario.
SELECT u.id,
       CONCAT(u.apellido, ', ', u.nombre)     AS usuario,
       u.email,
       GROUP_CONCAT(r.nombre ORDER BY r.nombre SEPARATOR ' / ') AS roles,
       IF(u.activo, 'Activo', 'Inactivo')     AS estado
FROM usuario u
         LEFT JOIN usuario_rol ur ON ur.usuario_id = u.id
         LEFT JOIN rol         r  ON r.id          = ur.rol_id
GROUP BY u.id, u.apellido, u.nombre, u.email, u.activo
ORDER BY u.apellido, u.nombre;

-- A.3 Avance de cada alumno por curso.
--     Combina el total de lecciones del plan con las efectivamente
--     completadas. La subconsulta evita que el LEFT JOIN de progreso
--     multiplique las filas del conteo de lecciones.
SELECT CONCAT(u.apellido, ', ', u.nombre) AS alumno,
       c.titulo                           AS curso,
       i.estado,
       plan.total_lecciones,
       COUNT(pl.id)                       AS lecciones_hechas,
       ROUND(COUNT(pl.id) * 100 / NULLIF(plan.total_lecciones, 0)) AS porcentaje
FROM inscripcion i
         JOIN usuario u ON u.id = i.alumno_id
         JOIN curso   c ON c.id = i.curso_id
         JOIN (SELECT m.curso_id, COUNT(l.id) AS total_lecciones
               FROM modulo m
                        JOIN leccion l ON l.modulo_id = m.id
               GROUP BY m.curso_id) plan ON plan.curso_id = c.id
         LEFT JOIN progreso_leccion pl ON pl.inscripcion_id = i.id
GROUP BY i.id, u.apellido, u.nombre, c.titulo, i.estado, plan.total_lecciones
ORDER BY porcentaje DESC, alumno;

-- A.4 Facturacion por academia: solo cuenta los pagos efectivamente
--     aprobados. Los intentos rechazados no suman ingresos.
SELECT a.razon_social                AS academia,
       COUNT(p.id)                   AS pagos_aprobados,
       CONCAT('ARS ', FORMAT(SUM(p.monto), 2)) AS total_recaudado
FROM pago p
         JOIN inscripcion i ON i.id = p.inscripcion_id
         JOIN curso       c ON c.id = i.curso_id
         JOIN academia    a ON a.id = c.academia_id
WHERE p.estado = 'APROBADO'
GROUP BY a.id, a.razon_social
ORDER BY SUM(p.monto) DESC;

-- A.5 Inscripciones que quedaron trabadas en el cobro, con el motivo del
--     ultimo rechazo. Es el listado que usaria soporte para recuperarlas.
SELECT CONCAT(u.apellido, ', ', u.nombre) AS alumno,
       u.email,
       c.titulo                           AS curso,
       p.medio_pago,
       p.detalle_rechazo,
       DATE_FORMAT(p.fecha_solicitud, '%d/%m/%Y %H:%i') AS intento
FROM inscripcion i
         JOIN usuario u ON u.id = i.alumno_id
         JOIN curso   c ON c.id = i.curso_id
         JOIN pago    p ON p.inscripcion_id = i.id
WHERE i.estado = 'PENDIENTE_PAGO'
  AND p.estado = 'RECHAZADO'
ORDER BY p.fecha_solicitud DESC;

-- A.6 Cursos publicados que todavia no tienen ningun inscripto.
--     Se resuelve con LEFT JOIN + IS NULL (antijoin).
SELECT c.titulo, a.razon_social AS academia
FROM curso c
         JOIN academia a ON a.id = c.academia_id
         LEFT JOIN inscripcion i ON i.curso_id = c.id
WHERE c.estado = 'PUBLICADO'
  AND i.id IS NULL
ORDER BY c.titulo;

-- A.7 Certificados emitidos, con el curso que acreditan.
SELECT cer.codigo,
       CONCAT(u.apellido, ', ', u.nombre) AS alumno,
       c.titulo                           AS curso,
       DATE_FORMAT(cer.fecha_emision, '%d/%m/%Y') AS emitido
FROM certificado cer
         JOIN inscripcion i ON i.id = cer.inscripcion_id
         JOIN usuario     u ON u.id = i.alumno_id
         JOIN curso       c ON c.id = i.curso_id
ORDER BY cer.fecha_emision DESC;

-- ---------------------------------------------------------------------
-- B. INSERCION DE REGISTROS
-- ---------------------------------------------------------------------

-- B.1 Alta de un alumno. La contrasena se guarda como hash BCrypt; el
--     valor de ejemplo corresponde a "Tribu2026!".
INSERT INTO usuario (email, password_hash, nombre, apellido, documento, telefono, activo)
VALUES ('lucia.mendez@gmail.com',
        '$2b$10$qfAez7DJSYKG1db9QuzzXuQL7xAMztYjmsaS2fHtW1snYgTRQvE1a',
        'Lucia', 'Mendez', '44555666', '+54 351 4000008', TRUE);

SET @nuevo_alumno = LAST_INSERT_ID();

-- B.2 Asignacion del rol ALUMNO al usuario recien creado.
INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT @nuevo_alumno, r.id FROM rol r WHERE r.nombre = 'ALUMNO';

-- B.3 Inscripcion al curso "Redes para desarrolladores" (id 4).
INSERT INTO inscripcion (alumno_id, curso_id, estado)
VALUES (@nuevo_alumno, 4, 'PENDIENTE_PAGO');

SET @nueva_inscripcion = LAST_INSERT_ID();

-- B.4 Registro del pago aprobado. El importe se toma del curso, no se
--     escribe a mano, para que no pueda divergir del precio publicado.
INSERT INTO pago (inscripcion_id, monto, moneda, medio_pago, estado,
                  clave_idempotencia, referencia_externa, fecha_confirmacion)
SELECT @nueva_inscripcion, c.precio, c.moneda, 'TARJETA_CREDITO', 'APROBADO',
       'idem-demo-sql-0001', 'PSP-99200777', NOW()
FROM curso c
WHERE c.id = 4;

-- B.5 Con el pago aprobado, la inscripcion pasa a ACTIVA.
UPDATE inscripcion
SET estado = 'ACTIVA'
WHERE id = @nueva_inscripcion;

-- Verificacion del alta completa.
SELECT u.email, c.titulo AS curso, i.estado AS inscripcion, p.estado AS pago, p.monto
FROM usuario u
         JOIN inscripcion i ON i.alumno_id = u.id
         JOIN curso       c ON c.id = i.curso_id
         JOIN pago        p ON p.inscripcion_id = i.id
WHERE u.id = @nuevo_alumno;

-- ---------------------------------------------------------------------
-- C. ACTUALIZACION DE REGISTROS
-- ---------------------------------------------------------------------

-- C.1 Baja logica de un usuario: se deshabilita el acceso pero se
--     conserva todo su historial academico y de pagos. Nunca se borra
--     fisicamente un usuario con inscripciones.
UPDATE usuario SET activo = FALSE WHERE email = 'pablo.diaz@gmail.com';

-- C.2 Ajuste de precio de un curso del catalogo.
UPDATE curso SET precio = 49500.00 WHERE id = 1;

-- C.3 Publicacion de un curso que estaba en BORRADOR, solo si ya tiene
--     lecciones cargadas (la misma regla que aplica la capa de servicio).
UPDATE curso c
SET c.estado = 'PUBLICADO',
    c.fecha_publicacion = NOW()
WHERE c.id = 5
  AND EXISTS (SELECT 1
              FROM modulo m JOIN leccion l ON l.modulo_id = m.id
              WHERE m.curso_id = c.id);

-- ---------------------------------------------------------------------
-- D. BORRADO DE REGISTROS
-- ---------------------------------------------------------------------

-- D.1 El alumno desmarca una leccion: se borra la fila de progreso.
--     Es el unico borrado fisico habitual del sistema.
DELETE FROM progreso_leccion
WHERE inscripcion_id = 1
  AND leccion_id = 3;

-- D.2 Baja de una inscripcion cancelada que nunca llego a pagarse.
--     Se borra primero el detalle y despues la cabecera; progreso_leccion
--     tiene ON DELETE CASCADE, pero se explicita para dejar clara la
--     dependencia.
DELETE FROM progreso_leccion WHERE inscripcion_id = 6;
DELETE FROM pago             WHERE inscripcion_id = 6;
DELETE FROM inscripcion      WHERE id = 6;

-- D.3 Intento de borrado que la base debe RECHAZAR.
--     La FK de inscripcion hacia usuario esta declarada ON DELETE RESTRICT,
--     de modo que no se puede eliminar un alumno con historial academico.
--     Descomentar para comprobar el error 1451:
--
--     DELETE FROM usuario WHERE id = 4;
--
--     ERROR 1451 (23000): Cannot delete or update a parent row:
--     a foreign key constraint fails (`tribu`.`inscripcion`,
--     CONSTRAINT `fk_inscripcion_alumno` FOREIGN KEY (`alumno_id`)
--     REFERENCES `usuario` (`id`))

-- D.4 Limpieza de los datos de demostracion creados en la seccion B,
--     para dejar la base como estaba.
DELETE FROM pago        WHERE clave_idempotencia = 'idem-demo-sql-0001';
DELETE FROM inscripcion WHERE alumno_id = @nuevo_alumno;
DELETE FROM usuario_rol WHERE usuario_id = @nuevo_alumno;
DELETE FROM usuario     WHERE id = @nuevo_alumno;
