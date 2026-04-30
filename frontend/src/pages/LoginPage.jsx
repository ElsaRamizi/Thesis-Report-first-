import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { useAuth } from '../context/useAuth';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const redirectTo = location.state?.from?.pathname;

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const data = await login(form);
      const destination = redirectTo ?? (data.role === 'CLINICIAN' ? '/clinician/dashboard' : '/user/dashboard');
      navigate(destination, { replace: true });
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to sign in.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page auth-login">
      <div className="auth-panel">
        <p className="eyebrow">MindMetrics</p>
        <h1>Welcome back</h1>
        <p className="auth-copy">Sign in to access your cognitive assessment workspace.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <Input label="Email" name="email" type="email" value={form.email} onChange={handleChange} required />
          <Input label="Password" name="password" type="password" value={form.password} onChange={handleChange} required />
          {error ? <p className="submit-error">{error}</p> : null}
          <Button type="submit" disabled={loading}>{loading ? 'Signing in...' : 'Log In'}</Button>
        </form>

        <p className="auth-footer">
          Need an account? <Link to="/register">Create one</Link>
        </p>
      </div>
    </div>
  );
}
