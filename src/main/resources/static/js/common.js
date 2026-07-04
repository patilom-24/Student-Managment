/**
 * common.js — Shared UI utilities
 * Toast notifications, loader, modal helpers, date formatting.
 */

// ── Toast Notification ──────────────────────────────────────────────────────
let _toastTimer = null;

function showToast(message, type = 'success', duration = 3500) {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    const icons = { success: '✓', error: '✕', warning: '⚠', info: 'ℹ' };
    toast.innerHTML = `<span class="toast-icon">${icons[type] || icons.info}</span>
                       <span class="toast-msg">${message}</span>`;

    container.appendChild(toast);
    // Trigger animation
    requestAnimationFrame(() => toast.classList.add('toast-show'));

    setTimeout(() => {
        toast.classList.remove('toast-show');
        toast.addEventListener('transitionend', () => toast.remove(), { once: true });
    }, duration);
}

// ── Loading Spinner ─────────────────────────────────────────────────────────
function showLoader(message = 'Loading...') {
    let overlay = document.getElementById('loader-overlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'loader-overlay';
        overlay.innerHTML = `<div class="loader-box">
            <div class="spinner"></div>
            <p class="loader-msg" id="loader-msg">${message}</p>
        </div>`;
        document.body.appendChild(overlay);
    } else {
        const msg = document.getElementById('loader-msg');
        if (msg) msg.textContent = message;
    }
    overlay.style.display = 'flex';
}

function hideLoader() {
    const overlay = document.getElementById('loader-overlay');
    if (overlay) overlay.style.display = 'none';
}

// ── Confirm Modal ───────────────────────────────────────────────────────────
function showConfirm(title, message) {
    return new Promise(resolve => {
        let modal = document.getElementById('confirm-modal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'confirm-modal';
            modal.className = 'modal-overlay';
            modal.innerHTML = `
                <div class="modal-box">
                    <h3 id="confirm-title"></h3>
                    <p id="confirm-message"></p>
                    <div class="modal-actions">
                        <button class="btn btn-danger"  id="confirm-yes">Yes, Delete</button>
                        <button class="btn btn-secondary" id="confirm-no">Cancel</button>
                    </div>
                </div>`;
            document.body.appendChild(modal);
        }
        document.getElementById('confirm-title').textContent   = title;
        document.getElementById('confirm-message').textContent = message;
        modal.style.display = 'flex';

        const yes = document.getElementById('confirm-yes');
        const no  = document.getElementById('confirm-no');

        const cleanup = (val) => {
            modal.style.display = 'none';
            yes.replaceWith(yes.cloneNode(true));  // remove stale listeners
            no.replaceWith(no.cloneNode(true));
            resolve(val);
        };

        document.getElementById('confirm-yes').addEventListener('click', () => cleanup(true),  { once: true });
        document.getElementById('confirm-no') .addEventListener('click', () => cleanup(false), { once: true });
        modal.addEventListener('click', e => { if (e.target === modal) cleanup(false); }, { once: true });
    });
}

// ── Generic Modal open/close ─────────────────────────────────────────────────
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) { modal.style.display = 'flex'; modal.classList.add('active'); }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) { modal.style.display = 'none'; modal.classList.remove('active'); }
}

// ── Date Formatting ──────────────────────────────────────────────────────────
function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function formatCurrency(amount) {
    if (amount === null || amount === undefined) return '—';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount);
}

// ── Empty-state helper ───────────────────────────────────────────────────────
function showEmptyState(tbodyId, colspan, message = 'No records found.') {
    const tbody = document.getElementById(tbodyId);
    if (tbody)
        tbody.innerHTML = `<tr><td colspan="${colspan}" class="empty-state">${message}</td></tr>`;
}

// ── Get value from input safely ──────────────────────────────────────────────
function val(id) {
    const el = document.getElementById(id);
    return el ? el.value.trim() : '';
}

// ── Set input value safely ───────────────────────────────────────────────────
function setVal(id, value) {
    const el = document.getElementById(id);
    if (el) el.value = (value !== null && value !== undefined) ? value : '';
}

// ── Status badge HTML ────────────────────────────────────────────────────────
function statusBadge(status) {
    if (!status) return '—';
    const colorMap = {
        ACTIVE:    'badge-success',
        INACTIVE:  'badge-secondary',
        SUSPENDED: 'badge-danger',
        PRESENT:   'badge-success',
        ABSENT:    'badge-danger',
        LATE:      'badge-warning',
        PAID:      'badge-success',
        PENDING:   'badge-warning',
        OVERDUE:   'badge-danger',
        PARTIAL:   'badge-info'
    };
    const cls = colorMap[status] || 'badge-secondary';
    return `<span class="badge ${cls}">${status}</span>`;
}

// ── Handle API error uniformly ───────────────────────────────────────────────
function handleApiError(err, fallbackMsg = 'Something went wrong.') {
    console.error('[API Error]', err);
    if (err.status === 401) {
        showToast('Session expired. Please login again.', 'error');
        setTimeout(() => window.location.href = '/page/login', 1500);
        return;
    }
    if (err.status === 403) {
        showToast('You do not have permission to do that.', 'error');
        return;
    }
    showToast(err.message || fallbackMsg, 'error');
}

// ── Escape HTML to prevent XSS ──────────────────────────────────────────────
function escHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}
