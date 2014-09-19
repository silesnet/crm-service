package net.snet.crm.service.bo;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class Draft {

	private Long id;
	private String type;
	private String userId;
	private String data;
	private String status;

	public Draft(Long id, String type, String userId, String data, String status) {
		this.id = id;
		this.type = type;
		this.userId = userId;
		this.data = data;
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Draft)) return false;

		Draft draft = (Draft) o;

		if (data != null ? !data.equals(draft.data) : draft.data != null) return false;
		if (id != null ? !id.equals(draft.id) : draft.id != null) return false;
		if (status != null ? !status.equals(draft.status) : draft.status != null) return false;
		if (type != null ? !type.equals(draft.type) : draft.type != null) return false;
		if (userId != null ? !userId.equals(draft.userId) : draft.userId != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (userId != null ? userId.hashCode() : 0);
		result = 31 * result + (data != null ? data.hashCode() : 0);
		result = 31 * result + (status != null ? status.hashCode() : 0);
		return result;
	}
}
