import EmptyState from "../components/ui/EmptyState.jsx";
import MetricCard from "../components/ui/MetricCard.jsx";
import SectionTitle from "../components/ui/SectionTitle.jsx";
import StatusPill from "../components/ui/StatusPill.jsx";
import { appointmentSupportLine, formatDateTime } from "../utils/formatters.js";

function AdminConsolePage({
  allAppointments,
  analytics,
  doctorForm,
  doctors,
  isSubmittingAdmin,
  onCreateDoctor,
  onCreateSlot,
  onCreateSpecialty,
  onDoctorFormChange,
  onSlotFormChange,
  onSpecialtyFormChange,
  onUpdateAppointmentStatus,
  report,
  slotForm,
  specialties,
  specialtyForm,
  stats,
}) {
  return (
    <section className="page-grid">
      <article className="panel">
        <SectionTitle
          title="Operations Snapshot"
          subtitle="Monitor appointments, patient growth, and clinic activity from one place."
        />
        <div className="metric-grid">
          <MetricCard label="Specialties" value={stats.specialties} />
          <MetricCard label="Doctors" value={stats.doctors} />
          <MetricCard label="Patients" value={stats.patients} />
          <MetricCard label="Confirmed today" value={stats.confirmed} />
        </div>
      </article>

      <article className="panel">
        <SectionTitle
          title="Daily Report"
          subtitle="Operational summary for the current day."
        />
        {report ? (
          <div className="report-grid">
            <MetricCard label="Confirmed" value={report.confirmedAppointments} />
            <MetricCard label="Completed" value={report.completedAppointments} />
            <MetricCard label="Cancelled" value={report.cancelledAppointments} />
            <MetricCard label="No-show" value={report.noShowAppointments} />
            <MetricCard label="Online" value={report.onlineAppointments} />
            <MetricCard label="Offline" value={report.offlineAppointments} />
          </div>
        ) : (
          <EmptyState title="No report data yet." detail="Daily summary will appear here when appointments are available." />
        )}
      </article>

      <article className="panel panel-wide">
        <SectionTitle
          title="Analytics Dashboard"
          subtitle="Stretch analytics by day, consultation mode, and doctor."
        />
        <div className="analytics-grid">
          <AnalyticsList
            title="Appointments by day"
            items={analytics?.appointmentsByDay || []}
          />
          <AnalyticsList
            title="Appointments by mode"
            items={analytics?.appointmentsByMode || []}
          />
          <AnalyticsList
            title="Appointments by doctor"
            items={analytics?.appointmentsByDoctor || []}
          />
        </div>
      </article>

      <article className="panel">
        <SectionTitle title="Add Specialty" subtitle="Create patient-facing specialties for discovery." />
        <form className="stack-form" onSubmit={onCreateSpecialty}>
          <label>
            Specialty name
            <input
              required
              value={specialtyForm.name}
              onChange={(event) => onSpecialtyFormChange({ name: event.target.value })}
              placeholder="Cardiology"
            />
          </label>
          <button type="submit" className="primary-button" disabled={isSubmittingAdmin}>
            {isSubmittingAdmin ? "Saving..." : "Create specialty"}
          </button>
        </form>
      </article>

      <article className="panel">
        <SectionTitle
          title="Add Doctor"
          subtitle="Create online, offline, or hybrid doctor profiles with clinic details."
        />
        <form className="stack-form" onSubmit={onCreateDoctor}>
          <label>
            Doctor name
            <input
              required
              value={doctorForm.name}
              onChange={(event) =>
                onDoctorFormChange((current) => ({ ...current, name: event.target.value }))
              }
              placeholder="Dr. Meera Nair"
            />
          </label>
          <label>
            Specialty
            <select
              required
              value={doctorForm.specialtyId}
              onChange={(event) =>
                onDoctorFormChange((current) => ({ ...current, specialtyId: event.target.value }))
              }
            >
              <option value="">Choose specialty</option>
              {specialties.map((specialty) => (
                <option key={specialty.id} value={specialty.id}>
                  {specialty.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Mode
            <select
              value={doctorForm.mode}
              onChange={(event) =>
                onDoctorFormChange((current) => ({ ...current, mode: event.target.value }))
              }
            >
              <option value="ONLINE">ONLINE</option>
              <option value="OFFLINE">OFFLINE</option>
              <option value="BOTH">BOTH</option>
            </select>
          </label>
          <label>
            Clinic address
            <input
              value={doctorForm.clinicAddress}
              onChange={(event) =>
                onDoctorFormChange((current) => ({ ...current, clinicAddress: event.target.value }))
              }
              placeholder="PulsePoint Clinic, MG Road, Bengaluru"
            />
          </label>
          <button type="submit" className="primary-button" disabled={isSubmittingAdmin}>
            {isSubmittingAdmin ? "Saving..." : "Create doctor"}
          </button>
        </form>
      </article>

      <article className="panel">
        <SectionTitle title="Create Slot" subtitle="Add date and time slots to make doctors bookable." />
        <form className="stack-form" onSubmit={onCreateSlot}>
          <label>
            Doctor
            <select
              required
              value={slotForm.doctorId}
              onChange={(event) =>
                onSlotFormChange((current) => ({ ...current, doctorId: event.target.value }))
              }
            >
              <option value="">Choose doctor</option>
              {doctors.map((doctor) => (
                <option key={doctor.id} value={doctor.id}>
                  {doctor.name} · {doctor.specialty?.name || "General practice"}
                </option>
              ))}
            </select>
          </label>
          <label>
            Start time
            <input
              required
              type="datetime-local"
              value={slotForm.startTime}
              onChange={(event) =>
                onSlotFormChange((current) => ({ ...current, startTime: event.target.value }))
              }
            />
          </label>
          <label>
            End time
            <input
              required
              type="datetime-local"
              value={slotForm.endTime}
              onChange={(event) =>
                onSlotFormChange((current) => ({ ...current, endTime: event.target.value }))
              }
            />
          </label>
          <button type="submit" className="primary-button" disabled={isSubmittingAdmin}>
            {isSubmittingAdmin ? "Saving..." : "Create slot"}
          </button>
        </form>
      </article>

      <article className="panel panel-wide">
        <SectionTitle
          title="Appointment Lifecycle"
          subtitle="Track confirmed, completed, cancelled, and no-show appointments."
        />
        {!allAppointments.length ? (
          <EmptyState
            title="No appointments yet."
            detail="Appointments will appear here once patients start booking."
          />
        ) : null}
        <div className="history-list">
          {allAppointments.map((appointment) => (
            <div key={appointment.id} className="history-card">
              <div>
                <strong>{appointment.doctor?.name || "Doctor unavailable"}</strong>
                <p>
                  Patient #{appointment.patientId} · {formatDateTime(appointment.slot?.startTime)}
                </p>
                <p>{appointmentSupportLine(appointment.mode, appointment.doctor, appointment.id)}</p>
              </div>
              <div className="admin-status-controls">
                <StatusPill value={appointment.status} />
                <StatusPill value={appointment.mode || appointment.doctor?.mode || "UNKNOWN"} />
                <button
                  type="button"
                  className="ghost-button"
                  onClick={() => onUpdateAppointmentStatus(appointment.id, "COMPLETED")}
                >
                  Complete
                </button>
                <button
                  type="button"
                  className="ghost-button"
                  onClick={() => onUpdateAppointmentStatus(appointment.id, "NO_SHOW")}
                >
                  No-show
                </button>
                <button
                  type="button"
                  className="ghost-button"
                  onClick={() => onUpdateAppointmentStatus(appointment.id, "CANCELLED")}
                >
                  Cancel
                </button>
              </div>
            </div>
          ))}
        </div>
      </article>
    </section>
  );
}

function AnalyticsList({ items, title }) {
  return (
    <div className="analytics-card">
      <strong>{title}</strong>
      {!items.length ? <p className="muted">No analytics available yet.</p> : null}
      <div className="analytics-list">
        {items.map((item) => (
          <div key={`${title}-${item.label}`} className="analytics-row">
            <span>{item.label}</span>
            <strong>{item.value}</strong>
          </div>
        ))}
      </div>
    </div>
  );
}

export default AdminConsolePage;
