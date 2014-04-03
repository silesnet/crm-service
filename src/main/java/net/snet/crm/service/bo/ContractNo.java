package net.snet.crm.service.bo;

public final class ContractNo {
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 99999;
    final private int contractNo;
    final private String value;

    public ContractNo(final int contractNo) {
        if (contractNo < MIN_VALUE)
            throw new IllegalArgumentException("contract number value too low '" + contractNo + "'");
        if (contractNo > MAX_VALUE)
            throw new IllegalArgumentException("contract number value too big '" + contractNo + "'");
        this.contractNo = contractNo;
        this.value = "" + contractNo;
    }

    public ContractNo(final String contractNo) {
        this(Integer.valueOf(contractNo));
    }

    public static ContractNo contractNo(final int contractNo) {
        return new ContractNo(contractNo);
    }

    public static ContractNo contractNo(final String contractNo) {
        return new ContractNo(contractNo);
    }

    public int value() {
        return contractNo;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractNo that = (ContractNo) o;
        if (contractNo != that.contractNo) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return contractNo;
    }
}
