/**
 * dropdown.js — Populate all <select> dropdowns from the live database
 *
 * Called on page load wherever forms exist.
 * Three data sources:
 *   GET /departments  → department dropdowns
 *   GET /courses      → course dropdowns (for student registration)
 *   GET /faculty      → instructor dropdowns (for course form)
 *
 * Usage:
 *   await loadDropdowns(['departments', 'courses', 'faculty']);
 */

// ── Cache to avoid repeat fetches within same page load ─────────────────────
const _dropdownCache = {};

// ── Master loader ────────────────────────────────────────────────────────────
async function loadDropdowns(types) {
    const tasks = [];
    if (types.includes('departments')) tasks.push(_loadDepartments());
    if (types.includes('courses'))     tasks.push(_loadCourses());
    if (types.includes('faculty'))     tasks.push(_loadFaculty());
    if (types.includes('students'))    tasks.push(_loadStudents());
    await Promise.allSettled(tasks);   // never block the page even if one fails
}

// ── DEPARTMENTS — GET /departments ───────────────────────────────────────────
async function _loadDepartments() {
    try {
        if (!_dropdownCache.departments) {
            const res = await apiGet('/departments');
            _dropdownCache.departments = res.data || [];
        }
        _fillDeptSelects(_dropdownCache.departments);
    } catch (err) {
        console.warn('Could not load departments for dropdown:', err.message);
    }
}

function _fillDeptSelects(departments) {
    // Find every <select> with data-type="department" on the page
    document.querySelectorAll('select[data-type="department"]').forEach(sel => {
        const currentVal = sel.value;
        const placeholder = '<option value="">— Select Department —</option>';
        const options = departments.map(d =>
            `<option value="${escHtml(d.code)}">${escHtml(d.name)} (${escHtml(d.code)})</option>`
        ).join('');
        sel.innerHTML = placeholder + options;
        if (currentVal) sel.value = currentVal;   // restore selection if re-populating
    });
}

// ── COURSES — GET /courses ───────────────────────────────────────────────────
async function _loadCourses() {
    try {
        if (!_dropdownCache.courses) {
            const res = await apiGet('/courses');
            _dropdownCache.courses = res.data || [];
        }
        _fillCourseSelects(_dropdownCache.courses);
    } catch (err) {
        console.warn('Could not load courses for dropdown:', err.message);
    }
}

function _fillCourseSelects(courses) {
    document.querySelectorAll('select[data-type="course"]').forEach(sel => {
        const currentVal = sel.value;
        const placeholder = '<option value="">— Select Course —</option>';
        const options = courses.map(c =>
            `<option value="${escHtml(c.courseCode)}"
                     data-semester="${escHtml(c.semester || '')}"
                     data-academic-year="${escHtml(c.academicYear || '')}"
                     data-dept="${escHtml(c.departmentCode || '')}">
                ${escHtml(c.courseCode)} — ${escHtml(c.courseName)}
            </option>`
        ).join('');
        sel.innerHTML = placeholder + options;
        if (currentVal) sel.value = currentVal;

        // Auto-fill semester & academicYear when a course is selected
        sel.addEventListener('change', function () {
            const opt = this.options[this.selectedIndex];
            const semEl  = document.getElementById('semester');
            const yearEl = document.getElementById('academicYear');
            const deptEl = document.getElementById('departmentCode');
            if (semEl  && opt.dataset.semester)      semEl.value  = opt.dataset.semester;
            if (yearEl && opt.dataset.academicYear)   yearEl.value = opt.dataset.academicYear;
            if (deptEl && opt.dataset.dept)           deptEl.value = opt.dataset.dept;
        });
    });
}

// ── FACULTY (Instructors) — GET /faculty ─────────────────────────────────────
async function _loadFaculty() {
    try {
        if (!_dropdownCache.faculty) {
            const res = await apiGet('/faculty');
            _dropdownCache.faculty = res.data || [];
        }
        _fillFacultySelects(_dropdownCache.faculty);
    } catch (err) {
        console.warn('Could not load faculty for dropdown:', err.message);
    }
}

function _fillFacultySelects(faculty) {
    document.querySelectorAll('select[data-type="instructor"]').forEach(sel => {
        const currentVal = sel.value;
        const placeholder = '<option value="">— Select Instructor (optional) —</option>';
        const options = faculty.map(f =>
            `<option value="${escHtml(f.employeeId)}">
                ${escHtml(f.firstName)} ${escHtml(f.lastName)} (${escHtml(f.employeeId)})
            </option>`
        ).join('');
        sel.innerHTML = placeholder + options;
        if (currentVal) sel.value = currentVal;
    });
}

// ── Re-populate after modal opens (call this whenever you open a modal) ──────
async function refreshDropdowns(types) {
    // Clear cache so fresh data is fetched
    types.forEach(t => delete _dropdownCache[t]);
    await loadDropdowns(types);
}

// ── STUDENTS — GET /students ─────────────────────────────────────────────────
async function _loadStudents() {
    try {
        if (!_dropdownCache.students) {
            const res = await apiGet('/students');
            _dropdownCache.students = res.data || [];
        }
        _fillStudentSelects(_dropdownCache.students);
    } catch (err) {
        console.warn('Could not load students for dropdown:', err.message);
    }
}

function _fillStudentSelects(students) {
    document.querySelectorAll('select[data-type="student"]').forEach(sel => {
        const currentVal = sel.value;
        const placeholder = '<option value="">— Select Student —</option>';
        const options = students.map(s =>
            `<option value="${escHtml(s.studentId)}">
                ${escHtml(s.studentId)} — ${escHtml(s.firstName)} ${escHtml(s.lastName)}
            </option>`
        ).join('');
        sel.innerHTML = placeholder + options;
        if (currentVal) sel.value = currentVal;
    });
    return students;   // return so callers can use the list
}

// ── Get cached students list (for auto-load logic) ───────────────────────────
function getCachedStudents() {
    return _dropdownCache.students || [];
}

// ── Get cached courses list ──────────────────────────────────────────────────
function getCachedCourses() {
    return _dropdownCache.courses || [];
}

// ── Fill plain course-code-only selects (data-type="courseCode") ─────────────
// Used in attendance/fees filters where only the code is needed
function _fillCourseCodeSelects(courses) {
    document.querySelectorAll('select[data-type="courseCode"]').forEach(sel => {
        const currentVal = sel.value;
        const placeholder = '<option value="">— Select Course —</option>';
        const options = courses.map(c =>
            `<option value="${escHtml(c.courseCode)}">${escHtml(c.courseCode)} — ${escHtml(c.courseName)}</option>`
        ).join('');
        sel.innerHTML = placeholder + options;
        if (currentVal) sel.value = currentVal;
    });
}

// Override _loadCourses to also fill courseCode selects
const _origLoadCourses = _loadCourses;
async function _loadCourses() {
    try {
        if (!_dropdownCache.courses) {
            const res = await apiGet('/courses');
            _dropdownCache.courses = res.data || [];
        }
        _fillCourseSelects(_dropdownCache.courses);
        _fillCourseCodeSelects(_dropdownCache.courses);
    } catch (err) {
        console.warn('Could not load courses for dropdown:', err.message);
    }
}
