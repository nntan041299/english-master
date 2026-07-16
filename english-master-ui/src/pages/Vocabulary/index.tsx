import { useState, useEffect } from "react";
import Layout from "@/layouts/Layout";
import LevelBadge from "@/components/LevelBadge";
import EmptyState from "@/components/EmptyState";
import { createWord, getWords } from "@/service/word";
import type { WordItem, WordMeaning, WordPage } from "@/service/word";

const PAGE_SIZE = 10;

/* ── Mobile card ── */
function WordCard({ w }: { w: WordItem }) {
  return (
    <div className="px-4 py-3.5 border-b border-surface-100 last:border-0">
      <div className="flex items-center justify-between gap-2 mb-1.5">
        <span
          className="font-bold text-surface-900 text-base truncate"
          style={{ fontFamily: "var(--font-display)" }}
        >
          {w.text}
        </span>
        <LevelBadge level={w.learningLevel} />
      </div>
      <div className="flex flex-col gap-1">
        {(w.meanings ?? []).map((m: WordMeaning) => (
          <div key={m.id} className="flex items-start gap-2 flex-wrap">
            <span className="text-xs font-medium text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded capitalize shrink-0">
              {m.partOfSpeech.toLowerCase()}
            </span>
            <span className="text-sm text-surface-600 leading-snug">
              {m.meaning}
            </span>
            {m.ipa && (
              <span className="text-xs text-surface-400 font-mono">
                {m.ipa}
              </span>
            )}
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

/* ── Desktop table row ── */
function WordRow({ w, idx, page }: { w: WordItem; idx: number; page: number }) {
  return (
    <tr className="border-b border-surface-100 last:border-0 hover:bg-surface-50 transition-colors">
      <td className="px-5 py-3.5 text-surface-400 text-xs">
        {page * PAGE_SIZE + idx + 1}
      </td>
      <td className="px-5 py-3.5">
        <span
          className="font-semibold text-surface-900"
          style={{ fontFamily: "var(--font-display)" }}
        >
          {w.text}
        </span>
      </td>
      <td className="px-5 py-3.5">
        <div className="flex flex-col gap-1">
          {(w.meanings ?? []).map((m: WordMeaning) => (
            <div key={m.id} className="flex items-center gap-2 flex-wrap">
              <span className="text-xs font-medium text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded capitalize">
                {m.partOfSpeech.toLowerCase()}
              </span>
              <span className="text-sm text-surface-700">{m.meaning}</span>
              {m.ipa && (
                <span className="text-xs text-surface-400 font-mono">
                  {m.ipa}
                </span>
              )}
            </div>
          ))}
          {(w.meanings ?? []).length === 0 && (
            <span className="text-xs text-surface-400 italic">Enriching…</span>
          )}
        </div>
      </td>
      <td className="px-5 py-3.5">
        <LevelBadge level={w.learningLevel} />
      </td>
    </tr>
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
  const [refreshKey, setRefreshKey] = useState(0);

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
    try {
      await createWord(word.trim());
      setWord("");
      setSearch("");
      setDebouncedSearch("");
      setPage(0);
      setRefreshKey((k) => k + 1);
    } finally {
      setSubmitting(false);
    }
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
              onChange={(e) => setWord(e.target.value)}
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
                      <WordCard key={w.id} w={w} />
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
                          Meanings
                        </th>
                        <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                          Level
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {words.map((w, idx) => (
                        <WordRow key={w.id} w={w} idx={idx} page={page} />
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
