import { useEffect, useState } from 'react';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import PageHero from '../components/layout/PageHero';
import TabBar from '../components/layout/TabBar';
import Input from '../components/ui/Input';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import {
  CLINICIAN_RESEARCH_TABS,
  EMPTY_FILTER,
  EMPTY_QUESTION,
  EMPTY_STUDY_FORM,
} from '../features/research/constants';
import { QUESTION_TYPES } from '../features/research/QuestionnaireForm';
import ResearchStudyCard from '../features/research/ResearchStudyCard';
import {
  addResearchQuestion,
  compareResearchCohorts,
  createResearchCohort,
  createResearchStudy,
  deleteResearchQuestion,
  deleteResearchStudy,
  exportResearchData,
  fetchClinicianStudies,
  fetchClinicianStudy,
  fetchResearchAnalytics,
  fetchResearchCohorts,
  previewResearchFilter,
  publishResearchStudy,
  updateResearchStudy,
} from '../services/researchService';

const emptyStudyForm = EMPTY_STUDY_FORM;
const emptyQuestion = EMPTY_QUESTION;
const emptyFilter = EMPTY_FILTER;
const clinicianTabs = CLINICIAN_RESEARCH_TABS;

export default function ClinicianResearchPage() {
  const [activeTab, setActiveTab] = useState('studies');
  const [studies, setStudies] = useState([]);
  const [selectedStudy, setSelectedStudy] = useState(null);
  const [studyForm, setStudyForm] = useState(emptyStudyForm);
  const [customQuestions, setCustomQuestions] = useState([]);
  const [newQuestion, setNewQuestion] = useState(emptyQuestion);
  const [cohorts, setCohorts] = useState([]);
  const [filters, setFilters] = useState([emptyFilter]);
  const [cohortName, setCohortName] = useState('');
  const [cohortDescription, setCohortDescription] = useState('');
  const [analytics, setAnalytics] = useState(null);
  const [preview, setPreview] = useState([]);
  const [comparison, setComparison] = useState(null);
  const [selectedCohortIds, setSelectedCohortIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadStudies = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchClinicianStudies();
      setStudies(data);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Studies could not be loaded.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStudies();
  }, []);

  const loadStudyDetail = async (studyId) => {
    setError('');
    try {
      const detail = await fetchClinicianStudy(studyId);
      setSelectedStudy(detail);
      const cohortData = await fetchResearchCohorts(studyId);
      setCohorts(cohortData);
      setStudyForm({
        title: detail.title,
        description: detail.description,
        instructions: detail.instructions ?? '',
        participationRequirements: detail.participationRequirements ?? '',
        estimatedDuration: detail.estimatedDuration ?? '',
        researchType: detail.researchType,
        rewarded: detail.rewarded,
        rewardDetails: detail.rewardDetails ?? '',
        anonymousFriendly: detail.anonymousFriendly,
        consentText: detail.consentText ?? '',
      });
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Study details could not be loaded.');
    }
  };

  const buildStudyPayload = () => ({
    ...studyForm,
    customQuestions: customQuestions.map((question, index) => ({
      questionText: question.questionText,
      questionType: question.questionType,
      options: question.optionsText
        ? question.optionsText.split('\n').map((item) => item.trim()).filter(Boolean)
        : [],
      required: question.required,
      sortOrder: question.sortOrder ?? index + 2,
    })),
  });

  const handleCreateStudy = async () => {
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      const created = await createResearchStudy(buildStudyPayload());
      setSuccess('Study draft created with default demographic questions.');
      setCustomQuestions([]);
      setStudyForm(emptyStudyForm);
      await loadStudies();
      await loadStudyDetail(created.id);
      setActiveTab('studies');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Study could not be created.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdateStudy = async () => {
    if (!selectedStudy) {
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      await updateResearchStudy(selectedStudy.id, buildStudyPayload());
      setSuccess('Study updated.');
      await loadStudyDetail(selectedStudy.id);
      await loadStudies();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Study could not be updated.');
    } finally {
      setSubmitting(false);
    }
  };

  const handlePublishStudy = async () => {
    if (!selectedStudy) {
      return;
    }
    setSubmitting(true);
    try {
      await publishResearchStudy(selectedStudy.id);
      setSuccess('Study published for participant recruitment.');
      await loadStudyDetail(selectedStudy.id);
      await loadStudies();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Study could not be published.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteStudy = async (studyId) => {
    setSubmitting(true);
    try {
      await deleteResearchStudy(studyId);
      setSelectedStudy(null);
      setSuccess('Study deleted.');
      await loadStudies();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Study could not be deleted.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddQuestion = async () => {
    if (!selectedStudy) {
      return;
    }
    setSubmitting(true);
    try {
      await addResearchQuestion(selectedStudy.id, {
        questionText: newQuestion.questionText,
        questionType: newQuestion.questionType,
        options: newQuestion.optionsText
          ? newQuestion.optionsText.split('\n').map((item) => item.trim()).filter(Boolean)
          : [],
        required: newQuestion.required,
        sortOrder: newQuestion.sortOrder,
      });
      setNewQuestion(emptyQuestion);
      await loadStudyDetail(selectedStudy.id);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Question could not be added.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteQuestion = async (questionId) => {
    if (!selectedStudy) {
      return;
    }
    setSubmitting(true);
    try {
      await deleteResearchQuestion(questionId);
      await loadStudyDetail(selectedStudy.id);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Question could not be deleted.');
    } finally {
      setSubmitting(false);
    }
  };

  const buildFiltersPayload = () => filters.map((filter) => ({
    questionId: filter.questionId ? Number(filter.questionId) : null,
    questionKey: filter.questionKey || null,
    operator: filter.operator,
    value: filter.value || null,
    minAge: filter.minAge ? Number(filter.minAge) : null,
    maxAge: filter.maxAge ? Number(filter.maxAge) : null,
    values: filter.valuesText
      ? filter.valuesText.split(',').map((item) => item.trim()).filter(Boolean)
      : null,
  }));

  const handlePreviewFilter = async () => {
    if (!selectedStudy) {
      return;
    }
    setSubmitting(true);
    try {
      const data = await previewResearchFilter(selectedStudy.id, buildFiltersPayload());
      setPreview(data);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Filter preview failed.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRunAnalytics = async () => {
    if (!selectedStudy) {
      return;
    }
    setSubmitting(true);
    try {
      const data = await fetchResearchAnalytics(selectedStudy.id, buildFiltersPayload());
      setAnalytics(data);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Analytics could not be loaded.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSaveCohort = async () => {
    if (!selectedStudy) {
      return;
    }
    setSubmitting(true);
    try {
      await createResearchCohort(selectedStudy.id, {
        name: cohortName,
        description: cohortDescription,
        filters: buildFiltersPayload(),
      });
      setCohortName('');
      setCohortDescription('');
      setSuccess('Filter saved.');
      await loadStudyDetail(selectedStudy.id);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Could not save filter.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCompareCohorts = async () => {
    if (!selectedStudy || selectedCohortIds.length < 2) {
      setError('Pick at least two filters to compare.');
      return;
    }
    setSubmitting(true);
    try {
      const data = await compareResearchCohorts(selectedStudy.id, { cohortIds: selectedCohortIds });
      setComparison(data);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Comparison failed.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleExport = async () => {
    if (!selectedStudy) {
      return;
    }
    setSubmitting(true);
    try {
      const blob = await exportResearchData(selectedStudy.id, buildFiltersPayload());
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `research-export-${selectedStudy.id}.csv`;
      link.click();
      window.URL.revokeObjectURL(url);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Export failed.');
    } finally {
      setSubmitting(false);
    }
  };

  const customStudyQuestions = selectedStudy?.questions?.filter((question) => !question.demographicDefault) ?? [];

  return (
    <div className="stack-lg">
      <PageHero
        eyebrow="Research"
        title="My Research"
        description="Studies, questionnaires, filters, CSV export."
      />

      {error ? <StatusMessage tone="error" title="Error" message={error} /> : null}
      {success ? <StatusMessage tone="neutral" title="Success" message={success} /> : null}

      <TabBar tabs={clinicianTabs} activeTab={activeTab} onChange={setActiveTab} />

      {loading && activeTab === 'studies' ? <LoadingState label="Loading your studies..." /> : null}

      {activeTab === 'studies' && !loading ? (
        <div className="stack-lg">
          <div className="research-card-grid">
            {studies.length === 0 ? (
              <StatusMessage tone="warning" message="You have not created any studies yet." />
            ) : null}
            {studies.map((study) => (
              <ResearchStudyCard
                key={study.id}
                study={study}
                actionLabel="Manage"
                onAction={() => {
                  loadStudyDetail(study.id);
                  setActiveTab('analytics');
                }}
                secondaryLabel="Edit"
                onSecondary={() => {
                  loadStudyDetail(study.id);
                  setActiveTab('create');
                }}
              />
            ))}
          </div>

          {selectedStudy ? (
            <Card title={`Managing: ${selectedStudy.title}`}>
              <div className="actions-row">
                <Button disabled={submitting || selectedStudy.status === 'PUBLISHED'} onClick={handlePublishStudy}>
                  Publish Study
                </Button>
                <button
                  type="button"
                  className="button button-secondary"
                  disabled={submitting}
                  onClick={() => handleDeleteStudy(selectedStudy.id)}
                >
                  Delete Study
                </button>
              </div>
            </Card>
          ) : null}
        </div>
      ) : null}

      {activeTab === 'create' ? (
        <div className="clinician-grid">
          <Card title="Study Details" accent="warm">
            <div className="research-form-grid">
              <Input label="Title" value={studyForm.title} onChange={(event) => setStudyForm({ ...studyForm, title: event.target.value })} />
              <label className="field">
                <span>Description</span>
                <textarea className="input" rows={4} value={studyForm.description} onChange={(event) => setStudyForm({ ...studyForm, description: event.target.value })} />
              </label>
              <label className="field">
                <span>Instructions</span>
                <textarea className="input" rows={3} value={studyForm.instructions} onChange={(event) => setStudyForm({ ...studyForm, instructions: event.target.value })} />
              </label>
              <Input label="Participation Requirements" value={studyForm.participationRequirements} onChange={(event) => setStudyForm({ ...studyForm, participationRequirements: event.target.value })} />
              <Input label="Estimated Duration" value={studyForm.estimatedDuration} onChange={(event) => setStudyForm({ ...studyForm, estimatedDuration: event.target.value })} />
              <label className="field">
                <span>Research Type</span>
                <select className="input" value={studyForm.researchType} onChange={(event) => setStudyForm({ ...studyForm, researchType: event.target.value })}>
                  <option value="ONLINE_TESTING">Online Testing</option>
                  <option value="IN_PERSON_TESTING">In-Person Testing</option>
                </select>
              </label>
              <label className="research-choice">
                <input type="checkbox" checked={studyForm.rewarded} onChange={(event) => setStudyForm({ ...studyForm, rewarded: event.target.checked })} />
                <span>Rewarded study</span>
              </label>
              {studyForm.rewarded ? (
                <Input label="Reward Details" value={studyForm.rewardDetails} onChange={(event) => setStudyForm({ ...studyForm, rewardDetails: event.target.value })} />
              ) : null}
              <label className="research-choice">
                <input type="checkbox" checked={studyForm.anonymousFriendly} onChange={(event) => setStudyForm({ ...studyForm, anonymousFriendly: event.target.checked })} />
                <span>Allow optional anonymous participation</span>
              </label>
              <label className="field">
                <span>Consent Text</span>
                <textarea className="input" rows={3} value={studyForm.consentText} onChange={(event) => setStudyForm({ ...studyForm, consentText: event.target.value })} />
              </label>
            </div>
            <div className="actions-row">
              {selectedStudy ? (
                <Button disabled={submitting} onClick={handleUpdateStudy}>Update Study</Button>
              ) : (
                <Button disabled={submitting} onClick={handleCreateStudy}>Create Study Draft</Button>
              )}
            </div>
          </Card>

          <Card title="Questionnaire" accent="cool">
            <StatusMessage tone="neutral" message="Date of birth and gender are added automatically for filtering." />
            <div className="research-form-grid">
              <Input label="Question Text" value={newQuestion.questionText} onChange={(event) => setNewQuestion({ ...newQuestion, questionText: event.target.value })} />
              <label className="field">
                <span>Question Type</span>
                <select className="input" value={newQuestion.questionType} onChange={(event) => setNewQuestion({ ...newQuestion, questionType: event.target.value })}>
                  {QUESTION_TYPES.map((type) => (
                    <option key={type.value} value={type.value}>{type.label}</option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Options (one per line for choice questions)</span>
                <textarea className="input" rows={4} value={newQuestion.optionsText} onChange={(event) => setNewQuestion({ ...newQuestion, optionsText: event.target.value })} />
              </label>
            </div>
            {selectedStudy ? (
              <Button disabled={submitting} onClick={handleAddQuestion}>Add Question to Study</Button>
            ) : (
              <p className="research-hint">Create the study first, then add live questionnaire items.</p>
            )}

            {selectedStudy ? (
              <div className="research-question-list">
                {customStudyQuestions.map((question) => (
                  <div key={question.id} className="research-question-item">
                    <strong>{question.questionText}</strong>
                    <span>{question.questionType}</span>
                    <button type="button" className="button button-secondary" onClick={() => handleDeleteQuestion(question.id)}>
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            ) : null}

            {!selectedStudy && customQuestions.length > 0 ? (
              <div className="research-question-list">
                {customQuestions.map((question, index) => (
                  <div key={`${question.questionText}-${index}`} className="research-question-item">
                    <strong>{question.questionText}</strong>
                    <span>{question.questionType}</span>
                  </div>
                ))}
              </div>
            ) : null}
          </Card>
        </div>
      ) : null}

      {activeTab === 'analytics' ? (
        <div className="stack-lg">
          <Card title="Select Study">
            <div className="research-card-grid">
              {studies.map((study) => (
                <button
                  key={study.id}
                  type="button"
                  className={`research-select-card ${selectedStudy?.id === study.id ? 'research-select-card-active' : ''}`}
                  onClick={() => loadStudyDetail(study.id)}
                >
                  <strong>{study.title}</strong>
                  <span>{study.participantCount} participants</span>
                </button>
              ))}
            </div>
          </Card>

          {selectedStudy ? (
            <>
              <Card title="Participant filters" accent="cool">
                {filters.map((filter, index) => (
                  <div key={`filter-${index}`} className="research-filter-row">
                    <label className="field">
                      <span>Question</span>
                      <select
                        className="input"
                        value={filter.questionId}
                        onChange={(event) => {
                          const next = [...filters];
                          const question = selectedStudy.questions.find((item) => String(item.id) === event.target.value);
                          next[index] = {
                            ...next[index],
                            questionId: event.target.value,
                            questionKey: question?.questionKey ?? '',
                          };
                          setFilters(next);
                        }}
                      >
                        <option value="">Select question...</option>
                        {selectedStudy.questions.map((question) => (
                          <option key={question.id} value={question.id}>{question.questionText}</option>
                        ))}
                      </select>
                    </label>
                    <label className="field">
                      <span>Operator</span>
                      <select className="input" value={filter.operator} onChange={(event) => {
                        const next = [...filters];
                        next[index] = { ...next[index], operator: event.target.value };
                        setFilters(next);
                      }}>
                        <option value="EQUALS">Equals</option>
                        <option value="NOT_EQUALS">Not equals</option>
                        <option value="CONTAINS">Contains</option>
                        <option value="IN">In list</option>
                        <option value="YES">Yes</option>
                        <option value="NO">No</option>
                        <option value="AGE_MIN">Minimum age</option>
                        <option value="AGE_MAX">Maximum age</option>
                        <option value="AGE_BETWEEN">Age between</option>
                      </select>
                    </label>
                    <Input label="Value" value={filter.value} onChange={(event) => {
                      const next = [...filters];
                      next[index] = { ...next[index], value: event.target.value };
                      setFilters(next);
                    }} />
                    <Input label="Min Age" value={filter.minAge} onChange={(event) => {
                      const next = [...filters];
                      next[index] = { ...next[index], minAge: event.target.value };
                      setFilters(next);
                    }} />
                    <Input label="Max Age" value={filter.maxAge} onChange={(event) => {
                      const next = [...filters];
                      next[index] = { ...next[index], maxAge: event.target.value };
                      setFilters(next);
                    }} />
                    <Input label="Values (comma-separated)" value={filter.valuesText} onChange={(event) => {
                      const next = [...filters];
                      next[index] = { ...next[index], valuesText: event.target.value };
                      setFilters(next);
                    }} />
                  </div>
                ))}
                <div className="actions-row">
                  <button type="button" className="button button-secondary" onClick={() => setFilters([...filters, emptyFilter])}>
                    Add Filter
                  </button>
                  <Button disabled={submitting} onClick={handlePreviewFilter}>Preview Matches</Button>
                  <Button disabled={submitting} onClick={handleRunAnalytics}>Run Analytics</Button>
                  <Button disabled={submitting} onClick={handleSaveCohort}>Save filter</Button>
                  <button type="button" className="button button-secondary" disabled={submitting} onClick={handleExport}>
                    Export CSV
                  </button>
                </div>
                <div className="research-form-grid">
                  <Input label="Name" value={cohortName} onChange={(event) => setCohortName(event.target.value)} />
                  <Input label="Description" value={cohortDescription} onChange={(event) => setCohortDescription(event.target.value)} />
                </div>
              </Card>

              {preview.length > 0 ? (
                <Card title={`Filter Preview (${preview.length} participants)`}>
                  <div className="participant-list">
                    {preview.map((participant) => (
                      <div key={participant.participationId} className="research-question-item">
                        <strong>{participant.displayName}</strong>
                        <span>{participant.status} · {participant.progressPercent}%</span>
                      </div>
                    ))}
                  </div>
                </Card>
              ) : null}

              {analytics ? (
                <div className="dashboard-grid results-metrics">
                  <Card title="Total Participants"><p className="metric-value">{analytics.totalParticipants}</p></Card>
                  <Card title="Anonymous" accent="cool"><p className="metric-value">{analytics.anonymousParticipants}</p></Card>
                  <Card title="Named" accent="warm"><p className="metric-value">{analytics.namedParticipants}</p></Card>
                  <Card title="Sessions Analyzed"><p className="metric-value">{analytics.gameCorrelation?.sessionsAnalyzed ?? 0}</p></Card>
                  <Card title="Avg Accuracy"><p className="metric-value">{analytics.gameCorrelation?.avgAccuracy ?? '-'}%</p></Card>
                  <Card title="Avg Reaction Time"><p className="metric-value">{analytics.gameCorrelation?.avgReactionTime ?? '-'} ms</p></Card>
                </div>
              ) : null}

              {analytics?.questionAnalytics?.length ? (
                <Card title="Questionnaire Analytics">
                  {analytics.questionAnalytics.map((item) => (
                    <div key={item.questionId} className="research-analytics-block">
                      <strong>{item.questionText}</strong>
                      <div className="chip-row">
                        {Object.entries(item.answerDistribution ?? {}).map(([answer, count]) => (
                          <span key={answer} className="chip">{answer}: {count}</span>
                        ))}
                      </div>
                    </div>
                  ))}
                </Card>
              ) : null}

              {analytics?.enrollmentTrend?.length ? (
                <Card title="Enrollment Trend">
                  <div className="chip-row">
                    {analytics.enrollmentTrend.map((point) => (
                      <span key={point.period} className="chip">{point.period}: {point.enrollments}</span>
                    ))}
                  </div>
                </Card>
              ) : null}

              {cohorts.length > 0 ? (
                <Card title="Saved filters">
                  <div className="research-question-list">
                    {cohorts.map((cohort) => (
                      <label key={cohort.id} className="research-choice">
                        <input
                          type="checkbox"
                          checked={selectedCohortIds.includes(cohort.id)}
                          onChange={(event) => {
                            setSelectedCohortIds((current) => (
                              event.target.checked
                                ? [...current, cohort.id]
                                : current.filter((id) => id !== cohort.id)
                            ));
                          }}
                        />
                        <span>{cohort.name} · {cohort.matchedParticipantCount} participants</span>
                      </label>
                    ))}
                  </div>
                  <Button disabled={submitting} onClick={handleCompareCohorts}>Compare filters</Button>
                </Card>
              ) : null}

              {comparison?.groups?.length ? (
                <Card title="Comparison">
                  {comparison.groups.map((group) => (
                    <div key={group.label} className="research-analytics-block">
                      <strong>{group.label}</strong>
                      <p>{group.participantCount} participants · Avg accuracy {group.gameCorrelation?.avgAccuracy ?? '-'}%</p>
                    </div>
                  ))}
                </Card>
              ) : null}
            </>
          ) : (
            <StatusMessage tone="warning" message="Pick a study first." />
          )}
        </div>
      ) : null}
    </div>
  );
}
