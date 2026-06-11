export default function PageHero({ eyebrow, title, description, children }) {
  return (
    <section className="hero-panel">
      {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
      {title ? <h2>{title}</h2> : null}
      {description ? <p>{description}</p> : null}
      {children}
    </section>
  );
}
