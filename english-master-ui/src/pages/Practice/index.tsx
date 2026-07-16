import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "@/layouts/Layout";
import CircleProgress from "@/components/CircleProgress";
import EmptyState from "@/components/EmptyState";
import { usePractices, useAnswerPractice } from "@/hook/usePractice";
import type { PracticeItem, PracticeOption } from "@/service/practice";

type AnswerState = "idle" | "correct" | "wrong";

const OPTION_LABELS = ["A", "B", "C", "D"];

function OptionButton({
  option,
  label,
  correctIds,
  selectedIds,
  answerState,
  onSelect,
}: {
  option: PracticeOption;
  label: string;
  correctIds: string[];
  selectedIds: string[];
  answerState: AnswerState;
  onSelect: (id: string) => void;
}) {
  const isSelected = selectedIds.includes(option.id);
  const isCorrect = correctIds.includes(option.id);
  const revealed = answerState !== "idle";

  let bg = "bg-white hover:bg-surface-50 border-surface-200 text-surface-800";
  let cursor = "cursor-pointer";

  if (revealed) {
    cursor = "cursor-default";
    if (isCorrect) {
      bg = "bg-sage-100 border-sage-600 text-sage-800";
    } else if (isSelected && !isCorrect) {
      bg = "bg-error-50 border-error-400 text-error-600";
    } else {
      bg = "bg-white border-surface-200 text-surface-400 opacity-50";
    }
  }

  return (
    <button
      onClick={() => !revealed && onSelect(option.id)}
      disabled={revealed}
      className={`
        w-full text-left px-4 py-3.5 rounded-xl border text-sm font-medium
        transition-all duration-200 select-none
        ${bg} ${cursor}
        ${!revealed ? "hover:shadow-sm active:scale-[0.98]" : ""}
        ${isSelected && !revealed ? "ring-2 ring-ink-900/20" : ""}
      `}
      style={{ fontFamily: "var(--font-sans)" }}
    >
      <span className="flex items-center gap-3">
        <span
          className={`
            shrink-0 w-6 h-6 rounded-full border flex items-center justify-center text-xs font-bold
            transition-colors duration-200
            ${
              revealed && isCorrect
                ? "bg-sage-600 border-sage-600 text-white"
                : revealed && isSelected && !isCorrect
                  ? "bg-error-500 border-error-500 text-white"
                  : "border-surface-300 text-surface-400"
            }
          `}
        >
          {label}
        </span>
        {option.text}
      </span>
    </button>
  );
}

