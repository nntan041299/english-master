import { request, ENDPOINT } from '@/rest';

export interface WordMeaning {
  id: number;
  partOfSpeech: string;
  meaning: string;
}

export interface WordItem {
  id: number;
  text: string;
  level: string;
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
  if (params.keyword) query.set('keyword', params.keyword);
  query.set('page', String(params.page ?? 0));
  query.set('size', String(params.size ?? 10));
  query.set('sort', 'id');
  const response = await request.get({
    path: `${ENDPOINT.WORDS}?${query.toString()}`,
  });
  return response.data.data;
};
