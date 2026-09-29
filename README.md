# Tribu — Plataforma de comunidades con cursos pagos

Prototipo operacional desarrollado para el **Trabajo Práctico 2 de Seminario de Práctica
Informática**, Universidad Siglo 21.

**Alumno:** Martín Alejandro Sánchez Cejas — Legajo VINF016509

---

## Qué es

Una plataforma web donde academias publican cursos y alumnos se inscriben, **pagan** y **cursan**,
con registro de avance y emisión automática de certificados. Tres perfiles de usuario: `ADMIN`,
`ACADEMIA` y `ALUMNO`.

## Cómo ejecutarlo

Requisito único: **Docker**. No hace falta instalar Java, Maven ni MySQL.

```bash
docker compose up --build
```

Luego abrir **http://localhost:8080**.

El primer arranque tarda unos minutos porque compila la aplicación dentro del contenedor. Flyway
crea el esquema y carga el juego de datos automáticamente.

Para detenerlo y borrar la base:

```bash
docker compose down -v
```

### Cuentas de demostración

Contraseña para todas: `Tribu2026!`

| Perfil | Email |
|---|---|
| `ADMIN` | `admin@tribu.com.ar` |
| `ACADEMIA` | `contacto@codeacademy.com.ar` · `info@institutodatos.com.ar` |
| `ALUMNO` | `ana.lopez@gmail.com` · `juan.perez@gmail.com` · `sofia.rios@gmail.com` |

### Tarjetas de prueba

La pasarela de pagos es **simulada** y determinística: el resultado depende de los últimos cuatro
dígitos de la tarjeta.

| Número | Resultado |
|---|---|
| `4509 9535 6623 3704` | Aprobado |
| `4509 9535 6623 0000` | Rechazado — fondos insuficientes |
| `4509 9535 6623 1111` | Rechazado — tarjeta vencida |

### Recorrido sugerido

1. Entrar al **catálogo** sin autenticarse.
2. Ingresar como **alumno** e inscribirse a un curso.
3. Pagar con la tarjeta terminada en `0000` → ver el rechazo y que el curso sigue bloqueado.
4. Pagar con la tarjeta válida → acceder al contenido.
5. Marcar todas las lecciones → ver el **certificado emitido**.
6. Ingresar como **administrador** y revisar el listado de usuarios.

---

## Stack

| Componente | Versión |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 3.3.5 |
| Spring Security | 6 |
| Thymeleaf | 3 |
| MySQL | 8.4 |
| Flyway | migraciones versionadas |
| JUnit 5 + Testcontainers | pruebas contra MySQL real |

---

## Estructura

```
tribu/
├── docker-compose.yml          Aplicación + MySQL
├── app/
│   ├── Dockerfile              Build multi-etapa (Maven → JRE)
│   └── src/
│       ├── main/java/…/tribu/
│       │   ├── domain/         Entidades JPA y enumerados
│       │   ├── repository/     Repositorios Spring Data
│       │   ├── service/        Reglas de negocio
│       │   ├── web/            Controladores MVC
│       │   ├── dto/            Proyecciones de lectura
│       │   └── config/         Seguridad
│       ├── main/resources/
│       │   ├── db/migration/   V1 esquema · V2 datos iniciales
│       │   └── templates/      Vistas Thymeleaf
│       └── test/               33 pruebas automatizadas
└── docs/
    ├── informe/INFORME_TP2.md  Informe del trabajo práctico
    ├── uml/                    Diagramas PlantUML (.puml + .png)
    ├── db/consultas.sql        Consultas SQL del TP
    ├── capturas/               Pantallas del prototipo
    └── capturar-pantallas.mjs  Script Playwright que las genera
```

---

## Desarrollo

### Ejecutar las pruebas

Requieren Docker (Testcontainers levanta un MySQL real):

```bash
cd app && mvn test
```

Si no tenés Maven instalado, dentro de un contenedor:

```bash
docker run --rm \
  -v "$PWD/app":/ws -v "$PWD/.m2":/root/.m2 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --add-host=host.docker.internal:host-gateway \
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
  -w /ws maven:3.9-eclipse-temurin-21 \
  mvn -B -DargLine="-Dapi.version=1.47" test
```

> `api.version` fuerza una versión de la API de Docker compatible con demonios 29 o superiores.

### Regenerar los diagramas UML

Los diagramas se versionan como texto (`.puml`) y se renderizan con PlantUML:

```bash
cd docs/uml
docker run --rm -v "$PWD":/work -w /work plantuml/plantuml -tpng -charset UTF-8 "*.puml"
```

### Regenerar las capturas de pantalla

Con el sistema levantado:

```bash
npm install playwright && npx playwright install chromium
node docs/capturar-pantallas.mjs
```

### Consultar la base de datos

MySQL queda expuesto en el puerto **3307** del host:

```bash
docker exec -it tribu-mysql mysql -utribu -ptribu tribu
```

Para ejecutar el archivo de consultas del TP:

```bash
docker exec -i tribu-mysql mysql -utribu -ptribu tribu --table < docs/db/consultas.sql
```

---

## Notas de diseño

- **El esquema lo gobierna Flyway, no Hibernate.** `hibernate.ddl-auto=validate` hace fallar el
  arranque si las entidades y las tablas divergen.
- **Las reglas críticas están en la base**, no sólo en el código: `UNIQUE (alumno_id, curso_id)`
  impide la inscripción duplicada y `UNIQUE (clave_idempotencia)` impide el doble cobro, incluso
  bajo peticiones concurrentes.
- **La pasarela de pagos está detrás de una interfaz** (`PasarelaPago`). Sustituir la implementación
  simulada por un proveedor real no obliga a tocar el resto del sistema.
- **Los números de tarjeta no se almacenan** ni se escriben completos en el log.

---

## Licencia

Trabajo académico. Universidad Siglo 21, 2026.
