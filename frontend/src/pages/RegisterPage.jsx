import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { registerUser } from '../services/authService';
import { validatePassword } from '../utils/passwordValidation';

const initialForm = {
  email: '',
  password: '',
  confirmPassword: '',
  role: 'USER',
};

// create account — password gets BCrypt hashed on backend, no auto-login
export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  /// typing in form fields
  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  /// validate passwords match, POST /api/auth/register, then go to login
  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    const passwordCheck = validatePassword(form.password);
    if (!passwordCheck.valid) {
      setError(`Password needs: ${passwordCheck.failures.join(', ')}.`);
      return;
    }

    setLoading(true);

    try {
      await registerUser({
        email: form.email,
        password: form.password,
        role: form.role,
      });
      setSuccess('Registration successful. Redirecting to login...');
      setTimeout(() => navigate('/login'), 1200);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to register.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-panel">
        <p className="eyebrow">ELTE · MindMetrics</p>
        <h1>Create account</h1>
        <p className="auth-copy">Create an account. Pick participant if you will play the tasks, clinician if you will review results.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <Input label="Email" name="email" type="email" value={form.email} onChange={handleChange} required />
          <Input label="Password" name="password" type="password" value={form.password} onChange={handleChange} required />
          <p className="auth-copy">Use at least 8 characters with uppercase, lowercase, and a number.</p>
          <Input label="Confirm password" name="confirmPassword" type="password" value={form.confirmPassword} onChange={handleChange} required />

          <label className="field">
            <span>Account type</span>
            <select className="input" name="role" value={form.role} onChange={handleChange}>
              <option value="USER">Participant</option>
              <option value="CLINICIAN">Clinician</option>
            </select>
          </label>

          {error ? <p className="submit-error">{error}</p> : null}
          {success ? <p className="submit-success">{success}</p> : null}
          <Button type="submit" disabled={loading}>{loading ? 'Creating account...' : 'Register'}</Button>
        </form>

        <p className="auth-footer">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
