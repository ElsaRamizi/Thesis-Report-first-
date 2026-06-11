import { Link } from 'react-router-dom';
import Card from '../ui/Card';

export default function QuickActionCard({ title, description, to, linkLabel, accent = 'default' }) {
  return (
    <Card title={title} accent={accent} footer={to ? <Link className="inline-link" to={to}>{linkLabel}</Link> : null}>
      <p>{description}</p>
    </Card>
  );
}
