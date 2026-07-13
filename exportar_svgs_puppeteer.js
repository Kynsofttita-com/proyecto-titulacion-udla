#!/usr/bin/env node

const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

const diagramas = [
  { numero: 1, nombre: 'diagrama-1-contexto-sistema', selector: '.mermaid:nth-child(1)' },
  { numero: 2, nombre: 'diagrama-2-procesos-principales', selector: '.mermaid:nth-child(2)' },
  { numero: 3, nombre: 'diagrama-3-interaccion-microservicios', selector: '.mermaid:nth-child(3)' },
  { numero: 4, nombre: 'diagrama-4-base-datos', selector: '.mermaid:nth-child(4)' },
  { numero: 5, nombre: 'diagrama-5-rabbitmq', selector: '.mermaid:nth-child(5)' },
  { numero: 6, nombre: 'diagrama-6-docker-compose', selector: '.mermaid:nth-child(6)' },
  { numero: 7, nombre: 'diagrama-7-validaciones-testing', selector: '.mermaid:nth-child(7)' }
];

async function exportarSVGs() {
  let browser;
  try {
    // Instalar puppeteer si es necesario
    console.log('Instalando Puppeteer...');
    const { execSync } = require('child_process');
    try {
      require.resolve('puppeteer');
    } catch (e) {
      execSync('npm install -g puppeteer', { stdio: 'inherit' });
    }

    console.log('Iniciando navegador...\n');
    browser = await puppeteer.launch({
      headless: 'new',
      args: ['--no-sandbox', '--disable-setuid-sandbox']
    });

    const page = await browser.newPage();
    const htmlPath = path.join(__dirname, 'diagramas-generados.html');
    const fileUrl = `file:///${htmlPath}`;

    console.log(`Abriendo: ${fileUrl}`);
    await page.goto(fileUrl, { waitUntil: 'networkidle0', timeout: 30000 });

    // Esperar a que se carguen los diagramas
    console.log('Esperando que se carguen los diagramas...');
    await page.waitForSelector('.mermaid svg', { timeout: 30000 }).catch(() => {
      console.log('SVGs no detectados, continuando...');
    });
    await new Promise(resolve => setTimeout(resolve, 3000));

    console.log('Exportando diagramas a SVG...\n');

    for (const diagrama of diagramas) {
      try {
        // Encontrar el contenedor del diagrama
        const svgHandle = await page.$eval(
          `.diagram-content:nth-child(2) > .mermaid svg`,
          (el) => el.outerHTML
        ).catch(async () => {
          // Fallback: buscar por indice
          return await page.$eval(
            `.diagram-section:nth-child(${diagrama.numero}) .mermaid svg`,
            (el) => el.outerHTML
          );
        });

        if (svgHandle) {
          const outputPath = path.join(__dirname, `${diagrama.nombre}.svg`);
          fs.writeFileSync(outputPath, svgHandle);
          console.log(`[OK] ${diagrama.nombre}.svg - ${fs.statSync(outputPath).size} bytes`);
        }
      } catch (error) {
        console.error(`[ERROR] ${diagrama.nombre}: ${error.message}`);
      }
    }

    await browser.close();
    console.log('\n[SUCCESS] Todos los diagramas han sido exportados!');

  } catch (error) {
    console.error('Error:', error.message);
    if (browser) await browser.close();
    process.exit(1);
  }
}

exportarSVGs();
