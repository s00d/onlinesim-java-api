package ru.onlinesim.response.get_numbers;

/**
 * NumberItemMessage resp
 */
public class NumberItemMessage {
	private final String from;
	private final String text;
	private final String code;

	public NumberItemMessage(String from, String text, String code) {
		this.from = from;
		this.text = text;
		this.code = code;
	}

	public String getFrom() {
		return from;
	}

	public String getText() {
		return text;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "Message{" +
				"name=" + from +
				", text=" + text +
				", code=" + code +
				'}';
	}
}
