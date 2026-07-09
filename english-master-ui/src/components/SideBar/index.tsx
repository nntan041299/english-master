import { useNavigate, useLocation } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { signOut } from '@/service/auth';
import { useAuth } from '@/context/AuthProvider';

interface NavItem {
  path: string;
  label: string;
  icon: string;
}

const NAV_ITEMS: NavItem[] = [
  { path: '/', label: 'Dashboard', icon: 'pi-th-large' },
  { path: '/vocabulary', label: 'Vocabulary', icon: 'pi-book' },
  { path: '/practice', label: 'Practice', icon: 'pi-pencil' },
];

export default function SideBar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { removeToken } = useAuth();

  const { mutate: handleSignOut, isPending } = useMutation({
    mutationFn: signOut,
    onSuccess: () => removeToken(),
    onError: () => removeToken(),
  });

  return (
    <aside className="w-60 h-full flex flex-col flex-shrink-0 bg-ink-900 select-none">
      {/* Brand */}
      <div className="h-14 flex items-center px-5 border-b border-white/8 flex-shrink-0">
        <span
          className="text-lg font-bold tracking-tight text-parchment"
          style={{ fontFamily: 'var(--font-display)' }}
        >
          English <span className="text-gold-500">Master</span>
        </span>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        {NAV_ITEMS.map(({ path, label, icon }) => {
          const active = location.pathname === path;
          return (
            <button
              key={path}
              onClick={() => navigate(path)}
              className={`
                w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium
                transition-colors duration-150 cursor-pointer text-left
                ${active
                  ? 'bg-white/10 text-white'
                  : 'text-white/50 hover:bg-white/6 hover:text-white/80'
                }
              `}
              style={{ fontFamily: 'var(--font-sans)' }}
            >
              <i className={`pi ${icon} text-sm ${active ? 'text-gold-400' : ''}`} />
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
          style={{ fontFamily: 'var(--font-sans)' }}
        >
          <i className="pi pi-sign-out text-sm" />
          {isPending ? 'Signing out…' : 'Sign out'}
        </button>
      </div>
    </aside>
  );
}
