import { useState, useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useMutation } from "@tanstack/react-query";
import { selectUser } from "@/redux/user/selectors";
import { setUserInfo } from "@/redux/user";
import { updateUserInfo } from "@/service/user";
import Layout from "@/layouts/Layout";
import { AppDispatch } from "@/redux/store";

interface FormState {
  fullName: string;
  email: string;
}

interface PasswordFormState {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

function SectionCard({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className="bg-white rounded-2xl border border-surface-200 overflow-hidden">
      <div className="px-6 py-4 border-b border-surface-100">
        <h2
          className="text-sm font-semibold text-surface-700 uppercase tracking-widest"
          style={{ fontFamily: "var(--font-sans)" }}
        >
          {title}
        </h2>
      </div>
      <div className="px-6 py-5">{children}</div>
    </div>
  );
}

function Field({
  label,
  children,
  error,
}: {
  label: string;
  children: React.ReactNode;
  error?: string;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label
        className="text-sm font-medium text-surface-700"
        style={{ fontFamily: "var(--font-sans)" }}
      >
        {label}
      </label>
      {children}
      {error && (
        <p className="text-xs text-red-500" style={{ fontFamily: "var(--font-sans)" }}>
          {error}
        </p>
      )}
    </div>
  );
}

function Input(props: React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      {...props}
      className={`
        w-full px-3.5 py-2.5 rounded-xl border border-surface-200 text-sm text-surface-900
        bg-white placeholder:text-surface-400
        focus:outline-none focus:ring-2 focus:ring-ink-900/20 focus:border-ink-900/40
        disabled:bg-surface-50 disabled:text-surface-400
        transition-all duration-150
        ${props.className ?? ""}
      `}
      style={{ fontFamily: "var(--font-sans)" }}
    />
  );
}

function PasswordInput({
  value,
  onChange,
  placeholder,
  name,
}: {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder?: string;
  name: string;
}) {
  const [show, setShow] = useState(false);
  return (
    <div className="relative">
      <Input
        type={show ? "text" : "password"}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        className="pr-10"
      />
      <button
        type="button"
        onClick={() => setShow((p) => !p)}
        className="absolute right-3 top-1/2 -translate-y-1/2 text-surface-400 hover:text-surface-600 transition-colors cursor-pointer"
        tabIndex={-1}
      >
        <i className={`pi ${show ? "pi-eye-slash" : "pi-eye"} text-sm`} />
      </button>
    </div>
  );
}

