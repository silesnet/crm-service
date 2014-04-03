package net.snet.crm.service.bo.enums;

/**
 * An interface Enum classes need to implement in order to persist correctly
 * (Enum <-> int) mapping.
 *
 * @author Richard Sikora
 */
public interface EnumPersistenceMapping<E extends Enum<E> & EnumPersistenceMapping<E>> {
    public int getId();

    public String getName();

    // not much clean but every Enumerator can return
    // other enum value by given id

    public E valueOf(int id);
}
