/**
 * Captura las pantallas del prototipo Tribu para incluirlas en el informe.
 *
 * Requisitos:
 *   1. El sistema levantado:  docker compose up --build
 *   2. Playwright instalado:  npm i playwright && npx playwright install chromium
 *
 * Uso:
 *   node docs/capturar-pantallas.mjs
 *
 * Las imágenes quedan en docs/capturas/.
 */

import { chromium } from 'playwright';
import { mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const RAIZ = resolve(dirname(fileURLToPath(import.meta.url)));
const SALIDA = resolve(RAIZ, 'capturas');
const BASE = process.env.TRIBU_URL ?? 'http://localhost:8080';

const CLAVE = 'Tribu2026!';
const ADMIN = 'admin@tribu.com.ar';
const ACADEMIA = 'contacto@codeacademy.com.ar';
const ALUMNO = 'juan.perez@gmail.com';

mkdirSync(SALIDA, { recursive: true });

const navegador = await chromium.launch();
let n = 0;

/** Guarda la página completa con un nombre numerado y ordenable. */
async function capturar(pagina, nombre) {
  n += 1;
  const archivo = `${String(n).padStart(2, '0')}-${nombre}.png`;
  await pagina.screenshot({ path: resolve(SALIDA, archivo), fullPage: true });
  console.log(`  ✓ ${archivo}`);
}

/** Abre una sesión nueva y autentica al usuario indicado. */
async function sesion(email) {
  const contexto = await navegador.newContext({ viewport: { width: 1280, height: 900 } });
  const pagina = await contexto.newPage();
  await pagina.goto(`${BASE}/login`);
  await pagina.fill('#email', email);
  await pagina.fill('#password', CLAVE);
  // Selector acotado al formulario de login: la cabecera de las páginas
  // autenticadas tiene su propio botón submit (el de "Salir").
  await pagina.click('form[action$="/login"] button[type=submit]');
  await pagina.waitForLoadState('networkidle');
  return { contexto, pagina };
}

/** Completa y envía el formulario de pago con el número de tarjeta indicado. */
async function pagarCon(pagina, numeroTarjeta) {
  await pagina.fill('#numeroTarjeta', numeroTarjeta);
  await pagina.click('form[action*="/alumno/pago/"] button[type=submit]');
  await pagina.waitForLoadState('networkidle');
}

// --- 1. Pantallas públicas ---------------------------------------------
console.log('Públicas:');
{
  const contexto = await navegador.newContext({ viewport: { width: 1280, height: 900 } });
  const pagina = await contexto.newPage();

  await pagina.goto(`${BASE}/catalogo`);
  await capturar(pagina, 'catalogo-publico');

  await pagina.goto(`${BASE}/catalogo/1`);
  await capturar(pagina, 'detalle-de-curso');

  await pagina.goto(`${BASE}/login`);
  await capturar(pagina, 'login');

  await pagina.goto(`${BASE}/registro`);
  await capturar(pagina, 'registro-de-alumno');

  await contexto.close();
}

// --- 2. ADMIN: gestión de usuarios (CU-02) ------------------------------
console.log('ADMIN:');
{
  const { contexto, pagina } = await sesion(ADMIN);

  await pagina.goto(`${BASE}/admin/usuarios`);
  await capturar(pagina, 'admin-listado-de-usuarios');

  await pagina.goto(`${BASE}/admin/usuarios?rol=ALUMNO`);
  await capturar(pagina, 'admin-usuarios-filtrados-por-rol');

  await contexto.close();
}

// --- 3. ACADEMIA: catálogo propio e inscriptos (CU-03) ------------------
console.log('ACADEMIA:');
{
  const { contexto, pagina } = await sesion(ACADEMIA);

  await pagina.goto(`${BASE}/academia/cursos`);
  await capturar(pagina, 'academia-mis-cursos');

  await pagina.goto(`${BASE}/academia/cursos/1/inscriptos`);
  await capturar(pagina, 'academia-inscriptos-del-curso');

  await contexto.close();
}

// --- 4. ALUMNO: inscripción, pago y cursado (CU-04 a CU-07) -------------
console.log('ALUMNO:');
{
  const { contexto, pagina } = await sesion(ALUMNO);

  await pagina.goto(`${BASE}/alumno/mis-cursos`);
  await capturar(pagina, 'alumno-mis-cursos');

  // La inscripción 4 (Juan al curso "Spring Boot aplicado") viene del juego de
  // datos en estado PENDIENTE_PAGO y con un intento de cobro ya rechazado.
  await pagina.goto(`${BASE}/alumno/pago/4`);
  await capturar(pagina, 'alumno-pago-pendiente');

  // Pago rechazado: tarjeta terminada en 0000.
  await pagarCon(pagina, '4509953566230000');
  await capturar(pagina, 'alumno-pago-rechazado');

  // Pago aprobado: deriva directo a la pantalla de cursado.
  await pagarCon(pagina, '4509953566233704');
  await capturar(pagina, 'alumno-pago-aprobado-y-cursado');

  // Se completan todas las lecciones para llegar al certificado.
  const marcar = pagina.locator('button:has-text("Marcar como completada")');
  while (await marcar.count() > 0) {
    await marcar.first().click();
    await pagina.waitForLoadState('networkidle');
  }
  await capturar(pagina, 'alumno-curso-completado-con-certificado');

  await contexto.close();
}

// --- 5. Alta de academia (CU-10) y creación de curso (CU-11) ------------
// Van al final para no alterar las capturas anteriores: la academia nueva
// aparecería en el listado de usuarios y el curso en el panel de la academia.
console.log('CU-10 y CU-11:');
{
  const { contexto, pagina } = await sesion(ADMIN);
  await pagina.goto(`${BASE}/admin/academias/nueva`);
  await pagina.fill('#razonSocial', 'Escuela Full Stack SAS');
  await pagina.fill('#cuit', '30-71900000-9');
  await pagina.fill('#descripcion', 'Comunidad de desarrollo web con cursos prácticos.');
  await pagina.fill('#nombre', 'Lucia');
  await pagina.fill('#apellido', 'Ortiz');
  await pagina.fill('#email', 'hola@fullstack.com.ar');
  await pagina.fill('#password', CLAVE);
  await capturar(pagina, 'admin-alta-de-academia');
  await contexto.close();
}
{
  const { contexto, pagina } = await sesion(ACADEMIA);
  await pagina.goto(`${BASE}/academia/cursos/nuevo`);
  await pagina.fill('#titulo', 'Docker para desarrolladores');
  await pagina.selectOption('#categoriaId', { label: 'Programacion' });
  await pagina.fill('#descripcion', 'Contenedores, imágenes y Docker Compose aplicados a proyectos Java.');
  await pagina.fill('#precio', '38000');
  await pagina.fill('#tituloModulo', 'Primeros pasos');
  await pagina.fill('#lecciones', 'Qué es un contenedor\nImágenes y capas\nDocker Compose');
  await capturar(pagina, 'academia-nuevo-curso');

  await pagina.click('form[action$="/academia/cursos"] button[type=submit]');
  await pagina.waitForLoadState('networkidle');
  await capturar(pagina, 'academia-curso-creado-en-borrador');
  await contexto.close();
}

await navegador.close();
console.log(`\nListo: ${n} capturas en docs/capturas/`);
