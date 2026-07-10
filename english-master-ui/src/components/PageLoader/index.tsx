const PageLoader = () => {
  return (
    <div
      className="min-h-screen flex flex-col items-center justify-center gap-4 bg-ink-950"
      style={{
        backgroundImage: `
          radial-gradient(ellipse at 75% 15%, color-mix(in srgb, var(--color-sage-600) 12%, transparent) 0%, transparent 55%),
          radial-gradient(ellipse at 15% 85%, color-mix(in srgb, var(--color-gold-500) 8%, transparent) 0%, transparent 50%)
        `,
      }}
    >
      <span
        className="font-display text-xl font-bold tracking-tight text-parchment mb-2"
        style={{ fontFamily: "var(--font-display)" }}
      >
        English <span className="text-gold-500">Master</span>
      </span>

      <div className="relative w-10 h-10">
        <span className="absolute inset-0 rounded-full border-2 border-white/10" />
        <span className="absolute inset-0 rounded-full border-2 border-transparent border-t-gold-500 animate-spin" />
      </div>
    </div>
  );
};

export default PageLoader;
