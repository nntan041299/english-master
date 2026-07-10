import { request, ENDPOINT } from "@/rest";

export interface PracticeItem {
  wordId: number;
  word: string;
  level: string;
  practiceId: number;
  partOfSpeech: string;
  meaning: string;
  option1: string;
  option2: string;
  option3: string;
  option4: string;
  correctAnswer: "OPTION_1" | "OPTION_2" | "OPTION_3" | "OPTION_4";
}

export interface AnswerRequest {
  wordId: number;
  practiceId: number;
  selectedOption: string;
}

export interface AnswerResponse {
  correct: boolean;
  correctAnswer: "OPTION_1" | "OPTION_2" | "OPTION_3" | "OPTION_4";
  newLevel: string;
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
