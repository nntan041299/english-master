import { useEffect, useRef, useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import type { AxiosError } from "axios";
import Layout from "@/layouts/Layout";
import EmptyState from "@/components/EmptyState";
import {
  useGenerateListeningChallenge,
  usePlayListeningAudio,
  useSubmitListening,
} from "@/hook/useListening";
import { selectUser } from "@/redux/user/selectors";
import { languageLevelLabel } from "@/constants/languageLevel";
import type {
  ListeningChallenge,
  ListeningFeedback,
} from "@/service/listening";

export default function Listening() {
  const navigate = useNavigate();
  const user = useSelector(selectUser);
  const [challenge, setChallenge] = useState<ListeningChallenge | null>(null);
  const [noPracticeAvailable, setNoPracticeAvailable] = useState(false);
  const [transcript, setTranscript] = useState("");
  const [feedback, setFeedback] = useState<ListeningFeedback | null>(null);
  const [submitError, setSubmitError] = useState("");
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [playbackError, setPlaybackError] = useState("");
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const audioUrlRef = useRef<string | null>(null);

  const generate = useGenerateListeningChallenge();
  const playAudio = usePlayListeningAudio();
  const submit = useSubmitListening();

  const stopAudio = () => {
    audioRef.current?.pause();
    if (audioUrlRef.current) {
      URL.revokeObjectURL(audioUrlRef.current);
      audioUrlRef.current = null;
    }
    setIsSpeaking(false);
  };

  const handleChallengeLoaded = (c: ListeningChallenge | null) => {
    setChallenge(c);
    setNoPracticeAvailable(c === null);
  };

  const loadChallenge = () => {
    setFeedback(null);
    setTranscript("");
    setSubmitError("");
    setPlaybackError("");
    setNoPracticeAvailable(false);
    stopAudio();
    generate.mutate(undefined, { onSuccess: handleChallengeLoaded });
  };

  // Load the first challenge on mount.
  useEffect(() => {
    generate.mutate(undefined, { onSuccess: handleChallengeLoaded });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Stop any playback in progress if the user navigates away.
  useEffect(() => stopAudio, []);

  const handlePlay = () => {
    if (!challenge) return;
    setPlaybackError("");
    stopAudio();
    playAudio.mutate(challenge.id, {
      onSuccess: (blob) => {
        const url = URL.createObjectURL(blob);
        audioUrlRef.current = url;
        const audio = new Audio(url);
        audioRef.current = audio;
        audio.onplay = () => setIsSpeaking(true);
        audio.onended = () => setIsSpeaking(false);
        audio.onerror = () => setIsSpeaking(false);
        audio.play().catch(() => {
          setIsSpeaking(false);
          setPlaybackError("Couldn’t play the sentence. Please try again.");
        });
      },
      onError: () => {
        setPlaybackError(
          "Couldn’t generate audio right now. Please try again.",
        );
      },
    });
  };

  const handleSubmit = () => {
    if (!challenge || !transcript.trim()) return;
    setSubmitError("");
    submit.mutate(
      { challengeId: challenge.id, transcript: transcript.trim() },
      {
        onSuccess: (f) => setFeedback(f),
        onError: (err) => {
          const apiError = err as AxiosError<{ data: { message?: string } }>;
          setSubmitError(
            apiError.response?.data?.data?.message ||
              "Couldn’t check your answer right now. Please try again.",
          );
        },
      },
    );
  };

  if (noPracticeAvailable && !generate.isPending) {
    return (
      <Layout>
        <EmptyState
          icon="pi-volume-off"
          title="No listening practice yet"
          description="New sentences are added periodically. Please check back soon."
          action={{ label: "Check again", onClick: loadChallenge }}
        />
      </Layout>
    );
  }

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
                Listening
              </h1>
              <p
                className="text-sm text-surface-500"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                Listen to the sentence, then type exactly what you heard.
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

          {/* Challenge card */}
          <div
            className="bg-white rounded-2xl border border-surface-200 px-6 py-8 flex flex-col items-center gap-4"
            style={{ boxShadow: "0 2px 20px 0 rgba(26,31,46,0.06)" }}
          >
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
              <div className="flex flex-col items-center gap-2 py-2">
                <p
                  className="text-sm text-error-500"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Something went wrong reaching the AI. Please try again.
                </p>
                <button
                  onClick={loadChallenge}
                  className="text-xs font-semibold text-ink-900 underline cursor-pointer"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Retry
                </button>
              </div>
            ) : challenge ? (
              <>
                <button
                  onClick={handlePlay}
                  disabled={playAudio.isPending}
                  aria-label="Play sentence"
                  className={`w-20 h-20 rounded-full flex items-center justify-center cursor-pointer border-none transition-all duration-200 disabled:opacity-40 disabled:cursor-not-allowed ${
                    isSpeaking
                      ? "bg-gold-500 scale-105"
                      : "bg-ink-900 hover:bg-ink-800"
                  }`}
                >
                  <i
                    className={`pi ${playAudio.isPending ? "pi-spin pi-spinner" : isSpeaking ? "pi-volume-up" : "pi-play"} text-2xl text-parchment`}
                  />
                </button>
                <p
                  className="text-sm text-surface-500"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {playAudio.isPending
                    ? "Loading audio…"
                    : isSpeaking
                      ? "Playing…"
                      : "Tap to hear the sentence"}
                </p>
                {playbackError && (
                  <p
                    className="text-xs text-error-500"
                    style={{ fontFamily: "var(--font-sans)" }}
                  >
                    {playbackError}
                  </p>
                )}
              </>
            ) : null}
          </div>

          {/* Answer area (hidden once feedback is shown) */}
          {!feedback && challenge && (
            <div className="flex flex-col gap-3">
              <textarea
                value={transcript}
                onChange={(e) => setTranscript(e.target.value)}
                placeholder="Type exactly what you heard…"
                rows={3}
                disabled={submit.isPending}
                className="w-full rounded-2xl border border-surface-200 bg-white px-5 py-4 text-sm text-ink-900 leading-relaxed resize-y outline-none focus:border-sage-500 focus:ring-2 focus:ring-sage-500/20 transition-colors disabled:opacity-60"
                style={{ fontFamily: "var(--font-sans)" }}
              />
              <div className="flex justify-end">
                <button
                  onClick={handleSubmit}
                  disabled={!transcript.trim() || submit.isPending}
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
                      Check answer
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
                  Your answer
                </span>
                <p
                  className="text-sm text-ink-900 leading-relaxed"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {transcript}
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
                  Correct sentence
                </span>
                <p
                  className="text-sm text-ink-900 leading-relaxed"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {feedback.sentence}
                </p>
              </div>

              <button
                onClick={loadChallenge}
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
