import { useState, useEffect, useCallback } from 'react';
import Layout from '@/layouts/Layout';
import { createWord, getWords } from '@/service/word';
import type { WordItem, WordMeaning, WordPage } from '@/service/word';

const PAGE_SIZE = 10;

const Vocabulary = () => {
  const [word, setWord] = useState('');
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
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

  const fetchWords = useCallback(async () => {
    setLoading(true);
    try {
      const result = await getWords({ keyword: debouncedSearch, page, size: PAGE_SIZE });
      setData(result);
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, page, refreshKey]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    fetchWords();
  }, [fetchWords]);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!word.trim()) return;
    setSubmitting(true);
    try {
      await createWord(word.trim());
      setWord('');
      setSearch('');
      setDebouncedSearch('');
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
      <div className="h-full flex flex-col px-6 py-4 gap-4 max-w-5xl mx-auto w-full">

        {/* Page header */}
        <h1 className="text-2xl font-bold text-surface-900 shrink-0" style={{ fontFamily: 'var(--font-display)' }}>
          Vocabulary
        </h1>

        {/* Add word form — compact, no card title */}
        <div className="bg-white rounded-2xl border border-surface-200 px-4 py-3 shrink-0">
          <form onSubmit={handleAdd} className="flex gap-3">
            <input
              type="text"
              value={word}
              onChange={(e) => setWord(e.target.value)}
              placeholder="Add a new word, e.g. eloquent"
              required
              autoFocus
              className="flex-1 px-3.5 py-2 rounded-lg border border-surface-200 bg-surface-50
                         text-sm text-surface-900 placeholder:text-surface-400
                         focus:outline-none focus:ring-2 focus:ring-ink-900/20 focus:border-ink-700
                         transition-colors"
              style={{ fontFamily: 'var(--font-sans)', borderRadius: 'var(--radius-input)' }}
            />
            <button
              type="submit"
              disabled={submitting || !word.trim()}
              className="flex items-center gap-2 px-4 py-2 rounded-lg
                         bg-ink-900 text-parchment text-sm font-semibold
                         hover:bg-ink-800 disabled:opacity-40 disabled:cursor-not-allowed
                         transition-colors duration-150 cursor-pointer border-none shrink-0"
              style={{ fontFamily: 'var(--font-sans)', borderRadius: 'var(--radius-btn)' }}
            >
              {submitting
                ? <><i className="pi pi-spin pi-spinner text-sm" /> Adding…</>
                : <><i className="pi pi-plus text-sm" /> Add</>
              }
            </button>
          </form>
        </div>

        {/* Word list — fills remaining height */}
        <div className="flex-1 flex flex-col min-h-0 gap-3">

          {/* Toolbar */}
          <div className="flex items-center justify-between gap-4 shrink-0">
            <span className="text-sm font-semibold text-surface-700 uppercase tracking-widest" style={{ fontFamily: 'var(--font-sans)' }}>
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
                className="pl-8 pr-3.5 py-2 rounded-lg border border-surface-200 bg-white
                           text-sm text-surface-900 placeholder:text-surface-400 w-52
                           focus:outline-none focus:ring-2 focus:ring-ink-900/20 focus:border-ink-700
                           transition-colors"
                style={{ fontFamily: 'var(--font-sans)', borderRadius: 'var(--radius-input)' }}
              />
            </div>
          </div>

          {/* Scrollable table */}
          <div className="flex-1 min-h-0 bg-white rounded-2xl border border-surface-200 overflow-hidden flex flex-col">
            <div className="flex-1 overflow-y-auto">
              {loading ? (
                <div className="h-full flex flex-col items-center justify-center gap-3">
                  <i className="pi pi-spin pi-spinner text-2xl text-surface-400" />
                  <p className="text-sm text-surface-400" style={{ fontFamily: 'var(--font-sans)' }}>Loading…</p>
                </div>
              ) : words.length === 0 ? (
                <div className="h-full flex flex-col items-center justify-center gap-3 text-center px-6">
                  <i className="pi pi-book text-3xl text-surface-300" />
                  <p className="text-sm font-medium text-surface-500" style={{ fontFamily: 'var(--font-sans)' }}>
                    {search ? 'No words match your search.' : 'No words yet. Add your first word above!'}
                  </p>
                </div>
              ) : (
                <table className="w-full text-sm" style={{ fontFamily: 'var(--font-sans)' }}>
                  <thead className="sticky top-0 z-10">
                    <tr className="border-b border-surface-100 bg-surface-50">
                      <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider w-8">#</th>
                      <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">Word</th>
                      <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">Meanings</th>
                      <th className="text-left px-5 py-3 text-xs font-semibold text-surface-500 uppercase tracking-wider">Level</th>
                    </tr>
                  </thead>
                  <tbody>
                    {words.map((w, idx) => (
                      <tr
                        key={w.id}
                        className="border-b border-surface-100 last:border-0 hover:bg-surface-50 transition-colors"
                      >
                        <td className="px-5 py-3.5 text-surface-400 text-xs">{page * PAGE_SIZE + idx + 1}</td>
                        <td className="px-5 py-3.5">
                          <span className="font-semibold text-surface-900" style={{ fontFamily: 'var(--font-display)' }}>
                            {w.text}
                          </span>
                        </td>
                        <td className="px-5 py-3.5">
                          <div className="flex flex-col gap-1">
                            {(w.meanings ?? []).map((m: WordMeaning) => (
                              <div key={m.id} className="flex items-center gap-2">
                                <span className="text-xs font-medium text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded capitalize">
                                  {m.partOfSpeech.toLowerCase()}
                                </span>
                                <span className="text-sm text-surface-700">{m.meaning}</span>
                              </div>
                            ))}
                          </div>
                        </td>
                        <td className="px-5 py-3.5">
                          <span className="text-xs font-medium text-surface-500 bg-surface-100 px-2 py-0.5 rounded-full">
                            {w.level}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            {/* Pagination — always visible inside the card */}
            <div
              className="shrink-0 border-t border-surface-100 px-5 py-3 flex items-center justify-between bg-white"
              style={{ fontFamily: 'var(--font-sans)' }}
            >
              <p className="text-xs text-surface-400">
                {totalPages > 0 ? `Page ${page + 1} of ${totalPages}` : ''}
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
                {Array.from({ length: totalPages }, (_, i) => (
                  <button
                    key={i}
                    onClick={() => setPage(i)}
                    className={`w-7 h-7 flex items-center justify-center rounded-md text-xs font-medium transition-colors cursor-pointer border
                      ${i === page
                        ? 'bg-ink-900 text-parchment border-ink-900'
                        : 'bg-white text-surface-600 border-surface-200 hover:bg-surface-50'
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
