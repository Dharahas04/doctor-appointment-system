# Doctor Appointment Frontend

This Vite app now connects to the Spring Boot backend in the sibling folder:

- Frontend: `/Users/saidharahasrao/doctor-appointment-system/frontend`
- Backend: `/Users/saidharahasrao/doctor-appointment-system/doctor-appointment`

## What works

- Browse specialties and doctors from the live API
- Filter doctors by specialty and mode
- Register a patient account with `/api/auth/register`
- Log in as a patient with `/api/auth/login`
- View available slots for a selected doctor
- Reserve a slot temporarily with Redis-backed locking
- Confirm an appointment with an `Idempotency-Key`
- View mode-specific notification details after confirmation
- View appointment history and cancel confirmed appointments
- Create specialties, doctors, and slots from the admin console
- Update appointment lifecycle state from the admin console
- View daily operational metrics from `/api/reports/daily-summary`

## Frontend environment

Create `.env` from `.env.example` if you want to override the backend URL:

```bash
cp .env.example .env
```

Default:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Run locally

1. Start MySQL and Redis locally.
2. Start the backend:

```bash
cd /Users/saidharahasrao/doctor-appointment-system/doctor-appointment
./mvnw spring-boot:run
```

3. Start the frontend in a second terminal:

```bash
cd /Users/saidharahasrao/doctor-appointment-system/frontend
npm install
npm run dev
```

4. Open the Vite URL, usually `http://localhost:5173`.

## Demo flow

1. Open `Admin Console`
2. Create one or more specialties
3. Add doctors, assign a mode, and set a clinic address if needed
4. Generate slots for those doctors
5. Open `Patient Flow`
6. Register a patient account or log in
7. Filter doctors, reserve a slot, and confirm the booking
8. Review notification details, appointment history, and reporting updates
