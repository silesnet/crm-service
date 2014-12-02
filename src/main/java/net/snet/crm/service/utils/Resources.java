package net.snet.crm.service.utils;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class Resources {
  public static void checkParam(boolean expression, @Nullable Object error) {
    try {
      Preconditions.checkArgument(expression, error);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }

  public static void checkParam(boolean expression,
      @Nullable String template,
      @Nullable Object... args) {
    try {
      Preconditions.checkArgument(expression, template, args);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }
}
