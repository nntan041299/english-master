import { useSelector } from "react-redux";
import { selectUser } from "@/redux/user/selectors";
import Layout from "@/layouts/Layout";

interface StatCardProps {
  label: string;
  value: string | number;
  icon: string;
  accent: string;
  sub?: string;
}

function StatCard({ label, value, icon, accent, sub }: StatCardProps) {
  return (
    <div className="bg-white rounded-2xl p-5 border border-surface-200 flex flex-col gap-3">
      <div
        className={`w-10 h-10 rounded-xl flex items-center justify-center ${accent}`}
      >
        <i className={`pi ${icon} text-base`} />
      </div>
      <div>
        <p
          className="text-2xl font-bold text-surface-900"
          style={{ fontFamily: "var(--font-display)" }}
        >
          {value}
        </p>
        <p
          className="text-sm font-medium text-surface-500 mt-0.5"
          style={{ fontFamily: "var(--font-sans)" }}
        >
          {label}
        </p>
        {sub && (
          <p
            className="text-xs text-surface-400 mt-1"
            style={{ fontFamily: "var(--font-sans)" }}
          >
            {sub}
          </p>
        )}
      </div>
    </div>
  );
}

interface ActivityRowProps {
  label: string;
  done: boolean;
}

function ActivityRow({ label, done }: ActivityRowProps) {
  return (
    <div className="flex items-center gap-3 py-2.5 border-b border-surface-100 last:border-0">
      <span
        className={`w-5 h-5 rounded-full flex items-center justify-center flex-shrink-0 text-xs
        ${done ? "bg-sage-100 text-sage-700" : "bg-surface-100 text-surface-400"}`}
      >
        <i className={`pi ${done ? "pi-check" : "pi-circle"} text-[10px]`} />
      </span>
      <span
        className="text-sm text-surface-600"
        style={{ fontFamily: "var(--font-sans)" }}
      >
        {label}
      </span>
    </div>
  );
}

const STATS: StatCardProps[] = [
  {
    label: "Words learned",
    value: "—",
    icon: "pi-book",
    accent: "bg-sage-100 text-sage-700",
  },
  {
    label: "Mastered",
    value: "—",
    icon: "pi-star",
    accent: "bg-gold-100 text-gold-600",
  },
  {
    label: "Need review",
    value: "—",
    icon: "pi-sync",
    accent: "bg-amber-50 text-amber-600",
  },
  {
    label: "Day streak",
    value: "—",
    icon: "pi-bolt",
    accent: "bg-ink-900/8 text-ink-900",
  },
];

const TODAY_ITEMS = [
  { label: "Complete daily review session", done: false },
  { label: "Practice 10 new words", done: false },
  { label: "Run a multiple-choice exercise", done: false },
];

const Dashboard = () => {
  const { firstName } = useSelector(selectUser);

  const greeting = (() => {
    const h = new Date().getHours();
    if (h < 12) return "Good morning";
    if (h < 18) return "Good afternoon";
    return "Good evening";
  })();

  return (
    <Layout>
      <div className="max-w-5xl mx-auto px-6 py-8 space-y-8">
        {/* Welcome banner */}
        <div className="bg-ink-900 rounded-2xl px-7 py-6 flex items-center justify-between overflow-hidden relative">
          {/* Subtle ambient glow */}
          <div
            className="absolute right-0 top-0 w-64 h-full opacity-20"
            style={{
              background:
                "radial-gradient(ellipse at right, var(--color-gold-500), transparent 70%)",
            }}
          />

          <div className="relative">
            <p
              className="text-sm font-medium text-white/50 mb-1"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              {greeting}
            </p>
            <h1
              className="text-2xl font-bold text-white"
              style={{ fontFamily: "var(--font-display)" }}
            >
              {firstName ? `Welcome back, ${firstName}` : "Welcome back"}
            </h1>
            <p
              className="text-sm text-white/50 mt-1"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              Keep building your vocabulary — every word counts.
            </p>
          </div>

          <div className="relative flex-shrink-0 text-center px-4">
            <p
              className="text-3xl font-bold text-gold-400"
              style={{ fontFamily: "var(--font-display)" }}
            >
              0
            </p>
            <p
              className="text-xs font-medium text-white/40 uppercase tracking-widest mt-0.5"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              Day streak
            </p>
          </div>
        </div>

        {/* Stat cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {STATS.map((s) => (
            <StatCard key={s.label} {...s} />
          ))}
        </div>

        {/* Bottom row */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          {/* Today's tasks */}
          <div className="lg:col-span-2 bg-white rounded-2xl p-5 border border-surface-200">
            <h2
              className="text-sm font-semibold text-surface-700 uppercase tracking-widest mb-4"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              Today&apos;s tasks
            </h2>
            {TODAY_ITEMS.map((item) => (
              <ActivityRow key={item.label} {...item} />
            ))}
          </div>

          {/* Quick actions */}
          <div className="bg-white rounded-2xl p-5 border border-surface-200 flex flex-col gap-3">
            <h2
              className="text-sm font-semibold text-surface-700 uppercase tracking-widest"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              Quick start
            </h2>

            <button
              className="w-full flex items-center gap-3 px-4 py-3 rounded-xl
                         bg-ink-900 text-parchment text-sm font-semibold
                         hover:bg-ink-800 transition-colors duration-150 cursor-pointer border-none"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              <i className="pi pi-play-circle text-base text-gold-400" />
              Start Practice
            </button>

            <button
              className="w-full flex items-center gap-3 px-4 py-3 rounded-xl
                         bg-surface-50 text-surface-700 text-sm font-medium
                         border border-surface-200 hover:bg-surface-100
                         transition-colors duration-150 cursor-pointer"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              <i className="pi pi-plus-circle text-base text-sage-600" />
              Add New Word
            </button>

            <button
              className="w-full flex items-center gap-3 px-4 py-3 rounded-xl
                         bg-surface-50 text-surface-700 text-sm font-medium
                         border border-surface-200 hover:bg-surface-100
                         transition-colors duration-150 cursor-pointer"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              <i className="pi pi-list text-base text-surface-400" />
              Browse Vocabulary
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Dashboard;