const Account = () => {
  const dispatch = useDispatch<AppDispatch>();
  const user = useSelector(selectUser);

  const [profileForm, setProfileForm] = useState<FormState>({
    fullName: "",
    email: "",
  });
  const [profileError, setProfileError] = useState("");
  const [profileSuccess, setProfileSuccess] = useState("");

  const [passwordForm, setPasswordForm] = useState<PasswordFormState>({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [passwordError, setPasswordError] = useState("");
  const [passwordSuccess, setPasswordSuccess] = useState("");

  useEffect(() => {
    const fullName = [user.firstName, user.lastName].filter(Boolean).join(" ");
    setProfileForm({ fullName, email: user.email ?? "" });
  }, [user.firstName, user.lastName, user.email]);

  const { mutate: saveProfile, isPending: savingProfile } = useMutation({
    mutationFn: updateUserInfo,
    onSuccess: (res) => {
      const data = res.data.data;
      const parts = (data.fullName ?? "").trim().split(/\s+/);
      dispatch(
        setUserInfo({
          id: String(data.id),
          username: data.username,
          email: data.email,
          firstName: parts[0] ?? "",
          lastName: parts.slice(1).join(" ") || undefined,
          avatarUrl: user.avatarUrl,
        }),
      );
      setProfileSuccess("Profile updated.");
      setTimeout(() => setProfileSuccess(""), 3000);
    },
    onError: () => {
      setProfileError("Failed to update profile. Please try again.");
    },
  });

  const { mutate: savePassword, isPending: savingPassword } = useMutation({
    mutationFn: updateUserInfo,
    onSuccess: () => {
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      setPasswordSuccess("Password changed successfully.");
      setTimeout(() => setPasswordSuccess(""), 3000);
    },
    onError: () => {
      setPasswordError("Incorrect current password or request failed.");
    },
  });

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setProfileError("");
    setProfileSuccess("");
    setProfileForm((p) => ({ ...p, [e.target.name]: e.target.value }));
  };

  const handleProfileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setProfileError("");
    if (!profileForm.fullName.trim()) {
      setProfileError("Full name is required.");
      return;
    }
    saveProfile({
      fullName: profileForm.fullName.trim(),
      email: profileForm.email.trim() || undefined,
    });
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPasswordError("");
    setPasswordSuccess("");
    setPasswordForm((p) => ({ ...p, [e.target.name]: e.target.value }));
  };

  const handlePasswordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPasswordError("");
    if (!passwordForm.currentPassword) {
      setPasswordError("Current password is required.");
      return;
    }
    if (passwordForm.newPassword.length < 8) {
      setPasswordError("New password must be at least 8 characters.");
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordError("Passwords do not match.");
      return;
    }
    savePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    });
  };

  const fullName = [user.firstName, user.lastName].filter(Boolean).join(" ") || "User";
  const initials =
    user.firstName && user.lastName
      ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase()
      : user.firstName
        ? user.firstName[0].toUpperCase()
        : "?";

  return (
    <Layout>
      <div className="max-w-2xl mx-auto px-4 md:px-6 py-8 space-y-6">
        {/* Page header */}
        <div>
          <h1
            className="text-2xl font-bold text-surface-900"
            style={{ fontFamily: "var(--font-display)" }}
          >
            Account
          </h1>
          <p
            className="text-sm text-surface-500 mt-1"
            style={{ fontFamily: "var(--font-sans)" }}
          >
            Manage your profile and security settings.
          </p>
        </div>

        {/* Avatar + name banner */}
        <div className="bg-ink-900 rounded-2xl px-6 py-5 flex items-center gap-4">
          <div
            className="w-14 h-14 rounded-full overflow-hidden bg-white/10 flex items-center
                       justify-center text-parchment text-xl font-bold flex-shrink-0"
            style={{ fontFamily: "var(--font-sans)" }}
          >
            {user.avatarUrl ? (
              <img src={user.avatarUrl} alt="avatar" className="w-full h-full object-cover" />
            ) : (
              initials
            )}
          </div>
          <div>
            <p
              className="text-white font-semibold text-base"
              style={{ fontFamily: "var(--font-display)" }}
            >
              {fullName}
            </p>
            <p
              className="text-white/50 text-sm"
              style={{ fontFamily: "var(--font-sans)" }}
            >
              @{user.username}
            </p>
          </div>
        </div>

        {/* Profile section */}
        <SectionCard title="Profile">
          <form onSubmit={handleProfileSubmit} className="space-y-4">
            <Field label="Full name">
              <Input
                name="fullName"
                value={profileForm.fullName}
                onChange={handleProfileChange}
                placeholder="Your full name"
              />
            </Field>
            <Field label="Email" error={undefined}>
              <Input
                name="email"
                type="email"
                value={profileForm.email}
                onChange={handleProfileChange}
                placeholder="Leave blank to keep current email"
              />
            </Field>

            {profileError && (
              <p className="text-sm text-red-500" style={{ fontFamily: "var(--font-sans)" }}>
                {profileError}
              </p>
            )}
            {profileSuccess && (
              <p className="text-sm text-sage-600" style={{ fontFamily: "var(--font-sans)" }}>
                {profileSuccess}
              </p>
            )}

            <div className="flex justify-end pt-1">
              <button
                type="submit"
                disabled={savingProfile}
                className="px-5 py-2.5 rounded-xl bg-ink-900 text-parchment text-sm font-semibold
                           hover:bg-ink-800 transition-colors duration-150 cursor-pointer
                           disabled:opacity-40 disabled:cursor-not-allowed"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {savingProfile ? "Saving…" : "Save changes"}
              </button>
            </div>
          </form>
        </SectionCard>

        {/* Change password section */}
        <SectionCard title="Change password">
          <form onSubmit={handlePasswordSubmit} className="space-y-4">
            <Field label="Current password">
              <PasswordInput
                name="currentPassword"
                value={passwordForm.currentPassword}
                onChange={handlePasswordChange}
                placeholder="Current password"
              />
            </Field>
            <Field label="New password">
              <PasswordInput
                name="newPassword"
                value={passwordForm.newPassword}
                onChange={handlePasswordChange}
                placeholder="Min. 8 characters"
              />
            </Field>
            <Field label="Confirm new password">
              <PasswordInput
                name="confirmPassword"
                value={passwordForm.confirmPassword}
                onChange={handlePasswordChange}
                placeholder="Repeat new password"
              />
            </Field>

            {passwordError && (
              <p className="text-sm text-red-500" style={{ fontFamily: "var(--font-sans)" }}>
                {passwordError}
              </p>
            )}
            {passwordSuccess && (
              <p className="text-sm text-sage-600" style={{ fontFamily: "var(--font-sans)" }}>
                {passwordSuccess}
              </p>
            )}

            <div className="flex justify-end pt-1">
              <button
                type="submit"
                disabled={savingPassword}
                className="px-5 py-2.5 rounded-xl bg-ink-900 text-parchment text-sm font-semibold
                           hover:bg-ink-800 transition-colors duration-150 cursor-pointer
                           disabled:opacity-40 disabled:cursor-not-allowed"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {savingPassword ? "Saving…" : "Change password"}
              </button>
            </div>
          </form>
        </SectionCard>
      </div>
    </Layout>
  );
};

export default Account;
