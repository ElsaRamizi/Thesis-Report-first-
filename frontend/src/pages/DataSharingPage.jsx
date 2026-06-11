import { useEffect, useState } from 'react';
import PageHero from '../components/layout/PageHero';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import {
  createDoctorConnection,
  fetchDoctorConnections,
  reactivateDoctorConnection,
  revokeDoctorConnection,
} from '../services/doctorConnectionService';

const emptyForm = {
  doctorName: '',
  doctorSurname: '',
  doctorEmail: '',
  institution: '',
  specialization: '',
  shareFullIdentifiable: false,
  shareAnonymizedOnly: true,
  shareSelectedGamesOnly: false,
  shareQuestionnaires: true,
  shareAnalyticsOnly: true,
  selectedGames: 'dual-n-back\nmemory-span',
  useAnonymousSharing: false,
  consentAccepted: false,
};

const formatDate = (value) => (value ? new Date(value).toLocaleString() : '-');

export default function DataSharingPage() {
  const [connections, setConnections] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadConnections = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchDoctorConnections();
      setConnections(data);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Doctor connections could not be loaded.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadConnections();
  }, []);

  const handleSubmit = async () => {
    if (!form.consentAccepted) {
      setError('You must explicitly consent before sharing any data with a doctor.');
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await createDoctorConnection({
        doctorName: form.doctorName,
        doctorSurname: form.doctorSurname,
        doctorEmail: form.doctorEmail,
        institution: form.institution,
        specialization: form.specialization,
        shareFullIdentifiable: form.shareFullIdentifiable,
        shareAnonymizedOnly: form.shareAnonymizedOnly,
        shareSelectedGamesOnly: form.shareSelectedGamesOnly,
        shareQuestionnaires: form.shareQuestionnaires,
        shareAnalyticsOnly: form.shareAnalyticsOnly,
        selectedGames: form.selectedGames.split('\n').map((item) => item.trim()).filter(Boolean),
        useAnonymousSharing: form.useAnonymousSharing,
        consentAccepted: form.consentAccepted,
      });
      setSuccess('Doctor connection created. Cognitive analytics will be shared according to your selected permissions.');
      setForm(emptyForm);
      await loadConnections();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Doctor connection could not be created.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRevoke = async (connectionId) => {
    setSubmitting(true);
    try {
      await revokeDoctorConnection(connectionId);
      setSuccess('Doctor access revoked.');
      await loadConnections();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Revoke failed.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleReactivate = async (connectionId) => {
    setSubmitting(true);
    try {
      await reactivateDoctorConnection(connectionId);
      setSuccess('Doctor connection reactivated.');
      await loadConnections();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Reactivation failed.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="stack-lg">
      <PageHero
        eyebrow="Sharing"
        title="Doctors"
        description="Who can see your task results and what they can see."
      />

      {error ? <StatusMessage tone="error" title="Data sharing error" message={error} /> : null}
      {success ? <StatusMessage tone="neutral" title="Updated" message={success} /> : null}

      <div className="clinician-grid">
        <Card title="Add Doctor" accent="warm">
          <div className="research-form-grid">
            <Input label="Doctor Name *" value={form.doctorName} onChange={(event) => setForm({ ...form, doctorName: event.target.value })} />
            <Input label="Doctor Surname *" value={form.doctorSurname} onChange={(event) => setForm({ ...form, doctorSurname: event.target.value })} />
            <Input label="Doctor Email *" type="email" value={form.doctorEmail} onChange={(event) => setForm({ ...form, doctorEmail: event.target.value })} />
            <Input label="Institution / Hospital" value={form.institution} onChange={(event) => setForm({ ...form, institution: event.target.value })} />
            <Input label="Specialization" value={form.specialization} onChange={(event) => setForm({ ...form, specialization: event.target.value })} />
            <label className="research-choice">
              <input type="checkbox" checked={form.shareFullIdentifiable} onChange={(event) => setForm({ ...form, shareFullIdentifiable: event.target.checked, shareAnonymizedOnly: event.target.checked ? false : form.shareAnonymizedOnly })} />
              <span>Share full identifiable data</span>
            </label>
            <label className="research-choice">
              <input type="checkbox" checked={form.shareAnonymizedOnly} onChange={(event) => setForm({ ...form, shareAnonymizedOnly: event.target.checked, shareFullIdentifiable: event.target.checked ? false : form.shareFullIdentifiable })} />
              <span>Share anonymized data only</span>
            </label>
            <label className="research-choice">
              <input type="checkbox" checked={form.useAnonymousSharing} onChange={(event) => setForm({ ...form, useAnonymousSharing: event.target.checked })} />
              <span>Use anonymous identifier for this doctor</span>
            </label>
            <label className="research-choice">
              <input type="checkbox" checked={form.shareSelectedGamesOnly} onChange={(event) => setForm({ ...form, shareSelectedGamesOnly: event.target.checked })} />
              <span>Share selected games only</span>
            </label>
            {form.shareSelectedGamesOnly ? (
              <label className="field">
                <span>Selected Games (one per line)</span>
                <textarea className="input" rows={3} value={form.selectedGames} onChange={(event) => setForm({ ...form, selectedGames: event.target.value })} />
              </label>
            ) : null}
            <label className="research-choice">
              <input type="checkbox" checked={form.shareQuestionnaires} onChange={(event) => setForm({ ...form, shareQuestionnaires: event.target.checked })} />
              <span>Share questionnaire responses</span>
            </label>
            <label className="research-choice">
              <input type="checkbox" checked={form.shareAnalyticsOnly} onChange={(event) => setForm({ ...form, shareAnalyticsOnly: event.target.checked })} />
              <span>Share analytics summaries</span>
            </label>
            <label className="research-choice">
              <input type="checkbox" checked={form.consentAccepted} onChange={(event) => setForm({ ...form, consentAccepted: event.target.checked })} />
              <span>I agree to share my data with this doctor for their review. *</span>
            </label>
          </div>
          <Button disabled={submitting || !form.consentAccepted} onClick={handleSubmit}>
            {submitting ? 'Saving...' : 'Connect Doctor'}
          </Button>
        </Card>

        <Card title="Connected Clinicians" accent="cool">
          {loading ? <LoadingState label="Loading doctor connections..." /> : null}
          {!loading && connections.length === 0 ? (
            <StatusMessage tone="warning" message="No active doctor connections yet." />
          ) : null}
          <div className="participant-list">
            {connections.map((connection) => (
              <div key={connection.id} className="research-question-item analytics-connection-item">
                <div>
                  <strong>{connection.doctorName} {connection.doctorSurname}</strong>
                  <span>{connection.doctorEmail}</span>
                  <span>{connection.institution || 'No institution provided'}</span>
                  <div className="chip-row">
                    {connection.active ? <span className="badge badge-joined">Active</span> : <span className="badge badge-status">Revoked</span>}
                    {connection.useAnonymousSharing ? <span className="badge badge-anon">{connection.anonymousIdentifier}</span> : null}
                    {connection.shareAnalyticsOnly ? <span className="chip">Analytics</span> : null}
                    {connection.shareQuestionnaires ? <span className="chip">Questionnaires</span> : null}
                  </div>
                  <span>Connected {formatDate(connection.createdAt)}</span>
                </div>
                <div className="actions-row">
                  {connection.active ? (
                    <button type="button" className="button button-secondary" disabled={submitting} onClick={() => handleRevoke(connection.id)}>
                      Revoke
                    </button>
                  ) : (
                    <button type="button" className="button button-secondary" disabled={submitting} onClick={() => handleReactivate(connection.id)}>
                      Reactivate
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
