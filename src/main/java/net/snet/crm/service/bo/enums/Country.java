package net.snet.crm.service.bo.enums;

import java.util.Locale;

/**
 * Country enumerator for customer address field.
 *
 * @author Richard Sikora
 */
public enum Country implements EnumPersistenceMapping<Country> {
    CZ(10, "enum.country.cz", "cs"), PL(20, "enum.country.pl", "pl"), SK(30,
            "enum.country.sk", "sk");

    private int fId;

    private String fName;

    private Locale locale;

    // has to declare it by hand
    private static ReverseEnumMap<Country> sReverseMap = new ReverseEnumMap<Country>(
            Country.class);

    private Country(int id, String name, String localeString) {
        fId = id;
        fName = name;
        locale = new Locale(localeString);
    }

    public String getName() {
        return fName;
    }

    public String toString() {
        return getName();
    }

    public int getId() {
        return fId;
    }

    public Country valueOf(int id) {
        return sReverseMap.get(id);
    }

    public String getShortName() {
        // return last part of country's full name
        return fName.substring(fName.lastIndexOf(".") + 1);
    }

    public Locale getLocale() {
        return locale;
    }
}
