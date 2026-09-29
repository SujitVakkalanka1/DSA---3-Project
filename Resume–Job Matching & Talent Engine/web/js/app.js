/**
 * Talent Engine Common UI Utilities
 * Course: DSA-3 (25CS2103E) - Team 14
 */

const UI = {
  // Toast notifications
  toast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:9999;display:flex;flex-direction:column;gap:8px;';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast-msg';
    
    let borderColor = 'var(--signal-lime)';
    let bgColor = 'var(--bg-card)';
    let textColor = 'var(--text-cream)';
    
    if (type === 'error') {
      borderColor = 'var(--alert-red-light)';
      bgColor = 'var(--alert-red-bg)';
    } else if (type === 'success') {
      borderColor = 'var(--accent-green)';
      bgColor = 'rgba(34, 197, 94, 0.15)';
    }

    toast.style.cssText = `background:${bgColor};border:1px solid ${borderColor};color:${textColor};padding:10px 16px;border-radius:4px;font-size:0.85rem;box-shadow:0 4px 14px rgba(0,0,0,0.8);display:flex;align-items:center;gap:8px;animation:fadeIn 0.2s ease;font-family:var(--font-mono);`;
    toast.innerHTML = `<span>${message}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.25s ease';
      setTimeout(() => toast.remove(), 250);
    }, 3200);
  },

  // Modal open/close helpers
  openModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.add('active');
  },

  closeModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.remove('active');
  },

  // Render score pill element
  renderScorePill(score) {
    let cls = 'low';
    if (score >= 75) cls = 'high';
    else if (score >= 50) cls = 'medium';
    return `<span class="score-pill ${cls}">${score.toFixed(1)}%</span>`;
  },

  // Render skill chips
  renderSkillChips(skills, type = 'canonical') {
    if (!skills || !skills.length) return '<span style="color:var(--text-muted);font-size:0.75rem;font-family:var(--font-mono);">None</span>';
    return skills.map(s => {
      let cls = 'skill-badge';
      if (type === 'matched') cls += ' matched';
      else if (type === 'missing') cls += ' missing';
      return `<span class="${cls}">${s}</span>`;
    }).join('');
  },

  // Highlight active link in navbar based on window.location
  highlightActiveNav() {
    const path = window.location.pathname;
    const links = document.querySelectorAll('.nav-link');
    links.forEach(link => {
      const href = link.getAttribute('href');
      if (href === path || (path === '/' && href === '/') || (path.includes(href) && href !== '/')) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });
  },

  initNav() {
    this.highlightActiveNav();
    const toggle = document.querySelector('.nav-mobile-toggle');
    const links = document.querySelector('.nav-links');
    if (toggle && links) {
      toggle.addEventListener('click', () => {
        links.classList.toggle('mobile-open');
      });
    }
  }
};

document.addEventListener('DOMContentLoaded', () => {
  UI.initNav();
});
