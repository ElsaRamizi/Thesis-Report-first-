export default function Card({ title, children, accent = 'default', footer }) {
  return (
    <section className={`card card-${accent}`}>
      {title ? <h3>{title}</h3> : null}
      <div>{children}</div>
      {footer ? <div className="card-footer">{footer}</div> : null}
    </section>
  );
}
