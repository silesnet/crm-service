package net.snet.crm.service.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class Service {

    private long id;
    @JsonProperty
    private long customerId;
    @JsonProperty
    private Timestamp periodFrom;
    @JsonProperty
    private Timestamp periodTo;
    private String name;
    private int price;
    private int frequency;
    private int  download;
    private int  upload;
    @JsonProperty
    private boolean aggregated;
    private String info;
    @JsonProperty
    private long replaceId;
    @JsonProperty
    private String additionalName;
    private String bps;
    @JsonProperty
    private long oldId;

    public Service(long id) {
        this.id = id;
    }

    public Service(long id, long customerId, Timestamp periodFrom, Timestamp periodTo, String name, int price, int frequency, int download, int upload, boolean aggregated, String info, long replaceId, String additionalName, String bps, long oldId) {
        this.id = id;
        this.customerId = customerId;
        this.periodFrom = periodFrom;
        this.periodTo = periodTo;
        this.name = name;
        this.price = price;
        this.frequency = frequency;
        this.download = download;
        this.upload = upload;
        this.aggregated = aggregated;
        this.info = info;
        this.replaceId = replaceId;
        this.additionalName = additionalName;
        this.bps = bps;
        this.oldId = oldId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public Timestamp getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(Timestamp periodFrom) {
        this.periodFrom = periodFrom;
    }

    public Timestamp getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(Timestamp periodTo) {
        this.periodTo = periodTo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getDownload() {
        return download;
    }

    public void setDownload(int download) {
        this.download = download;
    }

    public int getUpload() {
        return upload;
    }

    public void setUpload(int upload) {
        this.upload = upload;
    }

    @JsonProperty("isAggregated")
    public boolean isAggregated() {
        return aggregated;
    }

    @JsonProperty("isAggregated")
    public void setAggregated(boolean aggregated) {
        this.aggregated = aggregated;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public long getReplaceId() {
        return replaceId;
    }

    public void setReplaceId(long replaceId) {
        this.replaceId = replaceId;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public void setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
    }

    public String getBps() {
        return bps;
    }

    public void setBps(String bps) {
        this.bps = bps;
    }

    public long getOldId() {
        return oldId;
    }

    public void setOldId(long oldId) {
        this.oldId = oldId;
    }
}
