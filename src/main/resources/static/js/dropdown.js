/**
 * dropdown.js — Populate all <select> dropdowns from the live database
 *
 * data-type="department"  → filled from GET /departments
 * data-type="course"      → filled from GET /courses  (with auto-fill of semester/academicYear)
 * data-type="courseCode"  → filled from GET /courses  (code-only, for attendance/fees filters)
 * data-type="instructor"  → filled from GET /faculty
 * data-type="student"     → filled from GET /students
 *
 * Usage:
 *   await loadDropdowns(['departments', 'courses', 'faculty', 'students']);
 */

const _dropdownCache = {};

// ── Master loader ─────────────────────────────────────────────────────────────
async function loadDropdowns(types) {
    const tasks = [];
    if (types.includes('departments')) tasks.push(_fetchAndFillDepts());
    if (types.includes('courses'))     tasks.push(_fetchAndFillCourses());
    if (types.includes('faculty'))     tasks.push(_fetchAndFillFaculty());
    if (types.includes('students'))    tasks.push(_fetchAndFillStudents());
    await Promise.allSettled(tasks);
}

// ── Refresh (clear cache then reload) ────────────────────────────────────────
async function refreshDropdowns(types) {
    types.forEach(t => delete _dropdownCache[t]);
    await loadDropdowns(types);
}

// ── Cache accessors ───────────────────────────────────────────────────────────
function getCachedStudents() { return _dropdownCache.students || []; }
function getCachedCourses()  { return _dropdownCache.courses  || []; }

// ═════════════════════════════════════════════════════════════════════════════
// DEPARTMENTS  GET /departments
// ═════════════════════════════════════════════════════════════════════════════
async function _fetchAndFillDepts() {
    try {
        if (!_dropdownCache.departments) {
            const res = await apiGet('/departments');
            _dropdownCache.departments = res.data || [];
        }
        _fillByType('department', _dropdownCache.departments,
            d => ({ value: d.code, label: `${d.name} (${d.code})` })
        );
    } catch (err) {
        console.warn('Departments dropdown failed:', err.message);
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// COURSES  GET /courses
// Fills both data-type="course" and data-type="courseCode"
// ═════════════════════════════════════════════════════════════════════════════
async function _fetchAndFillCourses() {
    try {
        if (!_dropdownCache.courses) {
            const res = await apiGet('/courses');
            _dropdownCache.courses = res.data || [];
        }
        const courses = _dropdownCache.courses;

        // data-type="course" — full select with auto-fill on change
        document.querySelectorAll('select[data-type="course"]').forEach(sel => {
            const prev = sel.value;
            sel.innerHTML = '<option value="">— Select Course —</option>' +
                courses.map(c =>
                    `<option value="${escHtml(c.courseCode)}"
                             data-semester="${escHtml(c.semester || '')}"
                             data-academic-year="${escHtml(c.academicYear || '')}"
                             data-dept="${escHtml(c.departmentCode || '')}">
                        ${escHtml(c.courseCode)} — ${escHtml(c.courseName)}
                     </option>`
                ).join('');
            if (prev) sel.value = prev;

            // Re-attach change listener once (prevent duplicates with a flag)
            if (!sel._courseListenerAdded) {
                sel._courseListenerAdded = true;
                sel.addEventListener('change', function () {
                    const opt = this.options[this.selectedIndex];
                    _setIfExists('semester',       opt.dataset.semester);
                    _setIfExists('academicYear',   opt.dataset.academicYear);
                    _setIfExists('departmentCode', opt.dataset.dept);
                });
            }
        });

        // data-type="courseCode" — plain code only
        document.querySelectorAll('select[data-type="courseCode"]').forEach(sel => {
            const prev = sel.value;
            // Preserve any "All" placeholder the HTML already has
            const firstOpt = sel.options[0] && !sel.options[0].value ? sel.options[0].outerHTML : '';
            sel.innerHTML = (firstOpt || '<option value="">— Select Course —</option>') +
                courses.map(c =>
                    `<option value="${escHtml(c.courseCode)}">${escHtml(c.courseCode)} — ${escHtml(c.courseName)}</option>`
                ).join('');
            if (prev) sel.value = prev;
        });

    } catch (err) {
        console.warn('Courses dropdown failed:', err.message);
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FACULTY / INSTRUCTORS  GET /faculty
// ═════════════════════════════════════════════════════════════════════════════
async function _fetchAndFillFaculty() {
    try {
        if (!_dropdownCache.faculty) {
            const res = await apiGet('/faculty');
            _dropdownCache.faculty = res.data || [];
        }
        _fillByType('instructor', _dropdownCache.faculty,
            f => ({ value: f.employeeId, label: `${f.firstName} ${f.lastName} (${f.employeeId})` }),
            '— Select Instructor (optional) —'
        );
    } catch (err) {
        console.warn('Faculty dropdown failed:', err.message);
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// STUDENTS  GET /students
// ═════════════════════════════════════════════════════════════════════════════
async function _fetchAndFillStudents() {
    try {
        if (!_dropdownCache.students) {
            const res = await apiGet('/students');
            _dropdownCache.students = res.data || [];
        }
        _fillByType('student', _dropdownCache.students,
            s => ({ value: s.studentId, label: `${s.studentId} — ${s.firstName} ${s.lastName}` }),
            '— Select Student —'
        );
    } catch (err) {
        console.warn('Students dropdown failed:', err.message);
    }
}

// ── Generic fill helper ───────────────────────────────────────────────────────
function _fillByType(dataType, items, mapFn, placeholder = '— Select —') {
    document.querySelectorAll(`select[data-type="${dataType}"]`).forEach(sel => {
        const prev = sel.value;
        sel.innerHTML = `<option value="">${placeholder}</option>` +
            items.map(item => {
                const { value, label } = mapFn(item);
                return `<option value="${escHtml(value)}">${escHtml(label)}</option>`;
            }).join('');
        if (prev) sel.value = prev;
    });
}

// ── Safe setter ───────────────────────────────────────────────────────────────
function _setIfExists(id, value) {
    const el = document.getElementById(id);
    if (el && value) el.value = value;
}
