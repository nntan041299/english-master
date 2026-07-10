import { useState } from "react";
import { FaApple, FaFacebookF, FaGithub, FaGoogle } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import type { AxiosError } from "axios";
import Loading from "@/components/Loading";
import { getGoogleLoginUrl } from "@/service/auth";
import { useRegister } from "@/hook/useRegister";

interface SignupErrors {
  fullName: string;
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

const SignUp = () => {
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [signupErrors, setSignupErrors] = useState<SignupErrors>({
    fullName: "",
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const { mutate: register, isPending, isError, error } = useRegister();

  function signupValidate(
    values: {
      fullName: string;
      username: string;
      email: string;
      password: string;
      confirmPassword: string;
    } = { fullName, username, email, password, confirmPassword },
  ): boolean {
    const next: SignupErrors = {
      fullName: "",
      username: "",
      email: "",
      password: "",
      confirmPassword: "",
    };
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!values.fullName.trim()) next.fullName = "Required";
    if (!values.username.trim()) next.username = "Required";
    if (!values.email.trim()) next.email = "Required";
    else if (!emailPattern.test(values.email.trim()))
      next.email = "Invalid email";
    if (!values.password) next.password = "Required";
    if (!values.confirmPassword) next.confirmPassword = "Required";
    if (
      values.password &&
      values.confirmPassword &&
      values.password !== values.confirmPassword
    ) {
      next.confirmPassword = "Passwords do not match";
    }

    setSignupErrors(next);
    return Object.values(next).every((v) => !v);
  }

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!signupValidate()) return;
    register({ fullName, username, email, password });
  }

  const clearError = (key: keyof SignupErrors) => {
    if (signupErrors[key]) setSignupErrors((prev) => ({ ...prev, [key]: "" }));
  };

  function handleSocialLogin(provider: string) {
    if (provider === "google") window.location.href = getGoogleLoginUrl();
  }

  const apiError = error as AxiosError<{ data: string }> | null;

  return (
    <div className="auth-screen">
      <Loading isLoading={isPending} />

      <div className="auth-card auth-card-wide">
        <div className="auth-card-header">
          <span className="auth-brand-name">
            English <span>Master</span>
          </span>
          <span className="auth-brand-pill">Vocabulary for life</span>
        </div>

        <div className="auth-card-body">
          <div className="mb-5">
            <h1 className="auth-heading">Start learning</h1>
            <p className="auth-subheading">Create your free account</p>
          </div>

          {isError && (
            <div className="alert-error mb-4">
              {apiError?.response?.data?.data ||
                "Server error, try again later"}
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-3">
            {/* Row 1: Full name + Username */}
            <div className="form-row">
              <label className="block">
                <span className="form-label">Full name</span>
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => {
                    setFullName(e.target.value);
                    clearError("fullName");
                  }}
                  onBlur={() => signupValidate()}
                  disabled={isPending}
                  placeholder="John Doe"
                  aria-invalid={!!signupErrors.fullName}
                  className={`form-input ${signupErrors.fullName ? "form-input-error" : ""}`}
                />
                {signupErrors.fullName && (
                  <small className="form-field-error">
                    {signupErrors.fullName}
                  </small>
                )}
              </label>

              <label className="block">
                <span className="form-label">Username</span>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => {
                    setUsername(e.target.value);
                    clearError("username");
                  }}
                  onBlur={() => signupValidate()}
                  disabled={isPending}
                  placeholder="johndoe"
                  aria-invalid={!!signupErrors.username}
                  className={`form-input ${signupErrors.username ? "form-input-error" : ""}`}
                />
                {signupErrors.username && (
                  <small className="form-field-error">
                    {signupErrors.username}
                  </small>
                )}
              </label>
            </div>

            {/* Row 2: Email */}
            <label className="block">
              <span className="form-label">Email</span>
              <input
                type="email"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  clearError("email");
                }}
                onBlur={() => signupValidate()}
                disabled={isPending}
                placeholder="you@example.com"
                aria-invalid={!!signupErrors.email}
                className={`form-input ${signupErrors.email ? "form-input-error" : ""}`}
              />
              {signupErrors.email && (
                <small className="form-field-error">{signupErrors.email}</small>
              )}
            </label>

            {/* Row 3: Password + Confirm */}
            <div className="form-row">
              <label className="block">
                <span className="form-label">Password</span>
                <div className="relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    value={password}
                    onChange={(e) => {
                      setPassword(e.target.value);
                      clearError("password");
                      clearError("confirmPassword");
                    }}
                    onBlur={() => signupValidate()}
                    disabled={isPending}
                    placeholder="••••••••"
                    aria-invalid={!!signupErrors.password}
                    className={`form-input pr-14 ${signupErrors.password ? "form-input-error" : ""}`}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((s) => !s)}
                    disabled={isPending}
                    className="input-toggle-btn"
                  >
                    {showPassword ? "Hide" : "Show"}
                  </button>
                </div>
                {signupErrors.password && (
                  <small className="form-field-error">
                    {signupErrors.password}
                  </small>
                )}
              </label>

              <label className="block">
                <span className="form-label">Confirm password</span>
                <div className="relative">
                  <input
                    type={showConfirmPassword ? "text" : "password"}
                    value={confirmPassword}
                    onChange={(e) => {
                      setConfirmPassword(e.target.value);
                      clearError("confirmPassword");
                    }}
                    onBlur={() => signupValidate()}
                    disabled={isPending}
                    placeholder="••••••••"
                    aria-invalid={!!signupErrors.confirmPassword}
                    className={`form-input pr-14 ${signupErrors.confirmPassword ? "form-input-error" : ""}`}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword((s) => !s)}
                    disabled={isPending}
                    className="input-toggle-btn"
                  >
                    {showConfirmPassword ? "Hide" : "Show"}
                  </button>
                </div>
                {signupErrors.confirmPassword && (
                  <small className="form-field-error">
                    {signupErrors.confirmPassword}
                  </small>
                )}
              </label>
            </div>

            <button disabled={isPending} className="btn-primary mt-1">
              {isPending ? "Creating account…" : "Create Account"}
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
              disabled={isPending}
              className="btn-social"
              aria-label="Continue with Google"
            >
              <FaGoogle size={14} />
            </button>
            <button
              type="button"
              onClick={() => handleSocialLogin("facebook")}
              disabled={isPending}
              className="btn-social"
              aria-label="Continue with Facebook"
            >
              <FaFacebookF size={14} />
            </button>
            <button
              type="button"
              onClick={() => handleSocialLogin("github")}
              disabled={isPending}
              className="btn-social"
              aria-label="Continue with GitHub"
            >
              <FaGithub size={14} />
            </button>
            <button
              type="button"
              onClick={() => handleSocialLogin("apple")}
              disabled={isPending}
              className="btn-social"
              aria-label="Continue with Apple"
            >
              <FaApple size={14} />
            </button>
          </div>

          <p className="auth-footer-text">
            Already have an account?{" "}
            <a className="auth-footer-link" onClick={() => navigate("/")}>
              Sign in
            </a>
          </p>
        </div>
      </div>
    </div>
  );
};

export default SignUp;
