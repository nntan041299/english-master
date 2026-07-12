import { request, ENDPOINT } from "@/rest";

export interface WordMeaning {
  id: number;
  partOfSpeech: string;
  meaning: string;
  ipa?: string;
}

export interface WordItem {
  id: number;
  text: string;
  learningLevel: string;
  meanings: WordMeaning[];
}

export interface WordPage {
  content: WordItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface DashboardStats {
  totalWords: number;
  newWords: number;
  learningWords: number;
  familiarWords: number;
  masteredWords: number;
  practicesDone: number;
}

export const getDashboard = async (): Promise<DashboardStats> => {
  const response = await request.get({ path: ENDPOINT.WORDS_DASHBOARD });
  return response.data.data;
};

export const createWord = async (text: string): Promise<WordItem> => {
  const response = await request.post({
    path: ENDPOINT.WORDS,
    body: { text },
  });
  return response.data.data;
};

export const getWords = async (params: {
  keyword?: string;
  page?: number;
  size?: number;
}): Promise<WordPage> => {
  const query = new URLSearchParams();
  if (params.keyword) query.set("keyword", params.keyword);
  query.set("page", String(params.page ?? 0));
  query.set("size", String(params.size ?? 10));
  query.set("sort", "createdAt,desc");
  const response = await request.get({
    path: `${ENDPOINT.WORDS}?${query.toString()}`,
  });
  return response.data.data;
};
