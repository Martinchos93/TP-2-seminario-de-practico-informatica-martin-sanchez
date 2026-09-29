/**
 * Genera el PDF entregable a partir de docs/informe/INFORME_TP2.md.
 *
 * Produce «APELLIDO-NOMBRE»-AP2.pdf con portada de Universidad Siglo 21,
 * cuerpo en Carlito 11 (métricamente compatible con Calibri), A4, espaciado
 * simple, encabezado y pie con numeración de página.
 *
 * Requisitos:
 *   npm i marked playwright && npx playwright install chromium
 *
 * Uso:
 *   node docs/generar-pdf.mjs
 */

import { chromium } from 'playwright';
import { marked } from 'marked';
import { PDFDocument } from 'pdf-lib';
import { readFileSync, writeFileSync, unlinkSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const DOCS = resolve(dirname(fileURLToPath(import.meta.url)));
const INFORME = resolve(DOCS, 'informe/INFORME_TP2.md');
const SALIDA = resolve(DOCS, 'informe/SANCHEZ CEJAS-MARTIN ALEJANDRO-AP2.pdf');
const TEMPORAL = resolve(DOCS, 'informe/.informe-temporal.html');

const AUTOR = 'Martín Alejandro Sánchez Cejas';
const LEGAJO = 'VINF016509';
const REPO = "https://github.com/Martinchos93/TP-2-seminario-de-practico-informatica-martin-sanchez";
const MATERIA = 'Seminario de Práctica Informática';
const TRABAJO = 'Trabajo Práctico 2';
const CARRERA = 'Licenciatura en Informática';
const FECHA = new Date().toLocaleDateString('es-AR', {
  day: '2-digit', month: 'long', year: 'numeric',
});

// --- Conversión del Markdown -------------------------------------------
// Se quita la cabecera del .md (título, alumno, legajo): esos datos pasan a
// la portada, y repetirlos en la primera página sería redundante.
const markdown = readFileSync(INFORME, 'utf8');
const cuerpoMd = markdown.slice(markdown.indexOf('## 1. Análisis del modelo de negocio'));

marked.setOptions({ gfm: true, breaks: false });
const cuerpoHtml = marked.parse(cuerpoMd);

/**
 * El documento se arma en dos pasadas y se unen al final:
 *   1. la portada, sin encabezado ni pie ni numeración;
 *   2. el cuerpo, numerado desde 1.
 * Chromium aplica el encabezado y el pie a todas las páginas del documento que
 * imprime, así que separarlo es la única forma de dejar la portada limpia.
 */
const documento = (contenido) => `<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<style>
  /* Carlito: métricamente compatible con Calibri, licencia SIL OFL. */
  @font-face { font-family: 'Carlito'; font-style: normal; font-weight: 400;
               src: url('fuentes/Carlito-Regular.ttf') format('truetype'); }
  @font-face { font-family: 'Carlito'; font-style: normal; font-weight: 700;
               src: url('fuentes/Carlito-Bold.ttf') format('truetype'); }
  @font-face { font-family: 'Carlito'; font-style: italic; font-weight: 400;
               src: url('fuentes/Carlito-Italic.ttf') format('truetype'); }
  @font-face { font-family: 'Carlito'; font-style: italic; font-weight: 700;
               src: url('fuentes/Carlito-BoldItalic.ttf') format('truetype'); }

  @page { size: A4; margin: 2.5cm 2cm 2cm 2cm; }

  body {
    font-family: Calibri, Carlito, sans-serif;
    font-size: 11pt;
    line-height: 1.15;          /* espaciado simple */
    color: #16222e;
    margin: 0;
    text-align: justify;
    hyphens: auto;
  }

  /* --- Portada --- */
  .portada {
    text-align: center;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 23.2cm;   /* alto util de A4 con los margenes de @page */
  }
  .portada .universidad { font-size: 20pt; font-weight: 700; letter-spacing: .04em; }
  .portada .linea { width: 55%; height: 3px; background: #00857a; margin: .9cm auto 1.6cm; }
  .portada .carrera { font-size: 12.5pt; color: #5b6b7a; }
  .portada .trabajo { font-size: 13pt; font-weight: 700; color: #00857a; letter-spacing: .06em; }
  .portada .titulo { font-size: 25pt; font-weight: 700; margin: .5cm 0 .25cm; }
  .portada .subtitulo { font-size: 13pt; color: #5b6b7a; }
  .portada .datos { font-size: 12pt; margin-top: 2.4cm; line-height: 1.7; }
  .portada .datos b { display: inline-block; min-width: 3.1cm; text-align: right; margin-right: .5cm; }
  .portada .repo { font-size: 9.5pt; line-height: 1.4; margin-top: .4cm; }
  .portada .repo b { display: block; min-width: 0; text-align: center; margin: 0; }
  .portada .repo a { color: #00857a; text-decoration: none; }
  .portada .fecha { font-size: 11pt; color: #5b6b7a; }

  /* --- Cuerpo --- */
  h2 {
    font-size: 15pt; color: #00857a; margin: 1.05em 0 .4em;
    padding-bottom: .12em; border-bottom: 1.5px solid #dde4ea;
    page-break-after: avoid; text-align: left;
  }
  h2:first-of-type { margin-top: 0; }
  h3 { font-size: 12.5pt; margin: .95em 0 .3em; page-break-after: avoid; text-align: left; }
  h4 { font-size: 11pt; margin: .8em 0 .25em; page-break-after: avoid; text-align: left; }

  p { margin: 0 0 .5em; orphans: 2; widows: 2; }
  ul, ol { margin: .3em 0 .6em; padding-left: 1.5em; }
  li { margin-bottom: .18em; }

  strong { font-weight: 700; }

  table {
    border-collapse: collapse; width: 100%;
    margin: .55em 0 .9em; font-size: 9.5pt;
    page-break-inside: avoid; text-align: left;
  }
  th, td { border: 1px solid #c8d2da; padding: 4px 7px; vertical-align: top; text-align: left; }
  /* Identificadores (RF-01, CP-23, CU-10) y nombres de campo no se cortan con guion. */
  td:first-child { white-space: nowrap; hyphens: manual; }
  th { background: #e4f3f1; font-weight: 700; }

  code {
    font-family: Consolas, "DejaVu Sans Mono", monospace;
    font-size: 9pt; background: #f1f4f6; padding: .5px 3px; border-radius: 2px;
  }
  pre {
    background: #f7f9fa; border: 1px solid #dde4ea; border-left: 3px solid #00857a;
    border-radius: 3px; padding: 7px 10px; margin: .5em 0 .9em;
    font-size: 8.5pt; line-height: 1.32; overflow: hidden;
    white-space: pre-wrap; word-break: break-word;
    page-break-inside: avoid; text-align: left;
  }
  pre code { background: none; padding: 0; font-size: inherit; }

  img {
    max-width: 100%; height: auto; display: block; margin: .5em auto .3em;
    border: 1px solid #dde4ea; border-radius: 3px;
    page-break-inside: avoid;
  }
  /* Las capturas van en tablas de dos columnas: se las acota para que la
     comparación entre dos pantallas entre en una sola página. */
  td img { max-height: 8.5cm; }

  blockquote {
    margin: .5em 0; padding: .3em .8em;
    border-left: 3px solid #00857a; background: #f7f9fa; color: #5b6b7a;
  }
  hr { border: none; border-top: 1px solid #dde4ea; margin: 1.1em 0; }
  a { color: #00857a; text-decoration: none; }
</style>
</head>
<body>
${contenido}
</body>
</html>`;

const portadaHtml = `
<div class="portada">
  <div>
    <div class="universidad">UNIVERSIDAD SIGLO 21</div>
    <div class="linea"></div>
    <div class="carrera">${CARRERA}<br>${MATERIA}</div>
  </div>

  <div>
    <div class="trabajo">${TRABAJO.toUpperCase()}</div>
    <div class="titulo">Tribu</div>
    <div class="subtitulo">Plataforma de comunidades con cursos pagos</div>

    <div class="datos">
      <div><b>Alumno:</b> ${AUTOR}</div>
      <div><b>Legajo:</b> ${LEGAJO}</div>
      <div class="repo"><b>Repositorio:</b> <a href="${REPO}">${REPO.replace("https://", "")}</a></div>
    </div>
  </div>

  <div class="fecha">${FECHA}</div>
</div>`;

const estiloBanda = 'font-family:Carlito,sans-serif;font-size:8pt;color:#5b6b7a;width:100%;padding:0 2cm;';
const MARGENES = { top: '2.2cm', right: '2cm', bottom: '1.9cm', left: '2cm' };

const navegador = await chromium.launch();
const pagina = await navegador.newPage();

/** Imprime un fragmento HTML a PDF y devuelve los bytes. */
async function imprimir(contenido, opciones) {
  writeFileSync(TEMPORAL, documento(contenido), 'utf8');
  // Se carga por file:// para que las rutas relativas de imágenes y fuentes
  // resuelvan igual que en el Markdown.
  await pagina.goto('file://' + TEMPORAL, { waitUntil: 'networkidle' });
  await pagina.emulateMedia({ media: 'print' });
  // Los diagramas UML se renderizan a 200 dpi para que se vean nítidos:
  // se los escala al tamaño que tendrían a 96 dpi para que no desborden.
  await pagina.evaluate(() => {
    for (const img of document.querySelectorAll('img[src*="/uml/"]')) {
      img.style.width = `${img.naturalWidth * 96 / 200}px`;
    }
  });
  return pagina.pdf({ format: 'A4', printBackground: true, margin: MARGENES, ...opciones });
}

const portadaPdf = await imprimir(portadaHtml, { displayHeaderFooter: false });

const cuerpoPdf = await imprimir(cuerpoHtml, {
  displayHeaderFooter: true,
  headerTemplate: `<div style="${estiloBanda}display:flex;justify-content:space-between;border-bottom:.5px solid #dde4ea;padding-bottom:3px;">
      <span>Universidad Siglo 21 · ${MATERIA}</span>
      <span>${TRABAJO}</span>
    </div>`,
  footerTemplate: `<div style="${estiloBanda}display:flex;justify-content:space-between;border-top:.5px solid #dde4ea;padding-top:3px;">
      <span>${AUTOR} — ${LEGAJO}</span>
      <span>Página <span class="pageNumber"></span> de <span class="totalPages"></span></span>
    </div>`,
});

await navegador.close();
unlinkSync(TEMPORAL);

// --- Unión: portada sin numerar + cuerpo numerado -----------------------
const final = await PDFDocument.create();
for (const bytes of [portadaPdf, cuerpoPdf]) {
  const origen = await PDFDocument.load(bytes);
  const paginas = await final.copyPages(origen, origen.getPageIndices());
  paginas.forEach((p) => final.addPage(p));
}

final.setTitle(`${TRABAJO} — Tribu`);
final.setAuthor(AUTOR);
final.setSubject(MATERIA);

writeFileSync(SALIDA, await final.save());

console.log(`PDF generado (${final.getPageCount()} páginas):`);
console.log('  ' + SALIDA);
