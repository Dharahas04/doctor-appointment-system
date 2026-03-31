export const SESSION_STORAGE_KEY = "doctor-appointment.session";

export function readSession() {
  const storedSession = window.localStorage.getItem(SESSION_STORAGE_KEY);
  if (!storedSession) {
    return null;
  }

  try {
    return JSON.parse(storedSession);
  } catch {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return null;
  }
}

export function writeSession(session) {
  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
}

export function clearSession() {
  window.localStorage.removeItem(SESSION_STORAGE_KEY);
}

export function getAuthToken() {
  return readSession()?.token || null;
}
