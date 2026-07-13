interface CircleProgressProps {
  current: number;
  total: number;
}

const CircleProgress = ({ current, total }: CircleProgressProps) => {
  const r = 20;
  const circ = 2 * Math.PI * r;
  const dash = circ * (total > 0 ? current / total : 0);

  return (
    <div className="relative flex items-center justify-center w-14 h-14">
      <svg width="56" height="56" viewBox="0 0 56 56" className="-rotate-90">
        <circle
          cx="28"
          cy="28"
          r={r}
          fill="none"
          stroke="#E5E7EB"
          strokeWidth="3"
        />
        <circle
          cx="28"
          cy="28"
          r={r}
          fill="none"
          stroke="#C9934A"
          strokeWidth="3"
          strokeLinecap="round"
          strokeDasharray={`${dash} ${circ}`}
          style={{ transition: "stroke-dasharray 0.4s ease" }}
        />
      </svg>
      <span
        className="absolute text-xs font-semibold text-surface-700"
        style={{ fontFamily: "var(--font-sans)" }}
      >
        {current}
        <span className="text-surface-400">/{total}</span>
      </span>
    </div>
  );
};

export default CircleProgress;
