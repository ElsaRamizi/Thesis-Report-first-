export const CLINICIAN_RESEARCH_TABS = [
  { id: 'studies', label: 'My Studies' },
  { id: 'create', label: 'Create Study' },
  { id: 'analytics', label: 'Analytics & Cohorts' },
];

export const PARTICIPANT_RESEARCH_TABS = [
  { id: 'discover', label: 'Discover Studies' },
  { id: 'joined', label: 'My Studies' },
];

export const EMPTY_STUDY_FORM = {
  title: '',
  description: '',
  instructions: '',
  participationRequirements: '',
  estimatedDuration: '',
  researchType: 'ONLINE_TESTING',
  rewarded: false,
  rewardDetails: '',
  anonymousFriendly: true,
  consentText: '',
};

export const EMPTY_QUESTION = {
  questionText: '',
  questionType: 'SHORT_TEXT',
  optionsText: '',
  required: true,
  sortOrder: 2,
};

export const EMPTY_FILTER = {
  questionId: '',
  questionKey: '',
  operator: 'EQUALS',
  value: '',
  minAge: '',
  maxAge: '',
  valuesText: '',
};

export const DATA_SHARING_CONSENT =
  'I consent to my anonymized or identified responses being used for research analytics.';
