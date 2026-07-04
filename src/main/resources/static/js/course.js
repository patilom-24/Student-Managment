/**
 * course.js — Full CRUD for Courses
 *
 * Endpoints:
 *   GET    /courses                         → List all
 *   GET    /courses/search?keyword={}       → Search
 *   GET    /courses/{id}                    → Get one
 *   POST   /courses                         → Create  (CourseCreateRequest)
 *   PUT    /courses/{id}                    → Update  (CourseUpdateRequest)
 *   DELETE /courses/{id}                    → Delete
 *
 * CourseCreateRequest: courseCode*, courseName*, description, credits*, semester*, academicYear*, departmentCode*, instructorEmployeeId
 * CourseUpdateRequest: courseCode, courseName, description, credits, semester, academicYear, departmentCode, instructorEmployeeId
 * CourseResponse:      id, courseCode, courseName, description, credits, semester, academicYear, departmentCode, departmentName, instructorEmployeeId, instructorName
 */

let _editingCourseId = null;

// ─────────────────────────────────────────────────────────────────────────────
// LOAD ALL   GET /courses
// ─────────────────────────────────────────────────────────────────────────────
async function loadCourses() {
    showLoader('Loading courses...');
    try {
        const res = await apiGet('/courses');
        renderCourseTable(res.data || []);
    } catch (err) {
        handleApiError(err, 'Could not load courses.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SEARCH   GET /courses/search?keyword={}
// ─────────────────────────────────────────────────────────────────────────────
async function searchCourses() {
    const keyword = val('course-search');
    if (!keyword.trim()) { loadCourses(); return; }

    showLoader('Searching...');
    try {
        const res = await apiGet('/courses/search?keyword=' + encodeURIComponent(keyword));
        renderCourseTable(res.data || []);
    } catch (err) {
        handleApiError(err, 'Search failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RENDER TABLE
// ─────────────────────────────────────────────────────────────────────────────
function renderCourseTable(courses) {
    const tbody   = document.getElementById('course-tbody');
    const countEl = document.getElementById('course-count');
    if (!tbody) return;

    if (countEl) countEl.textContent = courses.length;

    if (!courses.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No courses found.</td></tr>';
        return;
    }

    tbody.innerHTML = courses.map(c => `
        <tr>
          <td>${escHtml(c.courseCode)}</td>
          <td>${escHtml(c.courseName)}</td>
          <td>${escHtml(c.departmentName || c.departmentCode || '—')}</td>
          <td>${c.credits ?? '—'}</td>
          <td>${escHtml(c.semester || '—')}</td>
          <td>${escHtml(c.academicYear || '—')}</td>
          <td>${escHtml(c.instructorName || '—')}</td>
          <td class="action-cell">
            <button class="btn btn-sm btn-primary" onclick="openEditCourse(${c.id})">✏ Edit</button>
            <button class="btn btn-sm btn-danger"  onclick="deleteCourse(${c.id}, '${escHtml(c.courseName)}')">🗑 Delete</button>
          </td>
        </tr>`).join('');
}

// ─────────────────────────────────────────────────────────────────────────────
// OPEN MODAL — Add
// ─────────────────────────────────────────────────────────────────────────────
function openAddCourse() {
    _editingCourseId = null;
    document.getElementById('course-modal-title').textContent = 'Create New Course';
    document.getElementById('course-form').reset();
    clearAllErrors('course-form');
    document.getElementById('courseCode').readOnly = false;
    openModal('course-modal');
    loadDropdowns(['departments', 'faculty']);
}

// ─────────────────────────────────────────────────────────────────────────────
// OPEN MODAL — Edit   GET /courses/{id}
// ─────────────────────────────────────────────────────────────────────────────
async function openEditCourse(id) {
    showLoader('Loading course data...');
    try {
        const res = await apiGet('/courses/' + id);
        const c   = res.data;
        _editingCourseId = id;

        document.getElementById('course-modal-title').textContent = 'Edit Course';
        clearAllErrors('course-form');

        setVal('courseCode',           c.courseCode);
        setVal('courseName',           c.courseName);
        setVal('description',          c.description || '');
        setVal('credits',              c.credits != null ? c.credits : '');
        setVal('semester',             c.semester || '');
        setVal('academicYear',         c.academicYear || '');
        setVal('departmentCode',       c.departmentCode || '');
        setVal('instructorEmployeeId', c.instructorEmployeeId || '');

        document.getElementById('courseCode').readOnly = true;
        openModal('course-modal');
        // Re-populate dropdowns and restore saved selections
        await loadDropdowns(['departments', 'faculty']);
        setVal('departmentCode',       c.departmentCode       || '');
        setVal('instructorEmployeeId', c.instructorEmployeeId || '');
    } catch (err) {
        handleApiError(err, 'Could not load course data.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SAVE — create or update
// ─────────────────────────────────────────────────────────────────────────────
async function saveCourse() {
    const isEdit = _editingCourseId !== null;

    const payload = {
        courseCode:           val('courseCode'),
        courseName:           val('courseName'),
        description:          val('description') || null,
        credits:              parseInt(val('credits')) || null,
        semester:             val('semester'),
        academicYear:         val('academicYear'),
        departmentCode:       val('departmentCode'),
        instructorEmployeeId: val('instructorEmployeeId') || null
    };

    if (!_validateCourseForm(payload)) return;

    showLoader(isEdit ? 'Updating course...' : 'Creating course...');
    try {
        if (isEdit) {
            await apiPut('/courses/' + _editingCourseId, payload);
            showToast('Course updated successfully!', 'success');
        } else {
            await apiPost('/courses', payload);
            showToast('Course created successfully!', 'success');
        }
        closeModal('course-modal');
        loadCourses();
    } catch (err) {
        handleApiError(err, 'Save failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DELETE   DELETE /courses/{id}
// ─────────────────────────────────────────────────────────────────────────────
async function deleteCourse(id, name) {
    const ok = await showConfirm('Delete Course', 'Delete "' + name + '"? This cannot be undone.');
    if (!ok) return;

    showLoader('Deleting...');
    try {
        await apiDelete('/courses/' + id);
        showToast('Course deleted successfully!', 'success');
        loadCourses();
    } catch (err) {
        handleApiError(err, 'Delete failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VALIDATION
// ─────────────────────────────────────────────────────────────────────────────
function _validateCourseForm(d) {
    clearAllErrors('course-form');
    const errs = [];
    const isEdit = _editingCourseId !== null;

    const checks = [
        ...(isEdit ? [] : [{ res: validateRequired(d.courseCode, 'Course Code'), id: 'courseCode' }]),
        { res: validateRequired(d.courseName,    'Course Name'),    id: 'courseName'    },
        { res: validateRequired(d.credits,       'Credits'),        id: 'credits'       },
        { res: validateRequired(d.semester,      'Semester'),       id: 'semester'      },
        { res: validateRequired(d.academicYear,  'Academic Year'),  id: 'academicYear'  },
        { res: validateRequired(d.departmentCode,'Department'),     id: 'departmentCode'},
    ];

    checks.forEach(({ res, id }) => {
        if (!res.valid) { showFieldError(id, res.message); errs.push(id); }
    });
    return errs.length === 0;
}
