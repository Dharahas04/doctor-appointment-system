import EmptyState from "../components/ui/EmptyState.jsx";
import MetricCard from "../components/ui/MetricCard.jsx";
import SectionTitle from "../components/ui/SectionTitle.jsx";
import StatusPill from "../components/ui/StatusPill.jsx";
import {
  appointmentSupportLine,
  formatDateBadge,
  formatDateTime,
  formatTimeRange,
} from "../utils/formatters.js";

function PatientDashboardPage({
  availableDates,
  doctorSearch,
  isLoadingAppointments,
  isLoadingDoctors,
  isLoadingSlots,
  isSubmittingReservation,
  latestReceipt,
  onConfirmBooking,
  onLogout,
  onReleaseReservation,
  onReserve,
  onUpdateAppointmentStatus,
  patientAppointments,
  reservation,
  selectedDoctor,
  selectedMode,
  selectedSlot,
  selectedSlotDate,
  selectedSpecialtyId,
  session,
  setDoctorSearch,
  setReservation,
  setSelectedDoctorId,
  setSelectedMode,
  setSelectedSlotDate,
  setSelectedSlotId,
  setSelectedSpecialtyId,
  slotsForSelectedDate,
  specialties,
  stats,
  visibleDoctors,
}) {
  return (
    <section className="page-grid page-grid-booking">
      <article className="panel">
        <SectionTitle
          title="Quick Summary"
          subtitle="Your dashboard is now focused on booking and appointment tracking."
        />
        <div className="metric-grid">
          <MetricCard label="Doctors" value={stats.doctors} />
          <MetricCard label="Specialties" value={stats.specialties} />
          <MetricCard label="Your appointments" value={patientAppointments.length} />
          <MetricCard label="Confirmed today" value={stats.confirmed} />
        </div>
        <div className="session-card">
          <strong>{session.fullName}</strong>
          <p>{session.email}</p>
          <StatusPill value={session.role} />
          <button type="button" className="ghost-button" onClick={onLogout}>
            Logout
          </button>
        </div>
      </article>

      <article className="panel">
        <SectionTitle
          title="Browse Specialties & Doctors"
          subtitle="Filter by specialty and consultation mode before selecting a doctor."
        />
        <div className="filters stacked">
          <label>
            Search doctors
            <input
              value={doctorSearch}
              onChange={(event) => setDoctorSearch(event.target.value)}
              placeholder="Search by doctor, specialty, or mode"
            />
          </label>
          <label>
            Specialty
            <select
              value={selectedSpecialtyId}
              onChange={(event) => setSelectedSpecialtyId(event.target.value)}
            >
              <option value="">All specialties</option>
              {specialties.map((specialty) => (
                <option key={specialty.id} value={specialty.id}>
                  {specialty.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Appointment mode
            <select value={selectedMode} onChange={(event) => setSelectedMode(event.target.value)}>
              <option value="ALL">All modes</option>
              <option value="ONLINE">Online</option>
              <option value="OFFLINE">Offline</option>
            </select>
          </label>
        </div>

        <div className="card-list">
          {isLoadingDoctors ? <p className="muted">Refreshing doctors...</p> : null}
          {!visibleDoctors.length && !isLoadingDoctors ? (
            <EmptyState
              title="No doctors found."
              detail="Try another specialty or mode, or ask an admin to add more doctor profiles."
            />
          ) : null}
          {visibleDoctors.map((doctor) => (
            <button
              key={doctor.id}
              type="button"
              className={String(selectedDoctor?.id) === String(doctor.id) ? "doctor-card active" : "doctor-card"}
              onClick={() => setSelectedDoctorId(String(doctor.id))}
            >
              <span>{doctor.name}</span>
              <small>{doctor.specialty?.name || "General practice"}</small>
              <small>{doctor.clinicAddress || "PulsePoint Clinic, MG Road, Bengaluru"}</small>
              <StatusPill value={doctor.mode || "UNSPECIFIED"} />
            </button>
          ))}
        </div>
      </article>

      <article className="panel">
        <SectionTitle
          title="Choose Date & Time"
          subtitle={selectedDoctor ? `Available slots for ${selectedDoctor.name}.` : "Select a doctor first to load appointment times."}
        />
        {selectedDoctor ? (
          <div className="selected-doctor-banner">
            <strong>{selectedDoctor.name}</strong>
            <span>{selectedDoctor.specialty?.name || "General practice"}</span>
            <span>{selectedDoctor.mode}</span>
          </div>
        ) : null}

        {isLoadingSlots ? <p className="muted">Loading available slots...</p> : null}
        {!availableDates.length && !isLoadingSlots ? (
          <EmptyState
            title="No available slots."
            detail="Try another doctor or ask an admin to create new appointment slots."
          />
        ) : null}

        {availableDates.length ? (
          <div className="slot-picker">
            <div className="date-strip">
              {availableDates.map((dateKey) => (
                <button
                  key={dateKey}
                  type="button"
                  className={selectedSlotDate === dateKey ? "date-button active" : "date-button"}
                  onClick={() => setSelectedSlotDate(dateKey)}
                >
                  {formatDateBadge(dateKey)}
                </button>
              ))}
            </div>

            <div className="time-grid">
              {slotsForSelectedDate.map((slot) => (
                <button
                  key={slot.id}
                  type="button"
                  className={String(selectedSlot?.id) === String(slot.id) ? "time-button active" : "time-button"}
                  onClick={() => setSelectedSlotId(String(slot.id))}
                >
                  <strong>{formatTimeRange(slot.startTime, slot.endTime)}</strong>
                  <span>{formatDateTime(slot.startTime)}</span>
                </button>
              ))}
            </div>
          </div>
        ) : null}
      </article>

      <article className="panel">
        <SectionTitle
          title="Reserve & Confirm"
          subtitle="Hold the slot first, then confirm the appointment before the timer expires."
        />
        {!selectedSlot ? (
          <EmptyState
            title="Pick a slot to continue."
            detail="Choose one of the available times, then reserve it temporarily."
          />
        ) : (
          <div className="confirmation-card">
            <strong>{formatDateTime(selectedSlot.startTime)}</strong>
            <p>Ends at {formatDateTime(selectedSlot.endTime)}.</p>
            <p>{appointmentSupportLine(reservation?.mode || selectedDoctor?.mode, selectedDoctor)}</p>
            {!reservation || reservation.slotId !== selectedSlot.id ? (
              <button
                type="button"
                className="primary-button"
                disabled={isSubmittingReservation}
                onClick={onReserve}
              >
                {isSubmittingReservation ? "Reserving..." : "Reserve selected slot"}
              </button>
            ) : (
              <>
                <p>Reservation expires at {formatDateTime(reservation.expiresAt)}.</p>
                <label>
                  Appointment mode
                  <select
                    value={reservation.mode}
                    disabled={reservation.doctor?.mode !== "BOTH"}
                    onChange={(event) =>
                      setReservation((current) => ({ ...current, mode: event.target.value }))
                    }
                  >
                    <option value="ONLINE">Online</option>
                    <option value="OFFLINE">Offline</option>
                  </select>
                </label>
                <p>{appointmentSupportLine(reservation.mode, reservation.doctor)}</p>
                <div className="action-row">
                  <button type="button" className="ghost-button" onClick={onReleaseReservation}>
                    Release hold
                  </button>
                  <button
                    type="button"
                    className="primary-button"
                    disabled={isSubmittingReservation}
                    onClick={onConfirmBooking}
                  >
                    {isSubmittingReservation ? "Confirming..." : "Confirm appointment"}
                  </button>
                </div>
              </>
            )}
          </div>
        )}
      </article>

      <article className="panel">
        <SectionTitle
          title="Confirmation & Reminders"
          subtitle="Appointment confirmation includes business-rule details for online and offline visits."
        />
        {!latestReceipt ? (
          <EmptyState
            title="No confirmation yet."
            detail="Once you confirm a held slot, confirmation and reminder details appear here."
          />
        ) : (
          <div className="notification-card">
            <strong>{latestReceipt.notification.subject}</strong>
            <p>{latestReceipt.notification.detail}</p>
            <p>Recipient: {latestReceipt.notification.recipient}</p>
            <p>Doctor: {latestReceipt.notification.doctorName}</p>
            <p>Specialty: {latestReceipt.notification.specialtyName}</p>
            <p>Appointment time: {latestReceipt.notification.appointmentWindow}</p>
            <p>Instructions: {latestReceipt.notification.instructions}</p>
            <p>Email delivery: {latestReceipt.notification.deliveryStatus}</p>
            <p>Reminder: {latestReceipt.notification.reminderStatus}</p>
            <p>Reminder time: {formatDateTime(latestReceipt.notification.reminderAt)}</p>
            {latestReceipt.notification.videoLink ? (
              <p>Video link: {latestReceipt.notification.videoLink}</p>
            ) : null}
            {latestReceipt.notification.clinicAddress ? (
              <p>Clinic address: {latestReceipt.notification.clinicAddress}</p>
            ) : null}
            <label>
              Email confirmation preview
              <textarea
                readOnly
                rows={10}
                value={latestReceipt.notification.emailBody}
              />
            </label>
          </div>
        )}
      </article>

      <article className="panel panel-wide">
        <SectionTitle
          title="Appointment History"
          subtitle="Track confirmed, completed, cancelled, or no-show appointments from your dashboard."
        />
        {isLoadingAppointments ? <p className="muted">Refreshing appointment history...</p> : null}
        {!patientAppointments.length && !isLoadingAppointments ? (
          <EmptyState
            title="No appointments yet."
            detail="Book your first appointment to start tracking history here."
          />
        ) : null}
        <div className="history-list">
          {patientAppointments.map((appointment) => (
            <div key={appointment.id} className="history-card">
              <div>
                <strong>{appointment.doctor?.name || "Doctor unavailable"}</strong>
                <p>
                  {appointment.doctor?.specialty?.name || "General practice"} ·{" "}
                  {formatDateTime(appointment.slot?.startTime)}
                </p>
                <p>{appointmentSupportLine(appointment.mode, appointment.doctor, appointment.id)}</p>
              </div>
              <div className="history-actions">
                <StatusPill value={appointment.status} />
                <StatusPill value={appointment.mode || appointment.doctor?.mode || "UNKNOWN"} />
                {appointment.status === "CONFIRMED" ? (
                  <button
                    type="button"
                    className="ghost-button"
                    onClick={() => onUpdateAppointmentStatus(appointment.id, "CANCELLED")}
                  >
                    Cancel
                  </button>
                ) : null}
              </div>
            </div>
          ))}
        </div>
      </article>
    </section>
  );
}

export default PatientDashboardPage;
