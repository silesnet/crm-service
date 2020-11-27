package net.snet.network.shared;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;

@Value
@Accessors(fluent = true)
public class JsonApiBody {
  List<JsonApiResource> resources;

  public boolean hasSingleResource() {
    return resources.size() == 1;
  }

  public JsonApiResource resource() {

    if (resources.size() == 0) {
      throw new IllegalStateException("there is no resource");
    }
    if (resources.size() > 1) {
      throw new IllegalStateException("there is more than one resource, resources count " + resources.size());
    }

    return resources.get(0);
  }
}
