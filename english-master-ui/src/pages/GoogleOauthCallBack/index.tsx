import { ReactNode, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { FaGoogle } from "react-icons/fa";
import { useGoogleLoginCallBack } from "@/hook/useGoogleLoginCallBack";

function Screen({ children }: { children: ReactNode }) {
  return (
    <div
      className="min-h-screen flex items-center justify-center px-4 bg-ink-950"
      style={{
        backgroundImage: `
          radial-gradient(ellipse at 75% 15%, color-mix(in srgb, var(--color-sage-600) 12%, transparent) 0%, transparent 55%),
          radial-gradient(ellipse at 15% 85%, color-mix(in srgb, var(--color-gold-500) 8%, transparent) 0%, transparent 50%)
        `,
      }}
    >
      <div className="w-full max-w-sm text-center">
        <div className="mb-8">
          <span className="font-display text-2xl font-bold tracking-tight text-parchment">
            English <span className="text-gold-500">Master</span>
          </span>
        </div>
        {children}
      </div>
    </div>
  );
}

export default function OAuthCallback() {
  const navigate = useNavigate();
  const [params] = useSearchParams();

  const code = params.get("code");
  const state = params.get("state");
  const error = params.get("error");

  const { mutate: getToken, isError } = useGoogleLoginCallBack();

  useEffect(() => {
    if (error || !code || !state) return;
    getToken({ code, state });
  }, [code, state, error, getToken]);

  if (error || !code || !state || isError) {
    const message = error
      ? `Google sign-in was cancelled or denied.`
      : isError
        ? "Could not complete Google authentication. Please try again."
        : "Missing authorization information. Please try again.";

    return (
      <Screen>
        <div className="bg-white/5 border border-white/10 rounded-2xl px-8 py-10">
          <div className="w-12 h-12 rounded-full bg-error-500/10 border border-error-500/20 flex items-center justify-center mx-auto mb-5">
            <svg
              className="w-5 h-5 text-error-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={2}
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </div>
          <h1
            className="text-lg font-semibold text-white mb-2"
            style={{ fontFamily: "var(--font-display)" }}
          >
            Sign-in failed
          </h1>
          <p
            className="text-sm text-white/50 mb-8 leading-relaxed"
            style={{ fontFamily: "var(--font-sans)" }}
          >
            {message}
          </p>
          <button
            onClick={() => navigate("/", { replace: true })}
            className="btn-primary"
          >
            Back to sign in
          </button>
        </div>
      </Screen>
    );
  }

  return (
    <Screen>
      <div className="bg-white/5 border border-white/10 rounded-2xl px-8 py-10">
        {/* Google icon with pulse ring */}
        <div className="relative w-16 h-16 mx-auto mb-7">
          <span className="absolute inset-0 rounded-full animate-ping bg-sage-600/20" />
          <div className="relative w-16 h-16 rounded-full bg-white/10 border border-white/15 flex items-center justify-center">
            <FaGoogle className="text-white/80" size={22} />
          </div>
        </div>

        <h1
          className="text-xl font-bold text-white mb-2"
          style={{ fontFamily: "var(--font-display)" }}
        >
          Signing you in
        </h1>
        <p
          className="text-sm text-white/50 mb-8"
          style={{ fontFamily: "var(--font-sans)" }}
        >
          Securely connecting your Google account…
        </p>

        {/* Animated progress dots */}
        <div className="flex justify-center gap-1.5">
          {[0, 1, 2].map((i) => (
            <span
              key={i}
              className="w-1.5 h-1.5 rounded-full bg-sage-500 animate-bounce"
              style={{ animationDelay: `${i * 0.15}s` }}
            />
          ))}
        </div>
      </div>
    </Screen>
  );
}
