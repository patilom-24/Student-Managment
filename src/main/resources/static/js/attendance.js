/**
 * attendance.js
 *
 * Endpoints:
 *   POST /attendance                                      → Mark
 *   GET  /attendance/student/{studentId}                  → By student
 *   GET  /attendance/course/{courseCode}                  → By course
 *   GET  /attendance/percentage?studentId=&courseCode=    → Percentage
 *
 * AttendanceMarkRequest:          studentId*, courseCode*, attendanceDate*, status*, remarks
 * AttendanceResponse:             id, studentId, studentName, courseCode, courseName,
 *                                 attendanceDate, status, remarks
 * AttendancePercentageResponse:   studentId, courseCode, percentage,
 *                                 totalSessions, presentCount
 */

// ─────────────────────────────────────────────────────────────────────────────
// AUTO-LOAD ALL  — loads every student's attendance in parallel on page open
// ─────────────────────────────────────────────────────────────────────────────
async function loadAllAttendance() {
    showLoader('Loading all attendance records…');
    try {
        const students = getCachedStudents();    // from dropdown.js cache
        if (!students.length) {
            renderAttendanceTable([]);
            return;
        }

        // Parallel fetch for every student
        const results = await Promise.allSettled(
            students.map(s => apiGet('/attendance/student/' + encodeURIComponent(s.studentId)))
        );

        const all = [];
        results.forEach(r => {
            if (r.status === 'fulfilled') all.push(...(r.value.data || []));
        });

        // Sort by date descending
        all.sort((a, b) => new Date(b.attendanceDate) - new Date(a.attendanceDate));
        renderAttendanceTable(all);
    } catch (err) {
        handleApiError(err, 'Could not load attendance.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MARK ATTENDANCE   POST /attendance
// ─────────────────────────────────────────────────────────────────────────────
async function markAttendance() {
    const payload = {
        studentId:      val('att-studentId'),     // from <select data-type="student">
        courseCode:     val('att-courseCode'),     // from <select data-type="courseCode">
        attendanceDate: val('att-date') || null,
        status:         val('att-status'),
        remarks:        val('att-remarks') || null
    };

    clearAllErrors('attendance-form');
    let hasError = false;
    if (!payload.studentId)      { showFieldError('att-studentId',  'Please select a student.');  hasError = true; }
    if (!payload.courseCode)     { showFieldError('att-courseCode', 'Please select a course.');   hasError = true; }
    if (!payload.attendanceDate) { showFieldError('att-date',       'Date is required.');         hasError = true; }
    if (!payload.status)         { showFieldError('att-status',     'Please select a status.');   hasError = true; }
    if (hasError) return;

    showLoader('Marking attendance…');
    try {
        await apiPost('/attendance', payload);
        showToast('Attendance marked successfully!', 'success');
        document.getElementById('attendance-form').reset();
        document.getElementById('att-date').value = new Date().toISOString().split('T')[0];
        // Refresh the filter that is currently active
        _refreshActiveFilter();
    } catch (err) {
        handleApiError(err, 'Could not mark attendance.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FILTER BY STUDENT (called from filter toolbar select)
// GET /attendance/student/{studentId}
// ─────────────────────────────────────────────────────────────────────────────
async function loadAttendanceByStudentSelect(studentId) {
    showLoader('Loading attendance…');
    try {
        const res = await apiGet('/attendance/student/' + encodeURIComponent(studentId));
        renderAttendanceTable(res.data || []);
    } catch (err) {
        handleApiError(err, 'Could not load attendance.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FILTER BY COURSE (called from filter toolbar select)
// GET /attendance/course/{courseCode}
// ─────────────────────────────────────────────────────────────────────────────
async function loadAttendanceByCourseSelect(courseCode) {
    showLoader('Loading attendance…');
    try {
        const res = await apiGet('/attendance/course/' + encodeURIComponent(courseCode));
        renderAttendanceTable(res.data || []);
    } catch (err) {
        handleApiError(err, 'Could not load attendance.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ATTENDANCE PERCENTAGE
// GET /attendance/percentage?studentId=&courseCode=
// ─────────────────────────────────────────────────────────────────────────────
async function loadAttendancePercentage() {
    const studentId  = val('pct-studentId');    // from <select data-type="student">
    const courseCode = val('pct-courseCode');   // from <select data-type="courseCode">

    if (!studentId || !courseCode) {
        showToast('Select both a student and a course first.', 'warning');
        return;
    }

    showLoader('Calculating…');
    try {
        const res = await apiGet(
            '/attendance/percentage?studentId=' + encodeURIComponent(studentId) +
            '&courseCode=' + encodeURIComponent(courseCode)
        );
        const p       = res.data;
        const pct     = typeof p.percentage === 'number' ? p.percentage : 0;
        const isGood  = pct >= 75;
        const absent  = (p.totalSessions || 0) - (p.presentCount || 0);
        const box     = document.getElementById('percentage-result');
        if (!box) return;

        box.style.display = 'block';
        box.innerHTML = `
            <div class="pct-card">
                <div class="pct-number ${isGood ? 'pct-good' : 'pct-low'}">${pct.toFixed(1)}%</div>
                <div class="pct-detail">
                    ✅ Present: <strong>${p.presentCount || 0}</strong> &nbsp;|&nbsp;
                    ❌ Absent:  <strong>${absent}</strong> &nbsp;|&nbsp;
                    📅 Total:   <strong>${p.totalSessions || 0}</strong>
                </div>
                <div class="pct-student">${escHtml(studentId)} — ${escHtml(courseCode)}</div>
                ${!isGood
                    ? '<div class="pct-warn">⚠ Below 75% attendance threshold</div>'
                    : '<div style="color:var(--success);margin-top:.5rem;font-weight:600">✓ Attendance requirement met</div>'
                }
            </div>`;
    } catch (err) {
        handleApiError(err, 'Could not calculate percentage.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RENDER TABLE
// ─────────────────────────────────────────────────────────────────────────────
function renderAttendanceTable(records) {
    const tbody    = document.getElementById('attendance-tbody');
    const countEl  = document.getElementById('att-record-count');
    if (!tbody) return;

    if (countEl) countEl.textContent = records.length ? records.length + ' record(s) shown.' : '';

    if (!records.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No attendance records found.</td></tr>';
        return;
    }

    tbody.innerHTML = records.map(r => `
        <tr>
            <td>${escHtml(r.studentId)}</td>
            <td>${escHtml(r.studentName || '—')}</td>
            <td>${escHtml(r.courseCode)}</td>
            <td>${escHtml(r.courseName || '—')}</td>
            <td>${formatDate(r.attendanceDate)}</td>
            <td>${statusBadge(r.status)}</td>
            <td>${escHtml(r.remarks || '—')}</td>
        </tr>`).join('');
}

// ─────────────────────────────────────────────────────────────────────────────
// INTERNAL: re-load whichever filter is currently selected
// ─────────────────────────────────────────────────────────────────────────────
function _refreshActiveFilter() {
    const sid  = document.getElementById('att-filter-studentId')  ? document.getElementById('att-filter-studentId').value  : '';
    const code = document.getElementById('att-filter-courseCode')  ? document.getElementById('att-filter-courseCode').value : '';
    if (sid)        loadAttendanceByStudentSelect(sid);
    else if (code)  loadAttendanceByCourseSelect(code);
    else            loadAllAttendance();
}
