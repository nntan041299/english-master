import type { LanguageLevel } from "@/constants/languageLevel";
import { request, ENDPOINT } from "@/rest";

export type WritingIssueType =
  | "GRAMMAR"
  | "SPELLING"
  | "PUNCTUATION"
  | "VOCABULARY"
  | "WORD_ORDER"
  | "STYLE"
  | "CLARITY"
  | "OTHER";

export interface WritingChallenge {
  id: number;
  level: LanguageLevel;
  title: string;
  prompt: string;
}

export interface WritingIssue {
  original: string;
  suggestion: string;
  explanation: string;
  type: WritingIssueType;
}

export interface WritingFeedback {
  submissionId: number;
  overallFeedback: string;
  score: number | null;
  issues: WritingIssue[];
}

export interface SubmitWritingRequest {
  challengeId: number;
  text: string;
}

/** Generates a challenge at the current user's own language level (set on their account). */
export const getWritingChallenge = async (): Promise<WritingChallenge> => {
  const response = await request.get({
    path: `${ENDPOINT.WRITING}/challenge`,
  });
  return response.data.data;
};

export const submitWriting = async (
  body: SubmitWritingRequest,
): Promise<WritingFeedback> => {
  const response = await request.post({
    path: `${ENDPOINT.WRITING}/submit`,
    body,
  });
  return response.data.data;
};
