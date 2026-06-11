export default function TabBar({ tabs, activeTab, onChange }) {
  return (
    <div className="research-tab-row" role="tablist">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          type="button"
          role="tab"
          aria-selected={activeTab === tab.id}
          className={`research-tab ${activeTab === tab.id ? 'research-tab-active' : ''}`}
          onClick={() => onChange(tab.id)}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}
