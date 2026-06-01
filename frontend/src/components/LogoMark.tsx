interface Props {
  size?: number;
  className?: string;
}

export function LogoMark({ size = 32, className = '' }: Props) {
  // All three signal arcs are true concentric circular arcs
  // centered on the dot at (24, 42), spanning ~90° each side of vertical.
  // Endpoints: (24 ± r·sin45°, 42 − r·cos45°)  →  (24 ± r/√2, 42 − r/√2)
  const r1 = 6, r2 = 12, r3 = 18;
  const sin45 = 0.7071;

  const arc = (r: number) => {
    const dx = r * sin45;
    const dy = r * sin45;
    const x1 = +(24 - dx).toFixed(2);
    const y1 = +(42 - dy).toFixed(2);
    const x2 = +(24 + dx).toFixed(2);
    return `M ${x1} ${y1} A ${r} ${r} 0 0 0 ${x2} ${y1}`;
  };

  return (
    <svg
      width={size}
      height={size * (44 / 48)}
      viewBox="0 0 48 44"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      {/* Cloud */}
      <path
        d="M9 23C5.7 23 3 20.3 3 17C3 13.7 5.7 11 9 11C9.4 11 9.7 11 10 11.1C10.6 7 14.1 4 18.5 4C20.8 4 22.9 4.9 24.5 6.4C26 4.3 28.4 3 31 3C35.4 3 39 6.4 39.4 10.7C42.1 11.5 44 14 44 17C44 20.3 41.3 23 38 23H9Z"
        fill="white"
      />

      {/* Signal dot */}
      <circle cx="24" cy="42" r="2.2" fill="#c2ff50" />

      {/* Arc 1 — smallest */}
      <path d={arc(r1)} stroke="#c2ff50" strokeWidth="2.2" strokeLinecap="round" fill="none" />

      {/* Arc 2 — medium */}
      <path d={arc(r2)} stroke="#c2ff50" strokeWidth="2.2" strokeLinecap="round" fill="none" />

      {/* Arc 3 — largest */}
      <path d={arc(r3)} stroke="#c2ff50" strokeWidth="2.2" strokeLinecap="round" fill="none" />
    </svg>
  );
}
