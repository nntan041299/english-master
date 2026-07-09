import { createContext, useContext, useEffect, useMemo, useState, ReactNode } from 'react';
import store from '@/redux/store';
import { RESET_STORE } from '@/redux/rootReducer';
import { setAuthHeader } from '@/rest/request';

interface AuthContextType {
  token: string | null;
  addToken: (accessToken: string, refreshToken?: string) => void;
  removeToken: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));

  useEffect(() => {
    setAuthHeader(token);
  }, [token]);

  const addToken = (accessToken: string, refreshToken?: string): void => {
    localStorage.setItem('token', accessToken);
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
    setAuthHeader(accessToken);
    setToken(accessToken);
  };

  const removeToken = (): void => {
    localStorage.clear();
    store.dispatch({ type: RESET_STORE });
    setToken(null);
  };

  const value = useMemo(() => ({ token, addToken, removeToken }), [token]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
