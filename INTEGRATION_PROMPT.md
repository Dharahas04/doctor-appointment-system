# Backend Utilization Prompt

Use this prompt with Codex/ChatGPT or another coding agent when you want it to continue building on this repo:

```text
Work inside /Users/saidharahasrao/doctor-appointment-system.

This repository has:
- React + Vite frontend in /Users/saidharahasrao/doctor-appointment-system/frontend
- Spring Boot backend in /Users/saidharahasrao/doctor-appointment-system/doctor-appointment

Backend rules:
- Use the existing Spring Boot API instead of mocking data.
- MySQL is the source of truth.
- Redis is used for cache/locking, especially appointment booking locks.
- Preserve the current booking flow with idempotency and slot locking.

Important API endpoints already in the backend:
- POST /api/auth/register
- POST /api/auth/login
- GET /api/specialties
- POST /api/specialties
- GET /api/doctors?specialtyId=&mode=
- POST /api/doctors
- GET /api/slots?doctorId=&status=
- POST /api/slots
- POST /api/appointments/reserve?slotId=&patientId=
- DELETE /api/appointments/reserve?slotId=&reservationToken=
- POST /api/appointments/confirm?slotId=&patientId=&reservationToken=&mode= with Idempotency-Key header
- GET /api/appointments?patientId=&doctorId=
- POST /api/appointments?slotId=&patientId= with Idempotency-Key header
- PUT /api/appointments/status?appointmentId=&status=
- GET /api/reports/daily-summary

Implementation expectations:
- Reuse the real backend in this folder and connect the frontend to it.
- Keep API base URL configurable with VITE_API_BASE_URL and default it to http://localhost:8080.
- Do not replace backend calls with local dummy arrays.
- Preserve or improve current hackathon features:
  specialties, doctor discovery, mode filter, patient register/login, slot reservation, booking confirmation, notification preview, history, admin setup, reporting, lifecycle updates.
- Keep the frontend demo-friendly: it should let someone create seed data from the UI if the database is empty.
- If you add auth UI, align it with the backend that currently exists instead of inventing JWT flows that are not implemented yet.
- Prefer safe incremental changes and do not revert unrelated local edits.

Before finishing:
- Verify the frontend builds.
- Verify backend tests or compilation if possible.
- Summarize any remaining gaps between the current backend and the ideal judge-ready architecture.
```
