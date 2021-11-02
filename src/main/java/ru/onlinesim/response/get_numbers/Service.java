package ru.onlinesim.response.get_numbers;

/**
 * Service resp
 */
public class Service {
	private final int id;
	private final int count;
	private final int price;
	private final String service;
	private final String slug;

	public Service(int id, int count, int price, String service, String slug) {
		this.id = id;
		this.count = count;
		this.price = price;
		this.service = service;
		this.slug = slug;
	}

	public int getId() {
		return id;
	}

	public int getCount() {
		return count;
	}

	public int getPrice() {
		return price;
	}

	public String getService() {
		return service;
	}

	public String getSlug() {
		return slug;
	}

	@Override
	public String toString() {
		return "Service{" +
				"id=" + id +
				", count=" + count +
				", price=" + price +
				", service=" + service +
				", slug=" + slug +
				'}';
	}
}
