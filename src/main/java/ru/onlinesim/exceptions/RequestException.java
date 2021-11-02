package ru.onlinesim.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * RequestException
 */
public class RequestException extends Exception {
	/**
	 * Error type by server response.
	 */
	private BaseTypeError type = BaseTypeError.UNKNOWN;

	private String err = "OTHER";

	/**
	 * Message on english language.
	 */
	private String message = "";

	public RequestException() {
		this.type = BaseTypeError.UNKNOWN;
		this.message = BaseTypeError.UNKNOWN.getMessage();
	}

	@NotNull
	public RequestException(@NotNull String name) {
		super();
		this.err = name.toUpperCase();

		for (BaseTypeError c : BaseTypeError.values()) {
			if (c.getResponse().equals(this.err)) {
				this.type = c;
				this.message = c.getMessage();
				break;
			}
		}
	}

	/**
	 * Returns the message on english language.
	 *
	 * @return message on english language.
	 */
	@Override
	@NotNull
	public String getMessage() {
		return message;
	}


	/**
	 * Returns the type error response from server.
	 *
	 * @return type error response from server.
	 */
	@NotNull
	public BaseTypeError getTypeError() {
		return type;
	}

	/**
	 * Returns the type error response from server.
	 *
	 * @return type error response from server.
	 */
	@NotNull
	public String getErr() {
		return err;
	}


	@Override
	public String toString() {
		return "BaseException{" +
				"type=" + type +
				", err=" + err +
				", message='" + message + '\'' +
				'}';
	}
}
