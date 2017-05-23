package net.snet.crm.service.utils;


import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Map;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
  private static final Logger log = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

  @Override
  public Response toResponse(RuntimeException exception) {
    final int status = (exception instanceof WebApplicationException) ?
        ((WebApplicationException) exception).getResponse().getStatus() :
        500;
    final String message = exception.getMessage() != null ? exception.getMessage() : "";
    final Map<String, String> error = ImmutableMap.of("detail", message);
    log.error("exception", exception);
    return Response
        .status(status)
        .entity(ImmutableMap.of("errors", error))
        .build();
  }
}
