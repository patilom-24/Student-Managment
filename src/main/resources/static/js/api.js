/**
 * api.js — Reusable Fetch wrapper
 *
 * BASE URL is hardcoded here. Change only this one constant
 * if the backend moves to a different host/port.
 *
 * Flow (Day 1 & 2):
 *   JavaScript → fetch() → HTTP Request → Spring Boot → JSON → JavaScript
 */

const BASE_URL    = 'http://localhost:8080/api/v1';
const TIMEOUT_MS  = 10000;           // 10 second timeout
const TOKEN_KEY   = 'jwtToken';      // localStorage key for JWT
const USER_KEY    = 'currentUser';   // localStorage key for user info

// ── Auth header builder ──────────────────────────────────────────────────────
function _getHeaders() {
    const token = localStorage.getItem(TOKEN_KEY);
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    return headers;
}

// ── Response parser — throws ApiError on any non-2xx ────────────────────────
async function _handleResponse(response) {
    let data;
    try {
        data = await response.json();
    } catch (e) {
        if (!response.ok) throw new ApiError('Server returned non-JSON error.', response.status, null);
        return null;
    }

    if (!response.ok) {
        // Try to extract the backend error message in multiple formats
        const message =
            data?.message ||
            data?.error ||
            (Array.isArray(data?.errors) ? data.errors.map(e => e.message || e.field + ': ' + e.defaultMessage).join(' | ') : null) ||
            'HTTP ' + response.status;
        throw new ApiError(message, response.status, data);
    }
    return data;
}

// ── Custom error class ───────────────────────────────────────────────────────
class ApiError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name   = 'ApiError';
        this.status = status;
        this.data   = data;
    }
}

// ── fetch with AbortController timeout ──────────────────────────────────────
async function _fetchWithTimeout(url, options) {
    const ctrl = new AbortController();
    const tid  = setTimeout(() => ctrl.abort(), TIMEOUT_MS);
    try {
        const res = await fetch(url, { ...options, signal: ctrl.signal });
        return res;
    } catch (err) {
        if (err.name === 'AbortError')
            throw new ApiError('Request timed out. Please try again.', 408, null);
        throw new ApiError('Network error. Is the server running on port 8080?', 0, null);
    } finally {
        clearTimeout(tid);
    }
}

// ── GET /api/v1/<endpoint> ───────────────────────────────────────────────────
async function apiGet(endpoint) {
    const res = await _fetchWithTimeout(BASE_URL + endpoint, {
        method:  'GET',
        headers: _getHeaders()
    });
    return _handleResponse(res);
}

// ── POST /api/v1/<endpoint> ──────────────────────────────────────────────────
async function apiPost(endpoint, body) {
    const res = await _fetchWithTimeout(BASE_URL + endpoint, {
        method:  'POST',
        headers: _getHeaders(),
        body:    JSON.stringify(body)
    });
    return _handleResponse(res);
}

// ── PUT /api/v1/<endpoint> ───────────────────────────────────────────────────
async function apiPut(endpoint, body) {
    const res = await _fetchWithTimeout(BASE_URL + endpoint, {
        method:  'PUT',
        headers: _getHeaders(),
        body:    JSON.stringify(body)
    });
    return _handleResponse(res);
}

// ── DELETE /api/v1/<endpoint> ────────────────────────────────────────────────
async function apiDelete(endpoint) {
    const res = await _fetchWithTimeout(BASE_URL + endpoint, {
        method:  'DELETE',
        headers: _getHeaders()
    });
    return _handleResponse(res);
}

// ── POST without auth — used ONLY for login ──────────────────────────────────
async function apiPostPublic(endpoint, body) {
    const res = await _fetchWithTimeout(BASE_URL + endpoint, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify(body)
    });
    return _handleResponse(res);
}
