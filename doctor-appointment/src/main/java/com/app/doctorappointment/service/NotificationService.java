package com.app.doctorappointment.service;

import com.app.doctorappointment.dto.NotificationDetailsResponse;
import com.app.doctorappointment.model.Appointment;
import com.app.doctorappointment.model.Doctor;
import com.app.doctorappointment.model.Slot;
import com.app.doctorappointment.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String DEFAULT_CLINIC_ADDRESS = "PulsePoint Clinic, MG Road, Bengaluru";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final JavaMailSender mailSender;
    private final boolean emailDeliveryEnabled;
    private final String fromEmail;

    public NotificationService() {
        this((JavaMailSender) null, false, "no-reply@pulsepoint.local");
    }

    @Autowired
    public NotificationService(ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.notification.email.enabled:false}") boolean emailDeliveryEnabled,
            @Value("${app.notification.from-email:no-reply@pulsepoint.local}") String fromEmail) {
        this(mailSenderProvider.getIfAvailable(), emailDeliveryEnabled, fromEmail);
    }

    NotificationService(JavaMailSender mailSender, boolean emailDeliveryEnabled, String fromEmail) {
        this.mailSender = mailSender;
        this.emailDeliveryEnabled = emailDeliveryEnabled;
        this.fromEmail = fromEmail == null ? "" : fromEmail.trim();
    }

    public NotificationDetailsResponse buildConfirmation(Appointment appointment, User patient) {
        return createConfirmation(appointment, patient, "PREVIEW_ONLY");
    }

    public NotificationDetailsResponse sendConfirmation(Appointment appointment, User patient) {
        NotificationDetailsResponse confirmation = createConfirmation(appointment, patient, currentDeliveryStatus());
        return switch (confirmation.deliveryStatus()) {
            case "READY_TO_SEND" -> deliver(confirmation);
            case "DISABLED" -> withDeliveryDetails(
                    confirmation,
                    "DISABLED",
                    "Appointment confirmed. Email delivery is currently disabled, so a preview is shown below.");
            case "NOT_CONFIGURED" -> withDeliveryDetails(
                    confirmation,
                    "NOT_CONFIGURED",
                    "Appointment confirmed. Email settings are not configured yet, so a preview is shown below.");
            default -> confirmation;
        };
    }

    private NotificationDetailsResponse createConfirmation(Appointment appointment, User patient, String deliveryStatus) {
        String mode = appointment.getMode() == null ? "OFFLINE" : appointment.getMode().toUpperCase();
        Doctor doctor = appointment.getDoctor();
        Slot slot = appointment.getSlot();
        String doctorName = doctor != null && doctor.getName() != null && !doctor.getName().isBlank()
                ? doctor.getName()
                : "Doctor unavailable";
        String specialtyName = doctor != null && doctor.getSpecialty() != null && doctor.getSpecialty().getName() != null
                && !doctor.getSpecialty().getName().isBlank()
                        ? doctor.getSpecialty().getName()
                        : "General practice";

        String clinicAddress = doctor != null && doctor.getClinicAddress() != null && !doctor.getClinicAddress().isBlank()
                ? doctor.getClinicAddress()
                : DEFAULT_CLINIC_ADDRESS;
        String videoLink = "ONLINE".equals(mode)
                ? "https://telemed.pulsepoint.local/visit/" + appointment.getId()
                : null;
        String appointmentTime = formatDateTime(slot != null ? slot.getStartTime() : null);
        String appointmentWindow = formatWindow(slot);
        String instructions = "ONLINE".equals(mode)
                ? "Join the video consultation 5 minutes early using the secure link below."
                : "Please arrive 10 minutes early, carry a valid ID, and check in at the clinic reception.";
        String detail = "ONLINE".equals(mode)
                ? "Your teleconsultation is confirmed with booking details and joining instructions."
                : "Your in-clinic appointment is confirmed with booking details and visit instructions.";
        String subject = "Appointment confirmed with " + doctorName + " on " + appointmentTime;
        String emailBody = buildEmailBody(
                patient.getFullName(),
                doctorName,
                specialtyName,
                mode,
                appointmentWindow,
                instructions,
                videoLink,
                clinicAddress,
                appointment.getId());

        return new NotificationDetailsResponse(
                "EMAIL",
                patient.getEmail(),
                subject,
                detail,
                doctorName,
                specialtyName,
                appointmentTime,
                appointmentWindow,
                instructions,
                emailBody,
                "OFFLINE".equals(mode) ? clinicAddress : null,
                videoLink,
                deliveryStatus,
                "SCHEDULED",
                slot != null && slot.getStartTime() != null
                        ? slot.getStartTime().minusMinutes(30).atOffset(ZoneOffset.UTC)
                        : OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(30));
    }

    private NotificationDetailsResponse deliver(NotificationDetailsResponse confirmation) {
        try {
            deliverMessage(confirmation);
            return withDeliveryDetails(
                    confirmation,
                    "SENT",
                    "Appointment confirmed. A confirmation email has been sent with your booking details.");
        } catch (MailException ex) {
            log.warn("Unable to send booking email to {}: {}", confirmation.recipient(), ex.getMessage());
            return withDeliveryDetails(
                    confirmation,
                    "FAILED",
                    "Appointment confirmed, but the email could not be delivered right now. The preview is shown below.");
        }
    }

    private void deliverMessage(NotificationDetailsResponse confirmation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(confirmation.recipient());
        message.setFrom(fromEmail);
        message.setSubject(confirmation.subject());
        message.setText(confirmation.emailBody());
        mailSender.send(message);
    }

    private NotificationDetailsResponse withDeliveryDetails(NotificationDetailsResponse confirmation,
            String deliveryStatus,
            String detail) {
        return new NotificationDetailsResponse(
                confirmation.channel(),
                confirmation.recipient(),
                confirmation.subject(),
                detail,
                confirmation.doctorName(),
                confirmation.specialtyName(),
                confirmation.appointmentTime(),
                confirmation.appointmentWindow(),
                confirmation.instructions(),
                confirmation.emailBody(),
                confirmation.clinicAddress(),
                confirmation.videoLink(),
                deliveryStatus,
                confirmation.reminderStatus(),
                confirmation.reminderAt());
    }

    private String currentDeliveryStatus() {
        if (!emailDeliveryEnabled) {
            return "DISABLED";
        }
        if (mailSender == null || fromEmail.isBlank() || fromEmail.endsWith("@pulsepoint.local")) {
            return "NOT_CONFIGURED";
        }
        return "READY_TO_SEND";
    }

    private String buildEmailBody(String patientName,
            String doctorName,
            String specialtyName,
            String mode,
            String appointmentWindow,
            String instructions,
            String videoLink,
            String clinicAddress,
            Long appointmentId) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello ").append(patientName == null || patientName.isBlank() ? "Patient" : patientName).append(",\n\n")
                .append("Your ").append(mode).append(" appointment has been confirmed.\n")
                .append("Doctor: ").append(doctorName).append('\n')
                .append("Specialty: ").append(specialtyName).append('\n')
                .append("Appointment time: ").append(appointmentWindow).append('\n')
                .append("Appointment ID: ").append(appointmentId).append('\n');

        if ("ONLINE".equals(mode)) {
            builder.append("Video link: ").append(videoLink).append('\n');
        } else {
            builder.append("Clinic address: ").append(clinicAddress).append('\n');
        }

        builder.append("Instructions: ").append(instructions).append("\n\n")
                .append("Thank you,\nPulsePoint Clinic");

        return builder.toString();
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "Time will be shared soon";
        }
        return value.format(DATE_TIME_FORMATTER);
    }

    private String formatWindow(Slot slot) {
        if (slot == null || slot.getStartTime() == null) {
            return "Time will be shared soon";
        }
        if (slot.getEndTime() == null) {
            return formatDateTime(slot.getStartTime());
        }
        return slot.getStartTime().format(DATE_TIME_FORMATTER) + " - " + slot.getEndTime().format(TIME_FORMATTER);
    }
}