export default function Practice() {
  const navigate = useNavigate();
  const {
    data: items = [],
    isLoading: loading,
    isError: error,
    refetch,
  } = usePractices();
  const answerMutation = useAnswerPractice();
  const [index, setIndex] = useState(0);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [answerState, setAnswerState] = useState<AnswerState>("idle");
  const [score, setScore] = useState(0);
  const [done, setDone] = useState(false);

  const current: PracticeItem | undefined = items[index];
  const total = items.length;

  const advance = () => {
    setTimeout(() => {
      if (index + 1 >= total) {
        setDone(true);
      } else {
        setIndex((i) => i + 1);
        setSelectedIds([]);
        setAnswerState("idle");
      }
    }, 1200);
  };

  const handleSelect = (id: string) => {
    if (answerState !== "idle") return;
    const next = [id];
    setSelectedIds(next);

    answerMutation.mutate(
      {
        wordId: current.wordId,
        practiceId: current.practiceId,
        selectedOptionIds: next,
      },
      {
        onSuccess: (result) => {
          setAnswerState(result.correct ? "correct" : "wrong");
          if (result.correct) setScore((s) => s + 1);
          advance();
        },
        onError: () => {
          const isCorrect =
            JSON.stringify(next) === JSON.stringify(current.correctAnswer);
          setAnswerState(isCorrect ? "correct" : "wrong");
          if (isCorrect) setScore((s) => s + 1);
          advance();
        },
      },
    );
  };

  const restart = () => {
    setIndex(0);
    setSelectedIds([]);
    setAnswerState("idle");
    setScore(0);
    setDone(false);
    refetch();
  };

  if (loading) {
    return (
      <Layout>
        <div className="h-full flex items-center justify-center">
          <div className="flex flex-col items-center gap-3">
            <i className="pi pi-spin pi-spinner text-2xl text-surface-400" />
            <p
              className="text-sm text-surface-400"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              Loading practice…
            </p>
          </div>
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <EmptyState
          icon="pi-exclamation-triangle"
          title="Something went wrong"
          description="Failed to load practice. Check your connection and try again."
          action={{ label: "Try again", onClick: () => refetch() }}
        />
      </Layout>
    );
  }

  if (total === 0) {
    return (
      <Layout>
        <EmptyState
          icon="pi-check-circle"
          title="All caught up"
          description="No words to practice right now. Add more words to your vocabulary and come back."
          action={{
            label: "Go to Vocabulary",
            onClick: () => navigate("/vocabulary"),
          }}
        />
      </Layout>
    );
  }

  if (done) {
    const pct = Math.round((score / total) * 100);
    const perfect = score === total;
    return (
      <Layout>
        <div className="h-full flex items-center justify-center px-4">
          <div
            className="w-full max-w-md bg-white rounded-2xl border border-surface-200 p-8 flex flex-col items-center gap-6 text-center"
            style={{ boxShadow: "0 4px 32px 0 rgba(26,31,46,0.07)" }}
          >
            <div
              className={`w-20 h-20 rounded-full flex items-center justify-center text-3xl ${perfect ? "bg-gold-100 text-gold-600" : "bg-sage-100 text-sage-600"}`}
            >
              {perfect ? "🎉" : "✓"}
            </div>
            <div>
              <h2
                className="text-2xl font-bold text-surface-900 mb-1"
                style={{ fontFamily: "var(--font-display)" }}
              >
                {perfect ? "Perfect session!" : "Session complete"}
              </h2>
              <p
                className="text-sm text-surface-500"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {perfect
                  ? "You got every answer right. Keep it up."
                  : `You got ${score} out of ${total} correct.`}
              </p>
            </div>
            <div className="w-full bg-surface-100 rounded-xl p-4 flex items-center justify-between">
              <span
                className="text-sm text-surface-500"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                Accuracy
              </span>
              <span
                className={`text-2xl font-bold ${pct >= 80 ? "text-sage-600" : pct >= 50 ? "text-gold-500" : "text-error-500"}`}
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {pct}%
              </span>
            </div>
            <div className="flex gap-3 w-full">
              <button
                onClick={() => navigate("/")}
                className="flex-1 px-4 py-2.5 rounded-lg border border-surface-200 bg-white text-surface-700 text-sm font-medium cursor-pointer hover:bg-surface-50 transition-colors"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                Dashboard
              </button>
              <button
                onClick={restart}
                className="flex-1 px-4 py-2.5 rounded-lg bg-ink-900 text-parchment text-sm font-semibold cursor-pointer border-none hover:bg-ink-800 transition-colors"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                Practice again
              </button>
            </div>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="h-full flex flex-col items-center justify-center px-4 py-6">
        <div className="w-full max-w-lg flex flex-col gap-6">
          {/* Progress row */}
          <div className="flex items-center gap-4">
            <CircleProgress current={index + 1} total={total} />
            <div className="flex-1">
              <div className="flex items-center justify-between mb-1.5">
                <span
                  className="text-xs font-semibold text-surface-500 uppercase tracking-widest"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  Practice
                </span>
                <span
                  className="text-xs text-surface-400"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  {index + 1} of {total}
                </span>
              </div>
              <div className="h-1.5 bg-surface-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gold-500 rounded-full transition-all duration-500 ease-out"
                  style={{ width: `${((index + 1) / total) * 100}%` }}
                />
              </div>
            </div>
          </div>

          {/* Question card */}
          <div
            className="bg-white rounded-2xl border border-surface-200 px-8 py-8 flex flex-col items-center text-center gap-3"
            style={{ boxShadow: "0 2px 20px 0 rgba(26,31,46,0.06)" }}
          >
            <span
              className="text-xs font-semibold uppercase tracking-widest px-3 py-1 rounded-full bg-surface-100 text-surface-500"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              {current.partOfSpeech.toLowerCase()}
            </span>
            <h1
              className="text-4xl font-bold text-ink-900 leading-tight"
              style={{ fontFamily: "var(--font-display)" }}
            >
              {current.word}
            </h1>
            <p
              className="text-sm text-surface-400 mt-1"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              {current.question ?? "What does this word mean?"}
            </p>
          </div>

          {/* Options grid */}
          <div className="grid grid-cols-2 gap-3">
            {current.options.map((option, i) => (
              <OptionButton
                key={option.id}
                option={option}
                label={OPTION_LABELS[i] ?? String(i + 1)}
                correctIds={current.correctAnswer}
                selectedIds={selectedIds}
                answerState={answerState}
                onSelect={handleSelect}
              />
            ))}
          </div>

          {/* Feedback bar */}
          <div
            className={`
              rounded-xl px-4 py-3 flex items-center gap-3 text-sm font-medium
              transition-all duration-300
              ${
                answerState === "correct"
                  ? "bg-sage-100 text-sage-800 opacity-100"
                  : answerState === "wrong"
                    ? "bg-error-50 text-error-600 opacity-100"
                    : "opacity-0 pointer-events-none bg-surface-50"
              }
            `}
            style={{ fontFamily: "var(--font-sans)" }}
          >
            {answerState === "correct" && (
              <>
                <i className="pi pi-check-circle text-sage-600" />
                Correct! Moving on…
              </>
            )}
            {answerState === "wrong" && (
              <>
                <i className="pi pi-times-circle text-error-500" />
                The correct answer is highlighted above.
              </>
            )}
            {answerState === "idle" && <span>&nbsp;</span>}
          </div>
        </div>
      </div>
    </Layout>
  );
}
