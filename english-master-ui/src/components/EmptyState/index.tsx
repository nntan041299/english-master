interface EmptyStateProps {
  icon: string;
  title: string;
  description: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

const EmptyState = ({ icon, title, description, action }: EmptyStateProps) => (
  <div className="flex flex-col items-center justify-center gap-4 text-center h-full px-6">
    <div className="w-14 h-14 rounded-full bg-surface-100 flex items-center justify-center">
      <i className={`pi ${icon} text-2xl text-surface-400`} />
    </div>
    <div className="flex flex-col gap-1">
      <h2
        className="text-xl font-bold text-surface-900"
        style={{ fontFamily: "var(--font-display)" }}
      >
        {title}
      </h2>
      <p
        className="text-sm text-surface-500 max-w-xs"
        style={{ fontFamily: "var(--font-sans)" }}
      >
        {description}
      </p>
    </div>
    {action && (
      <button
        onClick={action.onClick}
        className="px-5 py-2.5 rounded-lg bg-ink-900 text-parchment text-sm font-semibold
                   cursor-pointer border-none hover:bg-ink-800 transition-colors"
        style={{ fontFamily: "var(--font-sans)" }}
      >
        {action.label}
      </button>
    )}
  </div>
);

export default EmptyState;
