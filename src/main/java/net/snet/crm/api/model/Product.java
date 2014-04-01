package net.snet.crm.api.model;

/**
 * Created by admin on 22.12.13.
 */
public class Product {

	private long id;
	private String name;
	private int download;
	private int upload;
	private int price;

	public Product() {
	}

	public Product(long id, String name, int download, int upload, int price) {
		this.id = id;
		this.name = name;
		this.download = download;
		this.upload = upload;
		this.price = price;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Product)) {
			return false;
		}

		Product region = (Product) o;

		if (id != region.id) {
			return false;
		}
		if (!name.equals(region.name)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "Region{"
						+ "id=" + id
						+ ", name='" + name + '\''
						+ '}';
	}
}
