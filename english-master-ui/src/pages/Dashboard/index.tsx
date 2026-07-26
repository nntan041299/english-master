import { useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { selectUser } from "@/redux/user/selectors";
import Layout from "@/layouts/Layout";
import { createWord, getDashboard } from "@/service/word";
import { usePracticeStats } from "@/hook/usePractice";
import type { PracticeStatBucket } from "@/service/practice";

/* ── Progress donut — correct vs incorrect practice answers for a period ── */
function ProgressDonut({ bucket }: { bucket: PracticeStatBucket | undefined }) {
  const size = 96;
  const strokeWidth = 10;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;

  const total = bucket?.total ?? 0;
  const correct = bucket?.correct ?? 0;
  const correctPct = total > 0 ? correct / total : 0;
  const correctLen = circumference * correctPct;
  const accuracy = total > 0 ? Math.round(correctPct * 100) : null;

  return (
    <div className="relative shrink-0" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="var(--color-surface-100)"
          strokeWidth={strokeWidth}
        />
        {total > 0 && (
          <>
            <circle
              cx={size / 2}
              cy={size / 2}
              r={radius}
              fill="none"
              stroke="var(--color-error-400)"
              strokeWidth={strokeWidth}
              strokeDasharray={circumference}
              strokeLinecap="round"
            />
            <circle
              cx={size / 2}
              cy={size / 2}
              r={radius}
              fill="none"
              stroke="var(--color-sage-500)"
              strokeWidth={strokeWidth}
              strokeDasharray={`${correctLen} ${circumference}`}
              strokeLinecap="round"
              className="transition-all duration-500"
            />
          </>
        )}
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span
          className="text-xl font-bold text-surface-900"
          style={{ fontFamily: "var(--font-display)" }}
        >
          {accuracy === null ? "—" : `${accuracy}%`}
        </span>
        <span
          className="text-[10px] text-surface-400 font-medium"
          style={{ fontFamily: "var(--font-sans)" }}
        >
          {total} done
        </span>
      </div>
    </div>
  );
}

const STAT_CARDS: {
  key: "totalWords" | "masteredWords" | "learning" | "newWords";
  label: string;
  icon: string;
  accent: string;
}[] = [
  {
    key: "totalWords",
    label: "Total words",
    icon: "pi-book",
    accent: "bg-surface-300",
  },
  {
    key: "masteredWords",
    label: "Mastered",
    icon: "pi-star-fill",
    accent: "bg-sage-500",
  },
  {
    key: "learning",
    label: "Learning",
    icon: "pi-sync",
    accent: "bg-indigo-400",
  },
  { key: "newWords", label: "New", icon: "pi-sparkles", accent: "bg-gold-400" },
];

const Dashboard = () => {
  const navigate = useNavigate();
  const { firstName } = useSelector(selectUser);
  const [word, setWord] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const queryClient = useQueryClient();
  const { data: stats, isLoading: loadingChart } = useQuery({
    queryKey: ["dashboard"],
    queryFn: getDashboard,
  });
  const { data: practiceStats, isLoading: loadingPracticeStats } =
    usePracticeStats();

  const greeting = (() => {
    const h = new Date().getHours();
    if (h < 12) return "Good morning";
    if (h < 18) return "Good afternoon";
    return "Good evening";
  })();

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = word.trim();
    if (!trimmed) return;
    setSubmitting(true);
    setSubmitError("");
    try {
      await createWord(trimmed);
      setWord("");
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
    } catch {
      setSubmitError("Couldn't add that word. Try again.");
    } finally {
      setSubmitting(false);
    }
  };

  const statValues: Record<
    (typeof STAT_CARDS)[number]["key"],
    number | undefined
  > = {
    totalWords: stats?.totalWords,
    masteredWords: stats?.masteredWords,
    learning: stats ? stats.learningWords + stats.familiarWords : undefined,
    newWords: stats?.newWords,
  };

  return (
    <Layout>
      <div className="max-w-5xl mx-auto px-6 py-8 space-y-8">
        {/* ── Greeting ── */}
        <div className="rise-in flex items-center justify-between gap-4">
          <div>
            <p
              className="text-[11px] font-semibold text-gold-600 uppercase tracking-[0.2em] mb-1"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              {greeting}
            </p>
            <h1
              className="text-3xl text-ink-900 leading-none"
              style={{ fontFamily: "var(--font-display)", fontWeight: 700 }}
            >
              {firstName || "Welcome back"}
            </h1>
          </div>
          <div className="shrink-0 flex items-center gap-2 bg-white border border-surface-200 rounded-xl px-4 py-2.5">
            <i className="pi pi-check-circle text-sage-600 text-sm" />
            <span
              className="text-sm font-semibold text-surface-900"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              {stats?.practicesDone ?? 0}
            </span>
            <span
              className="text-xs text-surface-400 font-medium"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              practices done
            </span>
          </div>
        </div>

        {/* ── Quick capture — add a word, jump to vocabulary ── */}
        <div className="rise-in" style={{ animationDelay: "40ms" }}>
          <div className="flex items-center justify-between mb-3">
            <h2
              className="text-xs font-semibold text-surface-500 uppercase tracking-[0.15em]"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              Add a word
            </h2>
            <button
              onClick={() => navigate("/vocabulary")}
              className="text-xs font-semibold text-surface-500 hover:text-ink-900 transition-colors cursor-pointer bg-transparent border-none flex items-center gap-1"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              View vocabulary
              <i className="pi pi-arrow-right text-[10px]" />
            </button>
          </div>
          <form onSubmit={handleAdd}>
            <div
              className="flex items-stretch bg-white border-2 border-surface-200 rounded-2xl
                          overflow-hidden focus-within:border-ink-900 transition-colors duration-200"
            >
              <div className="flex items-center pl-5 pr-2 shrink-0">
                <i className="pi pi-plus-circle text-surface-300 text-lg" />
              </div>
              <input
                type="text"
                value={word}
                onChange={(e) => {
                  setWord(e.target.value);
                  setSubmitError("");
                }}
                placeholder="e.g. serendipity"
                autoComplete="off"
                className="flex-1 py-4 text-lg text-surface-900 placeholder:text-surface-300
                           bg-transparent focus:outline-none"
                style={{ fontFamily: "var(--font-display)" }}
              />
              <div className="p-2 shrink-0">
                <button
                  type="submit"
                  disabled={submitting || !word.trim()}
                  className="h-full px-3 sm:px-5 rounded-xl bg-ink-900 text-parchment text-sm font-semibold
                             hover:bg-ink-800 disabled:opacity-30 disabled:cursor-not-allowed
                             transition-colors duration-150 cursor-pointer border-none"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {submitting ? (
                    <i className="pi pi-spin pi-spinner text-sm" />
                  ) : (
                    <i className="pi pi-plus text-sm" />
                  )}
                </button>
              </div>
            </div>
            {submitError && (
              <p
                className="text-xs text-red-500 mt-2 pl-1"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {submitError}
              </p>
            )}
          </form>
        </div>

        {/* ── Vocabulary — word counts by level ── */}
        <div className="rise-in" style={{ animationDelay: "80ms" }}>
          <h2
            className="text-xs font-semibold text-surface-500 uppercase tracking-[0.15em] mb-3"
            style={{ fontFamily: "var(--font-sans)" }}
          >
            Vocabulary
          </h2>

          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {STAT_CARDS.map(({ key, label, icon, accent }) => {
              const value = statValues[key];
              return (
                <div
                  key={key}
                  className="relative bg-white rounded-2xl border border-surface-200 pl-5 pr-4 py-4 overflow-hidden"
                >
                  <span
                    className={`absolute inset-y-0 left-0 w-[3px] ${accent}`}
                  />
                  <p
                    className="text-3xl text-surface-900 leading-none"
                    style={{
                      fontFamily: "var(--font-display)",
                      fontWeight: 700,
                    }}
                  >
                    {loadingChart ? (
                      <span className="text-surface-300">—</span>
                    ) : (
                      value
                    )}
                  </p>
                  <p
                    className="text-xs font-medium text-surface-500 mt-2 uppercase tracking-wider flex items-center gap-1.5"
                    style={{ fontFamily: "var(--font-sans)" }}
                  >
                    <i className={`pi ${icon} text-[10px] text-surface-400`} />
                    {label}
                  </p>
                </div>
              );
            })}
          </div>
        </div>

        {/* ── Your progress — practice accuracy, today / this week / this month ── */}
        <div className="rise-in" style={{ animationDelay: "120ms" }}>
          <h2
            className="text-xs font-semibold text-surface-500 uppercase tracking-[0.15em] mb-3"
            style={{ fontFamily: "var(--font-sans)" }}
          >
            Your progress
          </h2>

          <div className="bg-white rounded-2xl border border-surface-200 p-6">
            {loadingPracticeStats ? (
              <div className="h-32 flex items-center justify-center">
                <i className="pi pi-spin pi-spinner text-surface-300 text-xl" />
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
                {(
                  [
                    ["Today", practiceStats?.today],
                    ["This week", practiceStats?.thisWeek],
                    ["This month", practiceStats?.thisMonth],
                  ] as const
                ).map(([label, bucket]) => (
                  <div
                    key={label}
                    className="flex flex-col items-center gap-3 sm:border-l sm:first:border-l-0 sm:border-surface-100 sm:pl-6 sm:first:pl-0"
                  >
                    <ProgressDonut bucket={bucket} />
                    <span
                      className="text-xs font-semibold text-surface-600 uppercase tracking-wider"
                      style={{ fontFamily: "var(--font-sans)" }}
                    >
                      {label}
                    </span>
                    <div className="flex items-center gap-3">
                      <span className="flex items-center gap-1.5 text-xs text-surface-500">
                        <span className="w-2 h-2 rounded-full bg-sage-500" />
                        {bucket?.correct ?? 0} correct
                      </span>
                      <span className="flex items-center gap-1.5 text-xs text-surface-500">
                        <span className="w-2 h-2 rounded-full bg-error-400" />
                        {bucket?.incorrect ?? 0} missed
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Dashboard;
