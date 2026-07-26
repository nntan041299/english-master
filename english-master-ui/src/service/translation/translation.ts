import type { LanguageLevel } from "@/constants/languageLevel";
import { request, ENDPOINT } from "@/rest";

export type TranslationDirection = "EN_TO_VI" | "VI_TO_EN";

export interface TranslationChallenge {
  id: number;
  direction: TranslationDirection;
  level: LanguageLevel;
  sourceText: string;
}

export interface TranslationFeedback {
  submissionId: number;
  correct: boolean;
  feedback: string;
  suggestedTranslation: string;
}

export interface SubmitTranslationRequest {
  challengeId: number;
  translation: string;
}

export interface TranslationHistoryItem {
  submissionId: number;
  challengeId: number;
  direction: TranslationDirection;
  level: LanguageLevel;
  sourceText: string;
  userTranslation: string;
  correct: boolean;
  feedback: string;
  suggestedTranslation: string;
  submittedAt: string;
}

export interface TranslationHistoryPage {
  content: TranslationHistoryItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

/**
 * Generates a translation challenge at the current user's own language level, in the given
 * direction. Reuses an existing unanswered challenge in that direction/level when one exists.
 */
export const getTranslationChallenge = async (
  direction: TranslationDirection,
): Promise<TranslationChallenge> => {
  const response = await request.get({
    path: `${ENDPOINT.TRANSLATION}/challenge?direction=${direction}`,
  });
  return response.data.data;
};

export const submitTranslation = async (
  body: SubmitTranslationRequest,
): Promise<TranslationFeedback> => {
  const response = await request.post({
    path: `${ENDPOINT.TRANSLATION}/submit`,
    body,
  });
  return response.data.data;
};

export const getTranslationHistory = async (params: {
  page?: number;
  size?: number;
}): Promise<TranslationHistoryPage> => {
  const query = new URLSearchParams();
  query.set("page", String(params.page ?? 0));
  query.set("size", String(params.size ?? 10));
  const response = await request.get({
    path: `${ENDPOINT.TRANSLATION}/history?${query.toString()}`,
  });
  return response.data.data;
};
