import { useState, useEffect } from "react";
import type { AxiosError } from "axios";
import Layout from "@/layouts/Layout";
import LevelBadge from "@/components/LevelBadge";
import EmptyState from "@/components/EmptyState";
import {
  createWord,
  deleteWord,
  getWords,
  updateMeaning,
} from "@/service/word";
import type { WordItem, WordMeaning, WordPage } from "@/service/word";

const PAGE_SIZE = 10;

interface EditingMeaning {
  wordId: number;
  meaningId: number;
  text: string;
}

interface RowActions {
  editing: EditingMeaning | null;
  savingEdit: boolean;
  deletingWordId: number | null;
  onStartEdit: (wordId: number, meaning: WordMeaning) => void;
  onEditTextChange: (text: string) => void;
  onSaveEdit: () => void;
  onCancelEdit: () => void;
  onDeleteWord: (wordId: number) => void;
}

/* ── Inline "meaning" editor, shared by the card and table layouts ── */
function MeaningText({
  w,
  m,
  actions,
}: {
  w: WordItem;
  m: WordMeaning;
  actions: RowActions;
}) {
  const { editing, savingEdit } = actions;
  const isEditing = editing?.wordId === w.id && editing?.meaningId === m.id;

  if (isEditing) {
    return (
      <div className="flex items-center gap-1.5 flex-1 min-w-[160px]">
        <input
          autoFocus
          value={editing.text}
          onChange={(e) => actions.onEditTextChange(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") actions.onSaveEdit();
            if (e.key === "Escape") actions.onCancelEdit();
          }}
          className="flex-1 min-w-0 px-2 py-1 rounded-md border border-ink-700 bg-white
                     text-sm text-surface-900 focus:outline-none focus:ring-2 focus:ring-ink-900/20"
          style={{ fontFamily: "var(--font-sans)" }}
        />
        <button
          onClick={actions.onSaveEdit}
          disabled={savingEdit || !editing.text.trim()}
          className="w-6 h-6 flex items-center justify-center rounded-md text-emerald-700
                     hover:bg-emerald-50 disabled:opacity-40 disabled:cursor-not-allowed
                     transition-colors cursor-pointer border-none bg-transparent"
          title="Save"
        >
          <i
            className={`pi ${savingEdit ? "pi-spin pi-spinner" : "pi-check"} text-xs`}
          />
        </button>
        <button
          onClick={actions.onCancelEdit}
          disabled={savingEdit}
          className="w-6 h-6 flex items-center justify-center rounded-md text-surface-500
                     hover:bg-surface-100 disabled:opacity-40 disabled:cursor-not-allowed
                     transition-colors cursor-pointer border-none bg-transparent"
          title="Cancel"
        >
          <i className="pi pi-times text-xs" />
        </button>
      </div>
    );
  }

  return (
    <span className="group inline-flex items-center gap-1.5">
      {m.meaning}
      <button
        onClick={() => actions.onStartEdit(w.id, m)}
        className="w-5 h-5 flex items-center justify-center rounded-md text-surface-300
                   hover:text-ink-700 hover:bg-surface-100 opacity-0 group-hover:opacity-100
                   transition-colors cursor-pointer border-none bg-transparent shrink-0"
        title="Edit meaning"
      >
        <i className="pi pi-pencil text-[10px]" />
      </button>
    </span>
  );
}

/* ── Mobile card ── */
function WordCard({ w, actions }: { w: WordItem; actions: RowActions }) {
  return (
    <div className="px-4 py-3.5 border-b border-surface-100 last:border-0">
      <div className="flex items-center justify-between gap-2 mb-1.5">
        <span
          className="font-bold text-surface-900 text-base truncate"
          style={{ fontFamily: "var(--font-display)" }}
        >
          {w.text}
        </span>
        <div className="flex items-center gap-2 shrink-0">
          <LevelBadge level={w.learningLevel} />
          <button
            onClick={() => actions.onDeleteWord(w.id)}
            disabled={actions.deletingWordId === w.id}
            className="w-6 h-6 flex items-center justify-center rounded-md text-surface-400
                       hover:text-red-600 hover:bg-red-50 disabled:opacity-40 disabled:cursor-not-allowed
                       transition-colors cursor-pointer border-none bg-transparent"
            title="Delete word"
          >
            <i
              className={`pi ${actions.deletingWordId === w.id ? "pi-spin pi-spinner" : "pi-trash"} text-xs`}
            />
          </button>
        </div>
      </div>
      <div className="flex flex-col gap-1">
        {(w.meanings ?? []).map((m: WordMeaning) => (
          <div key={m.id} className="flex items-start gap-2 flex-wrap">
            <span className="text-xs font-medium text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded capitalize shrink-0">
              {m.partOfSpeech.toLowerCase()}
            </span>
            <span className="text-sm text-surface-600 leading-snug">
              <MeaningText w={w} m={m} actions={actions} />
            </span>
            {m.ipa && (
              <span className="text-xs text-surface-400 font-mono">
                {m.ipa}
              </span>
            )}
            {(m.categories ?? []).map((c) => (
              <span
                key={c}
                className="text-xs font-medium text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded-full capitalize shrink-0"
              >
                {c}
              </span>
            ))}
          </div>
        ))}
        {(w.meanings ?? []).length === 0 && (
          <span
            className="text-xs text-surface-400 italic"
            style={{ fontFamily: "var(--font-sans)" }}
          >
            Enriching…
          </span>
        )}
      </div>
    </div>
  );
}

