const header = document.querySelector('header');
const introVideo = document.querySelector('#video');

function updateHeader() {
  header.classList.toggle('show-divider', introVideo.getBoundingClientRect().bottom <= header.offsetHeight);
}

addEventListener('scroll', updateHeader, { passive: true });
addEventListener('resize', updateHeader);
new ResizeObserver(updateHeader).observe(introVideo);
updateHeader();
