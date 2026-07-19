import type { LanguageLevel } from "@/constants/languageLevel";
import { request, ENDPOINT } from "@/rest";

export const getUserInfo = async () => {
  return request.get({ path: ENDPOINT.USERS });
};

export interface UpdateUserPayload {
  email?: string;
  fullName?: string;
  languageLevel?: LanguageLevel;
  currentPassword?: string;
  newPassword?: string;
}

export const updateUserInfo = async (payload: UpdateUserPayload) => {
  return request.put({
    path: ENDPOINT.USERS,
    body: payload as Record<string, unknown>,
  });
};