/* ── Desktop table rows: one row per meaning, word/level cells span all of a word's rows ── */
function WordRow({
  w,
  idx,
  page,
  actions,
}: {
  w: WordItem;
  idx: number;
  page: number;
  actions: RowActions;
}) {
  const meanings = w.meanings ?? [];
  const rowCount = Math.max(meanings.length, 1);

  return (
    <>
      {Array.from({ length: rowCount }, (_, mIdx) => {
        const m: WordMeaning | undefined = meanings[mIdx];
        return (
          <tr
            key={m?.id ?? `${w.id}-empty`}
            className="border-b border-surface-100 last:border-0 hover:bg-surface-50 transition-colors"
          >
            {mIdx === 0 && (
              <td
                rowSpan={rowCount}
                className="px-5 py-3.5 text-surface-400 text-xs align-top"
              >
                {page * PAGE_SIZE + idx + 1}
              </td>
            )}
            {mIdx === 0 && (
              <td rowSpan={rowCount} className="px-5 py-3.5 align-top">
                <span
                  className="font-semibold text-surface-900"
                  style={{ fontFamily: "var(--font-display)" }}
                >
                  {w.text}
                </span>
              </td>
            )}
            <td className="px-5 py-3.5">
              {m ? (
                <span className="text-xs font-medium text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded capitalize">
                  {m.partOfSpeech.toLowerCase()}
                </span>
              ) : (
                <span className="text-xs text-surface-400 italic">
                  Enriching…
                </span>
              )}
            </td>
            <td className="px-5 py-3.5 text-sm text-surface-700">
              {m && <MeaningText w={w} m={m} actions={actions} />}
            </td>
            <td className="px-5 py-3.5 text-xs text-surface-400 font-mono">
              {m?.ipa}
            </td>
            <td className="px-5 py-3.5">
              <div className="flex items-center gap-1 flex-wrap">
                {(m?.categories ?? []).map((c) => (
                  <span
                    key={c}
                    className="text-xs font-medium text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded-full capitalize"
                  >
                    {c}
                  </span>
                ))}
                {m && (m.categories ?? []).length === 0 && (
                  <span className="text-xs text-surface-300">—</span>
                )}
              </div>
            </td>
            {mIdx === 0 && (
              <td rowSpan={rowCount} className="px-5 py-3.5 align-top">
                <LevelBadge level={w.learningLevel} />
              </td>
            )}
            {mIdx === 0 && (
              <td rowSpan={rowCount} className="px-5 py-3.5 align-top">
                <button
                  onClick={() => actions.onDeleteWord(w.id)}
                  disabled={actions.deletingWordId === w.id}
                  className="w-7 h-7 flex items-center justify-center rounded-md text-surface-400
                             hover:text-red-600 hover:bg-red-50 disabled:opacity-40 disabled:cursor-not-allowed
                             transition-colors cursor-pointer border-none bg-transparent"
                  title="Delete word"
                >
                  <i
                    className={`pi ${actions.deletingWordId === w.id ? "pi-spin pi-spinner" : "pi-trash"} text-xs`}
                  />
                </button>
              </td>
            )}
          </tr>
        );
      })}
    </>
  );
}

