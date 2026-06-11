import { useEffect, useState } from 'react';
import PageHero from '../components/layout/PageHero';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import { fetchOwnProfile, updateOwnProfile } from '../services/profileService';

export default function ParticipantProfilePage() {
  const [profile, setProfile] = useState(null);
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    fetchOwnProfile()
      .then((data) => {
        if (!active) {
          return;
        }
        setProfile(data);
        setDateOfBirth(data.dateOfBirth ?? '');
        setNotes(data.notes ?? '');
        setLoading(false);
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Profile could not be loaded.');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setMessage('');

    try {
      const updated = await updateOwnProfile({
        dateOfBirth: dateOfBirth || null,
        notes,
      });
      setProfile(updated);
      setMessage('Profile saved.');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Profile could not be saved.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <LoadingState label="Loading profile..." />;
  }

  return (
    <div className="stack-lg">
      <PageHero
        eyebrow="Participant profile"
        title="Your profile"
        description="Optional demographic details and notes for your clinician."
      />

      {error ? <StatusMessage tone="error" title="Profile error" message={error} /> : null}
      {message ? <StatusMessage tone="success" message={message} /> : null}

      <Card title="Profile details" accent="cool">
        <div className="form-grid">
          <label className="form-field">
            <span>Email</span>
            <input value={profile?.email ?? ''} disabled />
          </label>
          <label className="form-field">
            <span>Date of birth</span>
            <input
              type="date"
              value={dateOfBirth}
              onChange={(event) => setDateOfBirth(event.target.value)}
            />
          </label>
          <label className="form-field">
            <span>Age</span>
            <input value={profile?.age ?? '—'} disabled />
          </label>
          <label className="form-field form-field-wide">
            <span>Notes</span>
            <textarea
              rows={4}
              value={notes}
              onChange={(event) => setNotes(event.target.value)}
              placeholder="Medication, concerns, or context for your clinician"
            />
          </label>
          {profile?.assignedClinicianEmail ? (
            <label className="form-field">
              <span>Assigned clinician</span>
              <input value={profile.assignedClinicianEmail} disabled />
            </label>
          ) : null}
        </div>
        <div className="actions-row">
          <Button onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save profile'}
          </Button>
        </div>
      </Card>
    </div>
  );
}
