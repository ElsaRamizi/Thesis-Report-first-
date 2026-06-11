import { useAuth } from '../context/useAuth';
import ClinicianResearchPage from './ClinicianResearchPage';
import PatientResearchPage from './PatientResearchPage';

export default function ResearchPage() {
  const { role } = useAuth();

  if (role === 'CLINICIAN') {
    return <ClinicianResearchPage />;
  }

  return <PatientResearchPage />;
}
