package ru.onlinesim.response.get_rent;

public class RentItemMessage {
	private final int id;
	private final String service;
	private final String text;
	private final String code;
	private final String created_at;

	public RentItemMessage(int id, String service, String text, String code, String created_at) {
		this.id = id;
		this.service = service;
		this.text = text;
		this.code = code;
		this.created_at = created_at;
	}

	public int getId() {
		return id;
	}

	public String getService() {
		return service;
	}

	public String getText() {
		return text;
	}

	public String getCode() {
		return code;
	}

	public String getCreated_at() {
		return created_at;
	}


	@Override
	public String toString() {
		return "Message{" +
				"id=" + id +
				", service=" + service +
				", text=" + text +
				", code=" + code +
				", created_at=" + created_at +
				'}';
	}
}
