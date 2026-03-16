export default function StatusMessage({ tone = 'neutral', title, message }) {
  return (
    <div className={`status-banner status-${tone}`}>
      {title ? <strong>{title}</strong> : null}
      <p>{message}</p>
    </div>
  );
}
