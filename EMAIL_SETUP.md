## Real Email Setup

The booking confirmation flow can now send real emails through SMTP.

### Recommended Gmail setup

Use these environment variables before starting the backend:

```bash
export APP_NOTIFICATION_EMAIL_ENABLED=true
export APP_MAIL_HOST=smtp.gmail.com
export APP_MAIL_PORT=587
export APP_MAIL_USERNAME="your-gmail@gmail.com"
export APP_MAIL_PASSWORD="your-16-char-app-password"
export APP_NOTIFICATION_FROM_EMAIL="your-gmail@gmail.com"
```

Then restart the backend:

```bash
cd /Users/saidharahasrao/doctor-appointment-system/doctor-appointment
mvn spring-boot:run
```

### Notes

- Use a Gmail App Password, not your normal Gmail password.
- If SMTP is not configured, bookings still succeed and the UI shows an email preview instead of a sent status.
- When SMTP is configured correctly, confirmation emails include booking details, appointment time, clinic address for offline visits, and video link for online visits.
