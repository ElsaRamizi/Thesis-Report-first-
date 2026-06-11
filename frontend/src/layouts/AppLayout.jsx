import { NavLink, useNavigate } from 'react-router-dom';
import { NAV_BY_ROLE } from '../config/navigation';
import { useAuth } from '../context/useAuth';
import AppFooter from '../components/layout/AppFooter';

const roleLabels = {
  USER: 'Participant',
  CLINICIAN: 'Clinician',
};

export default function AppLayout({ title, children }) {
  const { role, logout } = useAuth();
  const navigate = useNavigate();
  const navItems = NAV_BY_ROLE[role] ?? [];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-block">
          <p className="eyebrow">ELTE · MindMetrics</p>
          <h1>MindMetrics</h1>
        </div>

        <nav className="nav-list" aria-label="Main navigation">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              title={item.label}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
            >
              <span className="nav-link-full">{item.label}</span>
              <span className="nav-link-short">{item.shortLabel}</span>
            </NavLink>
          ))}
        </nav>

        <div className="topbar-actions">
          <span className="role-pill">{roleLabels[role] ?? role}</span>
          <button type="button" className="ghost-button topbar-logout" onClick={handleLogout}>
            Log out
          </button>
        </div>
      </header>

      <main className="main-panel">
        <header className="page-header">
          <div>
            <p className="eyebrow">{roleLabels[role] ?? role}</p>
            <h2>{title}</h2>
          </div>
        </header>
        {children}
      </main>

      <AppFooter />
    </div>
  );
}
