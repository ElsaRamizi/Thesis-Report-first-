import { useEffect, useMemo, useState } from 'react';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import PageHero from '../components/layout/PageHero';
import TabBar from '../components/layout/TabBar';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import { DATA_SHARING_CONSENT, PARTICIPANT_RESEARCH_TABS } from '../features/research/constants';
import QuestionnaireForm, { buildAnswerPayload } from '../features/research/QuestionnaireForm';
import ResearchStudyCard from '../features/research/ResearchStudyCard';
import {
  browseResearchStudies,
  fetchMyParticipations,
  fetchResearchStudy,
  joinResearchStudy,
  updateParticipationAnswers,
  withdrawFromStudy,
} from '../services/researchService';

const tabs = PARTICIPANT_RESEARCH_TABS;

const formatDate = (value) => (value ? new Date(value).toLocaleString() : '-');

export default function PatientResearchPage() {
  const [activeTab, setActiveTab] = useState('discover');
  const [studies, setStudies] = useState([]);
  const [participations, setParticipations] = useState([]);
  const [selectedStudy, setSelectedStudy] = useState(null);
  const [selectedParticipation, setSelectedParticipation] = useState(null);
  const [answers, setAnswers] = useState({});
  const [anonymous, setAnonymous] = useState(false);
  const [consentAccepted, setConsentAccepted] = useState(false);
  const [dataSharingAccepted, setDataSharingAccepted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const joinedStudyIds = useMemo(
    () => new Set(participations.filter((item) => item.status !== 'WITHDRAWN').map((item) => item.studyId)),
    [participations],
  );

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [studyData, participationData] = await Promise.all([
        browseResearchStudies(),
        fetchMyParticipations(),
      ]);
      setStudies(studyData);
      setParticipations(participationData);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Research studies could not be loaded.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openStudy = async (studyId) => {
    setError('');
    setSuccess('');
    try {
      const detail = await fetchResearchStudy(studyId);
      setSelectedStudy(detail);
      setSelectedParticipation(null);
      setAnswers({});
      setAnonymous(false);
      setConsentAccepted(false);
      setDataSharingAccepted(false);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Study details could not be loaded.');
    }
  };

  const openParticipation = async (participation) => {
    setError('');
    setSuccess('');
    try {
      const detail = await fetchResearchStudy(participation.studyId);
      setSelectedStudy(detail);
      setSelectedParticipation(participation);
      const initialAnswers = {};
      (participation.answers ?? []).forEach((answer) => {
        initialAnswers[answer.questionId] = answer.value;
      });
      setAnswers(initialAnswers);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Participation details could not be loaded.');
    }
  };

  const handleAnswerChange = (questionId, value) => {
    setAnswers((current) => ({ ...current, [questionId]: value }));
  };

  const handleJoin = async () => {
    if (!selectedStudy) {
      return;
    }

    if (!consentAccepted) {
      setError('Study consent must be accepted to join.');
      return;
    }

    if (!dataSharingAccepted) {
      setError(`You must consent: "${DATA_SHARING_CONSENT}"`);
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      const payload = {
        anonymous,
        consentAccepted,
        dataSharingAccepted,
        answers: buildAnswerPayload(selectedStudy.questions, answers),
      };
      await joinResearchStudy(selectedStudy.id, payload);
      setSuccess('You have joined the study successfully.');
      setSelectedStudy(null);
      await loadData();
      setActiveTab('joined');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Could not join the study.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSaveAnswers = async () => {
    if (!selectedParticipation || !selectedStudy) {
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await updateParticipationAnswers(
        selectedParticipation.id,
        buildAnswerPayload(selectedStudy.questions, answers),
      );
      setSuccess('Your responses have been saved.');
      await loadData();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Responses could not be saved.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleWithdraw = async (participationId) => {
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await withdrawFromStudy(participationId);
      setSuccess('You have withdrawn from the study.');
      setSelectedParticipation(null);
      setSelectedStudy(null);
      await loadData();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Withdrawal failed.');
    } finally {
      setSubmitting(false);
    }
  };

  const activeParticipations = participations.filter((item) => item.status !== 'WITHDRAWN');

  return (
    <div className="stack-lg">
      <PageHero
        eyebrow="Research"
        title="Participate in Research"
        description="Browse studies, complete questionnaires, and track your participation progress."
      />

      {error ? <StatusMessage tone="error" title="Error" message={error} /> : null}
      {success ? <StatusMessage tone="neutral" title="Success" message={success} /> : null}

      <TabBar tabs={tabs} activeTab={activeTab} onChange={(tabId) => {
        setActiveTab(tabId);
        setSelectedStudy(null);
        setSelectedParticipation(null);
      }} />

      {loading ? <LoadingState label="Loading research studies..." /> : null}

      {!loading && !selectedStudy && activeTab === 'discover' ? (
        <div className="research-card-grid">
          {studies.length === 0 ? (
            <StatusMessage tone="warning" message="No published studies are available right now." />
          ) : null}
          {studies.map((study) => (
            <ResearchStudyCard
              key={study.id}
              study={study}
              joined={joinedStudyIds.has(study.id)}
              actionLabel={joinedStudyIds.has(study.id) ? 'View Details' : 'Explore Study'}
              onAction={() => openStudy(study.id)}
            />
          ))}
        </div>
      ) : null}

      {!loading && !selectedStudy && activeTab === 'joined' ? (
        <div className="stack-lg">
          {activeParticipations.length === 0 ? (
            <StatusMessage tone="warning" message="You have not joined any studies yet." />
          ) : null}
          {activeParticipations.map((participation) => (
            <Card key={participation.id} title={participation.studyTitle} accent="warm">
              <div className="research-joined-meta">
                <span className="badge badge-status">{participation.status.replace(/_/g, ' ')}</span>
                {participation.anonymous ? <span className="badge badge-anon">Anonymous</span> : null}
                {participation.rewarded ? <span className="badge badge-reward">Rewarded</span> : null}
              </div>
              <p className="research-card-description">Researcher: {participation.researcherName}</p>
              <div className="progress-shell">
                <div className="progress-bar" style={{ width: `${participation.progressPercent}%` }} />
              </div>
              <p>{participation.progressPercent}% complete · Joined {formatDate(participation.joinedAt)}</p>
              <div className="actions-row">
                <Button onClick={() => openParticipation(participation)}>Continue</Button>
                <button
                  type="button"
                  className="button button-secondary"
                  disabled={submitting}
                  onClick={() => handleWithdraw(participation.id)}
                >
                  Withdraw
                </button>
              </div>
            </Card>
          ))}
        </div>
      ) : null}

      {selectedStudy ? (
        <Card title={selectedStudy.title} accent="cool">
          <p className="research-card-description">{selectedStudy.description}</p>
          {selectedStudy.instructions ? (
            <p><strong>Instructions:</strong> {selectedStudy.instructions}</p>
          ) : null}

          <QuestionnaireForm
            questions={selectedStudy.questions}
            answers={answers}
            onChange={handleAnswerChange}
            readOnly={selectedParticipation?.status === 'COMPLETED'}
          />

          {!selectedParticipation ? (
            <div className="research-consent-block">
              {selectedStudy.anonymousFriendly ? (
                <label className="research-choice">
                  <input
                    type="checkbox"
                    checked={anonymous}
                    onChange={(event) => setAnonymous(event.target.checked)}
                  />
                  <span>Participate anonymously (your name will be hidden from researchers)</span>
                </label>
              ) : null}
              <label className="research-choice">
                <input
                  type="checkbox"
                  checked={consentAccepted}
                  required
                  onChange={(event) => setConsentAccepted(event.target.checked)}
                />
                <span>{selectedStudy.consentText} *</span>
              </label>
              <label className="research-choice">
                <input
                  type="checkbox"
                  checked={dataSharingAccepted}
                  required
                  onChange={(event) => setDataSharingAccepted(event.target.checked)}
                />
                <span>{DATA_SHARING_CONSENT} *</span>
              </label>
              <div className="actions-row">
                <Button
                  disabled={submitting || !consentAccepted || !dataSharingAccepted}
                  onClick={handleJoin}
                >
                  {submitting ? 'Joining...' : 'Join Study'}
                </Button>
                <button type="button" className="button button-secondary" onClick={() => setSelectedStudy(null)}>
                  Back
                </button>
              </div>
            </div>
          ) : (
            <div className="actions-row">
              {selectedParticipation.status !== 'COMPLETED' ? (
                <Button disabled={submitting} onClick={handleSaveAnswers}>
                  {submitting ? 'Saving...' : 'Save Responses'}
                </Button>
              ) : null}
              <button
                type="button"
                className="button button-secondary"
                disabled={submitting}
                onClick={() => handleWithdraw(selectedParticipation.id)}
              >
                Withdraw
              </button>
              <button type="button" className="button button-secondary" onClick={() => setSelectedStudy(null)}>
                Back
              </button>
            </div>
          )}
        </Card>
      ) : null}
    </div>
  );
}
