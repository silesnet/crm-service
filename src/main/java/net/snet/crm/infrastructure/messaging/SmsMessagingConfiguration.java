package net.snet.crm.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SmsMessagingConfiguration {

  @Valid
  @NotNull
  @JsonProperty
  private String hostAndPort = "localhost:25";

  @Valid
  @NotNull
  @JsonProperty
  private String fromAddress = "admin@localhost";

  @Valid
  @NotNull
  @JsonProperty
  private String toAddress = "admin@localhost";

  @Valid
  @NotNull
  @JsonProperty
  private String subjectTemplate = "{{port}}-{{number}}";

  public String hostAndPort() {
    return hostAndPort;
  }

  public String fromAddress() {
    return fromAddress;
  }

  public String toAddress() {
    return toAddress;
  }

  public String subjectTemplate() {
    return subjectTemplate;
  }
}
