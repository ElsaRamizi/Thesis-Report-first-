import { formatResearchType } from './QuestionnaireForm';

export default function ResearchStudyCard({
  study,
  actionLabel,
  onAction,
  secondaryLabel,
  onSecondary,
  joined = false,
}) {
  return (
    <article className="research-card">
      <div className="research-card-header">
        <div>
          <p className="eyebrow">{formatResearchType(study.researchType)}</p>
          <h3>{study.title}</h3>
        </div>
        <div className="chip-row">
          {study.rewarded ? <span className="badge badge-reward">Rewarded</span> : null}
          {study.anonymousFriendly ? <span className="badge badge-anon">Anonymous OK</span> : null}
          {joined ? <span className="badge badge-joined">Joined</span> : null}
          {study.status ? <span className="badge badge-status">{study.status}</span> : null}
        </div>
      </div>

      <p className="research-card-description">{study.description}</p>

      <div className="research-card-meta">
        <div>
          <span>Researcher</span>
          <strong>{study.researcherName}</strong>
        </div>
        {study.estimatedDuration ? (
          <div>
            <span>Duration</span>
            <strong>{study.estimatedDuration}</strong>
          </div>
        ) : null}
        {study.participantCount != null ? (
          <div>
            <span>Participants</span>
            <strong>{study.participantCount}</strong>
          </div>
        ) : null}
      </div>

      {study.participationRequirements ? (
        <p className="research-card-requirements">
          <strong>Requirements:</strong> {study.participationRequirements}
        </p>
      ) : null}

      {study.rewarded && study.rewardDetails ? (
        <p className="research-card-reward">{study.rewardDetails}</p>
      ) : null}

      {(actionLabel || secondaryLabel) ? (
        <div className="actions-row research-card-actions">
          {actionLabel ? (
            <button type="button" className="button button-primary" onClick={onAction}>
              {actionLabel}
            </button>
          ) : null}
          {secondaryLabel ? (
            <button type="button" className="button button-secondary" onClick={onSecondary}>
              {secondaryLabel}
            </button>
          ) : null}
        </div>
      ) : null}
    </article>
  );
}
