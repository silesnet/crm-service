package net.snet.crm.domain.shared;

public abstract class GenericLongId<E> implements Id<E> {
  final long id;

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

  public long value() {
    return id;
  }
}
