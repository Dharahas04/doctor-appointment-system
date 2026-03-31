import { humanizeStatus } from "../../utils/formatters.js";

function StatusPill({ value }) {
  const normalizedValue = String(value || "UNKNOWN").toUpperCase();
  return (
    <span className={`status-pill status-${normalizedValue.toLowerCase()}`}>
      {humanizeStatus(normalizedValue)}
    </span>
  );
}

export default StatusPill;
