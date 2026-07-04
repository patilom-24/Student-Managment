/**
 * faculty.js — Full CRUD for Faculty
 *
 * Endpoints (all relative to BASE_URL):
 *   GET    /faculty                        → List all
 *   GET    /faculty/search?keyword={}      → Search
 *   GET    /faculty/{id}                   → Get one
 *   POST   /faculty                        → Register  (FacultyRegistrationRequest)
 *   PUT    /faculty/{id}                   → Update    (FacultyUpdateRequest)
 *   DELETE /faculty/{id}                   → Delete
 *
 * FacultyRegistrationRequest: employeeId, firstName, lastName, email, phone, specialization, departmentCode
 * FacultyUpdateRequest:       firstName, lastName, email, phone, specialization, departmentCode
 * FacultyResponse:            id, employeeId, firstName, lastName, email, phone, specialization, departmentCode, departmentName
 */

let _editingFacultyId = null;

// ─────────────────────────────────────────────────────────────────────────────
// LOAD ALL   GET /faculty
// ─────────────────────────────────────────────────────────────────────────────
async function loadFaculty() {
    showLoader('Loading faculty...');
    try {
        const res = await apiGet('/faculty');
        renderFacultyTable(res.data || []);
    } catch (err) {
        handleApiError(err, 'Could not load faculty.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SEARCH   GET /faculty/search?keyword={}
// ─────────────────────────────────────────────────────────────────────────────
async function searchFaculty() {
    const keyword = val('faculty-search');
    if (!keyword.trim()) { loadFaculty(); return; }

    showLoader('Searching...');
    try {
        const res = await apiGet('/faculty/search?keyword=' + encodeURIComponent(keyword));
        renderFacultyTable(res.data || []);
    } catch (err) {
        handleApiError(err, 'Search failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RENDER TABLE
// ─────────────────────────────────────────────────────────────────────────────
function renderFacultyTable(faculty) {
    const tbody   = document.getElementById('faculty-tbody');
    const countEl = document.getElementById('faculty-count');
    if (!tbody) return;

    if (countEl) countEl.textContent = faculty.length;

    if (!faculty.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No faculty records found.</td></tr>';
        return;
    }

    tbody.innerHTML = faculty.map(f => {
        const fullName = escHtml(f.firstName) + ' ' + escHtml(f.lastName);
        return `
        <tr>
          <td>${escHtml(f.employeeId)}</td>
          <td>${fullName}</td>
          <td>${escHtml(f.email)}</td>
          <td>${escHtml(f.phone)}</td>
          <td>${escHtml(f.specialization || '—')}</td>
          <td>${escHtml(f.departmentName || f.departmentCode || '—')}</td>
          <td class="action-cell">
            <button class="btn btn-sm btn-primary" onclick="openEditFaculty(${f.id})">✏ Edit</button>
            <button class="btn btn-sm btn-danger"  onclick="deleteFaculty(${f.id}, '${fullName}')">🗑 Delete</button>
          </td>
        </tr>`;
    }).join('');
}

// ─────────────────────────────────────────────────────────────────────────────
// OPEN MODAL — Add
// ─────────────────────────────────────────────────────────────────────────────
function openAddFaculty() {
    _editingFacultyId = null;
    document.getElementById('faculty-modal-title').textContent = 'Register New Faculty';
    document.getElementById('faculty-form').reset();
    clearAllErrors('faculty-form');
    document.getElementById('employeeId').readOnly = false;
    openModal('faculty-modal');
    loadDropdowns(['departments']);
}

// ─────────────────────────────────────────────────────────────────────────────
// OPEN MODAL — Edit   GET /faculty/{id}
// ─────────────────────────────────────────────────────────────────────────────
async function openEditFaculty(id) {
    showLoader('Loading faculty data...');
    try {
        const res = await apiGet('/faculty/' + id);
        const f   = res.data;
        _editingFacultyId = id;

        document.getElementById('faculty-modal-title').textContent = 'Edit Faculty';
        clearAllErrors('faculty-form');

        setVal('employeeId',     f.employeeId);
        setVal('firstName',      f.firstName);
        setVal('lastName',       f.lastName);
        setVal('email',          f.email);
        setVal('phone',          f.phone);
        setVal('specialization', f.specialization || '');
        setVal('departmentCode', f.departmentCode || '');

        document.getElementById('employeeId').readOnly = true;
        openModal('faculty-modal');
        // Re-populate dept dropdown and pre-select the stored dept
        await loadDropdowns(['departments']);
        setVal('departmentCode', f.departmentCode || '');
    } catch (err) {
        handleApiError(err, 'Could not load faculty data.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SAVE — create or update
// ─────────────────────────────────────────────────────────────────────────────
async function saveFaculty() {
    const isEdit = _editingFacultyId !== null;

    const payload = {
        employeeId:     val('employeeId'),
        firstName:      val('firstName'),
        lastName:       val('lastName'),
        email:          val('email'),
        phone:          val('phone'),
        specialization: val('specialization') || null,
        departmentCode: val('departmentCode')
    };

    if (!_validateFacultyForm(payload)) return;

    showLoader(isEdit ? 'Updating faculty...' : 'Registering faculty...');
    try {
        if (isEdit) {
            // PUT — FacultyUpdateRequest (no employeeId)
            const updatePayload = {
                firstName:      payload.firstName,
                lastName:       payload.lastName,
                email:          payload.email,
                phone:          payload.phone,
                specialization: payload.specialization,
                departmentCode: payload.departmentCode
            };
            await apiPut('/faculty/' + _editingFacultyId, updatePayload);
            showToast('Faculty updated successfully!', 'success');
        } else {
            // POST — FacultyRegistrationRequest (includes employeeId)
            await apiPost('/faculty', payload);
            showToast('Faculty registered successfully!', 'success');
        }
        closeModal('faculty-modal');
        loadFaculty();
    } catch (err) {
        handleApiError(err, 'Save failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DELETE   DELETE /faculty/{id}
// ─────────────────────────────────────────────────────────────────────────────
async function deleteFaculty(id, name) {
    const ok = await showConfirm('Delete Faculty', 'Delete "' + name + '"? This cannot be undone.');
    if (!ok) return;

    showLoader('Deleting...');
    try {
        await apiDelete('/faculty/' + id);
        showToast('Faculty deleted successfully!', 'success');
        loadFaculty();
    } catch (err) {
        handleApiError(err, 'Delete failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VALIDATION
// ─────────────────────────────────────────────────────────────────────────────
function _validateFacultyForm(d) {
    clearAllErrors('faculty-form');
    const errs = [];
    const isEdit = _editingFacultyId !== null;

    const checks = [
        ...(isEdit ? [] : [{ res: validateRequired(d.employeeId, 'Employee ID'), id: 'employeeId' }]),
        { res: validateName(d.firstName,  'First Name'), id: 'firstName'      },
        { res: validateName(d.lastName,   'Last Name'),  id: 'lastName'       },
        { res: validateEmail(d.email),                   id: 'email'          },
        { res: validatePhone(d.phone),                   id: 'phone'          },
        { res: validateRequired(d.departmentCode, 'Department'), id: 'departmentCode' },
    ];

    checks.forEach(({ res, id }) => {
        if (!res.valid) { showFieldError(id, res.message); errs.push(id); }
    });
    return errs.length === 0;
}
