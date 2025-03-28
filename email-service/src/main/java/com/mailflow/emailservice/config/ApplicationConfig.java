package com.mailflow.emailservice.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableR2dbcAuditing
public class ApplicationConfig {
  @Value("${spring.mail.host}")
  private String host;

  @Value("${spring.mail.port}")
  private int port;

  @Value("${spring.mail.username}")
  private String username;

  @Value("${spring.mail.password}")
  private String password;

  @Value("${spring.mail.properties.mail.smtp.auth:true}")
  private String auth;

  @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
  private String starttls;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setUsername(username);
    mailSender.setPassword(password);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", auth);
    props.put("mail.smtp.starttls.enable", starttls);
    props.put("mail.smtp.starttls.required", "true");

    props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
    props.put("mail.smtp.ssl.trust", host);

    props.put("mail.smtp.connectiontimeout", "5000");
    props.put("mail.smtp.timeout", "5000");
    props.put("mail.smtp.writetimeout", "5000");

    props.put("mail.debug", "true");

    return mailSender;
  }

  @Bean
  public HttpMessageConverters messageConverters() {
    return new HttpMessageConverters();
  }
}
