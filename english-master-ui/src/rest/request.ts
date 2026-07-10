import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import { API_BASE_URL } from "@/config/serverApiConfig";
import { ENDPOINT } from "./endpoint";

interface RequestParams {
  path: string;
  body?: Record<string, unknown>;
  headers?: Record<string, string>;
}

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

interface RefreshTokenResponse {
  data: {
    accessToken: string;
    refreshToken: string;
    tokenType?: string;
  };
}

axios.defaults.baseURL = API_BASE_URL;
axios.defaults.headers.common["Content-Type"] = "application/json";
axios.defaults.headers.common["Accept"] = "application/json";
axios.defaults.withCredentials = false;

export function setAuthHeader(token: string | null): void {
  if (token) {
    axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  } else {
    delete axios.defaults.headers.common["Authorization"];
  }
}

// Apply whatever token is already in storage on module load (page refresh).
setAuthHeader(localStorage.getItem("token"));

let isRefreshing = false;
let pendingRequests: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

function resolvePendingRequests(token: string): void {
  pendingRequests.forEach(({ resolve }) => resolve(token));
  pendingRequests = [];
}

function rejectPendingRequests(error: unknown): void {
  pendingRequests.forEach(({ reject }) => reject(error));
  pendingRequests = [];
}

function clearSession(): void {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  setAuthHeader(null);
  // Hard redirect so the whole app re-mounts and reads the cleared auth state.
  window.location.href = "/";
}

axios.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;

    const isRefreshCall = originalRequest?.url?.includes(
      ENDPOINT.REFRESH_TOKEN,
    );

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      isRefreshCall
    ) {
      if (isRefreshCall) clearSession();
      return Promise.reject(error);
    }

    const storedRefreshToken = localStorage.getItem("refreshToken");
    if (!storedRefreshToken) {
      clearSession();
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingRequests.push({
          resolve: (token: string) => {
            originalRequest.headers.set("Authorization", `Bearer ${token}`);
            resolve(axios(originalRequest));
          },
          reject,
        });
      });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const { data } = await axios.post<RefreshTokenResponse>(
        ENDPOINT.REFRESH_TOKEN,
        {
          refreshToken: storedRefreshToken,
        },
      );

      const { accessToken, refreshToken } = data.data;
      localStorage.setItem("token", accessToken);
      localStorage.setItem("refreshToken", refreshToken);
      setAuthHeader(accessToken);

      resolvePendingRequests(accessToken);

      originalRequest.headers.set("Authorization", `Bearer ${accessToken}`);
      return axios(originalRequest);
    } catch (refreshError) {
      rejectPendingRequests(refreshError);
      clearSession();
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

export const request = {
  get: async ({ path }: Pick<RequestParams, "path">) => {
    return await axios.get(path);
  },

  post: async ({ path, body, headers }: RequestParams) => {
    return await axios.post(path, body, { headers });
  },

  put: async ({ path, body, headers }: RequestParams) => {
    return await axios.put(path, body, { headers });
  },

  delete: async ({ path, body, headers }: RequestParams) => {
    return await axios.delete(path, { headers, data: body });
  },
};
