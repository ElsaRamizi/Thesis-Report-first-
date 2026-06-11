const QUESTION_TYPES = [
  { value: 'SHORT_TEXT', label: 'Short text' },
  { value: 'LONG_TEXT', label: 'Long text' },
  { value: 'MULTIPLE_CHOICE', label: 'Multiple choice' },
  { value: 'SINGLE_CHOICE', label: 'Single choice' },
  { value: 'YES_NO', label: 'Yes / No' },
  { value: 'DROPDOWN', label: 'Dropdown' },
  { value: 'NUMERIC', label: 'Numeric input' },
];

const CHOICE_TYPES = new Set(['MULTIPLE_CHOICE', 'SINGLE_CHOICE', 'DROPDOWN']);

export const formatResearchType = (type) => (
  type === 'IN_PERSON_TESTING' ? 'In-Person Testing' : 'Online Testing'
);

export const formatStatus = (status) => status.replace(/_/g, ' ');

export const buildAnswerPayload = (questions, answers) => (
  questions.map((question) => ({
    questionId: question.id,
    value: answers[question.id] ?? '',
  }))
);

export default function QuestionnaireForm({ questions, answers, onChange, readOnly = false }) {
  const renderInput = (question) => {
    const value = answers[question.id] ?? '';
    const commonProps = {
      disabled: readOnly,
      className: 'input',
    };

    if (question.questionKey === 'DEMOGRAPHIC_DOB') {
      return (
        <input
          type="date"
          {...commonProps}
          value={value}
          onChange={(event) => onChange(question.id, event.target.value)}
        />
      );
    }

    if (question.questionType === 'LONG_TEXT') {
      return (
        <textarea
          {...commonProps}
          rows={4}
          value={value}
          onChange={(event) => onChange(question.id, event.target.value)}
        />
      );
    }

    if (question.questionType === 'YES_NO') {
      return (
        <select
          {...commonProps}
          value={value}
          onChange={(event) => onChange(question.id, event.target.value)}
        >
          <option value="">Select...</option>
          <option value="Yes">Yes</option>
          <option value="No">No</option>
        </select>
      );
    }

    if (question.questionType === 'DROPDOWN') {
      return (
        <select
          {...commonProps}
          value={value}
          onChange={(event) => onChange(question.id, event.target.value)}
        >
          <option value="">Select...</option>
          {(question.options ?? []).map((option) => (
            <option key={option} value={option}>{option}</option>
          ))}
        </select>
      );
    }

    if (question.questionType === 'SINGLE_CHOICE') {
      return (
        <div className="options-grid">
          {(question.options ?? []).map((option) => (
            <label key={option} className="research-choice">
              <input
                type="radio"
                name={`question-${question.id}`}
                checked={value === option}
                disabled={readOnly}
                onChange={() => onChange(question.id, option)}
              />
              <span>{option}</span>
            </label>
          ))}
        </div>
      );
    }

    if (question.questionType === 'MULTIPLE_CHOICE') {
      const selected = value ? value.split(',').map((item) => item.trim()) : [];
      return (
        <div className="options-grid">
          {(question.options ?? []).map((option) => (
            <label key={option} className="research-choice">
              <input
                type="checkbox"
                checked={selected.includes(option)}
                disabled={readOnly}
                onChange={(event) => {
                  const next = event.target.checked
                    ? [...selected, option]
                    : selected.filter((item) => item !== option);
                  onChange(question.id, next.join(', '));
                }}
              />
              <span>{option}</span>
            </label>
          ))}
        </div>
      );
    }

    return (
      <input
        type={question.questionType === 'NUMERIC' ? 'number' : 'text'}
        {...commonProps}
        value={value}
        onChange={(event) => onChange(question.id, event.target.value)}
      />
    );
  };

  return (
    <div className="research-form-grid">
      {questions.map((question) => (
        <label key={question.id} className="field">
          <span>
            {question.questionText}
            {question.required ? ' *' : ''}
            {question.demographicDefault ? ' (Required demographic)' : ''}
          </span>
          {renderInput(question)}
          {CHOICE_TYPES.has(question.questionType) && !readOnly ? (
            <small className="research-hint">Options configured by the researcher.</small>
          ) : null}
        </label>
      ))}
    </div>
  );
}

export { QUESTION_TYPES, CHOICE_TYPES };
