/**
 * student.js — Full CRUD for Students
 *
 * Endpoints used (all relative to BASE_URL = http://localhost:8080/api/v1):
 *   GET    /students                         → List all students
 *   GET    /students/search?keyword={}       → Search students
 *   GET    /students/{id}                    → Get one student
 *   POST   /students                         → Register student  (StudentRegistrationRequest)
 *   PUT    /students/{id}                    → Update student    (StudentUpdateRequest)
 *   DELETE /students/{id}                    → Delete student
 *
 * StudentRegistrationRequest fields:
 *   studentId, firstName, lastName, email, phone,
 *   dateOfBirth (LocalDate), gender (MALE|FEMALE|OTHER),
 *   departmentCode, courseCode, semester, academicYear
 *
 * StudentUpdateRequest fields:
 *   firstName, lastName, email, phone,
 *   dateOfBirth, gender, status (ACTIVE|INACTIVE|SUSPENDED), departmentCode
 *
 * StudentResponse fields:
 *   id, studentId, firstName, lastName, email, phone,
 *   dateOfBirth, gender, status, enrollmentDate,
 *   departmentCode, departmentName
 */

let _editingStudentId = null;   // null → create mode | number → edit mode

// ─────────────────────────────────────────────────────────────────────────────
// LOAD ALL STUDENTS
// GET /students  →  ApiResponse<List<StudentResponse>>
// ─────────────────────────────────────────────────────────────────────────────
async function loadStudents() {
    showLoader('Loading students...');
    try {
        const res = await apiGet('/students');
        renderStudentTable(res.data || []);
    } catch (err) {
        handleApiError(err, 'Could not load students.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SEARCH STUDENTS
// GET /students/search?keyword={}
// ─────────────────────────────────────────────────────────────────────────────
async function searchStudents() {
    const keyword = val('student-search');
    if (!keyword.trim()) { loadStudents(); return; }

    showLoader('Searching...');
    try {
        const res = await apiGet('/students/search?keyword=' + encodeURIComponent(keyword));
        renderStudentTable(res.data || []);
    } catch (err) {
        handleApiError(err, 'Search failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RENDER TABLE  (DOM manipulation — no page reload)
// ─────────────────────────────────────────────────────────────────────────────
function renderStudentTable(students) {
    const tbody   = document.getElementById('student-tbody');
    const countEl = document.getElementById('student-count');
    if (!tbody) return;

    if (countEl) countEl.textContent = students.length;

    if (!students.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No students found.</td></tr>';
        return;
    }

    tbody.innerHTML = students.map(s => {
        const fullName = escHtml(s.firstName) + ' ' + escHtml(s.lastName);
        return `
        <tr>
          <td>${escHtml(s.studentId)}</td>
          <td>${fullName}</td>
          <td>${escHtml(s.email)}</td>
          <td>${escHtml(s.phone)}</td>
          <td>${escHtml(s.departmentName || s.departmentCode || '—')}</td>
          <td>${formatDate(s.enrollmentDate)}</td>
          <td>${statusBadge(s.status)}</td>
          <td class="action-cell">
            <button class="btn btn-sm btn-primary" onclick="openEditStudent(${s.id})">✏ Edit</button>
            <button class="btn btn-sm btn-danger"  onclick="deleteStudent(${s.id}, '${fullName}')">🗑 Delete</button>
          </td>
        </tr>`;
    }).join('');
}

// ─────────────────────────────────────────────────────────────────────────────
// OPEN MODAL — Add mode
// ─────────────────────────────────────────────────────────────────────────────
function openAddStudent() {
    _editingStudentId = null;
    document.getElementById('student-modal-title').textContent = 'Register New Student';
    document.getElementById('student-form').reset();
    clearAllErrors('student-form');

    document.getElementById('studentId').readOnly = false;
    document.getElementById('reg-only-fields').style.display = 'grid';
    document.getElementById('status-group').style.display    = 'none';

    openModal('student-modal');
    // Re-populate dropdowns so they show the latest data
    loadDropdowns(['departments', 'courses']);
}

// ─────────────────────────────────────────────────────────────────────────────
// OPEN MODAL — Edit mode
// GET /students/{id}
// ─────────────────────────────────────────────────────────────────────────────
async function openEditStudent(id) {
    showLoader('Loading student data...');
    try {
        const res = await apiGet('/students/' + id);
        const s   = res.data;
        _editingStudentId = id;

        document.getElementById('student-modal-title').textContent = 'Edit Student';
        clearAllErrors('student-form');

        // Populate all fields from StudentResponse
        setVal('studentId',      s.studentId);
        setVal('firstName',      s.firstName);
        setVal('lastName',       s.lastName);
        setVal('email',          s.email);
        setVal('phone',          s.phone);
        setVal('dateOfBirth',    s.dateOfBirth   || '');
        setVal('gender',         s.gender        || '');
        setVal('departmentCode', s.departmentCode || '');

        // status is part of StudentUpdateRequest
        setVal('status', s.status || 'ACTIVE');

        // studentId is read-only in edit mode
        document.getElementById('studentId').readOnly = true;

        // Hide registration-only fields (courseCode, semester, academicYear)
        document.getElementById('reg-only-fields').style.display = 'none';
        // Show status field (only in edit mode)
        document.getElementById('status-group').style.display    = 'block';

        openModal('student-modal');
        // Re-populate dept dropdown and pre-select the stored value
        await loadDropdowns(['departments']);
        setVal('departmentCode', s.departmentCode || '');
    } catch (err) {
        handleApiError(err, 'Could not load student data.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SAVE — routes to create or update
// ─────────────────────────────────────────────────────────────────────────────
async function saveStudent() {
    const isEdit = _editingStudentId !== null;

    if (isEdit) {
        await _updateStudent();
    } else {
        await _createStudent();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CREATE STUDENT
// POST /students   body → StudentRegistrationRequest
// ─────────────────────────────────────────────────────────────────────────────
async function _createStudent() {
    const payload = {
        studentId:      val('studentId'),
        firstName:      val('firstName'),
        lastName:       val('lastName'),
        email:          val('email'),
        phone:          val('phone'),
        dateOfBirth:    val('dateOfBirth')    || null,   // LocalDate string "YYYY-MM-DD"
        gender:         val('gender')         || null,   // MALE | FEMALE | OTHER | null
        departmentCode: val('departmentCode'),
        courseCode:     val('courseCode'),
        semester:       val('semester'),
        academicYear:   val('academicYear')
    };

    if (!_validateRegistrationForm(payload)) return;

    showLoader('Registering student...');
    try {
        const res = await apiPost('/students', payload);
        showToast('Student registered successfully!', 'success');
        closeModal('student-modal');
        loadStudents();
    } catch (err) {
        handleApiError(err, 'Registration failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UPDATE STUDENT
// PUT /students/{id}   body → StudentUpdateRequest
// ─────────────────────────────────────────────────────────────────────────────
async function _updateStudent() {
    const payload = {
        firstName:      val('firstName'),
        lastName:       val('lastName'),
        email:          val('email'),
        phone:          val('phone'),
        dateOfBirth:    val('dateOfBirth')    || null,
        gender:         val('gender')         || null,
        status:         val('status')         || null,   // StudentStatus enum
        departmentCode: val('departmentCode') || null
    };

    if (!_validateUpdateForm(payload)) return;

    showLoader('Updating student...');
    try {
        await apiPut('/students/' + _editingStudentId, payload);
        showToast('Student updated successfully!', 'success');
        closeModal('student-modal');
        loadStudents();
    } catch (err) {
        handleApiError(err, 'Update failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DELETE STUDENT
// DELETE /students/{id}
// ─────────────────────────────────────────────────────────────────────────────
async function deleteStudent(id, name) {
    const ok = await showConfirm('Delete Student', 'Delete "' + name + '"? This cannot be undone.');
    if (!ok) return;

    showLoader('Deleting student...');
    try {
        await apiDelete('/students/' + id);
        showToast('Student deleted successfully!', 'success');
        loadStudents();
    } catch (err) {
        handleApiError(err, 'Delete failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VALIDATION — Registration (all required fields)
// ─────────────────────────────────────────────────────────────────────────────
function _validateRegistrationForm(d) {
    clearAllErrors('student-form');
    const errs = [];

    const checks = [
        { res: validateRequired(d.studentId,      'Student ID'),    id: 'studentId'      },
        { res: validateName(d.firstName,           'First Name'),    id: 'firstName'      },
        { res: validateName(d.lastName,            'Last Name'),     id: 'lastName'       },
        { res: validateEmail(d.email),                               id: 'email'          },
        { res: validatePhone(d.phone),                               id: 'phone'          },
        { res: validatePastDate(d.dateOfBirth,     'Date of Birth'), id: 'dateOfBirth'    },
        { res: validateRequired(d.departmentCode,  'Department'),    id: 'departmentCode' },
        { res: validateRequired(d.courseCode,      'Course Code'),   id: 'courseCode'     },
        { res: validateRequired(d.semester,        'Semester'),      id: 'semester'       },
        { res: validateRequired(d.academicYear,    'Academic Year'), id: 'academicYear'   },
    ];

    checks.forEach(({ res, id }) => {
        if (!res.valid) { showFieldError(id, res.message); errs.push(id); }
    });
    return errs.length === 0;
}

// ─────────────────────────────────────────────────────────────────────────────
// VALIDATION — Update (only populated fields validated)
// ─────────────────────────────────────────────────────────────────────────────
function _validateUpdateForm(d) {
    clearAllErrors('student-form');
    const errs = [];

    const checks = [
        { res: validateName(d.firstName,  'First Name'), id: 'firstName' },
        { res: validateName(d.lastName,   'Last Name'),  id: 'lastName'  },
        { res: validateEmail(d.email),                   id: 'email'     },
        { res: validatePhone(d.phone),                   id: 'phone'     },
    ];

    checks.forEach(({ res, id }) => {
        if (!res.valid) { showFieldError(id, res.message); errs.push(id); }
    });
    return errs.length === 0;
}
