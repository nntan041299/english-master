import { request, ENDPOINT } from "@/rest";

export interface PracticeOption {
  id: string;
  text: string;
}

export interface PracticeItem {
  wordId: number;
  word: string;
  learningTracking: string;
  practiceId: number;
  partOfSpeech: string;
  meaning: string;
  question: string | null;
  options: PracticeOption[];
  correctAnswer: string[];
}

export interface AnswerRequest {
  wordId: number;
  practiceId: number;
  selectedOptionIds: string[];
}

export interface AnswerResponse {
  correct: boolean;
  correctAnswer: string[];
  newLearningTracking: string;
}

export interface PracticeStatBucket {
  correct: number;
  incorrect: number;
  total: number;
}

export interface PracticeStats {
  today: PracticeStatBucket;
  thisWeek: PracticeStatBucket;
  thisMonth: PracticeStatBucket;
}

export const getPractices = async (): Promise<PracticeItem[]> => {
  const response = await request.get({ path: ENDPOINT.PRACTICES });
  return response.data.data;
};

export const answerPractice = async (
  body: AnswerRequest,
): Promise<AnswerResponse> => {
  const response = await request.post({
    path: `${ENDPOINT.PRACTICES}/answer`,
    body,
  });
  return response.data.data;
};

export const getPracticeStats = async (): Promise<PracticeStats> => {
  const response = await request.get({ path: `${ENDPOINT.PRACTICES}/stats` });
  return response.data.data;
};
