import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItemsByRole = {
  USER: [
    { to: '/user/dashboard', label: 'Dashboard' },
    { to: '/tasks', label: 'Tasks' },
    { to: '/sessions', label: 'History' },
    { to: '/results/latest', label: 'Results' },
  ],
  CLINICIAN: [
    { to: '/clinician/dashboard', label: 'Clinician Dashboard' },
    { to: '/user/dashboard', label: 'Participant View' },
    { to: '/tasks', label: 'Tasks' },
    { to: '/sessions', label: 'History' },
    { to: '/results/latest', label: 'Results' },
  ],
};

export default function AppLayout({ title, children }) {
  const { role, logout } = useAuth();
  const navigate = useNavigate();
  const navItems = navItemsByRole[role] ?? [];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-block">
          <p className="eyebrow">MindMetrics</p>
          <h1>Research Console</h1>
        </div>

        <nav className="nav-list">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="topbar-actions">
          <span className="role-pill">{role}</span>
          <button className="ghost-button topbar-logout" onClick={handleLogout}>Log Out</button>
        </div>
      </header>

      <main className="main-panel">
        <header className="page-header">
          <div>
            <p className="eyebrow">Signed in as {role}</p>
            <h2>{title}</h2>
          </div>
        </header>
        {children}
      </main>
    </div>
  );
}
