const PASSWORD_RULES = [
  { test: (value) => value.length >= 8, message: 'At least 8 characters' },
  { test: (value) => /[A-Z]/.test(value), message: 'One uppercase letter' },
  { test: (value) => /[a-z]/.test(value), message: 'One lowercase letter' },
  { test: (value) => /\d/.test(value), message: 'One number' },
];

export const validatePassword = (password) => {
  const failures = PASSWORD_RULES.filter((rule) => !rule.test(password)).map((rule) => rule.message);
  return {
    valid: failures.length === 0,
    failures,
  };
};

export default PASSWORD_RULES;
