const params = new URLSearchParams(location.search);
const chromeView = params.get('vista');
const activePage = params.get('pagina');
const isStandalone = Boolean(chromeView);

function section(name) {
  return isStandalone ? '../?seccion=' + name : '#' + name;
}

function active(name) {
  return activePage === name ? ' aria-current="page"' : '';
}

const brand = '<img src="logo.webp" width="40" height="40" alt="">TerraSprout';
const header = document.querySelector('[data-site-header]');
const footer = document.querySelector('[data-site-footer]');

if (header) {
  header.innerHTML = `<nav class="wrap" aria-label="Navegación principal">
    <a class="brand" href="${isStandalone ? '../' : '#inicio'}">${brand}</a>
    <div class="links"><a href="${section('video')}">Video</a>
    <a href="${section('proyecto')}">Proyecto</a>
    <a href="${section('equipo')}">Equipo</a>
    <a href="/app/">App</a>
    <div class="header-buttons">
      <a class="header-button" href="/descargas/terracode.apk">Descargar APK</a>
      <a class="header-button header-button-primary" href="/app/">Iniciar sesión</a>
    </div></div>
  </nav>`;
}

if (header && !isStandalone) {
  function markSection() {
    for (const link of header.querySelectorAll('.links a[href^="#"]')) {
      if (link.hash === location.hash) link.setAttribute('aria-current', 'location');
      else link.removeAttribute('aria-current');
    }
  }
  addEventListener('hashchange', markSection);
  markSection();
}

if (footer) {
  footer.innerHTML = `<div class="wrap">
    <a class="brand" href="${isStandalone ? '../' : '#inicio'}">${brand}</a>
    <p>Proyecto del reto Field Shift · NASA Space Apps Challenge 2026</p>
    <nav aria-label="Enlaces del pie de página">
      <a href="${isStandalone ? '../' : '#inicio'}">${isStandalone ? 'Inicio' : 'Volver arriba ↑'}</a>
      <a href="${section('proyecto')}">Proyecto</a>
      <a href="${section('video')}">Video</a>
      <a href="${section('equipo')}">Equipo</a>
      <a href="/app/">Abrir TerraCode ↗</a>
    </nav>
    <p class="footer-credit">Imágenes Blue Marble de la NASA.</p>
  </div>`;
}

if (isStandalone) {
  document.documentElement.classList.add('chrome-view');
  document.body.classList.add('chrome-view', 'chrome-' + chromeView);
  for (const link of document.querySelectorAll('a')) link.target = '_top';
  if (chromeView === 'header') footer.remove();
  if (chromeView === 'footer') header.remove();
}
