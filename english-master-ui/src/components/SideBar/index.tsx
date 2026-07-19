import { useNavigate, useLocation } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import { signOut } from "@/service/auth";
import { useAuth } from "@/context/AuthProvider";

interface NavItem {
  path: string;
  label: string;
  icon: string;
}

const NAV_ITEMS: NavItem[] = [
  { path: "/", label: "Dashboard", icon: "pi-th-large" },
  { path: "/vocabulary", label: "Vocabulary", icon: "pi-book" },
  { path: "/practice", label: "Practice", icon: "pi-pencil" },
  { path: "/writing", label: "Writing", icon: "pi-file-edit" },
  { path: "/translation", label: "Translation", icon: "pi-language" },
];

interface SideBarProps {
  open?: boolean;
  onClose?: () => void;
}

export default function SideBar({ open, onClose }: SideBarProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { removeToken } = useAuth();

  const { mutate: handleSignOut, isPending } = useMutation({
    mutationFn: signOut,
    onSuccess: () => removeToken(),
    onError: () => removeToken(),
  });

  const handleNav = (path: string) => {
    navigate(path);
    onClose?.();
  };

  const sidebarContent = (
    <aside className="w-60 h-full flex flex-col flex-shrink-0 bg-ink-900 select-none">
      {/* Brand */}
      <div className="h-14 flex items-center justify-between px-5 border-b border-white/8 flex-shrink-0">
        <span
          className="text-lg font-bold tracking-tight text-parchment"
          style={{ fontFamily: "var(--font-display)" }}
        >
          English <span className="text-gold-500">Master</span>
        </span>
        {/* Close button — mobile only */}
        {onClose && (
          <button
            onClick={onClose}
            className="md:hidden text-white/50 hover:text-white transition-colors p-1 cursor-pointer"
            aria-label="Close menu"
          >
            <i className="pi pi-times text-base" />
          </button>
        )}
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        {NAV_ITEMS.map(({ path, label, icon }) => {
          const active = location.pathname === path;
          return (
            <button
              key={path}
              onClick={() => handleNav(path)}
              className={`
                w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium
                transition-colors duration-150 cursor-pointer text-left
                ${
                  active
                    ? "bg-white/10 text-white"
                    : "text-white/50 hover:bg-white/6 hover:text-white/80"
                }
              `}
              style={{ fontFamily: "var(--font-sans)" }}
            >
              <i
                className={`pi ${icon} text-sm ${active ? "text-gold-400" : ""}`}
              />
              {label}
            </button>
          );
        })}
      </nav>

      {/* Sign out */}
      <div className="px-3 py-4 border-t border-white/8">
        <button
          onClick={() => handleSignOut()}
          disabled={isPending}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium
                     text-white/40 hover:bg-white/6 hover:text-white/70
                     transition-colors duration-150 cursor-pointer
                     disabled:opacity-40 disabled:cursor-not-allowed"
          style={{ fontFamily: "var(--font-sans)" }}
        >
          <i className="pi pi-sign-out text-sm" />
          {isPending ? "Signing out…" : "Sign out"}
        </button>
      </div>
    </aside>
  );

  return (
    <>
      {/* Desktop sidebar */}
      <div className="hidden md:flex h-full">{sidebarContent}</div>

      {/* Mobile drawer */}
      {open !== undefined && (
        <>
          {/* Backdrop */}
          <div
            className={`
              fixed inset-0 z-40 bg-black/50 transition-opacity duration-300 md:hidden
              ${open ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"}
            `}
            onClick={onClose}
          />
          {/* Drawer */}
          <div
            className={`
              fixed inset-y-0 left-0 z-50 flex h-full transition-transform duration-300 md:hidden
              ${open ? "translate-x-0" : "-translate-x-full"}
            `}
          >
            {sidebarContent}
          </div>
        </>
      )}
    </>
  );
}
