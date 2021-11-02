package ru.onlinesim.exceptions;

import org.jetbrains.annotations.NotNull;

public enum BaseTypeError {
	UNKNOWN("", "Unknown type error."),
	NO_BALANCE("NO_BALANCE", "There is no money in the account."),
	NO_NUMBERS("NO_NUMBERS", "There are currently no numbers."),
	NO_MESSAGE("NO_MESSAGE", "empty message"),
	ERROR_WRONG_KEY("ERROR_WRONG_KEY", "wrong apikey"),
	ACCOUNT_BLOCKED("ACCOUNT_BLOCKED",  "Account blocked"),
	ERROR_NO_SERVICE("ERROR_NO_SERVICE",  "service not specified"),
	REQUEST_NOT_FOUND("REQUEST_NOT_FOUND",  "API method not specified"),
	API_ACCESS_DISABLED("API_ACCESS_DISABLED",  "api disabled"),
	API_ACCESS_IP("API_ACCESS_IP",  "access from this ip is disabled in the profile"),
	WARNING_NO_NUMS("WARNING_NO_NUMS",  "no matching numbers"),
	TZ_INPOOL("TZ_INPOOL",  "waiting for a number to be dedicated to the operation"),
	TZ_NUM_WAIT("TZ_NUM_WAIT", "waiting for response"),
	TZ_NUM_ANSWER("TZ_NUM_ANSWER", "response has arrived"),
	TZ_OVER_EMPTY("TZ_OVER_EMPTY", "response did not arrive within the specified time"),
	TZ_OVER_OK("TZ_OVER_OK", "operation has been completed"),
	ERROR_NO_TZID("ERROR_NO_TZID", "tzid is not specified"),
	ERROR_NO_OPERATIONS("ERROR_NO_OPERATIONS", "no operations"),
	EXCEEDED_CONCURRENT_OPERATIONS("EXCEEDED_CONCURRENT_OPERATIONS", "maximum quantity of numbers booked concurrently is exceeded for your account"),
	NO_NUMBER("NO_NUMBER", "temporarily no numbers available for the selected service"),
	TIME_INTERVAL_ERROR("TIME_INTERVAL_ERROR", "delayed SMS reception is not possible at this interval of time"),
	INTERVAL_CONCURRENT_REQUESTS_ERROR("INTERVAL_CONCURRENT_REQUESTS_ERROR", "maximum quantity of concurrent requests for number issue is exceeded, try again later"),
	TRY_AGAIN_LATER("TRY_AGAIN_LATER", "temporarily unable to perform the request"),
	NO_FORWARD_FOR_DEFFER("NO_FORWARD_FOR_DEFFER", "forwarding can be activated only for online reception"),
	NO_NUMBER_FOR_FORWARD("NO_NUMBER_FOR_FORWARD", "there are no numbers for forwarding"),
	ERROR_LENGTH_NUMBER_FOR_FORWARD("ERROR_LENGTH_NUMBER_FOR_FORWARD", "wrong length of the number for forwarding"),
	DUPLICATE_OPERATION("DUPLICATE_OPERATION", "adding operations with identical parameters"),
	ERROR_NO_NUMBER("ERROR_NO_NUMBER", "number is not specified"),
	ERROR_PARAMS("ERROR_PARAMS", "one or both parameters are wrong"),
	LIFICYCLE_NUM_EXPIRED("LIFICYCLE_NUM_EXPIRED", "the number has expired"),
	NEED_EXTENSION_NUMBER("NEED_EXTENSION_NUMBER", "you have to extend the number, see the Extension tab"),
	ERROR_NUMBERS_PARAMS("ERROR_NUMBERS_PARAMS", "error in the number format"),
	ERROR_WRONG_TZID("ERROR_WRONG_TZID", "error in the number format"),
	NO_COMPLETE_TZID("NO_COMPLETE_TZID", "unable to complete the operation."),
	NO_CONFIRM_FORWARD("NO_CONFIRM_FORWARD", "unable to confirm forwarding"),
	ERROR_NO_SERVICE_REPEAT("ERROR_NO_SERVICE_REPEAT", "no services for repeated reception"),
	SERVICE_TO_NUMBER_EMPTY("SERVICE_TO_NUMBER_EMPTY", "no numbers for repeated reception for this service"),
	ACCOUNT_IDENTIFICATION_REQUIRED("ACCOUNT_IDENTIFICATION_REQUIRED", "You have to go through an identification process: to order a messenger - in any way, for forward - on the passport."),
	;

	/**
	 * Description to english.
	 */
	private final String message;
	/**
	 * Specified name status.
	 */
	private final String response;

	BaseTypeError(@NotNull String response, @NotNull String message) {
		this.response = response;
		this.message = message;
	}

	/**
	 * Returns the description about status to english.
	 *
	 * @return description about status to english.
	 */
	@NotNull
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the response from server.
	 *
	 * @return response from server.
	 */
	@NotNull
	public String getResponse() {
		return response;
	}


	@Override
	public String toString() {
		return "BaseTypeError{" +
				"message='" + message + '\'' +
				", response='" + response + '\'' +
				'}';
	}
}
