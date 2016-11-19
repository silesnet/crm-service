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
  public boolean isPersisted() {
    return id != 0;
  }

  public long value() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GenericLongId<?> that = (GenericLongId<?>) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }

  @Override
  public String toString() {
    return "id=" + id;
  }
}
