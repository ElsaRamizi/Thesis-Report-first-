import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="status-page">
      <p className="eyebrow">404</p>
      <h1>Page not found</h1>
      <p>The route you requested does not exist in the frontend app.</p>
      <Link to="/">Return to the app</Link>
    </div>
  );
}
