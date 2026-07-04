/**
 * attendance.js — Mark attendance, view records, calculate percentage
 *
 * Endpoints:
 *   POST /attendance                                   → Mark attendance  (AttendanceMarkRequest)
 *   GET  /attendance/student/{studentId}               → By student
 *   GET  /attendance/course/{courseCode}               → By course
 *   GET  /attendance/percentage?studentId=&courseCode= → Percentage
 *
 * AttendanceMarkRequest:      studentId*, courseCode*, attendanceDate* (LocalDate), status* (PRESENT|ABSENT|LATE), remarks
 * AttendanceResponse:         id, studentId, studentName, courseCode, courseName, attendanceDate, status, remarks
 * AttendancePercentageResponse: studentId, courseCode, percentage, totalSessions, presentCount
 *   NOTE: field is totalSessions (NOT totalClasses)
 */

// ─────────────────────────────────────────────────────────────────────────────
// MARK ATTENDANCE   POST /attendance
// ─────────────────────────────────────────────────────────────────────────────
async function markAttendance() {
    const payload = {
        studentId:      val('att-studentId'),
        courseCode:     val('att-courseCode'),
        attendanceDate: val('att-date') || null,    // "YYYY-MM-DD"  → LocalDate
        status:         val('att-status'),
        remarks:        val('att-remarks') || null
    };

    // Inline validation
    clearAllErrors('attendance-form');
    let hasError = false;
    if (!payload.studentId)      { showFieldError('att-studentId',  'Student ID is required.');  hasError = true; }
    if (!payload.courseCode)     { showFieldError('att-courseCode', 'Course Code is required.'); hasError = true; }
    if (!payload.attendanceDate) { showFieldError('att-date',       'Date is required.');         hasError = true; }
    if (!payload.status)         { showFieldError('att-status',     'Status is required.');       hasError = true; }
    if (hasError) return;

    showLoader('Marking attendance...');
    try {
        await apiPost('/attendance', payload);
        showToast('Attendance marked successfully!', 'success');
        document.getElementById('attendance-form').reset();
        // Reset date to today
        document.getElementById('att-date').value = new Date().toISOString().split('T')[0];

        // Auto-refresh the table if a filter is active
        if (val('att-filter-studentId'))  loadAttendanceByStudent();
        else if (val('att-filter-courseCode')) loadAttendanceByCourse();
    } catch (err) {
        handleApiError(err, 'Could not mark attendance.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VIEW BY STUDENT   GET /attendance/student/{studentId}
// ─────────────────────────────────────────────────────────────────────────────
async function loadAttendanceByStudent() {
    const studentId = val('att-filter-studentId');
    if (!studentId) { showToast('Enter a Student ID first.', 'warning'); return; }

    showLoader('Loading attendance...');
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
// VIEW BY COURSE   GET /attendance/course/{courseCode}
// ─────────────────────────────────────────────────────────────────────────────
async function loadAttendanceByCourse() {
    const courseCode = val('att-filter-courseCode');
    if (!courseCode) { showToast('Enter a Course Code first.', 'warning'); return; }

    showLoader('Loading attendance...');
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
// ATTENDANCE PERCENTAGE   GET /attendance/percentage?studentId=&courseCode=
// ─────────────────────────────────────────────────────────────────────────────
async function loadAttendancePercentage() {
    const studentId  = val('pct-studentId');
    const courseCode = val('pct-courseCode');
    if (!studentId || !courseCode) {
        showToast('Enter both Student ID and Course Code.', 'warning');
        return;
    }

    showLoader('Calculating percentage...');
    try {
        const res = await apiGet(
            '/attendance/percentage?studentId=' + encodeURIComponent(studentId) +
            '&courseCode=' + encodeURIComponent(courseCode)
        );
        const p   = res.data;   // AttendancePercentageResponse
        const box = document.getElementById('percentage-result');
        if (!box) return;

        // Fields: studentId, courseCode, percentage, totalSessions, presentCount
        const pct        = typeof p.percentage === 'number' ? p.percentage : 0;
        const isGood     = pct >= 75;
        const absent     = (p.totalSessions || 0) - (p.presentCount || 0);

        box.style.display = 'block';
        box.innerHTML = `
            <div class="pct-card">
                <div class="pct-number ${isGood ? 'pct-good' : 'pct-low'}">${pct.toFixed(1)}%</div>
                <div class="pct-detail">
                    ✅ Present: <strong>${p.presentCount || 0}</strong> &nbsp;|&nbsp;
                    ❌ Absent:  <strong>${absent}</strong> &nbsp;|&nbsp;
                    📅 Total:   <strong>${p.totalSessions || 0}</strong>
                </div>
                <div class="pct-student">${escHtml(p.studentId)} — ${escHtml(p.courseCode)}</div>
                ${!isGood ? '<div class="pct-warn">⚠ Below 75% attendance threshold</div>' : '<div style="color:var(--success);margin-top:.5rem;font-weight:600">✓ Attendance requirement met</div>'}
            </div>`;
    } catch (err) {
        handleApiError(err, 'Could not calculate percentage.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RENDER ATTENDANCE TABLE
// ─────────────────────────────────────────────────────────────────────────────
function renderAttendanceTable(records) {
    const tbody = document.getElementById('attendance-tbody');
    if (!tbody) return;

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
