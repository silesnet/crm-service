package net.snet.crm.infrastructure.messaging;

/**
 * Created by Richard on 01.08.2016.
 */
public interface MessagingService {
  void send(SmsMessage message);
}
