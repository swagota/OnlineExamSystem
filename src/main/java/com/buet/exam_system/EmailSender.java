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
                                      double score,
                                      int total) {
//        System.out.println("Email method called");
        if (toEmail == null || toEmail.isEmpty()) return;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
//        props.put("mail.debug", "true");

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

            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", FROM_EMAIL, APP_PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

