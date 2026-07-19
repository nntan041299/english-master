import { useEffect, useMemo, useState } from "react";
import Layout from "@/layouts/Layout";
import EmptyState from "@/components/EmptyState";
import { useGenerateChallenge, useSubmitWriting } from "@/hook/useWriting";
import type {
  WritingChallenge,
  WritingFeedback,
  WritingIssue,
  WritingIssueType,
  WritingLevel,
} from "@/service/writing";

const LEVELS: { value: WritingLevel; label: string }[] = [
  { value: "BEGINNER", label: "Beginner" },
  { value: "INTERMEDIATE", label: "Intermediate" },
  { value: "ADVANCED", label: "Advanced" },
];

type Family = "error" | "gold" | "sage";

function familyOf(type: WritingIssueType): Family {
  switch (type) {
    case "GRAMMAR":
    case "SPELLING":
    case "PUNCTUATION":
    case "WORD_ORDER":
      return "error";
    case "VOCABULARY":
      return "gold";
    default:
      return "sage";
  }
}

const FAMILY_STYLES: Record<
  Family,
  { mark: string; markActive: string; badge: string; dot: string }
> = {
  error: {
    mark: "bg-error-50 border-b-2 border-error-400 text-error-600",
    markActive: "bg-error-100 ring-2 ring-error-400",
    badge: "bg-error-50 text-error-600",
    dot: "bg-error-400",
  },
  gold: {
    mark: "bg-gold-100 border-b-2 border-gold-500 text-gold-600",
    markActive: "bg-gold-100 ring-2 ring-gold-500",
    badge: "bg-gold-100 text-gold-600",
    dot: "bg-gold-500",
  },
  sage: {
    mark: "bg-sage-50 border-b-2 border-sage-500 text-sage-700",
    markActive: "bg-sage-100 ring-2 ring-sage-500",
    badge: "bg-sage-100 text-sage-700",
    dot: "bg-sage-500",
  },
};

const LEGEND: { family: Family; label: string }[] = [
  { family: "error", label: "Grammar / spelling" },
  { family: "gold", label: "Word choice" },
  { family: "sage", label: "Style / clarity" },
];

interface Segment {
  text: string;
  issueIndex: number | null;
}

/**
 * Splits the user's text into plain and highlighted segments by locating each issue's exact
 * `original` substring (first non-overlapping occurrence). Issues whose original can't be found
 * are skipped here but still shown in the list below.
 */
function buildSegments(text: string, issues: WritingIssue[]): Segment[] {
  const occupied = new Array(text.length).fill(false);
  const matches: { start: number; end: number; issueIndex: number }[] = [];

  issues.forEach((issue, issueIndex) => {
    const orig = issue.original;
    if (!orig) return;
    let from = 0;
    while (from <= text.length - orig.length) {
      const idx = text.indexOf(orig, from);
      if (idx === -1) break;
      let free = true;
      for (let i = idx; i < idx + orig.length; i++) {
        if (occupied[i]) {
          free = false;
          break;
        }
      }
      if (free) {
        for (let i = idx; i < idx + orig.length; i++) occupied[i] = true;
        matches.push({ start: idx, end: idx + orig.length, issueIndex });
        break;
      }
      from = idx + 1;
    }
  });

  matches.sort((a, b) => a.start - b.start);

  const segments: Segment[] = [];
  let cursor = 0;
  for (const m of matches) {
    if (m.start > cursor)
      segments.push({ text: text.slice(cursor, m.start), issueIndex: null });
    segments.push({
      text: text.slice(m.start, m.end),
      issueIndex: m.issueIndex,
    });
    cursor = m.end;
  }
  if (cursor < text.length)
    segments.push({ text: text.slice(cursor), issueIndex: null });
  return segments;
}

function scoreColor(score: number): string {
  if (score >= 80) return "text-sage-600";
  if (score >= 50) return "text-gold-500";
  return "text-error-500";
}

