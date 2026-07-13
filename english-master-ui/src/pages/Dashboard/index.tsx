import { useState } from "react";
import { useSelector } from "react-redux";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { selectUser } from "@/redux/user/selectors";
import Layout from "@/layouts/Layout";
import { createWord, getDashboard } from "@/service/word";

const LEVELS = ["NEW", "LEARNING", "FAMILIAR", "MASTERED"] as const;
type Level = (typeof LEVELS)[number];

const LEVEL_CONFIG: Record<
  Level,
  { label: string; bar: string; dot: string; text: string; bg: string }
> = {
  NEW: {
    label: "New",
    bar: "bg-surface-300",
    dot: "bg-surface-400",
    text: "text-surface-500",
    bg: "bg-surface-50",
  },
  LEARNING: {
    label: "Learning",
    bar: "bg-indigo-400",
    dot: "bg-indigo-400",
    text: "text-indigo-600",
    bg: "bg-indigo-50",
  },
  FAMILIAR: {
    label: "Familiar",
    bar: "bg-gold-400",
    dot: "bg-gold-400",
    text: "text-gold-600",
    bg: "bg-gold-50",
  },
  MASTERED: {
    label: "Mastered",
    bar: "bg-sage-500",
    dot: "bg-sage-500",
    text: "text-sage-700",
    bg: "bg-sage-50",
  },
};

const Dashboard = () => {
  const { firstName } = useSelector(selectUser);
  const [word, setWord] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const queryClient = useQueryClient();
  const { data: stats, isLoading: loadingChart } = useQuery({
    queryKey: ["dashboard"],
    queryFn: getDashboard,
  });

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

  const levelValues: Record<Level, number> = {
    NEW: stats?.newWords ?? 0,
    LEARNING: stats?.learningWords ?? 0,
    FAMILIAR: stats?.familiarWords ?? 0,
    MASTERED: stats?.masteredWords ?? 0,
  };
  const chartTotal = stats ? LEVELS.reduce((s, l) => s + levelValues[l], 0) : 0;

  return (
    <Layout>
      <div className="max-w-5xl mx-auto px-6 py-8 space-y-8">
        {/* Greeting */}
        <div className="flex items-center justify-between">
          <div>
            <p
              className="text-xs font-medium text-surface-400 uppercase tracking-widest mb-0.5"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              {greeting}
            </p>
            <h1
              className="text-2xl font-bold text-surface-900"
              style={{ fontFamily: "var(--font-display)" }}
            >
              {firstName || "Welcome back"}
            </h1>
          </div>
          <div className="flex items-center gap-2 bg-surface-50 border border-surface-200 rounded-xl px-4 py-2.5">
            <i className="pi pi-bolt text-gold-500 text-sm" />
            <span
              className="text-sm font-semibold text-surface-900"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              0
            </span>
            <span
              className="text-xs text-surface-400 font-medium"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              day streak
            </span>
          </div>
        </div>

        {/* Add word — split-pill input */}
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
              placeholder="Add a word — e.g. serendipity"
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

        {/* Stats row */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            {
              label: "Total words",
              value: stats?.totalWords ?? "—",
              icon: "pi-book",
              accent: "bg-surface-100 text-surface-600",
            },
            {
              label: "Mastered",
              value: stats?.masteredWords ?? "—",
              icon: "pi-star",
              accent: "bg-sage-100 text-sage-700",
            },
            {
              label: "Learning",
              value: stats ? stats.learningWords + stats.familiarWords : "—",
              icon: "pi-sync",
              accent: "bg-indigo-50 text-indigo-600",
            },
            {
              label: "New",
              value: stats?.newWords ?? "—",
              icon: "pi-sparkles",
              accent: "bg-gold-100 text-gold-600",
            },
          ].map(({ label, value, icon, accent }) => (
            <div
              key={label}
              className="bg-white rounded-2xl p-5 border border-surface-200 flex flex-col gap-3"
            >
              <div
                className={`w-9 h-9 rounded-xl flex items-center justify-center ${accent}`}
              >
                <i className={`pi ${icon} text-sm`} />
              </div>
              <div>
                <p
                  className="text-2xl font-bold text-surface-900"
                  style={{ fontFamily: "var(--font-display)" }}
                >
                  {loadingChart ? (
                    <span className="text-surface-300">—</span>
                  ) : (
                    value
                  )}
                </p>
                <p
                  className="text-xs font-medium text-surface-500 mt-0.5 uppercase tracking-wider"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {label}
                </p>
              </div>
            </div>
          ))}
        </div>

        {/* Progress chart */}
        <div className="bg-white rounded-2xl border border-surface-200 p-6">
          <div className="flex items-center justify-between mb-5">
            <h2
              className="text-sm font-semibold text-surface-700"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              Vocabulary progress
            </h2>
            {stats && (
              <span
                className="text-xs text-surface-400"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {stats.totalWords} word{stats.totalWords !== 1 ? "s" : ""} total
              </span>
            )}
          </div>

          {loadingChart ? (
            <div className="h-20 flex items-center justify-center">
              <i className="pi pi-spin pi-spinner text-surface-300 text-xl" />
            </div>
          ) : !stats || chartTotal === 0 ? (
            <div className="h-20 flex flex-col items-center justify-center gap-2">
              <p
                className="text-sm text-surface-400"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                Add your first word to see progress here.
              </p>
            </div>
          ) : (
            <>
              {/* Stacked bar */}
              <div className="flex h-6 rounded-lg overflow-hidden gap-px mb-5">
                {LEVELS.map((lvl) => {
                  const pct =
                    chartTotal > 0 ? (levelValues[lvl] / chartTotal) * 100 : 0;
                  if (pct === 0) return null;
                  return (
                    <div
                      key={lvl}
                      className={`${LEVEL_CONFIG[lvl].bar} transition-all duration-500`}
                      style={{ width: `${pct}%` }}
                      title={`${LEVEL_CONFIG[lvl].label}: ${levelValues[lvl]}`}
                    />
                  );
                })}
              </div>

              {/* Legend */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                {LEVELS.map((lvl) => {
                  const cfg = LEVEL_CONFIG[lvl];
                  const count = levelValues[lvl];
                  const pct =
                    chartTotal > 0 ? Math.round((count / chartTotal) * 100) : 0;
                  return (
                    <div key={lvl} className={`rounded-xl px-4 py-3 ${cfg.bg}`}>
                      <div className="flex items-center gap-2 mb-1">
                        <span className={`w-2 h-2 rounded-full ${cfg.dot}`} />
                        <span
                          className={`text-xs font-semibold uppercase tracking-wider ${cfg.text}`}
                          style={{ fontFamily: "var(--font-sans)" }}
                        >
                          {cfg.label}
                        </span>
                      </div>
                      <p
                        className="text-xl font-bold text-surface-900"
                        style={{ fontFamily: "var(--font-display)" }}
                      >
                        {count}
                      </p>
                      <p
                        className="text-xs text-surface-400 mt-0.5"
                        style={{ fontFamily: "var(--font-sans)" }}
                      >
                        {pct}% of total
                      </p>
                    </div>
                  );
                })}
              </div>
            </>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default Dashboard;
