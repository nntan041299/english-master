export type LanguageLevel = "A1" | "A2" | "B1" | "B2" | "C1" | "C2";

/** CEFR (Common European Framework of Reference for Languages) — the international standard scale. */
export const LANGUAGE_LEVELS: { value: LanguageLevel; label: string }[] = [
  { value: "A1", label: "A1 · Beginner" },
  { value: "A2", label: "A2 · Elementary" },
  { value: "B1", label: "B1 · Intermediate" },
  { value: "B2", label: "B2 · Upper Intermediate" },
  { value: "C1", label: "C1 · Advanced" },
  { value: "C2", label: "C2 · Proficient" },
];

export const languageLevelLabel = (level: LanguageLevel): string =>
  LANGUAGE_LEVELS.find((l) => l.value === level)?.label ?? level;
