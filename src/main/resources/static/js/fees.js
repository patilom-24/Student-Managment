/**
 * fees.js
 *
 * Endpoints:
 *   POST /fees                               → Create fee record  (FeeCreateRequest)
 *   POST /fees/pay                           → Record payment     (FeePaymentRequest)
 *   GET  /fees/student/{studentId}           → All fees by student
 *   GET  /fees/student/{studentId}/pending   → Pending fees only
 *
 * FeeCreateRequest:  studentId*, feeType*, amount*, dueDate, academicYear, semester
 * FeePaymentRequest: feeId*, paymentAmount*
 * FeeResponse:       id, studentId, studentName, feeType, amount, paidAmount,
 *                    outstandingAmount, status, dueDate, paidDate, academicYear, semester
 */

// ─────────────────────────────────────────────────────────────────────────────
// AUTO-LOAD ALL  — combines fees for every student on page open
// ─────────────────────────────────────────────────────────────────────────────
async function loadAllFees() {
    showLoader('Loading all fee records…');
    const labelEl = document.getElementById('fee-record-label');
    if (labelEl) labelEl.textContent = 'All students';

    try {
        const students = getCachedStudents();   // from dropdown.js
        if (!students.length) {
            renderFeeTable([]);
            return;
        }

        const results = await Promise.allSettled(
            students.map(s => apiGet('/fees/student/' + encodeURIComponent(s.studentId)))
        );

        const all = [];
        results.forEach(r => {
            if (r.status === 'fulfilled') all.push(...(r.value.data || []));
        });

        renderFeeTable(all);
    } catch (err) {
        handleApiError(err, 'Could not load fees.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CREATE FEE RECORD   POST /fees
// ─────────────────────────────────────────────────────────────────────────────
async function createFeeRecord() {
    const payload = {
        studentId:    val('fee-studentId'),           // from <select data-type="student">
        feeType:      val('fee-type'),
        amount:       parseFloat(val('fee-amount')) || null,
        dueDate:      val('fee-dueDate')      || null,
        academicYear: val('fee-academicYear') || null,
        semester:     val('fee-semester-text') || null
    };

    clearAllErrors('fee-form');
    let hasError = false;
    if (!payload.studentId) { showFieldError('fee-studentId', 'Please select a student.'); hasError = true; }
    if (!payload.feeType)   { showFieldError('fee-type',      'Fee type is required.');    hasError = true; }
    const amtCheck = validateAmount(payload.amount, 'Amount');
    if (!amtCheck.valid)    { showFieldError('fee-amount', amtCheck.message);              hasError = true; }
    if (hasError) return;

    showLoader('Creating fee record…');
    try {
        await apiPost('/fees', payload);
        showToast('Fee record created!', 'success');
        document.getElementById('fee-form').reset();

        // Auto-show the newly created student's fees
        document.getElementById('fee-filter-studentId').value  = payload.studentId;
        document.getElementById('fee-filter-sid-text').value   = payload.studentId;
        const labelEl = document.getElementById('fee-record-label');
        if (labelEl) labelEl.textContent = 'Student: ' + payload.studentId;
        loadFeesByStudent();
    } catch (err) {
        handleApiError(err, 'Could not create fee record.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RECORD PAYMENT   POST /fees/pay
// ─────────────────────────────────────────────────────────────────────────────
async function payFee() {
    const feeIdRaw = val('pay-feeId');
    const amtRaw   = val('pay-amount');

    clearAllErrors('pay-form');
    let hasError = false;
    if (!feeIdRaw || isNaN(parseInt(feeIdRaw))) { showFieldError('pay-feeId',  'Fee ID is required.'); hasError = true; }
    const amtCheck = validateAmount(parseFloat(amtRaw), 'Payment Amount');
    if (!amtCheck.valid) { showFieldError('pay-amount', amtCheck.message); hasError = true; }
    if (hasError) return;

    const payload = {
        feeId:         parseInt(feeIdRaw),
        paymentAmount: parseFloat(amtRaw)
    };

    showLoader('Processing payment…');
    try {
        await apiPost('/fees/pay', payload);
        showToast('Payment recorded successfully!', 'success');
        document.getElementById('pay-form').reset();
        // Refresh current view
        const sid = val('fee-filter-sid-text') || document.getElementById('fee-filter-studentId').value;
        if (sid) {
            document.getElementById('fee-filter-sid-text').value = sid;
            loadFeesByStudent();
        } else {
            loadAllFees();
        }
    } catch (err) {
        handleApiError(err, 'Payment failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ALL FEES BY STUDENT   GET /fees/student/{studentId}
// reads from the hidden input #fee-filter-sid-text (set by the select handler)
// ─────────────────────────────────────────────────────────────────────────────
async function loadFeesByStudent() {
    const studentId = val('fee-filter-sid-text') || val('fee-filter-studentId');
    if (!studentId) { showToast('Select a student first.', 'warning'); return; }

    const labelEl = document.getElementById('fee-record-label');
    if (labelEl) labelEl.textContent = 'Student: ' + studentId;

    showLoader('Loading fees…');
    try {
        const res = await apiGet('/fees/student/' + encodeURIComponent(studentId));
        renderFeeTable(res.data || []);
    } catch (err) {
        handleApiError(err, 'Could not load fees.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PENDING FEES   GET /fees/student/{studentId}/pending
// ─────────────────────────────────────────────────────────────────────────────
async function loadPendingFees() {
    const studentId = val('fee-filter-sid-text') || val('fee-filter-studentId');
    if (!studentId) { showToast('Select a student first.', 'warning'); return; }

    const labelEl = document.getElementById('fee-record-label');
    if (labelEl) labelEl.textContent = 'Pending — ' + studentId;

    showLoader('Loading pending fees…');
    try {
        const res = await apiGet('/fees/student/' + encodeURIComponent(studentId) + '/pending');
        renderFeeTable(res.data || []);
        const count = (res.data || []).length;
        showToast(count + ' pending fee record(s).', count > 0 ? 'warning' : 'info');
    } catch (err) {
        handleApiError(err, 'Could not load pending fees.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RENDER TABLE + SUMMARY
// ─────────────────────────────────────────────────────────────────────────────
function renderFeeTable(fees) {
    const tbody = document.getElementById('fee-tbody');
    const sumEl = document.getElementById('fee-summary');
    if (!tbody) return;

    if (!fees.length) {
        tbody.innerHTML = '<tr><td colspan="9" class="empty-state">No fee records found.</td></tr>';
        if (sumEl) sumEl.style.display = 'none';
        return;
    }

    let totalAmt = 0, totalPaid = 0, totalOutstanding = 0;

    tbody.innerHTML = fees.map(f => {
        totalAmt         += parseFloat(f.amount)            || 0;
        totalPaid        += parseFloat(f.paidAmount)        || 0;
        totalOutstanding += parseFloat(f.outstandingAmount) || 0;
        return `
        <tr>
            <td><strong>${f.id}</strong></td>
            <td>${escHtml(f.studentId || '—')}</td>
            <td>${escHtml(f.feeType)}</td>
            <td>${formatCurrency(f.amount)}</td>
            <td>${formatCurrency(f.paidAmount)}</td>
            <td>${formatCurrency(f.outstandingAmount)}</td>
            <td>${statusBadge(f.status)}</td>
            <td>${formatDate(f.dueDate)}</td>
            <td>${escHtml(f.semester || '—')}</td>
        </tr>`;
    }).join('');

    if (sumEl) {
        sumEl.style.display = 'block';
        sumEl.innerHTML = `
            <div class="fee-summary-grid">
                <div class="summary-card">
                    <span>Total Fees</span>
                    <strong>${formatCurrency(totalAmt)}</strong>
                </div>
                <div class="summary-card summary-paid">
                    <span>Paid</span>
                    <strong>${formatCurrency(totalPaid)}</strong>
                </div>
                <div class="summary-card summary-due">
                    <span>Outstanding</span>
                    <strong>${formatCurrency(totalOutstanding)}</strong>
                </div>
            </div>`;
    }
}
