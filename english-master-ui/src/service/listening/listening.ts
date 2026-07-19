import type { LanguageLevel } from "@/constants/languageLevel";
import { request, ENDPOINT } from "@/rest";

export interface ListeningChallenge {
  id: number;
  level: LanguageLevel;
  sentence: string;
}

export interface ListeningFeedback {
  submissionId: number;
  correct: boolean;
  feedback: string;
  sentence: string;
}

export interface SubmitListeningRequest {
  challengeId: number;
  transcript: string;
}

/** Generates a listening challenge at the current user's own language level (set on their account). */
export const getListeningChallenge = async (): Promise<ListeningChallenge> => {
  const response = await request.get({
    path: `${ENDPOINT.LISTENING}/challenge`,
  });
  return response.data.data;
};

/** Fetches the challenge's sentence synthesized as MP3 audio. */
export const getListeningAudio = async (challengeId: number): Promise<Blob> => {
  const response = await request.get({
    path: `${ENDPOINT.LISTENING}/${challengeId}/audio`,
    responseType: "blob",
  });
  return response.data;
};

export const submitListening = async (
  body: SubmitListeningRequest,
): Promise<ListeningFeedback> => {
  const response = await request.post({
    path: `${ENDPOINT.LISTENING}/submit`,
    body,
  });
  return response.data.data;
};
