/**
 * fees.js — Create fee records, record payments, view fees
 *
 * Endpoints:
 *   POST /fees                                  → Create fee record  (FeeCreateRequest)
 *   POST /fees/pay                              → Record payment     (FeePaymentRequest)
 *   GET  /fees/student/{studentId}              → All fees by student
 *   GET  /fees/student/{studentId}/pending      → Pending fees only
 *
 * FeeCreateRequest:  studentId*, feeType*, amount* (BigDecimal > 0), dueDate (LocalDate), academicYear, semester
 * FeePaymentRequest: feeId* (Long), paymentAmount* (BigDecimal > 0)
 * FeeResponse:       id, studentId, studentName, feeType, amount, paidAmount, outstandingAmount, status, dueDate, paidDate, academicYear, semester
 */

// ─────────────────────────────────────────────────────────────────────────────
// CREATE FEE RECORD   POST /fees
// ─────────────────────────────────────────────────────────────────────────────
async function createFeeRecord() {
    const payload = {
        studentId:    val('fee-studentId'),
        feeType:      val('fee-type'),
        amount:       parseFloat(val('fee-amount')) || null,
        dueDate:      val('fee-dueDate')      || null,
        academicYear: val('fee-academicYear') || null,
        semester:     val('fee-semester')     || null
    };

    clearAllErrors('fee-form');
    let hasError = false;
    if (!payload.studentId)             { showFieldError('fee-studentId', 'Student ID is required.'); hasError = true; }
    if (!payload.feeType)               { showFieldError('fee-type',      'Fee type is required.');   hasError = true; }
    const amtCheck = validateAmount(payload.amount, 'Amount');
    if (!amtCheck.valid)                { showFieldError('fee-amount', amtCheck.message);             hasError = true; }
    if (hasError) return;

    showLoader('Creating fee record...');
    try {
        await apiPost('/fees', payload);
        showToast('Fee record created!', 'success');
        document.getElementById('fee-form').reset();
        setVal('fee-filter-studentId', payload.studentId);
        loadFeesByStudent();
    } catch (err) {
        handleApiError(err, 'Could not create fee record.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RECORD PAYMENT   POST /fees/pay
// body: { feeId: Long, paymentAmount: BigDecimal }
// ─────────────────────────────────────────────────────────────────────────────
async function payFee() {
    const feeIdRaw = val('pay-feeId');
    const amtRaw   = val('pay-amount');

    clearAllErrors('pay-form');
    let hasError = false;
    if (!feeIdRaw || isNaN(parseInt(feeIdRaw)))  { showFieldError('pay-feeId',   'Fee ID is required.');           hasError = true; }
    const amtCheck = validateAmount(parseFloat(amtRaw), 'Payment Amount');
    if (!amtCheck.valid)                         { showFieldError('pay-amount',   amtCheck.message);               hasError = true; }
    if (hasError) return;

    const payload = {
        feeId:         parseInt(feeIdRaw),        // Long
        paymentAmount: parseFloat(amtRaw)          // BigDecimal → number is fine as JSON
    };

    showLoader('Processing payment...');
    try {
        await apiPost('/fees/pay', payload);
        showToast('Payment recorded successfully!', 'success');
        document.getElementById('pay-form').reset();
        const sid = val('fee-filter-studentId');
        if (sid) loadFeesByStudent();
    } catch (err) {
        handleApiError(err, 'Payment failed.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ALL FEES BY STUDENT   GET /fees/student/{studentId}
// ─────────────────────────────────────────────────────────────────────────────
async function loadFeesByStudent() {
    const studentId = val('fee-filter-studentId');
    if (!studentId) { showToast('Enter a Student ID first.', 'warning'); return; }

    showLoader('Loading fees...');
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
    const studentId = val('fee-filter-studentId');
    if (!studentId) { showToast('Enter a Student ID first.', 'warning'); return; }

    showLoader('Loading pending fees...');
    try {
        const res = await apiGet('/fees/student/' + encodeURIComponent(studentId) + '/pending');
        renderFeeTable(res.data || []);
        const count = (res.data || []).length;
        showToast(count + ' pending fee record(s) found.', count > 0 ? 'warning' : 'info');
    } catch (err) {
        handleApiError(err, 'Could not load pending fees.');
    } finally {
        hideLoader();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RENDER FEE TABLE + SUMMARY
// ─────────────────────────────────────────────────────────────────────────────
function renderFeeTable(fees) {
    const tbody  = document.getElementById('fee-tbody');
    const sumEl  = document.getElementById('fee-summary');
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
          <td>${escHtml(f.feeType)}</td>
          <td>${formatCurrency(f.amount)}</td>
          <td>${formatCurrency(f.paidAmount)}</td>
          <td>${formatCurrency(f.outstandingAmount)}</td>
          <td>${statusBadge(f.status)}</td>
          <td>${formatDate(f.dueDate)}</td>
          <td>${formatDate(f.paidDate)}</td>
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
