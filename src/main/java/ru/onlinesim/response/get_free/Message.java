package ru.onlinesim.response.get_free;

public class Message {
	private final String text;
	private final String in_number;
	private final String created_at;

	public Message(String text, String in_number, String created_at) {
		this.text = text;
		this.in_number = in_number;
		this.created_at = created_at;
	}

	public String getText() {
		return text;
	}

	public String getIn_number() {
		return in_number;
	}

	public String getCreated_at() {
		return created_at;
	}

	@Override
	public String toString() {
		return "Message{" +
				"text=" + text +
				", in_number=" + in_number +
				", created_at=" + created_at +
				'}';
	}
}
