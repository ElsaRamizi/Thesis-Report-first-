export default function StimulusDisplay({ trial, phase }) {
  const showStimulus = phase === 'stimulus';

  return (
    <div className="dual-display" aria-live="polite">
      <div className="dual-grid" aria-label="3 by 3 visual position grid">
        {Array.from({ length: 9 }, (_, index) => (
          <div
            key={index}
            className={`dual-cell ${showStimulus && trial?.position === index ? 'dual-cell-active' : ''}`}
          />
        ))}
      </div>
      <div className="dual-letter" aria-label="Letter stimulus">
        {showStimulus ? trial?.letter : ''}
      </div>
    </div>
  );
}
