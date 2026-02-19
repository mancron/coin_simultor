package com.team.coin_simulator.login;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailManager {

    private static final String SERVICE = "NAVER"; // "NAVER" 또는 "GOOGLE"
    private static final String SENDER = "네이버아이디@naver.com";
    private static final String APP_PASSWORD = "네이버앱비밀번호"; // 앱 비밀번호!

    public static void sendMail(String recipient, String subject, String body) throws Exception {
        Properties props = new Properties();

        if ("NAVER".equalsIgnoreCase(SERVICE)) {
            props.put("mail.smtp.host", "smtp.naver.com");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
