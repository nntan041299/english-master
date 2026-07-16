const LEVEL_COLOR: Record<string, string> = {
  NEW: "bg-sage-100 text-sage-700",
  LEVEL_1: "bg-indigo-50 text-indigo-600",
  LEVEL_2: "bg-indigo-50 text-indigo-600",
  LEVEL_3: "bg-indigo-50 text-indigo-600",
  LEVEL_4: "bg-gold-100 text-gold-600",
  LEVEL_5: "bg-gold-100 text-gold-600",
  MASTERED: "bg-surface-100 text-surface-500",
};

interface LevelBadgeProps {
  level: string;
}

const LevelBadge = ({ level }: LevelBadgeProps) => {
  const color = LEVEL_COLOR[level] ?? "bg-surface-100 text-surface-500";
  return (
    <span
      className={`text-xs font-medium px-2 py-0.5 rounded-full shrink-0 ${color}`}
      style={{ fontFamily: "var(--font-sans)" }}
    >
      {level.replace("_", " ")}
    </span>
  );
};

export default LevelBadge;
