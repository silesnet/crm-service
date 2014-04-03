package net.snet.crm.service.bo;

import net.snet.crm.service.bo.enums.Country;

public final class ServiceId {
    private static final int ONETIME_BASE = 100000000;
    private static final int COUNTRY_MASK = 10000000;
    private static final int ORDER_MASK = 100;
    private static final int FIRST_ORDER_NO = 1;
    private static final int LAST_ORDER_NO = 99;

    final private int id;
    final private Country country;
    final private ContractNo contractNo;
    final private int orderNo;
    final private boolean isOnetime;

    private ServiceId(final int id) {
        this.id = id;
        int value = id;
        if (value > ONETIME_BASE) {
            isOnetime = true;
            value -= ONETIME_BASE;
        } else {
            isOnetime = false;
        }
        int countryId = value / COUNTRY_MASK;
        if (countryId < 1 || countryId > 2)
            throw new IllegalArgumentException("unknown country id '" + countryId + "'");
        country = countryId == 1 ? Country.CZ : Country.PL;
        value %= COUNTRY_MASK;
        contractNo = new ContractNo(value / ORDER_MASK);
        orderNo = value % ORDER_MASK;
        if (orderNo == 0)
            throw new IllegalArgumentException("service order cannot be zero");
    }

    private ServiceId(final Country country, final ContractNo contractNo, final int orderNo, final boolean isOnetime) {
        this.country = country;
        this.contractNo = contractNo;
        if (orderNo < FIRST_ORDER_NO || orderNo > LAST_ORDER_NO)
            throw new IllegalArgumentException("order has to be in range [1..99], was '" + orderNo + "'");
        this.orderNo = orderNo;
        this.isOnetime = isOnetime;
        int value = isOnetime ? ONETIME_BASE : 0;
        value += Country.CZ.equals(country) ? COUNTRY_MASK : 2 * COUNTRY_MASK;
        value += contractNo.value() * ORDER_MASK;
        value += orderNo;
        this.id = value;
    }

    public static ServiceId serviceId(final int id) {
        return new ServiceId(id);
    }

    public static ServiceId firstServiceId(final Country country, final ContractNo contractNo) {
        return new ServiceId(country, contractNo, FIRST_ORDER_NO, false);
    }

    public static ServiceId firstOnetimeServiceId(final Country country, final ContractNo contractNo) {
        return new ServiceId(country, contractNo, FIRST_ORDER_NO, true);
    }

    public static ServiceId lastServiceId(final Country country, final ContractNo contractNo) {
        return new ServiceId(country, contractNo, LAST_ORDER_NO, false);
    }

    public static ServiceId lastOnetimeServiceId(final Country country, final ContractNo contractNo) {
        return new ServiceId(country, contractNo, LAST_ORDER_NO, true);
    }

    public int id() {
        return id;
    }

    public Country country() {
        return country;
    }

    public ContractNo contractNo() {
        return contractNo;
    }

    public int orderNo() {
        return orderNo;
    }

    public boolean isOnetime() {
        return isOnetime;
    }

    public boolean hasNext() {
        return orderNo < LAST_ORDER_NO;
    }

    public ServiceId next() {
        if (!hasNext())
            throw new IllegalStateException("service id does not have next id, because it is last one");
        return new ServiceId(country, contractNo, orderNo + 1, isOnetime);
    }

    public boolean isFirst() {
        return orderNo == FIRST_ORDER_NO;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("[");
        if (isOnetime())
            builder.append("onetime, ");
        builder.append("country=").append(Country.CZ.equals(country) ? "CZ, " : "PL, ")
                .append("contract=").append(contractNo.toString()).append(", ")
                .append("order=").append(orderNo)
                .append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceId serviceId = (ServiceId) o;

        if (id != serviceId.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
