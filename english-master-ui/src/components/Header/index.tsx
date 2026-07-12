import { useRef, useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { selectUser } from "@/redux/user/selectors";
import { useClickOutside } from "@/hook/useClickOutside";

interface HeaderProps {
  onMenuToggle?: () => void;
}

export default function Header({ onMenuToggle }: HeaderProps) {
  const { firstName, lastName, avatarUrl } = useSelector(selectUser);
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  useClickOutside(dropdownRef, () => setOpen(false));

  const initials =
    firstName && lastName
      ? `${firstName[0]}${lastName[0]}`.toUpperCase()
      : firstName
        ? firstName[0].toUpperCase()
        : "?";

  const fullName = [firstName, lastName].filter(Boolean).join(" ") || "User";

  const handleAccount = () => {
    setOpen(false);
    navigate("/account");
  };

  return (
    <header className="h-14 bg-white border-b border-surface-200 flex items-center justify-between px-4 md:px-6 flex-shrink-0">
      {/* Hamburger — mobile only */}
      <button
        onClick={onMenuToggle}
        className="md:hidden flex items-center justify-center w-9 h-9 rounded-lg text-surface-600
                   hover:bg-surface-100 transition-colors duration-150 cursor-pointer focus:outline-none"
        aria-label="Open menu"
      >
        <i className="pi pi-bars text-base" />
      </button>

      {/* Spacer so avatar stays right on desktop */}
      <div className="hidden md:block flex-1" />

      <div className="relative" ref={dropdownRef}>
        <button
          onClick={() => setOpen((prev) => !prev)}
          className="flex items-center justify-center w-9 h-9 rounded-full overflow-hidden
                     bg-ink-900 text-parchment text-sm font-semibold
                     ring-2 ring-transparent hover:ring-gold-500/40
                     transition-all duration-150 cursor-pointer focus:outline-none"
          style={{ fontFamily: "var(--font-sans)" }}
          aria-label="User menu"
        >
          {avatarUrl ? (
            <img
              src={avatarUrl}
              alt="avatar"
              className="w-full h-full object-cover"
            />
          ) : (
            initials
          )}
        </button>

        {open && (
          <div className="absolute right-0 mt-2 w-52 bg-white border border-surface-200 rounded-xl shadow-lg z-50 py-1 overflow-hidden">
            <div className="px-4 py-3 border-b border-surface-100">
              <p
                className="text-sm font-semibold text-surface-900 truncate"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {fullName}
              </p>
            </div>
            <div className="py-1">
              <button
                onClick={handleAccount}
                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-surface-700
                           hover:bg-surface-50 transition-colors duration-150 cursor-pointer text-left"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                <i className="pi pi-user text-sm text-surface-400" />
                Account settings
              </button>
            </div>
          </div>
        )}
      </div>
    </header>
  );
}
