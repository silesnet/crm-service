package net.snet.crm.domain.shared;

public interface Repository<E> {
  E add(E entity);

  E get(Id<E> id);

  E update(E entity);
}
