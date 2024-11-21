package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void  sendEmail(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendTestEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();

        System.out.println("Email адрес до отправки: [" + toEmail + "]");
        message.setTo(toEmail.trim()); // .trim() удалит случайные пробелы по краям строки
        message.setSubject("Test Email");
        message.setText("This is a test email from your application.");
        mailSender.send(message);
    }
}
