package com.app.doctorappointment.service;

import com.app.doctorappointment.dto.AnalyticsOverviewResponse;
import com.app.doctorappointment.dto.AnalyticsPointResponse;
import com.app.doctorappointment.dto.DailySummaryResponse;
import com.app.doctorappointment.model.Appointment;
import com.app.doctorappointment.model.Doctor;
import com.app.doctorappointment.model.Role;
import com.app.doctorappointment.repository.AppointmentRepository;
import com.app.doctorappointment.repository.DoctorRepository;
import com.app.doctorappointment.repository.SpecialtyRepository;
import com.app.doctorappointment.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    private final AppointmentRepository appointmentRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public ReportingService(AppointmentRepository appointmentRepository,
            SpecialtyRepository specialtyRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    public DailySummaryResponse getDailySummary() {
        LocalDate today = LocalDate.now();
        List<Appointment> todaysAppointments = appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getCreatedAt() != null)
                .filter(appointment -> today.equals(appointment.getCreatedAt().toLocalDate()))
                .toList();

        return new DailySummaryResponse(
                today,
                specialtyRepository.count(),
                doctorRepository.count(),
                userRepository.findAll().stream().filter(user -> user.getRole() == Role.Patient).count(),
                todaysAppointments.size(),
                countByStatus(todaysAppointments, "CONFIRMED"),
                countByStatus(todaysAppointments, "COMPLETED"),
                countByStatus(todaysAppointments, "CANCELLED"),
                countByStatus(todaysAppointments, "NO_SHOW"),
                countByMode(todaysAppointments, "ONLINE"),
                countByMode(todaysAppointments, "OFFLINE"));
    }

    public AnalyticsOverviewResponse getAnalyticsOverview() {
        List<Appointment> appointments = appointmentRepository.findAll();

        List<AnalyticsPointResponse> byDay = appointments.stream()
                .filter(appointment -> appointment.getSlot() != null && appointment.getSlot().getStartTime() != null)
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getSlot().getStartTime().toLocalDate().toString(),
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AnalyticsPointResponse(entry.getKey(), entry.getValue()))
                .toList();

        List<AnalyticsPointResponse> byMode = appointments.stream()
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getMode() == null ? "UNSPECIFIED" : appointment.getMode(),
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AnalyticsPointResponse(entry.getKey(), entry.getValue()))
                .toList();

        List<AnalyticsPointResponse> byDoctor = appointments.stream()
                .filter(appointment -> appointment.getDoctor() != null)
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getDoctor().getName(),
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new AnalyticsPointResponse(entry.getKey(), entry.getValue()))
                .toList();

        return new AnalyticsOverviewResponse(byDay, byMode, byDoctor);
    }

    private long countByStatus(List<Appointment> appointments, String status) {
        return appointments.stream()
                .filter(appointment -> status.equalsIgnoreCase(appointment.getStatus()))
                .count();
    }

    private long countByMode(List<Appointment> appointments, String mode) {
        return appointments.stream()
                .filter(appointment -> mode.equalsIgnoreCase(appointment.getMode()))
                .count();
    }
}
