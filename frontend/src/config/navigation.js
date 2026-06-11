// routes + nav labels
export const NAV_BY_ROLE = {
  USER: [
    { to: '/user/dashboard', label: 'Dashboard', shortLabel: 'Home' },
    { to: '/research', label: 'Research', shortLabel: 'Research' },
    { to: '/data-sharing', label: 'Data Sharing', shortLabel: 'Sharing' },
    { to: '/profile', label: 'Profile', shortLabel: 'Profile' },
    { to: '/tasks', label: 'Tasks', shortLabel: 'Tasks' },
    { to: '/sessions', label: 'History', shortLabel: 'History' },
    { to: '/results/latest', label: 'Results', shortLabel: 'Results' },
  ],
  CLINICIAN: [
    { to: '/clinician/dashboard', label: 'Dashboard', shortLabel: 'Home' },
    { to: '/clinician/group', label: 'Group Trends', shortLabel: 'Group' },
    { to: '/clinician/analytics', label: 'Analytics', shortLabel: 'Analytics' },
    { to: '/research', label: 'My Research', shortLabel: 'Research' },
    { to: '/user/dashboard', label: 'Participant View', shortLabel: 'Participant' },
    { to: '/tasks', label: 'Tasks', shortLabel: 'Tasks' },
    { to: '/sessions', label: 'History', shortLabel: 'History' },
    { to: '/results/latest', label: 'Results', shortLabel: 'Results' },
  ],
};

export const PAGE_TITLES = {
  '/user/dashboard': 'Dashboard',
  '/research': 'Research',
  '/data-sharing': 'Doctor Connections',
  '/profile': 'Profile',
  '/clinician/dashboard': 'Dashboard',
  '/clinician/analytics': 'Analytics',
  '/tasks': 'Tasks',
  '/sessions': 'Session History',
  '/results/latest': 'Results',
};

export const RESEARCH_TITLE_BY_ROLE = {
  CLINICIAN: 'My Research',
  USER: 'Participate in Research',
};
