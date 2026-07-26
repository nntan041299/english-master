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
  minWords: number;
  maxWords: number;
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

export interface WritingHistoryItem {
  submissionId: number;
  challengeId: number;
  level: LanguageLevel;
  title: string;
  prompt: string;
  text: string;
  overallFeedback: string;
  score: number | null;
  issues: WritingIssue[];
  submittedAt: string;
}

export interface WritingHistoryPage {
  content: WritingHistoryItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

/**
 * Generates a challenge at the current user's own language level (set on their account). The target
 * word count range is decided by the backend and comes back on the response — nothing to send here.
 */
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

export const getWritingHistory = async (params: {
  page?: number;
  size?: number;
}): Promise<WritingHistoryPage> => {
  const query = new URLSearchParams();
  query.set("page", String(params.page ?? 0));
  query.set("size", String(params.size ?? 10));
  const response = await request.get({
    path: `${ENDPOINT.WRITING}/history?${query.toString()}`,
  });
  return response.data.data;
};
