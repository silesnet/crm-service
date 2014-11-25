package net.snet.crm.service.utils;


import com.google.common.collect.ImmutableMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Map;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
  @Override
  public Response toResponse(RuntimeException exception) {
    final int status = (exception instanceof WebApplicationException) ?
        ((WebApplicationException) exception).getResponse().getStatus() :
        500;
    final Map<String, String> error = ImmutableMap.of("detail", exception.getMessage());
    return Response
        .status(status)
        .entity(ImmutableMap.of("errors", error))
        .build();
  }
}
