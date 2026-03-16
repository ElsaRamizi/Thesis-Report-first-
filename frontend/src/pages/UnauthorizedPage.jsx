import { Link } from 'react-router-dom';

export default function UnauthorizedPage() {
  return (
    <div className="status-page">
      <p className="eyebrow">403</p>
      <h1>Access denied</h1>
      <p>You are signed in, but this page is not available for your role.</p>
      <Link to="/">Go back home</Link>
    </div>
  );
}
