import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import type { AxiosError } from "axios";
import Layout from "@/layouts/Layout";
import {
  useGenerateTranslationChallenge,
  useSubmitTranslation,
} from "@/hook/useTranslation";
import { selectUser } from "@/redux/user/selectors";
import { languageLevelLabel } from "@/constants/languageLevel";
import type {
  TranslationChallenge,
  TranslationDirection,
  TranslationFeedback,
} from "@/service/translation";

const DIRECTIONS: { value: TranslationDirection; label: string }[] = [
  { value: "EN_TO_VI", label: "English → Vietnamese" },
  { value: "VI_TO_EN", label: "Vietnamese → English" },
];

function targetLanguageOf(direction: TranslationDirection): string {
  return direction === "EN_TO_VI" ? "Vietnamese" : "English";
}

export default function Translation() {
  const navigate = useNavigate();
  const user = useSelector(selectUser);
  const [direction, setDirection] = useState<TranslationDirection>("EN_TO_VI");
  const [challenge, setChallenge] = useState<TranslationChallenge | null>(null);
  const [answer, setAnswer] = useState("");
  const [feedback, setFeedback] = useState<TranslationFeedback | null>(null);
  const [submitError, setSubmitError] = useState("");

  const generate = useGenerateTranslationChallenge();
  const submit = useSubmitTranslation();

  const loadChallenge = (dir: TranslationDirection) => {
    setFeedback(null);
    setAnswer("");
    setSubmitError("");
    generate.mutate(dir, { onSuccess: (c) => setChallenge(c) });
  };

  // Load the first challenge on mount.
  useEffect(() => {
    generate.mutate(direction, { onSuccess: (c) => setChallenge(c) });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleDirectionChange = (dir: TranslationDirection) => {
    if (dir === direction) return;
    setDirection(dir);
    loadChallenge(dir);
  };

  const handleSubmit = () => {
    if (!challenge || !answer.trim()) return;
    setSubmitError("");
    submit.mutate(
      { challengeId: challenge.id, translation: answer.trim() },
      {
        onSuccess: (f) => setFeedback(f),
        onError: (err) => {
          const apiError = err as AxiosError<{ data: { message?: string } }>;
          setSubmitError(
            apiError.response?.data?.data?.message ||
              "Couldn’t check your translation right now. Please try again.",
          );
        },
      },
    );
  };

  const handleNext = () => loadChallenge(direction);

  const busy = generate.isPending || submit.isPending;

  return (
    <Layout>
      <div className="h-full overflow-y-auto">
        <div className="max-w-2xl mx-auto px-4 py-6 flex flex-col gap-6">
          {/* Header */}
          <div className="flex items-center justify-between flex-wrap gap-3">
            <div>
              <h1
                className="text-2xl font-bold text-ink-900"
                style={{ fontFamily: "var(--font-display)" }}
              >
                Translation
              </h1>
              <p
                className="text-sm text-surface-500"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                Translate the sentence below, then get instant AI feedback.
              </p>
            </div>
            {user.languageLevel && (
              <button
                onClick={() => navigate("/account")}
                className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface-100 hover:bg-surface-200 transition-colors cursor-pointer"
                title="Change your level in Account settings"
              >
                <span
                  className="text-xs font-semibold text-surface-700"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {languageLevelLabel(user.languageLevel)}
                </span>
                <i className="pi pi-pencil text-[10px] text-surface-400" />
              </button>
            )}
          </div>

          {/* Direction toggle */}
          <div className="flex items-center gap-1 bg-surface-100 rounded-lg p-1 self-start">
            {DIRECTIONS.map((d) => (
              <button
                key={d.value}
                onClick={() => handleDirectionChange(d.value)}
                disabled={busy}
                className={`px-3 py-1.5 rounded-md text-xs font-semibold transition-colors cursor-pointer disabled:cursor-not-allowed disabled:opacity-60 flex items-center gap-1.5 ${
                  direction === d.value
                    ? "bg-white text-ink-900 shadow-sm"
                    : "text-surface-500 hover:text-surface-700"
                }`}
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {d.label}
              </button>
            ))}
          </div>

          {/* Challenge card */}
          <div
            className="bg-white rounded-2xl border border-surface-200 px-6 py-5"
            style={{ boxShadow: "0 2px 20px 0 rgba(26,31,46,0.06)" }}
          >
            <span
              className="text-xs font-semibold uppercase tracking-widest text-gold-600 block mb-2"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              Translate into {targetLanguageOf(direction)}
            </span>

            {generate.isPending ? (
              <div className="flex items-center gap-2 py-4 text-surface-400">
                <i className="pi pi-spin pi-spinner" />
                <span
                  className="text-sm"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Generating a sentence…
                </span>
              </div>
            ) : generate.isError ? (
              <div className="flex flex-col items-start gap-2 py-2">
                <p
                  className="text-sm text-error-500"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Something went wrong reaching the AI. Please try again.
                </p>
                <button
                  onClick={() => loadChallenge(direction)}
                  className="text-xs font-semibold text-ink-900 underline cursor-pointer"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Retry
                </button>
              </div>
            ) : challenge ? (
              <p
                className="text-xl font-semibold text-ink-900 leading-relaxed"
                style={{ fontFamily: "var(--font-display)" }}
              >
                “{challenge.sourceText}”
              </p>
            ) : null}
          </div>

          {/* Answer area (hidden once feedback is shown) */}
          {!feedback && challenge && (
            <div className="flex flex-col gap-3">
              <textarea
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                placeholder={`Type your ${targetLanguageOf(direction)} translation here…`}
                rows={3}
                disabled={submit.isPending}
                className="w-full rounded-2xl border border-surface-200 bg-white px-5 py-4 text-sm text-ink-900 leading-relaxed resize-y outline-none focus:border-sage-500 focus:ring-2 focus:ring-sage-500/20 transition-colors disabled:opacity-60"
                style={{ fontFamily: "var(--font-sans)" }}
              />
              <div className="flex justify-end">
                <button
                  onClick={handleSubmit}
                  disabled={!answer.trim() || submit.isPending}
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
                      Check translation
                    </>
                  )}
                </button>
              </div>
              {submitError && (
                <p
                  className="text-xs text-error-500"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {submitError}
                </p>
              )}
            </div>
          )}

          {/* Feedback */}
          {feedback && (
            <div className="flex flex-col gap-4">
              <div
                className={`rounded-2xl border px-6 py-5 flex items-start gap-3 ${
                  feedback.correct
                    ? "bg-sage-50 border-sage-100"
                    : "bg-error-50 border-error-100"
                }`}
              >
                <i
                  className={`pi ${feedback.correct ? "pi-check-circle text-sage-600" : "pi-times-circle text-error-500"} text-xl mt-0.5`}
                />
                <div>
                  <p
                    className={`text-sm font-bold mb-1 ${feedback.correct ? "text-sage-800" : "text-error-600"}`}
                    style={{ fontFamily: "var(--font-sans)" }}
                  >
                    {feedback.correct ? "Correct!" : "Not quite right"}
                  </p>
                  <p
                    className={`text-sm leading-relaxed ${feedback.correct ? "text-sage-800" : "text-error-700"}`}
                    style={{ fontFamily: "var(--font-sans)" }}
                  >
                    {feedback.feedback}
                  </p>
                </div>
              </div>

              <div
                className="bg-white rounded-2xl border border-surface-200 px-6 py-5"
                style={{ boxShadow: "0 2px 20px 0 rgba(26,31,46,0.06)" }}
              >
                <span
                  className="text-xs font-semibold uppercase tracking-widest text-surface-500 block mb-2"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Your translation
                </span>
                <p
                  className="text-sm text-ink-900 leading-relaxed"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {answer}
                </p>
              </div>

              <div
                className="bg-white rounded-2xl border border-surface-200 px-6 py-5"
                style={{ boxShadow: "0 2px 20px 0 rgba(26,31,46,0.06)" }}
              >
                <span
                  className="text-xs font-semibold uppercase tracking-widest text-surface-500 block mb-2"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Suggested translation
                </span>
                <p
                  className="text-sm text-ink-900 leading-relaxed"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {feedback.suggestedTranslation}
                </p>
              </div>

              <button
                onClick={handleNext}
                disabled={generate.isPending}
                className="w-full px-4 py-2.5 rounded-lg bg-ink-900 text-parchment text-sm font-semibold cursor-pointer border-none hover:bg-ink-800 transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {generate.isPending ? (
                  <>
                    <i className="pi pi-spin pi-spinner text-xs" />
                    Loading…
                  </>
                ) : (
                  <>
                    Next sentence
                    <i className="pi pi-arrow-right text-xs" />
                  </>
                )}
              </button>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}
