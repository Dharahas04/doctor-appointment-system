import { startTransition, useDeferredValue, useEffect, useMemo, useState } from "react";
import {
  confirmAppointment,
  createDoctor,
  createSlot,
  createSpecialty,
  getAnalyticsOverview,
  getAppointments,
  getDailySummary,
  getDoctors,
  getSlots,
  getSpecialties,
  loginPatientAccount,
  registerPatientAccount,
  releaseReservation,
  reserveAppointment,
  updateAppointmentStatus,
} from "../api.js";
import { clearSession, readSession, writeSession } from "../lib/session.js";
import { getAvailableDates, normalizeBookingMode, sessionFromAuth, slotDateKey } from "../utils/formatters.js";

const emptyRegisterForm = {
  fullName: "",
  email: "",
  password: "",
};

const emptyLoginForm = {
  email: "",
  password: "",
};

const emptySpecialtyForm = {
  name: "",
};

const emptyDoctorForm = {
  name: "",
  specialtyId: "",
  mode: "ONLINE",
  clinicAddress: "",
};

const emptySlotForm = {
  doctorId: "",
  startTime: "",
  endTime: "",
};

export function usePortalController() {
  const [specialties, setSpecialties] = useState([]);
  const [allDoctors, setAllDoctors] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [slots, setSlots] = useState([]);
  const [allAppointments, setAllAppointments] = useState([]);
  const [patientAppointments, setPatientAppointments] = useState([]);
  const [report, setReport] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [session, setSession] = useState(() => readSession());
  const [selectedSpecialtyId, setSelectedSpecialtyId] = useState("");
  const [selectedMode, setSelectedMode] = useState("ALL");
  const [selectedDoctorId, setSelectedDoctorId] = useState("");
  const [selectedSlotDate, setSelectedSlotDate] = useState("");
  const [selectedSlotId, setSelectedSlotId] = useState("");
  const [doctorSearch, setDoctorSearch] = useState("");
  const [authTab, setAuthTab] = useState("login");
  const [registerForm, setRegisterForm] = useState(emptyRegisterForm);
  const [loginForm, setLoginForm] = useState(emptyLoginForm);
  const [specialtyForm, setSpecialtyForm] = useState(emptySpecialtyForm);
  const [doctorForm, setDoctorForm] = useState(emptyDoctorForm);
  const [slotForm, setSlotForm] = useState(emptySlotForm);
  const [reservation, setReservation] = useState(null);
  const [latestReceipt, setLatestReceipt] = useState(null);
  const [notice, setNotice] = useState(null);
  const [isLoadingDoctors, setIsLoadingDoctors] = useState(false);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [isLoadingAppointments, setIsLoadingAppointments] = useState(false);
  const [isSubmittingAuth, setIsSubmittingAuth] = useState(false);
  const [isSubmittingAdmin, setIsSubmittingAdmin] = useState(false);
  const [isSubmittingReservation, setIsSubmittingReservation] = useState(false);

  const deferredDoctorSearch = useDeferredValue(doctorSearch);
  const isAdmin = session?.role === "Admin";
  const selectedDoctor = doctors.find((doctor) => String(doctor.id) === String(selectedDoctorId));
  const selectedSlot = slots.find((slot) => String(slot.id) === String(selectedSlotId));
  const availableDates = getAvailableDates(slots);
  const slotsForSelectedDate = slots.filter((slot) => slotDateKey(slot.startTime) === selectedSlotDate);
  const visibleDoctors = useMemo(() => {
    return doctors.filter((doctor) => {
      if (!deferredDoctorSearch.trim()) {
        return true;
      }
      const haystack = `${doctor.name} ${doctor.specialty?.name || ""} ${doctor.mode || ""}`.toLowerCase();
      return haystack.includes(deferredDoctorSearch.trim().toLowerCase());
    });
  }, [doctors, deferredDoctorSearch]);

  const stats = {
    specialties: report?.specialties ?? specialties.length,
    doctors: report?.doctors ?? allDoctors.length,
    patients: report?.patients ?? (isAdmin ? 0 : 1),
    appointments: report?.totalAppointments ?? allAppointments.length,
    confirmed:
      report?.confirmedAppointments ??
      allAppointments.filter((appointment) => appointment.status === "CONFIRMED").length,
  };

  useEffect(() => {
    if (!session) {
      clearSession();
      resetAuthenticatedState();
      return;
    }

    writeSession(session);
    void refreshRoleData(session);
  }, [session]);

  useEffect(() => {
    if (!session || isAdmin) {
      return;
    }
    void refreshDoctors(selectedSpecialtyId, selectedMode);
  }, [session, isAdmin, selectedSpecialtyId, selectedMode]);

  useEffect(() => {
    if (!session || isAdmin || !selectedDoctorId) {
      startTransition(() => setSlots([]));
      return;
    }
    void refreshSlots(selectedDoctorId);
  }, [session, isAdmin, selectedDoctorId]);

  useEffect(() => {
    setSelectedSlotDate((currentDate) =>
      availableDates.includes(currentDate) ? currentDate : availableDates[0] || "",
    );
  }, [slots]);

  useEffect(() => {
    const availableSlotsForDate = slots.filter((slot) => slotDateKey(slot.startTime) === selectedSlotDate);
    setSelectedSlotId((currentSlotId) => {
      const hasCurrentSlot = availableSlotsForDate.some((slot) => String(slot.id) === String(currentSlotId));
      return hasCurrentSlot ? currentSlotId : String(availableSlotsForDate[0]?.id || "");
    });
  }, [slots, selectedSlotDate]);

  async function refreshRoleData(activeSession = session) {
    if (!activeSession) {
      return;
    }

    try {
      if (activeSession.role === "Admin") {
        const [specialtyData, catalogDoctors, appointmentData, dailySummary, analyticsOverview] = await Promise.all([
          getSpecialties(),
          getDoctors(),
          getAppointments(),
          getDailySummary(),
          getAnalyticsOverview(),
        ]);

        startTransition(() => {
          setSpecialties(specialtyData);
          setAllDoctors(catalogDoctors);
          setDoctors(catalogDoctors);
          setAllAppointments(appointmentData);
          setReport(dailySummary);
          setAnalytics(analyticsOverview);
          setDoctorForm((current) => ({
            ...current,
            specialtyId: current.specialtyId || String(specialtyData[0]?.id || ""),
          }));
          setSlotForm((current) => ({
            ...current,
            doctorId: current.doctorId || String(catalogDoctors[0]?.id || ""),
          }));
        });
        return;
      }

      const [specialtyData, catalogDoctors, patientHistory] = await Promise.all([
        getSpecialties(),
        getDoctors(),
        getAppointments(),
      ]);

      startTransition(() => {
        setSpecialties(specialtyData);
        setAllDoctors(catalogDoctors);
        setDoctors(catalogDoctors);
        setPatientAppointments(patientHistory);
        setAllAppointments(patientHistory);
        setReport(null);
        setAnalytics(null);
      });
    } catch (error) {
      showError(error);
    }
  }

  async function refreshDoctors(specialtyId = selectedSpecialtyId, mode = selectedMode) {
    setIsLoadingDoctors(true);
    try {
      const nextDoctors = await getDoctors({ specialtyId, mode });
      startTransition(() => {
        setDoctors(nextDoctors);
        setSelectedDoctorId((currentDoctorId) => {
          if (!nextDoctors.length) {
            return "";
          }
          const hasCurrentDoctor = nextDoctors.some(
            (doctor) => String(doctor.id) === String(currentDoctorId),
          );
          return hasCurrentDoctor ? currentDoctorId : String(nextDoctors[0].id);
        });
      });
    } catch (error) {
      showError(error);
    } finally {
      setIsLoadingDoctors(false);
    }
  }

  async function refreshSlots(doctorId) {
    setIsLoadingSlots(true);
    try {
      const nextSlots = await getSlots({
        doctorId,
        status: "AVAILABLE",
      });
      startTransition(() => setSlots(nextSlots));
    } catch (error) {
      showError(error);
    } finally {
      setIsLoadingSlots(false);
    }
  }

  async function refreshPatientAppointments() {
    if (!session || session.role !== "Patient") {
      return;
    }

    setIsLoadingAppointments(true);
    try {
      const history = await getAppointments();
      startTransition(() => {
        setPatientAppointments(history);
        setAllAppointments(history);
      });
    } catch (error) {
      showError(error);
    } finally {
      setIsLoadingAppointments(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setIsSubmittingAuth(true);
    try {
      const response = await registerPatientAccount(registerForm);
      const nextSession = sessionFromAuth(response);
      startTransition(() => {
        setSession(nextSession);
        setRegisterForm(emptyRegisterForm);
      });
      setNotice({ type: "success", message: response.message });
      return nextSession.dashboardPath || "/patient";
    } catch (error) {
      showError(error);
      return null;
    } finally {
      setIsSubmittingAuth(false);
    }
  }

  async function handleLogin(event) {
    event.preventDefault();
    setIsSubmittingAuth(true);
    try {
      const response = await loginPatientAccount(loginForm);
      const nextSession = sessionFromAuth(response);
      startTransition(() => {
        setSession(nextSession);
        setLoginForm(emptyLoginForm);
      });
      setNotice({ type: "success", message: response.message });
      return nextSession.dashboardPath || (nextSession.role === "Admin" ? "/admin" : "/patient");
    } catch (error) {
      showError(error);
      return null;
    } finally {
      setIsSubmittingAuth(false);
    }
  }

  async function handleLogout() {
    try {
      if (reservation?.reservationToken) {
        await releaseReservation({
          slotId: reservation.slotId,
          reservationToken: reservation.reservationToken,
        });
      }
    } catch (error) {
      showError(error);
    } finally {
      clearSession();
      startTransition(() => {
        setSession(null);
        setAuthTab("login");
      });
      setNotice({ type: "success", message: "Signed out successfully." });
    }
  }

  async function handleReserve() {
    if (!session?.userId || !selectedSlot) {
      setNotice({
        type: "error",
        message: "Choose a date and time slot before creating a temporary reservation.",
      });
      return;
    }

    setIsSubmittingReservation(true);
    try {
      if (reservation?.reservationToken && reservation.slotId !== selectedSlot.id) {
        await releaseReservation({
          slotId: reservation.slotId,
          reservationToken: reservation.reservationToken,
        });
      }

      const hold = await reserveAppointment({
        slotId: selectedSlot.id,
        patientId: session.userId,
      });

      startTransition(() => {
        setReservation({
          ...hold,
          slot: selectedSlot,
          doctor: selectedDoctor,
          mode: normalizeBookingMode(selectedDoctor?.mode),
        });
        setLatestReceipt(null);
      });

      setNotice({ type: "success", message: hold.message });
    } catch (error) {
      showError(error);
    } finally {
      setIsSubmittingReservation(false);
    }
  }

  async function handleReleaseReservation() {
    if (!reservation?.reservationToken) {
      return;
    }

    try {
      await releaseReservation({
        slotId: reservation.slotId,
        reservationToken: reservation.reservationToken,
      });
      startTransition(() => setReservation(null));
      setNotice({ type: "success", message: "Temporary reservation released." });
    } catch (error) {
      showError(error);
    }
  }

  async function handleConfirmBooking() {
    if (!session?.userId || !reservation?.reservationToken) {
      return;
    }

    setIsSubmittingReservation(true);
    try {
      const receipt = await confirmAppointment({
        slotId: reservation.slotId,
        patientId: session.userId,
        reservationToken: reservation.reservationToken,
        mode: reservation.mode,
      });

      startTransition(() => {
        setLatestReceipt(receipt);
        setReservation(null);
      });

      setNotice({ type: "success", message: receipt.message });
      await Promise.all([refreshPatientAppointments(), selectedDoctorId ? refreshSlots(selectedDoctorId) : Promise.resolve()]);
    } catch (error) {
      showError(error);
    } finally {
      setIsSubmittingReservation(false);
    }
  }

  async function handleCreateSpecialty(event) {
    event.preventDefault();
    setIsSubmittingAdmin(true);
    try {
      await createSpecialty(specialtyForm);
      startTransition(() => setSpecialtyForm(emptySpecialtyForm));
      setNotice({ type: "success", message: "Specialty added successfully." });
      await refreshRoleData(session);
    } catch (error) {
      showError(error);
    } finally {
      setIsSubmittingAdmin(false);
    }
  }

  async function handleCreateDoctor(event) {
    event.preventDefault();
    setIsSubmittingAdmin(true);
    try {
      await createDoctor(doctorForm);
      startTransition(() =>
        setDoctorForm((current) => ({
          ...emptyDoctorForm,
          specialtyId: current.specialtyId,
        })),
      );
      setNotice({ type: "success", message: "Doctor profile created successfully." });
      await refreshRoleData(session);
    } catch (error) {
      showError(error);
    } finally {
      setIsSubmittingAdmin(false);
    }
  }

  async function handleCreateSlot(event) {
    event.preventDefault();
    setIsSubmittingAdmin(true);
    try {
      await createSlot(slotForm);
      startTransition(() =>
        setSlotForm((current) => ({
          ...current,
          startTime: "",
          endTime: "",
        })),
      );
      setNotice({ type: "success", message: "Slot created successfully." });
      await refreshRoleData(session);
    } catch (error) {
      showError(error);
    } finally {
      setIsSubmittingAdmin(false);
    }
  }

  async function handleUpdateAppointmentStatus(appointmentId, status) {
    try {
      await updateAppointmentStatus({ appointmentId, status });
      setNotice({ type: "success", message: `Appointment updated to ${status}.` });
      if (session?.role === "Admin") {
        await refreshRoleData(session);
      } else {
        await refreshPatientAppointments();
      }
    } catch (error) {
      showError(error);
    }
  }

  function resetAuthenticatedState() {
    startTransition(() => {
      setAllDoctors([]);
      setDoctors([]);
      setSlots([]);
      setAllAppointments([]);
      setPatientAppointments([]);
      setReport(null);
      setAnalytics(null);
      setReservation(null);
      setLatestReceipt(null);
      setSelectedDoctorId("");
      setSelectedSlotDate("");
      setSelectedSlotId("");
    });
  }

  function showError(error) {
    setNotice({
      type: "error",
      message: error.message || "Something went wrong while talking to the server.",
    });
  }

  return {
    allAppointments,
    analytics,
    authTab,
    availableDates,
    doctorForm,
    doctorSearch,
    doctors: allDoctors,
    isAdmin,
    isLoadingAppointments,
    isLoadingDoctors,
    isLoadingSlots,
    isSubmittingAdmin,
    isSubmittingAuth,
    isSubmittingReservation,
    latestReceipt,
    loginForm,
    notice,
    patientAppointments,
    registerForm,
    report,
    reservation,
    selectedDoctor,
    selectedMode,
    selectedSlot,
    selectedSlotDate,
    selectedSpecialtyId,
    session,
    slotForm,
    slotsForSelectedDate,
    specialties,
    stats,
    specialtyForm,
    visibleDoctors,
    setAuthTab,
    setDoctorForm,
    setDoctorSearch,
    setLoginForm,
    setRegisterForm,
    setReservation,
    setSelectedDoctorId,
    setSelectedMode,
    setSelectedSlotDate,
    setSelectedSlotId,
    setSelectedSpecialtyId,
    setSlotForm,
    setSpecialtyForm,
    handleConfirmBooking,
    handleCreateDoctor,
    handleCreateSlot,
    handleCreateSpecialty,
    handleLogin,
    handleLogout,
    handleRegister,
    handleReleaseReservation,
    handleReserve,
    handleUpdateAppointmentStatus,
  };
}
