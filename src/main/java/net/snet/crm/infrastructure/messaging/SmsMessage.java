package net.snet.crm.infrastructure.messaging;

public class SmsMessage {
  private final String number;
  private final String text;

  public SmsMessage(String number, String text) {
    this.number = number;
    this.text = text;
  }

  String text() {
    return text;
  }
  String number() {
    return number;
  }


}
