/**
 * auth.js — Login, logout, token storage, route guard
 *
 * Reads/writes to localStorage.
 * TOKEN_KEY and USER_KEY match the constants in api.js.
 */

// ── Save token + user object after login ─────────────────────────────────────
function saveAuth(authData) {
    // authData = AuthResponse: { accessToken, tokenType, expiresIn, username, email, role }
    localStorage.setItem(TOKEN_KEY, authData.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify({
        username: authData.username,
        email:    authData.email,
        role:     authData.role       // e.g. "ROLE_ADMIN"
    }));
}

// ── Get stored user object ────────────────────────────────────────────────────
function getCurrentUser() {
    try {
        return JSON.parse(localStorage.getItem(USER_KEY));
    } catch (e) {
        return null;
    }
}

// ── Is the user logged in? ────────────────────────────────────────────────────
function isLoggedIn() {
    return !!localStorage.getItem(TOKEN_KEY);
}

// ── Logout — clear storage and go to login ────────────────────────────────────
function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.href = '/page/login';
}

// ── Guard: called at top of every protected page ──────────────────────────────
function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = '/page/login';
        return false;
    }
    return true;
}

// ── Guard: used on login page so logged-in users skip it ─────────────────────
function redirectIfLoggedIn() {
    if (isLoggedIn()) {
        window.location.href = '/page/home';
    }
}

// ── Populate sidebar user info after requireAuth() ────────────────────────────
function renderUserInfo() {
    const user   = getCurrentUser();
    const nameEl = document.getElementById('nav-user-name');
    const roleEl = document.getElementById('nav-user-role');
    const avEl   = document.getElementById('nav-avatar');

    if (!user) return;
    if (nameEl) nameEl.textContent = user.username;
    if (roleEl) roleEl.textContent = user.role ? user.role.replace('ROLE_', '') : '';
    if (avEl)   avEl.textContent   = user.username.charAt(0).toUpperCase();
}

// ── Login — POST /api/v1/auth/login ──────────────────────────────────────────
//    Called by login.html inline script.
async function doLogin(username, password) {
    // Uses apiPostPublic (no auth header) from api.js
    const res = await apiPostPublic('/auth/login', { username, password });

    // ApiResponse<AuthResponse>  →  res.data = AuthResponse
    if (res && res.data && res.data.accessToken) {
        saveAuth(res.data);
        return res.data;
    }
    throw new Error(res?.message || 'Login failed. Unexpected response from server.');
}
