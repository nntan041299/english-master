import { useState } from "react";
import { FaApple, FaFacebookF, FaGithub, FaGoogle } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import type { AxiosError } from "axios";

import Loading from "@/components/Loading";
import { useLogin } from "@/hook/useLogin";
import { getGoogleLoginUrl } from "@/service/auth";

interface LoginErrors {
  username: string;
  password: string;
}

const LoginPage = () => {
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const [loginErrors, setLoginErrors] = useState<LoginErrors>({
    username: "",
    password: "",
  });

  const { mutate: login, isPending, isError, error } = useLogin();

  function loginValidate(
    values: { username: string; password: string } = { username, password },
  ): boolean {
    const next: LoginErrors = { username: "", password: "" };
    if (!values.username.trim()) next.username = "Please fill in the username";
    if (!values.password) next.password = "Please fill in the password";
    setLoginErrors(next);
    return !next.username && !next.password;
  }

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!loginValidate()) return;
    login({ username, password });
  }

  function handleSocialLogin(provider: string) {
    if (provider === "google") window.location.href = getGoogleLoginUrl();
  }

  const apiError = error as AxiosError<{ data: string }> | null;

  return (
    <div className="auth-screen">
      <Loading isLoading={isPending} />

      <div className="auth-card">
        <div className="auth-card-header">
          <span className="auth-brand-name">
            English <span>Master</span>
          </span>
          <span className="auth-brand-pill">Vocabulary for life</span>
        </div>

        <div className="auth-card-body">
          <div className="mb-5">
            <h1 className="auth-heading">Welcome back</h1>
            <p className="auth-subheading">Sign in to continue learning</p>
          </div>

          {isError && (
            <div className="alert-error mb-4">
              {apiError?.response?.data?.data ||
                "Server error, try again later"}
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-3">
            <label className="block">
              <span className="form-label">Email / Username</span>
              <input
                type="text"
                value={username}
                onChange={(e) => {
                  setUsername(e.target.value);
                  if (loginErrors.username)
                    setLoginErrors((p) => ({ ...p, username: "" }));
                }}
                onBlur={() => loginValidate({ username, password })}
                disabled={isPending}
                placeholder="you@example.com"
                aria-invalid={!!loginErrors.username}
                className={`form-input ${loginErrors.username ? "form-input-error" : ""}`}
              />
              {loginErrors.username && (
                <small className="form-field-error">
                  {loginErrors.username}
                </small>
              )}
            </label>

            <label className="block">
              <span className="form-label">Password</span>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    if (loginErrors.password)
                      setLoginErrors((p) => ({ ...p, password: "" }));
                  }}
                  onBlur={() => loginValidate({ username, password })}
                  disabled={isPending}
                  placeholder="••••••••"
                  aria-invalid={!!loginErrors.password}
                  className={`form-input pr-14 ${loginErrors.password ? "form-input-error" : ""}`}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((p) => !p)}
                  className="input-toggle-btn"
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>
              {loginErrors.password && (
                <small className="form-field-error">
                  {loginErrors.password}
                </small>
              )}
            </label>

            <button disabled={isPending} className="btn-primary mt-1">
              {isPending ? "Signing in…" : "Sign In"}
            </button>
          </form>

          <div className="auth-divider">
            <span className="auth-divider-line" />
            <em className="auth-divider-label">or</em>
            <span className="auth-divider-line" />
          </div>

          <div className="flex justify-center gap-2">
            <button
              type="button"
              onClick={() => handleSocialLogin("google")}
              className="btn-social"
              aria-label="Continue with Google"
            >
              <FaGoogle size={14} />
            </button>
            <button
              type="button"
              onClick={() => handleSocialLogin("facebook")}
              className="btn-social"
              aria-label="Continue with Facebook"
            >
              <FaFacebookF size={14} />
            </button>
            <button
              type="button"
              onClick={() => handleSocialLogin("github")}
              className="btn-social"
              aria-label="Continue with GitHub"
            >
              <FaGithub size={14} />
            </button>
            <button
              type="button"
              onClick={() => handleSocialLogin("apple")}
              className="btn-social"
              aria-label="Continue with Apple"
            >
              <FaApple size={14} />
            </button>
          </div>

          <p className="auth-footer-text">
            New here?{" "}
            <span
              className="auth-footer-link"
              onClick={() => navigate("/signup")}
            >
              Create account
            </span>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