const Vocabulary = () => {
  const [word, setWord] = useState("");
  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [page, setPage] = useState(0);
  const [data, setData] = useState<WordPage | null>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [addError, setAddError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [editing, setEditing] = useState<EditingMeaning | null>(null);
  const [savingEdit, setSavingEdit] = useState(false);
  const [deletingWordId, setDeletingWordId] = useState<number | null>(null);

  useEffect(() => {
    const t = setTimeout(() => {
      setDebouncedSearch(search);
      setPage(0);
    }, 400);
    return () => clearTimeout(t);
  }, [search]);

  useEffect(() => {
    let cancelled = false;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setLoading(true);
    getWords({ keyword: debouncedSearch, page, size: PAGE_SIZE })
      .then((result) => {
        if (!cancelled) setData(result);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [debouncedSearch, page, refreshKey]);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!word.trim()) return;
    setSubmitting(true);
    setAddError("");
    try {
      await createWord(word.trim());
      setWord("");
      setSearch("");
      setDebouncedSearch("");
      setPage(0);
      setRefreshKey((k) => k + 1);
    } catch (err) {
      const apiError = err as AxiosError<{ data: { message?: string } }>;
      setAddError(
        apiError.response?.data?.data?.message ||
          "Couldn't add this word right now. Please try again.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleSaveEdit = async () => {
    if (!editing || !editing.text.trim()) return;
    setSavingEdit(true);
    try {
      await updateMeaning(
        editing.wordId,
        editing.meaningId,
        editing.text.trim(),
      );
      setEditing(null);
      setRefreshKey((k) => k + 1);
    } finally {
      setSavingEdit(false);
    }
  };

  const handleDeleteWord = async (wordId: number) => {
    if (
      !window.confirm(
        "Delete this word and all its progress? This can't be undone.",
      )
    )
      return;
    setDeletingWordId(wordId);
    try {
      await deleteWord(wordId);
      setRefreshKey((k) => k + 1);
    } finally {
      setDeletingWordId(null);
    }
  };

  const rowActions: RowActions = {
    editing,
    savingEdit,
    deletingWordId,
    onStartEdit: (wordId, meaning) =>
      setEditing({ wordId, meaningId: meaning.id, text: meaning.meaning }),
    onEditTextChange: (text) =>
      setEditing((prev) => (prev ? { ...prev, text } : prev)),
    onSaveEdit: handleSaveEdit,
    onCancelEdit: () => setEditing(null),
    onDeleteWord: handleDeleteWord,
  };

  const words: WordItem[] = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  return (
    <Layout>
      <div className="h-full flex flex-col px-4 sm:px-6 py-4 gap-4 max-w-5xl mx-auto w-full">
        {/* Page header */}
        <h1
          className="text-2xl font-bold text-surface-900 shrink-0"
          style={{ fontFamily: "var(--font-display)" }}
        >
          Vocabulary
        </h1>

        {/* Add word form */}
        <div className="bg-white rounded-2xl border border-surface-200 px-4 py-3 shrink-0">
          <form onSubmit={handleAdd} className="flex gap-3">
            <input
              type="text"
              value={word}
              onChange={(e) => {
                setWord(e.target.value);
                if (addError) setAddError("");
              }}
              placeholder="Add a new word, e.g. eloquent"
              required
              autoFocus
              className="flex-1 min-w-0 px-3.5 py-2 rounded-lg border border-surface-200 bg-surface-50
                         text-sm text-surface-900 placeholder:text-surface-400
                         focus:outline-none focus:ring-2 focus:ring-ink-900/20 focus:border-ink-700
                         transition-colors"
              style={{ fontFamily: "var(--font-sans)" }}
            />
            <button
              type="submit"
              disabled={submitting || !word.trim()}
              className="flex items-center gap-2 px-4 py-2 rounded-lg
                         bg-ink-900 text-parchment text-sm font-semibold
                         hover:bg-ink-800 disabled:opacity-40 disabled:cursor-not-allowed
                         transition-colors duration-150 cursor-pointer border-none shrink-0"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              {submitting ? (
                <i className="pi pi-spin pi-spinner text-sm" />
              ) : (
                <>
                  <i className="pi pi-plus text-sm" />
                  <span className="hidden sm:inline">Add</span>
                </>
              )}
            </button>
          </form>
          {addError && (
            <p
              className="mt-2 text-sm text-red-600 flex items-center gap-1.5"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              <i className="pi pi-exclamation-circle text-xs" />
              {addError}
            </p>
          )}
        </div>

        {/* Word list */}
        <div className="flex-1 flex flex-col min-h-0 gap-3">
          {/* Toolbar */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 shrink-0">
            <span
              className="text-sm font-semibold text-surface-700 uppercase tracking-widest"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              My words
              {!loading && (
                <span className="ml-2 font-normal text-surface-400 normal-case tracking-normal">
                  ({totalElements})
                </span>
              )}
            </span>

            <div className="relative">
              <i className="pi pi-search absolute left-3 top-1/2 -translate-y-1/2 text-surface-400 text-xs pointer-events-none" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search words…"
                className="w-full sm:w-52 pl-8 pr-3.5 py-2 rounded-lg border border-surface-200 bg-white
                           text-sm text-surface-900 placeholder:text-surface-400
                           focus:outline-none focus:ring-2 focus:ring-ink-900/20 focus:border-ink-700
                           transition-colors"
                style={{ fontFamily: "var(--font-sans)" }}
              />
            </div>
          </div>

          {/* Content card */}
          <div className="flex-1 min-h-0 bg-white rounded-2xl border border-surface-200 overflow-hidden flex flex-col">
            <div className="flex-1 overflow-y-auto">
              {loading ? (
                <div className="h-full flex flex-col items-center justify-center gap-3">
                  <i className="pi pi-spin pi-spinner text-2xl text-surface-400" />
                  <p
                    className="text-sm text-surface-400"
                    style={{ fontFamily: "var(--font-sans)" }}
                  >
                    Loading…
                  </p>
                </div>
              ) : words.length === 0 ? (
                <EmptyState
                  icon="pi-book"
                  title={search ? "No results" : "No words yet"}
                  description={
                    search
                      ? "No words match your search."
                      : "Add your first word above!"
                  }
                />
              ) : (
                <>
                  {/* Mobile: card list */}
                  <div className="sm:hidden">
                    {words.map((w) => (
                      <WordCard key={w.id} w={w} actions={rowActions} />
                    ))}
                  </div>

                  {/* Desktop: table */}
                  <table
                    className="hidden sm:table w-full text-sm"
                    style={{ fontFamily: "var(--font-sans)" }}
                  >
                    <thead className="sticky top-0 z-10">
                      <tr className="border-b border-surface-100 bg-surface-50">
                        <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider w-8">
                          #
                        </th>
                        <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                          Word
                        </th>
                        <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                          Part of speech
                        </th>
                        <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                          Meaning
                        </th>
                        <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                          IPA
                        </th>
                        <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                          Categories
                        </th>
                        <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                          Level
                        </th>
                        <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                          Actions
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {words.map((w, idx) => (
                        <WordRow
                          key={w.id}
                          w={w}
                          idx={idx}
                          page={page}
                          actions={rowActions}
                        />
                      ))}
                    </tbody>
                  </table>
                </>
              )}
            </div>

            {/* Pagination */}
            <div
              className="shrink-0 border-t border-surface-100 px-4 sm:px-5 py-3 flex items-center justify-between bg-white"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              <p className="text-xs text-surface-400">
                {totalPages > 0 ? `Page ${page + 1} of ${totalPages}` : ""}
              </p>
              <div className="flex items-center gap-1">
                <button
                  onClick={() => setPage((p) => p - 1)}
                  disabled={data?.first ?? true}
                  className="w-7 h-7 flex items-center justify-center rounded-md border border-surface-200
                             text-xs text-surface-600 bg-white hover:bg-surface-50
                             disabled:opacity-30 disabled:cursor-not-allowed transition-colors cursor-pointer"
                >
                  <i className="pi pi-chevron-left text-[10px]" />
                </button>

                {/* Page numbers — desktop only */}
                {Array.from({ length: totalPages }, (_, i) => (
                  <button
                    key={i}
                    onClick={() => setPage(i)}
                    className={`hidden sm:flex w-7 h-7 items-center justify-center rounded-md text-xs font-medium transition-colors cursor-pointer border
                      ${
                        i === page
                          ? "bg-ink-900 text-parchment border-ink-900"
                          : "bg-white text-surface-600 border-surface-200 hover:bg-surface-50"
                      }`}
                  >
                    {i + 1}
                  </button>
                ))}

                <button
                  onClick={() => setPage((p) => p + 1)}
                  disabled={data?.last ?? true}
                  className="w-7 h-7 flex items-center justify-center rounded-md border border-surface-200
                             text-xs text-surface-600 bg-white hover:bg-surface-50
                             disabled:opacity-30 disabled:cursor-not-allowed transition-colors cursor-pointer"
                >
                  <i className="pi pi-chevron-right text-[10px]" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Vocabulary;
