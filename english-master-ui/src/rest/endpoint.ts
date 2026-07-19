export const ENDPOINT = {
  LOGIN: "english-master-service/api/auth/login",
  GOOGLE_LOGIN: "english-master-service/api/auth/google/login",
  GOOGLE_LOGIN_GET_TOKEN: "english-master-service/api/auth/google/getToken",
  USERS: "english-master-service/api/users/current-user",
  SIGN_UP: "english-master-service/api/auth/register",
  SIGN_OUT: "english-master-service/api/auth/logout",
  REFRESH_TOKEN: "english-master-service/api/auth/refresh",
  WORDS: "english-master-service/api/words",
  WORDS_DASHBOARD: "english-master-service/api/words/dashboard",
  PRACTICES: "english-master-service/api/practices",
  WRITING: "english-master-service/api/writing",
} as const;
