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

/**
 * Fetches a listening challenge at the current user's own language level from the pre-generated
 * pool. Returns null when none are available yet (backend responds 204 No Content).
 */
export const getListeningChallenge =
  async (): Promise<ListeningChallenge | null> => {
    const response = await request.get({
      path: `${ENDPOINT.LISTENING}/challenge`,
    });
    if (response.status === 204) {
      return null;
    }
    return response.data.data;
  };

/** Fetches the challenge's pre-synthesized WAV audio. */
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
