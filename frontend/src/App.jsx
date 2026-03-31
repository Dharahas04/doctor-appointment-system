import { BrowserRouter as Router, Navigate, Route, Routes, useNavigate } from "react-router-dom";
import AppShell from "./components/layout/AppShell.jsx";
import ProtectedRoute from "./components/routing/ProtectedRoute.jsx";
import { usePortalController } from "./hooks/usePortalController.js";
import AdminConsolePage from "./pages/AdminConsolePage.jsx";
import AuthPage from "./pages/AuthPage.jsx";
import PatientDashboardPage from "./pages/PatientDashboardPage.jsx";

function App() {
  return (
    <Router>
      <AppRoutes />
    </Router>
  );
}

function AppRoutes() {
  const navigate = useNavigate();
  const controller = usePortalController();

  async function onLogin(event) {
    const nextPath = await controller.handleLogin(event);
    if (nextPath) {
      navigate(nextPath, { replace: true });
    }
  }

  async function onRegister(event) {
    const nextPath = await controller.handleRegister(event);
    if (nextPath) {
      navigate(nextPath, { replace: true });
    }
  }

  async function onLogout() {
    await controller.handleLogout();
    navigate("/auth", { replace: true });
  }

  return (
    <Routes>
      <Route
        path="/"
        element={
          <Navigate to="/auth" replace />
        }
      />

      <Route
        path="/auth"
        element={
          <AuthPage
            authTab={controller.authTab}
            isSubmittingAuth={controller.isSubmittingAuth}
            loginForm={controller.loginForm}
            notice={controller.notice}
            onLogin={onLogin}
            onRegister={onRegister}
            registerForm={controller.registerForm}
            setAuthTab={controller.setAuthTab}
            setLoginForm={controller.setLoginForm}
            setRegisterForm={controller.setRegisterForm}
          />
        }
      />

      <Route
        element={
          <ProtectedRoute session={controller.session}>
            <AppShell notice={controller.notice} onLogout={onLogout} session={controller.session} />
          </ProtectedRoute>
        }
      >
        <Route
          path="/patient"
          element={
            <ProtectedRoute allowedRoles={["Patient"]} session={controller.session}>
              <PatientDashboardPage
                availableDates={controller.availableDates}
                doctorSearch={controller.doctorSearch}
                isLoadingAppointments={controller.isLoadingAppointments}
                isLoadingDoctors={controller.isLoadingDoctors}
                isLoadingSlots={controller.isLoadingSlots}
                isSubmittingReservation={controller.isSubmittingReservation}
                latestReceipt={controller.latestReceipt}
                onConfirmBooking={controller.handleConfirmBooking}
                onLogout={onLogout}
                onReleaseReservation={controller.handleReleaseReservation}
                onReserve={controller.handleReserve}
                onUpdateAppointmentStatus={controller.handleUpdateAppointmentStatus}
                patientAppointments={controller.patientAppointments}
                reservation={controller.reservation}
                selectedDoctor={controller.selectedDoctor}
                selectedMode={controller.selectedMode}
                selectedSlot={controller.selectedSlot}
                selectedSlotDate={controller.selectedSlotDate}
                selectedSpecialtyId={controller.selectedSpecialtyId}
                session={controller.session}
                setDoctorSearch={controller.setDoctorSearch}
                setReservation={controller.setReservation}
                setSelectedDoctorId={controller.setSelectedDoctorId}
                setSelectedMode={controller.setSelectedMode}
                setSelectedSlotDate={controller.setSelectedSlotDate}
                setSelectedSlotId={controller.setSelectedSlotId}
                setSelectedSpecialtyId={controller.setSelectedSpecialtyId}
                slotsForSelectedDate={controller.slotsForSelectedDate}
                specialties={controller.specialties}
                stats={controller.stats}
                visibleDoctors={controller.visibleDoctors}
              />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={["Admin"]} session={controller.session}>
              <AdminConsolePage
                allAppointments={controller.allAppointments}
                analytics={controller.analytics}
                doctorForm={controller.doctorForm}
                doctors={controller.doctors}
                isSubmittingAdmin={controller.isSubmittingAdmin}
                onCreateDoctor={controller.handleCreateDoctor}
                onCreateSlot={controller.handleCreateSlot}
                onCreateSpecialty={controller.handleCreateSpecialty}
                onDoctorFormChange={controller.setDoctorForm}
                onSlotFormChange={controller.setSlotForm}
                onSpecialtyFormChange={controller.setSpecialtyForm}
                onUpdateAppointmentStatus={controller.handleUpdateAppointmentStatus}
                report={controller.report}
                slotForm={controller.slotForm}
                specialties={controller.specialties}
                specialtyForm={controller.specialtyForm}
                stats={controller.stats}
              />
            </ProtectedRoute>
          }
        />
      </Route>

      <Route
        path="*"
        element={
          <Navigate to="/auth" replace />
        }
      />
    </Routes>
  );
}

export default App;
