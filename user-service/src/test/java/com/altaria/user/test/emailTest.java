package com.altaria.user.test;

import cn.hutool.crypto.SignUtil;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.DigestUtils;

import java.util.Map;

@SpringBootTest
public class emailTest {


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    public void testEmail() throws MessagingException {
        System.out.println("Email test");
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        mimeMessage.setSubject("Testing email");
        mimeMessage.setText("This is a test email");
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("2744974948@qq.com"));
        javaMailSender.send(mimeMessage);
        System.out.println("Email sent");
    }

    @Test
    public void md5Test() {
        System.out.println("MD5 test");
        String password = "123456";
        String md5 = SignUtil.signParamsMd5(Map.of("password", password, "salt", "123456", "key", "123456"), password);
        System.out.println(md5);

        md5 = DigestUtils.md5DigestAsHex(password.getBytes());
        System.out.println(md5);
    }
}
