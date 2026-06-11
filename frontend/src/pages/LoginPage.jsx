import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { useAuth } from '../context/useAuth';

// same login page for USER and CLINICIAN — role decides where we redirect
export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // if they got sent here from a protected page, go back after login
  const redirectTo = location.state?.from?.pathname;

  /// update email/password in form state
  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  /// form submit — call AuthContext.login then navigate to dashboard
  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const data = await login(form);
      const destination = redirectTo ?? (data.role === 'CLINICIAN' ? '/clinician/dashboard' : '/user/dashboard');
      navigate(destination, { replace: true });
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-panel">
        <p className="eyebrow">ELTE · MindMetrics</p>
        <h1>Sign in</h1>
        <p className="auth-copy">Log in with the account you registered. Participants and clinicians use the same page.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <Input label="Email" name="email" type="email" value={form.email} onChange={handleChange} required />
          <Input label="Password" name="password" type="password" value={form.password} onChange={handleChange} required />
          {error ? <p className="submit-error">{error}</p> : null}
          <Button type="submit" disabled={loading}>{loading ? 'Signing in...' : 'Sign in'}</Button>
        </form>

        <p className="auth-footer">
          No account yet? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  );
}
