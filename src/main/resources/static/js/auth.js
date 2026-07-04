/**
 * auth.js — Login, logout, token storage, route guards, role helpers
 *
 * Roles stored in localStorage after login: ADMIN | FACULTY | STUDENT
 * The Role enum from Spring Boot serialises as plain string (no ROLE_ prefix).
 */

// ── Save token + user after login ────────────────────────────────────────────
function saveAuth(authData) {
    localStorage.setItem(TOKEN_KEY, authData.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify({
        username: authData.username,
        email:    authData.email,
        role:     String(authData.role).replace('ROLE_', '')  // normalise: always "ADMIN" etc.
    }));
}

// ── Read stored user ──────────────────────────────────────────────────────────
function getCurrentUser() {
    try { return JSON.parse(localStorage.getItem(USER_KEY)); }
    catch (e) { return null; }
}

function getRole() {
    const u = getCurrentUser();
    return u ? String(u.role).replace('ROLE_', '').toUpperCase() : '';
}

// ── Role helpers ──────────────────────────────────────────────────────────────
function isAdmin()   { return getRole() === 'ADMIN';   }
function isFaculty() { return getRole() === 'FACULTY'; }
function isStudent() { return getRole() === 'STUDENT'; }

// ── Token helpers ─────────────────────────────────────────────────────────────
function isLoggedIn() { return !!localStorage.getItem(TOKEN_KEY); }
function getToken()   { return localStorage.getItem(TOKEN_KEY);   }

// ── Logout ────────────────────────────────────────────────────────────────────
function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.href = '/page/login';
}

// ── Guard: redirect to login if not authenticated ─────────────────────────────
function requireAuth() {
    if (!isLoggedIn()) { window.location.href = '/page/login'; return false; }
    return true;
}

// ── Guard: redirect if role not allowed (pass array of allowed roles) ─────────
function requireRole(allowedRoles) {
    if (!requireAuth()) return false;
    if (!allowedRoles.includes(getRole())) {
        redirectByRole();   // send to their correct home
        return false;
    }
    return true;
}

// ── Redirect each role to its correct home page ───────────────────────────────
function redirectByRole() {
    const r = getRole();
    if (r === 'ADMIN')   { window.location.href = '/page/home';           return; }
    if (r === 'FACULTY') { window.location.href = '/page/faculty-portal'; return; }
    if (r === 'STUDENT') { window.location.href = '/page/student-portal'; return; }
    window.location.href = '/page/login';
}

// ── Used on login page — skip if already logged in ────────────────────────────
function redirectIfLoggedIn() {
    if (isLoggedIn()) redirectByRole();
}

// ── Populate sidebar with user info ───────────────────────────────────────────
function renderUserInfo() {
    const user = getCurrentUser();
    if (!user) return;
    const nameEl = document.getElementById('nav-user-name');
    const roleEl = document.getElementById('nav-user-role');
    const avEl   = document.getElementById('nav-avatar');
    if (nameEl) nameEl.textContent = user.username;
    if (roleEl) roleEl.textContent = user.role;
    if (avEl)   avEl.textContent   = user.username.charAt(0).toUpperCase();
}

// ── Apply role-based UI visibility on the current page ───────────────────────
// Elements with data-role="ADMIN"          → shown only to ADMIN
// Elements with data-role="ADMIN,FACULTY"  → shown to ADMIN and FACULTY
// Elements with data-hide-role="STUDENT"   → hidden from STUDENT
function applyRoleVisibility() {
    const role = getRole();
    document.querySelectorAll('[data-role]').forEach(el => {
        const allowed = el.dataset.role.split(',').map(r => r.trim().toUpperCase());
        el.style.display = allowed.includes(role) ? '' : 'none';
    });
    document.querySelectorAll('[data-hide-role]').forEach(el => {
        const hidden = el.dataset.hideRole.split(',').map(r => r.trim().toUpperCase());
        if (hidden.includes(role)) el.style.display = 'none';
    });
}

// ── Login call — POST /api/v1/auth/login ──────────────────────────────────────
async function doLogin(username, password) {
    const res = await apiPostPublic('/auth/login', { username, password });
    if (res && res.data && res.data.accessToken) {
        saveAuth(res.data);
        return res.data;
    }
    throw new Error(res?.message || 'Login failed.');
}
