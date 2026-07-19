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
