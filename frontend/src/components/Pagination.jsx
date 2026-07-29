export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, i) => i).filter(
    (p) => p === 0 || p === totalPages - 1 || Math.abs(p - page) <= 1
  );

  let lastRendered = -1;

  return (
    <div className="flex items-center gap-1">
      <button
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
        className="rounded-md border px-2.5 py-1.5 text-sm disabled:opacity-40"
      >
        Prev
      </button>
      {pages.map((p) => {
        const showEllipsis = p - lastRendered > 1;
        lastRendered = p;
        return (
          <span key={p} className="flex items-center">
            {showEllipsis && <span className="px-1 text-gray-400">…</span>}
            <button
              onClick={() => onPageChange(p)}
              className={`min-w-[2rem] rounded-md border px-2.5 py-1.5 text-sm ${
                p === page ? 'border-primary-600 bg-primary-600 text-white' : 'hover:bg-gray-50'
              }`}
            >
              {p + 1}
            </button>
          </span>
        );
      })}
      <button
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
        className="rounded-md border px-2.5 py-1.5 text-sm disabled:opacity-40"
      >
        Next
      </button>
    </div>
  );
}
