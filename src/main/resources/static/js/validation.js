/**
 * validation.js — All form validation logic
 * Pure functions — no DOM side-effects.
 * Returns: { valid: boolean, message: string }
 */

function validateEmail(email) {
    if (!email || !email.trim())
        return { valid: false, message: 'Email is required.' };
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim()))
        return { valid: false, message: 'Enter a valid email address.' };
    return { valid: true, message: '' };
}

function validatePhone(phone) {
    if (!phone || !phone.trim())
        return { valid: false, message: 'Phone number is required.' };
    // Accept any 10-digit number, optionally with +, country code prefix, or spaces/dashes
    const digits = phone.trim().replace(/[\s\-().+]/g, '');
    if (!/^\d{10,15}$/.test(digits))
        return { valid: false, message: 'Enter a valid phone number (10 digits).' };
    return { valid: true, message: '' };
}

function validateName(name, label = 'Name') {
    if (!name || !name.trim())
        return { valid: false, message: `${label} is required.` };
    if (name.trim().length < 2)
        return { valid: false, message: `${label} must be at least 2 characters.` };
    if (!/^[a-zA-Z\s\-']+$/.test(name.trim()))
        return { valid: false, message: `${label} must contain only letters.` };
    return { valid: true, message: '' };
}

function validateRequired(value, label = 'Field') {
    if (value === null || value === undefined || String(value).trim() === '')
        return { valid: false, message: `${label} is required.` };
    return { valid: true, message: '' };
}

function validateDate(dateStr, label = 'Date') {
    if (!dateStr)
        return { valid: false, message: `${label} is required.` };
    const d = new Date(dateStr);
    if (isNaN(d.getTime()))
        return { valid: false, message: `${label} is not a valid date.` };
    return { valid: true, message: '' };
}

function validatePastDate(dateStr, label = 'Date of Birth') {
    const result = validateDate(dateStr, label);
    if (!result.valid) return result;
    if (new Date(dateStr) >= new Date())
        return { valid: false, message: `${label} must be in the past.` };
    return { valid: true, message: '' };
}

function validateAmount(value, label = 'Amount') {
    if (value === null || value === undefined || String(value).trim() === '')
        return { valid: false, message: `${label} is required.` };
    const num = parseFloat(value);
    if (isNaN(num) || num <= 0)
        return { valid: false, message: `${label} must be a positive number.` };
    return { valid: true, message: '' };
}

// ── Show / clear inline field error ────────────────────────────────────────
function showFieldError(fieldId, message) {
    const el = document.getElementById(`${fieldId}-error`);
    if (el) { el.textContent = message; el.style.display = 'block'; }
    const input = document.getElementById(fieldId);
    if (input) input.classList.add('is-invalid');
}

function clearFieldError(fieldId) {
    const el = document.getElementById(`${fieldId}-error`);
    if (el) { el.textContent = ''; el.style.display = 'none'; }
    const input = document.getElementById(fieldId);
    if (input) input.classList.remove('is-invalid');
}

function clearAllErrors(formId) {
    const form = document.getElementById(formId);
    if (!form) return;
    form.querySelectorAll('.field-error').forEach(el => {
        el.textContent = '';
        el.style.display = 'none';
    });
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
}

// ── Validate student registration form ─────────────────────────────────────
function validateStudentForm(data) {
    const errors = [];
    clearAllErrors('student-form');

    const checks = [
        { fn: () => validateRequired(data.studentId, 'Student ID'),   id: 'studentId'   },
        { fn: () => validateName(data.firstName, 'First Name'),        id: 'firstName'   },
        { fn: () => validateName(data.lastName,  'Last Name'),         id: 'lastName'    },
        { fn: () => validateEmail(data.email),                         id: 'email'       },
        { fn: () => validatePhone(data.phone),                         id: 'phone'       },
        { fn: () => validatePastDate(data.dateOfBirth, 'Date of Birth'), id: 'dateOfBirth'},
        { fn: () => validateRequired(data.departmentCode, 'Department'), id: 'departmentCode'},
        { fn: () => validateRequired(data.courseCode, 'Course Code'),  id: 'courseCode'  },
        { fn: () => validateRequired(data.semester, 'Semester'),       id: 'semester'    },
        { fn: () => validateRequired(data.academicYear, 'Academic Year'), id: 'academicYear'},
    ];

    checks.forEach(({ fn, id }) => {
        const result = fn();
        if (!result.valid) { showFieldError(id, result.message); errors.push(result.message); }
    });
    return errors.length === 0;
}

// ── Validate faculty registration form ─────────────────────────────────────
function validateFacultyForm(data) {
    const errors = [];
    clearAllErrors('faculty-form');

    const checks = [
        { fn: () => validateRequired(data.employeeId, 'Employee ID'), id: 'employeeId' },
        { fn: () => validateName(data.firstName, 'First Name'),        id: 'firstName'  },
        { fn: () => validateName(data.lastName,  'Last Name'),         id: 'lastName'   },
        { fn: () => validateEmail(data.email),                         id: 'email'      },
        { fn: () => validatePhone(data.phone),                         id: 'phone'      },
        { fn: () => validateRequired(data.departmentCode, 'Department'), id: 'departmentCode'},
    ];

    checks.forEach(({ fn, id }) => {
        const result = fn();
        if (!result.valid) { showFieldError(id, result.message); errors.push(result.message); }
    });
    return errors.length === 0;
}

// ── Validate course form ────────────────────────────────────────────────────
function validateCourseForm(data) {
    const errors = [];
    clearAllErrors('course-form');

    const checks = [
        { fn: () => validateRequired(data.courseCode,    'Course Code'),    id: 'courseCode'   },
        { fn: () => validateRequired(data.courseName,    'Course Name'),    id: 'courseName'   },
        { fn: () => validateRequired(data.credits,       'Credits'),        id: 'credits'      },
        { fn: () => validateRequired(data.semester,      'Semester'),       id: 'semester'     },
        { fn: () => validateRequired(data.academicYear,  'Academic Year'),  id: 'academicYear' },
        { fn: () => validateRequired(data.departmentCode,'Department'),     id: 'departmentCode'},
    ];

    checks.forEach(({ fn, id }) => {
        const result = fn();
        if (!result.valid) { showFieldError(id, result.message); errors.push(result.message); }
    });
    return errors.length === 0;
}
