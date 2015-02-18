package net.snet.crm.domain.shared;

public abstract class GenericLongId<E> implements Id<E> {
  private final long id;

  public GenericLongId(long id) {
    this.id = id;
  }

  public GenericLongId() {
    this.id = 0;
  }

  @Override
  public boolean exist() {
    return id != 0;
  }
}
