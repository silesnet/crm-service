package net.snet.crm.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;

/**
 * Created by admin on 22.12.13.
 */
public class Customer {

	private long id;
	@JsonProperty("history_id")
	private long historyId;
	@JsonProperty("public_id")
	private String publicId;
	private String name;
	@JsonProperty("supplementary_name")
	private String supplementaryName;
	private String street;
	private String city;
	@JsonProperty("postal_code")
	private String postalCode;
	private int country;
	private String email;
	private String dic;
	@JsonProperty("contract_no")
	private String contractNo;
	@JsonProperty("connection_spot")
	private String connectionSpot;
	@JsonProperty("inserted_on")
	private Timestamp insertedOn;
	private int frequency;
	@JsonProperty("lastly_billed")
	private Timestamp lastlyBilled;
	@JsonProperty("is_billed_after")
	private boolean isBilledAfter;
	@JsonProperty("deliver_by_email")
	private boolean deliverByEmail;
	@JsonProperty("deliver_copy_email")
	private String deliverCopyEmail;
	@JsonProperty("deliver_by_mail")
	private boolean deliverByMail;
	@JsonProperty("is_auto_billing")
	private boolean isAutoBilling;
	private String info;
	@JsonProperty("contact_name")
	private String contactName;
	private String phone;
	@JsonProperty("is_active")
	private boolean isActive;
	private int status;
	@JsonProperty("shire_id")
	private long shireId;
	private int format;
	@JsonProperty("deliver_signed")
	private boolean deliverSigned;
	private String symbol;
	private Timestamp updated;
	@JsonProperty("account_no")
	private String accountNo;
	@JsonProperty("bank_no")
	private String bankNo;
	private int variable;

	public Customer() {
	}

	public Customer(long id, long historyId, String publicId, String name, String supplementaryName, String street, String city, String postalCode,
					int country, String email, String dic, String contractNo, String connectionSpot, Timestamp insertedOn,
					int frequency, Timestamp lastlyBilled, Boolean isBilledAfter, Boolean deliverByEmail, String deliverCopyEmail,
					Boolean deliverByMail, Boolean isAutoBilling, String info, String contactName, String phone, Boolean isActive,
					int status, Long shireId, int format, Boolean deliverSigned, String symbol, Timestamp updated, String accountNo,
					String bankNo, int variable) {

		this.id = id;
		this.historyId = historyId;
		this.publicId = publicId;
		this.name = name;
		this.supplementaryName = supplementaryName;
		this.street = street;
		this.city = city;
		this.postalCode = postalCode;
		this.country = country;
		this.email = email;
		this.dic = dic;
		this.contractNo = contractNo;
		this.connectionSpot = connectionSpot;
		this.insertedOn = insertedOn;
		this.frequency = frequency;
		this.lastlyBilled = lastlyBilled;
		this.isBilledAfter = isBilledAfter;
		this.deliverByEmail = deliverByEmail;
		this.deliverCopyEmail = deliverCopyEmail;
		this.deliverByMail = deliverByMail;
		this.isAutoBilling = isAutoBilling;
		this.info = info;
		this.contactName = contactName;
		this.phone = phone;
		this.isActive = isActive;
		this.status = status;
		this.shireId = shireId;
		this.format = format;
		this.deliverSigned = deliverSigned;
		this.symbol = symbol;
		this.updated = updated;
		this.accountNo = accountNo;
	}

	public Customer(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getHistoryId() {
		return historyId;
	}

	public void setHistoryId(long historyId) {
		this.historyId = historyId;
	}

	public String getPublicId() {
		return publicId;
	}

	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSupplementaryName() {
		return supplementaryName;
	}

	public void setSupplementaryName(String supplementaryName) {
		this.supplementaryName = supplementaryName;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setcity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDic() {
		return dic;
	}

	public void setDic(String dic) {
		this.dic = dic;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getConnectionSpot() {
		return connectionSpot;
	}

	public void setConnectionSpot(String connectionSpot) {
		this.connectionSpot = connectionSpot;
	}

	public Timestamp getInsertedOn() {
		return insertedOn;
	}

	public void setInsertedOn(Timestamp insertedOn) {
		this.insertedOn = insertedOn;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public Timestamp getLastlyBilled() {
		return lastlyBilled;
	}

	public void setLastlyBilled(Timestamp lastlyBilled) {
		this.lastlyBilled = lastlyBilled;
	}

	public boolean getIsBilledAfter() {
		return isBilledAfter;
	}

	public void setIsBilledAfter(boolean isBilledAfter) {
		this.isBilledAfter = isBilledAfter;
	}

	public boolean getDeliverByEmail() {
		return deliverByEmail;
	}

	public void setDeliverByEmail(boolean deliverByEmail) {
		this.deliverByEmail = deliverByEmail;
	}

	public String getDeliverCopyEmail() {
		return deliverCopyEmail;
	}

	public void setDeliverCopyEmail(String deliverCopyEmail) {
		this.deliverCopyEmail = deliverCopyEmail;
	}

	public boolean getDeliverByMail() {
		return deliverByMail;
	}

	public void setDeliverByMail(boolean deliverByMail) {
		this.deliverByMail = deliverByMail;
	}

	public boolean getIsAutoBilling() {
		return isAutoBilling;
	}

	public void setIsAutoBilling(boolean isAutoBilling) {
		this.isAutoBilling = isAutoBilling;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getShireId() {
		return shireId;
	}

	public void setShireId(long shireId) {
		this.shireId = shireId;
	}

	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	public boolean getDeliverSigned() {
		return deliverSigned;
	}

	public void setDeliverSigned(boolean deliverSigned) {
		this.deliverSigned = deliverSigned;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Timestamp getUpdated() {
		return updated;
	}

	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getBankNo() {
		return bankNo;
	}

	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}

	public int getVariable() {
		return variable;
	}

	public void setVariable(int variable) {
		this.variable = variable;
	}
}
