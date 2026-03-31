import { getAuthToken } from "./lib/session.js";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");

async function request(path, options = {}) {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
    ...options,
  });

  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message =
      typeof payload === "string"
        ? payload
        : payload.message || payload.error || "Request failed.";
    throw new Error(message);
  }

  return payload;
}

function buildQuery(params = {}) {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "" && value !== "ALL") {
      search.append(key, value);
    }
  });

  const queryString = search.toString();
  return queryString ? `?${queryString}` : "";
}

export function getSpecialties() {
  return request("/api/specialties");
}

export function createSpecialty({ name }) {
  return request("/api/specialties", {
    method: "POST",
    body: JSON.stringify({ name }),
  });
}

export function getDoctors(filters = {}) {
  return request(`/api/doctors${buildQuery(filters)}`);
}

export function createDoctor({ clinicAddress, mode, name, specialtyId }) {
  return request("/api/doctors", {
    method: "POST",
    body: JSON.stringify({
      name,
      mode,
      clinicAddress,
      specialty: { id: Number(specialtyId) },
    }),
  });
}

export function getSlots(filters = {}) {
  return request(`/api/slots${buildQuery(filters)}`);
}

export function createSlot({ doctorId, endTime, startTime }) {
  return request("/api/slots", {
    method: "POST",
    body: JSON.stringify({
      doctor: { id: Number(doctorId) },
      startTime,
      endTime,
      status: "AVAILABLE",
    }),
  });
}

export function registerPatientAccount({ email, fullName, password }) {
  return request("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({
      fullName,
      email,
      password,
    }),
  });
}

export function loginPatientAccount({ email, password }) {
  return request("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({
      email,
      password,
    }),
  });
}

export function reserveAppointment({ patientId, slotId }) {
  return request(
    `/api/appointments/reserve${buildQuery({
      patientId: Number(patientId),
      slotId: Number(slotId),
    })}`,
    {
      method: "POST",
    },
  );
}

export function releaseReservation({ reservationToken, slotId }) {
  return request(
    `/api/appointments/reserve${buildQuery({
      slotId: Number(slotId),
      reservationToken,
    })}`,
    {
      method: "DELETE",
    },
  );
}

export function confirmAppointment({ mode, patientId, reservationToken, slotId }) {
  return request(
    `/api/appointments/confirm${buildQuery({
      patientId: Number(patientId),
      reservationToken,
      slotId: Number(slotId),
      mode,
    })}`,
    {
      method: "POST",
    },
  );
}

export function getAppointments(filters = {}) {
  return request(`/api/appointments${buildQuery(filters)}`);
}

export function updateAppointmentStatus({ appointmentId, status }) {
  return request(
    `/api/appointments/status${buildQuery({
      appointmentId: Number(appointmentId),
      status,
    })}`,
    {
      method: "PUT",
    },
  );
}

export function getDailySummary() {
  return request("/api/reports/daily-summary");
}

export function getAnalyticsOverview() {
  return request("/api/reports/analytics");
}