export default function Writing() {
  const [level, setLevel] = useState<WritingLevel>("INTERMEDIATE");
  const [challenge, setChallenge] = useState<WritingChallenge | null>(null);
  const [text, setText] = useState("");
  const [submittedText, setSubmittedText] = useState("");
  const [feedback, setFeedback] = useState<WritingFeedback | null>(null);
  const [activeIssue, setActiveIssue] = useState<number | null>(null);

  const generate = useGenerateChallenge();
  const submit = useSubmitWriting();

  const loadChallenge = (lvl: WritingLevel) => {
    setFeedback(null);
    setText("");
    setActiveIssue(null);
    generate.mutate(lvl, { onSuccess: (c) => setChallenge(c) });
  };

  // Generate the first challenge on mount. State is already at its initial
  // values here, so we only kick off the mutation (no synchronous resets).
  useEffect(() => {
    generate.mutate(level, { onSuccess: (c) => setChallenge(c) });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleLevel = (lvl: WritingLevel) => {
    if (lvl === level && challenge) return;
    setLevel(lvl);
    loadChallenge(lvl);
  };

  const handleSubmit = () => {
    if (!challenge || !text.trim()) return;
    const trimmed = text.trim();
    submit.mutate(
      { challengeId: challenge.id, text: trimmed },
      {
        onSuccess: (f) => {
          setSubmittedText(trimmed);
          setFeedback(f);
        },
      },
    );
  };

  const wordCount = useMemo(
    () => text.trim().split(/\s+/).filter(Boolean).length,
    [text],
  );

  const segments = useMemo(
    () => (feedback ? buildSegments(submittedText, feedback.issues) : []),
    [feedback, submittedText],
  );

  return (
    <Layout>
      <div className="h-full overflow-y-auto">
        <div className="max-w-3xl mx-auto px-4 py-6 flex flex-col gap-6">
          {/* Header */}
          <div className="flex items-center justify-between flex-wrap gap-3">
            <div>
              <h1
                className="text-2xl font-bold text-ink-900"
                style={{ fontFamily: "var(--font-display)" }}
              >
                Writing
              </h1>
              <p
                className="text-sm text-surface-500"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                Respond to an AI prompt, then get instant feedback on your
                writing.
              </p>
            </div>
            <div className="flex items-center gap-1 bg-surface-100 rounded-lg p-1">
              {LEVELS.map((l) => (
                <button
                  key={l.value}
                  onClick={() => handleLevel(l.value)}
                  disabled={generate.isPending || submit.isPending}
                  className={`px-3 py-1.5 rounded-md text-xs font-semibold transition-colors cursor-pointer disabled:cursor-not-allowed disabled:opacity-60 ${
                    level === l.value
                      ? "bg-white text-ink-900 shadow-sm"
                      : "text-surface-500 hover:text-surface-700"
                  }`}
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {l.label}
                </button>
              ))}
            </div>
          </div>

          {/* Challenge card */}
          <div
            className="bg-white rounded-2xl border border-surface-200 px-6 py-5"
            style={{ boxShadow: "0 2px 20px 0 rgba(26,31,46,0.06)" }}
          >
            <div className="flex items-center justify-between gap-3 mb-2">
              <span
                className="text-xs font-semibold uppercase tracking-widest text-gold-600"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                Your challenge
              </span>
              <button
                onClick={() => loadChallenge(level)}
                disabled={generate.isPending || submit.isPending}
                className="text-xs font-medium text-surface-500 hover:text-ink-900 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1.5"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                <i
                  className={`pi pi-refresh text-xs ${generate.isPending ? "pi-spin" : ""}`}
                />
                New challenge
              </button>
            </div>

            {generate.isPending ? (
              <div className="flex items-center gap-2 py-4 text-surface-400">
                <i className="pi pi-spin pi-spinner" />
                <span
                  className="text-sm"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Generating a challenge…
                </span>
              </div>
            ) : generate.isError ? (
              <EmptyState
                icon="pi-exclamation-triangle"
                title="Couldn't generate a challenge"
                description="Something went wrong reaching the AI. Please try again."
                action={{ label: "Retry", onClick: () => loadChallenge(level) }}
              />
            ) : challenge ? (
              <>
                <h2
                  className="text-xl font-bold text-ink-900 mb-1.5"
                  style={{ fontFamily: "var(--font-display)" }}
                >
                  {challenge.title}
                </h2>
                <p
                  className="text-sm text-surface-700 leading-relaxed"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {challenge.prompt}
                </p>
              </>
            ) : null}
          </div>

          {/* Writing area (hidden once feedback is shown) */}
          {!feedback && challenge && (
            <div className="flex flex-col gap-3">
              <textarea
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Start writing your response here…"
                rows={10}
                disabled={submit.isPending}
                className="w-full rounded-2xl border border-surface-200 bg-white px-5 py-4 text-sm text-ink-900 leading-relaxed resize-y outline-none focus:border-sage-500 focus:ring-2 focus:ring-sage-500/20 transition-colors disabled:opacity-60"
                style={{ fontFamily: "var(--font-sans)" }}
              />
              <div className="flex items-center justify-between">
                <span
                  className="text-xs text-surface-400"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {wordCount} {wordCount === 1 ? "word" : "words"}
                </span>
                <button
                  onClick={handleSubmit}
                  disabled={!text.trim() || submit.isPending}
                  className="px-5 py-2.5 rounded-lg bg-ink-900 text-parchment text-sm font-semibold cursor-pointer border-none hover:bg-ink-800 transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-2"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {submit.isPending ? (
                    <>
                      <i className="pi pi-spin pi-spinner text-xs" />
                      Checking…
                    </>
                  ) : (
                    <>
                      <i className="pi pi-check text-xs" />
                      Get feedback
                    </>
                  )}
                </button>
              </div>
              {submit.isError && (
                <p
                  className="text-xs text-error-500"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Couldn’t get feedback right now. Please try again.
                </p>
              )}
            </div>
          )}

          {/* Feedback */}
          {feedback && (
            <div className="flex flex-col gap-5">
              {/* Score + overall */}
              <div
                className="bg-white rounded-2xl border border-surface-200 px-6 py-5 flex flex-col sm:flex-row sm:items-center gap-5"
                style={{ boxShadow: "0 2px 20px 0 rgba(26,31,46,0.06)" }}
              >
                {feedback.score !== null && (
                  <div className="flex flex-col items-center justify-center shrink-0">
                    <span
                      className={`text-4xl font-bold ${scoreColor(feedback.score)}`}
                      style={{ fontFamily: "var(--font-sans)" }}
                    >
                      {feedback.score}
                    </span>
                    <span
                      className="text-xs text-surface-400"
                      style={{ fontFamily: "var(--font-sans)" }}
                    >
                      out of 100
                    </span>
                  </div>
                )}
                <p
                  className="text-sm text-surface-700 leading-relaxed"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {feedback.overallFeedback}
                </p>
              </div>

              {/* Highlighted text */}
              <div
                className="bg-white rounded-2xl border border-surface-200 px-6 py-5"
                style={{ boxShadow: "0 2px 20px 0 rgba(26,31,46,0.06)" }}
              >
                <div className="flex items-center justify-between flex-wrap gap-2 mb-3">
                  <span
                    className="text-xs font-semibold uppercase tracking-widest text-surface-500"
                    style={{ fontFamily: "var(--font-sans)" }}
                  >
                    Your writing
                  </span>
                  <div className="flex items-center gap-3">
                    {LEGEND.map((l) => (
                      <span
                        key={l.family}
                        className="flex items-center gap-1.5 text-xs text-surface-500"
                        style={{ fontFamily: "var(--font-sans)" }}
                      >
                        <span
                          className={`w-2 h-2 rounded-full ${FAMILY_STYLES[l.family].dot}`}
                        />
                        {l.label}
                      </span>
                    ))}
                  </div>
                </div>
                <p
                  className="text-sm text-ink-900 leading-loose whitespace-pre-wrap"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {segments.map((seg, i) => {
                    if (seg.issueIndex === null)
                      return <span key={i}>{seg.text}</span>;
                    const issue = feedback.issues[seg.issueIndex];
                    const fam = FAMILY_STYLES[familyOf(issue.type)];
                    const active = activeIssue === seg.issueIndex;
                    return (
                      <mark
                        key={i}
                        onMouseEnter={() => setActiveIssue(seg.issueIndex)}
                        onMouseLeave={() => setActiveIssue(null)}
                        title={`${issue.suggestion} — ${issue.explanation}`}
                        className={`rounded px-0.5 cursor-help transition-shadow ${fam.mark} ${active ? fam.markActive : ""}`}
                      >
                        {seg.text}
                      </mark>
                    );
                  })}
                </p>
              </div>

              {/* Issues list */}
              <div className="flex flex-col gap-3">
                <span
                  className="text-xs font-semibold uppercase tracking-widest text-surface-500"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {feedback.issues.length === 0
                    ? "No issues found"
                    : `${feedback.issues.length} suggestion${feedback.issues.length === 1 ? "" : "s"}`}
                </span>

                {feedback.issues.length === 0 ? (
                  <div className="bg-sage-50 border border-sage-100 rounded-xl px-5 py-4 flex items-center gap-3">
                    <i className="pi pi-check-circle text-sage-600" />
                    <span
                      className="text-sm text-sage-800"
                      style={{ fontFamily: "var(--font-sans)" }}
                    >
                      Great work — the AI didn’t flag any mistakes.
                    </span>
                  </div>
                ) : (
                  feedback.issues.map((issue, i) => {
                    const fam = FAMILY_STYLES[familyOf(issue.type)];
                    return (
                      <div
                        key={i}
                        onMouseEnter={() => setActiveIssue(i)}
                        onMouseLeave={() => setActiveIssue(null)}
                        className={`bg-white rounded-xl border px-5 py-4 transition-colors ${activeIssue === i ? "border-surface-300" : "border-surface-200"}`}
                      >
                        <div className="flex items-center gap-2 mb-2 flex-wrap">
                          <span
                            className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full ${fam.badge}`}
                            style={{ fontFamily: "var(--font-sans)" }}
                          >
                            {issue.type.replace("_", " ").toLowerCase()}
                          </span>
                        </div>
                        <p
                          className="text-sm mb-1.5"
                          style={{ fontFamily: "var(--font-sans)" }}
                        >
                          <span className="text-error-500 line-through decoration-error-300">
                            {issue.original}
                          </span>
                          <i className="pi pi-arrow-right text-[10px] text-surface-400 mx-2" />
                          <span className="text-sage-700 font-medium">
                            {issue.suggestion}
                          </span>
                        </p>
                        <p
                          className="text-xs text-surface-500 leading-relaxed"
                          style={{ fontFamily: "var(--font-sans)" }}
                        >
                          {issue.explanation}
                        </p>
                      </div>
                    );
                  })
                )}
              </div>

              {/* Actions */}
              <div className="flex gap-3">
                <button
                  onClick={() => {
                    setFeedback(null);
                    setActiveIssue(null);
                  }}
                  className="flex-1 px-4 py-2.5 rounded-lg border border-surface-200 bg-white text-surface-700 text-sm font-medium cursor-pointer hover:bg-surface-50 transition-colors"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Edit my writing
                </button>
                <button
                  onClick={() => loadChallenge(level)}
                  className="flex-1 px-4 py-2.5 rounded-lg bg-ink-900 text-parchment text-sm font-semibold cursor-pointer border-none hover:bg-ink-800 transition-colors"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  New challenge
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}
