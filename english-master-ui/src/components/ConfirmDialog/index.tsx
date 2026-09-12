import { useEffect } from "react";

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: "danger" | "default";
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmDialog = ({
  open,
  title,
  description,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  variant = "default",
  loading = false,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) => {
  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !loading) onCancel();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [open, loading, onCancel]);

  if (!open) return null;

  const isDanger = variant === "danger";

  return (
    <div
      className="fixed inset-0 z-[60] flex items-center justify-center bg-ink-950/60 backdrop-blur-sm p-4"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && !loading) onCancel();
      }}
    >
      <div
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        className="rise-in w-full max-w-sm bg-white rounded-2xl border border-surface-200 shadow-xl p-6"
      >
        <div className="flex items-start gap-3.5">
          <div
            className={`w-10 h-10 shrink-0 rounded-full flex items-center justify-center ${
              isDanger ? "bg-red-50" : "bg-surface-100"
            }`}
          >
            <i
              className={`pi ${isDanger ? "pi-exclamation-triangle text-red-600" : "pi-question-circle text-surface-500"} text-base`}
            />
          </div>
          <div className="flex flex-col gap-1 pt-0.5">
            <h2
              id="confirm-dialog-title"
              className="text-base font-bold text-surface-900"
              style={{ fontFamily: "var(--font-display)" }}
            >
              {title}
            </h2>
            {description && (
              <p
                className="text-sm text-surface-500 leading-snug"
                style={{ fontFamily: "var(--font-sans)" }}
              >
                {description}
              </p>
            )}
          </div>
        </div>

        <div className="flex items-center justify-end gap-2 mt-6">
          <button
            type="button"
            onClick={onCancel}
            disabled={loading}
            className="px-4 py-2 rounded-lg text-sm font-semibold text-surface-600 bg-white
                       border border-surface-200 hover:bg-surface-50 disabled:opacity-40
                       disabled:cursor-not-allowed transition-colors cursor-pointer"
            style={{ fontFamily: "var(--font-sans)" }}
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={loading}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold text-parchment
                       border-none disabled:opacity-50 disabled:cursor-not-allowed transition-colors
                       cursor-pointer ${
                         isDanger
                           ? "bg-red-600 hover:bg-red-700"
                           : "bg-ink-900 hover:bg-ink-800"
                       }`}
            style={{ fontFamily: "var(--font-sans)" }}
          >
            {loading && <i className="pi pi-spin pi-spinner text-sm" />}
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmDialog;
