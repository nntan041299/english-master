interface LoadingProps {
  isLoading: boolean;
}

const Loading = ({ isLoading }: LoadingProps) => {
  if (!isLoading) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink-950/60 backdrop-blur-sm">
      <div className="flex flex-col items-center gap-4">
        <div className="relative w-11 h-11">
          <span className="absolute inset-0 rounded-full border-2 border-white/10" />
          <span className="absolute inset-0 rounded-full border-2 border-transparent border-t-gold-500 animate-spin" />
        </div>
        <span
          className="text-xs font-medium tracking-widest uppercase text-white/40"
          style={{ fontFamily: "var(--font-sans)" }}
        >
          Loading
        </span>
      </div>
    </div>
  );
};

export default Loading;
