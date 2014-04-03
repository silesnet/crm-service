package net.snet.crm.service.bo.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Map for fast id -> enum mapping.
 *
 * @author Richard Sikora
 */
public class ReverseEnumMap<E extends Enum<E> & EnumPersistenceMapping<E>> {
    private Map<Integer, E> reverseMap = new HashMap<Integer, E>();

    public ReverseEnumMap(Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants())
            reverseMap.put(e.getId(), e);
    }

    public E get(int id) {
        return reverseMap.get(id);
    }
}