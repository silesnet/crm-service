package net.snet.crm.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SmtpMessagingService implements MessagingService {
  private static final Logger LOG = LoggerFactory.getLogger(SmtpMessagingService.class);

  private final Properties properties;
  private final String fromAddress;
  private final String toAddress;
  private final String subjectTemplate;

  public SmtpMessagingService(SmsMessagingConfiguration configuration) {
    final String[] hostAndPort = configuration.hostAndPort().split(":");
    if (hostAndPort.length != 2) {
      throw new IllegalArgumentException("host and port not set correctly: " + configuration.hostAndPort());
    }
    final String host = hostAndPort[0];
    final int port = Integer.valueOf(hostAndPort[1]);
    final Properties properties = new Properties();
    properties.put("mail.transport.protocol", "smtp");
    properties.put("mail.smtp.host", host);
    properties.put("mail.smtp.port", port);
    this.properties = properties;
    this.fromAddress = configuration.fromAddress();
    this.toAddress = configuration.toAddress();
    subjectTemplate = configuration.subjectTemplate();
  }

  @Override
  public void send(SmsMessage message) {
    try {
      final Session session = Session.getDefaultInstance(properties);
      final MimeMessage email = new MimeMessage(session);
      email.setFrom(new InternetAddress(fromAddress));
      email.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
      final String subject = subjectTemplate
          .replaceAll("\\{\\{port\\}\\}", portOf(message.number()))
          .replaceAll("\\{\\{number\\}\\}", message.number());
      email.setSubject(subject);
      email.setText(message.text());
      Transport.send(email);
      LOG.info("sent SMS over email to '{}'", message.number());
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }

  private String portOf(String number) {
    return (number != null &&
        (number.startsWith("+48") || number.startsWith("48"))) ? "7" : "5";
  }
}
