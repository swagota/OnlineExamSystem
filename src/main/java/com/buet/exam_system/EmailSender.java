package com.buet.exam_system;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class EmailSender {
    private static final String FROM_EMAIL = "examora52@gmail.com";
    private static final String APP_PASSWORD = "wzplicbvscpffjvy";

    public static void sendResultMail(String toEmail,
                                      String studentName,
                                      String examName,
                                      int score,
                                      int total) {

        if (toEmail == null || toEmail.isEmpty()) return;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );

            message.setSubject("Exam Result - " + examName);

            String body =
                    "Dear Guardian,\n\n" +
                            "Your child has completed an exam.\n\n" +
                            "Student: " + studentName + "\n" +
                            "Exam: " + examName + "\n" +
                            "Marks: " + score + " / " + total + "\n\n" +
                            "Thank you.\nOnline Exam System";

            message.setText(body);

            Transport.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

