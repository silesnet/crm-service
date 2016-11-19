package net.snet.crm.domain.shared;

import java.io.Serializable;

public interface Id<E> extends Serializable {
  boolean isPersisted();
}
