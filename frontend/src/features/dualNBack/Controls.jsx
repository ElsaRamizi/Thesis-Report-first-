import Button from '../../components/ui/Button';

export default function Controls({ onPosition, onLetter, responses, disabled }) {
  return (
    <div className="dual-controls">
      <Button onClick={onPosition} disabled={disabled || responses.position}>
        Position match
      </Button>
      <Button onClick={onLetter} disabled={disabled || responses.letter}>
        Letter match
      </Button>
    </div>
  );
}
