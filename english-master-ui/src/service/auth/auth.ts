import type { LanguageLevel } from "@/constants/languageLevel";
import { API_BASE_URL } from "@/config/serverApiConfig";
import { request, ENDPOINT } from "@/rest";

interface LoginParams {
  username: string;
  password: string;
}

interface GoogleTokenParams {
  code: string;
  state: string;
}

interface SignUpParams {
  username: string;
  email: string;
  password: string;
  fullName: string;
  languageLevel: LanguageLevel;
}

export const login = async ({ username, password }: LoginParams) => {
  return request.post({
    path: API_BASE_URL + ENDPOINT.LOGIN,
    body: { username, password },
  });
};

export const getGoogleLoginUrl = (): string => {
  return API_BASE_URL + ENDPOINT.GOOGLE_LOGIN;
};

export const getGoogleToken = async ({ code, state }: GoogleTokenParams) => {
  return request.get({
    path:
      API_BASE_URL +
      ENDPOINT.GOOGLE_LOGIN_GET_TOKEN +
      "?code=" +
      code +
      "&state=" +
      state,
  });
};

export const signUp = async ({
  username,
  email,
  password,
  fullName,
  languageLevel,
}: SignUpParams) => {
  return await request.post({
    path: API_BASE_URL + ENDPOINT.SIGN_UP,
    body: { username, email, password, fullName, languageLevel },
  });
};

export const signOut = async () => {
  return await request.post({
    path: API_BASE_URL + ENDPOINT.SIGN_OUT,
  });
};
