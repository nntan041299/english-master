import { request, ENDPOINT } from "@/rest";

export type WritingLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";

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
  level: WritingLevel;
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

export const getWritingChallenge = async (
  level: WritingLevel,
): Promise<WritingChallenge> => {
  const response = await request.get({
    path: `${ENDPOINT.WRITING}/challenge?level=${level}`,
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
