import { useEffect, useState } from 'react';

import { Link, useParams } from 'react-router-dom';

import PageHero from '../components/layout/PageHero';

import Card from '../components/ui/Card';

import Button from '../components/ui/Button';

import LoadingState from '../components/ui/LoadingState';

import StatusMessage from '../components/ui/StatusMessage';

import {

  addParticipantAnnotation,

  fetchClinicianDirectory,

  fetchParticipantAnnotations,

  fetchParticipantProfile,

  fetchParticipantSessions,

  updateParticipantProfile,

} from '../services/clinicianService';



const formatSessionLabel = (session) => {

  if (!session) {

    return '';

  }

  return `${session.taskTitle} · ${new Date(session.startTime).toLocaleString()}`;

};



export default function ClinicianParticipantPage() {

  const { participantId } = useParams();

  const [profile, setProfile] = useState(null);

  const [annotations, setAnnotations] = useState([]);

  const [sessions, setSessions] = useState([]);

  const [clinicians, setClinicians] = useState([]);

  const [dateOfBirth, setDateOfBirth] = useState('');

  const [notes, setNotes] = useState('');

  const [assignedClinicianId, setAssignedClinicianId] = useState('');

  const [annotationText, setAnnotationText] = useState('');

  const [annotationSessionId, setAnnotationSessionId] = useState('');

  const [loading, setLoading] = useState(true);

  const [saving, setSaving] = useState(false);

  const [error, setError] = useState('');



  useEffect(() => {

    let active = true;



    Promise.all([

      fetchParticipantProfile(participantId),

      fetchParticipantAnnotations(participantId),

      fetchClinicianDirectory(),

      fetchParticipantSessions(participantId),

    ])

      .then(([profileData, annotationData, clinicianData, sessionData]) => {

        if (!active) {

          return;

        }

        setProfile(profileData);

        setAnnotations(annotationData);

        setClinicians(clinicianData);

        setSessions(sessionData);

        setDateOfBirth(profileData.dateOfBirth ?? '');

        setNotes(profileData.notes ?? '');

        setAssignedClinicianId(profileData.assignedClinicianId ?? '');

        setLoading(false);

      })

      .catch((requestError) => {

        if (active) {

          setError(requestError.response?.data?.message ?? 'Participant data could not be loaded.');

          setLoading(false);

        }

      });



    return () => {

      active = false;

    };

  }, [participantId]);



  const handleSaveProfile = async () => {

    setSaving(true);

    setError('');



    try {

      const updated = await updateParticipantProfile(participantId, {

        dateOfBirth: dateOfBirth || null,

        notes,

        assignedClinicianId: assignedClinicianId ? Number(assignedClinicianId) : null,

        clearAssignedClinician: !assignedClinicianId && Boolean(profile?.assignedClinicianId),

      });

      setProfile(updated);

    } catch (requestError) {

      setError(requestError.response?.data?.message ?? 'Profile could not be saved.');

    } finally {

      setSaving(false);

    }

  };



  const handleAddAnnotation = async () => {

    if (!annotationText.trim()) {

      return;

    }



    try {

      const created = await addParticipantAnnotation(participantId, {

        content: annotationText.trim(),

        sessionId: annotationSessionId ? Number(annotationSessionId) : null,

      });

      setAnnotations((current) => [created, ...current]);

      setAnnotationText('');

      setAnnotationSessionId('');

    } catch (requestError) {

      setError(requestError.response?.data?.message ?? 'Annotation could not be saved.');

    }

  };



  if (loading) {

    return <LoadingState label="Loading participant..." />;

  }



  return (

    <div className="stack-lg">

      <PageHero

        eyebrow="Participant"

        title={profile?.email ?? 'Participant'}

        description="DOB, notes, assign clinician."

      />



      {error ? <StatusMessage tone="error" title="Participant error" message={error} /> : null}



      <div className="actions-row">

        <Link to={`/clinician/participants/${participantId}/report`} className="button-link">Automated report</Link>

        <Link to={`/clinician/participants/${participantId}/compare`} className="button-link button-link-primary">Compare sessions</Link>

        <Link to="/clinician/dashboard" className="inline-link">Back to dashboard</Link>

      </div>



      <Card title="Participant profile" accent="cool">

        <div className="form-grid">

          <label className="form-field">

            <span>Date of birth</span>

            <input type="date" value={dateOfBirth} onChange={(event) => setDateOfBirth(event.target.value)} />

          </label>

          <label className="form-field">

            <span>Age</span>

            <input value={profile?.age ?? '—'} disabled />

          </label>

          <label className="form-field">

            <span>Assigned clinician</span>

            <select value={assignedClinicianId} onChange={(event) => setAssignedClinicianId(event.target.value)}>

              <option value="">Unassigned</option>

              {clinicians.map((clinician) => (

                <option key={clinician.participantId} value={clinician.participantId}>

                  {clinician.email}

                </option>

              ))}

            </select>

          </label>

          <label className="form-field form-field-wide">

            <span>Clinical notes</span>

            <textarea rows={4} value={notes} onChange={(event) => setNotes(event.target.value)} />

          </label>

        </div>

        <div className="actions-row">

          <Button onClick={handleSaveProfile} disabled={saving}>

            {saving ? 'Saving...' : 'Save profile'}

          </Button>

        </div>

      </Card>



      <Card title="Annotations">

        <div className="form-grid">

          <label className="form-field">

            <span>Related session (optional)</span>

            <select value={annotationSessionId} onChange={(event) => setAnnotationSessionId(event.target.value)}>

              <option value="">General participant note</option>

              {sessions.map((session) => (

                <option key={session.sessionId} value={session.sessionId}>

                  {formatSessionLabel(session)}

                </option>

              ))}

            </select>

          </label>

          <label className="form-field form-field-wide">

            <span>Add annotation</span>

            <textarea

              rows={3}

              value={annotationText}

              onChange={(event) => setAnnotationText(event.target.value)}

              placeholder="Clinical observation or session note"

            />

          </label>

        </div>

        <div className="actions-row">

          <Button onClick={handleAddAnnotation}>Save annotation</Button>

        </div>

        <div className="annotation-list">

          {annotations.length === 0 ? <p>No annotations yet.</p> : null}

          {annotations.map((annotation) => (

            <div key={annotation.id} className="annotation-item">

              <strong>{annotation.clinicianEmail}</strong>

              <span>{new Date(annotation.createdAt).toLocaleString()}</span>

              {annotation.sessionId ? (

                <span className="chip">

                  Session {annotation.sessionId}

                </span>

              ) : (

                <span className="chip">General note</span>

              )}

              <p>{annotation.content}</p>

            </div>

          ))}

        </div>

      </Card>

    </div>

  );

}


