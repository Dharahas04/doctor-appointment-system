export function normalizeBookingMode(doctorMode) {
  const normalizedDoctorMode = String(doctorMode || "OFFLINE").toUpperCase();
  if (normalizedDoctorMode === "BOTH") {
    return "ONLINE";
  }
  return normalizedDoctorMode;
}

export function appointmentSupportLine(mode, doctor, appointmentId) {
  const normalizedMode = String(mode || doctor?.mode || "OFFLINE").toUpperCase();
  if (normalizedMode === "ONLINE") {
    return appointmentId
      ? `Video consultation link is ready for appointment #${appointmentId}.`
      : "A video consultation link will be shared after confirmation.";
  }
  return `Clinic address: ${doctor?.clinicAddress || "PulsePoint Clinic, MG Road, Bengaluru"}`;
}

export function getAvailableDates(slots) {
  return [...new Set(slots.map((slot) => slotDateKey(slot.startTime)))];
}

export function slotDateKey(value) {
  return value ? String(value).slice(0, 10) : "";
}

export function formatDateBadge(dateKey) {
  if (!dateKey) {
    return "No date";
  }
  const date = new Date(`${dateKey}T00:00:00`);
  return new Intl.DateTimeFormat("en-IN", {
    weekday: "short",
    day: "numeric",
    month: "short",
  }).format(date);
}

export function formatTimeRange(startValue, endValue) {
  if (!startValue || !endValue) {
    return "Time unavailable";
  }
  const formatter = new Intl.DateTimeFormat("en-IN", {
    hour: "numeric",
    minute: "2-digit",
  });
  return `${formatter.format(new Date(startValue))} - ${formatter.format(new Date(endValue))}`;
}

export function formatDateTime(value) {
  if (!value) {
    return "Time unavailable";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function humanizeStatus(value) {
  return String(value || "UNKNOWN")
    .toLowerCase()
    .split("_")
    .map((chunk) => chunk.charAt(0).toUpperCase() + chunk.slice(1))
    .join(" ");
}

export function sessionFromAuth(response) {
  return {
    userId: response.userId,
    fullName: response.fullName,
    email: response.email,
    role: response.role,
    token: response.token,
    dashboardPath: response.dashboardPath,
  };
}
